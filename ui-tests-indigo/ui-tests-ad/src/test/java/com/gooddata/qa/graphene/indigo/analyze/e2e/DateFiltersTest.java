package com.gooddata.qa.graphene.indigo.analyze.e2e;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.text.ParseException;
import java.util.Calendar;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import org.jboss.arquillian.graphene.Graphene;

public class DateFiltersTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Date-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_add_and_remove_date_from_filter_bucket() {
        assertTrue(initAnalysePage().addDateFilter()
                .removeDateFilter()
                .getFilterBuckets()
                .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_not_be_possible_to_add_second_date_to_filter_bucket() {
        assertEquals(initAnalysePage().addDateFilter()
                .addDateFilter() // try to drag a second date filter
                .getFilterBuckets()
                .getFiltersCount(), 1);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_reflect_changes_in_category_bucket() {
        initAnalysePage().addDate()
                .getAttributesBucket()
                .changeDateDimension("Created");

        analysisPage.getFilterBuckets()
                .getDateFilter()
                .click();

        takeScreenshot(browser, "Selected-date-filter-applied", getClass());
        assertEquals(getValueFrom(".s-filter-date-date-dataset-switch"), "Created");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_display_picker() {
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .getDateFilter()
                .click();
        assertTrue(isElementPresent(cssSelector(".s-filter-picker"), browser));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_keep_selection_if_date_dimensions_reloaded_in_the_background() {
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .changeDateDimension("Activity", "Created");

        analysisPage.getFilterBuckets()
                .getDateFilter()
                .click();

        takeScreenshot(browser, "Date-filter-applied-on-filter-buckets", getClass());
        assertEquals(getValueFrom(".s-filter-date-date-dataset-switch"), "Created");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_prefill_interval_filters_when_floating_filter_is_selected() {
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .configDateFilter("Last quarter")
                .getDateFilter()
                .click();

        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.selectStaticPeriod();

        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-from input"), browser));
        assertTrue(isElementPresent(cssSelector(".s-filter-picker .s-interval-to input"), browser));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_support_date_ranges() throws ParseException {
        assertTrue(initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .configDateFilter("11/17/2015", "11/19/2015")
                .getDateFilterText().contains("Nov 17, 2015 - Nov 19, 2015"));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_correct_ranges_when_editing() {
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .getDateFilter()
                .click();

        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.selectStaticPeriod();

        String nextYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1);
        panel.fillInDateRange(waitForElementVisible(cssSelector(".s-interval-from input"), browser), "01/01/" + nextYear);
        assertEquals(waitForElementVisible(cssSelector(".s-interval-to input"), browser).getAttribute("value"),
                "01/01/" + nextYear);

        panel.fillInDateRange(waitForElementVisible(cssSelector(".s-interval-to input"), browser), "01/01/2003");
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");

        panel.fillInDateRange(waitForElementVisible(cssSelector(".s-interval-to input"), browser), "01/01/200");
        waitForElementVisible(cssSelector(".s-interval-from input"), browser).click();
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");
    }

    private String getValueFrom(String locator) {
        return waitForElementVisible(cssSelector(locator), browser).getText();
    }
}
