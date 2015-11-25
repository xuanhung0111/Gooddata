package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class SelectItemPopupPanel extends AbstractFragment {

    public static final By LOCATOR = By
            .cssSelector(".gdc-overlay-simple:not(.hidden):not(.yui3-overlay-hidden):not(.ember-view)");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    private static final String BUTTON_GROUP_XPATH_LOCATOR = "//*[contains(@class,'gdc-buttonGroup')]//span[.='%s']";

    @FindBys({
        @FindBy(css = ".overlayPlugin-plugged>:not(.gdc-hidden),.pickerRoot,.sndPanelFilter,.filter"),
        @FindBy(css = "input.gdc-input")
    })
    private WebElement searchInput;

    @FindBy(css = ".s-btn-add,.s-btn-select")
    private WebElement addButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    @FindBys({
        @FindBy(css = ".overlayPlugin-plugged>:not(.gdc-hidden),.afp-list"),
        @FindBy(css = "div.es_body:not(.hidden):not(.gdc-hidden)")
    })
    private List<WebElement> items;

    public void searchItem(String searchText) {
        waitForElementVisible(this.getRoot());

        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        // Sometimes clear() does nothing, so using hot keys instead
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchInput.sendKeys(Keys.DELETE);

        searchInput.sendKeys(searchText);
        waitForCollectionIsNotEmpty(items);
    }

    public void selectItem(String item) {
        selectItem(item, true);
    }

    public void selectEmbedItem(String item) {
        selectItem(item, false);
    }

    public SelectItemPopupPanel searchAndSelectItem(String item) {
        searchItem(item);
        selectItem(item);
        return this;
    }

    public void searchAndSelectEmbedItem(String item) {
        searchItem(item);
        selectEmbedItem(item);
    }

    public void changeGroup(String group) {
        waitForElementVisible(this.getRoot());
        final WebElement button = waitForElementVisible(By.xpath(
                String.format(BUTTON_GROUP_XPATH_LOCATOR, group)), browser);
        button.click();

        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return button.findElement(BY_PARENT).getAttribute("class")
                        .contains("yui3-c-label-selected");
            }
        });
    }

    public void submitPanel() {
        waitForElementVisible(addButton).click();
        waitForPanelNotVisible();
    }

    public SelectItemPopupPanel searchAndSelectItems(Collection<String> items) {
        items.stream().forEach(this::searchAndSelectEmbedItem);
        return this;
    }

    public Collection<String> getItems() {
        return items.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private void selectCheckboxItem(String item) {
        By label = By.cssSelector("label");
        for (WebElement e : items) {
            if (!item.equals(e.findElement(label).getText().trim()))
                continue;
            e.findElement(By.cssSelector("input")).click();
            return;
        }
        throw new IllegalArgumentException(String.format("Item '%s' is not found!", item));
    }

    private void selectTextItem(String item) {
        for (WebElement e : items) {
            if (!item.equals(e.getText().trim()))
                continue;
            e.click();
            return;
        }
        throw new IllegalArgumentException(String.format("Item '%s' is not found!", item));
    }

    private boolean isCheckboxItem() {
        waitForCollectionIsNotEmpty(items);
        try {
            items.get(0).findElement(By.cssSelector("input"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void waitForPanelNotVisible() {
        try {
            waitForElementNotVisible(this.getRoot());
        } catch (Exception e) {
            // In special case like "add drilling" in table report, the locator of
            // this fragment has more than 1 copy. In this case we have to use get(index)
            // from findElements() and IndexOutOfBoundException is thrown when we try to wait
            // for this panel invisible, because of DOM can remove this locator.
            
            // So, ignore this exception
        }
    }

    private void selectItem(String item, boolean isNotEmbed) {
        if (isCheckboxItem()) {
            selectCheckboxItem(item);
        } else {
            selectTextItem(item);
        }

        if (isNotEmbed) {
            submitPanel();
        }
    }
}
