package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FiltersBucket extends AbstractFragment {

    @FindBy(css = ".adi-bucket-item>button")
    private List<WebElement> filters;

    @FindBy(css = ".adi-filter-bucket-invitation")
    private WebElement addFilterBucket;

    private static final String CSS_DATE_FILTER = "s-date-filter";
    private static final String LOADING = "...";
    private static final By BY_FILTER_TEXT = By.cssSelector(".button-text");
    private static final By BY_TRASH_PANEL = By.cssSelector(".adi-trash-panel");

    public void addFilter(WebElement filter) {
        int oldFiltersCount = filters.size();
        new Actions(browser).dragAndDrop(filter, waitForElementVisible(addFilterBucket)).perform();
        assertEquals(filters.size(), oldFiltersCount + 1, "Filter is not added successfully");

        final WebElement newFilter = filters.get(filters.size() - 1);
        final String expectedText =
                filter.getText() + ": All" + (isDateFilter(newFilter) ? " time" : "");
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            public boolean apply(WebDriver input) {
                return expectedText.equals(getFilterTextHelper(newFilter));
            };
        });
    }

    public void removeFilter(final String dateOrAttribute) {
        int oldFiltersCount = filters.size();
        WebElement filter = getFilter(dateOrAttribute);

        new Actions(browser).dragAndDrop(filter, waitForElementPresent(BY_TRASH_PANEL, browser))
                .perform();
        assertEquals(filters.size(), oldFiltersCount - 1, "Filter is not removed successfully");
    }

    public boolean isBlankState() {
        return filters.size() == 0;
    }

    public void configTimeFilter(String period) {
        WebElement filter = getFilter("Date");
        filter.click();
        Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser)).select(period);
        assertEquals(getFilterTextHelper(filter), "Date: " + period);
    }

    public void configAttributeFilter(String attribute, String... values) {
        WebElement filter = getFilter(attribute);
        String oldText = getFilterTextHelper(filter);
        filter.click();
        Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                waitForElementVisible(AttributeFilterPickerPanel.LOCATOR, browser)).select(values);
        assertNotEquals(getFilterTextHelper(filter), oldText);
    }

    private boolean isDateFilter(WebElement filter) {
        return filter.findElement(BY_PARENT).getAttribute("class").contains(CSS_DATE_FILTER);
    }

    public boolean isFilterVisible(String dateOrAttribute) {
        try {
            getFilter(dateOrAttribute);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }

    }

    public String getFilterText(String dateOrAttribute) {
        waitForCollectionIsNotEmpty(filters);
        return getFilterTextHelper(getFilter(dateOrAttribute));
    }

    public WebElement getFilter(final String dateOrAttribute) {
        waitForCollectionIsNotEmpty(filters);
        return Iterables.find(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return waitForFilterLoaded(input).findElement(BY_FILTER_TEXT).getText()
                        .startsWith(dateOrAttribute + ": ");
            }
        });
    }

    private String getFilterTextHelper(WebElement filter) {
        return filter.findElement(BY_FILTER_TEXT).getText();
    }

    private WebElement waitForFilterLoaded(final WebElement filter) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !LOADING.equals(filter.findElement(BY_FILTER_TEXT).getText());
            }
        });
        return filter;
    }
}
