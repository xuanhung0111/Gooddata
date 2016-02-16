package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.Sleeper;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public abstract class ReactDropdownParent extends AbstractFragment {
    private static final String DISABLED_CLASS = "disabled";

    /**
     * This method is needed to find the correct dropdown, rendered in overlay,
     * because it's extracted from DOM and not rendered under dropdown button DOM tree.
     * @return dropdown css selector
     */
    public abstract String getDropdownCssSelector();

    /**
     * Get css selector for dropdown button
     * @return css selector for loaded dropdown button
     */
    public String getDropdownButtonCssSelector() {
        return "button.is-loaded";
    }

    /**
     * Get css selector for an ordinary (clickable, non-header) item in the list
     * @return css selector of an item in the list
     */
    public String getListItemCssSelector() {
        return ".gd-list-item:not(.is-header)";
    }

    public boolean isDropdownOpen() {
        String enabledButtonCSSSelector = getDropdownButtonCssSelector() + ":not(." + DISABLED_CLASS + ")";
        waitForElementVisible(By.cssSelector(enabledButtonCSSSelector), this.getRoot());

        return isElementPresent(By.cssSelector(getDropdownCssSelector()), browser);
    }

    public void ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public WebElement getDropdownButton() {
        return waitForElementVisible(By.cssSelector(getDropdownButtonCssSelector()), this.getRoot());
    }

    private void waitForDropdownLoaded() {
        waitForElementNotPresent(By.cssSelector(getDropdownCssSelector() + " .s-isLoading"));
    }

    protected boolean hasSearchField() {
        // wait until dropdown body is loaded and check search field
        waitForDropdownLoaded();
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
        Sleeper.sleepTight(500);
        waitForDropdownLoaded();
        return this;
    }

    protected void toggleDropdown() {
        waitForElementVisible(By.cssSelector(getDropdownButtonCssSelector()), this.getRoot())
                .click();
    }

}
