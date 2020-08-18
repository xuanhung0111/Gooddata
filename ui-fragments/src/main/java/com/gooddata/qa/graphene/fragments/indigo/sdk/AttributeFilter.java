package com.gooddata.qa.graphene.fragments.indigo.sdk;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.stream.Stream;

public class AttributeFilter extends AbstractReactDropDown {

    @FindBy(className = "gd-button-text")
    private WebElement buttonTitle;

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".dropdown-button";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    @Override
    protected String getNoMatchingDataMessageCssSelector() {
        return ".gd-list-noResults";
    }

    @Override
    protected boolean isDropdownOpen() {
        return isActive();
    }

    @Override
    protected WebElement getElementByName(String name) {
        return getElements().stream()
                .filter(e -> name.equals(e.getText()))
                .findFirst()
                .get();
    }

    @Override
    protected void waitForSelectionIsApplied(String name) {
        // ignore this in attribute filter
    }

    @Override
    protected void waitForPickerLoaded() {
        try {
            waitForElementVisible(className("s-dropdown-loading"), getRoot(), 1);
            waitForElementNotPresent(className("s-dropdown-loading"));
        } catch(Exception e) {
            // Do nothing
        }
    }

    public static final AttributeFilter getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributeFilter.class, waitForElementVisible(className("gd-attribute-filter"), context));
    }

    public AttributeFilter selectByNames(String... names) {
        Stream.of(names).forEach(name -> {
            selectByName(name);
            apply();
        });
        return this;
    }

    public AttributeFilter clearAllCheckedValues() {
        ensureDropdownOpen();
        waitForElementVisible(cssSelector(".s-clear"), browser).click();
        return this;
    }

    public void selectAllValues() {
        ensureDropdownOpen();
        waitForElementVisible(className("s-select_all"), browser).click();
        apply();
    }

    public void apply() {
        waitForElementVisible(cssSelector("button.s-apply"), browser).click();
    }

    public String getTitle() {
        waitForElementVisible(buttonTitle);
        return buttonTitle.getText();
    }

    public boolean isActive() {
        return waitForElementPresent(cssSelector(getDropdownButtonCssSelector()), getRoot()).getAttribute("class")
                .contains("is-active");
    }
}
