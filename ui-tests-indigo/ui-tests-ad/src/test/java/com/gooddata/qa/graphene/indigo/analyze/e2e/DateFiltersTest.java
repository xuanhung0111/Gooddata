package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import java.util.Calendar;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class DateFiltersTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Date-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_add_and_remove_date_from_filter_bucket() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " .s-date-filter");

        // try to drag a second date filter
        dragFromCatalogue(DATE, FILTERS_BUCKET);

        drag(FILTERS_BUCKET + " .s-date-filter", TRASH);
        expectMissing(FILTERS_BUCKET + " .s-date-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reflect_changes_in_category_bucket() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        select(CATEGORIES_BUCKET + " .s-date-dimension-switch", "created.dim_date");

        click(".s-date-filter .s-filter-button");

        assertEquals(getValueFrom(".s-filter-date-dimension-switch"), "created.dim_date");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_picker() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " .s-date-filter");

        click(".s-date-filter .s-filter-button");
        expectFind("#gd-overlays .s-filter-picker");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_selection_if_date_dimensions_reloaded_in_the_background() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);

        click(".s-date-filter .s-filter-button");
        select(".s-filter-date-dimension-switch", "created.dim_date");
        assertEquals(getValueFrom(".s-filter-date-dimension-switch"), "created.dim_date");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reflect_selection_changes() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " .s-date-filter");

        click(".s-date-filter .s-filter-button");
        assertThat(waitForElementVisible(cssSelector(FILTERS_BUCKET + " .s-date-filter"), browser)
            .getText(), containsString("Activity: All time"));

        click("#gd-overlays .s-filter-picker .s-filter-last_year");

        assertThat(waitForElementVisible(cssSelector(FILTERS_BUCKET + " .s-date-filter"), browser)
                .getText(), containsString("Activity: Last year"));
        expectFind(FILTERS_BUCKET + " .s-date-filter" + yearActivityLabel);
        expectFind(FILTERS_BUCKET + " .s-date-filter.s-where-___between____1__1__");

        expectFind(".adi-components .adi-component .s-property-where" + yearActivityLabel);
        expectFind(".adi-components .adi-component .s-property-where.s-where-___between____1__1__");

        click(".s-date-filter .s-filter-button");
        click("#gd-overlays .s-filter-picker .s-filter-last_12_months");

        assertThat(waitForElementVisible(cssSelector(FILTERS_BUCKET + " .s-date-filter"), browser)
                .getText(), containsString("Activity: Last 12 months"));
        expectFind(FILTERS_BUCKET + " .s-date-filter" + monthYearActivityLabel);
        expectFind(FILTERS_BUCKET + " .s-date-filter.s-where-___between____11_0__");

        expectFind(".adi-components .adi-component .s-property-where" + monthYearActivityLabel);
        expectFind(".adi-components .adi-component .s-property-where.s-where-___between____11_0__");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_filters_on_all_time() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " .s-date-filter");

        click(".s-date-filter .s-filter-button");
        expectFind("#gd-overlays .s-filter-picker");
        click("#gd-overlays .s-filter-picker .s-filter-all_time");

        expectFind(".adi-components .adi-component");
        expectMissing(".adi-components .adi-component.s-property-where");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_prefill_interval_filters_when_floating_filter_is_selected() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);
        click(".s-date-filter .s-filter-button");
        click(".s-filter-picker .s-filter-last_quarter");

        click(".s-date-filter .s-filter-button");
        click(".s-filter-picker .s-tab-date-range");

        expectFind(".s-filter-picker .s-interval-from input");
        expectFind(".s-filter-picker .s-interval-to input");

        click(".s-filter-picker .s-date-range-cancel");
        expectMissing(".s-filter-picker");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_support_date_ranges() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);

        click(".s-date-filter .s-filter-button");
        click(".s-filter-picker .s-tab-date-range");

        fillInDateRange(".s-interval-from input", "11/17/2015");
        fillInDateRange(".s-interval-to input", "11/19/2015");

        click(".s-filter-picker .s-date-range-apply");

        assertThat(waitForElementVisible(cssSelector(".s-date-filter span"), browser).getText(),
                containsString("Nov 17, 2015 – Nov 19, 2015"));

        expectMissing(".s-filter-picker");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_correct_ranges_when_editing() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);

        click(".s-date-filter .s-filter-button");
        click(".s-filter-picker .s-tab-date-range");

        String nextYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1);
        fillInDateRange(".s-interval-from input", "01/01/" + nextYear);
        assertEquals(waitForElementVisible(cssSelector(".s-interval-to input"), browser).getAttribute("value"),
                "01/01/" + nextYear);
        fillInDateRange(".s-interval-to input", "01/01/2003");
        assertEquals(waitForElementVisible(cssSelector(".s-interval-from input"), browser).getAttribute("value"),
                "01/01/2003");
        fillInDateRange(".s-interval-to input", "01/01/200");
        click(".adi-tab-date-range");
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
