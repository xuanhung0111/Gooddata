package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.Sleeper;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;

public abstract class ReactDropdownParent extends AbstractFragment {
    private static final String DISABLED_CLASS = "disabled";
    private static final String NO_MATCHING_DATA_MESSAGE_CLASS = "no-matching-data";
    private static final String IS_LOADING_CLASS = "s-isLoading";
    private static final String SEARCHFIELD_INPUT_CLASS = "searchfield-input";

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

    public void ensureDropdownClosed() {
        if (this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public WebElement getDropdownButton() {
        return waitForElementVisible(By.cssSelector(getDropdownButtonCssSelector()), this.getRoot());
    }

    public boolean isShowingNoMatchingDataMessage() {
        waitForDropdownLoaded();
        return isElementPresent(cssSelector(getNoMatchingDataMessageSelector()), browser);
    }

    public String getSearchText() {
        return waitForElementVisible(cssSelector(getSearchFieldSelector()), browser).getText();
    }

    protected String getNoMatchingDataMessageSelector() {
        return getDropdownCssSelector() + " ." + NO_MATCHING_DATA_MESSAGE_CLASS;
    }

    protected void waitForDropdownLoaded() {
        waitForElementNotPresent(By.cssSelector(getDropdownCssSelector() + "." + IS_LOADING_CLASS));
    }

    protected boolean hasSearchField() {
        // wait until dropdown body is loaded and check search field
        waitForDropdownLoaded();
        return isElementPresent(cssSelector(getSearchFieldSelector()), browser);
    }

    protected String getSearchFieldSelector() {
        return getDropdownCssSelector() + " ." + SEARCHFIELD_INPUT_CLASS;
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
