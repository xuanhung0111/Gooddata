package com.gooddata.qa.graphene.reset;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;

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

    private String user;
    private String oldPassword;

    private ImapClient imapClient;

    @Test(groups = {PROJECT_INIT_GROUP})
    public void init() {
        user = testParams.getUser();
        oldPassword = testParams.getPassword();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        imapClient = new ImapClient(imapHost, imapUser, imapPassword);
    }

    @Test(dependsOnMethods = {"init"}, groups = {PROJECT_INIT_GROUP})
    public void resetPasswordWithInvalidEmail() {
        initLostPasswordPage();

        lostPasswordPage.resetPassword(INVALID_EMAIL, false);
        assertEquals(lostPasswordPage.getErrorMessage(), INVALID_EMAIL_MESSAGE);
        takeScreenshot(browser, "Invalid email error message", this.getClass());

        lostPasswordPage.resetPassword(NON_REGISTERED_EMAIL, true);
        assertEquals(lostPasswordPage.getPageLocalMessage(), PASSWORD_PAGE_LOCAL_MESSAGE);
        takeScreenshot(browser, "Reset password with non-registered email", this.getClass());

        initLostPasswordPage();
        lostPasswordPage.backToLoginPage();
    }

    @Test(dependsOnMethods = {"resetPasswordWithInvalidEmail"}, groups = {PROJECT_INIT_GROUP})
    public void resetWithValidAndInvalidPassword() throws MessagingException, IOException, JSONException {
        LostPasswordPage lostPasswordPage = LoginFragment.getInstance(browser).openLostPasswordPage();

        String resetPasswordLink = lostPasswordPage.resetPassword(imapClient, user);
        assertEquals(lostPasswordPage.getPageLocalMessage(), PASSWORD_PAGE_LOCAL_MESSAGE);

        openUrl(resetPasswordLink);
        LostPasswordPage resetPasswordPage = Graphene.createPageFragment(LostPasswordPage.class,
                waitForElementVisible(RESET_PASSWORD_PAGE_LOCATOR, browser));

        resetPasswordPage.setNewPassword("aaaaa");
        assertEquals(resetPasswordPage.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        resetPasswordPage.setNewPassword("12345678");
        assertEquals(resetPasswordPage.getErrorMessage(), COMMONLY_PASSWORD_ERROR_MESSAGE);

        resetPasswordPage.setNewPassword("aaaaaaaa");
        assertEquals(resetPasswordPage.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);
        try {
            testParams.setPassword(NEW_PASSWORD);

            resetPasswordPage.setNewPassword(NEW_PASSWORD);
            assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), RESET_PASSWORD_SUCCESS_MESSAGE);

            LoginFragment.getInstance(browser).login(user, NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to reset password", this.getClass());
            throw e;

        } finally {
            logout()
                .login(testParams.getUser(), NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

            UserManagementRestUtils.updateCurrentUserPassword(getRestApiClient(), NEW_PASSWORD, oldPassword);
            testParams.setPassword(oldPassword);
        }
    }

    /*
     * Disable this test case due to bug https://jira.intgdc.com/browse/WA-5739, will enable again when it is fixed
     */
    @Test(dependsOnMethods = {"resetWithValidAndInvalidPassword"}, enabled = false)
    public void openOneProject() {
        initProjectsPage();

        List<String> projectIds = waitForFragmentVisible(projectsPage).getProjectsIds(PROJECT_NAME);
        assertFalse(projectIds.isEmpty(), "Project Ids are empty");

        projectsPage.goToProject(projectIds.get(0));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(projectIds.get(0));
    }

    @Test(dependsOnMethods = { "openOneProject" }, enabled = false)
    public void checkSectionManagementVulnerability() throws ParseException, JSONException, IOException {
        initDashboardsPage();
        UserManagementRestUtils.updateCurrentUserPassword(getRestApiClient(), oldPassword, NEW_PASSWORD);

        try {
            sleepTightInSeconds(600);

            browser.navigate().refresh();
            LoginFragment.waitForPageLoaded(browser);
            takeScreenshot(browser, "Out of section after reset password", this.getClass());

        } catch(Exception e) {
            takeScreenshot(browser, "Login section is still kept after reset password", this.getClass());
            throw e;

        } finally {
            LoginFragment.getInstance(browser).login(user, NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

            testParams.setPassword(NEW_PASSWORD);
            UserManagementRestUtils.updateCurrentUserPassword(getRestApiClient(), NEW_PASSWORD, oldPassword);

            testParams.setPassword(oldPassword);
            logout()
                .login(user, oldPassword, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        }
    }
}
