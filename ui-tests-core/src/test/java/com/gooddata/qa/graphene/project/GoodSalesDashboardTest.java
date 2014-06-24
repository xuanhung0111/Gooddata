package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.testng.annotations.Test;

import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = {"GoodSalesDashboard"}, description = "Tests for GoodSales project (dashboards functionality) in GD platform")
public class GoodSalesDashboardTest extends GoodSalesAbstractTest {

    private String exportedDashboardName;
    private static final long expectedDashboardExportSize = 65000L;

    @Test(dependsOnMethods = {"createProject"}, groups = {"dashboards-verification"})
    public void verifyDashboardTabs() throws InterruptedException {
        uiUtils.verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void exportFirstDashboard() throws InterruptedException {
        openUrl(uiUtils.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        checkUtils.waitForDashboardPageLoaded();
        waitForElementVisible(uiUtils.dashboardsPage.getRoot());
        exportedDashboardName = uiUtils.dashboardsPage.exportDashboardTab(0);
        checkUtils.checkRedBar();
    }

    @Test(dependsOnMethods = {"exportFirstDashboard"}, groups = {"dashboards-verification"})
    public void verifyExportedDashboardPDF() {
        uiUtils.verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void addNewTab() throws InterruptedException {
        uiUtils.addNewTabOnDashboard("Pipeline Analysis", "test", "GoodSales-new-tab");
    }

    /**
     * Temporarily disabled test for adding report on dashboard tab since there is a weird behavior
     * - dialog for adding report and report itself is present, but webdriver can't use it since it's not visible (probably some css issue?)
     *
     * @Test(dependsOnMethods = {"addNewTab"}, groups = {"dashboards-verification"})
     * public void addReportOnNewTab() throws InterruptedException {
     * initDashboardsPage();
     * dashboardsPage.selectDashboard("Pipeline Analysis");
     * waitForDashboardPageLoaded();
     * Thread.sleep(3000);
     * dashboardsPage.getTabs().openTab(9);
     * waitForDashboardPageLoaded();
     * dashboardsPage.editDashboard();
     * dashboardsPage.getDashboardEditBar().addReportToDashboard("Activities by Type");
     * dashboardsPage.getDashboardEditBar().saveDashboard();
     * waitForDashboardPageLoaded();
     * Screenshots.takeScreenshot(browser, "GoodSales-new-tab-with-chart", this.getClass());
     * }
     */

    @Test(dependsOnMethods = {"addNewTab"}, groups = {"dashboards-verification"})
    public void deleteNewTab() throws InterruptedException {
        uiUtils.initDashboardsPage();
        assertTrue(uiUtils.dashboardsPage.selectDashboard("Pipeline Analysis"), "Dashboard wasn't selected");
        checkUtils.waitForDashboardPageLoaded();
        Thread.sleep(5000);
        int tabsCount = uiUtils.dashboardsPage.getTabs().getNumberOfTabs();
        uiUtils.dashboardsPage.deleteDashboardTab(tabsCount - 1);
        Thread.sleep(5000);
        assertEquals(uiUtils.dashboardsPage.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewDashboard() throws InterruptedException {
        uiUtils.initDashboardsPage();
        String dashboardName = "test";
        uiUtils.dashboardsPage.addNewDashboard(dashboardName);
        checkUtils.waitForDashboardPageLoaded();
        waitForElementNotPresent(uiUtils.dashboardsPage.getDashboardEditBar().getRoot());
        Thread.sleep(5000);
        checkUtils.checkRedBar();
        assertEquals(uiUtils.dashboardsPage.getDashboardsCount(), 2, "New dashboard is not present");
        assertEquals(uiUtils.dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
        Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addNewDashboard"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewTabOnNewDashboard() throws InterruptedException {
        uiUtils.addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
    }

    @Test(dependsOnGroups = {"new-dashboard"}, groups = {"dashboards-verification"})
    public void deleteNewDashboard() throws InterruptedException {
        uiUtils.initDashboardsPage();
        int dashboardsCount = uiUtils.dashboardsPage.getDashboardsCount();
        if (uiUtils.dashboardsPage.selectDashboard("test")) {
            uiUtils.dashboardsPage.deleteDashboard();
            Thread.sleep(3000);
            checkUtils.waitForDashboardPageLoaded();
            assertEquals(uiUtils.dashboardsPage.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
            checkUtils.checkRedBar();
        } else {
            fail("Dashboard wasn't selected and not deleted");
        }
    }

    @Test(dependsOnGroups = {"dashboards-verification"}, groups = {"tests"})
    public void verifyDashboardTabsAfter() throws InterruptedException {
        uiUtils.verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }
}
