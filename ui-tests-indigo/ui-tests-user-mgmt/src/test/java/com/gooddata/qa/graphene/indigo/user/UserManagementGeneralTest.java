package com.gooddata.qa.graphene.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.createUser;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;
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
import org.testng.annotations.AfterClass;
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
import com.gooddata.qa.utils.http.RestApiClient;
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

    private String userManagementAdmin;
    private String userManagementPassword;
    private String editorUser;
    private String viewerUser;
    private String domainAdminUser;

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
        userManagementAdmin = generateEmail(testParams.getUser());
        userManagementPassword = testParams.getPassword();
        createUser(getRestApiClient(), testParams.getUserDomain(), userManagementAdmin, userManagementPassword);

        domainAdminUser = testParams.getUser();
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
        // Use another admin user (userManagementAdmin) for testing
        // The reason here is that user mangement page has no context (projectID)
        // the test will probably fail if the original admin user is used in parallel in many test executions
        UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), userManagementAdmin,
                UserRoles.ADMIN);
        logout();
        signInAtGreyPages(userManagementAdmin, userManagementPassword);

        // Go to Dashboard page of new created project to use User management page of that project
        initDashboardsPage();
        dashboardsPage.addNewDashboard(DASHBOARD_TEST);
    }

    @Test(dependsOnMethods = { "prepareUserManagementAdminAndDashboard" }, groups = { "initialize", "sanity" })
    public void prepareUserGroups() throws JSONException, IOException {
        createUserGroups(GROUP1, GROUP2, GROUP3);

        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.addUsersToGroup(GROUP1, userManagementAdmin)
            .addUsersToGroup(GROUP2, editorUser)
            .addUsersToGroup(GROUP3, viewerUser);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkXssInUsername() throws ParseException, JSONException, IOException {
        final RestApiClient restApiClient = getRestApiClient(userManagementAdmin, userManagementPassword);
        final String xssUser = "<button>XSS user</button>";
        String oldUser = UserManagementRestUtils.updateFirstNameOfCurrentAccount(restApiClient, xssUser);

        try {
            initDashboardsPage();
            initUserManagementPage();
            assertTrue(waitForFragmentVisible(userManagementPage).getAllUsernames()
                    .stream()
                    .anyMatch(name -> name.startsWith(xssUser)), "Cannot find user with first name: " + xssUser);
        } finally {
            UserManagementRestUtils.updateFirstNameOfCurrentAccount(restApiClient, oldUser);
        }
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkXssInGroupName() {
        final String xssGroup = "<button>group</button>";
        initDashboardsPage();
        initUserManagementPage();
        assertTrue(waitForFragmentVisible(userManagementPage).createNewGroup(xssGroup)
                .getAllUserGroups()
                .contains(xssGroup));

        assertFalse(userManagementPage.openSpecificGroupPage(xssGroup)
                .deleteUserGroup()
                .getAllUserGroups()
                .contains(xssGroup));
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement", "sanity" })
    public void accessFromProjectsAndUsersPage() {
        initProjectsAndUsersPage();
        projectAndUsersPage.openUserManagementPage();
        waitForFragmentVisible(userManagementPage).waitForUsersContentLoaded();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement", "sanity" })
    public void accessFromDashboardPage() {
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(false);
        dashboardsPage.openPermissionsDialog().openAddGranteePanel().openUserManagementPage();
        Sleeper.sleepTightInSeconds(1);
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(userManagementPage).waitForUsersContentLoaded();
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
            logout();
            signInAtGreyPages(userManagementAdmin, userManagementPassword);
        }
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI", "sanity" })
    public void verifyUserManagementUI() throws IOException, JSONException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().cancelInvitation();
        assertEquals(userManagementPage.getUsersCount(), 4);
        userManagementPage.selectUsers(userManagementAdmin, editorUser, viewerUser, domainAdminUser);
        userManagementPage.openGroupDialog(GroupDialog.State.CREATE).closeDialog();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI", "sanity" })
    public void verifyUserGroupsList() {
        List<String> groups = asList(GROUP1, GROUP2, GROUP3);
        initDashboardsPage();
        initUserManagementPage();

        assertEquals(userManagementPage.getAllUserGroups(), groups);
        assertEquals(userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .getAllUserGroups(), groups);
        assertEquals(userManagementPage.filterUserState(UserStates.INVITED)
                .waitForEmptyGroup()
                .getStateGroupMessage(), NO_ACTIVE_INVITATIONS_MESSAGE);

        initUserManagementPage();

        assertEquals( userManagementPage.openSpecificGroupPage(GROUP1)
                .getAllUserEmails(), asList(userManagementAdmin));
        assertEquals(userManagementPage.getAllUserGroups(), groups);
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyGroupWithNoMember() throws JSONException, IOException {
        String emptyGroup = "EmptyGroup";
        String groupUri = UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), emptyGroup);
        try {
            initDashboardsPage();
            initUserManagementPage();
            assertEquals(userManagementPage.openSpecificGroupPage(emptyGroup)
                    .waitForEmptyGroup()
                    .getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
            userManagementPage.startAddingUser();
            waitForFragmentVisible(userManagementPage);

            // Check "Active" and "All active users" links of sidebar are selected
            assertEquals(userManagementPage.getAllSidebarActiveLinks(), asList("Active", "All active users"));
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), 
                    asList(userManagementAdmin, editorUser, viewerUser, domainAdminUser)));
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
            initUserManagementPage();
            userManagementPage.addUsersToGroup(group, userManagementAdmin)
                .openSpecificGroupPage(group)
                .removeUsersFromGroup(group, userManagementAdmin)
                .waitForEmptyGroup();
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyUngroupedUsersGroup() {
        initDashboardsPage();
        initUserManagementPage();
        removeUserFromGroups(userManagementAdmin, GROUP1, GROUP2, GROUP3);

        try {
            initUngroupedUsersPage();
            assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));
        } finally {
            initUserManagementPage();
            userManagementPage.addUsersToGroup(GROUP1, userManagementAdmin);
        }
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "userManagement", "sanity" })
    public void adminChangeGroupsMemberOf() {
        final List<String> groups = asList(GROUP1, GROUP2);
        initDashboardsPage();
        initUserManagementPage();

        for (String group : groups) {
            assertEquals(userManagementPage.addUsersToGroup(group, userManagementAdmin, editorUser, viewerUser)
                    .getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        List<String> expectedUsers = asList(userManagementAdmin, editorUser, viewerUser);
        for (String group : groups) {
            assertTrue(compareCollections(userManagementPage.openSpecificGroupPage(group)
                    .getAllUserEmails(), expectedUsers));
        }
    }

    @Test(dependsOnMethods = { "adminChangeGroupsMemberOf" }, groups = { "userManagement" })
    public void adminChangeGroupsShared() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : asList(GROUP1, GROUP2)) {
            assertEquals(userManagementPage.removeUsersFromGroup(group, userManagementAdmin)
                    .getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        initUserManagementPage();
        assertTrue(userManagementPage.addUsersToGroup(GROUP3, userManagementAdmin)
                .openSpecificGroupPage(GROUP3)
                .getAllUserEmails().contains(userManagementAdmin));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException {
        String groupName = "New Group";
        UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        initUserManagementPage();
        int userGroupsCount = userManagementPage.getUserGroupsCount();

        assertEquals(userManagementPage.openSpecificGroupPage(groupName)
                .deleteUserGroup()
                .getUserGroupsCount(), userGroupsCount - 1);
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void changeRoleOfUsers() {
        try {
            initDashboardsPage();
            initUserManagementPage();
            String adminText = UserRoles.ADMIN.getName();

            userManagementPage.changeRoleOfUsers(UserRoles.ADMIN, editorUser, viewerUser);
            Predicate<WebDriver> changeRoleSuccessfully = 
                    browser -> String.format(CHANGE_ROLE_SUCCESSFUL_MESSAGE, adminText)
                        .equals(userManagementPage.getMessageText());
            Graphene.waitGui().until(changeRoleSuccessfully);

            for (String email : asList(editorUser, viewerUser)) {
                assertEquals(userManagementPage.getUserRole(email), adminText);
            }
        } finally {
            userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, editorUser);
            initUserManagementPage();
            userManagementPage.changeRoleOfUsers(UserRoles.VIEWER, viewerUser);
        }
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void checkUserCannotChangeRoleOfHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, userManagementAdmin);

        Predicate<WebDriver> changeRoleFailed = 
                browser -> String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName())
                    .equals(userManagementPage.getMessageText());
        Graphene.waitGui().until(changeRoleFailed);

        assertEquals(userManagementPage.getUserRole(userManagementAdmin), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = { "inviteUserToProject" }, groups = { "userManagement" }, alwaysRun = true)
    public void checkUserCannotChangeRoleOfPendingUser() {
        initDashboardsPage();
        initUserManagementPage();

        UserInvitationDialog dialog = userManagementPage.openInviteUserDialog();
        dialog.invitePeople(UserRoles.ADMIN, "Invite new admin user", INVITED_EMAIL);
        waitForFragmentNotVisible(dialog);

        assertFalse(userManagementPage.filterUserState(UserStates.INVITED)
                .selectUsers(INVITED_EMAIL)
                .isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void inviteUserToProject() throws IOException, MessagingException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", imapUser);

        assertEquals(userManagementPage.getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);
        assertTrue(userManagementPage.filterUserState(UserStates.INVITED)
                .getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        initUserManagementPage();
        assertTrue(userManagementPage.filterUserState(UserStates.ACTIVE)
                .getAllUserEmails().contains(imapUser));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProjectWithInvalidEmail() {
        for (String email : asList("abc@gooddata.c", "<button>abc</button>@gooddata.com")) {
            checkInvitedEmail(email, String.format(INVALID_EMAIL_MESSAGE, email));
        }

        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite existing editor user",
                editorUser);
        assertEquals(userManagementPage.getMessageText(), EXSITING_USER_MESSAGE,
                "Confirm message is not correct when adding exsiting user into the project!");
    }

    @Test(dependsOnGroups = { "userManagement" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        initUserManagementPage();
        assertEquals(userManagementPage.deactivateUsers(userManagementAdmin)
                .getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));
        assertEquals(userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .waitForEmptyGroup()
                .getStateGroupMessage(), NO_DEACTIVATED_USER_MESSAGE);
    }

    @Test(dependsOnMethods = { "checkUserCannotDeactivateHimself" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void deactivateUsers() {
        List<String> emailsList = asList(editorUser, viewerUser);

        initDashboardsPage();
        initUserManagementPage();
        assertEquals(userManagementPage.deactivateUsers(editorUser, viewerUser)
                .getMessageText(), DEACTIVATE_SUCCESSFUL_MESSAGE);
        assertFalse(userManagementPage.getAllUserEmails().containsAll(emailsList));
        assertTrue(compareCollections( userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .getAllUserEmails(), emailsList));
    }

    @Test(dependsOnMethods = { "deactivateUsers" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void activateUsers() {
        initDashboardsPage();
        initUserManagementPage();
        assertEquals(userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .activateUsers(editorUser, viewerUser)
                .getMessageText(), ACTIVATE_SUCCESSFUL_MESSAGE);
        userManagementPage.waitForEmptyGroup();

        initUserManagementPage();
        assertTrue(userManagementPage.getAllUserEmails().containsAll(asList(editorUser, viewerUser)));
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "sanity" })
    public void addNewGroup() {
        String group = "Test add group";
        checkAddingUserGroup(group);
        initUserManagementPage();
        userManagementPage.addUsersToGroup(group, userManagementAdmin);

        assertTrue(userManagementPage.openSpecificGroupPage(group)
                .getAllUserEmails().contains(userManagementAdmin));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addUnicodeGroupName() {
        checkAddingUserGroup("ພາສາລາວ résumé اللغة");
        checkAddingUserGroup("Tiếng Việt");
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelAddingNewGroup() {
        initDashboardsPage();
        initUserManagementPage();
        String group = "Test cancel group";
        assertFalse(userManagementPage.cancelCreatingNewGroup(group)
                .getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addExistingGroupName() {
        initDashboardsPage();
        initUserManagementPage();
        GroupDialog groupDialog = userManagementPage.openGroupDialog(GroupDialog.State.CREATE);
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
            initUserManagementPage();

            GroupDialog editGroupDialog = userManagementPage.openSpecificGroupPage(groupName)
                    .openGroupDialog(GroupDialog.State.EDIT);
            assertEquals(editGroupDialog.verifyStateOfDialog(GroupDialog.State.EDIT)
                    .getGroupNameText(), groupName);

            assertTrue(userManagementPage.renameUserGroup(newGroupName)
                    .getAllSidebarActiveLinks().contains(newGroupName));
            assertEquals(userManagementPage.getUserPageTitle(), newGroupName);
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroupWithExistingName() throws JSONException, IOException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openSpecificGroupPage(GROUP1);
        GroupDialog editGroupDialog = userManagementPage.openGroupDialog(GroupDialog.State.EDIT);
        assertFalse(editGroupDialog.enterGroupName(GROUP2).isSubmitButtonVisible());
        assertEquals(editGroupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, GROUP2));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelRenamingUserGroup() {
        String group = "Cancel group name";
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.openSpecificGroupPage(GROUP1)
            .cancelRenamingUserGroup(group)
            .getAllSidebarActiveLinks().contains(GROUP1);
        assertEquals(userManagementPage.getUserPageTitle(), GROUP1 + " " + userManagementPage.getUsersCount());
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = { "activeUser" }, groups = { "deleteGroup" })
    public void checkDeleteGroupLinkNotShownInBuiltInGroup() {
        initDashboardsPage();
        initUserManagementPage();

        assertFalse(userManagementPage.openSpecificGroupPage(UserManagementPage.ALL_ACTIVE_USERS_GROUP)
                .isDeleteGroupLinkPresent(), "Delete group link is shown in default group");

        assertFalse(userManagementPage.openSpecificGroupPage(UserManagementPage.UNGROUPED_USERS)
                .isDeleteGroupLinkPresent(), "Delete group link is shown in default group");
    }

    @Test(dependsOnMethods = { "checkDeleteGroupLinkNotShownInBuiltInGroup" }, groups = { "deleteGroup" })
    public void checkDeleteUserGroup() {
        initDashboardsPage();
        initUserManagementPage();

        int customGroupCount = userManagementPage.getUserGroupsCount(); 
        userManagementPage.openSpecificGroupPage(GROUP1);

        assertTrue(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link does not show");
        DeleteGroupDialog dialog = userManagementPage.openDeleteGroupDialog();
        assertEquals(dialog.getTitle(), "Delete group " + GROUP1, "Title of delete group dialog is wrong");
        assertEquals(dialog.getBodyContent(), DELETE_GROUP_DIALOG_CONTENT,
                "Content of delete group dialog is wrong");
        dialog.cancel();

        assertEquals(userManagementPage.getUserGroupsCount(), customGroupCount);

        List<String> emailsList = asList(domainAdminUser, userManagementAdmin, editorUser, viewerUser);
       
        assertEquals(userManagementPage.openSpecificGroupPage(GROUP1)
                .deleteUserGroup()
                .getUserGroupsCount(), customGroupCount-1);
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteUserGroup" }, groups = { "deleteGroup" })
    public void checkDeleteAllGroups() {
        initDashboardsPage();
        initUserManagementPage();

        List<String> emailsList = asList(domainAdminUser, userManagementAdmin, editorUser, viewerUser);
        while (userManagementPage.getUserGroupsCount() > 0) {
            userManagementPage.openSpecificGroupPage(userManagementPage.getAllUserGroups().get(0))
                .deleteUserGroup();
        }

        assertFalse(userManagementPage.isDefaultGroupsPresent(), "default groups are still shown");
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteAllGroups" }, groups = { "deleteGroup" })
    public void checkCreateANewGroupAfterRemovalOfAllGroups() {
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.createNewGroup(GROUP1);
        assertEquals(userManagementPage.getAllUserGroups(), asList(GROUP1));
        assertTrue(userManagementPage.isDefaultGroupsPresent(), "default groups are not shown");
    }

    @Test(dependsOnGroups = { "activeUser", "userManagement", "verifyUI", "initialize", "deleteGroup" },
            alwaysRun = true)
    public void turnOffUserManagementFeature() throws IOException, JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, false);
    }

    @AfterClass
    public void deleteAccount() throws ParseException, IOException, JSONException {
        deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), userManagementAdmin);
    }

    private void checkEditorCannotAccessUserGroupsLinkInDashboardPage(PermissionsDialog permissionsDialog) {
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        assertFalse(addGranteesDialog.isUserGroupLinkShown(), "Editor user could see user group link");
    }

    private void checkUserEmailsStillAvailableAfterDeletingGroup(List<String> emailsList) {
        assertTrue(userManagementPage.filterUserState(UserStates.ACTIVE)
                .getAllUserEmails().containsAll(emailsList), "missing user email");
    }

    private <T> boolean compareCollections(Collection<T> collectionA, Collection<T> collectionB) {
        if (collectionA.size() != collectionB.size())
            return false;

        return collectionA.containsAll(collectionB) && collectionB.containsAll(collectionA);
    }

    private void checkInvitedEmail(String email, String expectedMessage) {
        initDashboardsPage();
        initUserManagementPage();

        UserInvitationDialog dialog = userManagementPage.openInviteUserDialog();
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
            userManagementPage.removeUsersFromGroup(group, user);
        }
    }

    private void checkAddingUserGroup(String group) {
        initDashboardsPage();
        initUserManagementPage();

        GroupDialog createDialog = userManagementPage.openGroupDialog(GroupDialog.State.CREATE);
        createDialog.verifyStateOfDialog(GroupDialog.State.CREATE);
        createDialog.closeDialog();
        userManagementPage.createNewGroup(group)
                .waitForEmptyGroup();

        assertEquals(userManagementPage.getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
        assertTrue(userManagementPage.getAllUserGroups().contains(group));
        assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(group));
    }
}
