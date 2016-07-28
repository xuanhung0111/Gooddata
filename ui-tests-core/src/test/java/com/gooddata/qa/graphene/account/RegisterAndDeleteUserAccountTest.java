package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

public class RegisterAndDeleteUserAccountTest extends AbstractUITest {

    private static final By NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE = By.
            cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .title");
    private static final By CLOSE_DIALOG_BUTTON_LOCATOR = By.cssSelector(".s-btn-close");

    private static final String DEMO_PROJECT = "GoodSales";
    private static final String GOODDATA_PRODUCT_TOUR_PROJECT = "GoodData Product Tour";

    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String INVALID_PASSWORD = "aaaaaa";
    private static final String INVALID_PHONE_NUMBER = "12345678901234567890";

    private static final String EXISTED_EMAIL_ERROR_MESSAGE = "This email address is already in use.";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "This is not a valid email address.";
    private static final String FIELD_MISSING_ERROR_MESSAGE = "Field is required.";

    private static final String INVALID_PASSWORD_ERROR_MESSAGE = "Password too short."
            + " Minimum length is 7 characters.";

    private static final String INVALID_PHONE_NUMBER_ERROR_MESSAGE = "This is not a valid phone number.";

    private static final String ACTIVATION_SUCCESS_MESSAGE = "Account Activated"
            + "\nYour account has been successfully activated!";

    private static final String ALREADY_ACTIVATED_MESSAGE = "Already activated"
            + "\nThis registration has already been activated. Please log in below.";

    private static final String NOT_FULLY_ACTIVATED_MESSAGE = "Your account has not yet been fully activated. "
            + "Please click the activation link in the confirmation email sent to you.";

    private static final String REGISTRATION_USER = "gd.accregister@gmail.com";
    private static final String REGISTRATION_USER_PASSWORD = "changeit";

    private static final String NEED_ACTIVATE_ACCOUNT_MESSAGE = "Activate your account before inviting users";

    private String activationLink;

    private RegistrationForm registrationForm;

    @BeforeClass(alwaysRun = true)
    public void initData() {
        String registrationString = String.valueOf(System.currentTimeMillis());
        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(REGISTRATION_USER)
                .withPassword(REGISTRATION_USER_PASSWORD)
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government");

        imapHost = testParams.loadProperty("imap.host");
        imapUser = REGISTRATION_USER;
        imapPassword = REGISTRATION_USER_PASSWORD;
    }

    /**
     * Due to bug CL-9252, Walkme just appears one time and never display again.
     * So all test that depend on this test will continue and no need to check Walkme display or not
     * 
     * Notes: This test always run first base on alphabet order. 
     */
    @Test(groups = PROJECT_INIT_GROUP)
    public void checkWalkme() throws ParseException, JSONException, IOException {
        deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), REGISTRATION_USER);

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

        openProject(DEMO_PROJECT);
        assertFalse(isWalkmeDisplayed(), "Walkme-dialog-is-visible-in-project-that-is-not-Product-Tour");

        openProject(GOODDATA_PRODUCT_TOUR_PROJECT);
        assertFalse(isWalkmeDisplayed(), "Walkme-dialog-displays-more-than-one-time-in-Product-Tour-project");
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void selectLoginLink() {
        initRegistrationPage()
            .selectLoginLink()
            .openRegistrationPage();
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void registerUserWithInvalidValue() {
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
            .enterPassword(INVALID_PASSWORD)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_PASSWORD_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
            .fillInRegistrationForm(registrationForm)
            .enterPhoneNumber(INVALID_PHONE_NUMBER)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_PHONE_NUMBER_ERROR_MESSAGE);
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void loginAsUnverifiedUserAfterRegistering()
            throws ParseException, JSONException, IOException, MessagingException {
        deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), REGISTRATION_USER);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

        initProjectsAndUsersPage();
        assertFalse(projectAndUsersPage.isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is still displayed");

        projectAndUsersPage.clickInviteUserButton();
        assertFalse(isElementPresent(INVITE_USER_DIALOG_LOCATOR, browser));
        assertEquals(waitForElementVisible(NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE, browser).getText(),
                NEED_ACTIVATE_ACCOUNT_MESSAGE);
        takeScreenshot(browser, "Need activate account before inviting users", getClass());
        waitForElementVisible(CLOSE_DIALOG_BUTTON_LOCATOR, browser).click();

        UserProfilePage userProfilePage = projectAndUsersPage.openUserProfile(REGISTRATION_USER);
        assertEquals(userProfilePage.getUserRole(), "", "Unverified admin should not show role");
        takeScreenshot(browser, "Unverified user has no role", this.getClass());

        logout()
            .login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        assertEquals(getPageErrorMessage(), NOT_FULLY_ACTIVATED_MESSAGE);

        openUrl(activationLink);
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        LoginFragment.getInstance(browser).login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForDashboardPageLoaded(browser);

        initProjectsAndUsersPage();
        assertTrue(projectAndUsersPage.isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is not displayed");

        projectAndUsersPage.clickInviteUserButton();
        InviteUserDialog inviteUserDialog = Graphene.createPageFragment(InviteUserDialog.class,
              waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser));
        takeScreenshot(browser, "Active user can invite users", getClass());
        inviteUserDialog.cancelInvite();

        userProfilePage =  projectAndUsersPage.openUserProfile(REGISTRATION_USER);
        assertEquals(userProfilePage.getUserRole(), UserRoles.ADMIN.getName());
    }

    @Test(groups = {PROJECT_INIT_GROUP, "sanity"})
    public void registerNewUser() throws MessagingException, IOException, ParseException, JSONException {
        if (!testParams.isClusterEnvironment() || testParams.isProductionEnvironment() ||
                testParams.isPerformanceEnvironment()) {
            log.warning("Register New User is not tested on PI or Production environment");
            return;
        }
        deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), REGISTRATION_USER);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));

        waitForDashboardPageLoaded(browser);

        openUrl(activationLink);
        LoginFragment.waitForPageLoaded(browser);

        takeScreenshot(browser, "register user successfully", this.getClass());
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        try {
            LoginFragment.getInstance(browser).login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
            waitForDashboardPageLoaded(browser);

            openProject(DEMO_PROJECT);
            assertFalse(dashboardsPage.isEditButtonPresent(), "Dashboard can be edited in Goodsales Demo project");
        } finally {
            logout();
        }
    }

    @Test(groups = PROJECT_INIT_GROUP, dependsOnMethods = {"registerNewUser"})
    public void openAtivationLinkAfterRegistration() {
        openUrl(activationLink);
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ALREADY_ACTIVATED_MESSAGE);

        LoginFragment.getInstance(browser).login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
    }

    @Test(groups = PROJECT_INIT_GROUP, dependsOnMethods = {"registerNewUser"})
    public void registerUserWithEmailOfExistingAccount() {
        initRegistrationPage()
                .fillInRegistrationForm(new RegistrationForm())
                .enterEmail(generateEmail(REGISTRATION_USER))
                .enterSpecialCaptcha()
                .agreeRegistrationLicense()
                .submitForm();

        takeScreenshot(browser, "Verification-on-un-registered-email-show-nothing-when-missing-other-fields", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Captcha input shows error but expected is not");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser).enterEmail(REGISTRATION_USER).submitForm();

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
        testParams.setProjectId(getProjectId(DEMO_PROJECT));

        initAccountPage();

        accountPage.tryDeleteAccountButDiscard();
        logout()
            .login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initAccountPage();
        accountPage.deleteAccount();

        LoginFragment.getInstance(browser).login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        LoginFragment.getInstance(browser).checkInvalidLogin();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        if (!testParams.isClusterEnvironment() || testParams.isProductionEnvironment() ||
                testParams.isPerformanceEnvironment()) return;
        deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), REGISTRATION_USER);
    }

    private void openProject(String projectName) {
        projectsPage.goToProject(getProjectId(projectName));
        waitForDashboardPageLoaded(browser);
    }

    private String getPageErrorMessage() {
        return waitForElementVisible(By.xpath("//*[@class='login-message is-error']/p[1]"), browser).getText();
    }

    private String getProjectId(String name) {
        initProjectsPage();
        return waitForFragmentVisible(projectsPage).getProjectsIds(name).get(0);
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
