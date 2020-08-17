package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.Objects.isNull;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractPicker extends AbstractFragment {

    /**
     * Get css selector for an ordinary (clickable, non-header) item in the list
     * @return css selector of an item in the list
     */
    protected abstract String getListItemsCssSelector();

    /**
     * Get css selector to detect picker items is loaded
     */
    protected abstract void waitForPickerLoaded();

    /**
     * Get css selector for search field
     * @return css selector of search field or null if drop down does not have it
     */
    protected String getSearchInputCssSelector() {
        return null;
    }

    /**
     * Get web element map with the root of picker or drop down
     * @return web element
     */
    protected WebElement getPanelRoot() {
        return getRoot();
    }

    protected void clearSearchText() {
        getSearchInput().clear();
        sleepTight(500);
        waitForPickerLoaded();
    }

    protected WebElement getSearchInput() {
        if (!hasSearchField()) {
            throw new NoSuchElementException("There is no search field with locator: " + getSearchInputCssSelector());
        }
        return waitForElementVisible(cssSelector(getSearchInputCssSelector()), getPanelRoot());
    }

    protected boolean hasSearchField() {
        if (isNull(getSearchInputCssSelector())) {
            return false;
        }

        // wait until dropdown body is loaded and check search field
        waitForPickerLoaded();
        return isElementPresent(cssSelector(getSearchInputCssSelector()), getPanelRoot());
    }

    public AbstractPicker searchForText(final String text) {
        clearSearchText();
        final WebElement searchInput = getSearchInput();
        searchInput.sendKeys(text);
        sleepTight(500);
        waitForPickerLoaded();
        return this;
    }

    public String getSearchText() {
        return getSearchInput().getText();
    }

    protected WebElement getElement(final String howToLocate) {
        waitForPickerLoaded();
        return waitForElementVisible(cssSelector(getListItemsCssSelector() + howToLocate), getPanelRoot());
    }

    protected WebElement getElementByName(final String name) {
        return getElement(".s-" + simplifyText(name));
    }

    protected List<WebElement> getElements() {
        waitForPickerLoaded();
        return getPanelRoot().findElements(cssSelector(getListItemsCssSelector()));
    }
}
