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
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.InteractiveReportWidget;

@Test(groups = {"interactiveReport"}, description = "Test Interactive Report on dashboard in Portal")
public class InteractiveReportTest extends GoodSalesAbstractTest{

    @FindBy(xpath = "//iframe[contains(@src,'iaa/interactive_report')]")
    private InteractiveReportWidget interactiveReport;

    private static final long expectedDashboardExportSize = 42000L;

    @Test(dependsOnMethods = {"createProject"}, groups = {"interactiveReport-tooLarge"}, priority = 1)
    public void interactiveReportTooLargeWithLineChartTest() throws InterruptedException {
        interactiveReportTooLargeWithChartTest(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"interactiveReport-tooLarge"}, priority = 2)
    public void interactiveReportTooLargeWithAreaChartTest() throws InterruptedException {
        interactiveReportTooLargeWithChartTest(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"interactiveReport-tooLarge"}, priority = 3)
    public void interactiveReportTooLargeWithBarChartTest() throws InterruptedException {
        interactiveReportTooLargeWithChartTest(BAR_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-tooLarge"}, groups = {"interactiveReport-error"}, priority = 1)
    public void invalidConfigurationInInteractiveReportWithLineChartTest() throws InterruptedException {
        invalidConfigurationInInteractiveReportWithChartTest(LINE_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-tooLarge"}, groups = {"interactiveReport-error"}, priority = 2)
    public void invalidConfigurationInInteractiveReportWithAreaChartTest() throws InterruptedException {
        invalidConfigurationInInteractiveReportWithChartTest(AREA_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-tooLarge"}, groups = {"interactiveReport-error"}, priority = 3)
    public void invalidConfigurationInInteractiveReportWithBarChartTest() throws InterruptedException {
        invalidConfigurationInInteractiveReportWithChartTest(BAR_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-error"}, groups = {"interactiveReport"}, priority = 1)
    public void interactiveReportWithLineChartTest() throws InterruptedException {
        interactiveReportWithChartTestWithCleanUp(LINE_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-error"}, groups = {"interactiveReport"}, priority = 2)
    public void interactiveReportWithAreaChartTest() throws InterruptedException {
        interactiveReportWithChartTestWithCleanUp(AREA_CHART);
    }

    @Test(dependsOnGroups = {"interactiveReport-error"}, groups = {"interactiveReport"}, priority = 3)
    public void interactiveReportWithBarChartTest() throws InterruptedException {
        interactiveReportWithChartTest(BAR_CHART);
    }

    @Test(dependsOnMethods = {"interactiveReportWithBarChartTest"}, groups = {"interactiveReport"})
    public void workingOnColorOfInteractiveReportTest() {
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
    }

    @Test(dependsOnMethods = {"workingOnColorOfInteractiveReportTest"}, groups = {"interactiveReport"})
    public void workingOnTabularExplorerOfInteractiveReportTest() {
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
    }

    @Test(dependsOnMethods = {"workingOnTabularExplorerOfInteractiveReportTest"}, groups = {"interactiveReport"})
    public void workingOnExplorerTitleTest() {
        @SuppressWarnings("unused") boolean isEditMode = false;
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        interactiveReport.changeReportTitle("New report title");
        interactiveReport.changeReportSubtitle("New report subtitle");
        verifyReportTitleAndSubtitle(isEditMode = true);
        dashboardEditBar.saveDashboard();
        verifyReportTitleAndSubtitle(isEditMode = false);

        dashboardsPage.editDashboard();
        interactiveReport.changeReportTitle("You will not see this title");
        interactiveReport.changeReportSubtitle("You will not see this subtitle");
        dashboardEditBar.cancelDashboard();
        verifyReportTitleAndSubtitle(isEditMode = false);
    }

    @Test(dependsOnMethods = {"workingOnExplorerTitleTest"}, groups = {"interactiveReport"})
    public void fillterOutAllValuesOnInteractiveReportTest() throws InterruptedException {
        DashboardEditBar dashboardEditBar = null;
        try{
            dashboardEditBar = dashboardsPage.getDashboardEditBar();
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
            dashboardsPage.editDashboard();
            dashboardEditBar.getDashboardEditFilter().deleteFilter("stage_name");
            dashboardEditBar.saveDashboard();
        }
    }

    @Test(dependsOnMethods = {"fillterOutAllValuesOnInteractiveReportTest"}, groups = {"interactiveReport"})
    public void shareAndExportDashboardContainingInteractiveReportTest() throws InterruptedException {
        initDashboardsPage();
        String exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);

        browser.navigate().to(dashboardsPage.embedDashboard().getPreviewURI());
        waitForElementVisible(interactiveReport.getRoot());
        Thread.sleep(2000);
        testInteractiveReportDisplaying(BAR_CHART);
    }

    @Test(dependsOnMethods = {"shareAndExportDashboardContainingInteractiveReportTest"}, groups = {"tests"})
    public void prepareForEndingTest() {
        successfulTest = true;
    }

    private void interactiveReportWithChartTest(InteractiveReportWidget.ChartType type) throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard("Interactive Report");
        dashboardsPage.editDashboard();
        dashboardEditBar.initInteractiveReportWidget();
        interactiveReport.selectChartType(type)
                         .configureYAxis("Sales Figures", "Amount")
                         .configureXAxisWithValidAttribute("Stage", "Stage Name");
        dashboardEditBar.saveDashboard();

        testInteractiveReportDisplaying(type);
    }

    private void interactiveReportWithChartTestWithCleanUp(InteractiveReportWidget.ChartType type) throws InterruptedException {
        try {
            interactiveReportWithChartTest(type);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void testInteractiveReportDisplaying(InteractiveReportWidget.ChartType type) {
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

    private void verifyReportTitleAndSubtitle(boolean inEditMode) {
        assertEquals(interactiveReport.getReportTitle(inEditMode), "New report title");
        assertEquals(interactiveReport.getReportSubtitle(inEditMode), "New report subtitle");
    }

    private void interactiveReportTooLargeWithChartTest(InteractiveReportWidget.ChartType type) throws InterruptedException {
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard("Interactive Report");
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

    private void invalidConfigurationInInteractiveReportWithChartTest(InteractiveReportWidget.ChartType type) throws InterruptedException {
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard("Interactive Report");
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