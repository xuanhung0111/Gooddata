package com.gooddata.qa.graphene.project;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
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
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		DashboardsPage dashboards = Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
		exportedDashboardName = dashboards.exportDashboardTab(0);
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
	@Test(dependsOnMethods = { "addNewTab" }, groups = { "dashboards-verification" })
	public void addReportOnNewTab() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		dashboards.selectDashboard("Pipeline Analysis");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		dashboards.getTabs().openTab(9);
		waitForDashboardPageLoaded();
		dashboards.editDashboard();
		dashboards.getDashboardEditBar().addReportToDashboard("Activities by Type");
		dashboards.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "GoodSales-new-tab-with-chart", this.getClass());
	}
	*/
	
	@Test(dependsOnMethods = { "addNewTab" }, groups = { "dashboards-verification" })
	public void deleteNewTab() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		Assert.assertTrue(dashboards.selectDashboard("Pipeline Analysis"), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		int tabsCount = dashboards.getTabs().getNumberOfTabs();
		dashboards.deleteDashboardTab(expectedGoodSalesTabs.length);
		Assert.assertEquals(dashboards.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewDashboard() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		String dashboardName = "test";
		dashboards.addNewDashboard(dashboardName);
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboards.getDashboardEditBar().getRoot());
		Thread.sleep(5000);
		checkRedBar();
		Assert.assertEquals(dashboards.getDashboardsCount(), 2, "New dashboard is not present");
		Assert.assertEquals(dashboards.getDashboardName(), dashboardName, "New dashboard has invalid name");
		Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "addNewDashboard" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewTabOnNewDashboard() throws InterruptedException {
		addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
	}
	
	@Test(dependsOnGroups = { "new-dashboard" }, groups = { "dashboards-verification" })
	public void deleteNewDashboard() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		int dashboardsCount = dashboards.getDashboardsCount();
		if (dashboards.selectDashboard("test")) {
			dashboards.deleteDashboard();
			waitForDashboardPageLoaded();
			Assert.assertEquals(dashboards.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
			checkRedBar();
		} else {
			Assert.fail("Dashboard wasn't selected and not deleted");
		}
	}
	
	@Test(dependsOnGroups = { "dashboards-verification" }, groups = { "lastTest" })
	public void verifyDashboardTabsAfter() throws InterruptedException {
		verifyProjectDashboardTabs(true, expectedGoodSalesTabs.length, expectedGoodSalesTabs, true);
	}
	
	private DashboardsPage initDashboardsPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		return Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
	}
	
	private void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		Assert.assertTrue(dashboards.selectDashboard(dashboardName), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		int tabsCount = dashboards.getTabs().getNumberOfTabs();
		dashboards.editDashboard();
		waitForDashboardPageLoaded();
		dashboards.addNewTab(tabName);
		checkRedBar();
		Assert.assertEquals(dashboards.getTabs().getNumberOfTabs(), tabsCount + 1, "New tab is not present");
		Assert.assertTrue(dashboards.getTabs().isTabSelected(tabsCount), "New tab is not selected");
		Assert.assertEquals(dashboards.getTabs().getTabLabel(tabsCount), tabName, "New tab has invalid label");
		dashboards.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboards.getDashboardEditBar().getRoot());
		Assert.assertEquals(dashboards.getTabs().getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
		Assert.assertTrue(dashboards.getTabs().isTabSelected(tabsCount), "New tab is not selected after Save");
		Assert.assertEquals(dashboards.getTabs().getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
		Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
	}
}
