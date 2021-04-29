package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
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
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.java.Builder;

public class GoodSalesDashboardTest extends GoodSalesAbstractTest {

    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";
    private final String FIRST_TAB = "First Tab";
    private final String NON_EMPTY_TAB = "non-empty-tab";
    private final String EMPTY_TAB = "empty-tab";
    private final String DASH_BOARD_EMPTY_TAB_ADD = "Dashboard Empty Tab Add";
    private final String DASH_BOARD_NON_EMPTY_TAB_ADD = "Dashboard Non Empty Tab Add";
    private final String DASH_BOARD_EMPTY_TAB_DELETE = "Dashboard Empty Tab Delete";
    private final String DASH_BOARD_NON_EMPTY_TAB_DELETE = "Dashboard Non Empty Tab Delete";
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

        Dashboard dashboardEmptyTabDelete = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASH_BOARD_EMPTY_TAB_DELETE);
            dash.addTab(initDashboardTab(SOURCE_TAB, asList()));
            dash.addTab(initDashboardTab(TARGET_TAB, asList()));
            dash.addTab(initDashboardTab(EMPTY_TAB, asList()));
        }).build();

        Dashboard dashboardNonEmptyTabDelete = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASH_BOARD_NON_EMPTY_TAB_DELETE);
            dash.addTab(initDashboardTab(SOURCE_TAB, asList()));
            dash.addTab(initDashboardTab(TARGET_TAB, asList()));
            dash.addTab(initDashboardTab(NON_EMPTY_TAB, singletonList(createReportItem(reportUri))));
        }).build();

        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.createDashboard(dashboard.getMdObject());
        dashboardRequest.createDashboard(dashboardEmptyTabDelete.getMdObject());
        dashboardRequest.createDashboard(dashboardNonEmptyTabDelete.getMdObject());

        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[] {SOURCE_TAB, TARGET_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_EMPTY_TAB_DELETE, new String[] {SOURCE_TAB, TARGET_TAB, EMPTY_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_NON_EMPTY_TAB_DELETE, new String[] {SOURCE_TAB, TARGET_TAB, NON_EMPTY_TAB});
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
        dashboardsPage.openTab(0);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"exportFirstDashboard"}, groups = {"dashboards-verification"})
    public void verifyExportedDashboardPDF() {
        if (testParams.isClientDemoEnvironment()) {
            throw new SkipException("There isn't exported feature in client demo enviroment");
        }
        verifyDashboardExport(exportedDashboardName, SOURCE_TAB);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification"})
    public void addNewEmptyTab() {
        addNewTabOnNewDashboard(DASH_BOARD_EMPTY_TAB_ADD, EMPTY_TAB, "GoodSales-new-empty-tab", true);
    }

    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void addNewNonEmptyTab() {
        addNewTabOnNewDashboard(DASH_BOARD_NON_EMPTY_TAB_ADD, NON_EMPTY_TAB, "GoodSales-new-non-empty-tab", false);
        dashboardsPage.addLineToDashboard().saveDashboard();
    }

    @Test(dependsOnMethods = {"addNewEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteEmptyTab() {
        deleteTab(DASH_BOARD_EMPTY_TAB_DELETE,2);
    }

    @Test(dependsOnMethods = {"addNewNonEmptyTab"}, groups = {"dashboards-verification"})
    public void deleteNonEmptyTab() {
        deleteTab(DASH_BOARD_NON_EMPTY_TAB_DELETE,2);
    }

    @Test(dependsOnMethods = {"verifyDashboardTabs"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewDashboard() {
        initDashboardsPage();
        String dashboardName = "test";
        dashboardsPage.addNewDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        checkRedBar(browser);
        assertEquals(dashboardsPage.getDashboardsCount(), 4, "New dashboard is not present");
        assertEquals(dashboardsPage.getDashboardName(), dashboardName, "New dashboard has invalid name");
        Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addNewDashboard"}, groups = {"dashboards-verification", "new-dashboard"})
    public void addNewTabOnNewDashboard() {
        addNewTabOnNewDashboard("test1", "test2", "GoodSales-new-dashboard-new-tab", true);
    }

    @Test(dependsOnGroups = {"new-dashboard"}, groups = {"dashboards-verification"})
    public void deleteNewDashboard() {
        initDashboardsPage();
        int dashboardsCount = dashboardsPage.getDashboardsCount();
        dashboardsPage.selectDashboard("test1");
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
        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[] {SOURCE_TAB, TARGET_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_EMPTY_TAB_ADD, new String[] {FIRST_TAB, EMPTY_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_NON_EMPTY_TAB_ADD, new String[] {FIRST_TAB, NON_EMPTY_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_EMPTY_TAB_DELETE, new String[] {SOURCE_TAB, TARGET_TAB});
        expectedGoodSalesDashboardsAndTabs.put(DASH_BOARD_NON_EMPTY_TAB_DELETE, new String[] {SOURCE_TAB, TARGET_TAB});
        expectedGoodSalesDashboardsAndTabs.put("test", new String[] {FIRST_TAB});

        verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openDefaultDashboardWithoutPID() throws JSONException, IOException {
        String domainUser = testParams.getDomainUser() == null ? testParams.getUser() : testParams.getDomainUser();

        String defaultUserUri = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId())
                .getCurrentUserProfile()
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

    private void deleteTab(String dashboardName, int offset) {
        initDashboardsPage();
        dashboardsPage.selectDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(5);
        int tabsCount = dashboardsPage.getTabs().getNumberOfTabs();
        dashboardsPage.deleteDashboardTab(offset);
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
