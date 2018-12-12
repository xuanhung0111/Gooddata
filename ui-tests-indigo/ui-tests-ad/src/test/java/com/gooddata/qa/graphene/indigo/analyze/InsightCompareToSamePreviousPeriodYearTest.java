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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
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
import static java.util.Collections.emptyList;
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
    private static final String INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER =
            "Insight has 1 measure and applies Same period (SP) previous year filter";

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
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void supportSPPYMetricHasAttributeFilters() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_AMOUNT).addDateFilter().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().addFilterWithAllValue(ATTR_ACCOUNT);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        CompareApplyMeasure compareApplyMeasure = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
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
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures();
        assertEquals(getTitles(compareApplyMeasure.getValues()), asList(METRIC_SNAPSHOT_BOP));
        assertEquals(dateFilterPickerPanel.getMessageCompareApplyIncompatible(),
                "Displaying only supported measures.");
        //show tooltip
        assertEquals(compareApplyMeasure.getTitleItems(), asList(METRIC_SNAPSHOT_BOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTheApplyOnDropdownlistIsNotEnabled() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AVG_AMOUNT).addMetric(METRIC_AMOUNT).addDateFilter();
        ElementUtils.makeSureNoPopupVisible();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());
        metricsBucket.getMetricConfiguration(METRIC_AVG_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.LAST_YEAR.toString());

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .openCompareApplyMeasures().selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures()
                .getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(metricsBucket.getItemHeaderAndSequenceNumber(), listExpectedMeasure);

        ChartReport chartReport = analysisPage.getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(1),
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures().getSelection(), APPLY_ON_ALL_MODE);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(metricsBucket.getItemHeaderAndSequenceNumber(), listExpectedMeasure);

        ChartReport chartReport = analysisPage.getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0), hasItems(asList(DERIVED_METRIC_WON, "38,310,753")));
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(2),
                hasItems(asList(DERIVED_METRIC_AMOUNT, "$116,625,456.54")));
        assertEquals(chartReport.getLegendColors(), asList("rgb(161,224,243)", "rgb(20,178,226)",
                "rgb(153,230,209)", "rgb(0,193,141)"));
        assertFalse(metricsBucket.getMetricConfiguration(DERIVED_METRIC_AMOUNT).expandConfiguration()
                .isDateAndAddAttributeFilterVisible(), "Derived measure setting shouldn't be expanded");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableApplyButtonWithoutChoosedMeasure() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .clearAllCheckedValues().isApplyButtonEnabled(), "Apply button should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYOnMeasureWhichHasPercent() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter()
                .getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().showPercents();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + DERIVED_METRIC_AMOUNT, "41.96%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT, "41.96%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + DERIVED_METRIC_AMOUNT, "58.04%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(3), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT, "58.04%")));

        ///change attribute filter of master measure
        analysisPage.getMetricsBucket().getMetricConfiguration("% " + METRIC_AMOUNT)
                .expandConfiguration().addFilterBySelectOnly(ATTR_DEPARTMENT, "Inside Sales");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)"
                        + SP_YEAR_AGO, "42.44%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2), asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)", "42.44%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)"
                        + SP_YEAR_AGO, "57.56%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(3), asList(asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList("% " + METRIC_AMOUNT + " (" + ATTR_DEPARTMENT + ": Inside Sales)", "57.56%")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySPPYCompareOnPrimaryMeasureWithHeadline() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .changeReportType(ReportType.HEAD_LINE).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures and attributes are hidden");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        ElementUtils.makeSureNoPopupVisible();
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures and attributes are hidden");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        ElementUtils.makeSureNoPopupVisible();
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures and attributes are hidden");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        ElementUtils.makeSureNoPopupVisible();
        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket().getItemNames(),
                hasItem(DERIVED_METRIC_WON));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testStackByBucketAfterApplyingSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testStackByBucketAfterApplyingSPPYOnAMeasureAndStackBy() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate().addStack(ATTR_DEPARTMENT);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures and attributes are hidden");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
        ElementUtils.makeSureNoPopupVisible();
        //switch the chart insight to check reference point
        assertThat(analysisPage.changeReportType(ReportType.TABLE).getMetricsBucket().getItemNames(),
                hasItem(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addSomeMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_WON).addMetric(METRIC_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        analysisPage.addMetric(METRIC_AVG_AMOUNT);
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
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
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures();
        compareApplyMeasure.selectAllValues().apply();
        dateFilterPickerPanel.apply();

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).removeFilterByDate();
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
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        assertEquals(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), asList(METRIC_WON));

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
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItems(DERIVED_METRIC_AMOUNT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeUnselectedMeasuresWithSPPYIndividualMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeSelectedMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.removeMetric(METRIC_AMOUNT);
        assertThat(getTitles(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .openCompareApplyMeasures().getValues()), not(hasItems(METRIC_AMOUNT)));
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_ALL_MODE);
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItems(DERIVED_METRIC_AMOUNT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAllMeasuresWithSPPYAllMeasuresMode() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues();
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();

        analysisPage.removeMetric(METRIC_AMOUNT).removeMetric(METRIC_WON).removeMetric(METRIC_AVG_AMOUNT);//
        assertEquals(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .getCompareTypeDropdownButtonText(), "Nothing");
        assertFalse(dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .getCompareApplyMeasure().isDropdownButtonEnabled(), "Apply button should be disabled");
        assertEquals(compareApplyMeasure.getSelection(), APPLY_ON_ALL_MODE);
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME));
        assertThat(analysisPage.getMetricsBucket().getItemNames(),
                not(hasItems(DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON, DERIVED_METRIC_AVG_AMOUNT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeSomeDerivedMeasuresWithSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addMetric(METRIC_AVG_AMOUNT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON).apply();
        dateFilterPickerPanel.apply();
        analysisPage.removeMetric(DERIVED_METRIC_AMOUNT).waitForReportComputing();

        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItem(DERIVED_METRIC_AMOUNT)));
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M1"), "Checkbox of corresponding master measures should be unchecked");
        assertEquals(dateFilterPickerPanel.openCompareApplyMeasures().getSelection(),
                APPLY_ON_INDIVIDUAL_MODE + " (1)");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAllDerivedMeasuresWithSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectByNames(METRIC_AMOUNT, METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        analysisPage.removeMetric(DERIVED_METRIC_AMOUNT).removeMetric(DERIVED_METRIC_WON).waitForReportComputing();

        assertEquals(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
                .getCompareTypeDropdownButtonText(), "Nothing");
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deselectCompareAfterApplyingSPPY() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).changeCompareType(CompareType.NOTHING).apply();
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.removeDateFilter();
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
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.STACKED_AREA_CHART);
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(),
                "Unsupported measures and attributes are hidden");

        analysisPage.addMetric(METRIC_AVG_AMOUNT);
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        assertEquals(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");

    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToColumnAndHeadlineInsightWithAMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_FORECAST_CATEGORY).addAttribute(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        //reorder metrics to follow by requirement of test case
        analysisPage.reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT).changeReportType(ReportType.COLUMN_CHART)
                .waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();

        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertEquals(metricsBucket.getWarningMessage(), ReportType.COLUMN_CHART.getMetricMessage());

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToColumnAndHeadlineInsightWithTwoMeasures() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addAttribute(ATTR_FORECAST_CATEGORY).addAttribute(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getStackByMessage());

        analysisPage.changeReportType(ReportType.PIE_CHART).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromColumnInsightToTableAndHeadlineInsightWithAMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_FORECAST_CATEGORY).addStack(ATTR_DEPARTMENT).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));

        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromColumnInsightToTableAndHeadlineInsightWithThreeMeasures() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT, METRIC_WON).apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        analysisPage.reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT).reorderMetric(DERIVED_METRIC_WON, METRIC_WON)
                .changeReportType(ReportType.TABLE).waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertThat(metricsBucket.getItemNames(), hasItems(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON,
                DERIVED_METRIC_WON, METRIC_AVG_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY));

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe1stMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        //reorder metrics to comply requirement of test case
        analysisPage.reorderMetric(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(DERIVED_METRIC_AMOUNT));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (all) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertTrue(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M1"), "Checkbox of newly ones should be unchecked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe2ndMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        CompareApplyMeasure compareApplyMeasure = dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON);
        compareApplyMeasure.apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        //reorder metrics to comply requirement of test case
        analysisPage.reorderMetric(DERIVED_METRIC_WON, METRIC_WON).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(METRIC_WON));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(format("%s\n:\n%s\nCompare (1) to", DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME),
                        SAME_PERIOD_PREVIOUS_YEAR));
        assertFalse(filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).openCompareApplyMeasures()
                .isItemSelected(METRIC_AMOUNT + "M1"), "Checkbox of newly ones should be unchecked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromTableInsightToHeadlineInsightWhenApplyingThe3rdMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AVG_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        ElementUtils.makeSureNoPopupVisible();
        //reorder metrics to comply requirement of test case
        analysisPage.reorderMetric(DERIVED_METRIC_AVG_AMOUNT, METRIC_AVG_AMOUNT).changeReportType(ReportType.HEAD_LINE)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(METRIC_WON));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPY1stMeasureToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT).addDateFilter()
                .addMetricToSecondaryBucket(METRIC_WON);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPY2ndMeasureToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT)
                .addMetricToSecondaryBucket(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectByNames(METRIC_WON).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        ElementUtils.makeSureNoPopupVisible();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(METRIC_WON));

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_WON, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_WON, METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromHeadlineInsightWhichHasSPPYAllMeasuresToAnotherInsights() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE).addMetric(METRIC_AMOUNT)
                .addMetricToSecondaryBucket(METRIC_WON).addDateFilter();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                .selectAllValues().apply();
        dateFilterPickerPanel.apply();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), asList(DERIVED_METRIC_AMOUNT));

        ElementUtils.makeSureNoPopupVisible();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON,
                METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, DERIVED_METRIC_AMOUNT, DERIVED_METRIC_WON,
                METRIC_WON));
        assertEquals(attributesBucket.getItemNames(), emptyList());
        assertEquals(analysisPage.getStacksBucket().getWarningMessage(), ReportType.COLUMN_CHART.getStackByMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void placeInsightHasComparesMeasureOnKD() throws NoSuchFieldException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDateFilter().waitForReportComputing();
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(
                CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER);
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER).waitForWidgetsLoading();
        checkRedBar(browser);
        indigoDashboardsPage.selectDateFilterByName("This year").waitForWidgetsLoading();

        assertEquals(indigoDashboardsPage.selectFirstWidget(Insight.class).getChartReport()
                .getDataLabels(), asList("$3,644.00"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getHeadline(),
                INSIGHT_HAS_A_MEASURE_AND_APPLY_SPPY_FILTER);
        assertEquals(indigoDashboardsPage.selectFirstWidget(Insight.class)
                .getLegends(), asList(DERIVED_METRIC_AMOUNT, METRIC_AMOUNT));
    }

    private List<String> getTitles(Collection<String> listItems) {
        return listItems.stream()
                .map(item -> item.substring(0, item.lastIndexOf("M")))
                .collect(toList());
    }
}
