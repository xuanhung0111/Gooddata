package com.gooddata.qa.graphene.reset;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.PASSWORD_HINT;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.updateUserPassword;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.createBlankProject;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

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

        GoodData goodDataClient = getGoodDataClient(testUser, testParams.getPassword());
        testParams.setProjectId(createBlankProject(goodDataClient, "Reset-password-test",
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment()));
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
            updateUserPassword(getRestApiClient(), testParams.getUserDomain(), testUser, NEW_PASSWORD, testParams.getPassword());
        }
    }

    /*
     * Disable this test case due to bug https://jira.intgdc.com/browse/WA-5739, will enable again when it is fixed
     */
    @Test(dependsOnMethods = {"resetWithValidAndInvalidPassword"}, enabled = false)
    public void openOneProject() {
        List<String> projectIds = initProjectsPage().getProjectsIds(PROJECT_NAME);
        assertFalse(projectIds.isEmpty(), "Project Ids are empty");

        ProjectsPage.getInstance(browser).goToProject(projectIds.get(0));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(projectIds.get(0));
    }

    @Test(dependsOnMethods = { "openOneProject" }, enabled = false)
    public void checkSectionManagementVulnerability() throws ParseException, JSONException, IOException {
        initDashboardsPage();
        updateUserPassword(getRestApiClient(), testParams.getUserDomain(), testUser, testParams.getPassword(), NEW_PASSWORD);

        try {
            sleepTightInSeconds(600);

            browser.navigate().refresh();
            LoginFragment.waitForPageLoaded(browser);
            takeScreenshot(browser, "Out of section after reset password", this.getClass());

        } finally {
            updateUserPassword(getRestApiClient(), testParams.getUserDomain(), testUser, NEW_PASSWORD, testParams.getPassword());
        }
    }
}
