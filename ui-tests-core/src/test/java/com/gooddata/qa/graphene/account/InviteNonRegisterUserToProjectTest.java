package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.profile.UserProfilePage.USER_PROFILE_PAGE_LOCATOR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

public class InviteNonRegisterUserToProjectTest extends AbstractProjectTest {

    private static final String INVITATION_USER = "gd.accregister@gmail.com";
    private static final String INVITATION_USER_PASSWORD = "changeit";

    private static final By INVITATION_PAGE_LOCATOR = By.cssSelector(".s-invitationPage");

    private static final String USER_PROFILE_PAGE_URL = PAGE_UI_PROJECT_PREFIX + "%s|profilePage|%s";

    private static final String INVITATION_SUCCESS_MESSAGE = "Invitation was successfully sent.";

    private static final String JOINED_PROJECT_SUCCESS_MESSAGE = "Congratulations!"
            + "\nYou have successfully joined the project. Please log in below.";

    private RegistrationForm registrationForm;
    private ImapClient imapClient;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "Invite-non-register-user-to-project-test";
    }

    @Test(dependsOnMethods = "createProject")
    public void initData() {
        String registrationString = String.valueOf(System.currentTimeMillis());

        imapHost = testParams.loadProperty("imap.host");
        imapUser = INVITATION_USER;
        imapPassword = INVITATION_USER_PASSWORD;

        imapClient = new ImapClient(imapHost, imapUser, imapPassword);

        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(INVITATION_USER)
                .withPassword(INVITATION_USER_PASSWORD)
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government");
    }

    @Test(dependsOnMethods = "initData")
    public void confirmInvitationRegistrationForm() throws MessagingException, IOException, JSONException {
        deleteUserIfExist(getRestApiClient(), INVITATION_USER);

        initProjectsAndUsersPage();
        String invitationLink = projectAndUsersPage.inviteUsersWithBlankMessage(imapClient,
                projectTitle + " Invitation", UserRoles.EDITOR, INVITATION_USER);
        checkGreenBar(browser, INVITATION_SUCCESS_MESSAGE);

        logout();
        openUrl(invitationLink);
        RegistrationPage invitationPage = Graphene.createPageFragment(RegistrationPage.class,
                waitForElementVisible(INVITATION_PAGE_LOCATOR, browser));
        assertFalse(invitationPage.isEmailFieldEditable(), "Email is editable");
        assertFalse(invitationPage.isCaptchaFieldPresent(), "Captcha field is present");

        invitationPage.registerNewUser(registrationForm);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));
    }

    @Test(dependsOnMethods = "initData")
    public void inviteUnverifiedUserToProject()
            throws ParseException, JSONException, IOException, MessagingException {
        deleteUserIfExist(getRestApiClient(), INVITATION_USER);

        initRegistrationPage();
        registrationPage.registerNewUser(registrationForm);
        waitForFragmentNotVisible(registrationPage);

        waitForWalkmeAndTurnOff();
        assertEquals(waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).getText(),
                registrationForm.getFirstName() + " " + registrationForm.getLastName());

        logout();
        signIn(false, UserRoles.ADMIN);

        initProjectsAndUsersPage();
        String invitationLink = projectAndUsersPage.inviteUsersWithBlankMessage(imapClient,
                projectTitle + " Invitation", UserRoles.EDITOR, INVITATION_USER);
        checkGreenBar(browser, INVITATION_SUCCESS_MESSAGE);

        logout();
        openUrl(invitationLink);
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), JOINED_PROJECT_SUCCESS_MESSAGE);

        loginFragment.login(INVITATION_USER, INVITATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        RestApiClient restApiClientInvitedUser = getRestApiClient(INVITATION_USER, INVITATION_USER_PASSWORD);
        UserProfilePage userProfilePage = openUserProfileInProject(restApiClientInvitedUser,
                testParams.getProjectId());
        assertEquals(userProfilePage.getUserRole(), UserRoles.EDITOR.getName());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        deleteUserIfExist(getRestApiClient(), INVITATION_USER);
    }

    private UserProfilePage openUserProfileInProject(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        String userProfileUri = UserManagementRestUtils.getCurrentUserProfile(restApiClient)
                .getJSONObject("links")
                .getString("self");
        openUrl(format(USER_PROFILE_PAGE_URL, projectId, userProfileUri));
        return Graphene.createPageFragment(UserProfilePage.class,
                waitForElementVisible(USER_PROFILE_PAGE_LOCATOR, browser));
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
