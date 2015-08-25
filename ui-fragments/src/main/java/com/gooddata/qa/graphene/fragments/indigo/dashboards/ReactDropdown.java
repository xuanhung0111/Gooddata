package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import org.openqa.selenium.By;

import java.util.Collection;

import static com.gooddata.qa.graphene.utils.CheckUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.cssSelector;

public abstract class ReactDropdown extends ReactDropdownParent {

    public ReactDropdown selectByName(String name) {
        ensureDropdownOpen();

        String nameSimplified = simplifyText(name);
        // in case there is a search field, use it
        if (this.hasSearchField()) {
            this.searchForText(name);
        }

        By selectedItem = cssSelector(getDropdownCssSelector() + " .s-" + nameSimplified);
        waitForElementVisible(selectedItem, browser).click();

        // wait until the selection is made and propagated to the button title
        By buttonTitle = cssSelector("button.s-" + nameSimplified);
        waitForElementVisible(buttonTitle, this.getRoot());

        return this;
    }

    public String getSelection() {
        return getDropdownButton().getText();
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();

        String itemSelector = getDropdownCssSelector() + " .gd-list-item";
        return getElementTexts(cssSelector(itemSelector), browser);
    }

}
