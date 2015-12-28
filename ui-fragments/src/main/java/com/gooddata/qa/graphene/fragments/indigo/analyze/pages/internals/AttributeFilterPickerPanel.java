package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
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

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    public static final By LOCATOR = className("adi-attr-filter-picker");
    private static final By BY_INPUT = tagName("input");
    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    public void select(String... values) {
        waitForCollectionIsNotEmpty(items);
        if (values.length == 1 && "All".equals(values[0])) {
            selectAll();
            return;
        }

        waitForElementVisible(clearButton).click();
        Stream.of(values).forEach(this::selectItem);
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    private void selectItem(String item) {
        searchItem(item);
        items.stream()
            .filter(e -> item.equals(e.findElement(tagName("span")).getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find: " + item))
            .findElement(BY_INPUT)
            .click();
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

    private void searchItem(String name) {
        waitForElementVisible(this.getRoot());

        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        searchInput.clear();
        searchInput.sendKeys(name);
        waitForCollectionIsNotEmpty(items);
    }
}
