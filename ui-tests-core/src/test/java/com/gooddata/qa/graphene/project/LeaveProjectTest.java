package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class LeaveProjectTest extends AbstractProjectTest {

    private final static String ERROR_MESSAGE = "You cannot leave the project because you are the only administrator in it.";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "GoodSales-Leave-Project-Test";
    }

    @Override
    public void enableDynamicUser() {
        // these tests are working on another project & user
        // turn off using dynamic user to simplify workflow and increase readability
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testProjectHavingOneAdmin() {
        String workingProjectID = testParams.getProjectId();

        String anotherProject = "Project-Having-One-Admin";
        testParams.setProjectId(createBlankProject(anotherProject));
        try {
            ProjectsPage page = initProjectsPage().leaveProject(anotherProject);
            assertEquals(page.getPopupDialog().getMessage(), ERROR_MESSAGE, "The error msg is not correct");
        } finally {
            ProjectRestUtils.deleteProject(getGoodDataClient(), testParams.getProjectId());
            testParams.setProjectId(workingProjectID);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testProjectHavingMultipleAdmins() throws IOException, JSONException {
        String currentProjectID = testParams.getProjectId();
        String currentUser = testParams.getUser();

        String anotherProject = "Project-Having-Two-Admins";
        testParams.setProjectId(createBlankProject(anotherProject));
        try {
            // the test needs to use another user which is different from domain user
            String anotherUser = createDynamicUserFrom(testParams.getUser().replace("@", "+" + UserRoles.ADMIN
                    .getName().toLowerCase() + "@"));
            UserManagementRestUtils.addUserToProject(getRestApiClient(testParams.getUser(), testParams.getPassword())
                    , testParams.getProjectId(), anotherUser, UserRoles.ADMIN);

            testParams.setUser(anotherUser);
            logoutAndLoginAs(true, UserRoles.ADMIN);

            initProjectsPage().leaveProject(anotherProject);
            assertFalse(initProjectsPage().isProjectDisplayed(testParams.getProjectId()), "The project is not removed" +
                    " from list");
        } finally {
            // switch to domain user because the new user which is created in this test has just left created project
            testParams.setUser(currentUser);
            logoutAndLoginAs(true, UserRoles.ADMIN);

            ProjectRestUtils.deleteProject(getGoodDataClient(testParams.getUser(), testParams.getPassword()),
                    testParams.getProjectId());
            testParams.setProjectId(currentProjectID);
        }
    }

    private String createBlankProject(String name) {
        return ProjectRestUtils.createProject(getGoodDataClient(testParams.getUser(), testParams.getPassword()),
                name, null, testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams
                        .getProjectEnvironment());
    }
}
