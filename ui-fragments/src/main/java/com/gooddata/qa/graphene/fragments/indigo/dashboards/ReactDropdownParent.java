package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

public abstract class ReactDropdownParent extends AbstractFragment {

    /**
     * This method is needed to find the correct dropdown, rendered in overlay,
     * because it's extracted from DOM and not rendered under dropdown button DOM tree.
     * @return dropdown css selector
     */
    public abstract String getDropdownCssSelector();

    protected ReactDropdownParent ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }

        By dropdownLoaded = cssSelector(getDropdownCssSelector() + ".is-loaded");
        waitForElementVisible(dropdownLoaded, browser);

        return this;
    }

    protected boolean isDropdownOpen() {
        By dropdown = cssSelector(getDropdownCssSelector());
        return isElementPresent(dropdown, browser);
    }

    protected abstract ReactDropdownParent toggleDropdown();

    protected boolean hasSearchField() {
        By searchField = cssSelector(getSearchFieldSelector());
        return isElementPresent(searchField, browser);
    }

    protected String getSearchFieldSelector() {
        return getDropdownCssSelector() + " .searchfield-input";
    }

    protected ReactDropdownParent searchForText(String text) {
        By searchField = cssSelector(getSearchFieldSelector());
        waitForElementVisible(searchField, browser).sendKeys(text);
        return this;
    }

}
