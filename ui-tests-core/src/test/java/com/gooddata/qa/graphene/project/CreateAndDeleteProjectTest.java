package com.gooddata.qa.graphene.project;

import static com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar.getCurrentProjectName;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;

public class CreateAndDeleteProjectTest extends AbstractProjectTest {

    private static final String FIRST_EDITED_PROJECT_NAME = "Project rename first";
    private static final String SECOND_EDITED_PROJECT_NAME = "Project rename second";

    private String firstProjectId;
    private String secondProjectId;

    @Override
    protected void initProperties() {
        projectTitle = "Project-create-and-delete-test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        firstProjectId = testParams.getProjectId();

        openUrl(PAGE_GDC_PROJECTS);
        assertEquals(waitForFragmentVisible(gpProject).getDwhDriverSelected(), ProjectDriver.POSTGRES.getValue());

        secondProjectId = createNewEmptyProject(projectTitle);
    }

    @Test(dependsOnMethods = {"createAnotherProject"})
    public void renameProjectByOwner() {
        initProjectsPage().goToProject(firstProjectId);
        waitForDashboardPageLoaded(browser);

        assertEquals(initProjectsAndUsersPage().renameProject(FIRST_EDITED_PROJECT_NAME).getProjectName(),
                FIRST_EDITED_PROJECT_NAME);
        assertEquals(getCurrentProjectName(browser), FIRST_EDITED_PROJECT_NAME);

        assertTrue(initProjectsPage().isProjectDisplayed(FIRST_EDITED_PROJECT_NAME),
                "The project has not been renamed to " + FIRST_EDITED_PROJECT_NAME);
    }

    @Test(dependsOnMethods = {"renameProjectByOwner"})
    public void renameProjectByInvitedAdminUser() throws ParseException, JSONException, IOException {
        String invitedAdminUser = createAndAddUserToProject(UserRoles.ADMIN);

        logout();
        signInAtGreyPages(invitedAdminUser, testParams.getPassword());

        initProjectsPage().goToProject(firstProjectId);
        waitForDashboardPageLoaded(browser);

        assertEquals(initProjectsAndUsersPage().renameProject(SECOND_EDITED_PROJECT_NAME).getProjectName(),
                SECOND_EDITED_PROJECT_NAME);
        assertEquals(getCurrentProjectName(browser), SECOND_EDITED_PROJECT_NAME);

        assertTrue(initProjectsPage().isProjectDisplayed(SECOND_EDITED_PROJECT_NAME),
                "The project has not been renamed to " + SECOND_EDITED_PROJECT_NAME);
    }

    @Test(dependsOnMethods = { "renameProjectByInvitedAdminUser" })
    public void deleteProjectByInvitedAdminUser() {
        try {
            assertTrue(initProjectsAndUsersPage().isDeleteButtonEnabled(), "Delete button is not enabled");

            ProjectAndUsersPage.getInstance(browser).tryDeleteProjectButDiscard();
            assertTrue(initProjectsPage().isProjectDisplayed(firstProjectId),
                    "Project is still deleted after discard Delete project dialog");

            assertFalse(initProjectsAndUsersPage().deteleProject().isProjectDisplayed(firstProjectId), "Project is still not deleted");

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to delete project", this.getClass());
            throw e;

        } finally {
            testParams.setProjectId(secondProjectId);
        }
    }
}
