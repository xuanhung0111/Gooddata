package com.gooddata.qa.graphene.reports;

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
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PROBABILITY;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesRunningTotalsTest extends GoodSalesAbstractTest {

    private final static String RUNNING_TOTAL = "Running-Totals";
    private final static String YEAR_2011 = "2011";
    private final static String RUNNING_SUM = "Running Sum";
    private final static String RUNNING_AVERAGE = "Running Average";
    private final static String RUNNING_MINIMUM = "Running Minimum";
    private final static String RUNNING_MAXIMUM = "Running Maximum";
    private final static String INTEREST = "Interest";
    private final static String DISCOVERY = "Discovery";
    private final static String SHORT_LIST = "Short List";
    private final static String Q1_2011 = "Q1/2011";
    private final static String Q2_2011 = "Q2/2011";

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createProbabilityMetric();
        getMetricCreator().createAvgAmountMetric();

        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(RUNNING_TOTAL)
                .withWhats(METRIC_PROBABILITY)
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP, YEAR_2011)));
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

    @Test(dependsOnGroups = "createProject", dataProvider = "runningTotalDataProvider")
    public void addRunningTotalsToReport(final String type,
            final String runningTotalHeader, final List<Float> runningTotalValues) {

        final TableReport table = initReportsPage().openReport(RUNNING_TOTAL).getTableReport();
        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.RUNNING, type);
        assertTrue(table.getMetricHeaders().contains(runningTotalHeader), type + " headers have not been added");
        assertTrue(table.getMetricValues().equals(runningTotalValues), type + " values are not correct");

        final List<Float> metricValues = asList(10.0f, 20.0f, 30.0f, 16427388.57f, 3436167.70f, 3903521.33f);
        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .nonAggregateTableData(AggregationType.RUNNING, type);
        assertFalse(table.getMetricHeaders().contains(runningTotalHeader), type + " headers has not been removed");
        assertEquals(table.getMetricValues(), metricValues, type + " values has not been removed");

        table.openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.RUNNING, type);
        assertTrue(table.getMetricHeaders().contains(runningTotalHeader), type + " headers have not been added");
        assertEquals(table.getMetricValues(), runningTotalValues, type + " values are not correct");

        table.openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
                .nonAggregateTableData(AggregationType.RUNNING, type);
        assertFalse(table.getMetricHeaders().contains(runningTotalHeader), type + " headers has not been removed");
        assertEquals(table.getMetricValues(), metricValues, type + " values has not been removed");
    }

    @Test(dependsOnGroups = "createProject")
    public void customizeMetricAndAttributeInRunningSumReport() {
        final TableReport table = initReportsPage().openReport(RUNNING_TOTAL).getTableReport();

        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);

        reportPage.waitForReportExecutionProgress()
                .openWhatPanel()
                .selectItem(METRIC_AVG_AMOUNT)
                .deselectItem(METRIC_PROBABILITY) //minimize number of metrics to avoid handling scroll bar
                .done();

        assertTrue(table.getMetricHeaders().stream().filter(e -> e.equals(RUNNING_SUM)).count() == 1,
                "Running sum header is calculated for new metric");
        assertEquals(
                table.getMetricValues(), asList(16427388.57f, 3436167.70f, 3903521.33f, 16427388.57f, 19863556.27f,
                        23767077.60f, 130376.10f, 25265.94f, 29797.87f),
                "Running sum values are calculated for new metric");

        reportPage.openHowPanel()
                .selectAttribtues(singletonList(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP,
                        Q1_2011, Q2_2011)))
                .done();

        //use List.equal() to check value's order
        assertTrue(
                table.getMetricValues()
                        .equals(asList(1719072.21f, 1456305.27f, 1772094.56f, 1719072.21f, 3175377.48f, 4947472.04f,
                                34381.44f, 21105.87f, 24959.08f, 1663660.67f, 2350525.55f, 3121266.12f, 1663660.67f,
                                4014186.22f, 7135452.34f, 25206.98f, 27653.24f, 32855.43f)),
                "Running sum values are not correct");
    }

    @Test(dependsOnGroups = "createProject")
    public void testRunningTotalIsNotMetric() {
        final String metricAlias = "Amount-Sum";

        final Metric amountSum = getMdService()
                .createObj(getProject(), new Metric(metricAlias,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", 
                                getMetricByTitle(METRIC_AMOUNT).getUri())), "#,##0.00"));

        final String reportName = "Report-for-testing-running-totals";
        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri(), ATTR_STAGE_NAME)),
                singletonList(new MetricElement(amountSum)));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFrom(metricAlias, CellType.METRIC_HEADER)
                .aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);

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
                table.openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
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
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(ATTR_IS_ACTIVE, Position.LEFT))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP, YEAR_2011)));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.SUM, "of All Rows");
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);
        assertEquals(table.getTotalValues(), asList(23767077.60f, 0.0f),
                "Running sum of sum is not empty");

        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.SUM, "by Stage Name");
        assertEquals(table.getMetricValues(), asList(16427388.57f, 3436167.70f, 3903521.33f,
                16427388.57f, 3436167.70f, 3903521.33f));
        assertEquals(table.getTotalValues(), asList(16427388.57f, 3436167.70f, 3903521.33f, 0.0f ,0.0f, 0.0f,
                23767077.60f, 0.0f));

        table.sortBy(METRIC_AMOUNT, CellType.METRIC_HEADER, Sort.DESC);
        reportPage.waitForReportExecutionProgress();
        takeScreenshot(browser, "not-calculate-running-totals-for-total-columns", getClass());
        assertEquals(table.getTotalValues(), asList(23767077.60f, 0.0f));
    }

    @Test(dependsOnGroups = "createProject")
    public void drillOnReportContainingRunningTotals() {
        final String reportName = "Drill-in-report-containing-totals";
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(reportName)
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT, INTEREST, DISCOVERY, SHORT_LIST))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP)));

        final TableReport table = initReportsPage().openReport(reportName).getTableReport();
        table.openContextMenuFrom(YEAR_2011, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(AggregationType.RUNNING, RUNNING_SUM);
        table.drillOn(YEAR_2011, CellType.ATTRIBUTE_VALUE);

        reportPage.waitForReportExecutionProgress().openFilterPanel()
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_QUARTER_YEAR_SNAPSHOT, Q1_2011, Q2_2011));

        reportPage.waitForReportExecutionProgress();
        takeScreenshot(browser, "drill-on-report-containing-running-totals", getClass());
        assertTrue(table.getAttributeHeaders().contains(ATTR_QUARTER_YEAR_SNAPSHOT) && table.getAttributeValues()
                .containsAll(asList(Q1_2011, Q2_2011)), "The drill values are not displayed");

        assertEquals(table.getMetricValues(),
                asList(1719072.21f, 1456305.27f, 1772094.56f, 1719072.21f, 3175377.48f, 4947472.04f, 1663660.67f,
                        2350525.55f, 3121266.12f, 1663660.67f, 4014186.22f, 7135452.34f));

        browser.navigate().back();
        reportPage.waitForReportExecutionProgress()
                .openWhatPanel()
                .openMetricDetail(METRIC_AMOUNT)
                .addDrillStep(ATTR_IS_ACTIVE)
                .done();

        //click on any metric value to drill in
        reportPage.waitForReportExecutionProgress();
        table.drillOnFirstValue(CellType.METRIC_VALUE);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getMetricValues(), asList(1719072.21f, 1719072.21f));
    }
}
