package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class DateFiltersTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Date-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_add_and_remove_date_from_filter_bucket() {
        assertTrue(analysisPageReact.addDateFilter()
            // try to drag a second date filter
            .addDateFilter()
            .removeDateFilter()
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reflect_changes_in_category_bucket() {
        analysisPageReact.addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPageReact.getFilterBuckets()
            .getDateFilter()
            .click();

        assertEquals(getValueFrom(".s-filter-date-dimension-switch"), "created.dim_date");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_picker() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .getDateFilter()
            .click();
        assertTrue(isElementPresent(cssSelector("#gd-overlays .s-filter-picker"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_selection_if_date_dimensions_reloaded_in_the_background() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .changeDateDimension("Activity", "Created");
        assertEquals(getValueFrom(".s-filter-date-dimension-switch"), "created.dim_date");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reflect_selection_changes() {
        assertEquals(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDateFilter()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: All time");

        assertEquals(analysisPageReact.getFilterBuckets()
            .configDateFilter("Last year")
            .getDateFilterText(), "Activity: Last year");

        analysisPageReact.waitForReportComputing();
        String yearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Year (Activity)");
        assertTrue(isElementPresent(cssSelector(".s-date-filter" + yearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(".s-date-filter.s-where-___between____1__1__"), browser));

        assertTrue(isElementPresent(cssSelector(
                ".adi-components .adi-component .s-property-where" + yearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .adi-component .s-property-where.s-where-___between____1__1__"), browser));

        assertEquals(analysisPageReact.getFilterBuckets()
            .configDateFilter("Last 12 months")
            .getDateFilterText(), "Activity: Last 12 months");

        analysisPageReact.waitForReportComputing();
        String monthYearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Month/Year (Activity)", "Short");
        assertTrue(isElementPresent(cssSelector(".s-date-filter" + monthYearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(".s-date-filter.s-where-___between____11_0__"), browser));

        assertTrue(isElementPresent(cssSelector(
                ".adi-components .adi-component .s-property-where" + monthYearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .adi-component .s-property-where.s-where-___between____11_0__"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_filters_on_all_time() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDateFilter()
            .getFilterBuckets()
            .configDateFilter("All time");

        analysisPageReact.waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-components .adi-component"), browser));
        assertFalse(isElementPresent(cssSelector(".adi-components .adi-component.s-property-where"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_prefill_interval_filters_when_floating_filter_is_selected() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .configDateFilter("Last quarter")
            .getDateFilter()
            .click();

        waitForElementVisible(cssSelector(".s-filter-picker .s-tab-date-range"), browser).click();

        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-from input"), browser));
        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-to input"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_support_date_ranges() throws ParseException {
        assertTrue(analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .configDateFilter("11/17/2015", "11/19/2015")
            .getDateFilterText().contains("Nov 17, 2015 – Nov 19, 2015"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_correct_ranges_when_editing() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .getDateFilter()
            .click();
        waitForElementVisible(cssSelector(".s-filter-picker .s-tab-date-range"), browser).click();

        String nextYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1);
        fillInDateRange(".s-interval-from input", "01/01/" + nextYear);
        assertEquals(waitForElementVisible(cssSelector(".s-interval-to input"), browser).getAttribute("value"),
                "01/01/" + nextYear);
        fillInDateRange(".s-interval-to input", "01/01/2003");
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");
        fillInDateRange(".s-interval-to input", "01/01/200");
        waitForElementVisible(cssSelector(".adi-tab-date-range"), browser).click();
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");
    }

    private String getValueFrom(String locator) {
        return new Select(waitForElementVisible(cssSelector(locator), browser))
            .getFirstSelectedOption()
            .getAttribute("value");
    }

    private void fillInDateRange(String cssLocator, String date) {
        String script = "Em.$('%s').val('%s').change();";
        script = format(script, cssLocator, date);
        ((JavascriptExecutor) browser).executeScript(script);
    }
}
