package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.enums.DateRange.THIS_YEAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_TASK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.ATT_LCM_DATA_PRODUCT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class RenderingAnalyticalDashboardUsingClientIdTest extends AbstractProjectTest {

    protected boolean useK8sExecutor = false;

    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "Client project " + generateHashString();
    private static final String EMAIL = "Email";
    private static final String IN_PERSON_MEETING = "In Person Meeting";
    private static final String PHONE_CALL = "Phone Call";
    private static final String WEB_MEETING = "Web Meeting";

    private String devProjectId;
    private String clientProjectId;

    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void initProperties() {
        appliedFixture = GOODSALES;
        validateAfterClass = false;
        projectTitle = "KPI Dashboards with LCM";
    }

    @Override
    protected void customizeProject() throws Throwable {
        devProjectId = testParams.getProjectId();
        clientProjectId = createProjectUsingFixture(CLIENT_PROJECT_TITLE, ResourceTemplate.GOODSALES,
                testParams.getDomainUser());

        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        lcmBrickFlowBuilder.setSegmentId(SEGMENT_ID).setClientId(CLIENT_ID)
                .setDevelopProject(devProjectId).setClientProjects(clientProjectId).buildLcmProjectParameters();

        System.out.println("*******************************************************");
        System.out.println(format("* dev project id: %s    *", devProjectId));
        System.out.println(format("* client project id: %s *", clientProjectId));
        System.out.println("*******************************************************");
        lcmBrickFlowBuilder.runLcmFlow();

        testParams.setProjectId(clientProjectId);
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void supportProjectIdAndDashboardIdWithDataProductOnEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardId = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle).split("/obj/")[1];
        openUrl(format("/dashboards/embedded/#/project/%s/dashboardId/%s?showNavigation=true", clientProjectId, dashboardId));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdentifierWithDataProductOnEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);
        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndDashboardIdWithDataProductOnEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardId = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle).split("/obj/")[1];
        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboardId/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, dashboardId));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportProjectIdAndDashboardIdWithDataProductOnNonEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardId = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle).split("/obj/")[1];
        openUrl(format("/dashboards/#/project/%s/dashboardId/%s?showNavigation=true", clientProjectId, dashboardId));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdentifierWithDataProductOnNonEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndDashboardIdWithDataProductOnNonEmbeddedMode() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardId = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle).split("/obj/")[1];
        openUrl(format("/dashboards/#/product/%s/client/%s/dashboardId/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, dashboardId));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), analyzeDashboardTitle);

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void openOtherDashboardOnEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String otherAnalyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        createAnalyticalDashboard(otherAnalyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.selectKpiDashboard(otherAnalyzeDashboardTitle).waitForWidgetsLoading()
                .getDashboardTitle(), otherAnalyzeDashboardTitle);
        assertEquals(indigoDashboardsPage.getInsightTitles(), singletonList(widgetTitle));

        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void filterAnalyticalDashboardOnEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectLastWidget(Insight.class);
        indigoDashboardsPage.getConfigurationPanel().enableDateFilter();
        indigoDashboardsPage.waitForDashboardLoad()
                .saveEditModeWithWidgets()
                .selectDateFilterByName(THIS_YEAR.toString());
        assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).isEmptyValue(),
                "Should be no data for filter selection");
    }

    @Test(dependsOnGroups = "createProject")
    public void addAndDeleteAnalyticalDashboardOnEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        indigoDashboardsPage.addDashboard();
        assertEquals(indigoDashboardsPage.getDashboardTitle(), "Untitled");
        indigoDashboardsPage.cancelEditModeWithoutChange();
        indigoDashboardsPage.selectKpiDashboard(analyzeDashboardTitle);
        indigoDashboardsPage.switchToEditMode().deleteDashboard(true);
        assertThat(indigoDashboardsPage.getDashboardTitles(), not(hasItem(analyzeDashboardTitle)));
    }

    @Test(dependsOnGroups = "createProject")
    public void editAnalyticalDashboardOnEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String otherAnalyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/embedded/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage indigoDashboardsPage = IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode()
                .changeDashboardTitle(otherAnalyzeDashboardTitle)
                .addAttributeFilter(ATTR_ACTIVITY_TYPE, EMAIL);
        assertEquals(indigoDashboardsPage.getDashboardTitle(), otherAnalyzeDashboardTitle);
        ChartReport chartReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        assertEquals(chartReport.getAxisLabels(), singletonList(EMAIL));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void switchPageOnNonEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage.getInstance(browser).waitForDashboardLoad();
        DashboardsPage dashboardsPage = initDashboardsPage();
        assertTrue(dashboardsPage.isEmptyDashboard(), "Should move to empty dashboard");
    }

    @Test(dependsOnGroups = "createProject")
    public void switchProjectOnNonEmbeddedKDUrlUsingClientIdAndIdentifier() throws IOException {
        final String widgetTitle = "Widget-" + generateHashString();
        final String insightTitle = "Insight-" + generateHashString();
        final String analyzeDashboardTitle = "KD-" + generateHashString();
        final String insightUri = createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        final String dashboardUri = createAnalyticalDashboard(analyzeDashboardTitle, insightUri, widgetTitle);
        final String identifier = getObjIdentifiers(singletonList(dashboardUri)).get(0);

        openUrl(format("/dashboards/#/product/%s/client/%s/dashboard/%s?showNavigation=true",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        IndigoDashboardsPage.getInstance(browser).waitForWidgetsLoading();
        testParams.setProjectId(devProjectId);
        try {
            initDashboardsPage();
            assertTrue(browser.getCurrentUrl().contains(devProjectId), "Should switch to project " + devProjectId);
        } finally {
            testParams.setProjectId(clientProjectId);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }

    private Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    private String createInsightHasAttributeOnStackByAndViewBy(String title, String metric, String viewBy, String stack) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(viewBy),
                                        CategoryBucket.Type.VIEW),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.STACK))));
    }

    private String createAnalyticalDashboard(String title, String insightUrl, String titleWidget) throws IOException {
        return indigoRestRequest.createAnalyticalDashboard(
                singletonList(indigoRestRequest.createVisualizationWidget(insightUrl, titleWidget)),
                title);
    }
}
