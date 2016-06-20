package com.gooddata.qa.graphene.project;

import static com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar.selectProject;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.UUID;

import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;

public class ProjectSwitchingTest extends AbstractProjectTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 6);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String newAdminUser;
    private String newAdminPassword;
    private String newAdminUserUri;

    private String embededDashboardUser;
    private String embededDashboardUserPassword;

    private String currentProjectId;
    private String newProjectId;

    private GoodData newAdminGoodDataClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void inviteUsersToProject() throws ParseException, IOException, JSONException {
        newAdminUser = generateUniqueUserEmail(testParams.getUser());
        newAdminPassword = testParams.getPassword();

        newAdminUserUri = UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), 
                newAdminUser, newAdminPassword);

        embededDashboardUser = testParams.getViewerUser();
        embededDashboardUserPassword = testParams.getViewerPassword();

        addUserToProject(newAdminUser, UserRoles.ADMIN);
        addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        addUserToProject(embededDashboardUser, UserRoles.DASHBOARD_ONLY);

        logout();
        signInAtGreyPages(newAdminUser, newAdminPassword);
    }

    @Test(dependsOnMethods = "inviteUsersToProject")
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();

        newProjectId = ProjectRestUtils.createBlankProject(getNewAdminGoodDataClient(), NEW_PROJECT_NAME,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                testParams.getProjectEnvironment());
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void tryOpenProjectByDashboardOnlyUser() throws JSONException {
        try {
            logout();
            signInAtGreyPages(embededDashboardUser, embededDashboardUserPassword);

            initProjectsPage();
            assertTrue(projectsPage.isProjectDisplayed(testParams.getProjectId()),
                    "Dashboard-Only user cannot view the project in Projects page");

            projectsPage.goToProject(testParams.getProjectId());
            waitForProjectsPageLoaded(browser);

            takeScreenshot(browser, "Dashboard only user cannot access the project", getClass());
            assertThat(browser.getCurrentUrl(), containsString("cannotAccessWorkbench"));

        } finally {
            logout();
            signInAtGreyPages(newAdminUser, newAdminPassword);
        }
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void tryOpenProjectByDisabledUser() throws JSONException {
        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);

            initProjectsPage();
            assertTrue(projectsPage.isProjectDisplayed(testParams.getProjectId()),
                    "Project does not display with invited user");

            projectsPage.goToProject(testParams.getProjectId());
            waitForDashboardPageLoaded(browser);

            logout();
            signInAtGreyPages(newAdminUser, newAdminPassword);

            initProjectsAndUsersPage();
            projectAndUsersPage.disableUser(testParams.getEditorUser());
            assertFalse(projectAndUsersPage.isUserDisplayedInList(testParams.getEditorUser()),
                    "User is not disabled and still displays in Active tab");

            projectAndUsersPage.openDeactivatedUserTab();
            assertTrue(projectAndUsersPage.isUserDisplayedInList(testParams.getEditorUser()),
                    "User does not display in Deactivated tab after disabled");

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);

            initProjectsPage();

            takeScreenshot(browser, "Disable user cannot view the project", getClass());
            assertFalse(projectsPage.isProjectDisplayed(testParams.getProjectId()),
                    "Project still displays with disabled user");

        } finally {
            logout();
            signInAtGreyPages(newAdminUser, newAdminPassword);
        }
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void switchProjects() throws ParseException, JSONException, IOException {
        try {
            initDashboardsPage();

            switchProject(NEW_PROJECT_NAME);

            takeScreenshot(browser, "Switch-to-project-" + NEW_PROJECT_NAME, getClass());
            assertThat(browser.getCurrentUrl(), containsString(newProjectId));

            switchProject(projectTitle);

            takeScreenshot(browser, "Switch-to-project-" + projectTitle, getClass());
            assertThat(browser.getCurrentUrl(), containsString(currentProjectId));

        } finally {
            ProjectRestUtils.deleteProject(getNewAdminGoodDataClient(), newProjectId);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws JSONException {
        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);

        UserManagementRestUtils.deleteUserByUri(getRestApiClient(), newAdminUserUri);
    }

    private String generateUniqueUserEmail(String email) {
        return email.replace("@", "+" + UUID.randomUUID().toString().substring(0, 6) + "@");
    }

    private GoodData getNewAdminGoodDataClient() {
        if (isNull(newAdminGoodDataClient)) {
            newAdminGoodDataClient = getGoodDataClient(newAdminUser, newAdminPassword);
        }

        return newAdminGoodDataClient;
    }

    private void switchProject(String name) {
        selectProject(name, browser);
        waitForDashboardPageLoaded(browser);
    }
}
