package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class PermissionsDialog extends AbstractFragment {

    private static final By GRANTEE_EMAIL_CSS_SELECTOR = By.cssSelector(".grantee-email");
    private static final By ADDED_GRANTEE_DELETE_CSS_SELECTOR = By.cssSelector(".ss-delete");
    private static final By GRANTEE_LIST_CONTAINER_SELECTOR = By.cssSelector(".ember-list-container .grantee");
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

    @FindBy(css = ".permissionDialog-addGranteesButton")
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
     * {@link com.gooddata.qa.graphene.enums.PublishType#SPECIFIC_USERS_CAN_ACCESS}  - publish to specific user (by default owner + others can be added in different dialog)
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

    public AddGranteesDialog openAddGranteePanel() {
        waitForElementVisible(addGranteesButton).click();
        return  waitForFragmentVisible(addGranteesDialog);
    }

    public void removeUser(final String login) {
        for (WebElement element : getAddedGrantees()) {
            if (login.equals(element.findElement(GRANTEE_EMAIL_CSS_SELECTOR).getText().trim())) {
                element.findElement(ADDED_GRANTEE_DELETE_CSS_SELECTOR).click();
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

    public void submitEveryOneCanAccess() {
        waitForElementVisible(everyOneCanAccessChoose).click();
    }

    public void submitSpecificUsersAccess() {
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
}