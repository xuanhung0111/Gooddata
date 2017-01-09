package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class AttributeFilter extends AbstractReactDropDown {

    private static By APPLY_BUTTON_SELECTOR =  cssSelector("button.s-apply_button");

    @FindBy(className = "button-title")
    private WebElement buttonTitle;

    @FindBy(className = "button-subtitle")
    private WebElement buttonText;

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".attribute-filter-button";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .attributevalues-list";
    }

    public AttributeFilter selectByNames(String... names) {
        Stream.of(names).forEach(name -> {
            selectByName(name);
            waitForElementVisible(APPLY_BUTTON_SELECTOR, browser).click();
        });
        return this;
    }

    public void selectAllValues() {
        ensureDropdownOpen();
        waitForElementVisible(className("s-select_all"), browser).click();
        waitForElementVisible(APPLY_BUTTON_SELECTOR, browser).click();
    }

    public AttributeFilter clearAllCheckedValues() {
        ensureDropdownOpen();
        waitForElementVisible(cssSelector(".s-clear"), browser).click();
        return this;
    }

    public String getTitle() {
        waitForElementVisible(buttonTitle);
        return buttonTitle.getText();
    }

    public String getSelection() {
        waitForElementVisible(buttonText);
        return buttonText.getText();
    }

    public String getSelectedItems() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items"))).getText();
    }

    public String getSelectedItemsCount() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items-count"))).getText();
    }

    public boolean isActive() {
        return waitForElementPresent(cssSelector(getDropdownButtonCssSelector()), getRoot()).getAttribute("class")
                .contains("is-active");
    }

    @Override
    protected void waitForSelectionIsApplied(String name) {
        // ignore this in attribute filter
    }
}
