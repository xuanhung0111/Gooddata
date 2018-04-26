package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.java.Builder;

public class GoodSalesDashboardTest extends GoodSalesAbstractTest {

    private static final long expectedDashboardExportSize = 45000L;
    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";
    private String exportedDashboardName;
    private Map<String, String[]> expectedGoodSalesDashboardsAndTabs;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        String reportUri = getReportCreator().createAmountByProductReport();
        Tab sourceTab = initDashboardTab(SOURCE_TAB, singletonList(createReportItem(reportUri)));
        Tab targetTab = initDashboardTab(TARGET_TAB, singletonList(createReportItem(reportUri)));
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASH_PIPELINE_ANALYSIS);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
        }).build();

        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.createDashboard(dashboard.getMdObject());

        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[] {SOURCE_TAB, TARGET_TAB});
    }

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
        verifyDashboardExport(exportedDashboardName, SOURCE_TAB, expectedDashboardExportSize);
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

        String defaultUserUri = UserManagementRestUtils
                .getCurrentUserProfile(getDomainUserRestApiClient())
                .getJSONObject("links")
                .getString("self")
                .concat("/settings/defaults");

        new DashboardRestRequest(new RestClient(getProfile(DOMAIN)), testParams.getProjectId())
                .setDefaultDashboardForUser(DASH_PIPELINE_ANALYSIS, TARGET_TAB, defaultUserUri);

        logout();
        signInAtGreyPages(domainUser, testParams.getPassword());
        try {
            openDefaultDashboardOfDomainUser();

            DashboardRestRequest dashboardRequest = new DashboardRestRequest(
                    new RestClient(getProfile(ADMIN)), testParams.getProjectId());

            String dashboardUri = dashboardRequest.getDashboardUri(DASH_PIPELINE_ANALYSIS);
            String tabID = dashboardRequest.getTabId(DASH_PIPELINE_ANALYSIS, TARGET_TAB);

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

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }
}
