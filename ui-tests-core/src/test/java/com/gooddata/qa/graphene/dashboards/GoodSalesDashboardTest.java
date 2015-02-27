package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

import org.testng.annotations.Test;

import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"GoodSalesDashboard"}, description = "Tests for GoodSales project (dashboards functionality) in GD platform")
public class GoodSalesDashboardTest extends GoodSalesAbstractTest {

    private String exportedDashboardName;
    private static final long expectedDashboardExportSize = 65000L;

    @Test(dependsOnMethods = {"createProject"}, groups = {"dashboards-verification"})
    public void verifyDashboardTabs() throws InterruptedException {
        verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void exportFirstDashboard() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard("Pipeline Analysis");
        waitForDashboardPageLoaded(browser);
        exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"exportFirstDashboard"}, groups = {"dashboards-verification"})
    public void verifyExportedDashboardPDF() {
        verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void addNewEmptyTab() throws InterruptedException {
        addNewTabOnDashboard("Pipeline Analysis", "empty-tab", "GoodSales-new-empty-tab");
    }
    
    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void addNewNonEmptyTab() throws InterruptedException {
	DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        addNewTabOnDashboard("Pipeline Analysis", "non-empty-tab", "GoodSales-new-non-empty-tab");
        dashboardsPage.editDashboard();
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.saveDashboard();
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

    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteEmptyTab() throws InterruptedException {
        deleteTab(2);
    }
    
    @Test(dependsOnMethods = {"addNewNonEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteNonEmptyTab() throws InterruptedException {
	deleteTab(1);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewDashboard() throws InterruptedException {
        initDashboardsPage();
        String dashboardName = "test";
        dashboardsPage.addNewDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
        Thread.sleep(5000);
        checkRedBar(browser);
        assertEquals(dashboardsPage.getDashboardsCount(), 2, "New dashboard is not present");
        assertEquals(dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
        Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addNewDashboard"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewTabOnNewDashboard() throws InterruptedException {
        addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
    }

    @Test(dependsOnGroups = {"new-dashboard"}, groups = {"dashboards-verification"})
    public void deleteNewDashboard() throws InterruptedException {
        initDashboardsPage();
        int dashboardsCount = dashboardsPage.getDashboardsCount();
        if (dashboardsPage.selectDashboard("test")) {
            dashboardsPage.deleteDashboard();
            Thread.sleep(3000);
            waitForDashboardPageLoaded(browser);
            assertEquals(dashboardsPage.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
            checkRedBar(browser);
        } else {
            fail("Dashboard wasn't selected and not deleted");
        }
    }

    @Test(dependsOnGroups = {"dashboards-verification"})
    public void verifyDashboardTabsAfter() throws InterruptedException {
        verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }
    
    private void deleteTab(int offset) throws InterruptedException {
        initDashboardsPage();
        assertTrue(dashboardsPage.selectDashboard("Pipeline Analysis"), "Dashboard wasn't selected");
        waitForDashboardPageLoaded(browser);
        Thread.sleep(5000);
        int tabsCount = dashboardsPage.getTabs().getNumberOfTabs();
        dashboardsPage.deleteDashboardTab(tabsCount - offset);
        Thread.sleep(5000);
        assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
    }
}
