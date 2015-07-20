package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.HowItem;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

public class GoodSalesCellLimitTest extends GoodSalesAbstractTest {

    private static final String TEST_CELL_LIMIT = "test-cell-limit";
    private static final String TESTING_REPORT_TABLE = "Testing report table";
    private static final String TESTING_REPORT_CHART = "Testing report chart";

    private static final String ACCOUNT = "Account";
    private static final String DATE = "Date (Created)";
    private static final String AMOUNT = "Amount";
    private static final String QUOTA = "Quota";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-" + TEST_CELL_LIMIT;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createTestingReports() throws InterruptedException {
        initReportsPage();
        createReport(
                new ReportDefinition()
                    .withName(TESTING_REPORT_TABLE)
                    .withWhats(AMOUNT, QUOTA)
                    .withHows(
                            new HowItem(ACCOUNT, HowItem.Position.LEFT),
                            new HowItem(DATE, HowItem.Position.TOP)
                    ),
                TESTING_REPORT_TABLE
        );
        createReport(
                new ReportDefinition()
                        .withName(TESTING_REPORT_CHART)
                        .withWhats(AMOUNT)
                        .withHows(ACCOUNT)
                        .withType(ReportTypes.LINE),
                TESTING_REPORT_CHART
        );
        checkRedBar(browser);
    }


    @Test(dependsOnMethods = {"createTestingReports"})
    public void tableCellLimitAndShowAnyway() throws InterruptedException {
        try {
            addReportToNewDashboard(TESTING_REPORT_TABLE, TEST_CELL_LIMIT + TESTING_REPORT_TABLE);
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);

            assertTrue(report.isCellLimit());
            report.showAnyway();

            assertTrue(isEqualCollection(asList(AMOUNT, QUOTA), report.getMetricsHeader()));
            assertTrue(isEqualCollection(asList(ACCOUNT, DATE), report.getAttributesHeader()));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReports"})
    public void chartCellLimitAndShowAnyway() throws InterruptedException {
        try {
            addReportToNewDashboard(TESTING_REPORT_CHART, TEST_CELL_LIMIT + TESTING_REPORT_CHART);
            ChartReport report = dashboardsPage.getContent().getLatestReport(ChartReport.class);

            assertTrue(report.isCellLimit());
            report.showAnyway();

            assertTrue(report.isChart());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }
}
