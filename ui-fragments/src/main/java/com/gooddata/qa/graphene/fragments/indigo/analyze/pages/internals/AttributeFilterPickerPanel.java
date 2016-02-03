package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

import java.util.List;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AttributeFilterPickerPanel extends AbstractFragment {

    @FindBy(className = "searchfield-input")
    private WebElement searchInput;

    @FindBy(className = "s-select_all")
    private WebElement selectAllButton;

    @FindBy(className = "s-clear")
    private WebElement clearButton;

    @FindBy(css = ".s-filter-item > div")
    private List<WebElement> items;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-apply")
    private WebElement applyButton;

    public static final By LOCATOR = className("adi-attr-filter-picker");
    private static final By BY_INPUT = tagName("input");
    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    public void select(String... values) {
        waitForCollectionIsNotEmpty(getItems());
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

    public void selectItem(String item) {
        searchValidItem(item);
        getItems().stream()
            .filter(e -> item.equals(e.findElement(tagName("span")).getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find: " + item))
            .findElement(BY_INPUT)
            .click();
    }

    public void searchItem(String name) {
        waitForElementVisible(this.getRoot());

        clearSearchField();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(getItems());

        clearSearchField();
        searchInput.sendKeys(name);
    }

    public List<String> getItemNames() {
        return getElementTexts(getItems(), e -> e.findElement(tagName("span")));
    }

    public String getId(final String item) {
        return Stream.of(getItems().stream()
            .filter(e -> item.equals(e.findElement(tagName("span")).getText()))
            .findFirst()
            .get()
            .findElement(BY_PARENT)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];
    }

    public WebElement getApplyButton() {
        return waitForElementVisible(applyButton);
    }

    public WebElement getClearButton() {
        return waitForElementVisible(clearButton);
    }

    private void searchValidItem(String name) {
        searchItem(name);
        waitForCollectionIsNotEmpty(getItems());
    }

    private void clearSearchField() {
        final By searchFieldClear = className("searchfield-clear");
        if (isElementPresent(searchFieldClear, getRoot())) {
            waitForElementVisible(searchFieldClear, getRoot()).click();
        } else {
            waitForElementVisible(searchInput).clear();
        }
    }

    private List<WebElement> getItems() {
        waitForElementNotPresent(className("filter-items-loading"));
        return items;
    }
}
