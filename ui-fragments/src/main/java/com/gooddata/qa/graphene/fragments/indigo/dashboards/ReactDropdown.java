package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.cssSelector;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class ReactDropdown extends ReactDropdownParent {

    public ReactDropdown selectByName(String name) {
        searchByName(name);
        getElementByName(name).click();

        // wait until the selection is made and propagated to the button title
        By buttonTitle = cssSelector(getDropdownButtonCssSelector() + ".s-" + simplifyText(name));
        waitForElementVisible(buttonTitle, this.getRoot());

        return this;
    }

    public String getSelection() {
        return getDropdownButton().getText();
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();

        String itemSelector = getDropdownCssSelector() + " " + getListItemCssSelector();
        return getElementTexts(cssSelector(itemSelector), browser);
    }

    public void searchByName(String name) {
        ensureDropdownOpen();

        if (this.hasSearchField()) {
            this.searchForText(name);
        }
    }

    public void waitForItem(String name) {
        searchByName(name);
        waitForElementPresent(cssSelector(getDropdownCssSelector() + " .s-" + simplifyText(name)), browser);
    }

    protected WebElement getElementByName(String name) {
        return waitForElementVisible(cssSelector(getDropdownCssSelector() + " .s-" + simplifyText(name)), browser);
    }
}
