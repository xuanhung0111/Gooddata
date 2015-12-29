package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class NoncommonDateTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Noncommon-Date-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void date_dimension_in_chart_should_reflect_currently_selected_dimension() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        select(".s-date-dimension-switch", "created.dim_date");
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Created"));

        select(".s-date-dimension-switch", "activity.dim_date");
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Activity"));
    }

    @Test(dependsOnGroups = {"init"})
    public void enable_date_dimension_selects_correctly() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);

        click(".s-filter-button");
        expectFind(".s-filter-date-dimension-switch:not(.disabled)");
        click(".s-filter-button");

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        expectFind(CATEGORIES_BUCKET + " .s-date-dimension-switch:not(.disabled)");

        // date dimension picker in select is now disabled
        click(".s-filter-button");
        expectFind(".s-filter-picker .s-filter-date-dimension-switch[disabled]");
    }

    @Test(dependsOnGroups = {"init"})
    public void keeps_date_dimensions_in_categories_in_sync_with_the_filter() {
        visitEditor();

        dragFromCatalogue(DATE, FILTERS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        // assure there is Date dimension selected
        select(".s-date-dimension-switch", "created.dim_date");
        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Created: All time"));

        // update filter to last quarter
        click(".s-filter-button");
        click(".s-filter-last_quarter");
        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Created: Last quarter"));

        // switch date dimension to Foundation Date
        select(".s-date-dimension-switch", "activity.dim_date");

        // check that filter is kept switched to last quarter, but in switched date dimension
        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Activity: Last quarter"));
    }
}
