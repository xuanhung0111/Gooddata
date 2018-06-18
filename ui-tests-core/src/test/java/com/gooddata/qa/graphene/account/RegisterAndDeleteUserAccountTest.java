package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.PASSWORD_HINT;
import static java.lang.String.format;

import java.io.IOException;
import javax.mail.MessagingException;

import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

public class RegisterAndDeleteUserAccountTest extends AbstractUITest {

    private static final By NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE = By.
            cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .title");
    private static final By CLOSE_DIALOG_BUTTON_LOCATOR = By.cssSelector(".s-btn-close");

    private static final String GOODDATA_PRODUCT_TOUR_PROJECT = "GoodData Product Tour";

    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "Password too short. "
            + "Minimum length is 7 characters.";
    
    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "You selected a commonly used password. "
            + "Choose something unique.";

    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";

    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String INVALID_PHONE_NUMBER = "12345678901234567890";

    private static final String EXISTED_EMAIL_ERROR_MESSAGE = "This email address is already in use.";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "This is not a valid email address.";
    private static final String FIELD_MISSING_ERROR_MESSAGE = "Field is required.";

    private static final String INVALID_PHONE_NUMBER_ERROR_MESSAGE = "This is not a valid phone number.";

    private static final String ACTIVATION_SUCCESS_MESSAGE = "Account Activated"
            + "\nYour account has been successfully activated!";

    // due to cl-10948, change message when re-click on activation account link
    private static final String ALREADY_ACTIVATED_MESSAGE = "This activation link is not valid."
            + "\nRegister again or log in to your account.";
    private static final By LOG_IN_YOUR_ACCOUNT_LINK = By.cssSelector(".login-message a");

    private static final String NOT_FULLY_ACTIVATED_MESSAGE = "Your account has not yet been fully activated. "
            + "Please click the activation link in the confirmation email sent to you.";

    private static final String NEED_ACTIVATE_ACCOUNT_MESSAGE = "Activate your account before inviting users";

    private String registrationUser;
    private String activationLink;

    private RegistrationForm registrationForm;

    @BeforeClass(alwaysRun = true)
    public void initData() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        registrationUser = generateEmail(imapUser);

        String registrationString = String.valueOf(System.currentTimeMillis());
        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(registrationUser)
                .withPassword(testParams.getPassword())
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government");
    }

    /**
     * Due to bug CL-9252, Walkme just appears one time and never display again.
     * So all test that depend on this test will continue and no need to check Walkme display or not
     * 
     * Notes: This test always run first base on alphabet order. 
     */
    @Test
    public void checkWalkme() throws ParseException, JSONException, IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        initRegistrationPage()
            .registerNewUserSuccessfully(registrationForm);

        waitForDashboardPageLoaded(browser);
        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

//        Comment due to CL-9704: Walkme does not appear in analyze-new page
//        initAnalysePage();
//        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        initProjectsAndUsersPage();
        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        openProject(GOODDATA_PRODUCT_TOUR_PROJECT);
        assertFalse(isWalkmeDisplayed(), "Walkme-dialog-displays-more-than-one-time-in-Product-Tour-project");
    }

    @Test
    public void selectLoginLink() {
        initRegistrationPage()
            .selectLoginLink()
            .registerNewAccount();
    }

    @Test
    public void registerUserWithInvalidPasswordValidation() throws ParseException, IOException, JSONException {
        final SoftAssert softAssert = new SoftAssert();

        RegistrationPage registrationPage = initRegistrationPage();
        softAssert.assertEquals(registrationPage.getPasswordHint(), PASSWORD_HINT);

        registrationPage
                .fillInRegistrationForm(registrationForm)
                .enterEmail(generateEmail(registrationUser))
                .enterPassword("aaaaaa")
                .agreeRegistrationLicense()
                .submitForm();
        takeScreenshot(browser, "Error-message-for-short-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                SHORT_PASSWORD_ERROR_MESSAGE);

        registrationPage
                .enterPassword("12345678")
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-commonly-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                COMMONLY_PASSWORD_ERROR_MESSAGE);

        registrationPage
                .enterPassword("aaaaaaaa")
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-sequential-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                SEQUENTIAL_PASSWORD_ERROR_MESSAGE);
        softAssert.assertAll();
    }

    @Test
    public void registerUserWithInvalidValue() throws ParseException, IOException, JSONException {
        initRegistrationPage()
            .fillInRegistrationForm(new RegistrationForm())
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
            .fillInRegistrationForm(registrationForm)
            .enterEmail(INVALID_EMAIL)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_EMAIL_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
            .fillInRegistrationForm(registrationForm)
            .enterPhoneNumber(INVALID_PHONE_NUMBER)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_PHONE_NUMBER_ERROR_MESSAGE);
    }

    @Test
    public void loginAsUnverifiedUserAfterRegistering()
            throws ParseException, JSONException, IOException, MessagingException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

        assertFalse(initProjectsAndUsersPage().isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is still displayed");

        ProjectAndUsersPage.getInstance(browser).clickInviteUserButton();
        assertFalse(isElementPresent(INVITE_USER_DIALOG_LOCATOR, browser));
        assertEquals(waitForElementVisible(NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE, browser).getText(),
                NEED_ACTIVATE_ACCOUNT_MESSAGE);
        takeScreenshot(browser, "Need activate account before inviting users", getClass());
        waitForElementVisible(CLOSE_DIALOG_BUTTON_LOCATOR, browser).click();

        UserProfilePage userProfilePage = ProjectAndUsersPage.getInstance(browser).openUserProfile(registrationUser);
        assertEquals(userProfilePage.getUserRole(), "", "Unverified admin should not show role");
        takeScreenshot(browser, "Unverified user has no role", this.getClass());

        logout()
            .login(registrationUser, testParams.getPassword(), false);
        assertEquals(getPageErrorMessage(), NOT_FULLY_ACTIVATED_MESSAGE);

        openUrl(activationLink);
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
        waitForDashboardPageLoaded(browser);

        assertTrue(initProjectsAndUsersPage().isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is not displayed");

        ProjectAndUsersPage.getInstance(browser).clickInviteUserButton();
        InviteUserDialog inviteUserDialog = Graphene.createPageFragment(InviteUserDialog.class,
              waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser));
        takeScreenshot(browser, "Active user can invite users", getClass());
        inviteUserDialog.cancelInvite();

        userProfilePage =  ProjectAndUsersPage.getInstance(browser).openUserProfile(registrationUser);
        assertEquals(userProfilePage.getUserRole(), UserRoles.ADMIN.getName());
    }

    @Test(groups = {"sanity"})
    public void registerNewUser() throws MessagingException, IOException, ParseException, JSONException {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment() 
                || testParams.isPerformanceEnvironment()) {
            log.warning("Register New User is not tested on PI or Production environment");
            return;
        }
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));

        waitForDashboardPageLoaded(browser);

        openUrl(activationLink);
        LoginFragment.waitForPageLoaded(browser);

        takeScreenshot(browser, "register user successfully", this.getClass());
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        try {
            LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
            waitForDashboardPageLoaded(browser);

            openProject(GOODDATA_PRODUCT_TOUR_PROJECT);
            assertTrue(dashboardsPage.isEditButtonPresent(), format("Dashboard cannot be edited in %s project", 
                    GOODDATA_PRODUCT_TOUR_PROJECT));
        } finally {
            logout();
        }
    }

    @Test(dependsOnMethods = {"registerNewUser"})
    public void openAtivationLinkAfterRegistration() {
        openUrl(activationLink);

        String messageInfoLoginPage = waitForElementVisible(By.cssSelector(".login-message"), browser).getText();
        assertEquals(messageInfoLoginPage, ALREADY_ACTIVATED_MESSAGE);

        waitForElementVisible(LOG_IN_YOUR_ACCOUNT_LINK, browser).click();

        LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
    }

    @Test(dependsOnMethods = {"registerNewUser"})
    public void registerUserWithEmailOfExistingAccount() {
        initRegistrationPage()
                .fillInRegistrationForm(new RegistrationForm())
                .enterEmail(generateEmail(registrationUser))
                .enterSpecialCaptcha()
                .agreeRegistrationLicense()
                .submitForm();

        takeScreenshot(browser, "Verification-on-un-registered-email-show-nothing-when-missing-other-fields", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Captcha input shows error but expected is not");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser).enterEmail(registrationUser).submitForm();

        takeScreenshot(browser, "Verification-on-registered-email-show-nothing-when-missing-other-fields", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Captcha input shows error but expected is not");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser).enterCaptcha("aaaaa").submitForm();

        takeScreenshot(browser, "Email-and-captcha-field-show-nothing-when-enter-wrong-captcha", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Error not show on captcha input");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
                .fillInRegistrationForm(registrationForm)
                .enterSpecialCaptcha()
                .submitForm();

        takeScreenshot(browser, "Error-message-displays-when-register-user-with-an-existed-email", getClass());
        assertTrue(RegistrationPage.getInstance(browser).isEmailInputError(), "Error not show on email input");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), EXISTED_EMAIL_ERROR_MESSAGE);
    }

    @Test(dependsOnMethods = "registerUserWithEmailOfExistingAccount")
    public void deleteUserAccount() throws JSONException {
        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

        initAccountPage()
            .tryDeleteAccountButDiscard();
        logout()
            .login(registrationUser, testParams.getPassword(), true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initAccountPage()
            .deleteAccount();

        LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), false);
        LoginFragment.getInstance(browser).checkInvalidLogin();
    }

    @Test(dependsOnMethods = "deleteUserAccount", description = "WA-6433: 500 Internal Error when deleting user twice")
    public void deleteUserWithoutActivationTwice() {
        initRegistrationPage().registerNewUserSuccessfully(registrationForm);
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));
        initAccountPage().deleteAccount();

        LoginFragment loginPage = LoginFragment.getInstance(browser);
        loginPage.login(registrationUser, testParams.getPassword(), false);
        loginPage.checkInvalidLogin();

        initRegistrationPage().registerNewUserSuccessfully(registrationForm);

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));
        initAccountPage().deleteAccount();

        CheckUtils.checkRedBar(browser);
        takeScreenshot(browser, "deleteUserWithoutActivationTwice", getClass());
        loginPage.login(registrationUser, testParams.getPassword(), false);
        loginPage.checkInvalidLogin();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment() 
                || testParams.isPerformanceEnvironment()) return;
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);
    }

    private void openProject(String projectName) {
        String projectId = getProjectId(projectName);
        ProjectsPage.getInstance(browser).goToProject(projectId);
        waitForDashboardPageLoaded(browser);
    }

    private String getPageErrorMessage() {
        return waitForElementVisible(By.xpath("//*[@class='login-message is-error']/p[1]"), browser).getText();
    }

    private String getProjectId(String name) {
        return initProjectsPage().getProjectItem(name).getId();
    }

    private boolean isWalkmeDisplayed() {
        final int walkmeLoadTimeoutSeconds = 30;
        final By walkmeCloseLocator = By.cssSelector(".walkme-action-close, .walkme-action-cancel");

        try {
            waitForElementVisible(walkmeCloseLocator, browser, walkmeLoadTimeoutSeconds);
            return true;

        } catch (TimeoutException e) {
            takeScreenshot(browser, "Walkme-dialog-is-not-appeared", getClass());
            log.info("Walkme dialog is not appeared!");

            return false;
        }
    }
}
