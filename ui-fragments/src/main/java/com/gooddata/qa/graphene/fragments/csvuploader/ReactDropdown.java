package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openqa.selenium.By.cssSelector;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

/**
 * This componet is used for testing Dropdown component from gdc-data-section. It is based on {@link com.gooddata.qa.graphene.fragments.indigo.dashboards.ReactDropdown}
 * since the Dropdown is based on dropdown from dashboards. If you need to add some method then first check if you could be inspired
 * by the original {@link com.gooddata.qa.graphene.fragments.indigo.dashboards.ReactDropdown} since not supported or not used
 * methos were removed from this class.
 */
public class ReactDropdown extends AbstractFragment {

    /**
     * This field is needed to find the correct dropdown, rendered in overlay,
     * because it's extracted from DOM and not rendered under dropdown button DOM tree.
     */
    private final String dropdownSelector;

    public ReactDropdown() {
        this("");
    }

    public ReactDropdown(String dropdownSelector) {
        notNull(dropdownSelector, "Dropdown selector is null");
        this.dropdownSelector = dropdownSelector;
    }

    @FindBy(css = "button.button-dropdown:not([data-reactid*=date-format])")
    private WebElement dropdownTypeButtonLoaded;
    
    @FindBy(css = "button.button-dropdown[data-reactid*=date-format]")
    private WebElement dropdownFormatButtonLoaded;

    public ReactDropdown selectTypeByValue(final String value) {
        ensureTypeDropdownOpen();
        return selectByValue(value);
    }

    public String getTypeSelection() {
        return getTypeDropdownButton().getAttribute("title");
    }

    public Collection<String> getTypeValues() {
        ensureTypeDropdownOpen();
        return getElementTexts(getItemSelector(), browser);
    }

    public ReactDropdown selectFormatByValue(final String value) {
        ensureFormatDropdownOpen();
        return selectByValue(value);
    }

    public String getFormatSelection() {
        return getFormatDropdownButton().getAttribute("title");
    }

    public Collection<String> getFormatValues() {
        ensureFormatDropdownOpen();
        return getElementTexts(getItemSelector(), browser);
    }

    public WebElement getItemElement(final String itemType) {
        ensureTypeDropdownOpen();

        return browser
                .findElements(getItemSelector())
                .stream()
                .filter(e -> e.findElement(By.className("type-name")).getText().contains(itemType))
                .findFirst()
                .get();
    }

    private boolean isFormatDropdownOpen() {
        waitForElementVisible(dropdownFormatButtonLoaded);
        return isElementPresent(By.cssSelector("button.is-active"), this.getRoot());
    }

    private void toggleFormatDropdown() {
        waitForElementVisible(dropdownFormatButtonLoaded).click();
    }

    private void ensureFormatDropdownOpen() {
        if (!this.isFormatDropdownOpen()) {
            this.toggleFormatDropdown();
        }
    }

    private WebElement getFormatDropdownButton() {
        return waitForElementPresent(dropdownFormatButtonLoaded);
    }

    private boolean isTypeDropdownOpen() {
        waitForElementVisible(dropdownTypeButtonLoaded);
        return isElementPresent(By.cssSelector("button.is-active"), this.getRoot());
    }

    private void toggleTypeDropdown() {
        waitForElementVisible(dropdownTypeButtonLoaded).click();
    }

    private void ensureTypeDropdownOpen() {
        if (!this.isTypeDropdownOpen()) {
            this.toggleTypeDropdown();
        }
    }

    private WebElement getTypeDropdownButton() {
        return waitForElementPresent(dropdownTypeButtonLoaded);
    }

    private ReactDropdown selectByValue(final String value) {
        browser.findElements(cssSelector(dropdownSelector + " .gd-list-item:not(.is-disabled)"))
                .stream()
                .filter(e -> value.equals(e.getText()))
                .findFirst()
                .get()
                .click();
        return this;
    }

    private By getItemSelector() {
        return cssSelector(dropdownSelector + " .gd-list-item:not(.is-header)");
    }
}