package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown.CompareType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InsightCompareToPreviousPeriodTest extends AbstractAnalyseTest {

    @Override
    protected void customizeProject() throws Throwable {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        // TODO: BB-1448 enablePivot FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_PIVOT_TABLE, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);

        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createTimelineEOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkStatusOfPreviousPeriodCompare() {
        initAnalysePage().addDateFilter();
        DateFilterPickerPanel dateFilterPickerPanel = analysisPage.getFilterBuckets().openDateFilterPickerPanel();
        assertTrue(dateFilterPickerPanel.selectStaticPeriod().isCompareTypeEnabled(CompareType.PREVIOUS_PERIOD),
                "Compare the period with previous period should be enabled");
        assertFalse(dateFilterPickerPanel.changePeriod(DateRange.ALL_TIME.toString())
                .isCompareTypeEnabled(CompareType.PREVIOUS_PERIOD),
                "All time period shouldn't apply to previous period compare");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyPreviousPeriodCompare() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_TIMELINE_EOP).addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("01/01/2016", "12/31/2017")
                .changeCompareType(CompareType.PREVIOUS_PERIOD);
        assertEquals(dateFilterPickerPanel.getCompareApplyMeasuresText(), "All measures");

        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES,
                        METRIC_TIMELINE_EOP + " - period ago", METRIC_TIMELINE_EOP));
        assertEquals(filtersBucket.getDateFilterText(),
                    "Activity\n:\nJan 1, 2016 - Dec 31, 2017\nCompare (all) to:\nPrevious period");

        filtersBucket.openDateFilterPickerPanel()
                .openCompareApplyMeasures().selectByNames(METRIC_NUMBER_OF_ACTIVITIES).apply();
        assertEquals(dateFilterPickerPanel.getCompareApplyMeasuresText(), "Individual selection (1)");

        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES, METRIC_TIMELINE_EOP));
        assertEquals(analysisPage.waitForReportComputing().getPivotTableReport().getBodyContent(),
                singletonList(asList("18", "7", "44,195")));
        assertEquals(filtersBucket.getDateFilterText(),
                "Activity\n:\nJan 1, 2016 - Dec 31, 2017\nCompare (1) to:\nPrevious period");
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();
        assertEquals(analysisPageHeader.getExportButtonTooltipText(), "The insight is not compatible with Report Editor." +
                " To open the insight as a report, disable compare in the insight definition.");
        analysisPage.saveInsight("Insight Text");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void canNotApplyPreviousPeriodCompareWithAllTimePeriod() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        filtersBucket.openDateFilterPickerPanel()
                .changePeriod(DateRange.LAST_7_DAYS.toString())
                .changeCompareType(CompareType.PREVIOUS_PERIOD)
                .apply();
        assertFalse(filtersBucket.openDateFilterPickerPanel().changePeriod(DateRange.ALL_TIME.toString())
                .isCompareTypeEnabled(CompareType.PREVIOUS_PERIOD),
                "All time period shouldn't apply to previous period compare");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeTypeOfCompare() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_TIMELINE_EOP).addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDateFilterPickerPanel();
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("01/01/2016", "12/31/2017")
                .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR)
                .openCompareApplyMeasures().selectByNames(METRIC_NUMBER_OF_ACTIVITIES)
                .apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser, "Change type of compare", getClass());
        filtersBucket.openDateFilterPickerPanel().changeCompareType(CompareType.PREVIOUS_PERIOD);
        assertEquals(dateFilterPickerPanel.getCompareApplyMeasuresText(), "Individual selection (1)");
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES, METRIC_TIMELINE_EOP));

        filtersBucket.openDateFilterPickerPanel().changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        assertEquals(dateFilterPickerPanel.getCompareApplyMeasuresText(), "Individual selection (1)");
        dateFilterPickerPanel.apply();
        assertEquals(analysisPage.waitForReportComputing().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - SP year ago", METRIC_NUMBER_OF_ACTIVITIES, METRIC_TIMELINE_EOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeToNothingCompare() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_TIMELINE_EOP).addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDateFilterPickerPanel();
        dateFilterPickerPanel
                .configTimeFilterByRangeHelper("01/01/2016", "12/31/2017")
                .changeCompareType(CompareType.PREVIOUS_PERIOD)
                .openCompareApplyMeasures().selectByNames(METRIC_NUMBER_OF_ACTIVITIES)
                .apply();
        dateFilterPickerPanel.apply();

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser, "Change to nothing compare", getClass());
        filtersBucket.openDateFilterPickerPanel().changeCompareType(CompareType.NOTHING).apply();
        assertEquals( analysisPage.waitForReportComputing().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_TIMELINE_EOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void notApplyWeekFilterWithPreviousPeriodCompare()  {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        filtersBucket.openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("01/01/2016", "12/31/2017")
                .changeCompareType(CompareType.PREVIOUS_PERIOD)
                .apply();
        assertEquals(analysisPage.addDate().getAttributesBucket().getAllGranularities(),
                asList(DateGranularity.DAY.toString(), DateGranularity.MONTH.toString(),
                        DateGranularity.QUARTER.toString(), DateGranularity.YEAR.toString()));

        initAnalysePage().addDate().getAttributesBucket().changeGranularity(DateGranularity.WEEK_SUN_SAT);
        assertFalse(analysisPage.getFilterBuckets().openDateFilterPickerPanel().selectStaticPeriod()
                .isCompareTypeEnabled(CompareType.PREVIOUS_PERIOD), "Not apply week filter with previous period compare");
    }
}
