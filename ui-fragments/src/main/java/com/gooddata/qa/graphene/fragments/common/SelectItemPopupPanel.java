package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.Sleeper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.ElementUtils.clickElementByVisibleLocator;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;

public class SelectItemPopupPanel extends AbstractFragment {

    public static final By LOCATOR = By
            .cssSelector(".gdc-overlay-simple:not(.hidden):not(.yui3-overlay-hidden):not(.ember-view)");

    private static final String BUTTON_GROUP_XPATH_LOCATOR = "//*[contains(@class,'overlayPlugin-plugged') " +
            "and not(contains(@class,'gdc-hidden'))]//span[.='%s']";

    @FindBys({
        @FindBy(css = ".overlayPlugin-plugged>:not(.gdc-hidden),.sndPanelFilter,.filter,ul.c-AttributeFilterPicker"),
        @FindBy(css = "input.gdc-input")
    })
    private WebElement searchInput;

    @FindBy(css = ".overlayPlugin-plugged>:not(.gdc-hidden) .s-btn-add,.s-btn-select,.s-btn-apply")
    private WebElement submitButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    @FindBys({
        @FindBy(css = ".overlayPlugin-plugged>:not(.gdc-hidden),.afp-list"),
        @FindBy(css = "div.es_body:not(.hidden):not(.gdc-hidden),.yui3-c-label:not(.es_head):not(.gdc-hidden)")
    })
    private List<WebElement> items;

    public static SelectItemPopupPanel getInstance(SearchContext searchContext) {
        return getInstance(LOCATOR, searchContext);
    }

    public static SelectItemPopupPanel getInstance(By locator, SearchContext searchContext) {
        return getInstance(SelectItemPopupPanel.class, locator, searchContext);
    }

    public static <T extends SelectItemPopupPanel> T getInstance(Class<T> clazz, By locator, SearchContext searchContext) {
        WebElement root = waitForElementVisible(locator, searchContext);

        waitForElementVisible(By.cssSelector(".yui3-c-simpleColumn-window.loaded"), root);
        return Graphene.createPageFragment(clazz, root);
    }

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

    public SelectItemPopupPanel searchAndSelectItems(String... items) {
        searchAndSelectItems(asList(items));
        return this;
    }

    public void submitPanel() {
        waitForElementVisible(submitButton).click();
        waitForPanelNotVisible();
    }

    public void cancelPanel() {
        waitForElementVisible(cancelButton).click();
        waitForPanelNotVisible();
    }

    public SelectItemPopupPanel changeGroup(String group) {
        waitForElementVisible(this.getRoot());
        final WebElement button = waitForElementVisible(By.xpath(
                String.format(BUTTON_GROUP_XPATH_LOCATOR, group)), browser);
        button.click();

        Graphene.waitGui().until(input -> button.findElement(BY_PARENT).getAttribute("class")
                        .contains("yui3-c-label-selected"));

        return this;
    }

    public List<String> getItems() {
        return getElementTexts(getItemElements());
    }

    public List<WebElement> getItemElements() {
        return items;
    }

    public SelectItemPopupPanel clearAllItems() {
        clickElementByVisibleLocator(getRoot(), By.className("s-btn-none"), By.className("clearVisible"));
        return this;
    }

    public boolean areAllItemsSelected() {
        return getAllItemCheckboxes().allMatch(e -> e.isSelected());
    }

    public SelectItemPopupPanel selectAllItems() {
        clickElementByVisibleLocator(getRoot(), By.className("s-btn-all"), By.className("selectVisible"));
        return this;
    }

    public boolean areAllItemsDeselected() {
        return getAllItemCheckboxes().allMatch(e -> !e.isSelected());
    }

    // Just use this action when the expected item not visible in list
    public SelectItemPopupPanel searchItem(final String searchText) {
        searchInput.sendKeys(searchText);
        waitForLoaded();
        return this;
    }

    public SelectItemPopupPanel clearSearchInput() {
        // Sometimes clear() does nothing, so use custom method instead
        ElementUtils.clear(searchInput);

        waitForLoaded();
        return this;
    }

    public boolean isSearchInputVisible() {
        return waitForElementVisible(searchInput).isDisplayed();
    }

    private Stream<WebElement> getAllItemCheckboxes() {
        return waitForCollectionIsNotEmpty(getItemElements())
                .stream()
                .map(e -> e.findElement(By.tagName("input")));
    }

    private SelectItemPopupPanel selectItem(final String item) {
        final WebElement itemElement = findItemFrom(item, getItemElements()).get();
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
        if (!waitForElementVisible(searchInput).getAttribute("value").trim().isEmpty())
            clearSearchInput();

        return waitForCollectionIsNotEmpty(getItemElements());
    }

    private void waitForLoaded() {
        // Put some sleep to wait for loading wheel appear
        Sleeper.sleepTightInSeconds(1);

        // Cause that some popups have many groups and only one of them is visible
        // This code handles for below case which recommends in ticket QA-7679
        final Function<WebDriver, Boolean> predicate = browser -> getRoot().findElements(By.className("loaded"))
                .stream().filter(element -> isElementVisible(element)).collect(Collectors.toList()).size() > 0;
        Graphene.waitGui().until(predicate);
    }
}
