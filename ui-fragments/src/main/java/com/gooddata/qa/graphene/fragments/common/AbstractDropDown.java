package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.WebElement;

public abstract class AbstractDropDown extends AbstractPicker {

    /**
     * Get css selector for drop down panel
     * @return css selector for drop down panel
     */
    protected abstract String getDropdownCssSelector();

    /**
     * Get css selector for button which is used to open drop down
     * @return css selector for button
     */
    protected abstract String getDropdownButtonCssSelector();

    @Override
    protected WebElement getPanelRoot() {
        return waitForElementVisible(cssSelector(getDropdownCssSelector()), browser);
    }
}
