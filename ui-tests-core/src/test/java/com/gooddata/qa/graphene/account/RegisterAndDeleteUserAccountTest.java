package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Objects;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

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
                .withIndustry("Tech");

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
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);

        initRegistrationPage();
        registrationPage.registerNewUser(registrationForm);
        waitForFragmentNotVisible(registrationPage);

        waitForDashboardPageLoaded(browser);
        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

        initAnalysePageByUrl();
        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        initProjectsAndUsersPage();
        assertTrue(isWalkmeDisplayed(), "Walkme-dialog-is-not-visible");

        openProject(DEMO_PROJECT);
        assertFalse(isWalkmeDisplayed(), "Walkme-dialog-is-visible-in-project-that-is-not-Product-Tour");

        openProject(GOODDATA_PRODUCT_TOUR_PROJECT);
        assertFalse(isWalkmeDisplayed(), "Walkme-dialog-displays-more-than-one-time-in-Product-Tour-project");
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void selectLoginLink() {
        initRegistrationPage();

        registrationPage.selectLoginLink();
        waitForElementVisible(loginFragment.getRoot());

        loginFragment.openRegistrationPage();
        waitForElementVisible(registrationPage.getRoot());
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void registerUserWithInvalidValue() {
        initRegistrationPage();

        registrationPage.fillInRegistrationForm(new RegistrationForm())
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterEmail(INVALID_EMAIL)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_EMAIL_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterPassword(INVALID_PASSWORD)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_PASSWORD_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterPhoneNumber(INVALID_PHONE_NUMBER)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_PHONE_NUMBER_ERROR_MESSAGE);
    }

    @Test(groups = PROJECT_INIT_GROUP)
    public void loginAsUnverifiedUserAfterRegistering()
            throws ParseException, JSONException, IOException, MessagingException {
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);

        initRegistrationPage();

        activationLink = doActionWithImapClient(
                imapClient -> registrationPage.registerNewUser(imapClient, registrationForm));
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

        logout();
        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        assertEquals(getPageErrorMessage(), NOT_FULLY_ACTIVATED_MESSAGE);

        openUrl(activationLink);
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
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
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);

        initRegistrationPage();

        activationLink = doActionWithImapClient(
                imapClient -> registrationPage.registerNewUser(imapClient, registrationForm));
        waitForDashboardPageLoaded(browser);

        openUrl(activationLink);
        waitForElementVisible(loginFragment.getRoot());

        takeScreenshot(browser, "register user successfully", this.getClass());
        assertEquals(loginFragment.getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        try {
            loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
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
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), ALREADY_ACTIVATED_MESSAGE);

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
    }

    @Test(groups = PROJECT_INIT_GROUP, dependsOnMethods = {"registerNewUser"})
    public void registerUserWithEmailOfExistingAccount() {
        initRegistrationPage();

        registrationPage.registerNewUser(registrationForm);
        assertEquals(registrationPage.getErrorMessage(), EXISTED_EMAIL_ERROR_MESSAGE);
    }

    @Test(dependsOnMethods = "registerUserWithEmailOfExistingAccount")
    public void deleteUserAccount() throws JSONException {
        testParams.setProjectId(getProjectId(DEMO_PROJECT));

        initAccountPage();

        accountPage.tryDeleteAccountButDiscard();
        logout();
        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initAccountPage();
        accountPage.deleteAccount();
        waitForElementVisible(loginFragment.getRoot());

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        loginFragment.checkInvalidLogin();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);
    }

    private void openProject(String projectName) {
        projectsPage.goToProject(getProjectId(projectName));
        waitForDashboardPageLoaded(browser);
    }

    private void deleteUserIfExist(RestApiClient restApiClient, String userEmail)
            throws ParseException, JSONException, IOException {
        JSONObject userProfile = UserManagementRestUtils.getUserProfileByEmail(restApiClient, userEmail);
        if (Objects.nonNull(userProfile)) {
            String userProfileUri = userProfile.getJSONObject("links").getString("self");
            UserManagementRestUtils.deleteUserByUri(restApiClient, userProfileUri);
        }
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
