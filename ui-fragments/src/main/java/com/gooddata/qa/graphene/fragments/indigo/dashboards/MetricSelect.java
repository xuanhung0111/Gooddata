package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class MetricSelect extends AbstractReactDropDown {

    private static final By TOOLTIP_LOCATOR = By.className("bubble-content");

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .metrics-list";
    }

    public boolean isNameShortened(String name) {
        return getElementByName(name)
                .findElement(By.className("shortened"))
                .getAttribute("class")
                .contains("is-shortened");
    }

    public String getTooltip(String name) {
        new Actions(browser).moveToElement(getElementByName(name)).perform();
        return waitForElementVisible(TOOLTIP_LOCATOR, browser).getText();
    }
}
