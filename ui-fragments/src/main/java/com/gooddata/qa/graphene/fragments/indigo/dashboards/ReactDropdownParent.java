package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class ReactDropdownParent extends AbstractFragment {

    @FindBy(css = "button.is-loaded")
    private WebElement dropdownButtonLoaded;

    /**
     * This method is needed to find the correct dropdown, rendered in overlay,
     * because it's extracted from DOM and not rendered under dropdown button DOM tree.
     * @return dropdown css selector
     */
    public abstract String getDropdownCssSelector();

    public boolean isDropdownOpen() {
        waitForElementVisible(dropdownButtonLoaded);
        return isElementPresent(By.cssSelector("button.is-loaded.is-active"), this.getRoot());
    }

    public void ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public WebElement getDropdownButton() {
        return waitForElementPresent(dropdownButtonLoaded);
    }

    protected boolean hasSearchField() {
        By searchField = cssSelector(getSearchFieldSelector());
        return isElementPresent(searchField, browser);
    }

    protected String getSearchFieldSelector() {
        return getDropdownCssSelector() + " .searchfield-input";
    }

    protected ReactDropdownParent searchForText(String text) {
        By searchField = cssSelector(getSearchFieldSelector());
        waitForElementVisible(searchField, browser).clear();
        waitForElementVisible(searchField, browser).sendKeys(text);
        return this;
    }

    protected void toggleDropdown() {
        waitForElementVisible(dropdownButtonLoaded).click();
    }

}
