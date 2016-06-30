package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.text.ParseException;
import java.util.Calendar;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
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

        takeScreenshot(browser, "Selected-date-filter-applied", getClass());
        assertEquals(getValueFrom(".s-filter-date-date-dataset-switch"), "Created");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_picker() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .getDateFilter()
            .click();
        assertTrue(isElementPresent(cssSelector(".s-filter-picker"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_selection_if_date_dimensions_reloaded_in_the_background() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .changeDateDimension("Activity", "Created");

        takeScreenshot(browser, "Date-filter-applied-on-filter-buckets", getClass());
        assertEquals(getValueFrom(".s-filter-date-date-dataset-switch"), "Created");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_prefill_interval_filters_when_floating_filter_is_selected() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .configDateFilter("Last quarter")
            .getDateFilter()
            .click();

        waitForElementVisible(cssSelector(".s-filter-picker .s-tab-range"), browser).click();

        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-from input"), browser));
        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-to input"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_support_date_ranges() throws ParseException {
        assertTrue(analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .configDateFilter("11/17/2015", "11/19/2015")
            .getDateFilterText().contains("Nov 17, 2015 - Nov 19, 2015"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_correct_ranges_when_editing() {
        analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .getDateFilter()
            .click();
        waitForElementVisible(cssSelector(".s-filter-picker .s-tab-range"), browser).click();

        String nextYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1);
        fillInDateRange(".s-interval-from input", "01/01/" + nextYear);
        assertEquals(waitForElementVisible(cssSelector(".s-interval-to input"), browser).getAttribute("value"),
                "01/01/" + nextYear);

        fillInDateRange(".s-interval-to input", "01/01/2003");
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");

        fillInDateRange(".s-interval-to input", "01/01/200");
        waitForElementVisible(cssSelector(".adi-tab-range"), browser).click();
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");
    }

    private String getValueFrom(String locator) {
        return waitForElementVisible(cssSelector(locator), browser).getText();
    }

    private void fillInDateRange(String cssLocator, String date) {
        WebElement elem = waitForElementVisible(cssSelector(cssLocator), browser);
        for (int i = 0, n = elem.getAttribute("value").trim().length(); i < n; i++) {
            elem.sendKeys(Keys.BACK_SPACE);
        }
        elem.sendKeys(date, Keys.ENTER);
    }
}
