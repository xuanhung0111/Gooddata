package com.gooddata.qa.graphene.dashboards;

import java.util.List;
import java.util.Random;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.utils.graphene.Screenshots;


@Test(groups = { "dashboard" }, description = "Tests for basic dashboards functionality in GD platform")
public class DashboardsPageTest extends AbstractTest {
	
	public static final By BY_PROJECT_SELECTOR_BUTTON = By.xpath("//div[@id='subnavigation']/button");
	
	@BeforeClass
	public void initStartPage() {
		projectId = loadProperty("projectId");
		projectName = loadProperty("projectName");
		
		startPage = PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage";
	}
	
	DashboardsPage dashboards;
	
	/**
	 * Initial test for dashboard page
	 *  - verifies/do login at the beginning of the test
	 * @throws InterruptedException 
	 * @throws JSONException 
	 */
	@Test(groups = {"dashboardInit"}, alwaysRun = true)
	public void gd_Dashboard_001_DashboardElements() throws InterruptedException, JSONException {
		//TODO - redirect
		Thread.sleep(5000);
		validSignInWithDemoUser(false);
		waitForDashboardPageLoaded();
		dashboards = Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
		Assert.assertNotNull(dashboards, "Dashboard page not initialized!");
	}

	@Test(dependsOnGroups = {"dashboardInit"})
	public void gd_Dashboard_002_GetNumberOfTabs() {
		waitForDashboardPageLoaded();
		int numberOfTabs = dashboards.getTabs().getNumberOfTabs();
		System.out.println("Number of tabs for selected project: " + numberOfTabs);
		Assert.assertTrue(numberOfTabs > 0);
	}
	
	@Test(dependsOnGroups = {"dashboardInit"})
	public void gd_Dashboard_003_TabsSwitching() {
		waitForDashboardPageLoaded();
		DashboardTabs tabs = dashboards.getTabs();
		int numberOfTabs = tabs.getNumberOfTabs();
		for (int i = 0; i < numberOfTabs; i++) {
			tabs.openTab(i);
			System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
			waitForDashboardPageLoaded();
			Screenshots.takeScreenshot(browser, "dashboards-tab-" + i + "-" + tabs.getTabLabel(i), this.getClass());
			Assert.assertTrue(tabs.isTabSelected(i)); 
		}
	}
	
	@Test(dependsOnGroups = {"dashboardInit"})
	public void gd_Dashboard_004_GetTabLabels() {
		waitForDashboardPageLoaded();
		List<String> tabLabels = dashboards.getTabs().getAllTabNames();
		System.out.println("These tabs are available for selected project: " + tabLabels.toString());
		Assert.assertTrue(tabLabels.size() > 0 && tabLabels.get(0).length() > 0);
	}
	
	@Test(dependsOnGroups = {"dashboardInit"})
	public void gd_Dashboard_005_SameTabIsSelectedAfterRefresh() {
		waitForDashboardPageLoaded();
		DashboardTabs tabs = dashboards.getTabs();
		int numberOfTabs = tabs.getNumberOfTabs();
		int selectedTab = new Random().nextInt(numberOfTabs);
		System.out.println("Randomly selected tab: " + selectedTab);
		tabs.openTab(selectedTab);
		waitForDashboardPageLoaded();
		loadPlatformPageBeforeTestMethod(); // to refresh browser and initial page
		waitForDashboardPageLoaded();
		Assert.assertTrue(tabs.isTabSelected(selectedTab));
	}
}
