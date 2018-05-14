package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.browser.BrowserUtils.runScript;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MobileDropdownNavigationTest extends AbstractDashboardTest {

    private final String DASHBOARD_LONG_NAME = "Dashboard with the long title over width mobile screen - " + generateHashString();
    private final String DASHBOARD_LOST = "Dashboard Lost - " + generateHashString();
    private final String TARGET_DASHBOARD = "Dashboard Target - " + generateHashString();
    private final String SUB_DASHBOARD = "Dashboard Sub - " + generateHashString();
    private String dynamicUser;
    private String newProjectId;
    private String currentProjectId;

    @Override
    protected void customizeProject() {
        currentProjectId = testParams.getProjectId();
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createLostKpi()), DASHBOARD_LOST);
        indigoRestRequest.createAnalyticalDashboard(singletonList(createLostKpi()), DASHBOARD_LONG_NAME);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        dynamicUser = createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addUsersToNewProject() throws IOException {
        newProjectId = createProjectUsingFixture(projectTitle, GOODSALES,
                testParams.getDomainUser() == null ? testParams.getUser() : testParams.getDomainUser());
        UserManagementRestUtils.addUserToProject(getDomainUserRestApiClient(), newProjectId, testParams.getUser(), UserRoles.ADMIN);
        UserManagementRestUtils.addUserToProject(getDomainUserRestApiClient(), newProjectId, dynamicUser, UserRoles.EDITOR);
        testParams.setProjectId(newProjectId);
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createNumOfActivitiesKpi()), TARGET_DASHBOARD);
        indigoRestRequest.createAnalyticalDashboard(singletonList(createNumOfActivitiesKpi()), SUB_DASHBOARD);
        testParams.setProjectId(currentProjectId);
    }

    @DataProvider
    public Object[][] getUserRoles() {
        return new Object[][] { { UserRoles.ADMIN }, { UserRoles.EDITOR } };
    }

    @Test(dependsOnMethods = {"addUsersToNewProject"}, dataProvider = "getUserRoles")
    public void testMobileProjectList(UserRoles role) {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertEquals(indigoDashboardsPage.getDashboardTitles(),
                    asList(DASHBOARD_LOST, DASHBOARD_LONG_NAME));

            testParams.setProjectId(newProjectId);
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "drop-down-navigation-test-with" + role, getClass());
            assertEquals(indigoDashboardsPage.getDashboardTitles(), asList(SUB_DASHBOARD, TARGET_DASHBOARD));
        } finally {
            testParams.setProjectId(currentProjectId);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testMobileHamburgerMenu(UserRoles role) {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            HamburgerMenu hamburgerMenu = Header.getInstance(browser).openHamburgerMenu();
            takeScreenshot(browser, "hamburger-menu-test-with-" + role, getClass());
            assertEquals(hamburgerMenu.getAllMenuItems(),
                    asList("Dashboards", "Reports", "KPIs", "Analyze", "Load", "Manage"));
            hamburgerMenu.goToPage("Analyze");
            assertThat(browser.getCurrentUrl(), containsString("analyze/#/"+ testParams.getProjectId()));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testShortenedDashboardName(UserRoles role) {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            indigoDashboardsPage.selectKpiDashboard(DASHBOARD_LONG_NAME);

            takeScreenshot(browser, "shortened-dashboard-name-test-with-" + role, getClass());
            assertTrue(indigoDashboardsPage.isShortenTitleDesignByCss(557), "Title should be shortened");
            assertEquals(indigoDashboardsPage.getKpiTitles(), asList(METRIC_LOST));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testVisibilityOfScrollbarOnProjectList(UserRoles role) {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        List<String> dashboardLists = new ArrayList<>();
        for (int i = 0; i < 15; i++)
            dashboardLists.add(indigoRestRequest.createAnalyticalDashboard(singletonList(createLostKpi()),
                    "Dashboard " + i));
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(isScrollBarVisibleOnNavigation(), "Should display scroll bar on navigation");
        } finally {
            dashboardLists.stream().forEach(dashboard -> indigoRestRequest.deleteAnalyticalDashboard(dashboard));
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"testMobileProjectList"})
    public void tearDown() {
       deleteProject(newProjectId);
    }

    private boolean isScrollBarVisibleOnNavigation() {
        try {
            waitForElementVisible(By.className("mobile-navigation-button"), browser).click();
            return (Boolean) runScript(browser, "return arguments[0].scrollHeight > arguments[0].clientHeight;",
                    waitForElementVisible(className("gd-mobile-dropdown-content"), browser));
        } catch (Exception e) {
            throw new RuntimeException("Function just be valid on Mobile mode");
        }
    }
}
