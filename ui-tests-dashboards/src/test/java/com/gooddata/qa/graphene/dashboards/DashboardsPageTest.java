package com.gooddata.qa.graphene.dashboards;

import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.utils.graphene.Screenshots;


@Test(groups = {"dashboard"}, description = "Tests for basic dashboards functionality in GD platform")
public class DashboardsPageTest extends AbstractTest {

    @BeforeClass
    public void initStartPage() {
        testParams.setProjectId(testParams.loadProperty("projectId"));

        startPage = uiUtils.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage";
    }

    /**
     * Initial test for dashboard page
     * - verifies/do login at the beginning of the test
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test(groups = {"dashboardInit"}, alwaysRun = true)
    public void gd_Dashboard_001_DashboardElements() throws InterruptedException, JSONException {
        //TODO - redirect
        Thread.sleep(5000);
        validSignInWithDemoUser(false);
        checkUtils.waitForDashboardPageLoaded();
        waitForElementVisible(uiUtils.dashboardsPage.getRoot());
        Assert.assertNotNull(uiUtils.dashboardsPage, "Dashboard page not initialized!");
    }

    @Test(dependsOnGroups = {"dashboardInit"})
    public void gd_Dashboard_002_GetNumberOfTabs() {
        checkUtils.waitForDashboardPageLoaded();
        int numberOfTabs = uiUtils.dashboardsPage.getTabs().getNumberOfTabs();
        System.out.println("Number of tabs for selected project: " + numberOfTabs);
        Assert.assertTrue(numberOfTabs > 0);
    }

    @Test(dependsOnGroups = {"dashboardInit"})
    public void gd_Dashboard_003_TabsSwitching() {
        checkUtils.waitForDashboardPageLoaded();
        DashboardTabs tabs = uiUtils.dashboardsPage.getTabs();
        int numberOfTabs = tabs.getNumberOfTabs();
        for (int i = 0; i < numberOfTabs; i++) {
            tabs.openTab(i);
            System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
            checkUtils.waitForDashboardPageLoaded();
            Screenshots.takeScreenshot(browser, "dashboards-tab-" + i + "-" + tabs.getTabLabel(i), this.getClass());
            Assert.assertTrue(tabs.isTabSelected(i));
        }
    }

    @Test(dependsOnGroups = {"dashboardInit"})
    public void gd_Dashboard_004_GetTabLabels() {
        checkUtils.waitForDashboardPageLoaded();
        List<String> tabLabels = uiUtils.dashboardsPage.getTabs().getAllTabNames();
        System.out.println("These tabs are available for selected project: " + tabLabels.toString());
        Assert.assertTrue(tabLabels.size() > 0 && tabLabels.get(0).length() > 0);
    }

    @Test(dependsOnGroups = {"dashboardInit"})
    public void gd_Dashboard_005_SameTabIsSelectedAfterRefresh() {
        checkUtils.waitForDashboardPageLoaded();
        DashboardTabs tabs = uiUtils.dashboardsPage.getTabs();
        int numberOfTabs = tabs.getNumberOfTabs();
        int selectedTab = new Random().nextInt(numberOfTabs);
        System.out.println("Randomly selected tab: " + selectedTab);
        tabs.openTab(selectedTab);
        checkUtils.waitForDashboardPageLoaded();
        loadPlatformPageBeforeTestMethod(); // to refresh browser and initial page
        checkUtils.waitForDashboardPageLoaded();
        Assert.assertTrue(tabs.isTabSelected(selectedTab));
    }
}
