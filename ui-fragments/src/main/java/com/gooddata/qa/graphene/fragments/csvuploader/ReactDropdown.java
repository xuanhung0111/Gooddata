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

    @FindBy(css = "button.button-dropdown")
    private WebElement dropdownButtonLoaded;

    public boolean isDropdownOpen() {
        waitForElementVisible(dropdownButtonLoaded);
        return isElementPresent(By.cssSelector("button.is-active"), this.getRoot());
    }

    public void ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public WebElement getDropdownButton() {
        return waitForElementPresent(dropdownButtonLoaded);
    }

    protected void toggleDropdown() {
        waitForElementVisible(dropdownButtonLoaded).click();
    }

    public String getSelection() {
        return getDropdownButton().getText();
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();

        String itemSelector = dropdownSelector + " .gd-list-item:not(.is-header)";
        return getElementTexts(cssSelector(itemSelector), browser);
    }

    public ReactDropdown selectByValue(final String value) {

        ensureDropdownOpen();

        browser.findElements(cssSelector(dropdownSelector + " .gd-list-item:not(.is-disabled)"))
                .stream()
                .filter(e -> value.equals(e.getText()))
                .findFirst()
                .get()
                .click();
        return this;
    }
}