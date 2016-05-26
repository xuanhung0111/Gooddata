package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class ProjectSwitchTest extends AbstractCsvUploaderTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 10);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String newAdminUser;
    private String newAdminPassword;
    private String newAdminUserUri;

    private String currentProjectId;
    private String newProjectId;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void prepareUserForSwitchingTest() throws ParseException, JSONException, IOException {
        newAdminUser = generateEmail(testParams.getUser());
        newAdminPassword = testParams.getPassword();

        newAdminUserUri = UserManagementRestUtils.createUser(getRestApiClient(), newAdminUser, newAdminPassword);

        addUserToProject(newAdminUser, UserRoles.ADMIN);

        logout();
        signInAtGreyPages(newAdminUser, newAdminPassword);
    }

    @Test(dependsOnMethods = {"prepareUserForSwitchingTest"}, groups = {"precondition"})
    public void getMoreProject() {
        GoodData goodDataClient = getGoodDataClient(newAdminUser, newAdminPassword);

        currentProjectId = testParams.getProjectId();

        newProjectId = ProjectRestUtils.createBlankProject(goodDataClient, NEW_PROJECT_NAME,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                testParams.getProjectEnvironment());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void switchProjectsTest() {
        initDataUploadPage().switchProject(NEW_PROJECT_NAME);

        takeScreenshot(browser, "Switch-to-project-" + NEW_PROJECT_NAME, getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        datasetsListPage.switchProject(projectTitle);

        takeScreenshot(browser, "Switch-to-project-" + projectTitle, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Test(dependsOnMethods = { "switchProjectsTest" })
    public void openDataUploadPageAfterDeleteAnotherProject() {
        testParams.setProjectId(newProjectId);

        try {
            initDashboardsPage();
            assertThat(browser.getCurrentUrl(), containsString(newProjectId));

            initProjectsAndUsersPage().deteleProject();
            waitForProjectsPageLoaded(browser);

        } finally {
            testParams.setProjectId(currentProjectId);
        }

        initDataUploadPage();
        takeScreenshot(browser, "Re-open-data-upload-page-of-project-" + currentProjectId +
                "-after-delete-project-" + newProjectId, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws JSONException {
        logoutAndLoginAs(true, UserRoles.ADMIN);
        UserManagementRestUtils.deleteUserByUri(getRestApiClient(), newAdminUserUri);
    }
}
