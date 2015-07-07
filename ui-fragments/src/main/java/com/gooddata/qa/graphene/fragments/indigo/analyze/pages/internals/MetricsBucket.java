package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
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

    public void addMetric(WebElement metric) {
        new Actions(browser).dragAndDrop(metric, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains(metric.getText().trim()));
    }

    public void addMetricFromFact(WebElement fact) {
        new Actions(browser).dragAndDrop(fact, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains("Sum of " + fact.getText().trim()));
    }

    public void addMetricFromAttribute(WebElement attribute) {
        new Actions(browser).dragAndDrop(attribute, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains("Count of " + attribute.getText().trim()));
    }

    public void replaceMetric(String oldMetric, WebElement newMetric) {
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

    public String getStackByMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }

    public String getFactAggregation(String fact) {
        return getFactAggregationByIndex(fact, 0);
    }

    public String getFactAggregationByIndex(String fact, int index) {
        return new Select(getFactByIndex(fact, index).findElement(BY_FACT_AGGREGATION)).getFirstSelectedOption()
                .getText();
    }

    public Collection<String> getAllFactAggregations(String fact) {
        return Collections2.transform(new Select(getFact(fact).findElement(BY_FACT_AGGREGATION)).getOptions(),
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        });
    }

    public void changeAggregationOfFact(String fact, String newAggregation) {
        new Select(getFact(fact).findElement(BY_FACT_AGGREGATION)).selectByVisibleText(newAggregation);
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

    private WebElement getMetric(final String name) {
        List<WebElement> items = getItems(name);
        if (items.isEmpty()) {
            throw new NoSuchElementException("Cannot find metric: " + name);
        }
        return items.get(0);
    }

    private WebElement getFact(final String name) {
        return getFactByIndex(name, 0);
    }

    private WebElement getFactByIndex(final String name, final int index) {
        List<WebElement> items = getItems(name);
        if (items.isEmpty()) {
            throw new NoSuchElementException("Cannot find fact: " + name);
        }
        return items.get(index);
    }

    private List<WebElement> getItems(final String name) {
        List<WebElement> result = Lists.newArrayList();

        for (WebElement input : items) {
            if (name.equals(getHeaderFrom(input).getText())) {
                result.add(input);
            }
        }

        return result;
    }

    private boolean isMetricConfigurationCollapsed(WebElement metric) {
        return getHeaderFrom(metric).getAttribute("class").contains("collapsed");
    }

    private WebElement getHeaderFrom(WebElement metric) {
        return metric.findElement(BY_HEADER);
    }
}
