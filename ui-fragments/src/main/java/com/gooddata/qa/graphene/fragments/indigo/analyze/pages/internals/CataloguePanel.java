package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import com.google.common.base.Predicate;

public class CataloguePanel extends AbstractFragment {

    @FindBy(className = "searchfield-input")
    private WebElement searchInput;

    @FindBy(className = "adi-catalogue-item")
    private List<WebElement> items;

    @FindBy(className = "s-filter-all")
    private WebElement filterAll;

    @FindBy(className = "s-filter-metrics")
    private WebElement filterMetrics;

    @FindBy(className = "s-filter-attributes")
    private WebElement filterAttributes;

    @FindBy(className = "s-dataset-picker-toggle")
    private WebElement datasetPicker;

    private static final By BY_INLINE_HELP = By.cssSelector(".inlineBubbleHelp");
    private static final By BY_NO_ITEMS = By.className("adi-no-items");
    private static final By BY_UNRELATED_ITEMS_HIDDEN = By.cssSelector("footer > div");
    private static final By BY_UNAVAILABLE_ITEMS_MATCHED = By.className("s-unavailable-items-matched");
    private static final By BY_ADD_DATA = By.cssSelector(".csv-link-section .s-btn-add_data");
    private static final By BY_CLEAR_SEARCH_FIELD = By.className("searchfield-clear");
    private static final By BY_DATASOURCE_DROPDOWN = By.className("data-source-picker-dropdown");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    public int getUnrelatedItemsHiddenCount() {
        waitForItemLoaded();
        By locator = isElementPresent(BY_NO_ITEMS, browser) ?
                BY_UNAVAILABLE_ITEMS_MATCHED : BY_UNRELATED_ITEMS_HIDDEN;

        if (!isElementPresent(locator, getRoot())) {
            return 0;
        }

        String unrelatedItemsHiddenMessage = waitForElementVisible(locator, getRoot()).getText().trim();
        return Integer.parseInt(unrelatedItemsHiddenMessage.split(" ")[0]);
    }

    public CataloguePanel filterCatalog(CatalogFilterType type) {
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
        return this;
    }

    public WebElement getDate() {
        clearInputText();
        return waitForCollectionIsNotEmpty(items).stream()
            .filter(date -> "Date".equals(date.getText()))
            .filter(date -> date.getAttribute("class").contains(FieldType.DATE.toString()))
            .findFirst()
            .get();
    }

    public String getDateDescription() {
        WebElement field = getDate();
        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getTimeDescription();
    }

    public String getAttributeDescription(String attribute) {
        WebElement field = searchAndGet(attribute, FieldType.ATTRIBUTE);
        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
    }

    public String getMetricDescription(String metric) {
        WebElement field = searchAndGet(metric, FieldType.METRIC);

        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getMetricDescription();
    }

    public String getFactDescription(String fact) {
        WebElement field = searchAndGet(fact, FieldType.FACT);

        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getFactDescription();
    }

    public List<String> getFieldNamesInViewPort() {
        return getElementTexts(items);
    }

    /**
     * Search metric/attribute/fact ... in catalogue panel (The panel in the left of Analysis Page)
     * @param item
     * @return true if found something from search input, otherwise return false
     */
    public boolean search(String item) {
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

    public Collection<WebElement> getFieldsInViewPort() {
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
        getActions().moveToElement(waitForElementVisible(BY_ADD_DATA, getRoot())).perform();
        return waitForElementVisible(By.cssSelector(".bubble-content .content"), browser).getText();
    }

    public void goToDataSectionPage() {
        waitForElementVisible(BY_ADD_DATA, getRoot()).click();
    }

    public CataloguePanel changeDataset(String dataset) {
        waitForElementVisible(datasetPicker).click();
        Graphene.createPageFragment(DatasourceDropDown.class,
                waitForElementVisible(BY_DATASOURCE_DROPDOWN, browser)).select(dataset);
        waitForItemLoaded();
        return this;
    }

    public WebElement searchAndGet(final String item, final FieldType type) {
        search(item);
        return items.stream()
            .filter(e -> item.equals(e.getText().trim()))
            .filter(e -> e.getAttribute("class").contains(type.toString()))
            .findFirst()
            .get();
    }

    private void waitForItemLoaded() {
        Predicate<WebDriver> itemsLoaded = browser -> !isElementPresent(By.cssSelector(".gd-spinner.small"),
                browser);
        Graphene.waitGui().until(itemsLoaded);
    }

    public class DatasourceDropDown extends AbstractFragment {

        @FindBy(className = "gd-list-item")
        private List<WebElement> items;

        public void select(final String dataset) {
            waitForCollectionIsNotEmpty(items).stream()
                .filter(e -> dataset.equals(e.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find dataset: " + dataset))
                .click();
        }
    }
}
