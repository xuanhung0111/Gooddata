package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class NoncommonDateTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Noncommon-Date-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void date_dimension_in_chart_should_reflect_currently_selected_dimension() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPageReact.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Created"));

        analysisPageReact.getAttributesBucket()
            .changeDateDimension("Activity");
        analysisPageReact.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Activity"));
    }

    @Test(dependsOnGroups = {"init"})
    public void enable_date_dimension_selects_correctly() {
        WebElement dateFilter = analysisPageReact.addDateFilter()
            .getFilterBuckets()
            .getDateFilter();

        dateFilter.click();
        assertTrue(isElementPresent(cssSelector(".s-filter-date-date-dataset-switch:not(.disabled)"), browser));
        dateFilter.click();

        analysisPageReact.addDate();
        assertTrue(isElementPresent(cssSelector(".s-date-dataset-switch:not(.disabled)"), browser));

        // date dimension picker in select is now disabled
        dateFilter.click();
        assertTrue(isElementPresent(cssSelector(".s-filter-date-date-dataset-switch[disabled]"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void keeps_date_dimensions_in_categories_in_sync_with_the_filter() {
        analysisPageReact.addDateFilter()
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Created: All time"));

        // update filter to last quarter
        analysisPageReact.getFilterBuckets()
            .configDateFilter("Last quarter");
        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Created: Last quarter"));

        // switch date dimension to Foundation Date
        analysisPageReact.getAttributesBucket()
            .changeDateDimension("Activity");

        // check that filter is kept switched to last quarter, but in switched date dimension
        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Activity: Last quarter"));
    }
}
