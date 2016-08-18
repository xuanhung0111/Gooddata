package com.gooddata.qa.graphene.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.dashboard.PublishType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.user.UserStates;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.DeleteGroupDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.GroupDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.UserInvitationDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class UserManagementGeneralTest extends GoodSalesAbstractTest {

    private static final String DASHBOARD_TEST = "New Dashboard";
    private static final String DELETE_GROUP_DIALOG_CONTENT = "The group with associated permissions will be "
            + "removed. Users will remain.\nThis action cannot be undone.";
    private static final String GROUP1 = "Group1";
    private static final String GROUP2 = "Group2";
    private static final String GROUP3 = "Group3";

    private String editorUser;
    private String viewerUser;

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
    private static final String EXSITING_USER_MESSAGE = "All users were already in the project.";
    private static final String EMPTY_GROUP_STATE_MESSAGE = "This group is empty";
    private static final String NO_ACTIVE_INVITATIONS_MESSAGE = "No active invitations";
    private static final String NO_DEACTIVATED_USER_MESSAGE = "There are no deactivated users";
    private static final String EXISTING_USER_GROUP_MESSAGE = 
            "Choose a different name for your group. %s already exists.";

    private static final String INVITED_EMAIL = "abc@mail.com";

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "User-management-general" + System.currentTimeMillis();
    }

    @BeforeClass(alwaysRun = true)
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "initialize", "sanity" })
    public void initData() throws JSONException, IOException {
        editorUser = testParams.getEditorUser();
        viewerUser = testParams.getViewerUser();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, true);
    }

    @Test(dependsOnMethods = { "initData" }, groups = { "initialize", "sanity" })
    public void prepareUserManagementAdminAndDashboard() throws 
            ParseException, IOException, JSONException {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(DASHBOARD_TEST);
    }

    @Test(dependsOnMethods = { "prepareUserManagementAdminAndDashboard" }, groups = { "initialize", "sanity" })
    public void prepareUserGroups() throws JSONException, IOException {
        createUserGroups(GROUP1, GROUP2, GROUP3);

        initDashboardsPage();

        initUserManagementPage().addUsersToGroup(GROUP1, testParams.getUser())
            .addUsersToGroup(GROUP2, editorUser)
            .addUsersToGroup(GROUP3, viewerUser);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkXssInUsername() throws ParseException, JSONException, IOException {
        final String xssUser = "<button>XSS user</button>";
        String oldUser = UserManagementRestUtils.updateFirstNameOfCurrentAccount(getRestApiClient(), xssUser);

        try {
            initDashboardsPage();
            assertTrue(initUserManagementPage().getAllUsernames()
                    .stream()
                    .anyMatch(name -> name.startsWith(xssUser)), "Cannot find user with first name: " + xssUser);
        } finally {
            UserManagementRestUtils.updateFirstNameOfCurrentAccount(getRestApiClient(), oldUser);
        }
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkXssInGroupName() {
        final String xssGroup = "<button>group</button>";
        initDashboardsPage();
        assertTrue(initUserManagementPage().createNewGroup(xssGroup)
                .getAllUserGroups()
                .contains(xssGroup));

        assertFalse(UserManagementPage.getInstance(browser).openSpecificGroupPage(xssGroup)
                .deleteUserGroup()
                .getAllUserGroups()
                .contains(xssGroup));
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement", "sanity" })
    public void accessFromProjectsAndUsersPage() {
        initProjectsAndUsersPage()
            .openUserManagementPage()
            .waitForUsersContentLoaded();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement", "sanity" })
    public void accessFromDashboardPage() {
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(false);
        dashboardsPage.openPermissionsDialog().openAddGranteePanel().openUserManagementPage();
        Sleeper.sleepTightInSeconds(1);
        BrowserUtils.switchToLastTab(browser);
        UserManagementPage.getInstance(browser).waitForUsersContentLoaded();
        BrowserUtils.switchToFirstTab(browser);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkEditorCannotAccessFromDashboardPage() throws JSONException{
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(true);
        try {
            logout();
            signIn(false, UserRoles.EDITOR);
            initDashboardsPage();
            selectDashboard(DASHBOARD_TEST);
            checkEditorCannotAccessUserGroupsLinkInDashboardPage(dashboardsPage.openPermissionsDialog());
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI", "sanity" })
    public void verifyUserManagementUI() throws IOException, JSONException {
        initDashboardsPage();
        initUserManagementPage().openInviteUserDialog().cancelInvitation();
        assertEquals(UserManagementPage.getInstance(browser).getUsersCount(), 4);
        UserManagementPage.getInstance(browser)
            .selectUsers(testParams.getUser(), editorUser, viewerUser, testParams.getDomainUser())
            .openGroupDialog(GroupDialog.State.CREATE).closeDialog();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI", "sanity" })
    public void verifyUserGroupsList() {
        List<String> groups = asList(GROUP1, GROUP2, GROUP3);
        initDashboardsPage();

        assertEquals(initUserManagementPage().getAllUserGroups(), groups);
        assertEquals(UserManagementPage.getInstance(browser).filterUserState(UserStates.DEACTIVATED)
                .getAllUserGroups(), groups);
        assertEquals(UserManagementPage.getInstance(browser).filterUserState(UserStates.INVITED)
                .waitForEmptyGroup()
                .getStateGroupMessage(), NO_ACTIVE_INVITATIONS_MESSAGE);

        assertEquals(initUserManagementPage().openSpecificGroupPage(GROUP1)
                .getAllUserEmails(), asList(testParams.getUser()));
        assertEquals(UserManagementPage.getInstance(browser).getAllUserGroups(), groups);
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyGroupWithNoMember() throws JSONException, IOException {
        String emptyGroup = "EmptyGroup";
        String groupUri = UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), emptyGroup);
        try {
            initDashboardsPage();
            assertEquals(initUserManagementPage().openSpecificGroupPage(emptyGroup)
                    .waitForEmptyGroup()
                    .getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);

            // Check "Active" and "All active users" links of sidebar are selected
            assertEquals(UserManagementPage.getInstance(browser).startAddingUser().getAllSidebarActiveLinks(),
                    asList("Active", "All active users"));
            assertTrue(compareCollections(UserManagementPage.getInstance(browser).getAllUserEmails(), 
                    asList(testParams.getUser(), editorUser, viewerUser, testParams.getDomainUser())));
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void updateGroupAfterRemoveMember() throws JSONException, IOException {
        String group = "Test Group";
        String groupUri = UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        try {
            initDashboardsPage();
            initUserManagementPage().addUsersToGroup(group, testParams.getUser())
                .openSpecificGroupPage(group)
                .removeUsersFromGroup(group, testParams.getUser())
                .waitForEmptyGroup();
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyUngroupedUsersGroup() {
        initDashboardsPage();
        initUserManagementPage();
        removeUserFromGroups(testParams.getUser(), GROUP1, GROUP2, GROUP3);

        try {
            assertTrue(initUngroupedUsersPage().getAllUserEmails().contains(testParams.getUser()));
        } finally {
            initUserManagementPage().addUsersToGroup(GROUP1, testParams.getUser());
        }
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "userManagement", "sanity" })
    public void adminChangeGroupsMemberOf() {
        final List<String> groups = asList(GROUP1, GROUP2);
        initDashboardsPage();
        initUserManagementPage();

        for (String group : groups) {
            assertEquals(UserManagementPage.getInstance(browser).addUsersToGroup(group, testParams.getUser(), editorUser, viewerUser)
                    .getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        List<String> expectedUsers = asList(testParams.getUser(), editorUser, viewerUser);
        for (String group : groups) {
            assertTrue(compareCollections(UserManagementPage.getInstance(browser).openSpecificGroupPage(group)
                    .getAllUserEmails(), expectedUsers));
        }
    }

    @Test(dependsOnMethods = { "adminChangeGroupsMemberOf" }, groups = { "userManagement" })
    public void adminChangeGroupsShared() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : asList(GROUP1, GROUP2)) {
            assertEquals(UserManagementPage.getInstance(browser).removeUsersFromGroup(group, testParams.getUser())
                    .getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        assertTrue(initUserManagementPage().addUsersToGroup(GROUP3, testParams.getUser())
                .openSpecificGroupPage(GROUP3)
                .getAllUserEmails().contains(testParams.getUser()));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException {
        String groupName = "New Group";
        UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        int userGroupsCount = initUserManagementPage().getUserGroupsCount();

        assertEquals(UserManagementPage.getInstance(browser).openSpecificGroupPage(groupName)
                .deleteUserGroup()
                .getUserGroupsCount(), userGroupsCount - 1);
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void changeRoleOfUsers() {
        try {
            initDashboardsPage();
            String adminText = UserRoles.ADMIN.getName();

            initUserManagementPage().changeRoleOfUsers(UserRoles.ADMIN, editorUser, viewerUser);
            Predicate<WebDriver> changeRoleSuccessfully = 
                    browser -> String.format(CHANGE_ROLE_SUCCESSFUL_MESSAGE, adminText)
                        .equals(UserManagementPage.getInstance(browser).getMessageText());
            Graphene.waitGui().until(changeRoleSuccessfully);

            for (String email : asList(editorUser, viewerUser)) {
                assertEquals(UserManagementPage.getInstance(browser).getUserRole(email), adminText);
            }
        } finally {
            UserManagementPage.getInstance(browser).changeRoleOfUsers(UserRoles.EDITOR, editorUser);
            initUserManagementPage().changeRoleOfUsers(UserRoles.VIEWER, viewerUser);
        }
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void checkUserCannotChangeRoleOfHimself() {
        initDashboardsPage();
        initUserManagementPage().changeRoleOfUsers(UserRoles.EDITOR, testParams.getUser());

        Predicate<WebDriver> changeRoleFailed = 
                browser -> String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName())
                    .equals(UserManagementPage.getInstance(browser).getMessageText());
        Graphene.waitGui().until(changeRoleFailed);

        assertEquals(UserManagementPage.getInstance(browser).getUserRole(testParams.getUser()), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = { "inviteUserToProject" }, groups = { "userManagement" }, alwaysRun = true)
    public void checkUserCannotChangeRoleOfPendingUser() {
        initDashboardsPage();

        UserInvitationDialog dialog = initUserManagementPage().openInviteUserDialog();
        dialog.invitePeople(UserRoles.ADMIN, "Invite new admin user", INVITED_EMAIL);
        waitForFragmentNotVisible(dialog);

        assertFalse(UserManagementPage.getInstance(browser).filterUserState(UserStates.INVITED)
                .selectUsers(INVITED_EMAIL)
                .isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void inviteUserToProject() throws IOException, MessagingException {
        initDashboardsPage();
        initUserManagementPage().openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", imapUser);

        assertEquals(UserManagementPage.getInstance(browser).getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);
        assertTrue(UserManagementPage.getInstance(browser).filterUserState(UserStates.INVITED)
                .getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        assertTrue(initUserManagementPage().filterUserState(UserStates.ACTIVE)
                .getAllUserEmails().contains(imapUser));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProjectWithInvalidEmail() {
        for (String email : asList("abc@gooddata.c", "<button>abc</button>@gooddata.com")) {
            checkInvitedEmail(email, String.format(INVALID_EMAIL_MESSAGE, email));
        }

        initDashboardsPage();
        initUserManagementPage().openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite existing editor user",
                editorUser);
        assertEquals(UserManagementPage.getInstance(browser).getMessageText(), EXSITING_USER_MESSAGE,
                "Confirm message is not correct when adding exsiting user into the project!");
    }

    @Test(dependsOnGroups = { "userManagement" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        assertEquals(initUserManagementPage().deactivateUsers(testParams.getUser())
                .getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(UserManagementPage.getInstance(browser).getAllUserEmails().contains(testParams.getUser()));
        assertEquals(UserManagementPage.getInstance(browser).filterUserState(UserStates.DEACTIVATED)
                .waitForEmptyGroup()
                .getStateGroupMessage(), NO_DEACTIVATED_USER_MESSAGE);
    }

    @Test(dependsOnMethods = { "checkUserCannotDeactivateHimself" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void deactivateUsers() {
        List<String> emailsList = asList(editorUser, viewerUser);

        initDashboardsPage();
        assertEquals(initUserManagementPage().deactivateUsers(editorUser, viewerUser)
                .getMessageText(), DEACTIVATE_SUCCESSFUL_MESSAGE);
        assertFalse(UserManagementPage.getInstance(browser).getAllUserEmails().containsAll(emailsList));
        assertTrue(compareCollections(UserManagementPage.getInstance(browser).filterUserState(UserStates.DEACTIVATED)
                .getAllUserEmails(), emailsList));
    }

    @Test(dependsOnMethods = { "deactivateUsers" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void activateUsers() {
        initDashboardsPage();
        assertEquals(initUserManagementPage().filterUserState(UserStates.DEACTIVATED)
                .activateUsers(editorUser, viewerUser)
                .getMessageText(), ACTIVATE_SUCCESSFUL_MESSAGE);
        UserManagementPage.getInstance(browser).waitForEmptyGroup();

        assertTrue(initUserManagementPage().getAllUserEmails().containsAll(asList(editorUser, viewerUser)));
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "sanity" })
    public void addNewGroup() {
        String group = "Test add group";
        checkAddingUserGroup(group);
        initUserManagementPage().addUsersToGroup(group, testParams.getUser());

        assertTrue(UserManagementPage.getInstance(browser).openSpecificGroupPage(group)
                .getAllUserEmails().contains(testParams.getUser()));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addUnicodeGroupName() {
        checkAddingUserGroup("ພາສາລາວ résumé اللغة");
        checkAddingUserGroup("Tiếng Việt");
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelAddingNewGroup() {
        initDashboardsPage();
        String group = "Test cancel group";
        assertFalse(initUserManagementPage().cancelCreatingNewGroup(group)
                .getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addExistingGroupName() {
        initDashboardsPage();
        GroupDialog groupDialog = initUserManagementPage().openGroupDialog(GroupDialog.State.CREATE);
        groupDialog.enterGroupName(GROUP1);
        assertFalse(groupDialog.isSubmitButtonVisible());
        assertEquals(groupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, GROUP1));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroup() throws JSONException, IOException {
        String groupName = "Rename group test";
        String newGroupName = groupName + " renamed";
        String groupUri = UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        try {
            initDashboardsPage();

            GroupDialog editGroupDialog = initUserManagementPage().openSpecificGroupPage(groupName)
                    .openGroupDialog(GroupDialog.State.EDIT);
            assertEquals(editGroupDialog.verifyStateOfDialog(GroupDialog.State.EDIT)
                    .getGroupNameText(), groupName);

            assertTrue(UserManagementPage.getInstance(browser).renameUserGroup(newGroupName)
                    .getAllSidebarActiveLinks().contains(newGroupName));
            assertEquals(UserManagementPage.getInstance(browser).getUserPageTitle(), newGroupName);
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroupWithExistingName() throws JSONException, IOException {
        initDashboardsPage();
        GroupDialog editGroupDialog = initUserManagementPage().openSpecificGroupPage(GROUP1).openGroupDialog(GroupDialog.State.EDIT);
        assertFalse(editGroupDialog.enterGroupName(GROUP2).isSubmitButtonVisible());
        assertEquals(editGroupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, GROUP2));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelRenamingUserGroup() {
        String group = "Cancel group name";
        initDashboardsPage();

        initUserManagementPage().openSpecificGroupPage(GROUP1)
            .cancelRenamingUserGroup(group)
            .getAllSidebarActiveLinks().contains(GROUP1);
        assertEquals(UserManagementPage.getInstance(browser).getUserPageTitle(), GROUP1 + " " +
            UserManagementPage.getInstance(browser).getUsersCount());
        assertFalse(UserManagementPage.getInstance(browser).getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = { "activeUser" }, groups = { "deleteGroup" })
    public void checkDeleteGroupLinkNotShownInBuiltInGroup() {
        initDashboardsPage();

        assertFalse(initUserManagementPage().openSpecificGroupPage(UserManagementPage.ALL_ACTIVE_USERS_GROUP)
                .isDeleteGroupLinkPresent(), "Delete group link is shown in default group");

        assertFalse(UserManagementPage.getInstance(browser).openSpecificGroupPage(UserManagementPage.UNGROUPED_USERS)
                .isDeleteGroupLinkPresent(), "Delete group link is shown in default group");
    }

    @Test(dependsOnMethods = { "checkDeleteGroupLinkNotShownInBuiltInGroup" }, groups = { "deleteGroup" })
    public void checkDeleteUserGroup() {
        initDashboardsPage();

        int customGroupCount = initUserManagementPage().getUserGroupsCount(); 
        UserManagementPage.getInstance(browser).openSpecificGroupPage(GROUP1);

        assertTrue(UserManagementPage.getInstance(browser).isDeleteGroupLinkPresent(), "Delete group link does not show");
        DeleteGroupDialog dialog = UserManagementPage.getInstance(browser).openDeleteGroupDialog();
        assertEquals(dialog.getTitle(), "Delete group " + GROUP1, "Title of delete group dialog is wrong");
        assertEquals(dialog.getBodyContent(), DELETE_GROUP_DIALOG_CONTENT,
                "Content of delete group dialog is wrong");
        dialog.cancel();

        assertEquals(UserManagementPage.getInstance(browser).getUserGroupsCount(), customGroupCount);

        List<String> emailsList = asList(testParams.getDomainUser(), testParams.getUser(), editorUser, viewerUser);

        assertEquals(UserManagementPage.getInstance(browser).openSpecificGroupPage(GROUP1)
                .deleteUserGroup()
                .getUserGroupsCount(), customGroupCount-1);
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteUserGroup" }, groups = { "deleteGroup" })
    public void checkDeleteAllGroups() {
        initDashboardsPage();
        initUserManagementPage();

        List<String> emailsList = asList(testParams.getDomainUser(), testParams.getUser(), editorUser, viewerUser);
        while (UserManagementPage.getInstance(browser).getUserGroupsCount() > 0) {
            UserManagementPage.getInstance(browser).openSpecificGroupPage(UserManagementPage.getInstance(browser).getAllUserGroups().get(0))
                .deleteUserGroup();
        }

        assertFalse(UserManagementPage.getInstance(browser).isDefaultGroupsPresent(), "default groups are still shown");
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteAllGroups" }, groups = { "deleteGroup" })
    public void checkCreateANewGroupAfterRemovalOfAllGroups() {
        initDashboardsPage();

        assertEquals(initUserManagementPage().createNewGroup(GROUP1).getAllUserGroups(), asList(GROUP1));
        assertTrue(UserManagementPage.getInstance(browser).isDefaultGroupsPresent(), "default groups are not shown");
    }

    @Test(dependsOnGroups = { "activeUser", "userManagement", "verifyUI", "initialize", "deleteGroup" },
            alwaysRun = true)
    public void turnOffUserManagementFeature() throws IOException, JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, false);
    }

    private void checkEditorCannotAccessUserGroupsLinkInDashboardPage(PermissionsDialog permissionsDialog) {
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        assertFalse(addGranteesDialog.isUserGroupLinkShown(), "Editor user could see user group link");
    }

    private void checkUserEmailsStillAvailableAfterDeletingGroup(List<String> emailsList) {
        assertTrue(UserManagementPage.getInstance(browser).filterUserState(UserStates.ACTIVE)
                .getAllUserEmails().containsAll(emailsList), "missing user email");
    }

    private <T> boolean compareCollections(Collection<T> collectionA, Collection<T> collectionB) {
        if (collectionA.size() != collectionB.size())
            return false;

        return collectionA.containsAll(collectionB) && collectionB.containsAll(collectionA);
    }

    private void checkInvitedEmail(String email, String expectedMessage) {
        initDashboardsPage();

        UserInvitationDialog dialog = initUserManagementPage().openInviteUserDialog();
        dialog.invitePeople(UserRoles.ADMIN, "Invite new admin user", email);
        assertEquals(dialog.getErrorMessage(), expectedMessage);
        dialog.cancelInvitation();
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

    private String getEmailContent(final ImapClient imapClient, final String mailTitle) throws IOException,
            MessagingException {
        Collection<Message> messages = waitForMessages(imapClient, GDEmails.INVITATION, mailTitle, 1);
        return Iterables.getLast(messages).getContent().toString().trim();
    }

    private void createUserGroups(String... groupNames) throws JSONException, IOException {
        for (String group : groupNames) {
            UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        }
    }

    private void removeUserFromGroups(String user, String... groups) {
        for (String group : groups) {
            UserManagementPage.getInstance(browser).removeUsersFromGroup(group, user);
        }
    }

    private void checkAddingUserGroup(String group) {
        initDashboardsPage();

        GroupDialog createDialog = initUserManagementPage().openGroupDialog(GroupDialog.State.CREATE);
        createDialog.verifyStateOfDialog(GroupDialog.State.CREATE);
        createDialog.closeDialog();
        UserManagementPage.getInstance(browser).createNewGroup(group)
                .waitForEmptyGroup();

        assertEquals(UserManagementPage.getInstance(browser).getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
        assertTrue(UserManagementPage.getInstance(browser).getAllUserGroups().contains(group));
        assertTrue(UserManagementPage.getInstance(browser).getAllSidebarActiveLinks().contains(group));
    }
}
