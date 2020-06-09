package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_TASK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.ATT_LCM_DATA_PRODUCT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class RenderingInsightUsingClientIdTest extends AbstractProjectTest {

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

        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(useK8sExecutor);
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
    public void supportProjectIdAndIdentifierDefaultDataProductOnEmbeddedMode() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/%s/%s/edit", clientProjectId, identifier));
        ChartReport chartReport = AnalysisPage.getInstance(browser).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdentifierWithDataProductOnEmbeddedMode() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        ChartReport chartReport = AnalysisPage.getInstance(browser).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdInsightWithDataProductOnEmbeddedMode() {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        AnalysisPage analysisPage = initAnalysePage().openInsight(insightTitle);
        String urlUtilizeClientId = browser.getCurrentUrl().replace("#/" + clientProjectId,
                String.format("embedded/#/client/%s:%s", ATT_LCM_DATA_PRODUCT, CLIENT_ID));
        openUrl(urlUtilizeClientId);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportProjectIdAndIdentifierWithDataProductOnNonEmbeddedMode() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/#/%s/%s/edit", clientProjectId, identifier));
        ChartReport chartReport = AnalysisPage.getInstance(browser).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdentifierWithDataProductOnNonEmbeddedMode() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        ChartReport chartReport = AnalysisPage.getInstance(browser).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void supportClientIdAndIdInsightWithDataProductOnNonEmbeddedMode() {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        AnalysisPage analysisPage = initAnalysePage().openInsight(insightTitle);
        String urlUtilizeClientId = browser.getCurrentUrl().replace(clientProjectId,
                String.format("client/%s:%s", ATT_LCM_DATA_PRODUCT, CLIENT_ID));
        openUrl(urlUtilizeClientId);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, IN_PERSON_MEETING, PHONE_CALL, WEB_MEETING));
        assertEquals(chartReport.getXaxisTitle(), ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getYaxisTitle(), METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = "createProject")
    public void openOtherInsightOnEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        final String otherInsight = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        createInsightHasAttributeOnStackByAndViewBy(
                otherInsight, METRIC_AMOUNT, ATTR_DEPARTMENT, ATTR_IS_ACTIVE);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser).saveInsight().openInsight(otherInsight);
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), otherInsight);
        ChartReport chartReport = AnalysisPage.getInstance(browser).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getXaxisTitle(), ATTR_DEPARTMENT);
        assertEquals(chartReport.getYaxisTitle(), METRIC_AMOUNT);
        assertEquals(chartReport.getChartType(), ReportType.COLUMN_CHART.getLabel());
    }

    @Test(dependsOnGroups = "createProject")
    public void createAndDeleteInsightOnEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();
        analysisPageHeader.resetToBlankState();
        assertEquals(analysisPageHeader.getInsightTitle(), "Untitled insight");

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisInsightSelectionPanel analysisInsightSelectionPanel = AnalysisPage.getInstance(browser)
                .getPageHeader().expandInsightSelection();
        analysisInsightSelectionPanel.deleteInsight(insightTitle);
        assertFalse(analysisPageHeader.expandInsightSelection().isExist(insightTitle), "Insight should be removed");
    }

    @Test(dependsOnGroups = "createProject")
    public void editInsightOnEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        final String otherInsight = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader().setInsightTitle(otherInsight);
        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing().replaceStack(ATTR_IS_CLOSED).saveInsight();
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);
        assertEquals(analysisPageHeader.getInsightTitle(), otherInsight);
    }

    @Test(dependsOnGroups = "createProject")
    public void filterInsightOnEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/embedded/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser).waitForReportComputing();
        analysisPage.addFilter(ATTR_ACTIVITY_TYPE).setFilterIsValues(ATTR_ACTIVITY_TYPE, EMAIL, PHONE_CALL);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getAxisLabels(), asList(EMAIL, PHONE_CALL));

        analysisPage.addDateFilter().getFilterBuckets().configDateFilter("01/01/2015", "01/01/2017");
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList("TRUE", "6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void switchPageOnNonEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage.getInstance(browser).waitForReportComputing();
        DashboardsPage dashboardsPage = initDashboardsPage();
        assertTrue(dashboardsPage.isEmptyDashboard(), "Should move to empty dashboard");
    }

    @Test(dependsOnGroups = "createProject")
    public void switchProjectOnNonEmbeddedADUrlUsingClientIdAndIdentifier() throws IOException {
        final String insightTitle = "Insight" + generateHashString();
        createInsightHasAttributeOnStackByAndViewBy(
                insightTitle, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_IS_TASK);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        openUrl(format("/analyze/#/client/%s:%s/%s/edit", ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        AnalysisPage.getInstance(browser).waitForReportComputing();
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

    private void createInsightHasAttributeOnStackByAndViewBy(String title, String metric, String viewBy, String stack) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(viewBy),
                                        CategoryBucket.Type.VIEW),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.STACK))));
    }
}
