package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static com.gooddata.qa.utils.mail.ImapUtils.getLastEmail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.entity.mail.Email;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class InviteUserTest extends AbstractProjectTest {

    private static final String INVITATION_MESSAGE = "We invite you to our project";

    private static final By INVITATION_PAGE_LOCATOR = By.cssSelector(".s-invitationPage");

    private String invitationSubject;
    private RegistrationForm registrationForm;

    //  Use this variable to avoid connecting to inbox many times 
    private int expectedMessageCount;

    @BeforeClass
    public void initData() throws IOException, MessagingException {
        final String uniqueString = String.valueOf(System.currentTimeMillis());
        projectTitle += uniqueString;

        imapHost = testParams.loadProperty("imap.host");
        imapPassword = testParams.loadProperty("imap.password");
        imapUser = testParams.loadProperty("imap.user");

        invitationSubject = projectTitle + "-" + testParams.getProjectDriver().name() + " Invitation";

        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + uniqueString)
                .withLastName("LastName " + uniqueString)
                .withEmail(imapUser)
                .withPassword(imapPassword)
                .withPhone(uniqueString)
                .withCompany("Company " + uniqueString)
                .withJobTitle("Title " + uniqueString)
                .withIndustry("Government");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void getExpectedMessage() {
        expectedMessageCount = doActionWithImapClient(imapClient ->
                imapClient.getMessagesCount(GDEmails.INVITATION, invitationSubject));
    }

    @Test(dependsOnMethods = {"getExpectedMessage"})
    public void inviteRegistedUser() throws Throwable {
        loginAndOpenProjectAndUserPage();

        final String invitationLink = inviteUsers(invitationSubject, UserRoles.ADMIN,
                INVITATION_MESSAGE, imapUser);

        projectAndUsersPage.openInvitedUserTab();
        checkResendAndCancelInvitationAvailable(imapUser);

        final Email lastEmail = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount + 1));

        assertEquals(lastEmail.getFrom(), GDEmails.INVITATION.getEmailAddress());
        assertEquals(lastEmail.getSubject(), invitationSubject);

        assertTrue(lastEmail.getBody().contains(INVITATION_MESSAGE),
                "The invitation email does not contain expected message");
        assertTrue(lastEmail.getBody().contains(invitationLink),
                "The invitation email does not contain invitation link");

        logout();
        openUrl(invitationLink);
        signInAtUI(imapUser, imapPassword);
        waitForDashboardPageLoaded(browser);
        assertTrue(browser.getCurrentUrl().contains(testParams.getProjectId()), imapUser + " can't log on to the project");
        takeScreenshot(browser, "registed-user-access-the-project-successfully", getClass());

        initProjectsAndUsersPage();
        assertTrue(projectAndUsersPage.isUserDisplayedInList(imapUser), imapUser + " has not been active");
        assertEquals(projectAndUsersPage.getUserRole(imapUser), UserRoles.ADMIN.getName());

        ++expectedMessageCount;
    }

    @Test(dependsOnMethods = {"inviteRegistedUser"})
    public void inviteNonRegistedUser() throws Throwable {
        final String nonRegistedUser = generateEmail(imapUser);

        loginAndOpenProjectAndUserPage();
        final String invitationLink = inviteUsers(invitationSubject, UserRoles.EDITOR,
                INVITATION_MESSAGE, nonRegistedUser);

        logout();
        openUrl(invitationLink);

        try {
            final RegistrationPage invitationPage = Graphene.createPageFragment(RegistrationPage.class,
                    waitForElementVisible(INVITATION_PAGE_LOCATOR, browser));
            assertFalse(invitationPage.isCaptchaFieldPresent(), "The security code is displayed");

            invitationPage.registerNewUser(registrationForm);
            waitForDashboardPageLoaded(browser);
            assertTrue(browser.getCurrentUrl().contains(testParams.getProjectId()),
                    nonRegistedUser + " has not been created or invited sucessfully");
            takeScreenshot(browser, "non-registed-user-access-the-project-successfully", getClass());

            ++expectedMessageCount;
        } finally {
            UserManagementRestUtils.deleteUserByEmail(getRestApiClient(), nonRegistedUser);
        }
    }

    @Test(dependsOnMethods = {"inviteNonRegistedUser"})
    public void resendInvitation() throws JSONException {
        final String nonRegistedUser = generateEmail(imapUser);

        loginAndOpenProjectAndUserPage();

        projectAndUsersPage.openInviteUserDialog().inviteUsers(UserRoles.EDITOR, INVITATION_MESSAGE, nonRegistedUser);

        ++expectedMessageCount;

        projectAndUsersPage.resendInvitation(nonRegistedUser);

        final Collection<Message> invitations = doActionWithImapClient(imapClient ->waitForMessages(
                imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount + 1));
        assertTrue(invitations.size() == expectedMessageCount + 1, "The resend invitation has not been sent");

        ++expectedMessageCount;
    }

    @Test(dependsOnMethods = {"resendInvitation"})
    public void inviteAlreadyPresentUser() throws MessagingException, IOException, JSONException {
        initProjectsAndUsersPage();

        projectAndUsersPage.openInviteUserDialog().inviteUsers(UserRoles.EDITOR, INVITATION_MESSAGE, imapUser);

        assertEquals(Graphene.createPageFragment(InviteUserDialog.class,
                waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser))
                .getErrorMessage(), imapUser + " is already a member of this project");
    }

    @Test(dependsOnMethods = {"inviteAlreadyPresentUser"}) 
    public void inviteManyUsers() {
        final String nonRegistedUserA = generateEmail(imapUser);
        final String nonRegistedUserB = generateEmail(imapUser);

        initProjectsAndUsersPage();
        projectAndUsersPage.openInviteUserDialog().inviteUsers(UserRoles.EDITOR,
                INVITATION_MESSAGE, nonRegistedUserA, nonRegistedUserB);

        final Collection<Message> invitations = doActionWithImapClient(imapClient -> waitForMessages(
                imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount + 2));
        assertTrue(invitations.size() == expectedMessageCount + 2, "There are less than 2 new invitations in inbox");

        projectAndUsersPage.openInvitedUserTab();
        checkResendAndCancelInvitationAvailable(nonRegistedUserA);
        checkResendAndCancelInvitationAvailable(nonRegistedUserB);

        expectedMessageCount = expectedMessageCount + 2; 
    }

    private String inviteUsers(String emailSubject, UserRoles role, String message, String... emails) {
        return doActionWithImapClient((imapClient) ->
            projectAndUsersPage.inviteUsers(imapClient, invitationSubject, UserRoles.ADMIN, INVITATION_MESSAGE, emails));
    }

    private void loginAndOpenProjectAndUserPage() throws JSONException {
        logoutAndLoginAs(true, UserRoles.ADMIN);
        initProjectsAndUsersPage();
    }

    private void checkResendAndCancelInvitationAvailable(String userEmail) {
        assertTrue(projectAndUsersPage.isResendInvitationAvailable(userEmail) && projectAndUsersPage
                .isCancelInvitationAvailable(userEmail),"Resend and Cancel invitation are not displayed");
    }

}
