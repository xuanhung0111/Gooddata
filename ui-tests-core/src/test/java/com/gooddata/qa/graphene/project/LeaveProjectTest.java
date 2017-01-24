package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.projects.PopupDialog;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
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

    private String createBlankProject(String name) {
        return ProjectRestUtils.createProject(getGoodDataClient(testParams.getUser(), testParams.getPassword()),
                name, null, testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams
                        .getProjectEnvironment());
    }
}
