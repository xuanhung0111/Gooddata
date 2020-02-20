package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown.CompareType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CompareApplyMeasure;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.SP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InsightCompareToSamePreviousPeriodYearTest extends AbstractAnalyseTest {

    private static final String DERIVED_METRIC_WON = METRIC_WON + SP_YEAR_AGO;
    private static final String DERIVED_METRIC_AMOUNT = METRIC_AMOUNT + SP_YEAR_AGO;
    private static final String DERIVED_METRIC_AVG_AMOUNT = METRIC_AVG_AMOUNT + SP_YEAR_AGO;
    private static final String SAME_PERIOD_PREVIOUS_YEAR = "Same period (SP) previous year";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private static final String APPLY_ON_ALL_MODE = "All measures";
    private static final String APPLY_ON_INDIVIDUAL_MODE = "Individual selection";
    private static final String ALL_TIME = DateRange.ALL_TIME.toString();

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
    public void supportSPPYMetricHasAttributeFilters() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_AMOUNT).addDateFilter().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().addFilterWithAllValue(ATTR_ACCOUNT);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        CompareApplyMeasure compareApplyMeasure = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures();
        assertEquals(getTitles(compareApplyMeasure.getValues()), asList(METRIC_SNAPSHOT_BOP, METRIC_AMOUNT));
        //show tooltip
        assertEquals(compareApplyMeasure.getTitleItems(), asList(METRIC_SNAPSHOT_BOP, METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void notSupportSPPYMetricHasFilterByDate() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_AMOUNT).addDateFilter().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures();
        assertEquals(getTitles(compareApplyMeasure.getValues()), singletonList(METRIC_SNAPSHOT_BOP));
        assertEquals(dateFilterPickerPanel.getMessageCompareApplyIncompatible(),
                "Displaying only supported measures.");
        //show tooltip
        assertEquals(compareApplyMeasure.getTitleItems(), singletonList(METRIC_SNAPSHOT_BOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTheApplyOnDropDownListIsNotEnabled() {
        initAnalysePage().addMetric(METRIC_AVG_AMOUNT).addMetric(METRIC_AMOUNT).addDateFilter();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        metricsBucket.getMetricConfiguration(METRIC_AVG_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        assertEquals(dateFilterPickerPanel.getMessageCompareApplyIncompatible(),
                "Displaying only supported measures.");
        assertFalse(dateFilterPickerPanel.getCompareApplyMeasure()
                .isDropdownButtonEnabled(), "The dropdown should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDerivedMeasuresAfterApplyingSPPYForSomeMeasures() throws NoSuchFieldException {
        List<String> listExpectedMeasure = asList("M1\n" + METRIC_SNAPSHOT_BOP,
                "M2\n" + DERIVED_METRIC_AMOUNT + "\nfrom M3;", "M3\n" + METRIC_AMOUNT);

        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .openCompareApplyMeasures().selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures()
                .getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(metricsBucket.getItemHeaderAndSequenceNumber(), listExpectedMeasure);

        ChartReport chartReport = analysisPage.getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                hasItems(asList(DERIVED_METRIC_AMOUNT, "$116,625,456.54")));
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(153,230,209)", "rgb(0,193,141)"));
        assertFalse(metricsBucket.getMetricConfiguration(DERIVED_METRIC_AMOUNT).expandConfiguration()
                .isDateAndAddAttributeFilterVisible(), "Derived measure setting shouldn't be expanded");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDerivedMeasuresAfterApplyingSPPYForAllMeasures() throws NoSuchFieldException {
        List<String> listExpectedMeasure = asList("M1\n" + DERIVED_METRIC_WON
                + "\n" + "from M2;", "M2\n" + METRIC_WON, "M3\n" + DERIVED_METRIC_AMOUNT
                + "\n" + "from M4;", "M4\n" + METRIC_AMOUNT);
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures().getSelection(), APPLY_ON_ALL_MODE);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(metricsBucket.getItemHeaderAndSequenceNumber(), listExpectedMeasure);

        ChartReport chartReport = analysisPage.getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0, 0), hasItems(asList(DERIVED_METRIC_WON, "38,310,753")));
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
                hasItems(asList(DERIVED_METRIC_AMOUNT, "$116,625,456.54")));
        assertEquals(chartReport.getLegendColors(), asList("rgb(161,224,243)", "rgb(20,178,226)",
                "rgb(153,230,209)", "rgb(0,193,141)"));
        assertFalse(metricsBucket.getMetricConfiguration(DERIVED_METRIC_AMOUNT).expandConfiguration()
                .isDateAndAddAttributeFilterVisible(), "Derived measure setting shouldn't be expanded");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableApplyButtonWithoutChoosingMeasure() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .clearAllCheckedValues().isApplyButtonEnabled(), "Apply button should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYOnMeasureWhichHasPercent() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter()
                .getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().showPercents();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + DERIVED_METRIC_AMOUNT, "41.96%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT, "41.96%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + DERIVED_METRIC_AMOUNT, "58.04%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT, "58.04%")));

        ///change attribute filter of master measure
        analysisPage.getMetricsBucket().getMetricConfiguration("% " + METRIC_AMOUNT)
                .expandConfiguration().addFilterBySelectOnly(ATTR_DEPARTMENT, "Inside Sales");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)"
                        + SP_YEAR_AGO, "42.44%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)", "42.44%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)"
                        + SP_YEAR_AGO, "57.56%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)", "57.56%")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYCompareOnPrimaryMeasureWithHeadline() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .changeReportType(ReportType.HEAD_LINE).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measure is hidden");
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        ElementUtils.moveToElementActions(metricsBucket.getRoot(), 1, 1).perform();
        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket()
                .getItemNames(), hasItem(METRIC_WON));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYCompareOnAllMeasureWithHeadline() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .changeReportType(ReportType.HEAD_LINE).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures are hidden");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        ElementUtils.moveToElementActions(analysisPage.getMetricsBucket().getRoot(), 1, 1).perform();
        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket().getItemNames(),
                hasItems(DERIVED_METRIC_WON, METRIC_WON));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYOnSecondaryMeasureWithHeadline() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .changeReportType(ReportType.HEAD_LINE).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures are hidden");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), singletonList(DERIVED_METRIC_AMOUNT));
        ElementUtils.moveToElementActions(analysisPage.getMetricsBucket().getRoot(), 1, 1).perform();
        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket().getItemNames(),
                hasItem(DERIVED_METRIC_WON));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testStackByBucketAfterApplyingSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(),
                ReportType.COLUMN_CHART.getExtendedStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testStackByBucketAfterApplyingSPPYOnAMeasureAndStackBy() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate().addStack(ATTR_DEPARTMENT);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measure is hidden");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));

        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket().getItemNames(),
                hasItem(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addSomeMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().addMetric(METRIC_AVG_AMOUNT);
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AVG_AMOUNT + "M4"), "Checkbox of newly ones should be unchecked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addSomeMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        List<String> listNewDerivedMeasureExpected = asList("M1\n" + DERIVED_METRIC_WON + "\nfrom M2;",
                "M2\n" + METRIC_WON, "M3\n" + DERIVED_METRIC_AMOUNT + "\nfrom M4;", "M4\n" + METRIC_AMOUNT,
                "M5\n" + DERIVED_METRIC_AVG_AMOUNT + "\nfrom M6;", "M6\n" + METRIC_AVG_AMOUNT);
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), asList(METRIC_WON, METRIC_AMOUNT));

        analysisPage.addMetric(METRIC_AVG_AMOUNT);
        assertEquals(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), asList(METRIC_WON, METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertTrue(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AVG_AMOUNT + "M6"), "Checkbox of newly ones should be checked");
        assertEquals(analysisPage.getMetricsBucket().getItemHeaderAndSequenceNumber(), listNewDerivedMeasureExpected);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeFilterByDateMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures();
        compareApplyMeasure.selectAllValues().apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).removeFilterByDate();
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M3"), "Checkbox of newly ones should be unchecked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeFilterByDateMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        List<String> listNewDerivedMeasureExpected = asList("M1\n" + DERIVED_METRIC_WON + "\nfrom M2;", "M2\n"
                + METRIC_WON, "M3\n" + METRIC_AMOUNT);
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        assertEquals(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), singletonList(METRIC_WON));

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).removeFilterByDate();
        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures();
        assertEquals(getTitles(compareApplyMeasure.getValues()), asList(METRIC_WON, METRIC_AMOUNT));
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M3"), "Checkbox of newly ones should be unchecked");
        assertEquals(analysisPage.getMetricsBucket().getItemHeaderAndSequenceNumber(), listNewDerivedMeasureExpected);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeSelectedMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItems(DERIVED_METRIC_AMOUNT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeUnselectedMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeSelectedMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        analysisPage.removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_ALL_MODE);
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItems(DERIVED_METRIC_AMOUNT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAllMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing().removeMetric(METRIC_AMOUNT).removeMetric(METRIC_WON)
                .removeMetric(METRIC_AVG_AMOUNT);
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .getCompareApplyMeasure().isDropdownButtonEnabled(), "Apply button should be disabled");
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_ALL_MODE);
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"));
        assertTrue(analysisPage.getMetricsBucket().isEmpty(), "Metrics bucket should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeSomeDerivedMeasuresWithSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().removeMetric(DERIVED_METRIC_AMOUNT).waitForReportComputing();

        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItem(DERIVED_METRIC_AMOUNT)));
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M1"), "Checkbox of corresponding master measures should be unchecked");
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures().getSelection(),
                APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"),
                        SAME_PERIOD_PREVIOUS_YEAR));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAllDerivedMeasuresWithSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(
                    CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectByNames(METRIC_AMOUNT, METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().removeMetric(DERIVED_METRIC_AMOUNT).removeMetric(DERIVED_METRIC_WON)
                .waitForReportComputing();

        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, "Jan 1, 2006 - Jan 1, 2020"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deselectCompareAfterApplyingSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).changeCompareType(CompareType.NOTHING)
            .changePeriod(ALL_TIME).apply();
        assertThat(analysisPage.getMetricsBucket().getItemNames(),
                not(hasItems(DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON)));
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .isApplyButtonEnabled(), "Apply button should be disabled");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeDateFilterDialogAfterApplyingSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().removeDateFilter();
        assertFalse(analysisPage.getFilterBuckets().isFilterVisible(DATE_DATASET_CLOSED + ":\n" + DATE_FILTER_ALL_TIME),
                "Filter should be not displayed");

        analysisPage.addDateFilter();
        assertEquals(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .getCompareTypeDropdownButtonText(), "Nothing");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReferencePointWhenSwitchingBetweenInvalidInsights() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().changeReportType(ReportType.STACKED_AREA_CHART);
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures are hidden");

        analysisPage.addMetric(METRIC_AVG_AMOUNT);
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        assertEquals(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");
    }

    private List<String> getTitles(Collection<String> listItems) {
        return listItems.stream()
                .map(item -> item.substring(0, item.lastIndexOf("M")))
                .collect(toList());
    }
}
