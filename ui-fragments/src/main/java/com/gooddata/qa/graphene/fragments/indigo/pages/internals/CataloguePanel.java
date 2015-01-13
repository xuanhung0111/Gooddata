package com.gooddata.qa.graphene.fragments.indigo.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.description.DescriptionPanel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CataloguePanel extends AbstractFragment {

    @FindBy(css = ".searchfield-input")
    private WebElement searchInput;

    @FindBy(css = ".catalogue-container div.adi-bucket-item-handle>div")
    private List<WebElement> items;

    private static final By BY_INLINE_HELP = By.cssSelector(".inlineBubbleHelp");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    private static final String METRIC_TYPE = "type-metric";
    private static final String ATTRIBUTE_TYPE = "type-attribute";
    private static final String DATE_TYPE = "type-date";

    public WebElement getMetric(String metric) {
        return searchAndGetItem(metric, METRIC_TYPE);
    }

    public WebElement getCategory(String category) {
        return searchAndGetItem(category, ATTRIBUTE_TYPE);
    }

    public WebElement getTime(String filter) {
        return searchAndGetItem(filter, DATE_TYPE);
    }

    public String getTimeDescription(String time) {
        WebElement field = getTime(time);
        WebElement iconElement = field.findElement(BY_PARENT).findElement(BY_INLINE_HELP);
        new Actions(browser).moveToElement(field).moveToElement(iconElement).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getTimeDescription();
    }

    public String getAttributeDescription(String attribute) {
        WebElement field = getCategory(attribute);
        WebElement iconElement = field.findElement(BY_PARENT).findElement(BY_INLINE_HELP);
        new Actions(browser).moveToElement(field).moveToElement(iconElement).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
    }

    public String getMetricDescription(String metric) {
        WebElement field = getMetric(metric);
        WebElement iconElement = field.findElement(BY_PARENT).findElement(BY_INLINE_HELP);
        new Actions(browser).moveToElement(field).moveToElement(iconElement).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getMetricDescription();
    }

    private void waitForItemLoaded() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            public boolean apply(WebDriver input) {
                return browser.findElements(
                        By.cssSelector(".catalogue-container .gd-list-view-loading")).isEmpty();
            };
        });
    }

    private WebElement searchAndGetItem(final String item, final String type) {
        waitForItemLoaded();
        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        searchInput.clear();
        searchInput.sendKeys(item);
        waitForCollectionIsNotEmpty(items);

        return Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return item.equals(input.getText().trim())
                        && input.findElement(BY_PARENT).getAttribute("class").contains(type);
            }
        });
    }
}
