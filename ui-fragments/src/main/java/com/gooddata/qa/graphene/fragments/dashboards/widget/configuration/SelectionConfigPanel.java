package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class SelectionConfigPanel extends AbstractFragment {
    @FindBy(xpath = "//div[./label[.='One value']]/input")
    private WebElement oneValue;

    @FindBy(xpath = "//div[./label[.='Multiple values']]/input")
    private WebElement multipleValue;

    @FindBy(css = ".selectionModeRow input")
    private WebElement hideFromToCheckBox;

    @FindBy(css = ".disabledTimeInterval input")
    private WebElement hideCalendarPickerCheckBox;

    public void changeSelectionToOneValue() {
        waitForElementVisible(oneValue).click();
    }

    public void changeSelectionToMultipleValues() {
        waitForElementVisible(multipleValue).click();
    }

    public boolean isHideFromToOptionSelected() {
        return waitForElementVisible(hideFromToCheckBox).isSelected();
    }

    public boolean isHideFromToOptionVisible() {
        return isElementVisible(hideFromToCheckBox);
    }

    public boolean isHideFromToOptionEnabled() {
        return hideFromToCheckBox.isEnabled();
    }

    public SelectionConfigPanel setHideFromToOption(boolean enabled) {
        if (isHideFromToOptionSelected() != enabled)
            waitForElementVisible(hideFromToCheckBox).click();

        return this;
    }

    public boolean isHideCalendarOptionSelected() {
        return hideCalendarPickerCheckBox.isSelected();
    }

    public boolean isHideCalendarOptionEnabled() {
        return hideCalendarPickerCheckBox.isEnabled();
    }

    public SelectionConfigPanel setHideCalendarOption(boolean enabled) {
        if (waitForElementVisible(hideCalendarPickerCheckBox).isSelected() != enabled)
            hideCalendarPickerCheckBox.click();

        return this;
    }
}
