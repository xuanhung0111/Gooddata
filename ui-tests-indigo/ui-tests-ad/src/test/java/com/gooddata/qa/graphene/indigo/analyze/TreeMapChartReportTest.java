package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect.DateDimensionGroup;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_TIMELINE;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TreeMapChartReportTest extends AbstractAnalyseTest {
    private List<String> listReportType = Arrays.asList(
            ReportType.TABLE.getFormat(), ReportType.COLUMN_CHART.getFormat(), ReportType.BAR_CHART.getFormat(),
            ReportType.LINE_CHART.getFormat(), ReportType.STACKED_AREA_CHART.getFormat(), ReportType.COMBO_CHART.getFormat(),
            ReportType.HEAD_LINE.getFormat(), ReportType.SCATTER_PLOT.getFormat(), ReportType.BUBBLE_CHART.getFormat(),
            ReportType.PIE_CHART.getFormat(), ReportType.DONUT_CHART.getFormat(),
            ReportType.TREE_MAP.getFormat(), ReportType.HEAT_MAP.getFormat(),
            ReportType.BULLET_CHART.getFormat(), ReportType.GEO_CHART.getFormat());
    private List<String> listRecommendedDate = Arrays.asList(DATE_DATASET_CLOSED, DATE_DATASET_CREATED,
            DATE_DATASET_ACTIVITY, DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE);
    private List<String> listRecommendedDateInKD = Arrays.asList(DATE_DATASET_CLOSED, DATE_DATASET_ACTIVITY, 
            DATE_DATASET_CREATED, DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE);
    private final String INSIGHT_TEST = "INSIGHT TEST" + generateHashString();
    private final String INSIGHT_TEST_SAVE = "INSIGHT TEST SAVE" + generateHashString();

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "TreeMap Chart";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createNumberOfActivitiesMetric();
        metrics.createSnapshotBOPMetric();
        metrics.createOppFirstSnapshotMetric();
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testListVisualization() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        log.info("listReportType : " + listReportType);
        log.info("analysisPage.getListVisualization() : " + analysisPage.getListVisualization());
        assertEquals(analysisPage.getListVisualization(), listReportType);
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void chooseTreeMapOnVisualizationBucket() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .addStack(ATTR_STAGE_NAME).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_DEPARTMENT);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_STAGE_NAME);
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 18);
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();
        OptionalExportMenu exportToSelect = analysisPageHeader.clickOptionsButton();
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertTrue(analysisPageHeader.isOpenButtonEnabled(), "Open button should be enabled");
        assertTrue(analysisPageHeader.isResetButtonEnabled(), "Clear button should be enabled");
        assertTrue(analysisPageHeader.isSaveButtonEnabled(), "Save button should be enabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
        assertFalse(exportToSelect.isOpenAsReportButtonEnabled(), "Open as report should be disabled");
        assertEquals(getTooltipFromElement(ReportType.TREE_MAP.getLocator(), browser), "Treemap");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasMetricAndSecondaryAttributeOnViewByAndSegmentBy() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
        assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), ReportType.TREE_MAP.getMetricMessage());
        assertEquals(report.getLegends(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
        assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, "Email"), asList("Department", "Direct Sales"),
                        asList(METRIC_NUMBER_OF_ACTIVITIES, "21,615")));
        assertEquals(report.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_ACTIVITY_TYPE, "In Person Meeting"), asList("Department", "Direct Sales"),
                        asList(METRIC_NUMBER_OF_ACTIVITIES, "22,088")));
    }
    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasSecondaryMetricAndAttributeOnSegment() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
        assertEquals(analysisPage.getAttributesBucket().getWarningMessage(), ReportType.TREE_MAP.getViewbyByMessage());
        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        if (BrowserUtils.isFirefox()) {
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Department", "Direct Sales"), asList("# of Activities", "101,054")));
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 1),
                    asList(asList("Department", "Direct Sales"), asList("_Opp. First Snapshot", "40,334.00")));
        } else {
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Department", "Inside Sales"), asList("# of Activities", "53,217")));
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 1),
                    asList(asList("Department", "Inside Sales"), asList("_Opp. First Snapshot", "40,334.00")));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasSecondaryMetric() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
        assertEquals(analysisPage.getAttributesBucket().getWarningMessage(), ReportType.TREE_MAP.getViewbyByMessage());
        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT));
        if (BrowserUtils.isFirefox()) {
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Department", "Direct Sales"), asList("# of Activities", "101,054")));
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 1),
                    asList(asList("Department", "Direct Sales"), asList("_Opp. First Snapshot", "40,334.00")));
        } else {
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Department", "Inside Sales"), asList("# of Activities", "53,217")));
            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 1),
                    asList(asList("Department", "Inside Sales"), asList("_Opp. First Snapshot", "40,334.00")));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasSecondaryAttributeOnViewByAndSegmentBy() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasAttributeOnViewBy() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasAttributeOnSegmentBy() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO MEASURE IN YOUR INSIGHT\n" +
                "Add a measure to your insight, or switch to table view.\n" +
                "Once done, you'll be able to save it.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTooLargeReport() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACCOUNT).addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(),
                "TOO MANY DATA POINTS TO DISPLAY\nAdd a filter, or switch to table view.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testNoDataInsight() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.addFilter(ATTR_ACCOUNT).getFilterBuckets().configAttributeFilter(ATTR_ACCOUNT, "1000Bulbs.com");
        analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(),
                "NO DATA FOR YOUR FILTER SELECTION\nTry adjusting or removing some of the filters.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDragAndDropMetricToCanvas() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetricToRecommendedStepsPanelOnCanvas(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertTrue(analysisPage.getMetricsBucket().getItemNames().contains(METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightHasTheSameObjectsOnMeasureViewByAndSegment() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetricByAttribute(ATTR_ACTIVITY_TYPE).addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        OptionalExportMenu exportToSelect = analysisPage.getPageHeader().clickOptionsButton();
        assertEquals(exportToSelect.getExportButtonTooltipText(),
                "The insight is not compatible with Report Editor. " +
                        "To open the insight as a report, select another insight type.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInComputedChartInsight() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(),
                "SORRY, WE CAN'T DISPLAY THIS INSIGHT\n" +
                        "Try applying different filters, or using different measures or attributes.\n" +
                        "If this did not help, contact your administrator.");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testInteractionWithLegendAnToolTipOnTreeMapChartReport() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        ChartReport chartReport = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_DEPARTMENT).waitForReportComputing()
                .saveInsight("INSIGHT HAVE A METRIC AND TWO ATTRIBUTE ON VIEW BY AND SEGMENT BY").getChartReport();
        assertEquals(chartReport.getLegends(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, "Email"), asList(ATTR_DEPARTMENT, "Inside Sales"),
                        asList(METRIC_NUMBER_OF_ACTIVITIES, "12,305")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testShowPercentAndOpenAsReportOnInsight() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter().getFilterBuckets().openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        analysisPage.waitForReportComputing();
        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration().showPercents();
        analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getDataLabels(),
                asList("Email (21.99%)", "In Person Meeting (23.32%)", "Phone Call (32.92%)", "Web Meeting (21.78%)"));
        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration("% " + METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(metricConfiguration.isShowPercentSelected(), "Show percent should be selected");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
        assertEquals(analysisPage.getChartReport().getDataLabels(),
                asList("Email (21.99%)", "In Person Meeting (23.32%)", "Phone Call (32.92%)", "Web Meeting (21.78%)"));
        DateFilterPickerPanel dateFilterPickerPanel = analysisPage.getFilterBuckets().openDateFilterPickerPanel();
        assertEquals(dateFilterPickerPanel.getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTreeMapChartAppliedDateFilterAndAttributeFilterUnderMetric() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).
                expandConfiguration().addFilterByDate(DATE_DATASET_CREATED, "01/01/2010", "01/01/2019");
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration().addFilter(ATTR_ACTIVITY_TYPE, "Email");
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTreeMapChartAppliedDateFilterAndAttributeFilterOnFilterBucket() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().addDateFilter().getFilterBuckets()
                .configDateFilter("01/01/2010", "01/01/2019");
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email");
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTreeMapChartAssociateFiltersBetweenGlobalFilterAndMetricFilter() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CREATED, "01/01/2010", "01/01/2019");
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration().addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call");
        analysisPage.waitForReportComputing().addDateFilter().getFilterBuckets()
                .configDateFilter("01/01/2019", "01/01/2019").changeDateDimension(DATE_DATASET_CREATED);
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testRecommendedDateDimensionOnAD() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight("TEST RECOMMENDED DATE CLOSED");

        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CREATED, DateRange.LAST_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight("TEST RECOMMENDED DATE CREATED");
        MetricConfiguration metricConfiguration = initAnalysePage()
                .changeReportType(ReportType.TREE_MAP).waitForReportComputing()
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT).getMetricsBucket()
                .getMetricConfiguration(METRIC_OPP_FIRST_SNAPSHOT).expandConfiguration();
        assertEquals(metricConfiguration.getlistRecommended(), listRecommendedDate);
        
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing()
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

    @Test(dependsOnGroups = {"createProject"})
    public void testActionWithTreeMapChartOnInsight() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).saveInsight(INSIGHT_TEST);
        analysisPage.openInsight(INSIGHT_TEST).waitForReportComputing().saveInsightAs(INSIGHT_TEST_SAVE);
        analysisPage.openInsight(INSIGHT_TEST_SAVE).waitForReportComputing();
        assertTrue(analysisPage.searchInsight(INSIGHT_TEST), INSIGHT_TEST + " is available");
        assertTrue(analysisPage.searchInsight(INSIGHT_TEST_SAVE), INSIGHT_TEST_SAVE + " is available");
        analysisPage.getPageHeader().expandInsightSelection().deleteInsight(INSIGHT_TEST);
        assertFalse(analysisPage.getPageHeader().expandInsightSelection()
                .isExist(INSIGHT_TEST), INSIGHT_TEST + " should be removed");
        analysisPage.addStack(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        analysisPage.undo().waitForReportComputing();
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), "");
        analysisPage.redo().waitForReportComputing();
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchingBetweenColumnChartAndTreeMapChartOnInsight() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).addAttribute(ATTR_ACTIVITY_TYPE)
                .addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 24);
        analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT, METRIC_SNAPSHOT_BOP));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchingBetweenBarChartAndTreeMapChartOnInsight() {
        initAnalysePage().changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT, METRIC_SNAPSHOT_BOP));
        assertTrue(analysisPage.getAttributesBucket().isDisabled(), "Attribute Bucket should be disabled");
        assertFalse(analysisPage.getStacksBucket().isDisabled(), "Stacked Bucket should be visible");
        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT, METRIC_SNAPSHOT_BOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchingBetweenPieChartAndTreeMapChartOnInsight() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter().waitForReportComputing();
        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
        analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), "");
        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchingBetweenTableChartAndTreeMapChartOnInsight() {
        initAnalysePage().changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addAttribute(ATTR_DEPARTMENT).addDateFilter().waitForReportComputing();
        analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getMetricsBucket().getMetricName(), "M1\n" + METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }
}
