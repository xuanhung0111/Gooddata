package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PROBABILITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu.AggregationType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class GoodSalesTotalsInReportTest extends GoodSalesAbstractTest {

    private final static String SIMPLE_REPORT = "Simpe-Report";
    private final static String Q1_2011 = "Q1/2011";
    private final static String Q1_2012 = "Q1/2012";
    private final static String MAY_2011 = "May 2011";
    private final static String JAN_2011 = "Jan 2011";
    private final static String OF_ALL_ROWS = "Of All Rows";
    private final static String CLOSED_LOST = "Closed Lost";
    private final static String YEAR_2011 = "2011";
    private final static String BY_STAGE_NAME = "by Stage Name";
    private final static String BY_YEAR_SNAPSHOT = "by Year (Snapshot)";
    private final static String BY_QUARTER_YEAR_SNAPSHOT = "by Quarter/Year (Snapshot)";
    private final static String BY_OPPORTUNITY = "by Opportunity";
    private final static String MULTIPLE_TOTALS_REPORT = "Adding-Multiple-Rollup-And-Median-Totals";
    private final static String SHORT_LIST = "Short List";
    private final static String OF_ALL_COLUMNS = "of All Columns";

    @Test(dependsOnGroups = {"createProject"})
    public void createReport() {
        //create report on UI due to attribute order
        initReportCreation().createReport(new UiReportDefinition()
                .withName(SIMPLE_REPORT)
                .withWhats(METRIC_AMOUNT)
                .withWhats(METRIC_PROBABILITY)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP, Q1_2011, Q1_2012))
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT, "Access Insurance Holdings > Explorer",
                        "Access America Transport > Grammar Plus", "Access America Transport > Educationly",
                        "Access Information Management > PhoenixSoft")));
        takeScreenshot(browser, "simple-report", getClass());
    }

    @DataProvider(name = "rowTotalDataProvider")
    public Object[][] rowTotalDataProvider() {
        return new Object[][] {
                {AggregationType.SUM, asList(66519.20f, 90.0f, 117164.00f, 90.0f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 71166.00f,
                                30.0f, 60.0f, 0.0f)},
                {AggregationType.AVERAGE, asList(22173.07f, 30.0f, 29291.00f, 22.5f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 35583.00f,
                                30.0f, 60.0f, 0.0f)},
                {AggregationType.MINIMUM, asList(5998.00f, 0.0f, 5998.00f, 0.0f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 20521.20f,
                                30.0f, 60.0f, 0.0f)},
                {AggregationType.MAXIMUM, asList(40000.00f, 60.0f, 50644.80f, 60.0f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 50644.80f,
                                30.0f, 60.0f, 0.0f)},
                {AggregationType.MEDIAN, asList(20521.20f, 30.0f, 30260.60f, 15.0f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 35583.00f,
                                30.0f, 60.0f, 0.0f)},
                {AggregationType.ROLLUP, asList(66519.20f, 30.0f, 117164.00f, 22.5f),
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f, 5998.00f, 40000.00f, 71166.00f,
                                30.0f, 60.0f, 0.0f)}
        };
    }

    @Test(dependsOnMethods = {"createReport"}, dataProvider = "rowTotalDataProvider")
    public void addAndRemoveRowTotalOnAttributeHeader(final AggregationType type,
            final List<Float> totalValuesOfAllRows, final List<Float> totalValuesOfEachStage) {
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(type, OF_ALL_ROWS);
        checkNumberOfTotalHeaders(type, 1);

        //use List.equal() due to checking value order
        assertTrue(table.getTotalValues().equals(totalValuesOfAllRows), type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(type, BY_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();

        takeScreenshot(browser, "Row-" + type.getType() + "-total-values", getClass());
        checkNumberOfTotalHeaders(type, 4);

        assertTrue(
                table.getTotalValues().equals(
                        Stream.concat(totalValuesOfEachStage.stream(), totalValuesOfAllRows.stream())
                                .collect(Collectors.toList())),
                type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(YEAR_2011).nonAggregateTableData(type, OF_ALL_ROWS);
        checkNumberOfTotalHeaders(type, 3);

        //use List.equals() to check total value order
        assertTrue(table.getTotalValues().equals(totalValuesOfEachStage),
                type.getType() + " header & values of all rows have not been removed");

        table.openContextMenuFromCellValue(YEAR_2011).nonAggregateTableData(type, BY_STAGE_NAME);
        assertTrue(table.getTotalHeaders().isEmpty() && table.getTotalValues().isEmpty(),
                type.getType() + " headers & values have not been removed");
    }

    @DataProvider(name = "columnTotalDataProvider")
    public Object[][] columnTotalDataProvider() {
        return new Object[][] {
                {AggregationType.SUM,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)},
                {AggregationType.AVERAGE,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)},
                {AggregationType.MINIMUM,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)},
                {AggregationType.MAXIMUM,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)},
                {AggregationType.MEDIAN,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)},
                {AggregationType.ROLLUP,
                        asList(5998.00f, 40000.00f, 20521.20f, 30.0f, 60.0f, 0.0f)}
        };
    }

    @Test(dependsOnMethods = {"createReport"}, dataProvider = "columnTotalDataProvider")
    public void addAndRemoveColumnTotalOnAttributeHeader(final AggregationType type,
            List<Float> totalValuesOfAllColumns) {

        //minimize number of metrics to avoid handling scroll bar
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT)
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_YEAR_SNAPSHOT, YEAR_2011))
                .waitForReportExecutionProgress()
                .getTableReport();

        //total values for each year and all columns always are the same
        //because we only show data for 1 quarter
        final List<Float> totalValuesOfEachYear = totalValuesOfAllColumns;

        table.openContextMenuFromCellValue(SHORT_LIST).aggregateTableData(type, OF_ALL_COLUMNS);
        checkNumberOfTotalHeaders(type, 1);

        //use List.equal() due to checking value order
        assertTrue(table.getTotalValues().equals(totalValuesOfAllColumns), type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(SHORT_LIST).aggregateTableData(type, BY_YEAR_SNAPSHOT);
        reportPage.waitForReportExecutionProgress();

        takeScreenshot(browser, "Column-" + type.getType() + "-total-values", getClass());
        checkNumberOfTotalHeaders(type, 2);

        assertTrue(
                table.getTotalValues().equals(
                        Stream.concat(totalValuesOfEachYear.stream(), totalValuesOfAllColumns.stream())
                            .collect(Collectors.toList())),
                type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(SHORT_LIST).nonAggregateTableData(type, OF_ALL_COLUMNS);
        checkNumberOfTotalHeaders(type, 1);

        //use List.equals() to check total value order
        assertTrue(table.getTotalValues().equals(totalValuesOfEachYear),
                type.getType() + " headers & values of all columns have not been removed");

        table.openContextMenuFromCellValue(SHORT_LIST).nonAggregateTableData(type, BY_YEAR_SNAPSHOT);
        assertTrue(table.getTotalHeaders().isEmpty() && table.getTotalValues().isEmpty(),
                type.getType() + " headers & values have not been removed");
    }

  @DataProvider(name = "singleMetricTotalDataProvider")
  public Object[][] singleMetricTotalDataProvider() {
      return new Object[][] {
                {AggregationType.AVERAGE, asList(66519.20f, 0.0f, 0.0f, 30.0f, 117164.00f, 0.0f, 0.0f, 22.5f),
                        asList(22173.07f, 0.0f, 29291.00f, 0.0f)},
                {AggregationType.MINIMUM, asList(66519.20f, 0.0f, 0.0f, 0.0f, 117164.00f, 0.0f, 0.0f, 0.0f),
                        asList(5998.00f, 0.0f, 5998.00f, 0.0f)},
                {AggregationType.MAXIMUM, asList(66519.20f, 0.0f, 0.0f, 60.0f, 117164.00f, 0.0f, 0.0f, 60.0f),
                        asList(40000.00f, 0.0f, 50644.80f, 0.0f)},
                {AggregationType.MEDIAN, asList(66519.20f, 0.0f, 0.0f, 30.0f, 117164.00f, 0.0f, 0.0f, 15.0f),
                        asList(20521.20f, 0.0f, 30260.60f, 0.0f)},
                {AggregationType.ROLLUP, asList(66519.20f, 0.0f, 0.0f, 30.0f, 117164.00f, 0.0f, 0.0f, 22.5f),
                        asList(66519.20f, 0.0f, 117164.00f, 0.0f)}
      };
  }

    @Test(dependsOnMethods = {"createReport"}, dataProvider = "singleMetricTotalDataProvider")
    public void addAndRemoveTotalsForSingleMetric(final AggregationType type,
            final List<Float> otherMetricTotalValues,final List<Float> totalValues) {

        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, OF_ALL_ROWS);
        //use List.equal() due to checking value order
        assertTrue(table.getTotalValues().equals(asList(66519.20f, 0.0f, 117164.00f, 0.0f)), "The total values are not correct");

        table.openContextMenuFromCellValue(METRIC_PROBABILITY).aggregateTableData(type, OF_ALL_ROWS);
        takeScreenshot(browser, type.getType() + "-values-for-other-metric", getClass());
        //use List.equal() due to checking value order
        assertTrue(table.getTotalValues().equals(otherMetricTotalValues), type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(METRIC_PROBABILITY).nonAggregateTableData(type, OF_ALL_ROWS);
        assertTrue(isEqualCollection(table.getTotalValues(), asList(66519.20f, 0.0f, 117164.00f, 0.0f)),
                type.getType() + " values have not been removed");

        table.openContextMenuFromCellValue(METRIC_AMOUNT).nonAggregateTableData(AggregationType.SUM, OF_ALL_ROWS);
        assertTrue(table.getTotalHeaders().isEmpty() && table.getTotalValues().isEmpty(),
                "Total headers & values have not been removed");

        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(type, OF_ALL_ROWS);
        takeScreenshot(browser, type.getType() + "-values-for-single-metric", getClass());
        //use List.equal() due to checking value order
        assertTrue(table.getTotalValues().equals(totalValues), type.getType() + " values are not correct");

        table.openContextMenuFromCellValue(METRIC_AMOUNT).nonAggregateTableData(type, OF_ALL_ROWS);
        assertTrue(table.getTotalHeaders().isEmpty() && table.getTotalValues().isEmpty(),
                "Total headers & values have not been removed");
    }

    @Test(dependsOnMethods = {"createReport"})
    public void removeTotalsFromMetric() {
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.SUM, OF_ALL_ROWS);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.SUM, BY_STAGE_NAME);
        checkNumberOfTotalHeaders(AggregationType.SUM, 4);
        
        table.openContextMenuFromCellValue("90.0%").selectItem("Remove from Probability");
        assertTrue(table.getTotalValues().equals(asList(5998.00f, 40000.00f, 20521.20f, 30.0f,
                60.0f, 0.0f, 5998.00f, 40000.00f, 71166.00f, 30.0f, 60.0f, 0.0f, 66519.20f, 0.0f, 117164.00f, 0.0f)),
                "Totals of probability have not been removed");

        table.openContextMenuFromCellValue(table.getTotalHeaders().get(0))
                .selectItem("Remove from all metrics");

        assertTrue(table.getTotalValues().equals(asList(66519.20f, 0.0f, 117164.00f, 0.0f)),
                "Totals values have not been removed");
    }

    @Test(dependsOnMethods = {"createReport"})
    public void cancelAddingTotals() {
        initReportsPage().openReport(SIMPLE_REPORT).getTableReport().openContextMenuFromCellValue(YEAR_2011);

        final By contextMenuLocator = By.id("ctxMenu");
        assertTrue(isElementVisible(contextMenuLocator, browser), "The context menu is not displayed");

        //click on anywhere to close context menu
        reportPage.getRoot().click();
        assertFalse(isElementVisible(contextMenuLocator, browser), "The context menu is displayed");
    }

    @DataProvider(name = "totalsOfTotalsDataProvider")
    public Object[][] totalsOfTotalsDataProvider() {
        return new Object[][] {
            {AggregationType.SUM,
                    asList(11996.00f, 80000.00f, 50644.80f, 41042.40f,
                            60.0f, 120.0f, 0.0f, 0.0f, 66519.20f, 90.0f, 117164.00f, 90.0f, 183683.20f, 180.0f)},
            {AggregationType.AVERAGE,
                    asList(5998.00f, 40000.00f, 50644.80f, 20521.20f,
                            30.0f, 60.0f, 0.0f, 0.0f, 22173.07f, 30.0f, 29291.00f, 22.5f, 0.0f, 0.0f)},
            {AggregationType.MINIMUM,
                    asList(5998.00f, 40000.00f, 50644.80f, 20521.20f,
                            30.0f, 60.0f, 0.0f, 0.0f, 5998.00f, 0.0f, 5998.00f, 0.0f, 5998.00f, 0.0f)},
            {AggregationType.MAXIMUM,
                    asList(5998.00f, 40000.00f, 50644.80f, 20521.20f,
                            30.0f, 60.0f, 0.0f, 0.0f, 40000.00f, 60.0f, 50644.80f, 60.0f, 50644.80f, 60.0f)},
            {AggregationType.MEDIAN,
                    asList(5998.00f, 40000.00f, 50644.80f, 20521.20f,
                            30.0f, 60.0f, 0.0f, 0.0f, 20521.20f, 30.0f, 30260.60f, 15.0f, 0.0f, 0.0f)},
            {AggregationType.ROLLUP,
                    asList(5998.00f, 40000.00f, 50644.80f, 20521.20f,
                            30.0f, 60.0f, 0.0f, 0.0f, 66519.20f, 30.0f, 117164.00f, 22.5f, 117164.00f, 22.5f)},
        };
    }

    @Test(dependsOnMethods = {"createReport"}, dataProvider = "totalsOfTotalsDataProvider")
    public void addTotalsOfTotals(final AggregationType type, final List<Float> totalValues) {
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFromCellValue(Q1_2011).aggregateTableData(type, OF_ALL_ROWS);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(SHORT_LIST)
                .aggregateTableData(type, OF_ALL_COLUMNS);

        assertTrue(table.getTotalValues().equals(totalValues), type.getType() + " values are not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportForAddingMultipleTotals() {
        initReportCreation().createReport(new UiReportDefinition()
                .withName(MULTIPLE_TOTALS_REPORT)
                .withWhats(METRIC_AMOUNT)
                .withWhats(METRIC_PROBABILITY)
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT,
                        "1000Bulbs.com > Educationly", "1000Bulbs.com > PhoenixSoft", "101 Financial > Educationly"))
                .withHows(new HowItem(ATTR_ACCOUNT, Position.LEFT))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_MONTH_YEAR_SNAPSHOT, Position.TOP, JAN_2011, MAY_2011)));
    }

    @Test(dependsOnMethods = {"createReportForAddingMultipleTotals"})
    public void addMultipleRollupTotals() {
        final TableReport table = initReportsPage().openReport(MULTIPLE_TOTALS_REPORT).getTableReport();
        table.openContextMenuFromCellValue(MAY_2011).aggregateTableData(AggregationType.ROLLUP, BY_OPPORTUNITY);
        checkNumberOfTotalHeaders(AggregationType.ROLLUP, 3);

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.ROLLUP, BY_STAGE_NAME);
        checkNumberOfTotalHeaders(AggregationType.ROLLUP, 4);

        table.openContextMenuFromCellValue(Q1_2011).aggregateTableData(AggregationType.ROLLUP, OF_ALL_ROWS);
        checkNumberOfTotalHeaders(AggregationType.ROLLUP, 5);
        assertTrue(
                table.getTotalValues()
                        .equals(asList(0.0f, 18000.00f, 24000.00f, 42000.00f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                                18000.00f, 24000.00f, 42000.00f, 0.0f, 0.0f, 0.0f, 0.0f, 42000.00f, 0.0f,
                                42000.00f, 0.0f)),
                "Rollup values are not correct");

        //minimize number of metrics to 1 to avoid handling scroll bar
        reportPage.openWhatPanel()
                .selectInapplicableMetric(METRIC_PROBABILITY)
                .doneSndPanel()
                .waitForReportExecutionProgress()
                .openFilterPanel()
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_QUARTER_YEAR_SNAPSHOT, Q1_2011));
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(CLOSED_LOST).aggregateTableData(AggregationType.ROLLUP, OF_ALL_COLUMNS);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(CLOSED_LOST).aggregateTableData(AggregationType.ROLLUP, BY_YEAR_SNAPSHOT);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(CLOSED_LOST).aggregateTableData(AggregationType.ROLLUP, BY_QUARTER_YEAR_SNAPSHOT);
        reportPage.waitForReportExecutionProgress();

        checkNumberOfTotalHeaders(AggregationType.ROLLUP, 8);
        assertTrue(
                table.getTotalValues().equals(
                        asList(0.0f, 18000.00f, 24000.00f, 42000.00f,
                                0.0f, 0.0f, 18000.00f, 18000.00f, 24000.00f, 24000.00f, 42000.00f,
                                0.0f, 0.0f, 18000.00f, 18000.00f, 24000.00f, 24000.00f, 42000.00f, 
                                0.0f, 0.0f, 18000.00f, 18000.00f, 24000.00f, 24000.00f, 42000.00f,
                                42000.00f, 42000.00f, 42000.00f, 42000.00f)),
                "Rollup values are not correct");
    }

    @Test(dependsOnMethods = {"createReportForAddingMultipleTotals"})
    public void addMultipleMedianTotals() throws FileNotFoundException, IOException {
        final TableReport table = initReportsPage().openReport(MULTIPLE_TOTALS_REPORT).getTableReport();
        table.openContextMenuFromCellValue(MAY_2011).aggregateTableData(AggregationType.MEDIAN, BY_OPPORTUNITY);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.MEDIAN, BY_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(Q1_2011).aggregateTableData(AggregationType.MEDIAN, OF_ALL_ROWS);

        checkNumberOfTotalHeaders(AggregationType.MEDIAN, 5);

        assertTrue(
                table.getTotalValues()
                        .equals(asList(0.0f, 18000.00f, 24000.00f, 21000.00f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 18000.00f,
                                24000.00f, 21000.00f, 0.0f, 0.0f, 0.0f, 0.0f, 21000.00f, 0.0f, 21000.00f, 0.0f)),
                "Median values are not correct");

        reportPage.exchangeColAndRowHeaders();

        List<List<String>> actualResult = new ArrayList<>();
        try (CsvListReader reader = new CsvListReader(new FileReader(new File(testParams.getDownloadFolder(),
                reportPage.exportReport(ExportFormat.CSV) + ".csv")), CsvPreference.STANDARD_PREFERENCE)) {

            reader.getHeader(true);
            List<String> reportResult;

            while ((reportResult = reader.read()) != null) {
                if(reportResult.get(0) != null && reportResult.get(0).equals(YEAR_2011)) {
                    actualResult.add(reportResult);
                }
            }
        }

        assertEquals(actualResult, asList(
                asList("2011", "Q1/2011", "Jan 2011", null, "0", null, "0", "18000", "0", "18000", "0", "24000", "0",
                        "24000", "0", "21000", "0", "21000", "0"),
                asList("2011", "Q2/2011", "May 2011", null, "0", null, "0", "18000", "0", "18000", "0", "24000", "0",
                        "24000", "0", "21000", "0", "21000", "0")));

        //minimize number of metrics to 1 to avoid handling scroll bar
        reportPage.exchangeColAndRowHeaders()
                .openWhatPanel()
                .selectInapplicableMetric(METRIC_PROBABILITY)
                .doneSndPanel()
                .waitForReportExecutionProgress();

        reportPage.displayMetricsInDifferentRows();
        assertTrue(
                table.getTotalValues().equals(
                        asList(0.0f, 18000.00f, 24000.00f, 21000.00f, 0.0f, 18000.00f,
                                24000.00f, 21000.00f, 21000.00f, 21000.00f)),
                "Median values are not correct");
    }

    private void checkNumberOfTotalHeaders(final AggregationType type, final int expectedNumber) {
        final List<String> totalHeaders = reportPage.getTableReport().getTotalHeaders();
        assertTrue(
                totalHeaders.size() == expectedNumber && totalHeaders.stream().allMatch(e -> e.equals(type.getType())),
                type.getType() + " headers are not as expected");
    }
}
