package com.gooddata.qa.graphene.project;

import org.testng.annotations.Test;

import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = { "GoodSalesDashboard" }, description = "Tests for GoodSales project (dashboards functionality) in GD platform")
public class GoodSalesDashboardTest extends GoodSalesAbstractTest {
	
	private String exportedDashboardName;
	private long expectedDashboardExportSize = 65000L;
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "dashboards-verification" })
	public void verifyDashboardTabs() throws InterruptedException {
		verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
	}

	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification" })
	public void exportFirstDashboard() throws InterruptedException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		waitForElementVisible(dashboardsPage.getRoot());
		exportedDashboardName = dashboardsPage.exportDashboardTab(0);
		checkRedBar();
	}
	
	@Test(dependsOnMethods = { "exportFirstDashboard" }, groups = { "dashboards-verification" })
	public void verifyExportedDashboardPDF() {
		verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification" })
	public void addNewTab() throws InterruptedException {
		addNewTabOnDashboard("Pipeline Analysis", "test", "GoodSales-new-tab");
	}
	
	/** 
	 * Temporarily disabled test for adding report on dashboard tab since there is a weird behavior
	 *  - dialog for adding report and report itself is present, but webdriver can't use it since it's not visible (probably some css issue?)
	 *  
	@Test(dependsOnMethods = { "addNewTab" }, groups = { "dashboards-verification" })
	public void addReportOnNewTab() throws InterruptedException {
		initDashboardsPage();
		dashboardsPage.selectDashboard("Pipeline Analysis");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		dashboardsPage.getTabs().openTab(9);
		waitForDashboardPageLoaded();
		dashboardsPage.editDashboard();
		dashboardsPage.getDashboardEditBar().addReportToDashboard("Activities by Type");
		dashboardsPage.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "GoodSales-new-tab-with-chart", this.getClass());
	}
	*/

	@Test(dependsOnMethods = { "addNewTab" }, groups = { "dashboards-verification" })
	public void deleteNewTab() throws InterruptedException {
		initDashboardsPage();
		assertTrue(dashboardsPage.selectDashboard("Pipeline Analysis"), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		Thread.sleep(5000);
		int tabsCount = dashboardsPage.getTabs().getNumberOfTabs();
		dashboardsPage.deleteDashboardTab(expectedGoodSalesDashboardsAndTabs.size());
		Thread.sleep(5000);
		assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewDashboard() throws InterruptedException {
		initDashboardsPage();
		String dashboardName = "test";
		dashboardsPage.addNewDashboard(dashboardName);
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
		Thread.sleep(5000);
		checkRedBar();
		assertEquals(dashboardsPage.getDashboardsCount(), 2, "New dashboard is not present");
		assertEquals(dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
		Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "addNewDashboard" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewTabOnNewDashboard() throws InterruptedException {
		addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
	}
	
	@Test(dependsOnGroups = { "new-dashboard" }, groups = { "dashboards-verification" })
	public void deleteNewDashboard() throws InterruptedException {
		initDashboardsPage();
		int dashboardsCount = dashboardsPage.getDashboardsCount();
		if (dashboardsPage.selectDashboard("test")) {
			dashboardsPage.deleteDashboard();
			waitForDashboardPageLoaded();
			assertEquals(dashboardsPage.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
			checkRedBar();
		} else {
			fail("Dashboard wasn't selected and not deleted");
		}
	}
	
	@Test(dependsOnGroups = { "dashboards-verification" }, groups = { "tests" })
	public void verifyDashboardTabsAfter() throws InterruptedException {
		verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
	}
}
