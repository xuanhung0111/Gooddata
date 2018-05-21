package com.gooddata.qa.graphene.reset;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.PASSWORD_HINT;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;

import com.gooddata.project.Project;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;

public class ResetPasswordTest extends AbstractUITest {

    private static final By RESET_PASSWORD_PAGE_LOCATOR = By.className("s-resetPasswordPage");
    
    private static final String PROJECT_NAME = "GoodSales";

    private static final String NEW_PASSWORD = "Gooddata12345";

    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "Password too short. "
            + "Minimum length is 7 characters.";
    
    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "You selected a commonly used password. "
            + "Choose something unique.";

    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";

    private static final String INVALID_EMAIL_MESSAGE = "This is not a valid email address.";

    private static final String PASSWORD_PAGE_LOCAL_MESSAGE = "Check your email"
            + "\nAn email has been sent with instructions for resetting your password.";

    private static final String RESET_PASSWORD_SUCCESS_MESSAGE = "Your password has been reset"
            + "\nPlease use your new password to login below.";

    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String NON_REGISTERED_EMAIL = "gooddata@mailinator.com";

    private String testUser;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test
    public void prepareUserForTest() throws ParseException, JSONException, IOException {
        testUser = createDynamicUserFrom(imapUser);
        testParams.setProjectId(createNewEmptyProject("Reset-password-test"));
    }

    @Test(dependsOnMethods = {"prepareUserForTest"})
    public void resetPasswordWithInvalidEmail() {
        assertEquals(initLostPasswordPage()
            .resetPassword(INVALID_EMAIL, false)
            .getErrorMessage(), INVALID_EMAIL_MESSAGE);
        takeScreenshot(browser, "Invalid email error message", this.getClass());

        LostPasswordPage.getInstance(browser)
            .resetPassword(NON_REGISTERED_EMAIL, true);
        assertEquals(LostPasswordPage.getPageLocalMessage(browser), PASSWORD_PAGE_LOCAL_MESSAGE);
        takeScreenshot(browser, "Reset password with non-registered email", this.getClass());

        initLostPasswordPage()
            .backToLoginPage();
    }

    @Test(dependsOnMethods = {"resetPasswordWithInvalidEmail"})
    public void resetWithValidAndInvalidPassword() throws MessagingException, IOException, JSONException {
        String resetPasswordLink = doActionWithImapClient(imapClient ->
                LoginFragment.getInstance(browser)
                        .openLostPasswordPage()
                        .resetPassword(imapClient, testUser));
        assertEquals(LostPasswordPage.getPageLocalMessage(browser), PASSWORD_PAGE_LOCAL_MESSAGE);

        openUrl(resetPasswordLink);
        LostPasswordPage resetPasswordPage = LostPasswordPage.getInstance(RESET_PASSWORD_PAGE_LOCATOR, browser);

        final SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(resetPasswordPage.getPasswordHint(), PASSWORD_HINT);

        resetPasswordPage.setNewPassword("aaaaa");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        resetPasswordPage.setNewPassword("12345678");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(), COMMONLY_PASSWORD_ERROR_MESSAGE);

        resetPasswordPage.setNewPassword("aaaaaaaa");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);
        softAssert.assertAll();

        try {
            resetPasswordPage.setNewPassword(NEW_PASSWORD);
            assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), RESET_PASSWORD_SUCCESS_MESSAGE);

            LoginFragment.getInstance(browser).login(testUser, NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        } finally {
            new UserManagementRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                    .updateUserPassword(testParams.getUserDomain(), testUser, NEW_PASSWORD, testParams.getPassword());
        }
    }

    /*
     * Disable this test case due to bug https://jira.intgdc.com/browse/WA-5739, will enable again when it is fixed
     */
    @Test(dependsOnMethods = {"resetWithValidAndInvalidPassword"}, enabled = false)
    public void openOneProject() {
        assertTrue(initProjectsPage().isProjectDisplayed(PROJECT_NAME));

        ProjectsPage.ProjectItem projectItem = initProjectsPage().getProjectItem(PROJECT_NAME);
        projectItem.open();
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(projectItem.getId());
    }

    @Test(dependsOnMethods = { "openOneProject" }, enabled = false)
    public void checkSectionManagementVulnerability() throws ParseException, JSONException, IOException {
        initDashboardsPage();
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        userManagementRestRequest.updateUserPassword(testParams.getUserDomain(), testUser, testParams.getPassword(), NEW_PASSWORD);

        try {
            sleepTightInSeconds(600);

            browser.navigate().refresh();
            LoginFragment.waitForPageLoaded(browser);
            takeScreenshot(browser, "Out of section after reset password", this.getClass());

        } finally {
            userManagementRestRequest.updateUserPassword(testParams.getUserDomain(), testUser, NEW_PASSWORD, testParams.getPassword());
        }
    }

    protected String createNewEmptyProject(String projectTitle) {
        RestClient restClient = new RestClient(getProfile(ADMIN));
        final Project project = new Project(projectTitle, testParams.getAuthorizationToken());
        project.setDriver(testParams.getProjectDriver());
        project.setEnvironment(testParams.getProjectEnvironment());

        return restClient.getProjectService().createProject(project).get().getId();
    }
}
