package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.cssSelector;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.browser.BrowserUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class AbstractReactDropDown extends AbstractDropDown {

    private static final String DISABLED_CLASS = "disabled";
    private static final String IS_LOADING_CLASS = "s-isLoading";

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not(.is-header)";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return ".gd-input-search input";
    }

    @Override
    protected void waitForPickerLoaded() {
        waitForElementNotPresent(cssSelector(getDropdownCssSelector() + " ." + IS_LOADING_CLASS));
    }

    @Override
    public AbstractPicker searchForText(String text) {
        if (hasSearchField()) {
            return super.searchForText(text);
        }
        return this;
    }

    public boolean isDropdownOpen() {
        String enabledButtonCSSSelector = getDropdownButtonCssSelector() + ":not(." + DISABLED_CLASS + ")";
        waitForElementVisible(By.cssSelector(enabledButtonCSSSelector), getRoot());

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
        // some drop down button is not visible, so in general case, make sure it appears in DOM
        return waitForElementPresent(By.cssSelector(getDropdownButtonCssSelector()), getRoot());
    }

    protected void toggleDropdown() {
        waitForElementVisible(By.cssSelector(getDropdownButtonCssSelector()), getRoot())
                .click();
    }

    public AbstractReactDropDown selectByName(String name) {
        ensureDropdownOpen();
        searchForText(name);
        getElementByName(name).click();

        // wait until the selection is made and propagated to the button title
        waitForSelectionIsApplied(name);

        return this;
    }

    public String getSelection() {
        return getDropdownButton().getText();
    }

    protected void waitForSelectionIsApplied(String name) {
        By buttonTitle = cssSelector(getDropdownButtonCssSelector() + ".s-" + simplifyText(name));
        waitForElementVisible(buttonTitle, this.getRoot());
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();
        return getElementTexts(getElements());
    }

    public boolean isShowingNoMatchingDataMessage() {
        waitForPickerLoaded();
        return isElementPresent(cssSelector(getNoMatchingDataMessageCssSelector()), getPanelRoot());
    }

    /**
     * get all values on dropdown in case having scrollbar
     * @return list of value names
     */
    public List<String> getValuesWithScrollbar() {
        // does not work if value list is empty
        waitForElementVisible(cssSelector(getSearchInputCssSelector()), getPanelRoot());

        // add a break to handle lazy load (< 20 values)
        // for larger number of values, it should be managed by scrolling (use js) to the end of scrollbar.
        Sleeper.sleepTightInSeconds(1);

        // use javascript which is not affected by scrollbar to get all attributes
        List<WebElement> elements = (List<WebElement>) BrowserUtils.runScript(
                browser, "return document.querySelectorAll(\"" + getListItemsCssSelector() + "\")");

        return elements.stream()
                .map(e -> {
                    scrollElementIntoView(e, browser);// return empty value if the element is not in viewport
                    return e.getText();
                })
                .collect(Collectors.toList());
    }

    protected String getNoMatchingDataMessageCssSelector() {
        return ".gd-no-matching-data";
    }
}
