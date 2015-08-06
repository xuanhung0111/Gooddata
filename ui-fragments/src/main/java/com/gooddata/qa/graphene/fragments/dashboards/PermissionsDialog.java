package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class PermissionsDialog extends AbstractFragment {

    private static final By DONE_BUTTON_CSS_SELECTOR = By.cssSelector(".s-btn-done");
    private static final By GRANTEE_EMAIL_CSS_SELECTOR = By.cssSelector(".grantee-email");
    private static final By GRANTEE_NAME_CSS_SELECTOR = By.cssSelector(".grantee-name");
    private static final By ADDED_GRANTEE_INFO_CSS_SELECTOR = By.cssSelector(".grantee-info");
    private static final By ADDED_GRANTEE_DELETE_CSS_SELECTOR = By.cssSelector(".ss-delete");
    private static final By ADDED_GRANTEE_UNDO_CSS_SELECTOR = By.cssSelector(".grantee-revoke>a");
    private static final By GRANTEE_LIST_CONTAINER_SELECTOR = By.cssSelector(".ember-list-container .grantee");
    private static final By LOCK_OPTIONS_SELECTOR = By.cssSelector("input[name=settings-lock-radio]");
    public static final By GRANTEES_PANEL = By.cssSelector(".grantees");
    public static final By ALERT_INFOBOX_CSS_SELECTOR = By.cssSelector(".ss-alert");

    @FindBy(css = ".submit-button")
    private WebElement submitButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancel;

    @FindBy(css = ".visibility-button")
    private WebElement visibilityButton;

    @FindBy(css = "input[name=settings-lock-radio][value=admin]")
    private WebElement lockAdminRadio;

    @FindBy(css = "input[name=settings-lock-radio][value=all]")
    private WebElement lockAllRadio;

    @FindBy(css = ".visibility-options")
    private WebElement visibilityOptionsContainer;

    @FindBy(xpath = "//div[@id='gd-overlays']//div[contains(@class,'s-everyone_can_access')]/div")
    private WebElement everyOneCanAccessChoose;
    
    @FindBy(xpath = "//div[@id='gd-overlays']//div[contains(@class,'s-specific_users_can_access')]/div")
    private WebElement specificUsersAccessChoose;

    @FindBy(css = ".permissionDialog-addGranteesButton:not(.disabled)")
    private WebElement addGranteesButton;

    @FindBy(xpath = "//div[@id='gd-overlays']//div[contains(@class,'grantee-candidates-dialog')]")
    private AddGranteesDialog addGranteesDialog;

    @FindBy(css = ".searchfield-input")
    private WebElement searchForGranteeInput;

    public WebElement getLockAdminRadio() {
        return lockAdminRadio;
    }

    public WebElement getLockAllRadio() {
        return lockAllRadio;
    }
    
    public boolean isLockOptionDisplayed() {
        return browser.findElements(LOCK_OPTIONS_SELECTOR).size() == 2;
    }

    public WebElement getVisibilityButton() {
        return visibilityButton;
    }

    public void lock() {
        lockUnlockAction(lockAdminRadio);
    }

    public void unlock() {
        lockUnlockAction(lockAllRadio);
    }

    private void lockUnlockAction(final WebElement radioElement) {
        waitForElementVisible(radioElement);
        radioElement.click();
    }

    /**
     * @param publishType  {@link com.gooddata.qa.graphene.enums.PublishType#EVERYONE_CAN_ACCESS} - publish to everyone,
     * {@link com.gooddata.qa.graphene.enums.PublishType#SPECIFIC_USERS_CAN_ACCESS}  - 
     * publish to specific user (by default owner + others can be added in different dialog)
     */
    public void publish(PublishType publishType) {
        openVisibilityPanel();
        switch (publishType) {
            case EVERYONE_CAN_ACCESS:
                submitEveryOneCanAccess();
                break;
            case SPECIFIC_USERS_CAN_ACCESS:
                submitSpecificUsersAccess();
                break;
            default:
                throw new IllegalArgumentException("Unexpected publish type: " + publishType);
        }
    }

    public AddGranteesDialog openAddGranteePanel() throws InterruptedException {
        waitForElementVisible(addGranteesButton).click();
        return  waitForFragmentVisible(addGranteesDialog);
    }

    public void removeUser(final String login) {
        for (WebElement element : getAddedGrantees()) {
            if (element.findElements(GRANTEE_EMAIL_CSS_SELECTOR).size() != 0 && 
                    login.equals(element.findElement(GRANTEE_EMAIL_CSS_SELECTOR).getText().trim())) {
                element.findElement(ADDED_GRANTEE_DELETE_CSS_SELECTOR).click();
                return;
            }
        }
    }

    public void removeGroup(final String groupName) {
        for (WebElement element : getAddedGrantees()) {
            if (groupName.equals(element.findElement(GRANTEE_NAME_CSS_SELECTOR).getText().trim())) {
                element.findElement(ADDED_GRANTEE_DELETE_CSS_SELECTOR).click();
                return;
            }
        }
    }

    public boolean checkCannotRemoveOwner() {
        for (WebElement element : getAddedGrantees()) {
            if ("Owner".equals(element.findElement(ADDED_GRANTEE_INFO_CSS_SELECTOR).getText().trim())) {
                return element.findElements(ADDED_GRANTEE_DELETE_CSS_SELECTOR).size() == 0;
            }
        }
        return true;
    }

    public void undoRemoveUser(final String login) {
        for (WebElement element : getAddedGrantees()) {
            if (element.findElements(GRANTEE_EMAIL_CSS_SELECTOR).size() != 0 && 
                    login.equals(element.findElement(GRANTEE_EMAIL_CSS_SELECTOR).getText().trim())) {
                element.findElement(ADDED_GRANTEE_UNDO_CSS_SELECTOR).click();
                return;
            }
        }
    }

    public void undoRemoveGroup(final String groupName) {
        for (WebElement element : getAddedGrantees()) {
            if (groupName.equals(element.findElement(GRANTEE_NAME_CSS_SELECTOR).getText().trim())) {
                element.findElement(ADDED_GRANTEE_UNDO_CSS_SELECTOR).click();
                return;
            }
        }
    }

    /**
     * Expects that there is always some element in the grantees list (the owner)
     * @return list of grantees with granted visibility to this md object
     */
    public List<WebElement> getAddedGrantees() {
        final WebElement granteesPanel = getGranteesPanel();
        waitForCollectionIsNotEmpty(granteesPanel.findElements(GRANTEE_LIST_CONTAINER_SELECTOR));
        return granteesPanel.findElements(GRANTEE_LIST_CONTAINER_SELECTOR);
    }

    public WebElement getGranteesPanel() {
        return waitForElementVisible(root.findElement(GRANTEES_PANEL));
    }

    private void submitEveryOneCanAccess() {
        waitForElementVisible(everyOneCanAccessChoose).click();
    }

    private void submitSpecificUsersAccess() {
        waitForElementVisible(specificUsersAccessChoose).click();
    }

    public void openVisibilityPanel() {
        waitForElementVisible(visibilityButton).click();
    }

    public void submit() {
        waitForElementVisible(submitButton).click();
        waitForElementNotVisible(getRoot());
    }

    public void cancel() {
        waitForElementVisible(cancel).click();
    }

    public void done() {
        waitForElementVisible(DONE_BUTTON_CSS_SELECTOR, browser).click();
    }

    public boolean isDoneButtonVisible() {
        return browser.findElements(DONE_BUTTON_CSS_SELECTOR).size() > 0;
    }

    public String getTitleOfSubmitButton() {
        return waitForElementVisible(submitButton).getText();
    }
}
