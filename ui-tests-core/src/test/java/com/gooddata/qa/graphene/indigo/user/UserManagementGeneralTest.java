package com.gooddata.qa.graphene.indigo.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.UserStates;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

import static java.util.Arrays.asList;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;

public class UserManagementGeneralTest extends AbstractProjectTest {

    private boolean canAccessUserManagementByDefault;
    private String group1;
    private String group2;
    private String group3;
    private String adminUser;
    private String editorUser;
    private String viewerUser;
    private List<String> group1Group2List;

    private static final String FEATURE_FLAG = ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT.getFlagName();
    private static final String CHANGE_GROUP_SUCCESSFUL_MESSAGE = "Group membership successfully changed.";
    private static final String DEACTIVATE_SUCCESSFUL_MESSAGE = "The selected users have been deactivated.";
    private static final String ACTIVATE_SUCCESSFUL_MESSAGE = "The selected users have been activated.";
    private static final String CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE =
            "All users except for yours have been deactivated. You cannot deactivate yourself.";
    private static final String CHANGE_ROLE_SUCCESSFUL_MESSAGE =
            "The role of selected users has been changed to %s.";
    private static final String CHANGE_ROLE_FAILED_MESSAGE = "You cannot change your role to %s.";
    private static final String INVITE_USER_SUCCESSFUL_MESSAGE = "Users successfully invited.";
    private static final String INVALID_EMAIL_MESSAGE = "\"%s\" is not a valid email address.";
    private static final String EXSITING_USER_MESSAGE = "User %s is already in this project.";

    private static final String INVITATION_FROM_EMAIL = "invitation@gooddata.com";
    private static final String INVITED_EMAIL = "abc@mail.com";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-management-general" + System.currentTimeMillis();
    }

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"initialize"})
    public void initData() throws JSONException, IOException {
        group1 = "Group1";
        group2 = "Group2";
        group3 = "Group3";
        group1Group2List = asList(group1, group2);
        adminUser = testParams.getUser();
        editorUser = testParams.getEditorUser();
        viewerUser = testParams.getViewerUser();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        enableUserManagementFeature();
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"initialize"})
    public void addUserGroups() throws JSONException, IOException {
        createUserGroups(group1, group2, group3);

        // Go to Dashboard page of new created project to use User management page of that project
        initProjectsPage();
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.addUsersToGroup(group1 , adminUser);
        userManagementPage.addUsersToGroup(group2 , editorUser);
        userManagementPage.addUsersToGroup(group3 , viewerUser);
    }

    @Test(dependsOnGroups = {"initialize"}, groups = {"userManagement"}) 
    public void accessToUserManagementFromHeader() {
        initDashboardsPage();

        assertTrue(ApplicationHeaderBar.isSearchIconPresent(browser));
        assertTrue(ApplicationHeaderBar.getSearchLinkText(browser).isEmpty());
        ApplicationHeaderBar.goToSearchPage(browser);

        ApplicationHeaderBar.goToUserManagementPage(browser);
        waitForFragmentVisible(userManagementPage);
    }

    @Test(dependsOnGroups = {"initialize"}, groups = {"userManagement"})
    public void verifyUserManagementUI() throws IOException, JSONException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().cancelInvitation();
        assertEquals(userManagementPage.getUsersCount(), 3);
        userManagementPage.selectUsers(adminUser, editorUser, viewerUser);
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"})
    public void adminChangeGroupsMemberOf(){
        initDashboardsPage();
        initUserManagementPage();
        List<String> userEmailsOfProject = asList(adminUser, editorUser, viewerUser);

        for (String group : group1Group2List) {
            userManagementPage.addUsersToGroup(group, adminUser, editorUser, viewerUser);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        for (String group : group1Group2List) {
            userManagementPage.openSpecificGroupPage(group);
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), userEmailsOfProject));
        }
    }

    @Test(dependsOnMethods = {"adminChangeGroupsMemberOf"}, groups = {"userManagement"})
    public void adminChangeGroupsShared(){
        initDashboardsPage();
        initUserManagementPage();

        for (String group : group1Group2List) {
            userManagementPage.removeUsersFromGroup(group, adminUser);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

         initUngroupedUsersPage();
         assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));

        initUserManagementPage();
        userManagementPage.addUsersToGroup(group3, adminUser);

        userManagementPage.openSpecificGroupPage(group3);
        assertTrue(userManagementPage.getAllUserEmails().containsAll(asList(adminUser, viewerUser)));
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"})
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException{
        String groupName = "New Group";
        String userGroupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        initUserManagementPage();
        int userGroupsCount = userManagementPage.getUserGroupsCount();

        RestUtils.deleteUserGroup(restApiClient, userGroupUri);
        refreshPage();
        assertEquals(userManagementPage.getUserGroupsCount(), userGroupsCount-1);
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"}) 
    public void changeRoleOfUsers() {
        try {
            initDashboardsPage();
            initUserManagementPage();
            String adminText = UserRoles.ADMIN.getName();

            final String message = String.format(CHANGE_ROLE_SUCCESSFUL_MESSAGE, adminText);
            userManagementPage.changeRoleOfUsers(UserRoles.ADMIN, editorUser, viewerUser);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return message.equals(userManagementPage.getMessageText());
                }
            });

            for (String email : asList(editorUser, viewerUser)) {
                assertEquals(userManagementPage.getUserRole(email), adminText);
            }
        }
        finally {
            userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, editorUser);
            userManagementPage.changeRoleOfUsers(UserRoles.VIEWER, viewerUser);
        }
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"}) 
    public void checkUserCannotChangeRoleOfHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, adminUser);
        assertEquals(userManagementPage.getMessageText(),
                String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName()));

        refreshPage();
        assertEquals(userManagementPage.getUserRole(adminUser), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = {"inviteUserToProject"}, groups = {"userManagement"}, alwaysRun = true) 
    public void checkUserCannotChangeRoleOfPendingUser(){
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN,
                "Invite new admin user", INVITED_EMAIL);

        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        userManagementPage.selectUsers(INVITED_EMAIL);
        assertFalse(userManagementPage.isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"}) 
    public void inviteUserToProject() throws IOException, MessagingException{
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN,
                "Invite new admin user", imapUser);

        assertEquals(userManagementPage.getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);
        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        initUserManagementPage();
        userManagementPage.filterUserState(UserStates.ACTIVE);
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));
    }

    @Test(dependsOnMethods = {"verifyUserManagementUI"}, groups = {"userManagement"}) 
    public void inviteUserToProjectWithInvalidEmail(){
        for (String email : asList("abc@gooddata.c", "<button>abc</button>@gooddata.com")) {
            checkInvitedEmail(email, String.format(INVALID_EMAIL_MESSAGE, email));
        }

        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN,
                "Invite existing editor user", editorUser);
        assertEquals(userManagementPage.getMessageText(), String.format(EXSITING_USER_MESSAGE, editorUser));
    }

    @Test(dependsOnGroups = {"userManagement"}, groups = {"activeUser"}, alwaysRun = true) 
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.deactivateUsers(adminUser);
        assertEquals(userManagementPage.getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        userManagementPage.waitForEmptyGroup();
    }

    @Test(dependsOnMethods = {"checkUserCannotDeactivateHimself"}, groups = {"activeUser"}, alwaysRun = true) 
    public void deactivateUsers() {
        List<String> emailsList = asList(editorUser, viewerUser);

        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.deactivateUsers(editorUser, viewerUser);
        assertEquals(userManagementPage.getMessageText(), DEACTIVATE_SUCCESSFUL_MESSAGE);
        assertFalse(userManagementPage.getAllUserEmails().containsAll(emailsList));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        assertTrue(compareCollections(userManagementPage.getAllUserEmails(), emailsList));
    }

    @Test(dependsOnMethods = {"deactivateUsers"}, groups = {"activeUser"}, alwaysRun = true) 
    public void activateUsers() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        refreshPage();
        userManagementPage.activateUsers(editorUser, viewerUser);
        assertEquals(userManagementPage.getMessageText(), ACTIVATE_SUCCESSFUL_MESSAGE);
        userManagementPage.waitForEmptyGroup();

        initUserManagementPage();
        assertTrue(userManagementPage.getAllUserEmails().containsAll(asList(editorUser, viewerUser)));
    }

    @Test(dependsOnGroups = {"activeUser"}, alwaysRun = true)
    public void turnOffUserManagementFeature() throws InterruptedException, IOException, JSONException {
        disableUserManagementFeature();
    }

    private void enableUserManagementFeature() throws IOException, JSONException {
        canAccessUserManagementByDefault = RestUtils.isFeatureFlagEnabled(getRestApiClient(), FEATURE_FLAG);
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, true));
        }
    }

    private void disableUserManagementFeature() throws IOException, JSONException {
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, false));
        }
    }

    private <T> boolean compareCollections(Collection<T> collectionA, Collection<T> collectionB) {
        if (collectionA.size() != collectionB.size()) 
            return false;

        return collectionA.containsAll(collectionB) && collectionB.containsAll(collectionA);
    }

    private void checkInvitedEmail(String email, String expectedMessage) {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN,
                "Invite new admin user", email);
        assertEquals(userManagementPage.openInviteUserDialog().getErrorMessage(), expectedMessage);
    }

    private void activeEmailUser(String mailTitle) throws IOException, MessagingException {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        openInvitationEmailLink(getEmailContent(imapClient, mailTitle));
        waitForFragmentVisible(dashboardsPage);
        System.out.println("Dashboard page is loaded.");
    }

    private void openInvitationEmailLink(String mailContent) {
        // Get index of invitation id at email content
        int index = mailContent.indexOf("/p/") + 3;
        // Get invitation id (having the length is 32 chars) and open active link
        openUrl("p/" + mailContent.substring(index, index + 32));
    }

    private String getEmailContent(final ImapClient imapClient,
            final String mailTitle) throws IOException, MessagingException {
        final List<Message> messages = new ArrayList<Message>();
        // Add all current messages with the same title before waiting new message from inbox
        messages.addAll(Arrays.asList(imapClient.getMessagesFromInbox(INVITATION_FROM_EMAIL, mailTitle)));
        // Save begin size of message list
        final int currentSize = messages.size();

        Graphene.waitGui().withTimeout(10, TimeUnit.MINUTES)
                          .pollingEvery(10, TimeUnit.SECONDS)
                          .withMessage("Waiting for messages ..." + mailTitle)
                          .until(new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver input) {
                        messages.addAll(Arrays.asList(imapClient.getMessagesFromInbox(INVITATION_FROM_EMAIL,
                                mailTitle)));
                        // New message arrived when new size of message list > begin size
                        return messages.size() > currentSize;
                    }
                });
        System.out.println("The message arrived");
        return messages.get(messages.size() - 1).getContent().toString().trim();
    }

    private void createUserGroups(String... groupNames) throws JSONException, IOException{
        for (String group : groupNames) {
            RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        }
    }

    private void refreshPage() {
        browser.navigate().refresh();
        waitForFragmentVisible(userManagementPage);
    }
}
