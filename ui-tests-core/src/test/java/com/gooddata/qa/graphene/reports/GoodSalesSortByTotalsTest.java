package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu.AggregationType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;

public class GoodSalesSortByTotalsTest extends GoodSalesAbstractTest {

    private final static String Q1_2011 = "Q1/2011";
    private final static String OF_ALL_ROWS = "Of All Rows";
    private final static String YEAR_2011 = "2011";
    private final static String OF_ALL_COLUMNS = "of All Columns";
    private final static String SUM = "Sum";
    private final static String REPORT_CONTAINING_ONE_METRIC = "Report-containing-one-metric";
    private final static String BY_STAGE_NAME = "by Stage Name";

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();
        createAvgAmountMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void sortByTotalsInOnTopAttributeReport() {
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName("Ontop-attribute-report")
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP)));

        final TableReport table = reportPage.getTableReport();
        reportPage.displayMetricsInDifferentRows();
        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, OF_ALL_COLUMNS);
        reportPage.waitForReportExecutionProgress();

        table.sortByHeader(SUM, Sort.DESC);
        reportPage.waitForReportExecutionProgress();
        takeScreenshot(browser, "sort-totals-in-ontop-attribute-report", getClass());
        assertEquals(table.getMetricElements(), asList(26922362.00f, 86178611.71f, 116625456.54f, 229726430.25f),
                "Report is rendered well and expected values are displayed");
    }

    @Test(dependsOnGroups = "createProject")
    public void createReportContainingOneMetric() {
        //create report using UI due to attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(REPORT_CONTAINING_ONE_METRIC)
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP, YEAR_2011))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT, "1000Bulbs.com > Educationly",
                        "Gerimedix > Educationly", "Square One Salon and Spa > WonderKid")));
    }

    @Test(dependsOnMethods = {"createReportContainingOneMetric"})
    public void sortByTotalsInReportContainingOneMetric() {
        final TableReport table = initReportsPage().openReport(REPORT_CONTAINING_ONE_METRIC).getTableReport();
        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, OF_ALL_ROWS);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(886.15f, 886.15f, 886.15f, 3318.24f));
        
        table.sortByHeader(SUM, Sort.DESC);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(3318.24f, 886.15f, 886.15f, 886.15f));

        table.sortByHeader(SUM, Sort.ASC);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(886.15f, 886.15f, 886.15f, 3318.24f));

        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, BY_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f,
                2432.09f, 886.15f, 0.0f, 886.15f, 886.15f, 886.15f, 3318.24f));

        table.sortByHeader(SUM, Sort.DESC);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(2432.09f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f,
                0.0f, 886.15f, 0.0f, 3318.24f, 886.15f, 886.15f, 886.15f));

        table.sortByHeader(SUM, Sort.ASC);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getTotalValues(), asList(0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f,
                2432.09f, 886.15f, 0.0f, 886.15f, 886.15f, 886.15f, 3318.24f));

        table.sortByHeader(METRIC_AMOUNT, Sort.DESC);
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getMetricElements(), asList(886.15f, 0.0f, 0.0f, 886.15f, 0.0f, 0.0f, 886.15f, 0.0f, 0.0f,
                886.15f, 2432.09f, 0.0f, 886.15f, 886.15f, 886.15f, 3318.24f));

        reportPage.openFilterPanel().addFilter(FilterItem.Factory.createAttributeFilter(ATTR_QUARTER_YEAR_SNAPSHOT, Q1_2011));
        reportPage.waitForReportExecutionProgress();
        assertEquals(table.getMetricElements(), asList(886.15f, 0.0f, 886.15f));
    }

    @Test(dependsOnMethods = {"createReportContainingOneMetric"})
    public void sortByTotalsInReportContainingTwoMetrics() {
        final TableReport table = initReportsPage().openReport(REPORT_CONTAINING_ONE_METRIC).getTableReport();
        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, OF_ALL_ROWS);
        reportPage.waitForReportExecutionProgress();

        table.openContextMenuFromCellValue(METRIC_AMOUNT).aggregateTableData(AggregationType.SUM, BY_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();

        reportPage.openWhatPanel()
                .selectMetric(METRIC_AVG_AMOUNT)
                .doneSndPanel()
                .waitForReportExecutionProgress()
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_QUARTER_YEAR_SNAPSHOT, Q1_2011))
                .waitForReportExecutionProgress();

        takeScreenshot(browser, "sort-by-totals-in-report-containing-two-metrics", getClass());
        assertFalse(table.isSortAvailable(SUM), "Sort icons are displayed");
    }
}
