package com.gooddata.qa.graphene.project;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.projects.PopupDialog;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class LeaveProjectTest extends AbstractProjectTest {

    private final static String ERROR_MESSAGE = "You cannot leave the project because you are the only administrator in it.";
    private String anotherAdminUser;

    @Override
    public void enableDynamicUser() {
        // these tests are working on another project & user
        // turn off using dynamic user to simplify workflow and increase readability
        useDynamicUser = false;
    }

    @Override
    protected void initProperties() {
        // use empty project
        projectTitle = "Leave-Project-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        anotherAdminUser = createAndAddUserToProject(UserRoles.ADMIN);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testProjectHavingOneAdmin() {
        String blankProjectID = createBlankProject("Project-Having-One-Admin");
        try {
            initProjectsPage().getProjectItem(blankProjectID).leave();
            assertEquals(PopupDialog.getInstance(browser).getMessage(),
                    ERROR_MESSAGE, "The error msg is not correct");
        } finally {
            ProjectRestUtils.deleteProject(getGoodDataClient(), blankProjectID);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testProjectHavingMultipleAdmins() throws IOException, JSONException {
        String workingProjectID = testParams.getProjectId();
        testParams.setProjectId(createBlankProject("Project-Having-Two-Admins"));
        try {
            String anotherUser = createAndAddUserToProject(UserRoles.ADMIN);

            logout();
            signInAtGreyPages(anotherUser, testParams.getPassword());

            initProjectsPage().getProjectItem(testParams.getProjectId()).leave();
            assertFalse(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                    "The project is not removed from list");
        } finally {
            // switch to domain user because the new user which is created in this test has just left created project
            logoutAndLoginAs(true, UserRoles.ADMIN);
            ProjectRestUtils.deleteProject(getGoodDataClient(), testParams.getProjectId());
            testParams.setProjectId(workingProjectID);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveProjectInProjectAndUsersPage() throws ParseException, JSONException, IOException {
        try {
            logout();
            signInAtGreyPages(anotherAdminUser, testParams.getPassword());
            initProjectsAndUsersPage().leaveProject();
            assertFalse(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                "The project is not removed from list");
    
            logoutAndLoginAs(true, UserRoles.ADMIN);
            ProjectAndUsersPage projectAndUsersPage = initProjectsAndUsersPage();
            assertFalse(projectAndUsersPage.isLeaveButtonEnable(), "The only administrator still can leave project");
            assertEquals(projectAndUsersPage.getMessageNextToLeaveButton(), ERROR_MESSAGE);
        } finally {
            addUserToProject(anotherAdminUser, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveProjectInProjectsPage() throws JSONException, IOException {
        try {
            logout();
            signInAtGreyPages(anotherAdminUser, testParams.getPassword());
            initProjectsPage().getProjectItem(testParams.getProjectId()).leave();
            assertFalse(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                "The project is not removed from list");

            logoutAndLoginAs(true, UserRoles.VIEWER);
            initProjectsPage().getProjectItem(testParams.getProjectId()).leave();
            assertFalse(initProjectsPage().isProjectDisplayed(testParams.getProjectId()),
                "The project is not removed from list");

            logoutAndLoginAs(true, UserRoles.ADMIN);
            initProjectsPage().getProjectItem(testParams.getProjectId()).leave();
            assertEquals(PopupDialog.getInstance(browser).getMessage(), ERROR_MESSAGE, "The error msg is not correct");
        } finally {
            addUserToProject(testParams.getViewerUser(), UserRoles.VIEWER);
            addUserToProject(anotherAdminUser, UserRoles.ADMIN);
        }
    }

    private String createBlankProject(String name) {
        return ProjectRestUtils.createProject(getGoodDataClient(testParams.getUser(), testParams.getPassword()),
                name, null, testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams
                        .getProjectEnvironment());
    }
}
