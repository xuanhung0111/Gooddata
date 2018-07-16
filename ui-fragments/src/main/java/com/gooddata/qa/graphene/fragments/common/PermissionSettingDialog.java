package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

public class PermissionSettingDialog extends AbstractFragment {

    private static final By LOCATOR = className("s-permissionSettingsDialog");
    private static final By VISIBILITY_CHECKBOX_LOCATOR = id("settings-visibility");

    @FindBy(className = "submit-button")
    private WebElement saveButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(id = "settings-visibility")
    private WebElement visibilityCheckBox;

    public static PermissionSettingDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PermissionSettingDialog.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public PermissionSettingDialog setVisibility(boolean visible) {
        WebElement visibleCheckbox = waitForElementVisible(visibilityCheckBox);
        if (visible != visibleCheckbox.isSelected()) {
            visibleCheckbox.click();
        }
        return this;
    }

    public PermissionSettingDialog setEditingPermission(PermissionType permissionType) {
        getRoot().findElements(className("input-radio")).stream()
                .filter(element -> element.getAttribute("value").equals(permissionType.getPermission()))
                .findFirst()
                .get()
                .click();
        return this;
    }

    public void save() {
        waitForElementVisible(saveButton).click();
        waitForFragmentNotVisible(this);
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public enum PermissionType {
        ALL("all"),
        ADMIN("admin");

        private String permission;

        PermissionType(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return this.permission;
        }
    }
}
