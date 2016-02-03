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
import java.util.ArrayList;
import java.util.List;

import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.RestUtils;

public class ProjectSwitchingTest extends AbstractProjectTest {

    private List<String> projectIds = new ArrayList<>();

    private String editorUser;
    private String editorPassword;
    private String embededDashboardUser;
    private String embededDashboardUserPassword;

    @Test(dependsOnMethods = {"createProject"})
    public void inviteUsersToProject() throws ParseException, IOException, JSONException {
        editorUser = testParams.getEditorUser();
        editorPassword = testParams.getEditorPassword();

        embededDashboardUser = testParams.getViewerUser();
        embededDashboardUserPassword = testParams.getViewerPassword();

        addUserToProject(editorUser, UserRoles.EDITOR);
        addUserToProject(embededDashboardUser, UserRoles.DASHBOARD_ONLY);
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
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void tryOpenProjectByDisabledUser() throws JSONException {
        try {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initProjectsPage();
            assertTrue(projectsPage.isProjectDisplayed(testParams.getProjectId()),
                    "Project does not display with invited user");

            projectsPage.goToProject(testParams.getProjectId());
            waitForDashboardPageLoaded(browser);

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

            initProjectsAndUsersPage();
            projectAndUsersPage.disableUser(editorUser);
            assertFalse(projectAndUsersPage.isUserDisplayedInList(editorUser),
                    "User is not disabled and still displays in Active tab");

            projectAndUsersPage.openDeactivatedUserTab();
            assertTrue(projectAndUsersPage.isUserDisplayedInList(editorUser),
                    "User does not display in Deactivated tab after disabled");

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initProjectsPage();

            takeScreenshot(browser, "Disable user cannot view the project", getClass());
            assertFalse(projectsPage.isProjectDisplayed(testParams.getProjectId()),
                    "Project still displays with disabled user");

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"inviteUsersToProject"})
    public void switchProjects() throws ParseException, JSONException, IOException {
        try {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);
            restApiClient = getRestApiClient(editorUser, editorPassword);

            for (int i = 0; i < 3; i++) {
                projectIds.add(ProjectRestUtils.createBlankProject(getGoodDataClient(), "Project switching " + i,
                        testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                        testParams.getProjectEnvironment()));
            }

            try {
                initDashboardsPage();

                for (String projectId : projectIds) {
                    selectProject(projectId, browser);
                    waitForDashboardPageLoaded(browser);

                    takeScreenshot(browser, "Switch to project: " + projectId, getClass());
                    assertThat(browser.getCurrentUrl(), containsString(projectId));
                }

            } finally {
                for (String projectId : projectIds) {
                    ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
                }
            }
        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }
}
