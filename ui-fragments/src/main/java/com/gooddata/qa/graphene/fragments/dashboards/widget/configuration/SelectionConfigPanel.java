package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SelectionConfigPanel extends AbstractFragment {
    @FindBy(xpath = "//div[./label[.='One value']]/input")
    private WebElement oneValue;

    @FindBy(xpath = "//div[./label[.='Multiple values']]/input")
    private WebElement multipleValue;

    private final static String HIDE_DATE_RANGE_CHECKBOX_CSS = ".selectionModeRow input";

    public void changeSelectionToOneValue() {
        waitForElementVisible(oneValue).click();
    }

    public void changeSelectionToMultipleValues() {
        waitForElementVisible(multipleValue).click();
    }

    public boolean isHideDateRangeSelected() {
        return getHideDateRangeCheckbox().isSelected();
    }

    public boolean isHideDateRangeSelectionVisible() {
        return isElementVisible(By.cssSelector(HIDE_DATE_RANGE_CHECKBOX_CSS), getRoot());
    }

    public SelectionConfigPanel setHideDateRange(boolean enabled) {
        if (isHideDateRangeSelected() != enabled)
            getHideDateRangeCheckbox().click();

        return this;
    }

    private WebElement getHideDateRangeCheckbox() {
        return waitForElementVisible(By.cssSelector(HIDE_DATE_RANGE_CHECKBOX_CSS), getRoot());
    }
}
