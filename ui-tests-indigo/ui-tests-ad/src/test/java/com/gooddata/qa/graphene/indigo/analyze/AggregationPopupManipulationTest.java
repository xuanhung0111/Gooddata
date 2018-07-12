package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationItem;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationPopup;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AggregationPopupManipulationTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Aggregation Popup";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createNumberOfActivitiesMetric();
        metrics.createCloseEOPMetric();
    }

    /**
     * Data provider to test hovering on metric column
     * @return 2 dimension array contains:
     * List of measures to add to a table report
     * List of attributes to add to a table report
     * List of filters: attribute to be used as filter and expected filter value
     * List of results: hovered column (attribute/measure) and its aggregation status (available or not)
     */
    @DataProvider(name = "getHoveringTestData")
    public Object[][] getHoveringTestData() {
        return new Object[][]{
                // case1: no measure, one attribute, hover on attribute column and don't show Total icon
                {Collections.emptyList(), Arrays.asList(ATTR_DEPARTMENT),
                        Collections.emptyList(), Arrays.asList(Pair.of(ATTR_DEPARTMENT, false))},
                // case2: one measure, no attribute, hover on measure column and don't show Total icon
                {Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList(),
                        Collections.emptyList(), Arrays.asList(Pair.of(METRIC_NUMBER_OF_ACTIVITIES, false))},
                // case3: one measure, one attribute, filter to limit the data into row, hover on measure column
                // and show Total icon, hover on attribute column and don't show Total icon.
                {Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Arrays.asList(ATTR_DEPARTMENT), Arrays.asList(
                        Pair.of(ATTR_DEPARTMENT, Arrays.asList("Direct Sales"))
                ), Arrays.asList(Pair.of(METRIC_NUMBER_OF_ACTIVITIES, true), Pair.of(ATTR_DEPARTMENT, false))}
        };
    }

    @Test(dataProvider = "getHoveringTestData", dependsOnGroups = "createProject")
    public void testHovering(List<String> measures, List<String> attributes,
                             List<Pair<String, List<String>>> filters, List<Pair<String, Boolean>> results)
            throws ParseException {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);

            measures.forEach(measure -> analysisPage.addMetric(measure));
            attributes.forEach(attribute -> analysisPage.addAttribute(attribute));

            filters.forEach(filter -> analysisPage.getFilterBuckets().configAttributeFilter(filter.getLeft(),
                    (String[]) filter.getRight().toArray()));
            analysisPage.waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport();
            results.forEach(result -> {
                tableReport.hoverOnColumn(result.getLeft());
                assertEquals(tableReport.isTotalsElementShowed(result.getLeft()), result.getRight().booleanValue());
                if(result.getRight().booleanValue()) {
                    tableReport.openAggregationPopup(result.getLeft());
                    tableReport.closeAggregationPopup(result.getLeft());
                }
            });
        } finally {
            analysisPage.resetToBlankState();
        }

    }

    @Test(dependsOnGroups = "createProject")
    public void testAggregationHasNullString() throws ParseException {
        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount")));
        createMetric("EMPTY_SHOW_NULL_STRING", metricExpression, "#'##0,00 formatted; [=null] null value!");

        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_AMOUNT)
                    .addDate();
            analysisPage.addMetric("EMPTY_SHOW_NULL_STRING");
            analysisPage.waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport().addNewTotals(AggregationItem.SUM
                    , METRIC_AMOUNT);
            tableReport.addTotalsForCell(AggregationItem.SUM, "EMPTY_SHOW_NULL_STRING");

            assertEquals(tableReport.getTotalsValue(AggregationItem.SUM, METRIC_AMOUNT), "$116,625,456.54");
            assertEquals(tableReport.getTotalsValue(AggregationItem.SUM, "EMPTY_SHOW_NULL_STRING"), "null value!");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testAggregationWithoutNullString() throws ParseException {
        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount")));
        createMetric("EMPTY_NO_NULL_STRING", metricExpression, "#'##0,00");

        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_AMOUNT)
                    .addDate();
            analysisPage.addMetric("EMPTY_NO_NULL_STRING");
            analysisPage.waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport().addNewTotals(AggregationItem.SUM
                    , METRIC_AMOUNT);
            tableReport.addTotalsForCell(AggregationItem.SUM, "EMPTY_NO_NULL_STRING");

            assertEquals(tableReport.getTotalsValue(AggregationItem.SUM, METRIC_AMOUNT), "$116,625,456.54");
            assertEquals(tableReport.getTotalsValue(AggregationItem.SUM, "EMPTY_NO_NULL_STRING"), "–");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testShowAggregationPopup() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
            TableReport tableReport = analysisPage.getTableReport();
            List<String> aggregations = tableReport.getAggregations(METRIC_NUMBER_OF_ACTIVITIES);
            List<String> expectedAggregation = AggregationItem.getAllFullNames();
            assertEquals(aggregations, expectedAggregation, "List of aggregation's full names does not match");
        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testMaximumAggregationFunction() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport()
                    .addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
            List<String> functions = tableReport.getEnabledAggregations(METRIC_NUMBER_OF_ACTIVITIES);
            Collections.shuffle(functions);
            functions.stream().forEach(aggFunctionText -> {
                tableReport.addNewTotals(AggregationItem.fromString(aggFunctionText), METRIC_NUMBER_OF_ACTIVITIES);
            });

            tableReport.hoverOnColumn(METRIC_NUMBER_OF_ACTIVITIES);
            assertFalse(tableReport.isTotalsElementShowed(METRIC_NUMBER_OF_ACTIVITIES),
                    "Total button should not be showed for metric number of activities");

            List<String> texts = tableReport.getAggregationRows();
            assertEquals(texts, AggregationItem.getAllRowNames(), "Aggregation rows order is not correct!");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testHidingOpenReport() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
            analysisPage.getTableReport().addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

            assertFalse(analysisPage.getPageHeader().isExportButtonEnabled(), "export report should be disabled");
            String tooltip = analysisPage.getPageHeader().getExportButtonTooltipText();
            assertEquals(tooltip, "The insight is not compatible with Report Editor. To open the insight as a report, " +
                    "remove table totals from the insight definition.");
        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testAddIconOnAggregationCell() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_CLOSE_EOP)
                    .addAttribute(ATTR_DEPARTMENT).waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport();
            tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

            tableReport.hoverOnColumn(METRIC_CLOSE_EOP);
            assertTrue(tableReport.isAddAggregationButtonOnCell(AggregationItem.MAX, METRIC_CLOSE_EOP),
                    "add button should be showed on aggregation max of close eop metric");

            tableReport.addTotalsForCell(AggregationItem.MAX, METRIC_CLOSE_EOP);
            WebElement maxOfCloseEOP = tableReport.getTotalsElement(AggregationItem.MAX, METRIC_CLOSE_EOP);
            assertEquals(maxOfCloseEOP.getText(), "42,794.00");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testCloseAggregationPopup() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
            TableReport tableReport = analysisPage.getTableReport();
            AggregationPopup popUp = tableReport.openAggregationPopup(METRIC_NUMBER_OF_ACTIVITIES);
            assertTrue(CollectionUtils.isNotEmpty(browser
                    .findElements(AggregationPopup.LOCATOR)), "Aggregation popup shoud be visible");
            tableReport.closeAggregationPopup(METRIC_NUMBER_OF_ACTIVITIES);
            waitForFragmentNotVisible(popUp);
            assertTrue(CollectionUtils.isEmpty(browser
                    .findElements(AggregationPopup.LOCATOR)), "Aggregation popup shoud be closed");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testAddTotalRowToFooter() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
            TableReport tableReport = analysisPage.getTableReport();

            tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);

            assertTrue(CollectionUtils.isEmpty(browser
                    .findElements(AggregationPopup.LOCATOR)), "Aggregation popup shoud be closed");
            assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054",
                    "Aggregation max is not correct");
            WebElement maxOfActivities = tableReport.getTotalsElement(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
            assertEquals(maxOfActivities.getText(), "101,054", "Tooltip of aggregation max is not correct");

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testHoverReportWithDateAttribute() {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();

            analysisPage.getFilterBuckets().openDateFilterPickerPanel().applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);
            analysisPage.waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport();
            tableReport.hoverOnColumn(METRIC_NUMBER_OF_ACTIVITIES);
            assertTrue(tableReport.isTotalsElementShowed(METRIC_NUMBER_OF_ACTIVITIES),
                    "Total button for number of activities metric must be showed");
            tableReport.openAggregationPopup(METRIC_NUMBER_OF_ACTIVITIES);
            tableReport.closeAggregationPopup(METRIC_NUMBER_OF_ACTIVITIES);

        } finally {
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testHoverReportWithDateFilter() throws ParseException {
        AnalysisPage analysisPage = initAnalysePage();
        try {
            analysisPage.changeReportType(ReportType.TABLE);

            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter()
                    .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011")
                    .getRoot().click();

            analysisPage.getFilterBuckets().openDateFilterPickerPanel().applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);
            analysisPage.waitForReportComputing();

            TableReport tableReport = analysisPage.getTableReport();
            tableReport.hoverOnColumn(METRIC_NUMBER_OF_ACTIVITIES);
            assertFalse(tableReport.isTotalsElementShowed(METRIC_NUMBER_OF_ACTIVITIES),
                    "Total button should not be showed for metric number of activities");

        } finally {
            analysisPage.resetToBlankState();
        }
    }
}
