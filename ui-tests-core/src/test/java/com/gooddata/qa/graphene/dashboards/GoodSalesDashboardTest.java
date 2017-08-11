package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;

public class GoodSalesDashboardTest extends GoodSalesAbstractTest {

    private static final long expectedDashboardExportSize = 65000L;
    private String exportedDashboardName;

    @Test(dependsOnGroups = {"createProject"}, groups = {"dashboards-verification"})
    public void verifyDashboardTabs() {
        verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void exportFirstDashboard() {
        if (testParams.isClientDemoEnvironment()) return;
        initDashboardsPage();
        dashboardsPage.selectDashboard(DASH_PIPELINE_ANALYSIS);
        waitForDashboardPageLoaded(browser);
        exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"exportFirstDashboard"}, groups = {"dashboards-verification"})
    public void verifyExportedDashboardPDF() {
        if (testParams.isClientDemoEnvironment()) return;
        verifyDashboardExport(exportedDashboardName, "Outlook", expectedDashboardExportSize);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void addNewEmptyTab() {
        addNewTabOnDashboard(DASH_PIPELINE_ANALYSIS, "empty-tab", "GoodSales-new-empty-tab");
    }

    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void addNewNonEmptyTab() {
        addNewTabOnDashboard(DASH_PIPELINE_ANALYSIS, "non-empty-tab", "GoodSales-new-non-empty-tab");
        dashboardsPage.addLineToDashboard().saveDashboard();
    }

    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteEmptyTab() {
        deleteTab(2);
    }

    @Test(dependsOnMethods = {"addNewNonEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteNonEmptyTab() {
        deleteTab(1);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewDashboard() {
        initDashboardsPage();
        String dashboardName = "test";
        dashboardsPage.addNewDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        checkRedBar(browser);
        assertEquals(dashboardsPage.getDashboardsCount(), 2, "New dashboard is not present");
        assertEquals(dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
        Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addNewDashboard"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewTabOnNewDashboard() {
        addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
    }

    @Test(dependsOnGroups = {"new-dashboard"}, groups = {"dashboards-verification"})
    public void deleteNewDashboard() {
        initDashboardsPage();
        int dashboardsCount = dashboardsPage.getDashboardsCount();
        dashboardsPage.selectDashboard("test");
        dashboardsPage.deleteDashboard();
        sleepTightInSeconds(3);
        waitForDashboardPageLoaded(browser);
        assertEquals(dashboardsPage.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
        checkRedBar(browser);

        // webapp can use this time to update a dashboard has been deleted
        // to avoid RED BAR - Dashboard no longer exists
        sleepTightInSeconds(5);
    }

    @Test(dependsOnGroups = {"dashboards-verification"})
    public void verifyDashboardTabsAfter() {
        verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openDefaultDashboardWithoutPID() throws JSONException, IOException {
        String domainUser = testParams.getDomainUser() == null ? testParams.getUser() : testParams.getDomainUser();
        DashboardsRestUtils.setDefaultDashboardForDomainUser(getDomainUserRestApiClient(), testParams.getProjectId(),
                DASH_PIPELINE_ANALYSIS, DASH_TAB_WHATS_CHANGED);
        logout();
        signInAtGreyPages(domainUser, testParams.getPassword());
        try {
            openDefaultDashboardOfDomainUser();
            String dashboardUri = DashboardsRestUtils.getDashboardUri(getRestApiClient(), testParams.getProjectId(), 
                    DASH_PIPELINE_ANALYSIS);
            String tabID = DashboardsRestUtils.getTabId(getRestApiClient(), testParams.getProjectId(), 
                    DASH_PIPELINE_ANALYSIS, DASH_TAB_WHATS_CHANGED);
            assertThat(browser.getCurrentUrl(), containsString(dashboardUri));
            assertThat(browser.getCurrentUrl(), containsString(tabID));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private void deleteTab(int offset) {
        initDashboardsPage();
        dashboardsPage.selectDashboard(DASH_PIPELINE_ANALYSIS);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(5);
        int tabsCount = dashboardsPage.getTabs().getNumberOfTabs();
        dashboardsPage.deleteDashboardTab(tabsCount - offset);
        sleepTightInSeconds(5);
        assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), tabsCount - 1, "Tab is still present");
    }

    private void openDefaultDashboardOfDomainUser() {
        openUrl("/dashboard.html");
        waitForDashboardPageLoaded(browser);
    }
}
