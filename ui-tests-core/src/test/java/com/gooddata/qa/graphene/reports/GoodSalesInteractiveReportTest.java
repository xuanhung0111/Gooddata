package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.fragments.reports.InteractiveReportWidget.ChartType.BAR_CHART;
import static com.gooddata.qa.graphene.fragments.reports.InteractiveReportWidget.ChartType.LINE_CHART;
import static com.gooddata.qa.graphene.fragments.reports.InteractiveReportWidget.ChartType.AREA_CHART;

import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.InteractiveReportWidget;

@Test(groups = {"interactiveReportTest"}, description = "Test Interactive Report on dashboard")
public class GoodSalesInteractiveReportTest extends GoodSalesAbstractTest {

    private static final String DASHBOARD_NAME = "Interactive Report";

    @FindBy(xpath = "//iframe[contains(@src,'iaa/interactive_report')]")
    private InteractiveReportWidget interactiveReport;

    private static final long expectedDashboardExportSize = 42000L;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-interactive-report";
    }
    
    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeLineChart() throws InterruptedException {
        verifyTooLargeChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeAreaChart() throws InterruptedException {
        verifyTooLargeChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeBarChart() throws InterruptedException {
        verifyTooLargeChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationLineChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationAreaChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationBarChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyLineChart() throws InterruptedException {
        verifyChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyAreaChart() throws InterruptedException {
        verifyChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyBarChart() throws InterruptedException {
        verifyChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnColor() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            interactiveReport.enableChartColor()
                             .configureColor("Stage", "Status");
            dashboardEditBar.saveDashboard();
    
            assertTrue(interactiveReport.isColorAppliedOnChart() &&
                       interactiveReport.isChartTableContainsHeader("Status") &&
                       interactiveReport.areTableValuesInSpecificRowMatchedChartLegendNames("Status"),
                       String.format("There is something wrong when applying color attribute '%s' to report!", "Status"));
    
            int totalTableRows = interactiveReport.getTotalTableRows();
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", totalTableRows));
    
            assertTrue(interactiveReport.clickOnSeries(0)
                                        .areTrackersSelectedWhenClickOnSeries(BAR_CHART, 0),
                       String.format("Trackers are not selected when click on series index [%d]!", 0));
    
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("6 selected out of %d total", totalTableRows));
    
            assertTrue(interactiveReport.resetTrackerSelection()
                                        .isTrackerSelectionReset(BAR_CHART),
                       "Chart is not reset!");
    
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", totalTableRows));
    
            dashboardsPage.editDashboard();
            interactiveReport.disableChartColor();
    
            assertTrue(interactiveReport.isChartSeriesReset(),
                       "Chart series is not reset!");
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Status"),
                        "Explorer table still contains column 'Status'!");
            dashboardEditBar.saveDashboard();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnTabularExplorer() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            interactiveReport.enableAbilityToAddMoreTableAttributes()
                             .addMoreTableAttributes("Product", "Product");
            assertTrue(interactiveReport.isChartTableContainsHeader("Product Name"),
                       "Explorer table does not contains column 'Product Name'!");
    
            dashboardEditBar.saveDashboard();
    
            assertTrue(interactiveReport.isChartTableContainsHeader("Product Name"),
                       "Explorer table does not contains column 'Product Name'!");
    
            dashboardsPage.editDashboard();
            interactiveReport.deleteTableAttribute("Product");
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Product Name"),
                        "Explorer table still contains column 'Product Name'!");
    
            dashboardEditBar.saveDashboard();
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Product Name"),
                        "Explorer table still contains column 'Product Name'!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnExplorerTitle() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            interactiveReport.changeReportTitle("New report title");
            interactiveReport.changeReportSubtitle("New report subtitle");
            verifyReportTitleAndSubtitleInEditMode();
            dashboardEditBar.saveDashboard();
            verifyReportTitleAndSubtitleInViewMode();
    
            dashboardsPage.editDashboard();
            interactiveReport.changeReportTitle("You will not see this title");
            interactiveReport.changeReportSubtitle("You will not see this subtitle");
            dashboardEditBar.cancelDashboard();
            verifyReportTitleAndSubtitleInViewMode();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void fillterOutAllValues() throws InterruptedException {
        try{
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Stage Name");
            dashboardEditBar.saveDashboard();

            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            assertEquals(interactiveReport.getTotalTableRows(), 8);
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", 8));

            dashboardsPage.getFilters().get(0).changeAttributeFilterValue("Interest");

            assertEquals(interactiveReport.getTotalTableRows(), 1);
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", 1));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void shareAndExportDashboard() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            browser.navigate().to(dashboardsPage.embedDashboard().getPreviewURI());
            waitForElementVisible(interactiveReport.getRoot());
            Thread.sleep(2000);
            testDisplaying(BAR_CHART);

            initDashboardsPage();
            String exportedDashboardName = dashboardsPage.exportDashboardTab(0);
            verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
    
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void addReportInNewDashboard(InteractiveReportWidget.ChartType type) throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard(DASHBOARD_NAME);
        dashboardsPage.editDashboard();
        dashboardEditBar.initInteractiveReportWidget();
        interactiveReport.selectChartType(type)
                         .configureYAxis("Sales Figures", "Amount")
                         .configureXAxisWithValidAttribute("Stage", "Stage Name");
        dashboardEditBar.saveDashboard();
    }

    private void verifyChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            addReportInNewDashboard(type);
            testDisplaying(type);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void testDisplaying(InteractiveReportWidget.ChartType type) {
        assertTrue(interactiveReport.hoverOnTracker(0)
                                    .isTooltipVisible(),
                   "Tooltip is not visible!");

        int totalTableRows = interactiveReport.getTotalTableRows();
        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("%d total", totalTableRows));

        assertTrue(interactiveReport.clickOnTracker(0)
                                    .isTrackerSelected(type, 0),
                   String.format("Tracker index [%d] is not selected!", 0));

        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("1 selected out of %d total", totalTableRows));

        assertTrue(interactiveReport.resetTrackerSelection()
                                    .isTrackerSelectionReset(type),
                   "Chart is not reset!");

        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("%d total", totalTableRows));
    }

    private void verifyReportTitleAndSubtitleInViewMode() {
        verifyReportTitleAndSubtitle(false);
    }

    private void verifyReportTitleAndSubtitleInEditMode() {
        verifyReportTitleAndSubtitle(true);
    }

    private void verifyReportTitleAndSubtitle(boolean editMode) {
        assertEquals(interactiveReport.getReportTitle(editMode), "New report title");
        assertEquals(interactiveReport.getReportSubtitle(editMode), "New report subtitle");
    }

    private void verifyTooLargeChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard(DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            dashboardEditBar.initInteractiveReportWidget();
            interactiveReport.selectChartType(type)
                             .configureYAxis("Sales Figures", "Amount")
                             .configureXAxisWithValidAttribute("Account", "Account");
            assertTrue(interactiveReport.isChartAlertMessageVisible(),
                       "'Too many data points' message is not shown!");
            dashboardEditBar.saveDashboard();
            assertTrue(interactiveReport.isChartAlertMessageVisible(),
                       "'Too many data points' message is not shown!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void verifyInvalidConfigurationChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard(DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            dashboardEditBar.initInteractiveReportWidget();
            interactiveReport.selectChartType(type)
                             .configureYAxis("Sales Figures", "Amount")
                             .configureXAxisWithInvalidAttribute("Activity", "Activity");
            assertTrue(interactiveReport.isChartErrorMessageVisible(),
                       "'Invalid configuration' message is not shown!");
            dashboardEditBar.saveDashboard();
            assertTrue(interactiveReport.isChartErrorMessageVisible(),
                       "'Invalid configuration' message is not shown!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }
}