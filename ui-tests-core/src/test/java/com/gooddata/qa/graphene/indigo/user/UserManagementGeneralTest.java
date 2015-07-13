package com.gooddata.qa.graphene.indigo.user;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

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

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.DeleteGroupDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.GroupDialog;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.UserStates;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static java.util.Arrays.asList;

public class UserManagementGeneralTest extends GoodSalesAbstractTest {

    private static final String DASHBOARD_TEST = "New Dashboard";
    private static final String DELETE_GROUP_DIALOG_CONTENT = "The group with associated permissions will be "
            + "removed. Users will remain.\nThis action cannot be undone.";
    private boolean canAccessUserManagementByDefault;
    private String group1;
    private String group2;
    private String group3;
    private String userManagementAdmin;
    private String editorUser;
    private String viewerUser;
    private String domainAdminUser;
    private List<String> group1Group2List;
    private List<String> allUserEmails;

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
    private static final String EXSITING_USER_MESSAGE = "All users were already in the project.";
    private static final String EMPTY_GROUP_STATE_MESSAGE = "This group is empty";
    private static final String NO_ACTIVE_INVITATIONS_MESSAGE = "No active invitations";
    private static final String NO_DEACTIVATED_USER_MESSAGE = "There are no deactivated users";
    private static final String EXISTING_USER_GROUP_MESSAGE = 
            "Choose a different name for your group. %s already exists.";

    private static final String INVITED_EMAIL = "abc@mail.com";

    private String userManagementPassword;
    private String userManagementUri;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-management-general" + System.currentTimeMillis();
    }

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "initialize" })
    public void initData() throws JSONException, IOException {
        group1 = "Group1";
        group2 = "Group2";
        group3 = "Group3";
        group1Group2List = asList(group1, group2);

        userManagementAdmin = testParams.loadProperty("userManagementAdmin");
        userManagementPassword = testParams.loadProperty("userManagementPassword");
        userManagementUri = testParams.loadProperty("userManagementUri");

        domainAdminUser = testParams.getUser();
        editorUser = testParams.getEditorUser();
        viewerUser = testParams.getViewerUser();
        allUserEmails = asList(userManagementAdmin, editorUser, viewerUser, domainAdminUser);

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        enableUserManagementFeature();
    }

    @Test(dependsOnMethods = { "initData" }, groups = { "initialize" })
    public void prepareUserManagementAdminAndDashboard() throws InterruptedException,
            ParseException, IOException, JSONException {
        // Use another admin user (userManagementAdmin) for testing
        // The reason here is that user mangement page has no context (projectID)
        // the test will probably fail if the original admin user is used in parallel in many test executions
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(), domainAdminUser,
                testParams.getPassword(), userManagementUri, UserRoles.ADMIN);
        logout();
        signInAtGreyPages(userManagementAdmin, userManagementPassword);

        // Go to Dashboard page of new created project to use User management page of that project
        initProjectsPage();
        initDashboardsPage();
        dashboardsPage.addNewDashboard(DASHBOARD_TEST);
    }

    @Test(dependsOnMethods = { "prepareUserManagementAdminAndDashboard" }, groups = { "initialize" })
    public void prepareUserGroups() throws JSONException, IOException {
        createUserGroups(group1, group2, group3);

        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.addUsersToGroup(group1, userManagementAdmin);
        userManagementPage.addUsersToGroup(group2, editorUser);
        userManagementPage.addUsersToGroup(group3, viewerUser);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void accessFromProjectsAndUsersPage() {
        initProjectsAndUsersPage();
        projectAndUsersPage.openUserManagementPage();
        waitForFragmentVisible(userManagementPage);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void accessFromDashboardPage() throws InterruptedException {
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(false);
        dashboardsPage.openPermissionsDialog().openAddGranteePanel().openUserManagementPage();
        waitForFragmentVisible(userManagementPage);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void accessFromEmbeddedDashboardPage() throws InterruptedException{
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(false);
        DashboardEmbedDialog dialog = dashboardsPage.embedDashboard();
        String uri = dialog.getPreviewURI();
        browser.get(uri);
        waitForElementVisible(dashboardsPage.getContent().getRoot());
        Thread.sleep(2000);
        dashboardsPage.unlistedIconClick().openAddGranteePanel().openUserManagementPage();
        waitForFragmentVisible(userManagementPage);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkEditorCannotAccessFromDashboardPage() throws InterruptedException, JSONException{
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

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkEditorCannotAccessFromEmbeddedDashboard() throws InterruptedException, JSONException{
        initDashboardsPage();
        selectDashboard(DASHBOARD_TEST);
        publishDashboard(false);
        DashboardEmbedDialog dialog = dashboardsPage.embedDashboard();
        String uri = dialog.getPreviewURI();
        try {
            logout();
            signIn(false, UserRoles.EDITOR);
            browser.get(uri);
            waitForElementVisible(dashboardsPage.getContent().getRoot());
            Thread.sleep(2000);
            checkEditorCannotAccessUserGroupsLinkInDashboardPage(dashboardsPage.unlistedIconClick());
        } finally {
            logout();
            signInAtGreyPages(userManagementAdmin, userManagementPassword);
        }
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI" })
    public void verifyUserManagementUI() throws IOException, JSONException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().cancelInvitation();
        assertEquals(userManagementPage.getUsersCount(), 4);
        userManagementPage.selectUsers(userManagementAdmin, editorUser, viewerUser, domainAdminUser);
        userManagementPage.openGroupDialog(GroupDialog.State.CREATE).closeDialog();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI" })
    public void verifyUserGroupsList() {
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.getAllUserGroups();
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));

        userManagementPage.filterUserState(UserStates.INVITED);
        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), NO_ACTIVE_INVITATIONS_MESSAGE);

        initUserManagementPage();
        userManagementPage.openSpecificGroupPage(group1);
        assertEquals(userManagementPage.getAllUserEmails(), asList(userManagementAdmin));
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyGroupWithNoMember() throws JSONException, IOException {
        String emptyGroup = "EmptyGroup";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), emptyGroup);
        try {
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.openSpecificGroupPage(emptyGroup);
            userManagementPage.waitForEmptyGroup();
            assertEquals(userManagementPage.getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
            userManagementPage.startAddingUser();
            waitForFragmentVisible(userManagementPage);

            // Check "Active" and "All active users" links of sidebar are selected
            assertEquals(userManagementPage.getAllSidebarActiveLinks(), asList("Active", "All active users"));
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), allUserEmails));
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void updateGroupAfterRemoveMember() throws JSONException, IOException {
        String group = "Test Group";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        try {
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.addUsersToGroup(group, userManagementAdmin);

            userManagementPage.openSpecificGroupPage(group);
            userManagementPage.removeUsersFromGroup(group, userManagementAdmin);

            refreshPage();
            userManagementPage.waitForEmptyGroup();
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyUngroupedUsersGroup() {
        initDashboardsPage();
        initUserManagementPage();
        removeUserFromGroups(userManagementAdmin, group1, group2, group3);

        try {
            initUngroupedUsersPage();
            assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));
        } finally {
            initUserManagementPage();
            userManagementPage.addUsersToGroup(group1, userManagementAdmin);
        }
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "userManagement" })
    public void adminChangeGroupsMemberOf() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : group1Group2List) {
            userManagementPage.addUsersToGroup(group, userManagementAdmin, editorUser, viewerUser);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        List<String> expectedUsers = asList(userManagementAdmin, editorUser, viewerUser);
        for (String group : group1Group2List) {
            userManagementPage.openSpecificGroupPage(group);
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), expectedUsers));
        }
    }

    @Test(dependsOnMethods = { "adminChangeGroupsMemberOf" }, groups = { "userManagement" })
    public void adminChangeGroupsShared() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : group1Group2List) {
            userManagementPage.removeUsersFromGroup(group, userManagementAdmin);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        initUserManagementPage();
        userManagementPage.addUsersToGroup(group3, userManagementAdmin);

        userManagementPage.openSpecificGroupPage(group3);
        assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException {
        String groupName = "New Group";
        String userGroupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        initUserManagementPage();
        int userGroupsCount = userManagementPage.getUserGroupsCount();

        RestUtils.deleteUserGroup(restApiClient, userGroupUri);
        refreshPage();
        assertEquals(userManagementPage.getUserGroupsCount(), userGroupsCount - 1);
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
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

        final String message = String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName());
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return message.equals(userManagementPage.getMessageText());
            }
        });

        refreshPage();
        assertEquals(userManagementPage.getUserRole(userManagementAdmin), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = { "inviteUserToProject" }, groups = { "userManagement" }, alwaysRun = true)
    public void checkUserCannotChangeRoleOfPendingUser() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user",
                INVITED_EMAIL);

        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        userManagementPage.selectUsers(INVITED_EMAIL);
        assertFalse(userManagementPage.isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProject() throws IOException, MessagingException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", imapUser);

        assertEquals(userManagementPage.getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);
        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        initUserManagementPage();
        userManagementPage.filterUserState(UserStates.ACTIVE);
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));
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

    @Test(dependsOnGroups = { "userManagement" }, groups = { "activeUser" }, alwaysRun = true)
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.deactivateUsers(userManagementAdmin);
        assertEquals(userManagementPage.getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), NO_DEACTIVATED_USER_MESSAGE);
    }

    @Test(dependsOnMethods = { "checkUserCannotDeactivateHimself" }, groups = { "activeUser" }, alwaysRun = true)
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

    @Test(dependsOnMethods = { "deactivateUsers" }, groups = { "activeUser" }, alwaysRun = true)
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

    @Test(dependsOnGroups = "verifyUI")
    public void addNewGroup() {
        String group = "Test add group";
        checkAddingUserGroup(group);
        initUserManagementPage();
        userManagementPage.addUsersToGroup(group, userManagementAdmin);

        userManagementPage.openSpecificGroupPage(group);
        assertTrue(userManagementPage.getAllUserEmails().contains(userManagementAdmin));
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
        userManagementPage.cancelCreatingNewGroup(group);
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addExistingGroupName() {
        initDashboardsPage();
        initUserManagementPage();
        GroupDialog groupDialog = userManagementPage.openGroupDialog(GroupDialog.State.CREATE);
        groupDialog.enterGroupName(group1);
        assertFalse(groupDialog.isSubmitButtonVisible());
        assertEquals(groupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, group1));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroup() throws JSONException, IOException {
        String groupName = "Rename group test";
        String newGroupName = groupName + " renamed";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        try {
            initDashboardsPage();
            initUserManagementPage();
            refreshPage();
            userManagementPage.openSpecificGroupPage(groupName);

            GroupDialog editGroupDialog = userManagementPage.openGroupDialog(GroupDialog.State.EDIT);
            editGroupDialog.verifyStateOfDialog(GroupDialog.State.EDIT);
            assertEquals(editGroupDialog.getGroupNameText(), groupName);

            userManagementPage.renameUserGroup(newGroupName);
            assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(newGroupName));
            assertEquals(userManagementPage.getUserPageTitle(), newGroupName);
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroupWithExistingName() throws JSONException, IOException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openSpecificGroupPage(group1);

        GroupDialog editGroupDialog = userManagementPage.openGroupDialog(GroupDialog.State.EDIT);
        editGroupDialog.enterGroupName(group2);
        assertFalse(editGroupDialog.isSubmitButtonVisible());
        assertEquals(editGroupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, group2));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelRenamingUserGroup() {
        String group = "Cancel group name";
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.openSpecificGroupPage(group1);
        userManagementPage.cancelRenamingUserGroup(group);

        userManagementPage.getAllSidebarActiveLinks().contains(group1);
        assertEquals(userManagementPage.getUserPageTitle(), group1 + " " + userManagementPage.getUsersCount());
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = { "activeUser" }, groups = { "deleteGroup" })
    public void checkDeleteGroupLinkNotShownInBuiltInGroup() {
        initDashboardsPage();
        initUserManagementPage();
        
        userManagementPage.openSpecificGroupPage(UserManagementPage.ALL_ACTIVE_USERS_GROUP);
        assertFalse(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link is shown in default group");
        
        userManagementPage.openSpecificGroupPage(UserManagementPage.UNGROUPED_USERS);
        assertFalse(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link is shown in default group");
    }

    @Test(dependsOnMethods = { "checkDeleteGroupLinkNotShownInBuiltInGroup" }, groups = { "deleteGroup" })
    public void checkDeleteUserGroup() {
        initDashboardsPage();
        initUserManagementPage();
        
        int customGroupCount = userManagementPage.getUserGroupsCount(); 
        userManagementPage.openSpecificGroupPage(group1);
        
        assertTrue(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link does not show");
        DeleteGroupDialog dialog = userManagementPage.openDeleteGroupDialog();
        assertEquals(dialog.getTitle(), "Delete group " + group1, "Title of delete group dialog is wrong");
        assertEquals(dialog.getBodyContent(), DELETE_GROUP_DIALOG_CONTENT, 
                "Content of delete group dialog is wrong");
        dialog.cancel();
        
        userManagementPage.cancelDeleteUserGroup();
        assertEquals(userManagementPage.getUserGroupsCount(), customGroupCount);

        List<String> emailsList = asList(domainAdminUser, userManagementAdmin, editorUser, viewerUser);
        userManagementPage.openSpecificGroupPage(group1);
        userManagementPage.deleteUserGroup();
        assertEquals(userManagementPage.getUserGroupsCount(), customGroupCount-1);
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteUserGroup" }, groups = { "deleteGroup" })
    public void checkDeleteAllGroups() {
        initDashboardsPage();
        initUserManagementPage();
        
        List<String> emailsList = asList(domainAdminUser, userManagementAdmin, editorUser, viewerUser);
        while (userManagementPage.getUserGroupsCount() > 0) {
            userManagementPage.openSpecificGroupPage(userManagementPage.getAllUserGroups().get(0));
            userManagementPage.deleteUserGroup();
        }
        
        assertFalse(userManagementPage.isDefaultGroupsPresent(), "default groups are still shown");
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteAllGroups" }, groups = { "deleteGroup" })
    public void checkCreateANewGroupAfterRemovalOfAllGroups() {
        initDashboardsPage();
        initUserManagementPage();
        
        userManagementPage.createNewGroup(group1);
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1));
        assertTrue(userManagementPage.isDefaultGroupsPresent(), "default groups are not shown");
    }

    @Test(dependsOnGroups = { "activeUser", "userManagement", "verifyUI", "initialize", "deleteGroup" },
            alwaysRun = true)
    public void turnOffUserManagementFeature() throws InterruptedException, IOException, JSONException {
        disableUserManagementFeature();
    }

    private void checkEditorCannotAccessUserGroupsLinkInDashboardPage(PermissionsDialog permissionsDialog) 
            throws InterruptedException {
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        assertFalse(addGranteesDialog.isUserGroupLinkShown(), "Editor user could see user group link");
    }

    private void checkUserEmailsStillAvailableAfterDeletingGroup(List<String> emailsList) {
        userManagementPage.filterUserState(UserStates.ACTIVE);
        assertTrue(userManagementPage.getAllUserEmails().containsAll(emailsList), "missing user email");
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
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", email);
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

    private String getEmailContent(final ImapClient imapClient, final String mailTitle) throws IOException,
            MessagingException {
        Collection<Message> messages = ImapUtils.waitForMessage(imapClient, GDEmails.INVITATION, mailTitle);
        System.out.println("The message arrived");
        return Iterables.getLast(messages).getContent().toString().trim();
    }

    private void createUserGroups(String... groupNames) throws JSONException, IOException {
        for (String group : groupNames) {
            RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        }
    }

    private void refreshPage() {
        browser.navigate().refresh();
        waitForFragmentVisible(userManagementPage);
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
        userManagementPage.createNewGroup(group);

        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
        assertTrue(userManagementPage.getAllUserGroups().contains(group));
        assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(group));
    }
}
