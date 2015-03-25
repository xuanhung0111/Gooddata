package com.gooddata.qa.graphene.fragments.indigo.user;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.UserStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;

public class UserManagementPage extends AbstractFragment {

    @FindBy(className = "s-btn-invite_people")
    private WebElement inviteUsersButton;

    @FindBy(className = "users-filter-state")
    private WebElement usersFilterButton;

    @FindBy(css = ".list.users-list")
    private UsersTable usersTable;

    private static final By BY_SELECTED_USERS = By.className("users-selected-count");
    private static final By BY_CHANGE_ROLE_BUTTON = By.className("users-change-role");
    private static final By BY_USER_DEACTIVATE_BUTTON = By.className("users-deactivate");

    public UserInvitationDialog openInviteUserDialog() {
        waitForElementVisible(inviteUsersButton).click();
        return Graphene.createPageFragment(UserInvitationDialog.class,
                waitForElementVisible(By.className("invitationDialog"), browser));
    }

    public UserManagementPage filterUserState(UserStates state) {
        waitForElementVisible(usersFilterButton).click();
        Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("users-filter-dropdown"), browser))
                    .selectItem(state.getText());
        return this;
    }

    public UserManagementPage changeRoleOfUsers(UserRoles role, String... emails) {
        selectUsers(emails);
        waitForElementVisible(BY_CHANGE_ROLE_BUTTON, browser).click();
        Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("users-roles-dropdown"), browser))
                    .selectItem(role.getText());
        return this;
    }

    public UserManagementPage deactivateUsers(String... emails) {
        selectUsers(emails);
        waitForElementVisible(BY_USER_DEACTIVATE_BUTTON, browser).click();
        return this;
    }

    public int getUsersCount() {
        return Integer.parseInt(waitForElementVisible(By.className("users-count"), browser).getText());
    }

    public UserManagementPage selectUsers(String... emails) {
        waitForFragmentVisible(usersTable).selectUsers(emails);
        assertEquals(waitForElementVisible(BY_SELECTED_USERS, browser).getText().trim(),
                emails.length + " selected user" + (emails.length > 1 ? "s" : ""));
        return this;
    }
}
