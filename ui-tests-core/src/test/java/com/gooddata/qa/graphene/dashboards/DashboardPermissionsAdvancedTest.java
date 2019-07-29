package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static org.hamcrest.Matchers.hasItem;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class DashboardPermissionsAdvancedTest extends GoodSalesAbstractTest {

    private DashboardRestRequest dashboardRequest;
    private static final String WAR_MSG = "You do not have permission to access this dashboard.";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard Permissions Advanced Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DASHBOARD_ACCESS_CONTROL, true);
    }

    @DataProvider(name = "userRoles")
    public Object[][] getUserRoles() {
        return new Object[][]{
                {UserRoles.EDITOR},
                {UserRoles.VIEWER},
                {UserRoles.EDITOR_AND_INVITATIONS},
                {UserRoles.EDITOR_AND_USER_ADMIN},
                {UserRoles.VIEWER_DISABLED_EXPORT}
        };
    }

    @DataProvider(name = "editorGroupUserRoles")
    public Object[][] getEditorGroupUserRoles() {
        return new Object[][]{
                {UserRoles.EDITOR},
                {UserRoles.EDITOR_AND_INVITATIONS},
                {UserRoles.EDITOR_AND_USER_ADMIN}
        };
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
        createAndAddUserToProject(UserRoles.EDITOR_AND_INVITATIONS);
        createAndAddUserToProject(UserRoles.VIEWER_DISABLED_EXPORT);
    }

    /**
     * senario 3
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "userRoles")
    public void shouldViewDashboardNotShareAndNotSACByUserRoles(UserRoles userRoles) throws Exception {
        String dashboardName = "Dashboard Not Share And SAC";
        String dashboardUri = preparePrivateDashboard(dashboardName);
        dashboardsPage.hasNotSAC();
        Screenshots.takeScreenshot(browser, "Should View Dashboard Not Share And Not SAC By " + userRoles.getName(), getClass());
        try {
            log.info("userRoles : " + userRoles);
            signInAndAccessDashboardWithUserRoles(dashboardUri, userRoles);
            List<String> dashboards = dashboardsPage.getDashboardsNames();
            assertThat(dashboards, hasItem(dashboardName));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "userRoles")
    public void shouldViewDashboardSharedAndSACByUserRoles(UserRoles userRoles) throws Exception {
        String dashboardName = "Dashboard Shared And SAC";
        String dashboardUri = preparePrivateDashboard(dashboardName);
        dashboardsPage.hasSAC();
        addUserShare(userRoles);
        Screenshots.takeScreenshot(browser, "Should View Dashboard Share And SAC By " + userRoles.getName(), getClass());
        try {
            log.info("userRoles : " + userRoles);
            signInAndAccessDashboardWithUserRoles(dashboardUri, userRoles);
            List<String> dashboards = dashboardsPage.getDashboardsNames();
            assertThat(dashboards, hasItem(dashboardName));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "userRoles")
    public void shouldViewDashboardSharedAndNotSACByUserRoles(UserRoles userRoles) throws Exception {
        String dashboardName = "Dashboard Shared And Not SAC";
        String dashboardUri = preparePrivateDashboard(dashboardName);
        dashboardsPage.hasNotSAC();
        addUserShare(userRoles);
        Screenshots.takeScreenshot(browser, "Should View Dashboard Share And Not SAC By "
                + userRoles.getName(), getClass());
        try {
            signInAndAccessDashboardWithUserRoles(dashboardUri, userRoles);
            List<String> dashboards = dashboardsPage.getDashboardsNames();
            assertThat(dashboards, hasItem(dashboardName));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    /**
     * senario 4
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "userRoles")
    public void shouldShowWaringMessagePage(UserRoles userRoles) throws Exception {
        String dashboardName = "Show warning page when access SAC dashboard";
        String dashboardUri = preparePrivateDashboard(dashboardName);
        dashboardsPage.hasSAC();
        try {
            signInAndAccessDashboardWithUserRoles(dashboardUri, userRoles);

            String warningMess = dashboardsPage.getDashboardNoPermissionAccessText();
            Screenshots.takeScreenshot(browser, "Should Show Waring Message Page ", getClass());
            assertEquals(warningMess, "You do not have permission to access this dashboard.");

            dashboardsPage.takeToMyDashboard();
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    /**
     * senario 6B
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "editorGroupUserRoles")
    public void shouldEditorCanSetSACUnlockedDashboard(UserRoles userRoles) throws Exception {
        String dashboardName = "Unlocked and published for Editor";
        String dashboardUri = createTestDashboard(dashboardName);
        try {
            dashboardRequest.setPrivateDashboard(dashboardName, true);
            initDashboardsPage();

            logoutAndLoginAs(true, userRoles);

            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardUri);

            waitForDashboardPageLoaded(browser);

            final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
            assertEquals(permissionsDialog.getAddedGrantees().size(), 1);
            assertTrue(permissionsDialog.isStrictAccessControlCheckBoxSelected(),
                    "Strict access control checkbox should be selected");
            permissionsDialog.submit();
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    /**
     * senario 7
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "editorGroupUserRoles")
    public void shouldEditorCanNotSetSACLockedDashboard(UserRoles userRoles) throws Exception {
        initDashboardsPage();
        String dashboardName = "Locked and published for Editor";
        String dashboardUri = createTestDashboard(dashboardName);
        try {
            dashboardRequest.setPrivateDashboard(dashboardName, true);
            dashboardRequest.setLockedDashboard(dashboardName, true);
            initDashboardsPage();

            logoutAndLoginAs(true, userRoles);

            // Editor loads the dashboard url of Admin
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardUri);

            waitForDashboardPageLoaded(browser);
            Screenshots.takeScreenshot(browser, "Should Editor Can Not Set SAC Locked Dashboard ", getClass());

            final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

            assertEquals(permissionsDialog.getAddedGrantees().size(), 1);
            assertFalse(permissionsDialog.isSACOptionDisplayed(), "SAC option shouldn't display");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    /**
     * senario 16
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"}, dataProvider = "userRoles")
    public void editorLoginLogoutSACdashboard(UserRoles userRoles) throws Exception {
        String dashboardName = "Editor login/logout SAC dashboard";
        String dashboardUri = preparePrivateDashboard(dashboardName);
        dashboardsPage.hasSAC();
        try {
            log.info("Logout and Login with user : " + userRoles);
            signInAndAccessDashboardWithUserRoles(dashboardUri, userRoles);
            Screenshots.takeScreenshot(browser, userRoles.getName() +
                    " Login Logout SAC Dashboard", DashboardPermissionsBasicTest.class);

            String warningMess = dashboardsPage.getDashboardNoPermissionAccessText();

            assertEquals(warningMess, WAR_MSG);

            dashboardsPage.takeToMyDashboard();
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    private String preparePrivateDashboard(String dashboardName) throws JSONException, IOException {
        String dashboardUri = createTestDashboard(dashboardName);
        dashboardRequest.setPrivateDashboard(dashboardName, true);
        return dashboardUri;
    }

    private void signInAndAccessDashboardWithUserRoles(String dashboardUri, UserRoles userRoles) {
        logoutAndLoginAs(true, userRoles);
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardUri);
    }

    private void addUserShare(UserRoles userRoles){
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        String user = getUser(userRoles);
        dashboardsPage.addUserGroup(permissionsDialog, addGranteesDialog, user);
    }
}
