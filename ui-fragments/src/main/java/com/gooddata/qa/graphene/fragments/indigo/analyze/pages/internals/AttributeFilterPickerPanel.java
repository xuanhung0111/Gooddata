package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.AbstractPicker;

public class AttributeFilterPickerPanel extends AbstractPicker {

    @FindBy(className = "s-select_all")
    private WebElement selectAllButton;

    @FindBy(className = "s-clear")
    private WebElement clearButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-apply")
    private WebElement applyButton;

    private static final By CLEAR_SEARCH_TEXT_SHORTCUT = className("searchfield-clear");

    public static AttributeFilterPickerPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                waitForElementVisible(className("adi-attr-filter-picker"), context));
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".s-filter-item";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return ".searchfield-input";
    }

    @Override
    protected void waitForPickerLoaded() {
        waitForElementNotPresent(cssSelector(".filter-items-loading"));
    }

    @Override
    protected void clearSearchText() {
        if (isElementPresent(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot())) {
            waitForElementVisible(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot()).click();
            return;
        }

        super.clearSearchText();
    }

    public void select(String... values) {
        waitForPickerLoaded();
        if (values.length == 1 && "All".equals(values[0])) {
            selectAll();
            return;
        }

        waitForElementVisible(clearButton).click();
        Stream.of(values).forEach(this::selectItem);
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void selectAll() {
        waitForElementVisible(selectAllButton).click();
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void selectItem(String item) {
        searchForText(item);
        getElement(format("[title='%s']", item))
            .findElement(tagName("input"))
            .click();
    }

    public String getId(final String item) {
        return Stream.of(getElement(format("[title='%s']", item))
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];
    }

    public void discard() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void assertPanel() {
        waitForElementVisible(selectAllButton);
        waitForElementVisible(clearButton);
        waitForElementVisible(applyButton);
        waitForElementVisible(cancelButton);
    }

    public List<String> getItemNames() {
        return getElementTexts(getElements(), e -> e.findElement(tagName("span")));
    }

    public WebElement getApplyButton() {
        return waitForElementVisible(applyButton);
    }

    public WebElement getClearButton() {
        return waitForElementVisible(clearButton);
    }
}
