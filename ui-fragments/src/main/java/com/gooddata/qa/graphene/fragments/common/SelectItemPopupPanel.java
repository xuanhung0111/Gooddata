package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
        @FindBy(css = "div.es_body:not(.hidden):not(.gdc-hidden),.gdc-list .yui3-c-label"),
    })
    private List<WebElement> items;

    public SelectItemPopupPanel searchAndSelectItem(String item) {
        final Optional<WebElement> itemElement = findItemFrom(item, getItemListInDefaultStage());

        if (!itemElement.isPresent() || !itemElement.get().isDisplayed()) {
            searchItem(item);
        }

        selectItem(item);
        return this;
    }

    public SelectItemPopupPanel searchAndSelectItems(Collection<String> items) {
        items.stream().forEach(this::searchAndSelectItem);
        return this;
    }

    public void submitPanel() {
        waitForElementVisible(addButton).click();
        waitForPanelNotVisible();
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

    public List<String> getItems() {
        return getElementTexts(items);
    }

    // Just use this action when the expected item not visible in list
    private SelectItemPopupPanel searchItem(final String searchText) {
        final int currentItems = waitForCollectionIsNotEmpty(items).size();

        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(searchText);

        // After searching, the item list will change stage from full list --> empty --> list contains items 
        // with search pattern.
        // If using waitForCollectionIsNotEmpty() like normal way, there has a risk that code run too fast,
        // it catches the item list at stage 1, then the next action to get or select item 
        // will fail with IndexOutOfBoundException thrown because the list changes to empty stage

        // In this case, we should wait until the items displayed and less than the full list.
        // There still has a risk in this approach when the list contains only items with the same search pattern.
        // This makes the list after search is same as before (Just a special case and never happen in reality)
        Predicate<WebDriver> itemFound = browser -> waitForCollectionIsNotEmpty(items).size() < currentItems;
        Graphene.waitGui().until(itemFound);

        return this;
    }

    private SelectItemPopupPanel selectItem(final String item) {
        final WebElement itemElement = findItemFrom(item, items).get();
        final By byCheckbox = By.tagName("input");

        if (!isElementPresent(byCheckbox, itemElement)) {
            itemElement.click();
            return this;
        }

        final WebElement checkbox = waitForElementVisible(itemElement.findElement(byCheckbox));

        if (!checkbox.isSelected()) checkbox.click();
        return this;
    }

    private void waitForPanelNotVisible() {
        try {
            Graphene.waitGui().withTimeout(30, TimeUnit.SECONDS).until()
                    .element(this.getRoot()).is().not().visible();

        } catch (Exception e) {
            // In special case like "add drilling" in table report, the locator of
            // this fragment has more than 1 copy. In this case we have to use get(index)
            // from findElements() and IndexOutOfBoundException is thrown when we try to wait
            // for this panel invisible, because of DOM can remove this locator.
            
            // So, ignore this exception
        }
    }

    private Optional<WebElement> findItemFrom(final String item, final Collection<WebElement> collection) {
        return waitForCollectionIsNotEmpty(collection)
                .stream()
                .filter(e -> item.equals(e.findElement(By.cssSelector("label,.label")).getText()))
                .findFirst();
    }

    private List<WebElement> getItemListInDefaultStage() {
        if (waitForElementVisible(searchInput).getAttribute("value").trim().isEmpty())
            return waitForCollectionIsNotEmpty(items);

        searchInput.clear();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        // Sometimes clear() does nothing, so using hot keys instead
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchInput.sendKeys(Keys.DELETE);

        return waitForCollectionIsNotEmpty(items);
    }
}
