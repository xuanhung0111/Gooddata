package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.md.Dataset;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.InsightItem;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.lcm.LCMServiceProject;
import com.gooddata.qa.utils.lcm.LcmRestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DashboardsDistributedByLcmTest extends AbstractProjectTest {

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    private final String FIRST_DASHBOARD = "dashboard_1_" + generateHashString();
    private final String SECOND_DASHBOARD = "dashboard_2_" + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "Client project " + generateHashString();
    private final String FIRST_TEST_INSIGHT = "ATT-Test-Insight1_" + generateHashString();
    private final String SECOND_TEST_INSIGHT = "ATT-Test-Insight2_" + generateHashString();

    private String devProjectId;
    private String clientProjectId;
    private LCMServiceProject lcmServiceProject;
    private JSONArray releaseSegments;
    private JSONObject datasource;
    private JSONArray segmentFilters;

    @Override
    protected void initProperties() {
        appliedFixture = GOODSALES;
        validateAfterClass = false;
        projectTitle = "KPI Dashboards with LCM";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        String numberKpiUri = createNumOfActivitiesKpi();
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        String insightUri = indigoRestRequest.createInsight(
                new InsightMDConfiguration(FIRST_TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(SECOND_TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        indigoRestRequest.createAnalyticalDashboard(
                asList(numberKpiUri, indigoRestRequest.createVisualizationWidget(
                        insightUri, FIRST_TEST_INSIGHT)), FIRST_DASHBOARD);
        indigoRestRequest.createAnalyticalDashboard(
                asList(numberKpiUri, indigoRestRequest.createVisualizationWidget(
                        insightUri, FIRST_TEST_INSIGHT)), SECOND_DASHBOARD);

        lcmServiceProject = LCMServiceProject.newWorkFlow(testParams);
        devProjectId = testParams.getProjectId();
        clientProjectId = createProjectUsingFixture(CLIENT_PROJECT_TITLE, ResourceTemplate.GOODSALES,
                testParams.getDomainUser());

        log.info("------dev project id:" + devProjectId);
        log.info("------client project id:" + clientProjectId);

        releaseSegments = new JSONArray() {{
            put(new JSONObject() {{
                put("segment_id", SEGMENT_ID);
                put("development_pid", devProjectId);
                put("driver", testParams.getProjectDriver().getValue());
                put("master_name", "Master of " + SEGMENT_ID);
            }});
        }};
        datasource = lcmServiceProject.createProvisionDatasource(SEGMENT_ID, CLIENT_ID, clientProjectId);
        segmentFilters = new JSONArray() {{
            put(SEGMENT_ID);
        }};

        testParams.setProjectId(clientProjectId);
        addUsersToClientProject();
    }

    @Test(dependsOnGroups = "createProject")
    public void testSyncLockedFlag() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), devProjectId);

        indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 1);
        indigoRestRequest.setLockedAttribute(SECOND_DASHBOARD, 0);

        int lockedValue = indigoRestRequest.getLockedAttribute(FIRST_DASHBOARD);
        assertEquals(lockedValue, 1, FIRST_DASHBOARD + " should be locked");
        lockedValue = indigoRestRequest.getLockedAttribute(SECOND_DASHBOARD);
        assertEquals(lockedValue, 0, SECOND_DASHBOARD + " should not be locked");

        runLcmFlow();

        indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);

        lockedValue = indigoRestRequest.getLockedAttribute(FIRST_DASHBOARD);
        assertEquals(lockedValue, 1, FIRST_DASHBOARD + " should be locked");
        lockedValue = indigoRestRequest.getLockedAttribute(SECOND_DASHBOARD);
        assertEquals(lockedValue, 1, SECOND_DASHBOARD + " should be locked");
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testKpiDashboardsWithAdminRole() {
        logoutAndLoginAs(true, UserRoles.ADMIN);
        initIndigoDashboardsPageWithWidgets();
        indigoDashboardsPage.selectKpiDashboard(FIRST_DASHBOARD);
        assertThat("Admin should be able to edit dashboard", indigoDashboardsPage.isEditButtonVisible());
        indigoDashboardsPage.switchToEditMode();
        indigoDashboardsPage.getInsightSelectionPanel().switchFilter(FilterType.ALL);
        indigoDashboardsPage.addInsight(SECOND_TEST_INSIGHT).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), SECOND_TEST_INSIGHT);
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightsWithAdminRole() {
        AnalysisPage analysisPage = initAnalysePage();
        InsightItem insightItem = analysisPage.getPageHeader()
                .expandInsightSelection().switchFilter(FilterType.ALL)
                .getInsightItem(FIRST_TEST_INSIGHT);
        assertThat("VisualizationsDropdown should have a locked icon", insightItem.hasLockedIcon());
        assertThat("EditorHeader should have a locked icon", analysisPage.openInsight(FIRST_TEST_INSIGHT)
                .getPageHeader().hasLockedIcon());
        analysisPage.getPageHeader().setInsightTitle("new insight");
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), "new insight");
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testDefaultDataProduce() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(FIRST_TEST_INSIGHT))).get(0);
        AnalysisPage analysisPage = initAnalysePage().openInsight(FIRST_TEST_INSIGHT);

        String urlUtilizeClientId = browser.getCurrentUrl().replace(clientProjectId,
                String.format("client/%s:%s", LcmRestUtils.ATT_LCM_DATA_PRODUCT, CLIENT_ID));
        openUrl(urlUtilizeClientId);
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);

        openUrl(format("/analyze/#/client/%s:%s/%s/edit", LcmRestUtils.ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);

        openUrl(format("/analyze/#/%s/%s/edit", clientProjectId, identifier));
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testKpiDashboardsWithEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Editor shouldnot be able to edit dashboard");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightsWithEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            AnalysisPage analysisPage = initAnalysePage();
            InsightItem insightItem = analysisPage.getPageHeader()
                    .expandInsightSelection()
                    .switchFilter(FilterType.ALL)
                    .getInsightItem(SECOND_TEST_INSIGHT);
            assertTrue(insightItem.hasLockedIcon(), "VisualizationsDropdown should have a locked icon");
            assertFalse(insightItem.hasVisibleDeleteButton(), "VisualizationsDropdown should have a locked icon");
            assertTrue(analysisPage.openInsight(SECOND_TEST_INSIGHT).waitForReportComputing()
                    .getPageHeader().hasLockedIcon(), "EditorHeader should have a locked icon");
            assertTrue(analysisPage.getPageHeader().isSaveAsButtonEnabled(), "Editor should able to save as");
            assertFalse(analysisPage.getPageHeader().isInsightTitleEditable(), "Editor should not be able to edit title");
            analysisPage.getPageHeader().saveInsightAs("new insight");
            assertEquals(analysisPage.getPageHeader().getInsightTitle(), "new insight");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testWithViewer() {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Viewer shouldnot be able to edit dashboard");
            indigoDashboardsPage.selectKpiDashboard(SECOND_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Viewer shouldnot be able to edit dashboard");
            assertFalse(ElementUtils.isElementVisible(By.cssSelector(".gd-header-menu-item.s-menu-analyze"), browser),
                    "Viewer should not see AD button");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testUnlockDashboards() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);
        try {
            indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 0);

            logoutAndLoginAs(true, UserRoles.EDITOR);
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertTrue(indigoDashboardsPage.isEditButtonVisible(), "Editor should be able to edit dashboard");

            logoutAndLoginAs(true, UserRoles.VIEWER);
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Editor should not be able to edit dashboard");
        } finally {
            indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 1);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        log.info("--------------start cleanup lcm service");
        lcmServiceProject.cleanUp(testParams.getUserDomain());
    }

    private void runLcmFlow() {
        lcmServiceProject.release(releaseSegments);
        log.info("----finished releasing--------------");

        lcmServiceProject.provision(segmentFilters, datasource);
        log.info("----finished provisioning--------------");

        lcmServiceProject.rollout(segmentFilters);
        log.info("----finished rolling--------------");
    }

    private void addUsersToClientProject() throws JSONException, IOException {
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        addUserToProject(testParams.getViewerUser(), UserRoles.VIEWER);
    }

    private String createNumOfActivitiesKpi() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES);
        } catch (ObjNotFoundException e) {
            getMetricCreator().createNumberOfActivitiesMetric();
        }

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getTitle())
                .metric(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
    }

    private Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    private String createKpiUsingRest(final KpiMDConfiguration kpiConfig) {
        String kpiWidget;
        try {
            kpiWidget = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .createKpiWidget(kpiConfig);
        } catch (JSONException | IOException e) {
            throw new RuntimeException("There is error while create Kpi Widget");
        }
        return kpiWidget;
    }

    private String getDateDatasetUri(final String dataset) {
        return getMdService().getObjUri(getProject(), Dataset.class, title(format("Date (%s)", dataset)));
    }
}