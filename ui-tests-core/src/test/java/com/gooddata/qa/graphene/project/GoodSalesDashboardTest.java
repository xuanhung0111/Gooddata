package com.gooddata.qa.graphene.project;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "GoodSalesDashboard" }, description = "Tests for GoodSales project (dashboards functionality) in GD platform")
public class GoodSalesDashboardTest extends GoodSalesAbstractTest {
	
	private String exportedDashboardName;
	private long expectedDashboardExportSize = 65000L;
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "dashboards-verification" })
	public void verifyDashboardTabs() throws InterruptedException {
		verifyProjectDashboardTabs(true, expectedGoodSalesTabs.length, expectedGoodSalesTabs, true);
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
		Assert.assertTrue(dashboardsPage.selectDashboard("Pipeline Analysis"), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		int tabsCount = dashboardsPage.getTabs().getNumberOfTabs();
		dashboardsPage.deleteDashboardTab(expectedGoodSalesTabs.length);
		Assert.assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
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
		Assert.assertEquals(dashboardsPage.getDashboardsCount(), 2, "New dashboard is not present");
		Assert.assertEquals(dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
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
			Assert.assertEquals(dashboardsPage.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
			checkRedBar();
		} else {
			Assert.fail("Dashboard wasn't selected and not deleted");
		}
	}
	
	@Test(dependsOnGroups = { "dashboards-verification" }, groups = { "lastTest" })
	public void verifyDashboardTabsAfter() throws InterruptedException {
		verifyProjectDashboardTabs(true, expectedGoodSalesTabs.length, expectedGoodSalesTabs, true);
	}
	
	private void initDashboardsPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		waitForElementVisible(dashboardsPage.getRoot());
	}
	
	private void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
		initDashboardsPage();
		Assert.assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		DashboardTabs tabs = dashboardsPage.getTabs();
		int tabsCount = tabs.getNumberOfTabs();
		dashboardsPage.editDashboard();
		waitForDashboardPageLoaded();
		dashboardsPage.addNewTab(tabName);
		checkRedBar();
		Assert.assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
		Assert.assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
		Assert.assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
		dashboardsPage.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
		Assert.assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
		Assert.assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
		Assert.assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
		Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
	}
}
