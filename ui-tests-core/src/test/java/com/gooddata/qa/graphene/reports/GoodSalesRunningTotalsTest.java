package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu.AggregationType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;

public class GoodSalesRunningTotalsTest extends GoodSalesAbstractTest {

    private final static String RUNNING_TOTAL = "Running-Totals";
    private final static String AMOUNT = "Amount";
    private final static String PROBABILITY = "Probability";
    private final static String STAGE_NAME = "Stage Name";
    private final static String YEAR_2011 = "2011";
    private final static String YEAR_SNAPSHOT = "Year (Snapshot)";
    private final static String QUARTER_YEAR_SNAPSHOT = "Quarter/Year (Snapshot)";
    private final static String RUNNING_SUM = "Running Sum";
    private final static String RUNNING_AVERAGE = "Running Average";
    private final static String RUNNING_MINIMUM = "Running Minimum";
    private final static String RUNNING_MAXIMUM = "Running Maximum";
    private final static String INTEREST = "Interest";
    private final static String DISCOVERY = "Discovery";
    private final static String SHORT_LIST = "Short List";
    private final static String Q1_2011 = "Q1/2011";
    private final static String Q2_2011 = "Q2/2011";
    private final static String STATUS = "Status";
    private final static String AVG_AMOUNT = "Avg. Amount";
    
    @Test(dependsOnGroups = "createProject")
    public void createRunningTotalReport() {
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(RUNNING_TOTAL)
                .withWhats(PROBABILITY)
                .withWhats(AMOUNT)
                .withHows(new HowItem(STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(YEAR_SNAPSHOT, Position.TOP, YEAR_2011)));
    }

    @DataProvider(name = "runningTotalDataProvider")
    public Object[][] runningTotalDataProvider() {
        return new Object[][] {
            {RUNNING_SUM, RUNNING_SUM, asList(10.0f, 20.0f, 30.0f, 10.0f, 30.0f, 60.0f, 16427388.57f, 3436167.70f,
                    3903521.33f, 16427388.57f, 19863556.27f, 23767077.60f)},
            {RUNNING_AVERAGE, "Running Avg", asList(10.0f, 20.0f, 30.0f, 10.0f, 15.0f, 20.0f, 16427388.57f,
                    3436167.70f, 3903521.33f, 16427388.57f, 9931778.13f, 7922359.20f)},
            {RUNNING_MINIMUM, "Running Min", asList(10.0f, 20.0f, 30.0f, 10.0f, 10.0f, 10.0f, 16427388.57f,
                    3436167.70f, 3903521.33f, 16427388.57f, 3436167.70f, 3436167.70f)},
            {RUNNING_MAXIMUM, "Running Max", asList(10.0f, 20.0f, 30.0f, 10.0f, 20.0f, 30.0f, 16427388.57f,
                    3436167.70f, 3903521.33f, 16427388.57f, 16427388.57f, 16427388.57f)}
        };
    }

    @Test(dependsOnMethods = "createRunningTotalReport", dataProvider = "runningTotalDataProvider")
    public void addRunningTotalsToReport(final String type,
            final String runningTotalHeader, final List<Float> runningTotalValues) {

        final TableReport table = initReportsPage().openReport(RUNNING_TOTAL).getTableReport();
        table.openContextMenuFromCellValue(YEAR_2011)
                .aggregateTableData(AggregationType.RUNNING, type);
        assertTrue(table.getMetricsHeader().contains(runningTotalHeader), type + " headers have not been added");
        assertTrue(table.getMetricElements().equals(runningTotalValues), type + " values are not correct");

        final List<Float> metricValues = asList(10.0f, 20.0f, 30.0f, 16427388.57f, 3436167.70f, 3903521.33f);
        table.openContextMenuFromCellValue(YEAR_2011).nonAggregateTableData(AggregationType.RUNNING, type);
        assertFalse(table.getMetricsHeader().contains(runningTotalHeader), type + " headers has not been removed");
        assertEquals(table.getMetricElements(), metricValues, type + " values has not been removed");

        table.openContextMenuFromCellValue(INTEREST).aggregateTableData(AggregationType.RUNNING, type);
        assertTrue(table.getMetricsHeader().contains(runningTotalHeader), type + " headers have not been added");
        assertEquals(table.getMetricElements(), runningTotalValues, type + " values are not correct");

        table.openContextMenuFromCellValue(INTEREST).nonAggregateTableData(AggregationType.RUNNING, type);
        assertFalse(table.getMetricsHeader().contains(runningTotalHeader), type + " headers has not been removed");
        assertEquals(table.getMetricElements(), metricValues, type + " values has not been removed");
    }

    @Test(dependsOnMethods = "createRunningTotalReport")
    public void customizeMetricAndAttributeInRunningSumReport() {
        final TableReport table = initReportsPage().openReport(RUNNING_TOTAL).getTableReport();

        table.openContextMenuFromCellValue(YEAR_2011)
                .aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);

        reportPage.waitForReportExecutionProgress()
                .openWhatPanel()
                .selectMetric(AVG_AMOUNT)
                .deselectMetric(PROBABILITY) //minimize number of metrics to avoid handling scroll bar
                .doneSndPanel()
                .waitForReportExecutionProgress();

        assertTrue(table.getMetricsHeader().stream().filter(e -> e.equals(RUNNING_SUM)).count() == 1,
                "Running sum header is calculated for new metric");
        assertEquals(
                table.getMetricElements(), asList(16427388.57f, 3436167.70f, 3903521.33f, 16427388.57f, 19863556.27f,
                        23767077.60f, 130376.10f, 25265.94f, 29797.87f),
                "Running sum values are calculated for new metric");

        reportPage.openHowPanel()
                .selectAttributes(singletonList(new HowItem(QUARTER_YEAR_SNAPSHOT, Position.TOP, Q1_2011, Q2_2011)))
                .doneSndPanel()
                .waitForReportExecutionProgress();

        //use List.equal() to check value's order
        assertTrue(
                table.getMetricElements()
                        .equals(asList(1719072.21f, 1456305.27f, 1772094.56f, 1719072.21f, 3175377.48f, 4947472.04f,
                                34381.44f, 21105.87f, 24959.08f, 1663660.67f, 2350525.55f, 3121266.12f, 1663660.67f,
                                4014186.22f, 7135452.34f, 25206.98f, 27653.24f, 32855.43f)),
                "Running sum values are not correct");
    }

    @Test(dependsOnGroups = "createProject")
    public void testRunningTotalIsNotMetric() {
        final String metricAlias = "Amount-Sum";
        final Attribute stageName = getMdService().getObj(getProject(), Attribute.class, title(STAGE_NAME));
        final String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));

        final Metric amountSum = getMdService()
                .createObj(getProject(), new Metric(metricAlias,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)), "#,##0.00"));

        final String reportName = "Report-for-testing-running-totals";
        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri(), stageName.getTitle())),
                singletonList(new MetricElement(amountSum)));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFromCellValue(metricAlias).aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);

        reportPage.waitForReportExecutionProgress()
                .showConfiguration()
                .showCustomNumberFormat();

        assertTrue(isEqualCollection(reportPage.getCustomFormatItemTitles(), singletonList(metricAlias)),
                metricAlias + " is not the only one");

        reportPage.openFilterPanel()
                .clickAddFilter()
                .openRankingFilterFragment()
                .openSelectMetricPopupPanel();

        //check Amount-Sum is on top of metric pop-up panel
        assertEquals(Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).getItems().get(0), metricAlias);

        reportPage.hideFilterPanel()
                .selectReportVisualisation(ReportTypes.LINE)
                .waitForReportExecutionProgress()
                .openMetricAxisConfiguration();

        assertTrue(isEqualCollection(reportPage.getMetricAxisConfigurationNames(), singletonList(metricAlias)));

        //back to table type to reduce height of configuration panel
        assertTrue(reportPage.selectReportVisualisation(ReportTypes.TABLE)
                .waitForReportExecutionProgress()
                .getReportStatistic().contains("1 Metrics"), "There is more than 1 metric");

        assertTrue(
                table.openContextMenuFromCellValue(INTEREST)
                        .getItemNames()
                        .stream().filter(e -> e.contains(metricAlias)).count() == 1,
                "There is more than 1 item which relates to metric");
    }

    @Test(dependsOnGroups = "createProject")
    public void calculateRunningTotalsForTotalColumns() {
        final String reportName = "Running-totals-for-total-columns";
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(reportName)
                .withWhats(AMOUNT)
                .withHows(new HowItem(STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(STATUS, Position.LEFT))
                .withHows(new HowItem(YEAR_SNAPSHOT, Position.TOP, YEAR_2011)));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.SUM, "of All Rows");
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);
        assertEquals(table.getTotalValues(), asList(23767077.60f, 0.0f),
                "Running sum of sum is not empty");

        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.SUM, "by Stage Name");
        assertEquals(table.getMetricElements(), asList(16427388.57f, 16427388.57f, 3436167.70f, 3436167.70f,
                3903521.33f, 3903521.33f, 16427388.57f, 0.0f, 3436167.70f, 0.0f, 3903521.33f, 0.0f, 23767077.60f, 0.0f),
                "The total values are computed incorrectly");

        table.sortByHeader(AMOUNT, Sort.DESC);
        takeScreenshot(browser, "not-calculate-running-totals-for-total-columns", getClass());
        assertEquals(table.getTotalValues(), asList(23767077.60f, 0.0f));
    }

    @Test(dependsOnGroups = "createProject")
    public void drillOnReportContainingRunningTotals() {
        final String reportName = "Drill-in-report-containing-totals";
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(reportName)
                .withWhats(AMOUNT)
                .withHows(new HowItem(STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(YEAR_SNAPSHOT, Position.TOP)));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFromCellValue(YEAR_2011).aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);
        table.clickOnAttributeToOpenDrillReport(YEAR_2011);

        reportPage.waitForReportExecutionProgress().openFilterPanel()
                .addFilter(FilterItem.Factory.createAttributeFilter(QUARTER_YEAR_SNAPSHOT, Q1_2011, Q2_2011));

        takeScreenshot(browser, "drill-on-report-containing-running-totals", getClass());
        assertTrue(table.getAttributesHeader().contains(QUARTER_YEAR_SNAPSHOT) && table.getAttributeElements()
                .containsAll(asList(Q1_2011, Q2_2011)), "The drill values are not displayed");

        assertEquals(table.getMetricElements(),
                asList(1719072.21f, 1456305.27f, 1772094.56f, 1719072.21f, 3175377.48f, 4947472.04f, 1663660.67f,
                        2350525.55f, 3121266.12f, 1663660.67f, 4014186.22f, 7135452.34f, 1663660.67f, 2350525.55f,
                        3121266.12f));

        browser.navigate().back();
        reportPage.waitForReportExecutionProgress()
                .openWhatPanel()
                .selectMetric(AMOUNT)
                .addDrillStep(STATUS)
                .doneSndPanel()
                .waitForReportExecutionProgress();

        //click on any metric value to drill in 
        table.drillOnMetricValue();
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getMetricElements(), asList(1719072.21f, 1719072.21f, 1719072.21f, 0.0f));
    }
}
