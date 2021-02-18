package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import com.gooddata.qa.graphene.fragments.freegrowth.ManageTabs;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;

public class ManageUserTest extends AbstractProjectTest {

    private String newAdminUser;

    @Test(dependsOnGroups = "createProject")
    public void addNewUserToProject() throws ParseException, IOException, JSONException {
        newAdminUser = createAndAddUserToProject(UserRoles.ADMIN);

        assertThat(accessProjectWithAnotherLogin(newAdminUser, testParams.getPassword(),
                "viewer-can-access-the-project", true), containsString(testParams.getProjectId()));
    }

    @Test(dependsOnMethods = "addNewUserToProject")
    public void disableAddedUser() throws JSONException {
        logoutAndLoginAs(true, UserRoles.ADMIN);

        initProjectsAndUsersPage().disableUser(newAdminUser);

        assertFalse(ProjectAndUsersPage.getInstance(browser).isUserDisplayedInList(newAdminUser),
                newAdminUser + " has not been deactivated");

        assertTrue(ProjectAndUsersPage.getInstance(browser).openDeactivatedUserTab().isUserDisplayedInList(newAdminUser),
                newAdminUser + " does not exist in deactive tab");

        assertTrue(accessProjectWithAnotherLogin(newAdminUser, testParams.getPassword(),
                "Deactivated-user-cannot-access-the-project", false)
                .contains("#status=notAuthorized"), "Not authorized msg is not displayed ");
    }

    @Test(dependsOnMethods = "disableAddedUser")
    public void enableAddedUser() throws JSONException {
        logoutAndLoginAs(true, UserRoles.ADMIN);

        initProjectsAndUsersPage().enableUser(newAdminUser);

        assertFalse(ProjectAndUsersPage.getInstance(browser).isUserDisplayedInList(newAdminUser),
                newAdminUser + " has not been activated");

        assertTrue(ProjectAndUsersPage.getInstance(browser).openActiveUserTab().isUserDisplayedInList(newAdminUser),
                newAdminUser + " does not exist in active tab");
        
        assertThat(accessProjectWithAnotherLogin(newAdminUser, testParams.getPassword(),
                "Viewer-can-access-the-project-again", true), containsString(testParams.getProjectId()));

        assertFalse(initProjectsAndUsersPage().openActiveUserTab()
                .isDeactivePermissionAvailable(newAdminUser), "Deactive button is displayed");
    }

    @Test(dependsOnMethods = "enableAddedUser")
    public void allowUserRoleEnterMangeUserGroups() throws JSONException {
        log.info("Login with Editor + Admin user");
        logoutAndLoginAs(true, UserRoles.EDITOR_AND_USER_ADMIN);
        initManagePage();
        ManageTabs manageTabs  = ManageTabs.getInstance(browser);
        assertTrue(manageTabs.isProjectAndUserTabVisible(), "Project and user tab should be visible");
        ProjectAndUsersPage projectAndUsersPage =initProjectsAndUsersPage();
        assertTrue(projectAndUsersPage.isManageUserGroupLinkDisplayed(), "Manage User Group link should be displayed");
        UserManagementPage userManage = projectAndUsersPage.openUserManagementPage().waitForUsersContentLoaded();
        assertEquals(userManage.getUserRole(testParams.getInfoUser(UserRoles.EDITOR_AND_USER_ADMIN).getKey()),
                "Editor + User Admin");

        log.info("Login with editor user");
        logoutAndLoginAs(true, UserRoles.EDITOR);
        initManagePage();
        assertFalse(manageTabs.isProjectAndUserTabVisible(), "Project and user tab shouldn't be visible");
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
    }

    private String accessProjectWithAnotherLogin(String user, String password, String screenShotName, boolean isSuccess)
            throws JSONException {
        logout();
        signInAtGreyPages(user, password);

        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId());
        if (isSuccess) {
            waitForDashboardPageLoaded(browser);
        } else {
            waitForProjectsPageLoaded(browser);
        }

        takeScreenshot(browser, screenShotName, getClass());
        return browser.getCurrentUrl();
    }

}