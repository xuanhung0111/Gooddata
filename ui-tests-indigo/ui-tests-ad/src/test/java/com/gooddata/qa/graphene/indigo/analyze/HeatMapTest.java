package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect.DateDimensionGroup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
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
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_TIMELINE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static java.lang.String.format;

public class HeatMapTest extends AbstractAnalyseTest {
    private List<String> listReportType = asList(
            ReportType.TABLE.getFormat(), ReportType.COLUMN_CHART.getFormat(), ReportType.BAR_CHART.getFormat(),
            ReportType.LINE_CHART.getFormat(), ReportType.STACKED_AREA_CHART.getFormat(), ReportType.COMBO_CHART.getFormat(),
            ReportType.HEAD_LINE.getFormat(), ReportType.SCATTER_PLOT.getFormat(), ReportType.BUBBLE_CHART.getFormat(),
            ReportType.PIE_CHART.getFormat(), ReportType.DONUT_CHART.getFormat(), ReportType.TREE_MAP.getFormat(),
            ReportType.HEAT_MAP.getFormat(), ReportType.BULLET_CHART.getFormat(), ReportType.GEO_CHART.getFormat());
    private List<String> listRecommendedDate = Arrays.asList(DATE_DATASET_CLOSED, DATE_DATASET_CREATED,
            DATE_DATASET_ACTIVITY, DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE);
    private List<String> listRecommendedDateInKD = Arrays.asList(DATE_DATASET_CLOSED, DATE_DATASET_ACTIVITY, 
            DATE_DATASET_CREATED, DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE);
    private final String INSIGHT_TEST = "INSIGHT TEST" + generateHashString();
    private final String INSIGHT_TEST_SAVE = "INSIGHT TEST SAVE" + generateHashString();
    private final String INSIGHT_HAS_METRIC_AND_ATTRIBUTE = "Insight Has Metric And Attribute" + generateHashString();
    private static final String DF_VARIABLE_NAME = "DF-Variable-Name";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += " HEATMAP CHART";
    }

    @Override
    public void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createOppFirstSnapshotMetric();
        metrics.createSnapshotBOPMetric();
        metrics.createNumberOfActivitiesMetric();
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = "createProject")
    protected void testPositionHeatMap() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getListVisualization(), listReportType);
    }

    @Test(dependsOnGroups = "createProject")
    protected void chooseHeatMapFromVisualizationCatalog() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.saveInsight("INSIGHT TEST");
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();

        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be visible.");
        assertTrue(analysisPageHeader.isResetButtonEnabled(), "Clear button should be visible.");
        assertTrue(analysisPageHeader.isOpenButtonEnabled(), "Open button should be visible.");

        assertFalse(analysisPageHeader.isSaveButtonEnabled(), "Save button should be disabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
        assertFalse(analysisPageHeader.clickOptionsButton().isOpenAsReportButtonEnabled(),
                "Open As Report button should be disabled");

        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        assertEquals(analysisPage.getChartReport().getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = "createProject")
    protected void testDataInsight() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_DEPARTMENT).waitForReportComputing();
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Web Meeting"), asList("Department", "Direct Sales"), asList("# of Activities", "23,931")));
        assertEquals(chartReport.getXaxisLabels(), asList("Direct Sales", "Inside Sales"));
        assertEquals(chartReport.getYaxisLabels(), asList("Web Meeting", "Phone Call", "In Person Meeting", "Email"));
        assertEquals(chartReport.getTrackerLegends(), 7);
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightWithOneColumnAndOneRow() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightWithOneColumn() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightWithOneRow() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testTooLargeReport() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetricByAttribute(ATTR_ACCOUNT).addAttribute(ATTR_ACTIVITY).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "TOO MANY DATA POINTS TO DISPLAY\n" +
                "Try applying one or more filters.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInComputedChartInsight() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(),
                "SORRY, WE CAN'T DISPLAY THIS INSIGHT\n" +
                        "Try applying different filters, or using different measures or attributes.\n" +
                        "If this did not help, contact your administrator.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testNoDataInsight() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY).waitForReportComputing();
        analysisPage.addFilter(ATTR_ACCOUNT).getFilterBuckets().configAttributeFilter(ATTR_ACCOUNT, "101 Financial");
        analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO DATA FOR YOUR FILTER SELECTION\n" +
                "Try adjusting or removing some of the filters.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testDragAndDropObjectsToCanvas() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetricToRecommendedStepsPanelOnCanvas(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertTrue(analysisPage.getMetricsBucket().getItemNames().contains(METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasTheSameObjectAttribute() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetricByAttribute(ATTR_ACTIVITY_TYPE).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" +
                "Count of " + ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
    }

    @Test(dependsOnGroups = "createProject")
    protected void testShowInPercentAndPOPAndOpenAsReportInsight() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        analysisPage.addDateFilter().waitForReportComputing();
        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration();
        assertFalse(metricConfiguration.isShowInPercentHidden(), "Show In Percent should be hidden");
        DateFilterPickerPanel dateFilterPickerPanel = analysisPage.getFilterBuckets().openDateFilterPickerPanel();
        assertEquals(dateFilterPickerPanel.getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");
        OptionalExportMenu exportToSelect = analysisPage.getPageHeader().clickOptionsButton();
        assertFalse(exportToSelect.isOpenAsReportButtonEnabled(), "Open as report should be disabled");
        assertEquals(exportToSelect.getExportButtonTooltipText(), "The insight is not compatible with Report Editor." +
                " To open the insight as a report, select another insight type.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testApplyDateFilterAndAttributeOnGlobalFilterBar() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();

        analysisPage.addDateFilter().getFilterBuckets().configDateFilter("01/01/2015", "01/01/2019");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("2", "3", "1", "6"));

        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("3", "6"));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testApplyDateFilterAndAttributeUnderMetricConfiguration() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "01/01/2015", "01/01/2019");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("2", "3", "1", "6"));

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 3);
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("2", "3", "6"));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testAssociateFiltersBetweenGlobalFilterAndMetricFilterByDateAndAttribute() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "01/01/2015", "01/01/2016");
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting");
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 3);
        assertEquals(chartReport.getDataLabels(), asList("1", "2", "2"));

        analysisPage.addDateFilter().getFilterBuckets().configDateFilter("01/01/2016", "01/01/2019");
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call");
        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getDataLabels(), asList("2", "2"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Phone Call"),
                        asList("# of Activities, Activity: Jan 1, 2015 - Jan 1, 2016 (Activity Type: Email, Phone Call, Web Meeting)", "2")));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testAttributeFiltersAndNotRelatedToAnyDate() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        ChartReport chartReport = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing().getChartReport();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call");
        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getDataLabels(), asList("50,780", "33,920"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Phone Call"), asList("# of Activities (Activity Type: Email, Phone Call)", "50,780")));
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testRecommendedDateDimensionOnAD() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight("TEST RECOMMENDED DATE CLOSED");
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();

        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CREATED, DateRange.LAST_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight("TEST RECOMMENDED DATE CREATED");
        MetricConfiguration metricConfiguration = initAnalysePage()
                .changeReportType(ReportType.HEAT_MAP).waitForReportComputing()
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT).expandConfiguration();
        assertEquals(metricConfiguration.getlistRecommended(), listRecommendedDate);
        
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing()
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addDateFilter().waitForReportComputing().saveInsight("TEST RECOMMENDED DATE IN FILTER BAR");
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateDimensionSelect dateDatasetSelect = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).getDateDatasetSelect();
        DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup("RECOMMENDED");
        DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup("OTHER");
        assertEquals(recommended.getDateDimensions(), asList(DATE_DATASET_CLOSED, DATE_DATASET_CREATED));
        assertEquals(other.getDateDimensions(), asList(DATE_DATASET_ACTIVITY, DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE));
    }

    @Test(dependsOnMethods = {"testRecommendedDateDimensionOnAD"})
    protected void testRecommendedDateDimensionOnKD() {
        ConfigurationPanel configurationPanel;
        configurationPanel = initIndigoDashboardsPage().addDashboard().addInsight("TEST RECOMMENDED DATE CLOSED")
                .waitForWidgetsLoading().getConfigurationPanel();
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(), "Checkbox Date filter should be disabled");

        configurationPanel = initIndigoDashboardsPage().addDashboard().addInsight("TEST RECOMMENDED DATE CREATED")
                .waitForWidgetsLoading().getConfigurationPanel();
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(), "Checkbox Date filter should be disabled");
        
        configurationPanel = initIndigoDashboardsPage().addDashboard().addInsight("TEST RECOMMENDED DATE IN FILTER BAR")
                .waitForWidgetsLoading().getConfigurationPanel();
        assertEquals(configurationPanel.getListDateDataset(), listRecommendedDateInKD);
    }

    @Test(dependsOnGroups = "createProject")
    protected void testSomeActions() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_TEST);
        analysisPage.openInsight(INSIGHT_TEST).waitForReportComputing().saveInsightAs(INSIGHT_TEST_SAVE);
        analysisPage.openInsight(INSIGHT_TEST_SAVE).waitForReportComputing();
        assertTrue(analysisPage.searchInsight(INSIGHT_TEST), INSIGHT_TEST + " is available");
        assertTrue(analysisPage.searchInsight(INSIGHT_TEST_SAVE), INSIGHT_TEST_SAVE + " is available");
        analysisPage.getPageHeader().expandInsightSelection().deleteInsight(INSIGHT_TEST);
        assertFalse(analysisPage.getPageHeader().expandInsightSelection()
                .isExist(INSIGHT_TEST), INSIGHT_TEST + " should be removed");

        ChartReport chartReport = analysisPage.addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackersCount(), 8);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Web Meeting"), asList("Department", "Direct Sales"), asList("# of Activities", "23,931")));

        analysisPage.reorderRowAndColumn(ATTR_DEPARTMENT, ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.getTrackersCount(), 8);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Department", "Inside Sales"), asList("Activity Type", "Email"), asList("# of Activities", "12,305")));

        analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_DEPARTMENT, ATTR_DEPARTMENT + "Rename");
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT + "Rename\n" + ATTR_DEPARTMENT));
        log.info("analysisPage.getAttributesBucket().getItemNames() : " + analysisPage.getAttributesBucket().getItemNames());

        analysisPage.undo().waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_DEPARTMENT);
        analysisPage.redo().waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT + "Rename\n" + ATTR_DEPARTMENT));
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasThreeMetricsAndOneAttributeSwitchingBetweenColumnAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), "");
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT, METRIC_SNAPSHOT_BOP));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasThreeMetricsAndTwoAttributesSwitchingBetweenColumnAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).addAttribute(ATTR_ACTIVITY_TYPE)
                .addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_DEPARTMENT));
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT, METRIC_SNAPSHOT_BOP));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasTwoMetricsAndThreeAttributesSwitchingBetweenColumnAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE).addAttribute(ATTR_DEPARTMENT)
                .addAttribute(ATTR_REGION).waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_DEPARTMENT));
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(),
                asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT, ATTR_REGION));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasThreeMetricsDateAndTwoAttributesSwitchingBetweenTableAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addDate().addAttribute(ATTR_ACTIVITY_TYPE)
                .addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList("Date"));
        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList("Date", ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasThreeMetricsAttributeAndDateAndAttributeSwitchingBetweenTableAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE).addDate().addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList("Date"));
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, "Date", ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInsightHasThreeMetricsAndTwoAttributesAndDateSwitchingBetweenTableAndHeatMapChart() {
        initAnalysePage().changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE).addAttribute(ATTR_DEPARTMENT).addDate().waitForReportComputing();
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_DEPARTMENT));
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT, "Date"));
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

        ChartReport chartReport = initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing()
                .addMetric("METRIC TEST").waitForReportComputing().getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("3"));
        log.info("chartReport.getTooltipTextOnTrackerByIndex(0,0) : " + chartReport.getTooltipTextOnTrackerByIndex(0, 0));
        Screenshots.takeScreenshot(browser, "Check Insight With Custom Metric", getClass());

        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            chartReport = initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing()
                    .addMetric("METRIC TEST").waitForReportComputing().getChartReport();
            assertEquals(chartReport.getDataLabels(), asList("2"));
            log.info("chartReport.getTooltipTextOnTrackerByIndex(0,0) : " + chartReport.getTooltipTextOnTrackerByIndex(0, 0));
            Screenshots.takeScreenshot(browser, "Check Insight With Custom Metric For User Editor", getClass());

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
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

