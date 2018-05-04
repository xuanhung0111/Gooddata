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

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;

public class ProjectSwitchingTest extends AbstractProjectTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 6);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String currentProjectId;
    private String newProjectId;

    @Override
    protected void initProperties() {
        // use empty project
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();

        newProjectId = createNewEmptyProject(NEW_PROJECT_NAME);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryOpenProjectByDashboardOnlyUser() throws JSONException, ParseException, IOException {
        String embededDashboardUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
        try {
            logout();
            signInAtGreyPages(embededDashboardUser, testParams.getPassword());

            assertTrue(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                    "Dashboard-Only user cannot view the project in Projects page");

            ProjectsPage.getInstance(browser).goToProject(testParams.getProjectId());
            waitForProjectsPageLoaded(browser);

            takeScreenshot(browser, "Dashboard only user cannot access the project", getClass());
            assertThat(browser.getCurrentUrl(), containsString("cannotAccessWorkbench"));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryOpenProjectByDisabledUser() throws JSONException {
        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);

            assertTrue(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                    "Project does not display with invited user");

            ProjectsPage.getInstance(browser).goToProject(testParams.getProjectId());
            waitForDashboardPageLoaded(browser);

            logout();
            signIn(true, UserRoles.ADMIN);

            assertFalse(initProjectsAndUsersPage().disableUser(testParams.getEditorUser()).isUserDisplayedInList(testParams.getEditorUser()),
                    "User is not disabled and still displays in Active tab");

            assertTrue(ProjectAndUsersPage.getInstance(browser).openDeactivatedUserTab().isUserDisplayedInList(testParams.getEditorUser()),
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

    @Test(dependsOnMethods = {"getMoreProject"})
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
            deleteProject(newProjectId);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void switchProject(String name) {
        selectProject(name, browser);
        waitForDashboardPageLoaded(browser);
    }
}
