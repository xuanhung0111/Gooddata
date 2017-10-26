package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SaveAsDialog extends AbstractFragment {
    
    @FindBy(css = ".gdc-input-wrapper>input")
    private WebElement dashboardNameInput;

    @FindBy(css = ".rememberFilters input")
    private WebElement savedViewsCheckbox;

    @FindBy(css = ".sharing input[value='private']")
    private WebElement onlyAuthorRadio;
    
    @FindBy(css = ".sharing input[value='no change']")
    private WebElement existingPermissionsRadio;
    
    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;
    
    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    public void saveAs(String dashboardName, boolean isSavedViews, PermissionType permissionType) {
        setDashboardName(dashboardName);
        setSavedViews(isSavedViews);
        setPermissionType(permissionType);
        submit();
    }
    
    public enum PermissionType {
        ONLY_YOU, USE_EXISTING_PERMISSIONS
    }

    public void setDashboardName(String dashboardName) {
        if (dashboardName.isEmpty()) {
            return;
        }
        waitForElementVisible(dashboardNameInput).clear();
        dashboardNameInput.sendKeys(dashboardName);
    }

    public String getDashboardName() {
        return waitForElementVisible(dashboardNameInput).getText();
    }
    
    public void setSavedViews(boolean isSavedViews) {
        if (!waitForElementVisible(savedViewsCheckbox).isSelected() && isSavedViews)
            savedViewsCheckbox.click();
    }
    
    public void setPermissionType(PermissionType permissionType) {
        waitForElementVisible(existingPermissionsRadio);
        switch (permissionType) {
            case USE_EXISTING_PERMISSIONS:
                existingPermissionsRadio.click();
                break;
            case ONLY_YOU:
                onlyAuthorRadio.click();
        }
    }
    
    public void submit() {
        waitForElementVisible(saveButton).click();
    }
    
    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }
}
