package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class PermissionsDialog extends AbstractFragment {

    @FindBy(css = ".s-btn-set_permissions")
    private WebElement setPermissions;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancel;

    @FindBy(css = "input[type=checkbox]")
    private WebElement visibilityCheckbox;

    @FindBy(css = "input[name=settings-lock-radio][value=admin]")
    private WebElement lockAdminRadio;

    @FindBy(css = "input[name=settings-lock-radio][value=all]")
    private WebElement lockAllRadio;

    public WebElement getLockAdminRadio() {
        return lockAdminRadio;
    }

    public WebElement getLockAllRadio() {
        return lockAllRadio;
    }

    public WebElement getVisibilityCheckbox() {
        return visibilityCheckbox;
    }

    public void lock() {
        waitForElementVisible(lockAdminRadio);
        lockAdminRadio.click();
    }

    public void unlock() {
        waitForElementVisible(lockAllRadio);
        lockAllRadio.click();
    }

    public void publish(boolean listed) {
        waitForElementVisible(visibilityCheckbox);
        if ((listed && !isListedChecked()) || (!listed && isListedChecked())) visibilityCheckbox.click();
    }

    private boolean isListedChecked() {
        return visibilityCheckbox.getAttribute("checked") != null;
    }

    public void submit() {
        waitForElementVisible(setPermissions).click();
    }

    public void cancel() {
        waitForElementVisible(cancel).click();
    }
}