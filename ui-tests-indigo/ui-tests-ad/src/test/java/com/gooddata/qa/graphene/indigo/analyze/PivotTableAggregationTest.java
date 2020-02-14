package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.apache.commons.lang.StringUtils.EMPTY;

public class PivotTableAggregationTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Pivot-Table-Aggregation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createCloseEOPMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void deleteTableTotalItem() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_CLOSE_EOP)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        assertTrue(pivotTableReport.containsGrandTotals());

        pivotTableReport.removeTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser, "delete-aggregation-from-table", getClass());

        assertFalse(pivotTableReport.containsGrandTotals());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkTableTotalHasNoAttributeWithPOP() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addDate()
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.getAttributesBucket().changeDateDimension(DATE_DATASET_ACTIVITY);
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.getFilterBuckets()
            .openDateFilterPickerPanel()
            .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
            .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "73,073", "73,073"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);

        analysisPage.getFilterBuckets()
            .openDateFilterPickerPanel()
            .applyCompareType(CompareTypeDropdown.CompareType.NOTHING);

        analysisPage.waitForReportComputing();

        expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "73,073"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkTableTotalHasAttributeWithPOP() throws ParseException {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .addDateFilter().getFilterBuckets().configDateFilter("01/01/2015", "12/31/2015");
        analysisPage.waitForReportComputing();


        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.getFilterBuckets()
            .openDateFilterPickerPanel()
            .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "9", "3"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);

        analysisPage.getFilterBuckets()
            .openDateFilterPickerPanel()
            .applyCompareType(CompareTypeDropdown.CompareType.NOTHING);

        analysisPage.waitForReportComputing();

        expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "3"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterAddAndRemoveMeasure() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.addMetric(METRIC_CLOSE_EOP)
            .waitForReportComputing();

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054", "42,794.00"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);

        analysisPage.removeMetric(METRIC_CLOSE_EOP)
            .waitForReportComputing();

        expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterRemoveAllMeasure() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertFalse(pivotTableReport.containsGrandTotals(), "The total rows should be hidden");

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertFalse(pivotTableReport.containsGrandTotals(), "The hidden total rows should not be restored");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterSortMetrics() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_TIMELINE_EOP)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.waitForReportComputing();

        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054", EMPTY));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterAddDateFilter() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.addDateFilter().waitForReportComputing();
        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterShowPercent() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();
        analysisPage.waitForReportComputing();

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "65.50%"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterRemoveAllAttribute() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport tableReport = analysisPage.getPivotTableReport();
        tableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.removeAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertFalse(tableReport.containsGrandTotals(), "The total rows should be hidden");

        analysisPage.addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertFalse(tableReport.containsGrandTotals(), "The hidden total rows should not be restored");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterChangeTypeReport() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertTrue(pivotTableReport.containsGrandTotals(), "The hidden total rows should be restored");
    }
}
