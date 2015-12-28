package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class TrendingRecommendationTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Trending-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_add_date_item_with_proper_granularity_to_category_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        expectFind(".s-recommendation-trending");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_month_in_trending_widget_and_hide_it() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        select(".s-date-granularity-switch", "GDC.time.month");
        click(".s-recommendation-trending .s-apply-recommendation");
        expectMissing(".s-recommendation-trending");
        expectFind(CATEGORIES_BUCKET + " " + monthYearActivityLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void ashould_have_quarter_selected_after_resetting_a_widget() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        select(".s-date-granularity-switch", "GDC.time.month");
        resetReport();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-granularity-switch"), browser))
            .getFirstSelectedOption().getAttribute("value"), "GDC.time.quarter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_select_last_4_quarters_on_date_filter_when_trending() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        click(".s-recommendation-trending .s-apply-recommendation");

        expectFind(FILTERS_BUCKET + " .s-date-filter" + quarterYearActivityLabel);
        expectFind(FILTERS_BUCKET + " .s-date-filter.s-where-___between____3_0__");
    }
}
