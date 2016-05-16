package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.cssSelector;

import java.util.Collection;

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
        return ".searchfield-input";
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

    protected boolean isDropdownOpen() {
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

    protected String getNoMatchingDataMessageCssSelector() {
        return ".gd-no-matching-data";
    }
}
