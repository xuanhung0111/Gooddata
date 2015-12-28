package com.gooddata.qa.graphene.fragments.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.user.UserStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.gooddata.qa.utils.CssUtils;

public class UserManagementPage extends AbstractFragment {

    @FindBy(css = ".users-titlebar-title > h2")
    private WebElement userTitleBar;

    @FindBy(className = "rename-group-link")
    private WebElement renameGroupLink;

    @FindBy(className = "s-btn-invite_people")
    private WebElement inviteUsersButton;

    @FindBy(className = "s-btn-create_group")
    private WebElement createGroupButton;

    @FindBy(css = ".users-check-all input")
    private WebElement checkAllUserEmailCheckbox;

    @FindBy(css = ".list.users-list")
    private UsersTable usersTable;

    @FindBy(css = ".sidebar-column a.active")
    private List<WebElement> sidebarActiveLinks;

    @FindBy(css = ".sidebar-groups .user-filter")
    private List<WebElement> userGroupFilters;

    @FindBy(className = "users-user-email")
    private List<WebElement> userEmailsList;

    @FindBy(className = "users-user-name")
    private List<WebElement> usernames;

    @FindBy(className = "users-change-group")
    private WebElement changeGroupButton;

    @FindBy(className = "s-btn-apply")
    private WebElement changeGroupApplyButton;

    @FindBy(className = "users-selected-count")
    private WebElement selectedUserCount;

    @FindBy(className = "users-activate")
    private WebElement activateUserButton;

    @FindBy(className = "users-deactivate")
    private WebElement deactivateUserButton;

    @FindBy(className = "s-btn-start_adding_users")
    private WebElement startAddingUserButton;

    @FindBy(css = ".gd-state-text h2")
    private WebElement stateMessage;

    @FindBy(className = "users-content-column")
    private WebElement userContentColumn;

    private static final By DELETE_GROUP_LINK = By.className("s-btn-delete_group");
    private static final By BY_CHANGE_ROLE_BUTTON = By.className("users-change-role");
    private static final By BY_MESSAGE_TEXT = By.className("gd-message-text");
    private static final By BY_EMPTY_GROUP = By.className("list-state");

    private static final String GROUP_NAME_CHECKBOX_CSS = ".s-${groupName} .input-checkbox";
    private static final String GROUP_LINK_XPATH =
            "//span[contains(@class, 'menu-item-title') and (text() = '${groupName}')]";
    
    public static final String ALL_ACTIVE_USERS_GROUP = "All active users";
    public static final String UNGROUPED_USERS = "Ungrouped users";

    public UserInvitationDialog openInviteUserDialog() {
        waitForElementVisible(inviteUsersButton).click();
        return Graphene.createPageFragment(UserInvitationDialog.class,
                waitForElementVisible(UserInvitationDialog.LOCATOR, browser));
    }

    public GroupDialog openGroupDialog(GroupDialog.State state) {
        waitForElementVisible(state == GroupDialog.State.CREATE ? createGroupButton : renameGroupLink).click();
        return Graphene.createPageFragment(GroupDialog.class,
                waitForElementVisible(GroupDialog.LOCATOR, browser));
    }

    public void createNewGroup(String name) {
        openGroupDialog(GroupDialog.State.CREATE).submitDialogGroup(name);
    }

    public void renameUserGroup(String newName) {
        openGroupDialog(GroupDialog.State.EDIT).submitDialogGroup(newName);
    }

    public void deleteUserGroup() {
        openDeleteGroupDialog().submit();
    }

    public void cancelDeleteUserGroup() {
        openDeleteGroupDialog().cancel();
    }
    
    public boolean isDeleteGroupLinkPresent() {
        waitForElementVisible(userTitleBar);
        return browser.findElements(DELETE_GROUP_LINK).size() > 0;
    }

    public DeleteGroupDialog openDeleteGroupDialog() {
        waitForElementVisible(DELETE_GROUP_LINK, browser).click();
        return Graphene.createPageFragment(DeleteGroupDialog.class,
                waitForElementVisible(DeleteGroupDialog.LOCATOR, browser));
    }

    public void cancelCreatingNewGroup(String name) {
        openGroupDialog(GroupDialog.State.CREATE).cancelSubmitDialogGroup(name);
    }

    public void cancelRenamingUserGroup(String newName) {
        openGroupDialog(GroupDialog.State.EDIT).cancelSubmitDialogGroup(newName);
    }

    public UserManagementPage changeRoleOfUsers(UserRoles role, String... emails) {
        deselectAllUserEmails();
        selectUsers(emails);
        waitForElementVisible(BY_CHANGE_ROLE_BUTTON, browser).click();
        Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("ember-list-container"), browser)).selectItem(role.getName());
        return this;
    }

    public boolean isChangeRoleButtonPresent() {
        return browser.findElements(BY_CHANGE_ROLE_BUTTON).size() > 0;
    }

    public UserManagementPage addUsersToGroup(String group, String... emails) {
        return changeGroupOfUsers(true, group, emails);
    }

    public UserManagementPage removeUsersFromGroup(String group, String... emails) {
        return changeGroupOfUsers(false, group, emails);
    }

    public UserManagementPage deactivateUsers(String... emails) {
        selectUsers(emails);
        waitForElementVisible(deactivateUserButton).click();
        return this;
    }

    public UserManagementPage activateUsers(String... emails) {
        selectUsers(emails);
        waitForElementVisible(activateUserButton).click();
        return this;
    }

    public int getUsersCount() {
        return Integer.parseInt(waitForElementVisible(By.className("users-count"), browser).getText());
    }

    public UserManagementPage selectUsers(String... emails) {
        waitForFragmentVisible(usersTable).selectUsers(emails);
        return this;
    }

    public String getSelectedUsersCount() {
        return waitForElementVisible(selectedUserCount).getText().trim();
    }

    public UserManagementPage openSpecificGroupPage(String groupName) {
        waitForElementVisible(By.xpath(GROUP_LINK_XPATH.replace("${groupName}", groupName)), browser).click();
        waitForElementVisible(userContentColumn);
        browser.navigate().refresh();
        waitForElementVisible(this.getRoot());
        return this;
    }

    public boolean isDefaultGroupsPresent() {
        waitForElementVisible(By.className("sidebar-subtitle"),browser);
        return browser.findElements(
                By.xpath(GROUP_LINK_XPATH.replace("${groupName}", ALL_ACTIVE_USERS_GROUP))).size() > 0 ||
                browser.findElements(
                        By.xpath(GROUP_LINK_XPATH.replace("${groupName}", UNGROUPED_USERS))).size() > 0;
    }

    public UserManagementPage filterUserState(UserStates state) {
        waitForElementVisible(By.className(state.getClassName()), browser).click();
        return this;
    }

    public String getUserPageTitle() {
        return waitForElementVisible(userTitleBar).getText().trim();
    }

    public List<String> getAllUserEmails() {
        List<String> allUserEmails = new ArrayList<String>();

        waitForFragmentVisible(usersTable);
        for (WebElement email : userEmailsList) {
            allUserEmails.add(waitForElementVisible(email).getText().trim());
        }

        return allUserEmails;
    }

    public List<String> getAllUsernames() {
        List<String> allUsernames = new ArrayList<String>();

        waitForFragmentVisible(usersTable);
        for (WebElement name : usernames) {
            allUsernames.add(waitForElementVisible(name).getText().trim());
        }

        return allUsernames;
    }

    public List<String> getAllUserGroups() {
        List<String> userGroups = new ArrayList<String>();
        waitForCollectionIsNotEmpty(userGroupFilters);
        for (WebElement e : userGroupFilters) {
            userGroups.add(e.findElement(By.className("menu-item-title")).getText().trim());
        }
        return userGroups;
    }

    public int getUserGroupsCount() {
        if (!isDefaultGroupsPresent()) {
            return 0;
        }
        waitForElementVisible(By.className("sidebar-groups"), browser);
        return userGroupFilters.size();
    }

    public String getMessageText() {
        return waitForElementVisible(BY_MESSAGE_TEXT, browser).getText().trim();
    }

    public void waitForEmptyGroup() {
        waitForElementVisible(BY_EMPTY_GROUP, browser);
    }

    public String getStateGroupMessage() {
        return waitForElementVisible(stateMessage).getText().trim();
    }

    public void startAddingUser() {
        waitForElementVisible(startAddingUserButton).click();
    }

    public String getUserRole(String email) {
        return waitForFragmentVisible(usersTable).getUserRole(email);
    }

    public List<String> getAllSidebarActiveLinks() {
        List<String> activeLinks = new ArrayList<String>();
        waitForCollectionIsNotEmpty(sidebarActiveLinks);
        for (WebElement e : sidebarActiveLinks) {
            activeLinks.add(e.getText().trim());
        }
        return activeLinks;
    }

    private UserManagementPage changeGroupOfUsers(boolean isSelect, String group, String... emails) {
        // Deselect all selected users of current page first
        deselectAllUserEmails();
        selectUsers(emails);

        waitForElementVisible(changeGroupButton).click();
        selectUserGroup(group, isSelect);
        waitForElementVisible(changeGroupApplyButton).click();

        return this;
    }

    private void selectAllUserEmails() {
        if (!waitForElementVisible(checkAllUserEmailCheckbox).isSelected()) {
            checkAllUserEmailCheckbox.click();
        }
    }

    private void deselectAllUserEmails() {
        // Select all user emails first
        selectAllUserEmails();
        // After that, click on check-all button to deselect all users.
        checkAllUserEmailCheckbox.click();
    }

    private void selectUserGroup(String groupName, boolean isSelect) {
        WebElement groupNameCheckBox =
                waitForElementVisible(By.cssSelector(GROUP_NAME_CHECKBOX_CSS.replace("${groupName}",
                                CssUtils.simplifyText(groupName))), browser);

        if (groupNameCheckBox.isSelected() ^ isSelect) {
            groupNameCheckBox.click();
        }
    }
}
