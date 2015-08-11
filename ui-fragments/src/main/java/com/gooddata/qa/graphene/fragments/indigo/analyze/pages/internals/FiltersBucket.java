package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy");

    public void addFilter(WebElement filter) {
        int oldFiltersCount = filters.size();
        new Actions(browser).dragAndDrop(filter, waitForElementVisible(addFilterBucket)).perform();
        sleepTightInSeconds(2);
        assertEquals(filters.size(), oldFiltersCount + 1, "Filter is not added successfully");

        final WebElement newFilter = filters.get(filters.size() - 1);
        final String expectedText = ": All" + (isDateFilter(newFilter) ? " time" : "");
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            public boolean apply(WebDriver input) {
                return getFilterTextHelper(newFilter).endsWith(expectedText);
            };
        });
    }

    public void removeFilter(final String dateOrAttribute) {
        int oldFiltersCount = filters.size();
        WebElement filter = getFilter(dateOrAttribute);

        Actions action = new Actions(browser);
        action.clickAndHold(filter).moveToElement(addFilterBucket).perform();
        action.moveToElement(waitForElementPresent(BY_TRASH_PANEL, browser)).perform();
        action.release().perform();

        assertEquals(filters.size(), oldFiltersCount - 1, "Filter is not removed successfully");
    }

    public boolean isBlankState() {
        return filters.size() == 0;
    }

    public void configTimeFilter(String period) {
        WebElement filter = getDateFilter();
        filter.click();
        Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser)).select(period);
        assertTrue(getFilterTextHelper(filter).endsWith(": " + period));
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
        if (filters.isEmpty()) {
            return false;
        }

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

    public String getDateFilterText() {
        waitForCollectionIsNotEmpty(filters);
        return getFilterTextHelper(getDateFilter());
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

    public WebElement getDateFilter() {
        waitForCollectionIsNotEmpty(filters);
        return Iterables.find(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return waitForFilterLoaded(input).getAttribute("class").contains("adi-date-filter-button");
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

    public List<String> getAllTimeFilterOptions() {
        WebElement filter = getDateFilter();
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        List<String> ret = panel.getAllPeriods();
        filter.click();
        waitForFragmentNotVisible(panel);
        return ret;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public void configTimeFilterByRangeButNotApply(String dateFilter, String from, String to) {
        WebElement filter = getFilter(dateFilter);
        String oldFilterText = getFilterTextHelper(filter);
        openDatePanelOfFilter(filter).configTimeFilterByRangeButNotApply(from, to);
        assertEquals(getFilterTextHelper(filter), oldFilterText);
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     * @throws ParseException 
     */
    public void configTimeFilterByRange(String dateFilter, String from, String to) throws ParseException {
        WebElement filter = getFilter(dateFilter);
        openDatePanelOfFilter(filter).configTimeFilterByRange(from, to);
        assertEquals(getFilterTextHelper(filter),
                dateFilter + ": " + getAnotherTimeFormat(from) + " – " + getAnotherTimeFormat(to));
    }

    private String getAnotherTimeFormat(String time) throws ParseException {
        return outputFormat.format(inputFormat.parse(time));
    }

    public void changeDimensionSwitchInFilter(String currentRelatedDate, String dimensionSwitch) {
        WebElement filter = getFilter(currentRelatedDate);
        openDatePanelOfFilter(filter).changeDimensionSwitchInFilter(dimensionSwitch);
    }

    public boolean isDateFilterVisible() {
        if (filters.isEmpty()) {
            return false;
        }

        try {
            getDateFilter();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private DateFilterPickerPanel openDatePanelOfFilter(WebElement filter) {
        filter.click();
        return Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
    }
}
