package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.PASSWORD_HINT;

import java.io.IOException;
import java.util.Objects;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.google.common.base.Predicate;

public class InviteNonRegisterUserToProjectTest extends AbstractProjectTest {

    private static final By INVITATION_PAGE_LOCATOR = By.cssSelector(".s-invitationPage");

    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "Password too short. "
            + "Minimum length is 7 characters.";
    
    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "You selected a commonly used password. "
            + "Choose something unique.";

    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";

    private static final String USER_PROFILE_PAGE_URL = PAGE_UI_PROJECT_PREFIX + "%s|profilePage|%s";

    private static final String INVITATION_SUCCESS_MESSAGE = "Invitation was successfully sent.";

    private static final String JOINED_PROJECT_SUCCESS_MESSAGE = "Congratulations!"
            + "\nYou have successfully joined the project. Please log in below.";

    private RegistrationForm registrationForm;
    private String invitationUser;

    @Override
    protected void initProperties() {
        // use empty project
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        invitationUser = generateEmail(imapUser);

        String registrationString = String.valueOf(System.currentTimeMillis());

        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(invitationUser)
                .withPassword(testParams.getPassword())
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void confirmInvitationRegistrationForm() throws MessagingException, IOException, JSONException {
        deleteUserIfExist(testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient(), invitationUser);

        String invitationLink = doActionWithImapClient(imapClient ->
                initProjectsAndUsersPage().inviteUsersWithBlankMessage(imapClient, projectTitle + " Invitation",
                        UserRoles.EDITOR, invitationUser));
        checkGreenBar(browser, INVITATION_SUCCESS_MESSAGE);

        logout();
        openUrl(invitationLink);
        RegistrationPage invitationPage = Graphene.createPageFragment(RegistrationPage.class,
                waitForElementVisible(INVITATION_PAGE_LOCATOR, browser));
        assertFalse(invitationPage.isEmailFieldEditable(), "Email is editable");
        assertFalse(invitationPage.isCaptchaFieldPresent(), "Captcha field is present");

        final SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(invitationPage.getPasswordHint(), PASSWORD_HINT);

        invitationPage
                .fillInRegistrationForm(registrationForm)
                .enterPassword("aaaaaa")
                .agreeRegistrationLicense()
                .submitForm();
        takeScreenshot(browser, "Error-message-for-short-password-shows", getClass());
        softAssert.assertEquals(invitationPage.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        invitationPage
                .enterPassword("12345678")
                .submitForm();
        takeScreenshot(browser, "Error-message-for-commonly-password-shows", getClass());
        softAssert.assertEquals(invitationPage.getErrorMessage(), COMMONLY_PASSWORD_ERROR_MESSAGE);

        invitationPage
                .enterPassword("aaaaaaaa")
                .submitForm();
        takeScreenshot(browser, "Error-message-for-sequential-password-shows", getClass());
        softAssert.assertEquals(invitationPage.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);
        softAssert.assertAll();

        invitationPage
                .enterPassword(testParams.getPassword())
                .submitForm();
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void inviteUnverifiedUserToProject()
            throws ParseException, JSONException, IOException, MessagingException {
        deleteUserIfExist(testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient(), invitationUser);

        initRegistrationPage()
            .registerNewUserSuccessfully(registrationForm);

        waitForWalkmeAndTurnOff();
        assertEquals(waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).getText(),
                registrationForm.getFirstName() + " " + registrationForm.getLastName());

        logout();
        signIn(false, UserRoles.ADMIN);

        String invitationLink = doActionWithImapClient(imapClient ->
                initProjectsAndUsersPage().inviteUsersWithBlankMessage(imapClient, projectTitle + " Invitation",
                        UserRoles.EDITOR, invitationUser));
        checkGreenBar(browser, INVITATION_SUCCESS_MESSAGE);

        logout();
        openUrl(invitationLink);
        assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), JOINED_PROJECT_SUCCESS_MESSAGE);

        LoginFragment.getInstance(browser).login(invitationUser, testParams.getPassword(), true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        RestApiClient restApiClientInvitedUser = getRestApiClient(invitationUser, testParams.getPassword());
        UserProfilePage userProfilePage = openUserProfileInProject(restApiClientInvitedUser,
                testParams.getProjectId());
        assertEquals(userProfilePage.getUserRole(), UserRoles.EDITOR.getName());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        deleteUserIfExist(testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient(), invitationUser);
    }

    private UserProfilePage openUserProfileInProject(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        String userProfileUri = UserManagementRestUtils.getCurrentUserProfile(restApiClient)
                .getJSONObject("links")
                .getString("self");
        openUrl(format(USER_PROFILE_PAGE_URL, projectId, userProfileUri));
        return UserProfilePage.getInstance(browser);
    }

    private void deleteUserIfExist(RestApiClient restApiClient, String userEmail)
            throws ParseException, JSONException, IOException {
        JSONObject userProfile = UserManagementRestUtils.getUserProfileByEmail(restApiClient, testParams.getUserDomain(),
                userEmail);
        if (Objects.nonNull(userProfile)) {
            String userProfileUri = userProfile.getJSONObject("links").getString("self");
            UserManagementRestUtils.deleteUserByUri(restApiClient, userProfileUri);
        }
    }

    private void waitForWalkmeAndTurnOff() {
        final int walkmeLoadTimeoutSeconds = 30;

        final By dashboardPageLocator = By.cssSelector("#p-projectDashboardPage.s-displayed");
        final By walkmeCloseLocator = By.className("walkme-action-close");

        Predicate<WebDriver> dashboardOrWalkmeAppear = browser ->
                isElementPresent(dashboardPageLocator, browser) || isElementPresent(walkmeCloseLocator, browser);

        Graphene.waitGui().until(dashboardOrWalkmeAppear);

        try {
            WebElement walkmeCloseElement = 
                    waitForElementVisible(walkmeCloseLocator, browser, walkmeLoadTimeoutSeconds);

            walkmeCloseElement.click();
            waitForElementNotPresent(walkmeCloseElement);

        } catch (TimeoutException e) {
            takeScreenshot(browser, "Walkme dialog is not appeared", getClass());
            log.info("Walkme dialog is not appeared!");
        }
    }
}
