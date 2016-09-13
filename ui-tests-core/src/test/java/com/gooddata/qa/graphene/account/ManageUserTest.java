package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

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

        assertTrue(accessProjectWithAnotherLogin(newAdminUser, testParams.getPassword(), "viewer-can-access-the-project", true)
                .contains(testParams.getProjectId()));
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
        
        assertTrue(accessProjectWithAnotherLogin(newAdminUser, testParams.getPassword(), "Viewer-can-access-the-project-again", true)
                .contains(testParams.getProjectId()));

        assertFalse(initProjectsAndUsersPage().openActiveUserTab()
                .isDeactivePermissionAvailable(newAdminUser), "Deactive button is displayed");
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