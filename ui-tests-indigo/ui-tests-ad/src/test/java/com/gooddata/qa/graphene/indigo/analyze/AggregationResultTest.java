package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.CompareType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationItem;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.annotations.Test;

import java.text.ParseException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AggregationResultTest extends AbstractAnalyseTest {

    private static final String SP_YEAR_AGO = " - SP year ago";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Aggregation-Result-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createCloseEOPMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void deleteAggregationResultFromCell() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_CLOSE_EOP)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES)
                .addTotalsForCell(AggregationItem.MAX, METRIC_CLOSE_EOP);
        assertTrue(tableReport.hoverItem(tableReport.getTotalsElement(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES))
                .isRemoveTotalsResultCellButtonVisible(), "remove aggregation result cell button should be displayed");

        tableReport.deleteTotalsResultCell(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
        Screenshots.takeScreenshot(browser, "delete-aggregation-result-from-cell", getClass());
        assertTrue(tableReport.isAddTotalResultCellButtonVisible());
    }

    @Test(dependsOnGroups = "createProject")
    public void deleteAggregationResultFromRow() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(tableReport.hoverItem(tableReport.getTotalsElement(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES))
                .isRemoveTotalsResultButtonVisible(), "remove aggregation result row button should be displayed");

        tableReport.deleteTotalsResultRow(AggregationItem.MAX);
        Screenshots.takeScreenshot(browser, "delete-aggregation-result-from-row", getClass());
        assertTrue(tableReport.getEnabledAggregations(METRIC_NUMBER_OF_ACTIVITIES)
                .contains(AggregationItem.MAX.getFullName()), "Max aggregation should be enabled");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalHasNoAttributeWithPOP() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addDate()
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        checkTableTotalWithSamePeriodComparison("73,073");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalHasAttributeWithPOP() throws ParseException {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .addDateFilter().getFilterBuckets().configDateFilter("01/01/2015", "12/31/2015");
        analysisPage.waitForReportComputing();
        checkTableTotalWithSamePeriodComparison("3");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterAddAndRemoveMeasure() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.addMetric(METRIC_CLOSE_EOP).waitForReportComputing();
        assertTrue(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_CLOSE_EOP).isEmpty(),
                format("Total cell of %s should be empty", METRIC_CLOSE_EOP));

        analysisPage.removeMetric(METRIC_CLOSE_EOP).waitForReportComputing();
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterRemoveAllMeasure() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertFalse(tableReport.hasTotalsResult(), "The total rows should be hidden");

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertFalse(tableReport.hasTotalsResult(), "The hidden total rows should not be restored");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterSortMetrics() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_TIMELINE_EOP)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        tableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterAddDateFilter() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.addDateFilter().waitForReportComputing();
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterShowPercent() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();
        analysisPage.waitForReportComputing();
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, "% " + METRIC_NUMBER_OF_ACTIVITIES), "65.50%");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterRemoveAllAttribute() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.removeAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertFalse(tableReport.hasTotalsResult(), "The total rows should be hidden");

        analysisPage.addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertFalse(tableReport.hasTotalsResult(), "The hidden total rows should not be restored");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkTableTotalAfterChangeTypeReport() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertTrue(tableReport.hasTotalsResult(), "The hidden total rows should be restored");
    }

    private void checkTableTotalWithSamePeriodComparison(String cellValue) {
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

        analysisPage.applyCompareType(CompareType.SAME_PERIOD_LAST_YEAR);

        assertTrue(tableReport.getTotalsElement(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO).isDisplayed(),
                format("Total cell of metric %s should be displayed", METRIC_NUMBER_OF_ACTIVITIES));

        tableReport.deleteTotalsResultCell(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO);

        analysisPage.applyCompareType(CompareType.NOTHING);

        assertFalse(tableReport.getHeaders().contains(METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO),
                format("Metric %s should be hidden", METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO));
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), cellValue);
    }
}
