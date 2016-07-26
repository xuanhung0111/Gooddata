package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;

public class GoodSalesCellLimitTest extends GoodSalesAbstractTest {

    private static final String TEST_CELL_LIMIT = "test-cell-limit";
    private static final String TESTING_REPORT_TABLE = "Testing report table";
    private static final String TESTING_REPORT_CHART = "Testing report chart";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-" + TEST_CELL_LIMIT;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createTestingReports() {
        initReportsPage();
        createReport(
                new UiReportDefinition()
                    .withName(TESTING_REPORT_TABLE)
                    .withWhats(METRIC_AMOUNT, METRIC_QUOTA)
                    .withHows(
                            new HowItem(ATTR_ACCOUNT, HowItem.Position.LEFT),
                            new HowItem(ATTR_DATE_CREATED, HowItem.Position.TOP)
                    ),
                TESTING_REPORT_TABLE
        );

        initReportCreation();
        reportPage.initPage()
            .setReportName(TESTING_REPORT_CHART)
            .openWhatPanel()
            .selectMetric(METRIC_AMOUNT)
            .openHowPanel()
            .selectAttribute(ATTR_ACCOUNT)
            .doneSndPanel()
            .selectReportVisualisation(ReportTypes.LINE)
            .forceRenderChartReport()
            .finishCreateReport();
        checkRedBar(browser);
    }


    @Test(dependsOnMethods = {"createTestingReports"})
    public void tableCellLimitAndShowAnyway() {
        try {
            addReportToNewDashboard(TESTING_REPORT_TABLE, TEST_CELL_LIMIT + TESTING_REPORT_TABLE);
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);

            assertTrue(report.isCellLimit());
            report.showAnyway();

            assertTrue(isEqualCollection(asList(METRIC_AMOUNT, METRIC_QUOTA), report.getMetricsHeader()));
            assertTrue(isEqualCollection(asList(ATTR_ACCOUNT, ATTR_DATE_CREATED), report.getAttributesHeader()));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReports"})
    public void chartCellLimitAndShowAnyway() {
        try {
            addReportToNewDashboard(TESTING_REPORT_CHART, TEST_CELL_LIMIT + TESTING_REPORT_CHART);
            ChartReport report = dashboardsPage.getContent().getLatestReport(ChartReport.class);

            assertTrue(report.isCellLimit());
            report.showAnyway();

            assertTrue(report.isChart());
            Sleeper.sleepTightInSeconds(5);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }
}
