package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

/**
 * Fragment represents setting dialog. 
 * That dialog is the one when we in edit mode, click Actions > Setting...
 *
 */
public class DashboardSettingsDialog extends AbstractFragment{

    @FindBy(xpath = "//section[.//span[text()='Saved Views'] and contains(@class,'rememberFilters')]//input[contains(@class,'ember-checkbox')]")
    private WebElement savedViewsCheckbox;

    @FindBy(xpath = "//section[.//span[text()='Filters'] and contains(@class,'rememberFilters')]//input[contains(@class,'ember-checkbox')]")
    private WebElement filtersCheckbox;

    @FindBy(xpath = "//footer[@class='buttons']//button[contains(@class,'s-btn-save')]")
    private WebElement saveButton;

    @FindBy(xpath = "//footer[@class='buttons']//button[contains(@class,'s-btn-cancel')]")
    private WebElement cancelButton;

    /**
     * Use to turn on or off saved view mode when project is enable
     * saved view mode in gray page
     * 
     * @param   on 
     */
    public void turnSavedViewOption(boolean on) {
        if (!(on ^ waitForElementVisible(savedViewsCheckbox).isSelected())) {
            System.out.printf("Saved filter is alreay %s!\n", on ? "enabled" : "disabled");
        } else {
            savedViewsCheckbox.click();
        }
        waitForElementVisible(saveButton).click();
        waitForElementNotPresent(this.getRoot());
    }

    public WebElement getSavedViewsCheckbox() {
        return savedViewsCheckbox;
    }

    public WebElement getSaveButton() {
        return saveButton;
    }

    public WebElement getCancelButton() {
        return cancelButton;
    }

    public WebElement getFiltersCheckbox() {
        return filtersCheckbox;
    }
}
