package com.gooddata.qa.graphene.reset;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import com.gooddata.project.Project;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
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

    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "The password must have at least 7 characters.";

    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "Given password is commonly used.";

    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";

    private static final String PASSWORD_MATCHES_OLD_PASSWORD = "The password must be different from your last 1 passwords.";

    private static final String PASSWORD_CONTAINS_LOGIN = "Password contains login which is forbidden.";

    private static final String INVALID_EMAIL_MESSAGE = "This is not a valid email address.";

    private static final String PASSWORD_PAGE_LOCAL_MESSAGE = "Check your email"
            + "\nAn email has been sent with instructions for resetting your password.";

    private static final String RESET_PASSWORD_SUCCESS_MESSAGE = "Your password has been reset"
            + "\nPlease use your new password to login below.";

    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String NON_REGISTERED_EMAIL = "gooddata@mailinator.com";

    private String testUser;
    private String testHistoryPasswordUser;
    private int historyPasswordLimit;

    ProjectRestRequest projectRestRequest;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() throws IOException{
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        historyPasswordLimit = Integer.parseInt(
            projectRestRequest.getValueOfDomainFeatureFlag("security.password.history.limit"));
    }

    @Test
    public void prepareUserForTest() throws ParseException, JSONException, IOException {
        testUser = createDynamicUserFrom(imapUser);
        testHistoryPasswordUser = createDynamicUserFrom(imapUser);
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
                        .resetPassword(imapClient, testHistoryPasswordUser));
        assertEquals(LostPasswordPage.getPageLocalMessage(browser), PASSWORD_PAGE_LOCAL_MESSAGE);

        openUrl(resetPasswordLink);
        LostPasswordPage resetPasswordPage = LostPasswordPage.getInstance(RESET_PASSWORD_PAGE_LOCATOR, browser);

        final SoftAssert softAssert = new SoftAssert();

        resetPasswordPage.setNewPassword("aaaaa");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(),
            SHORT_PASSWORD_ERROR_MESSAGE +
            '\n' +
            SEQUENTIAL_PASSWORD_ERROR_MESSAGE
        );

        resetPasswordPage.setNewPassword("12345678");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(),
            COMMONLY_PASSWORD_ERROR_MESSAGE+
            '\n' +
            SEQUENTIAL_PASSWORD_ERROR_MESSAGE
        );

        resetPasswordPage.setNewPassword("aaaaaaaa");
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);

        if (isDefaultHistoryPasswordLimit()) {
            resetPasswordPage.setNewPassword(testParams.getPassword());
            softAssert.assertEquals(resetPasswordPage.getErrorMessage(), PASSWORD_MATCHES_OLD_PASSWORD);

        } else {
            // cover for ticket https://jira.intgdc.com/browse/QA-9109
            String newFirstPassword = "NewPass";

            resetPasswordPage.setNewPassword(newFirstPassword);
            for (int i = 0; i < historyPasswordLimit - 1; i++) {
                // When resetting the password greater than 6 times, captcha is required. So don't cover cases.
                if (i == 6){
                    break;
                }
                resetPasswordLink = doActionWithImapClient(imapClient ->
                    LoginFragment.getInstance(browser)
                        .openLostPasswordPage()
                        .resetPassword(imapClient, testHistoryPasswordUser));

                openUrl(resetPasswordLink);

                String newPassword = "NewPass" + i;
                log.info("NEW PASSWORD: " + newPassword);
                resetPasswordPage.setNewPassword(newPassword);
            }
            resetPasswordLink = doActionWithImapClient(imapClient ->
                LoginFragment.getInstance(browser)
                    .openLostPasswordPage()
                    .resetPassword(imapClient, testHistoryPasswordUser));

            openUrl(resetPasswordLink);
            resetPasswordPage.setNewPassword(newFirstPassword);
            softAssert.assertEquals(resetPasswordPage.getErrorMessage(),
                "The password must be different from your last " + historyPasswordLimit + " passwords.");
        }

        resetPasswordPage.setNewPassword(testHistoryPasswordUser);
        softAssert.assertEquals(resetPasswordPage.getErrorMessage(), PASSWORD_CONTAINS_LOGIN);
        softAssert.assertAll();

        resetPasswordPage.setNewPassword(NEW_PASSWORD);
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), RESET_PASSWORD_SUCCESS_MESSAGE);

        LoginFragment.getInstance(browser).login(testHistoryPasswordUser, NEW_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
    }

    /*
     * Disable this test case due to bug https://jira.intgdc.com/browse/WA-5739, will enable again when it is fixed
     */
    @Test(dependsOnMethods = {"resetWithValidAndInvalidPassword"}, enabled = false)
    public void openOneProject() {
        assertTrue(initProjectsPage().isProjectDisplayed(PROJECT_NAME), PROJECT_NAME + " should display");

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

        return restClient.getProjectService().createProject(project).get(testParams.getCreateProjectTimeout(), TimeUnit.MINUTES).getId();
    }

    private boolean isDefaultHistoryPasswordLimit() {
        return historyPasswordLimit == 1;
    }
}
