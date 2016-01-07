package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;

public class MetricConfiguration extends AbstractFragment {

    @FindBy(className = "adi-bucket-item-header")
    private WebElement header;

    @FindBy(className = ADD_ATTRIBUTE_FILTER_CLASS)
    private WebElement addAttributeFilter;

    @FindBy(className = "s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(className = "s-show-pop")
    private WebElement compareToSamePeriod;

    private static final By BY_REMOVE_ATTRIBUTE_FILTER = By.className("remove-attribute-filter");
    private static final By BY_ATTRIBUTE_FILTER_PICKER = By.className("adi-attr-filter-picker");
    private static final By BY_ATTRIBUTE_FILTER_BUTTON = By.className("adi-attr-filter-button");
    private static final By BY_FACT_AGGREGATION = By.className("s-fact-aggregation-switch");

    private static final String ADD_ATTRIBUTE_FILTER_CLASS = "s-add_attribute_filter";

    private static final String DISABLED = "is-disabled";

    public String getHeader() {
        return waitForElementVisible(className("s-title"), waitForElementVisible(header)).getText();
    }

    public MetricConfiguration showPercents() {
        waitForElementVisible(showInPercents).click();
        assertTrue(showInPercents.isSelected());
        return this;
    }

    public boolean isShowPercentEnabled() {
        return !waitForElementPresent(showInPercents).findElement(BY_PARENT).getAttribute("class")
                .contains(DISABLED);
    }

    public boolean isPopEnabled() {
        return !waitForElementPresent(compareToSamePeriod).findElement(BY_PARENT)
                .getAttribute("class").contains(DISABLED);
    }

    public boolean isShowPercentSelected() {
        return waitForElementPresent(showInPercents).isSelected();
    }

    public boolean isPopSelected() {
        return waitForElementPresent(compareToSamePeriod).isSelected();
    }

    public MetricConfiguration showPop() {
        waitForElementVisible(compareToSamePeriod).click();
        assertTrue(compareToSamePeriod.isSelected());
        return this;
    }

    public String getAggregation() {
        return new Select(getRoot().findElement(BY_FACT_AGGREGATION)).getFirstSelectedOption()
                .getText();
    }

    public Collection<String> getAllAggregations() {
        return getElementTexts(new Select(getRoot().findElement(BY_FACT_AGGREGATION)).getOptions());
    }

    public void changeAggregation(String newAggregation) {
        new Select(getRoot().findElement(BY_FACT_AGGREGATION)).selectByVisibleText(newAggregation);
    }

    public MetricConfiguration expandConfiguration() {
        if (isConfigurationCollapsed()) {
            waitForElementVisible(header).click();
        }
        return this;
    }

    public void collapseConfiguration() {
        if (isConfigurationCollapsed()) {
            return;
        }
        waitForElementVisible(header).click();
    }

    public boolean isConfigurationCollapsed() {
        return waitForElementVisible(header).getAttribute("class").contains("collapsed");
    }

    public void addFilter(String attribute, String... values) {
        waitForElementVisible(addAttributeFilter).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAttribute(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectItems(values)
                .apply();
    }

    public MetricConfiguration addFilterBySelectOnly(String attribute, String value) {
        waitForElementVisible(addAttributeFilter).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAttribute(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectOnly(value)
                .apply();
        return this;
    }

    public void addFilterWithLargeNumberValues(String attribute, String... unselectedValues) {
        waitForElementVisible(addAttributeFilter).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAttribute(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAll()
                .selectItems(unselectedValues)
                .apply();
    }

    public String getFilterText() {
        return waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()).getText();
    }

    public boolean canAddAnotherFilter() {
        return isElementPresent(className(ADD_ATTRIBUTE_FILTER_CLASS), getRoot());
    }

    public MetricConfiguration removeFilter() {
        waitForElementVisible(BY_REMOVE_ATTRIBUTE_FILTER, getRoot()).click();
        return this;
    }

    public String getAttributeDescription(String attribute) {
        waitForElementVisible(addAttributeFilter).click();
        return Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser)).getAttributeDescription(attribute);
    }

    public class AttributeFilterPicker extends AbstractFragment {

        @FindBy(className = "searchfield-input")
        private WebElement searchInput;

        @FindBy(className = "s-clear")
        private WebElement clearButton;

        @FindBy(className = "s-select_all")
        private WebElement selectAllButton;

        @FindBy(xpath = "//*[contains(@class, 'adi-filter-item')]")
        private List<WebElement> items;

        @FindBy(css = ".s-btn-apply:not(.disabled)")
        private WebElement applyButton;

        private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

        public AttributeFilterPicker clear() {
            waitForElementVisible(clearButton).click();
            return this;
        }

        public AttributeFilterPicker selectAll() {
            waitForElementVisible(selectAllButton).click();
            return this;
        }

        public AttributeFilterPicker selectOnly(String element) {
            searchItem(element);
            WebElement ele = waitForCollectionIsNotEmpty(items).stream()
                    .filter(item -> element.equals(item.findElement(tagName("span")).getText()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element));
            getActions().moveToElement(ele).perform();
            waitForElementVisible(className("gd-list-item-only"), ele).click();
            return this;
        }

        public String getAttributeDescription(String element) {
            searchItem(element);
            WebElement ele = waitForCollectionIsNotEmpty(items).stream()
                    .filter(item -> element.equals(item.findElement(cssSelector(".attr-field-icon + span")).getText()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element));
            getActions().moveToElement(ele).perform();
            getActions().moveToElement(waitForElementPresent(cssSelector(".inlineBubbleHelp"), ele)).perform();

            return Graphene.createPageFragment(DescriptionPanel.class,
                    waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
        }

        public void selectAttribute(String element) {
            searchItem(element);
            waitForCollectionIsNotEmpty(items).stream()
                .map(item -> item.findElement(cssSelector(".attr-field-icon + span")))
                .filter(item -> element.equals(item.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element))
                .click();
        }

        public AttributeFilterPicker selectItems(String... items) {
            Stream.of(items).forEach(this::selectInputItem);
            return this;
        }

        public void apply() {
            waitForElementVisible(applyButton).click();
            waitForElementNotVisible(getRoot());
        }

        private void selectInputItem(String element) {
            searchItem(element);
            waitForCollectionIsNotEmpty(items).stream()
                .filter(item -> element.equals(item.findElement(tagName("span")).getText()))
                .findFirst()
                .map(item -> item.findElement(tagName("input")))
                .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element))
                .click();
        }

        private void searchItem(String name) {
            waitForElementVisible(this.getRoot());

            clearSearchField();
            searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
            waitForCollectionIsEmpty(items);

            clearSearchField();
            searchInput.sendKeys(name);
            waitForCollectionIsNotEmpty(items);
        }

        private void clearSearchField() {
            final By searchFieldClear = className("searchfield-clear");
            if (isElementPresent(searchFieldClear, getRoot())) {
                waitForElementVisible(searchFieldClear, getRoot()).click();
            } else {
                waitForElementVisible(searchInput).clear();
            }
        }
    }
}
