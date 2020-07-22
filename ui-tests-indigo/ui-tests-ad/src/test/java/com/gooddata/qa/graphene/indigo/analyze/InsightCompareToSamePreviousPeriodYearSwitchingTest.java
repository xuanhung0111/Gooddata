package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown.CompareType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CompareApplyMeasure;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InsightCompareToSamePreviousPeriodYearSwitchingTest extends AbstractAnalyseTest {

    private static final String DERIVED_METRIC_WON = METRIC_WON + SP_YEAR_AGO;
    private static final String DERIVED_METRIC_AMOUNT = METRIC_AMOUNT + SP_YEAR_AGO;
    private static final String DERIVED_METRIC_AVG_AMOUNT = METRIC_AVG_AMOUNT + SP_YEAR_AGO;
    private static final String SAME_PERIOD_PREVIOUS_YEAR = "Same period (SP) previous year";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private static final String INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER =
            "Insight has 1 measure and applies Same period (SP) previous year filter";

    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Comparing To Same Previous Year Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createSnapshotBOPMetric();
        metrics.createWonMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToColumnAndHeadlineInsightWithAMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_FORECAST_CATEGORY).addAttribute(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel =
                filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();

        //reorder metrics to follow by requirement of test case
        analysisPage.waitForReportComputing().reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT)
                .changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();

        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getWarningMessage(), ReportType.COLUMN_CHART.getExtendedStackByMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertEquals(metricsBucket.getWarningMessage(), ReportType.LINE_CHART.getMetricMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.STACKED_AREA_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getWarningMessage(), ReportType.STACKED_AREA_CHART.getStackByMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToColumnAndHeadlineInsightWithTwoMeasures() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addAttribute(ATTR_FORECAST_CATEGORY).addAttribute(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel =
                filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getExtendedStackByMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();
        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getWarningMessage(), ReportType.LINE_CHART.getStackByMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.STACKED_AREA_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        assertEquals(attributesBucket.getWarningMessage(), ReportType.STACKED_AREA_CHART.getViewbyByMessage());
        assertEquals(stacksBucket.getWarningMessage(), ReportType.STACKED_AREA_CHART.getExtendedStackByMessage());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromColumnInsightToTableAndHeadlineInsightWithAMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_FORECAST_CATEGORY).addStack(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().changeReportType(ReportType.TABLE).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        AttributesBucket attributeColumnBucket = analysisPage.getAttributesColumnsBucket();

        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_FORECAST_CATEGORY));
        assertEquals(attributeColumnBucket.getItemNames(), singletonList(ATTR_DEPARTMENT));

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromColumnInsightToTableAndHeadlineInsightWithThreeMeasures() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON).apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT)
                .reorderMetric(DERIVED_METRIC_WON, METRIC_WON).changeReportType(ReportType.TABLE).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertThat(metricsBucket.getItemNames(), hasItems(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON,
                DERIVED_METRIC_WON, METRIC_AVG_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_FORECAST_CATEGORY));

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe1stMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        //reorder metrics to comply requirement of test case
        analysisPage.waitForReportComputing().reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));
        CompareApplyMeasure compareApplyMeasure = filterBucket.openDatePanelOfFilter(
            filterBucket.getDateFilter()).openCompareApplyMeasures();
        WaitUtils.waitForCollectionIsNotEmpty(compareApplyMeasure.getValues(), 1);
        assertTrue(compareApplyMeasure.isItemSelected(METRIC_AMOUNT + "M1"),
            "Checkbox of newly ones should be checked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe2ndMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        //reorder metrics to comply requirement of test case
        analysisPage.waitForReportComputing().reorderMetric(DERIVED_METRIC_WON, METRIC_WON).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(METRIC_WON));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));

        compareApplyMeasure = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures();
        WaitUtils.waitForCollectionIsNotEmpty(compareApplyMeasure.getValues(), 2);
        assertFalse(compareApplyMeasure.isItemSelected(METRIC_AMOUNT + "M1"),
            "Checkbox of newly ones should be unchecked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe3rdMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AVG_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        //reorder metrics to comply requirement of test case
        analysisPage.waitForReportComputing().reorderMetric(DERIVED_METRIC_AVG_AMOUNT, METRIC_AVG_AMOUNT).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(METRIC_WON));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPY1stMeasureToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT).addDateFilter()
                .addMetricToSecondaryBucket(METRIC_WON);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(),
                ReportType.COLUMN_CHART.getExtendedStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPY2ndMeasureToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT)
                .addMetricToSecondaryBucket(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_WON));

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_WON, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_WON, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(),
                ReportType.COLUMN_CHART.getExtendedStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPYAllMeasuresToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT)
                .addMetricToSecondaryBucket(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON,
                METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON,
                METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(),
                ReportType.COLUMN_CHART.getExtendedStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void placeInsightHasComparesMeasureOnKD() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addDateFilter()
            .waitForReportComputing();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER);
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER).selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .waitForWidgetsLoading();
        checkRedBar(browser);
        Insight insight = indigoDashboardsPage.selectFirstWidget(Insight.class);
        assertEquals(insight.getChartReport().getDataLabels(), asList("$116,625,456.54", "$116,625,456.54"));
        assertEquals(insight.getHeadline(), INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER);
        assertEquals(insight.getLegends(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));

        indigoDashboardsPage.selectDateFilterByName("This year").waitForWidgetsLoading().selectFirstWidget(Insight.class);
        assertTrue(insight.isEmptyValue(), "Should be no data");
    }
}
