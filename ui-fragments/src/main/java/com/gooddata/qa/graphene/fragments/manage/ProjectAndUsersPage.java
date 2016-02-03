package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.fragments.profile.UserProfilePage.USER_PROFILE_PAGE_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

public class ProjectAndUsersPage extends AbstractFragment {

    @FindBy(xpath = "//span[@class='deleteProject']/button")
    private WebElement deleteProjectButton;

    @FindBy(xpath = "//form/div/span/button[text()='Delete']")
    private WebElement deleteProjectDialogButton;

    @FindBy(className = "project-page-manage-link")
    private WebElement userManagementLink;
    
    @FindBy(css = ".leaveProject .s-btn-leave")
    private WebElement leaveProjectButton;

    @FindBy(css = ".projectNameIpe")
    private WebElement projectNameTag;

    @FindBy(css = ".item.user")
    private List<WebElement> users;

    @FindBy(css = ".s-btn-invite_users")
    private WebElement inviteUserButton;

    @FindBy(css = ".s-filterActive")
    private WebElement filterActiveButton;

    @FindBy(css = ".s-filterDisabled")
    private WebElement filterDeactivatedButton;

    private static final By BY_PROJECTS_LIST = By.className("userProjects");
    private static final By BY_LEAVE_PROJECT_DIALOG_BUTTON = By.cssSelector("form .s-btn-leave");
    private static final By PROJECT_NAME_INPUT_LOCATOR = By.cssSelector(".ipeEditor");
    private static final By SAVE_BUTTON_LOCATOR = By.cssSelector(".s-ipeSaveButton");
    private static final By CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR = By
            .cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .s-btn-cancel");

    private static final By EMAILING_DASHBOARDS_TAB_LOCATOR = By.cssSelector(".s-menu-schedulePage");

    public void deteleProject() {
        waitForElementVisible(deleteProjectButton).click();
        waitForElementVisible(deleteProjectDialogButton).click();
        //redirect to projects page
        waitForElementVisible(BY_PROJECTS_LIST, browser);
        System.out.println("Project deleted...");
    }

    public void openUserManagementPage() {
        waitForElementVisible(userManagementLink).click();
    }
    
    public void leaveProject() {
        waitForElementVisible(leaveProjectButton).click();
        waitForElementVisible(BY_LEAVE_PROJECT_DIALOG_BUTTON, browser).click();
    }

    public void renameProject(String name) {
        waitForElementVisible(projectNameTag).click();
        WebElement projectNameInput = waitForElementVisible(PROJECT_NAME_INPUT_LOCATOR, browser);
        projectNameInput.clear();
        projectNameInput.sendKeys(name);
        waitForElementVisible(SAVE_BUTTON_LOCATOR, browser).click();
    }

    public String getProjectName() {
        return waitForElementVisible(projectNameTag).getText();
    }

    public boolean isDeleteButtonEnabled() {
        return !waitForElementVisible(deleteProjectButton)
                .getAttribute("class")
                .contains("button-disabled");
    }

    public void tryDeleteProjectButDiscard() {
        waitForElementVisible(deleteProjectButton).click();
        waitForElementVisible(CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR, browser).click();
    }

    public String inviteUsersWithBlankMessage(ImapClient imapClient, String emailSubject,
            UserRoles role, String...emails) throws MessagingException, IOException {
        return inviteUsers(imapClient, emailSubject, role, null, emails);
    }

    public boolean isEmailingDashboardsTabDisplayed() {
        return isElementPresent(EMAILING_DASHBOARDS_TAB_LOCATOR, browser);
    }

    public UserProfilePage openUserProfile(final String userEmail) {
        users.stream()
                .filter(e -> e.findElement(By.cssSelector(".email")).getText().equals(userEmail))
                .map(e -> e.findElement(By.cssSelector(".name")))
                .findFirst()
                .get()
                .click();
        return Graphene.createPageFragment(UserProfilePage.class,
                waitForElementVisible(USER_PROFILE_PAGE_LOCATOR, browser));
    }

    public void clickInviteUserButton() {
        waitForElementVisible(inviteUserButton).click();
    }

    public void disableUser(final String userEmail) {
        openActiveUserTab();
        int numberOfUsers = getUsersCount();
        users.stream()
                .filter(e -> e.findElement(By.cssSelector(".email")).getText().equals(userEmail))
                .map(e -> e.findElement(By.tagName("button")))
                .findFirst()
                .get()
                .click();
        Predicate<WebDriver> predicate = input -> getUsersCount() < numberOfUsers;
        Graphene.waitGui().until(predicate);
    }

    public boolean isUserDisplayedInList(final String userEmail) {
        return users.stream()
                .map(e -> e.findElement(By.cssSelector(".email")))
                .filter(e -> e.getText().equals(userEmail))
                .findFirst()
                .isPresent();
    }

    public void openDeactivatedUserTab() {
        waitForElementVisible(filterDeactivatedButton).click();
    }

    private String inviteUsers(ImapClient imapClient, String emailSubject,
            UserRoles role, String message, String...emails) throws MessagingException, IOException {
        waitForElementVisible(inviteUserButton).click();
        InviteUserDialog inviteUserDialog = Graphene.createPageFragment(InviteUserDialog.class,
                waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser));
        return inviteUserDialog.inviteUsers(imapClient, emailSubject, role, message, emails);
    }

    private int getUsersCount() {
        return users.size();
    }

    private void openActiveUserTab() {
        waitForElementVisible(filterActiveButton).click();
    }
}
