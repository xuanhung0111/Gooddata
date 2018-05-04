package com.gooddata.qa.graphene.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
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
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.collect.Iterables;

public class UserManagementGeneralTest extends AbstractProjectTest {

    private static final String DASHBOARD_TEST = "New Dashboard";
    private static final String DELETE_GROUP_DIALOG_CONTENT = "The group with associated permissions will be "
            + "removed. Users will remain.\nThis action cannot be undone.";
    private static final String GROUP1 = "Group1";
    private static final String GROUP2 = "Group2";
    private static final String GROUP3 = "Group3";

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
    @Override
    public void enableDynamicUser() {
        // always turn on dynamic user on this test or we have to fix the test logic to adapt when turn off that feature
        useDynamicUser = true;
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void initProperties() {
        projectTitle = "User-management-general" + System.currentTimeMillis(); // use empty project
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, true);

        //we need to upload dummy data so we can work with Dashboard page.
        String csvFilePath = new CsvFile("data.csv")
                .columns(new CsvFile.Column("Att1"), new CsvFile.Column("Fact1"))
                .rows("Anh", "100")
                .rows("Nguyen", "200")
                .saveToDisc(testParams.getCsvFolder());
        uploadCSV(csvFilePath);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "initialize", "sanity" })
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
            .addUsersToGroup(GROUP2, testParams.getEditorUser())
            .addUsersToGroup(GROUP3, testParams.getViewerUser());
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void checkXssInUsername() throws ParseException, JSONException, IOException {
        final String xssUser = "<button>XSS user</button>";
        String oldUser = UserManagementRestUtils.updateFirstNameOfCurrentAccount(getRestApiClient(), xssUser);

        try {
            initDashboardsPage();
            UserManagementPage userManagementPage = initUserManagementPage();
            takeScreenshot(browser, "Check-xss-in-user-name", getClass());
            assertTrue(waitForFragmentVisible(userManagementPage).getAllUsernames()
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

        List<String> userGroups = initUserManagementPage()
                .createNewGroup(xssGroup)
                .getAllUserGroups();
        takeScreenshot(browser, "Check-xss-in-group-name", getClass());
        assertTrue(userGroups.contains(xssGroup));

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

        int browserTabs = BrowserUtils.getWindowHandles(browser).size();

        dashboardsPage.openPermissionsDialog().openAddGranteePanel().openUserManagementPage();
        waitForNewTabOpen(browserTabs);

        try {
            BrowserUtils.switchToLastTab(browser);
            UserManagementPage.getInstance(browser).waitForUsersContentLoaded();
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
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
        UserManagementPage userManagementPage = initUserManagementPage();
        userManagementPage.openInviteUserDialog().cancelInvitation();
        takeScreenshot(browser, "Verify-user-management-ui", getClass());
        assertEquals(userManagementPage.getUsersCount(), 4);
        userManagementPage.selectUsers(testParams.getUser(), testParams.getEditorUser(),
                testParams.getViewerUser(), testParams.getDomainUser());
        userManagementPage.openGroupDialog(GroupDialog.State.CREATE).closeDialog();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI", "sanity" })
    public void verifyUserGroupsList() {
        List<String> groups = asList(GROUP1, GROUP2, GROUP3);
        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage();

        takeScreenshot(browser, "All-user-groups-list", getClass());
        assertEquals(userManagementPage.getAllUserGroups(), groups);

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        takeScreenshot(browser, "All-user-groups-list-in-deactivated-stage", getClass());
        assertEquals(userManagementPage.getAllUserGroups(), groups);

        userManagementPage.filterUserState(UserStates.INVITED).waitForEmptyGroup();
        takeScreenshot(browser, "All-user-groups-list-in-invited-stage", getClass());
        assertEquals(userManagementPage.getStateGroupMessage(), NO_ACTIVE_INVITATIONS_MESSAGE);

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

            takeScreenshot(browser, "Verify-group-with-no-member", getClass());
            // Check "Active" and "All active users" links of sidebar are selected
            assertEquals(UserManagementPage.getInstance(browser).startAddingUser().getAllSidebarActiveLinks(),
                    asList("Active", "All active users"));
            assertTrue(compareCollections(UserManagementPage.getInstance(browser).getAllUserEmails(), 
                    asList(testParams.getUser(), testParams.getEditorUser(),
                            testParams.getViewerUser(), testParams.getDomainUser())));
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
            UserManagementPage userManagementPage = initUngroupedUsersPage();
            takeScreenshot(browser, "Verify-ungrouped-user-group", getClass());
            assertTrue(userManagementPage.getAllUserEmails().contains(testParams.getUser()));
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
            assertEquals(UserManagementPage.getInstance(browser)
                    .addUsersToGroup(group, testParams.getUser(), testParams.getEditorUser(), testParams.getViewerUser())
                    .getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        List<String> expectedUsers = asList(testParams.getUser(), testParams.getEditorUser(), testParams.getViewerUser());
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

        List<String> userEmails = initUserManagementPage()
                .addUsersToGroup(GROUP3, testParams.getUser())
                .openSpecificGroupPage(GROUP3)
                .getAllUserEmails();
        takeScreenshot(browser, "Admin-change-group-shared", getClass());
        assertTrue(userEmails.contains(testParams.getUser()));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException {
        String groupName = "New Group";
        UserManagementRestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage();
        int userGroupsCount = initUserManagementPage().getUserGroupsCount();
        userManagementPage.openSpecificGroupPage(groupName).deleteUserGroup();

        takeScreenshot(browser, "Admin-removes-user-group", getClass());
        assertEquals(userManagementPage.getUserGroupsCount(), userGroupsCount - 1);
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void changeRoleOfUsers() {
        try {
            initDashboardsPage();
            String adminText = UserRoles.ADMIN.getName();

            initUserManagementPage()
                    .changeRoleOfUsers(UserRoles.ADMIN, testParams.getEditorUser(), testParams.getViewerUser());
            Function<WebDriver, Boolean> changeRoleSuccessfully = 
                    browser -> String.format(CHANGE_ROLE_SUCCESSFUL_MESSAGE, adminText)
                        .equals(UserManagementPage.getInstance(browser).getMessageText());
            Graphene.waitGui().until(changeRoleSuccessfully);

            for (String email : asList(testParams.getEditorUser(), testParams.getViewerUser())) {
                assertEquals(UserManagementPage.getInstance(browser).getUserRole(email), adminText);
            }
        } finally {
            UserManagementPage.getInstance(browser).changeRoleOfUsers(UserRoles.EDITOR, testParams.getEditorUser());
            initUserManagementPage().changeRoleOfUsers(UserRoles.VIEWER, testParams.getViewerUser());
        }
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void checkUserCannotChangeRoleOfHimself() {
        initDashboardsPage();
        initUserManagementPage().changeRoleOfUsers(UserRoles.EDITOR, testParams.getUser());

        Function<WebDriver, Boolean> changeRoleFailed = 
                browser -> String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName())
                    .equals(UserManagementPage.getInstance(browser).getMessageText());
        Graphene.waitGui().until(changeRoleFailed);

        takeScreenshot(browser, "User-cannot-change-role-of-himself", getClass());
        assertEquals(UserManagementPage.getInstance(browser)
                .getUserRole(testParams.getUser()), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = { "inviteUserToProject" }, groups = { "userManagement" }, alwaysRun = true)
    public void checkUserCannotChangeRoleOfPendingUser() {
        initDashboardsPage();

        UserManagementPage userManagementPage = initUserManagementPage();
        UserInvitationDialog dialog = userManagementPage.openInviteUserDialog();
        dialog.invitePeople(UserRoles.ADMIN, "Invite new admin user", INVITED_EMAIL);
        waitForFragmentNotVisible(dialog);

        userManagementPage.filterUserState(UserStates.INVITED).selectUsers(INVITED_EMAIL);
        takeScreenshot(browser, "User-cannot-change-role-of-pending-user", getClass());
        assertFalse(userManagementPage.isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement", "sanity" })
    public void inviteUserToProject() throws IOException, MessagingException {
        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", imapUser);

        assertEquals(userManagementPage.getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);

        userManagementPage.filterUserState(UserStates.INVITED);
        takeScreenshot(browser, "User-appears-in-invited-stage", getClass());
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        initUserManagementPage().filterUserState(UserStates.ACTIVE);
        takeScreenshot(browser, "User-appears-in-active-stage", getClass());
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProjectWithInvalidEmail() {
        for (String email : asList("abc@gooddata.c", "<button>abc</button>@gooddata.com")) {
            checkInvitedEmail(email, String.format(INVALID_EMAIL_MESSAGE, email));
        }

        initDashboardsPage();
        initUserManagementPage().openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite existing editor user",
                testParams.getEditorUser());
        takeScreenshot(browser, "Invite-user-with-invalid-email", getClass());
        assertEquals(UserManagementPage.getInstance(browser).getMessageText(), EXSITING_USER_MESSAGE,
                "Confirm message is not correct when adding exsiting user into the project!");
    }

    @Test(dependsOnGroups = { "userManagement" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage().deactivateUsers(testParams.getUser());
        takeScreenshot(browser, "User-cannot-deactivate-himself", getClass());
        assertEquals(userManagementPage.getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(userManagementPage.getAllUserEmails().contains(testParams.getUser()));
        assertEquals(userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .waitForEmptyGroup()
                .getStateGroupMessage(), NO_DEACTIVATED_USER_MESSAGE);
    }

    @Test(dependsOnMethods = { "checkUserCannotDeactivateHimself" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void deactivateUsers() {
        List<String> emailsList = asList(testParams.getEditorUser(), testParams.getViewerUser());

        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage()
                .deactivateUsers(testParams.getEditorUser(), testParams.getViewerUser());
        takeScreenshot(browser, "Deactivate-users", getClass());
        assertEquals(userManagementPage.getMessageText(), DEACTIVATE_SUCCESSFUL_MESSAGE);
        assertFalse(userManagementPage.getAllUserEmails().containsAll(emailsList));
        assertTrue(compareCollections( userManagementPage.filterUserState(UserStates.DEACTIVATED)
                .getAllUserEmails(), emailsList));
    }

    @Test(dependsOnMethods = { "deactivateUsers" }, groups = { "activeUser", "sanity" }, alwaysRun = true)
    public void activateUsers() {
        initDashboardsPage();
        assertEquals(initUserManagementPage().filterUserState(UserStates.DEACTIVATED)
                .activateUsers(testParams.getEditorUser(), testParams.getViewerUser())
                .getMessageText(), ACTIVATE_SUCCESSFUL_MESSAGE);
        UserManagementPage.getInstance(browser).waitForEmptyGroup();

        UserManagementPage userManagementPage = initUserManagementPage();
        takeScreenshot(browser, "Users-activate-successfully", getClass());
        assertTrue(userManagementPage.getAllUserEmails().containsAll(
                asList(testParams.getEditorUser(), testParams.getViewerUser())));
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "sanity" })
    public void addNewGroup() {
        String group = "Test add group";
        checkAddingUserGroup(group);
        UserManagementPage userManagementPage = initUserManagementPage()
                .addUsersToGroup(group, testParams.getUser())
                .openSpecificGroupPage(group);

        takeScreenshot(browser, "Add-new-group", getClass());
        assertTrue(userManagementPage.getAllUserEmails().contains(testParams.getUser()));
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
        UserManagementPage userManagementPage = initUserManagementPage().cancelCreatingNewGroup(group);

        takeScreenshot(browser, "Cancel-adding-new-group", getClass());
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addExistingGroupName() {
        initDashboardsPage();
        GroupDialog groupDialog = initUserManagementPage().openGroupDialog(GroupDialog.State.CREATE);
        groupDialog.enterGroupName(GROUP1);
        takeScreenshot(browser, "Add-existing-group-name", getClass());
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

            UserManagementPage userManagementPage = initUserManagementPage();
            assertEquals(userManagementPage
                    .openSpecificGroupPage(groupName)
                    .openGroupDialog(GroupDialog.State.EDIT)
                    .verifyStateOfDialog(GroupDialog.State.EDIT)
                    .getGroupNameText(),
                    groupName);

            userManagementPage.renameUserGroup(newGroupName);
            takeScreenshot(browser, "Rename-user-group", getClass());
            assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(newGroupName));
            assertEquals(userManagementPage.getUserPageTitle(), newGroupName);
        } finally {
            UserManagementRestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroupWithExistingName() throws JSONException, IOException {
        initDashboardsPage();

        GroupDialog editGroupDialog = initUserManagementPage()
                .openSpecificGroupPage(GROUP1)
                .openGroupDialog(GroupDialog.State.EDIT)
                .enterGroupName(GROUP2);

        takeScreenshot(browser, "Rename-user-group-with-existing-name", getClass());
        assertFalse(editGroupDialog.isSubmitButtonVisible());
        assertEquals(editGroupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, GROUP2));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelRenamingUserGroup() {
        String group = "Cancel group name";
        initDashboardsPage();

        UserManagementPage userManagementPage = initUserManagementPage()
                .openSpecificGroupPage(GROUP1)
                .cancelRenamingUserGroup(group);

        takeScreenshot(browser, "Cancel-rename-user-group", getClass());
        assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(GROUP1));
        assertEquals(userManagementPage.getUserPageTitle(), GROUP1 + " " + userManagementPage.getUsersCount());
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = { "activeUser" }, groups = { "deleteGroup" })
    public void checkDeleteGroupLinkNotShownInBuiltInGroup() {
        initDashboardsPage();

        UserManagementPage userManagementPage =
                initUserManagementPage().openSpecificGroupPage(UserManagementPage.ALL_ACTIVE_USERS_GROUP);
        takeScreenshot(browser, "Delete-group-link-not-show-in-all-activate-user-group", getClass());
        assertFalse(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link is shown in default group");

        userManagementPage.openSpecificGroupPage(UserManagementPage.UNGROUPED_USERS);
        takeScreenshot(browser, "Delete-group-link-not-show-in-ungrouped-users", getClass());
        assertFalse(userManagementPage.isDeleteGroupLinkPresent(), "Delete group link is shown in default group");
    }

    @Test(dependsOnMethods = { "checkDeleteGroupLinkNotShownInBuiltInGroup" }, groups = { "deleteGroup" })
    public void checkDeleteUserGroup() {
        initDashboardsPage();

        UserManagementPage userManagementPage = initUserManagementPage();
        int customGroupCount = userManagementPage.getUserGroupsCount(); 
        userManagementPage.openSpecificGroupPage(GROUP1);

        assertTrue(UserManagementPage.getInstance(browser).isDeleteGroupLinkPresent(), "Delete group link does not show");
        DeleteGroupDialog dialog = UserManagementPage.getInstance(browser).openDeleteGroupDialog();
        assertEquals(dialog.getTitle(), "Delete group " + GROUP1, "Title of delete group dialog is wrong");
        assertEquals(dialog.getBodyContent(), DELETE_GROUP_DIALOG_CONTENT,
                "Content of delete group dialog is wrong");
        dialog.cancel();

        takeScreenshot(browser, "Group-is-not-deleted", getClass());
        assertEquals(userManagementPage.getUserGroupsCount(), customGroupCount);

        List<String> emailsList = asList(testParams.getDomainUser(), testParams.getUser(),
                testParams.getEditorUser(), testParams.getViewerUser());

        userManagementPage.openSpecificGroupPage(GROUP1).deleteUserGroup();
        takeScreenshot(browser, "Group-is-deleted", getClass());
        assertEquals(userManagementPage.getUserGroupsCount(), customGroupCount-1);
        checkUserEmailsStillAvailableAfterDeletingGroup(emailsList);
    }

    @Test(dependsOnMethods = { "checkDeleteUserGroup" }, groups = { "deleteGroup" })
    public void checkDeleteAllGroups() {
        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage();

        List<String> emailsList = asList(testParams.getDomainUser(), testParams.getUser(),
                testParams.getEditorUser(), testParams.getViewerUser());
        while (UserManagementPage.getInstance(browser).getUserGroupsCount() > 0) {
            UserManagementPage.getInstance(browser)
                    .openSpecificGroupPage(UserManagementPage.getInstance(browser).getAllUserGroups().get(0))
                    .deleteUserGroup();
        }

        takeScreenshot(browser, "All-groups-are-deleted", getClass());
        assertFalse(userManagementPage.isDefaultGroupsPresent(), "default groups are still shown");
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
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, false);
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

    private void waitForNewTabOpen(int currentTabs) {
        Function<WebDriver, Boolean> newTabOpen = browser -> BrowserUtils.getWindowHandles(browser).size() > currentTabs;
        Graphene.waitGui().until(newTabOpen);
    }
}
