package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TreeMapChartReportAdvancedTest extends AbstractAnalyseTest {
    private final String INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT = "Insight Has Metric Attribute Segment" + generateHashString();
    private final String INSIGHT_HAS_METRIC_AND_ATTRIBUTE = "Insight Has Metric And Attribute" + generateHashString();
    private final String INSIGHT_TEST = "Insight test" + generateHashString();
    private IndigoRestRequest indigoRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private static final String DF_VARIABLE_NAME = "DF-Variable-Name";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "TreeMap Chart Report Advanced ";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createOppFirstSnapshotMetric();
        metrics.createNumberOfActivitiesMetric();

        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, true);

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTotalsResultWithMUF() throws ParseException, JSONException, IOException {
        createInsightHasSingleMetricAndSingleAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTE,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT);

        final String newInsight = "TreeMap with MUF";
        final String productValues = format("[%s]",
                getMdService().getAttributeElements(getAttributeByTitle(ATTR_DEPARTMENT)).get(1).getUri());

        final String expression = format("[%s] IN (%s)", getAttributeByTitle(ATTR_DEPARTMENT).getUri(), productValues);
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());

        final String mufUri = dashboardRestRequest.createMufObjectByUri("muf", expression);

        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String assignedMufUserId = userManagementRestRequest
                .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());

        dashboardRestRequest.addMufToUser(assignedMufUserId, mufUri);

        AnalysisPage analysisPage = initAnalysePage();
        ChartReport chartReport = analysisPage.openInsight(INSIGHT_HAS_METRIC_AND_ATTRIBUTE).getChartReport();
        analysisPage.saveInsightAs(newInsight);

        assertEquals(chartReport.getDataLabels(), asList("Direct Sales (101,054)", "Inside Sales (53,217)"));
        assertEquals(chartReport.getTrackersCount(), 2);

        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            chartReport = initAnalysePage().openInsight(newInsight).getChartReport();
            Screenshots.takeScreenshot(browser, "Check totals results with MUF", getClass());

            assertEquals(chartReport.getDataLabels(), asList("Inside Sales (53,217)"));
            assertEquals(chartReport.getTrackersCount(), 1);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testInsightWithVariable() throws ParseException, JSONException, IOException {
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String getUserEditorUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());

        VariableRestRequest request = new VariableRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String variableUri = request.createFilterVariable(DF_VARIABLE_NAME, request.getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri(),
                asList("Email", "Phone Call", "Web Meeting"));

        initVariablePage().openVariableFromList(DF_VARIABLE_NAME)
                .selectUserSpecificAttributeValues(getUserEditorUri, asList("Email", "Phone Call")).saveChange();
        Screenshots.takeScreenshot(browser, "Create Variable", getClass());

        String activityTypeUri = request.getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri();
        createMetric("METRIC TEST", format("SELECT COUNT([%s]) WHERE [%s]", activityTypeUri, variableUri), "#,##0");

        initMetricPage().openMetricDetailPage("METRIC TEST").openPermissionSettingDialog().setVisibility(true).save();
        Screenshots.takeScreenshot(browser, "Check Metric", getClass());

        ChartReport chartReport = initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing()
                .addMetric("METRIC TEST").waitForReportComputing().getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("METRIC TEST (3)"));
        Screenshots.takeScreenshot(browser, "Check Insight With Custom Metric", getClass());

        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            chartReport = initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing()
                    .addMetric("METRIC TEST").waitForReportComputing().getChartReport();
            assertEquals(chartReport.getDataLabels(), asList("METRIC TEST (2)"));
            Screenshots.takeScreenshot(browser, "Check Insight With Custom Metric For User Editor", getClass());

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testTreeMapWithImportedAndExportedProject() {
        createInsightHasSingleMetricAndSingleAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        String exportToken = exportProject(true, true, true, DEFAULT_PROJECT_CHECK_LIMIT);

        testParams.setProjectId(targetProjectId);

        try {
            importProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_METRIC_AND_ATTRIBUTE)
                    .waitForReportComputing().getChartReport();
            Screenshots.takeScreenshot(browser, "testTreeMapWithImportedAndExportedProject", getClass());

            assertEquals(chartReport.getDataLabels(), asList("Email (33,920)", "In Person Meeting (35,975)",
                    "Phone Call (50,780)", "Web Meeting (33,596)"));
            assertEquals(chartReport.getTrackersCount(), 4);
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Activity Type", "Email"), asList("# of Activities", "33,920")));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testTreeMapWithImportedAndExportedProject"})
    protected void testTreeMapWithImportedAndExportedPartialProject() {
        String insight = createInsightHasSingleMetricAndSingleAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);

        testParams.setProjectId(targetProjectId);

        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_METRIC_AND_ATTRIBUTE)
                    .waitForReportComputing().getChartReport();
            takeScreenshot(browser, "testTreeMapWithImportedAndExportedPartialProject", getClass());

            assertEquals(chartReport.getDataLabels(), asList("Email (33,920)", "In Person Meeting (35,975)",
                    "Phone Call (50,780)", "Web Meeting (33,596)"));
            assertEquals(chartReport.getTrackersCount(), 4);
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Activity Type", "Email"), asList("# of Activities", "33,920")));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsOnKD() {
        createInsightHasSingleMetricAndMultiAttributesOnViewByAndSegmentBy(INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        createInsightHasSingleMetricAndSingleAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT)
                .addInsight(INSIGHT_HAS_METRIC_AND_ATTRIBUTE).selectDateFilterByName("All time").waitForWidgetsLoading();
        assertTrue(indigoDashboardsPage.searchInsight(INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT),
                INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT + " Insight should be existed");

        Widget firstWidget = indigoDashboardsPage.getFirstWidget(Insight.class);
        Widget lastWidget = indigoDashboardsPage.getLastWidget(Insight.class);

        indigoDashboardsPage.dragWidget(firstWidget, lastWidget, Widget.DropZone.NEXT);
        assertEquals(indigoDashboardsPage.getInsightTitles(),
                asList(INSIGHT_HAS_METRIC_AND_ATTRIBUTE, INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT));
        firstWidget.setHeadline("Change Name Insight");
        assertEquals(firstWidget.getHeadline(), "Change Name Insight");

        indigoDashboardsPage.getLastWidget(Insight.class);
        indigoDashboardsPage.deleteInsightItem();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(INSIGHT_HAS_METRIC_ATTRIBUTE_SEGMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithDateAndAttributeFilterOnKD() {
        initAnalysePage().addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing().saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY, "Email with 1 Source Consulting on Apr-27-08");
        indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_TEST).clickOnContent();
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertEquals(configurationPanel.getErrorMessage(), "The insight cannot be filtered by Date. Unselect the check box.");
        configurationPanel.disableDateFilter();
        assertEquals(configurationPanel.getErrorMessage(), "The insight cannot be filtered by Activity. Unselect the check box.");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithDateFilterOnAllMeasure() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "06/09/2010", "07/08/2019");

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "06/09/2010", "07/08/2019");

        analysisPage.saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.clickDashboardBody();
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel isn't checked");
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel is disabled, unchangeable");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithInsightHasDateSomeMeasureAndSomeMeasureNotDate() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "06/09/2010", "07/08/2019");

        analysisPage.saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).selectDateFilterByName("All time")
                .waitForWidgetsLoading().clickDashboardBody();
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel is checked");
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel isn't disabled, unchangeable");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithInsightHasNotDateOnMultiMeasure() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing().saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).selectDateFilterByName("All time")
                .waitForWidgetsLoading().clickDashboardBody();
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel is checked");
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel isn't disabled, unchangeable");
    }

    private String createInsightHasSingleMetricAndMultiAttributesOnViewByAndSegmentBy(String title, String metric,
                                                                                      String attribute, String segment) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TREE_MAP)
                        .setMeasureBucket(asList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(segment), CategoryBucket.Type.SEGMENT))));
    }

    private String createInsightHasSingleMetricAndSingleAttribute(String title, String metric, String attribute) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TREE_MAP)
                        .setMeasureBucket(singletonList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.ATTRIBUTE))));
    }
}
