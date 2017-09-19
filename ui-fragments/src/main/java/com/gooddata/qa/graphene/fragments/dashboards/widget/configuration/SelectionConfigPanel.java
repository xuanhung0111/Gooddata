package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

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

    public boolean isHideDateRangeSelected() {
        return waitForElementVisible(hideFromToCheckBox).isSelected();
    }

    public boolean isHideDateRangeSelectionVisible() {
        return isElementVisible(hideFromToCheckBox);
    }

    public SelectionConfigPanel setHideDateRange(boolean enabled) {
        if (isHideDateRangeSelected() != enabled)
            waitForElementVisible(hideFromToCheckBox).click();

        return this;
    }

    public SelectionConfigPanel setHideDatePicker(boolean enabled) {
        if (waitForElementVisible(hideCalendarPickerCheckBox).isSelected() != enabled)
            hideCalendarPickerCheckBox.click();

        return this;
    }
}
