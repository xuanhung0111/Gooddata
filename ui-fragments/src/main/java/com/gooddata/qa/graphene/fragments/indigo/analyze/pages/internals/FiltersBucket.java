package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
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
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Predicate;

public class FiltersBucket extends AbstractBucket {

    @FindBy(css = ".adi-bucket-item > button")
    private List<WebElement> filters;

    private static final String LOADING = "...";
    private static final By BY_FILTER_TEXT = By.cssSelector(".button-text");

    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("MMM d, yyyy");

    public FiltersBucket configTimeFilter(String period) {
        WebElement filter = getDateFilter();
        filter.click();
        Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser)).select(period);
        assertTrue(getFilterTextHelper(filter).endsWith(": " + period));
        return this;
    }

    public FiltersBucket configAttributeFilter(String attribute, String... values) {
        WebElement filter = getFilter(attribute);
        String oldText = getFilterTextHelper(filter);
        filter.click();
        Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                waitForElementVisible(AttributeFilterPickerPanel.LOCATOR, browser)).select(values);
        assertNotEquals(getFilterTextHelper(filter), oldText);
        return this;
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
        return waitForCollectionIsNotEmpty(filters).stream()
            .filter(e -> waitForFilterLoaded(e).findElement(BY_FILTER_TEXT).getText().startsWith(dateOrAttribute + ": "))
            .findFirst()
            .get();
    }

    public WebElement getDateFilter() {
        return waitForCollectionIsNotEmpty(filters).stream()
            .filter(e -> waitForFilterLoaded(e).getAttribute("class").contains("adi-date-filter-button"))
            .findFirst()
            .get();
    }

    private String getFilterTextHelper(WebElement filter) {
        return filter.findElement(BY_FILTER_TEXT).getText();
    }

    private WebElement waitForFilterLoaded(final WebElement filter) {
        Predicate<WebDriver> filterLoaded = browser -> !LOADING.equals(getFilterTextHelper(filter));
        Graphene.waitGui().until(filterLoaded);
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
        return OUTPUT_FORMAT.format(INPUT_FORMAT.parse(time));
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

    @Override
    public String getWarningMessage() {
        throw new UnsupportedOperationException();
    }
}
