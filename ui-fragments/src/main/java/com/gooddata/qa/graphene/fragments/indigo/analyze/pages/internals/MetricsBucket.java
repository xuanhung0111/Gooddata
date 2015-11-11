package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MetricsBucket extends AbstractFragment {

    @FindBy(css = ".adi-bucket-configuration .s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(css = ".adi-bucket-configuration .s-show-pop")
    private WebElement compareToSamePeriod;

    @FindBy(css = ".adi-bucket-item")
    private List<WebElement> items;

    private static final String DISABLED = "is-disabled";
    private static final String EMPTY = "s-bucket-empty";

    private static final By BY_TRASH_PANEL = By.cssSelector(".adi-trash-panel");
    private static final By BY_BUCKET_INVITATION = By.cssSelector(".adi-bucket-invitation");
    private static final By BY_STACK_WARNING = By.className("adi-stack-warn");
    public static final By BY_FACT_AGGREGATION = By.className("s-fact-aggregation-switch");
    private static final By BY_HEADER = By.className("adi-bucket-item-header");
    private static final By BY_ADD_ATTRIBUTE_FILTER = By.className("s-btn-add_attribute_filter");
    private static final By BY_REMOVE_ATTRIBUTE_FILTER = By.className("remove-attribute-filter");
    private static final By BY_ATTRIBUTE_FILTER_PICKER = By.className("adi-attr-filter-picker");
    private static final By BY_ATTRIBUTE_FILTER_BUTTON = By.className("adi-attr-filter-button");

    public void addMetric(final WebElement metric) {
        new Actions(browser).dragAndDrop(metric, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains(metric.getText().trim()));
    }

    public void addMetricFromFact(final WebElement fact) {
        new Actions(browser).dragAndDrop(fact, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains("Sum of " + fact.getText().trim()));
    }

    public void addMetricFromAttribute(final WebElement attribute) {
        new Actions(browser).dragAndDrop(attribute, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains("Count of " + attribute.getText().trim()));
    }

    public void replaceMetric(String oldMetric, final WebElement newMetric) {
        new Actions(browser).dragAndDrop(newMetric, getMetric(oldMetric)).perform();
        assertTrue(getItemNames().contains(newMetric.getText().trim()));
    }

    public boolean isWarningMessageShown() {
        return false;
    }

    public List<String> getItemNames() {
        return Lists.newArrayList(Collections2.transform(items, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return getHeaderFrom(input).getText();
            }
        }));
    }

    public void removeMetric(final String metric) {
        int oldItemsCount = items.size();
        WebElement element = Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return metric.equals(getHeaderFrom(input).getText());
            }
        });

        Actions action = new Actions(browser);
        WebElement catalogue = browser.findElement(By.className("s-catalogue"));
        Point location = catalogue.getLocation();
        Dimension dimension = catalogue.getSize();
        action.clickAndHold(element).moveByOffset(location.x + dimension.width/2, location.y + dimension.height/2)
            .perform();
        action.moveToElement(waitForElementPresent(BY_TRASH_PANEL, browser)).perform();
        action.release().perform();

        assertEquals(items.size(), oldItemsCount - 1, "Metric is not removed yet!");
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public void turnOnShowInPercents() {
        waitForElementVisible(showInPercents).click();
        assertTrue(showInPercents.isSelected());
    }

    public boolean isShowPercentConfigEnabled() {
        return !waitForElementPresent(showInPercents).findElement(BY_PARENT).getAttribute("class")
                .contains(DISABLED);
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        return !waitForElementPresent(compareToSamePeriod).findElement(BY_PARENT)
                .getAttribute("class").contains(DISABLED);
    }

    public boolean isShowPercentConfigSelected() {
        return waitForElementPresent(showInPercents).isSelected();
    }

    public boolean isCompareSamePeriodConfigSelected() {
        return waitForElementPresent(compareToSamePeriod).isSelected();
    }

    public void compareToSamePeriodOfYearBefore() {
        waitForElementVisible(compareToSamePeriod).click();
        assertTrue(compareToSamePeriod.isSelected());
    }

    public String getMetricMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }

    public String getMetricAggregation(String metric) {
        return new Select(getMetric(metric).findElement(BY_FACT_AGGREGATION)).getFirstSelectedOption()
                .getText();
    }

    public Collection<String> getAllMetricAggregations(String metric) {
        return Collections2.transform(new Select(getMetric(metric).findElement(BY_FACT_AGGREGATION)).getOptions(),
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        });
    }

    public void changeMetricAggregation(String metric, String newAggregation) {
        new Select(getMetric(metric).findElement(BY_FACT_AGGREGATION)).selectByVisibleText(newAggregation);
    }

    public boolean isMetricConfigurationCollapsed(String name) {
        return isMetricConfigurationCollapsed(getMetric(name));
    }

    public void expandMetricConfiguration(String name) {
        WebElement metric = getMetric(name);
        if (!isMetricConfigurationCollapsed(metric)) {
            return;
        }
        getHeaderFrom(metric).click();
    }

    public void collapseMetricConfiguration(String name) {
        WebElement metric = getMetric(name);
        if (isMetricConfigurationCollapsed(metric)) {
            return;
        }
        getHeaderFrom(metric).click();
    }

    public void addFilterMetric(String metric, String attribute, String... values) {
        getMetric(metric).findElement(BY_ADD_ATTRIBUTE_FILTER).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectTextItem(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectItems(values)
                .apply();
    }

    public void addFilterMetricBySelectOnly(String metric, String attribute, String value) {
        getMetric(metric).findElement(BY_ADD_ATTRIBUTE_FILTER).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectTextItem(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectOnly(value)
                .apply();
    }

    public void addFilterMetricWithLargeNumberValues(String metric, String attribute, String... unselectedValues) {
        getMetric(metric).findElement(BY_ADD_ATTRIBUTE_FILTER).click();

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectTextItem(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAll()
                .selectItems(unselectedValues)
                .apply();
    }

    public String getFilterMetricText(String metric) {
        return getMetric(metric).findElement(BY_ATTRIBUTE_FILTER_BUTTON).getText();
    }

    public boolean canAddAnotherAttributeFilterToMetric(String metric) {
        return getMetric(metric).findElements(BY_ADD_ATTRIBUTE_FILTER).size() > 0;
    }

    public void removeAttributeFilterFromMetric(String metric) {
        getMetric(metric).findElement(BY_REMOVE_ATTRIBUTE_FILTER).click();
    }

    public String getAttributeDescription(String metric, String attribute) {
        getMetric(metric).findElement(BY_ADD_ATTRIBUTE_FILTER).click();
        return Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser)).getDescription(attribute);
    }

    private WebElement getMetric(final String name) {
        WebElement item = getItem(name);
        if (item == null) {
            throw new NoSuchElementException("Cannot find metric: " + name);
        }
        return item;
    }

    private WebElement getItem(final String name) {
        for (WebElement input : items) {
            if (name.equals(getHeaderFrom(input).findElement(By.className("s-title")).getText())) {
                return input;
            }
        }

        return null;
    }

    private boolean isMetricConfigurationCollapsed(WebElement metric) {
        return getHeaderFrom(metric).getAttribute("class").contains("collapsed");
    }

    private WebElement getHeaderFrom(WebElement metric) {
        return metric.findElement(BY_HEADER);
    }

    public class AttributeFilterPicker extends AbstractFragment {

        @FindBy(className = "searchfield-input")
        private WebElement searchInput;

        @FindBy(className = "s-clear")
        private WebElement clearButton;

        @FindBy(className = "s-select_all")
        private WebElement selectAllButton;

        @FindBy(css = ".gd-list-item")
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
            WebElement ele = items.stream()
                    .filter(item -> element.equals(item.findElement(tagName("span")).getText()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element));
            new Actions(browser).moveToElement(ele).perform();
            waitForElementVisible(className("gd-list-item-only"), ele).click();
            return this;
        }

        public String getDescription(String element) {
            searchItem(element);
            WebElement ele = items.stream()
                    .map(item -> item.findElement(cssSelector("span:last-child")))
                    .filter(item -> element.equals(item.getText()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element));
            new Actions(browser).moveToElement(ele).perform();

            return Graphene.createPageFragment(DescriptionPanel.class,
                    waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
        }

        public void selectTextItem(String element) {
            searchItem(element);
            items.stream()
                .map(item -> item.findElement(cssSelector("span:last-child")))
                .filter(item -> element.equals(item.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find: " + element))
                .click();
        }

        private void selectInputItem(String element) {
            searchItem(element);
            items.stream()
                .filter(item -> element.equals(item.findElement(tagName("span")).getText()))
                .findFirst()
                .map(item -> item.findElement(tagName("input")))
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
}
