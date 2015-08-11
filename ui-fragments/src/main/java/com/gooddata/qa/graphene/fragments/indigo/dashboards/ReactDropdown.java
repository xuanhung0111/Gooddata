package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import java.util.Collection;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class ReactDropdown extends AbstractFragment {

    @FindBy(css = "button.is-loaded")
    private WebElement dropdownButtonLoaded;

    /**
     * This method is needed to find the correct dropdown, rendered in overlay,
     * because it's extracted from DOM and not rendered under dropdown button DOM tree.
     * @return dropdown css selector
     */
    public abstract String getDropdownCssSelector();

    public ReactDropdown selectByName(String name) {
        ensureDropdownOpen();

        String nameSimplified = simplifyText(name);
        // in case there is a search field, use it
        if (this.hasSearchField()) {
            this.searchForText(name);
        }

        By selectedItem = By.cssSelector(getDropdownCssSelector() + " .s-" + nameSimplified);
        waitForElementVisible(selectedItem, browser).click();

        // wait until the selection is made and propagated to the button title
        By buttonTitle = By.cssSelector("button.s-" + nameSimplified);
        waitForElementVisible(buttonTitle, this.getRoot());

        return this;
    }

    public String getSelection() {
        waitForElementVisible(dropdownButtonLoaded);
        return dropdownButtonLoaded.getText();
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();

        String itemSelector = getDropdownCssSelector() + " .gd-list-item";
        return getElementTexts(By.cssSelector(itemSelector), browser);
    }

    protected ReactDropdown ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }

        By dropdownLoaded = By.cssSelector(getDropdownCssSelector() + ".is-loaded");
        waitForElementVisible(dropdownLoaded, browser);

        return this;
    }

    protected ReactDropdown searchForText(String text) {
        By searchField = By.cssSelector(getSearchFieldSelector());
        waitForElementVisible(searchField, browser).sendKeys(text);
        return this;
    }

    protected String getSearchFieldSelector() {
        return getDropdownCssSelector() + " .searchfield-input";
    }

    protected ReactDropdown toggleDropdown() {
        waitForElementVisible(dropdownButtonLoaded).click();
        return this;
    }

    protected boolean isDropdownOpen() {
        By dropdown = By.cssSelector(getDropdownCssSelector());
        return isElementPresent(dropdown, browser);
    }

    protected boolean hasSearchField() {
        By searchField = By.cssSelector(getSearchFieldSelector());
        return isElementPresent(searchField, browser);
    }

}
