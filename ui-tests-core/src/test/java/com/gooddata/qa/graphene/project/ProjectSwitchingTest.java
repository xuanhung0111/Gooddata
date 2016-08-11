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

import java.io.IOException;
import java.util.UUID;

import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

public class ProjectSwitchingTest extends AbstractProjectTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 6);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String embededDashboardUser;
    private String embededDashboardUserPassword;

    private String currentProjectId;
    private String newProjectId;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void inviteUsersToProject() throws ParseException, IOException, JSONException {
        embededDashboardUser = testParams.getViewerUser();
        embededDashboardUserPassword = testParams.getViewerPassword();

        addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        addUserToProject(embededDashboardUser, UserRoles.DASHBOARD_ONLY);
    }

    @Test(dependsOnMethods = "inviteUsersToProject")
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();

        newProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), NEW_PROJECT_NAME,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                testParams.getProjectEnvironment());
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void tryOpenProjectByDashboardOnlyUser() throws JSONException {
        try {
            logout();
            signInAtGreyPages(embededDashboardUser, embededDashboardUserPassword);

            assertTrue(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                    "Dashboard-Only user cannot view the project in Projects page");

            ProjectsPage.getInstance(browser).goToProject(testParams.getProjectId());
            waitForProjectsPageLoaded(browser);

            takeScreenshot(browser, "Dashboard only user cannot access the project", getClass());
            assertThat(browser.getCurrentUrl(), containsString("cannotAccessWorkbench"));

        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void tryOpenProjectByDisabledUser() throws JSONException {
        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);

            assertTrue(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                    "Project does not display with invited user");

            ProjectsPage.getInstance(browser).goToProject(testParams.getProjectId());
            waitForDashboardPageLoaded(browser);

            logout();
            signIn(true, UserRoles.ADMIN);

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
            assertFalse(ProjectsPage.getInstance(browser).isProjectDisplayed(testParams.getProjectId()),
                    "Project still displays with disabled user");

        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
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
            ProjectRestUtils.deleteProject(getGoodDataClient(), newProjectId);
        }
    }

    private void switchProject(String name) {
        selectProject(name, browser);
        waitForDashboardPageLoaded(browser);
    }
}
