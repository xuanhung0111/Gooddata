package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CataloguePanel extends AbstractFragment {

    @FindBy(css = ".searchfield-input")
    private WebElement searchInput;

    @FindBy(css = ".adi-catalogue-item")
    private List<WebElement> items;

    @FindBy(className = "s-filter-all")
    private WebElement filterAll;

    @FindBy(className = "s-filter-metrics")
    private WebElement filterMetrics;

    @FindBy(className = "s-filter-attributes")
    private WebElement filterAttributes;

    private Actions actions;

    private static final By BY_INLINE_HELP = By.cssSelector(".inlineBubbleHelp");
    private static final By BY_NO_ITEMS = By.className("adi-no-items");
    private static final By BY_UNRELATED_ITEMS_HIDDEN = By.cssSelector("footer > div");
    private static final By BY_UNAVAILABLE_ITEMS_MATCHED = By.className("s-unavailable-items-matched");
    private static final By BY_ADD_DATA = By.cssSelector(".csv-link-section .s-btn-add_data");
    private static final By BY_CLEAR_SEARCH_FIELD = By.className("searchfield-clear");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    public int getUnrelatedItemsHiddenCount() {
        By locator = isElementPresent(BY_NO_ITEMS, browser) ?
                BY_UNAVAILABLE_ITEMS_MATCHED : BY_UNRELATED_ITEMS_HIDDEN;

        if (!isElementPresent(locator, getRoot())) {
            return 0;
        }

        String unrelatedItemsHiddenMessage = waitForElementVisible(locator, getRoot()).getText().trim();
        return Integer.parseInt(unrelatedItemsHiddenMessage.split(" ")[0]);
    }

    public void filterCatalog(CatalogFilterType type) {
        WebElement filter;
        switch(type) {
            case ALL:
                filter = filterAll;
                break;
            case MEASURES:
                filter = filterMetrics;
                break;
            case ATTRIBUTES:
                filter = filterAttributes;
                break;
            default:
                filter = filterAll;
                break;
        }
        waitForElementVisible(filter).click();
    }

    public WebElement getMetric(String metric) {
        return searchAndGetItem(metric, FieldType.METRIC);
    }

    public WebElement getCategory(String category) {
        return searchAndGetItem(category, FieldType.ATTRIBUTE);
    }

    public WebElement getFact(String fact) {
        return searchAndGetItem(fact, FieldType.FACT);
    }

    public WebElement getInapplicableCategory(String category) {
        return searchAndGetInapplicableItem(category, FieldType.ATTRIBUTE);
    }

    public WebElement getTime(String filter) {
        clearInputText();
        return waitForCollectionIsNotEmpty(items).stream()
            .filter(date -> filter.equals(date.getText()))
            .filter(date -> date.getAttribute("class").contains(FieldType.DATE.toString()))
            .findFirst()
            .get();
    }

    public String getTimeDescription(String time) {
        WebElement field = getTime(time);
        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getTimeDescription();
    }

    public String getAttributeDescription(String attribute) {
        WebElement field = getCategory(attribute);
        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
    }

    public String getMetricDescription(String metric) {
        WebElement field = getMetric(metric);

        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getMetricDescription();
    }

    public String getFactDescription(String fact) {
        WebElement field = getFact(fact);

        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getFactDescription();
    }

    public List<String> getAllCatalogFieldNamesInViewPort() {
        return Lists.newArrayList(Collections2.transform(items, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    /**
     * Search metric/attribute/fact ... in catalogue panel (The panel in the left of Analysis Page)
     * @param item
     * @return true if found something from search input, otherwise return false
     */
    public boolean searchBucketItem(String item) {
        waitForItemLoaded();
        clearInputText();

        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForItemLoaded();
        waitForCollectionIsEmpty(items);

        clearInputText();
        searchInput.sendKeys(item);
        waitForItemLoaded();

        if (!isElementPresent(BY_NO_ITEMS, browser)) {
            waitForCollectionIsNotEmpty(items);
            return true;
        }
        WebElement noItem = browser.findElement(BY_NO_ITEMS).findElement(By.cssSelector("p:first-child"));
        assertEquals(noItem.getText().trim(), "No data matching\n\"" + item + "\"");
        return false;
    }

    private void clearInputText() {
        if (isElementPresent(BY_CLEAR_SEARCH_FIELD, getRoot())) {
            WebElement clearIcon = waitForElementVisible(BY_CLEAR_SEARCH_FIELD, getRoot());
            clearIcon.click();
            waitForElementNotPresent(clearIcon);
        } else {
            waitForElementVisible(searchInput).clear();
        }
        waitForItemLoaded();
    }

    public boolean isInapplicableAttributeMetricInViewPort() {
        return Iterables.any(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains("not-available");
            }
        });
    }

    public Collection<WebElement> getAllCatalogFieldsInViewPort() {
        return items;
    }

    public boolean isDataApplicable(final String data) {
        return items.stream()
            .map(WebElement::getText)
            .anyMatch(text -> data.equals(text.trim()));
    }

    public boolean isAddDataLinkVisible() {
        if (!isElementPresent(BY_ADD_DATA, getRoot())) {
            return false;
        }

        waitForElementVisible(BY_ADD_DATA, getRoot());
        return true;
    }

    public String getDataLinkBubbleMessage() {
        new Actions(browser).moveToElement(waitForElementVisible(BY_ADD_DATA, getRoot())).perform();
        return waitForElementVisible(By.cssSelector(".bubble-content .content"), browser).getText();
    }

    public void goToDataSectionPage() {
        waitForElementVisible(BY_ADD_DATA, getRoot()).click();
    }

    private void waitForItemLoaded() {
        Predicate<WebDriver> itemsLoaded = browser -> !isElementPresent(By.cssSelector(".gd-spinner.small"),
                browser);
        Graphene.waitGui().until(itemsLoaded);
    }

    private WebElement searchAndGetInapplicableItem(final String item, final FieldType type) {
        searchBucketItem(item);
        return Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return item.equals(input.getText().trim())
                        && input.getAttribute("class").contains(type.toString())
                        && input.getAttribute("class").contains("not-available");
            }
        });
    }

    private WebElement searchAndGetItem(final String item, final FieldType type) {
        searchBucketItem(item);
        return Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(final WebElement input) {
                return item.equals(input.getText().trim())
                        && input.getAttribute("class").contains(type.toString());
            }
        });
    }

    private Actions getActions() {
        if (actions == null) actions = new Actions(browser);
        return actions;
    }
}
