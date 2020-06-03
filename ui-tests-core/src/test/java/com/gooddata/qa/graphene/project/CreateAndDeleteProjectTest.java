package com.gooddata.qa.graphene.project;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar.getCurrentProjectName;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.TimeoutException;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.project.ProjectDriver;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
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

    @Test(dependsOnMethods = { "deleteProjectByInvitedAdminUser" })
    public void switchToDashboardPageWithOneProject() throws IOException {
        if (!useDynamicUser) {
            throw new SkipException("To avoid affecting to configuration of another project, should use dynamic user");
        }
        RestClient restClient = new RestClient(getProfile(ADMIN));
        UserManagementRestRequest userManagementRestRequest =
                new UserManagementRestRequest(restClient, testParams.getProjectId());
        int countProjects = restClient.getProjectService().getProjects().size();
        if (countProjects > 1) {
            throw new SkipException("Don't test for user has more than 1 project");
        }
        userManagementRestRequest.setFeatureFlags(ProjectFeatureFlags.IS_REDIRECTED_FOR_ONE_PROJECT, true);
        logoutAndLoginAs(true, UserRoles.ADMIN);
        try {
            openUrl(PAGE_PROJECTS);
            waitForDashboardPageLoaded(browser);
        } catch (TimeoutException e) {
            fail("Don't switch to dashboard page");
        } finally {
            userManagementRestRequest.setFeatureFlags(ProjectFeatureFlags.IS_REDIRECTED_FOR_ONE_PROJECT, false);
        }
    }
}
