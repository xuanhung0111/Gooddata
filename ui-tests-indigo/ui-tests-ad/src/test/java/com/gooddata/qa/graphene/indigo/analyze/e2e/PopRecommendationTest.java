package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class PopRecommendationTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Error-States-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply__period_over_period__recommendation() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectMissing(".s-recommendation-comparison-with-period");
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        expectMissing(".adi-components .visualization-column .highcharts-series [fill=\"rgb(172,220,254)\"]");

        click(".s-recommendation-comparison-with-period .s-apply-recommendation");

        expectFind(".adi-components .visualization-column .s-property-color.s-id-metricnames");
        expectFind(".adi-components .visualization-column .s-property-y.s-id-metricvalues");

        expectFind(".adi-components .visualization-column .s-property-where" + quarterYearActivityLabel);
        expectFind(".adi-components .visualization-column .s-property-where.s-where-___between___0_0__");

        assertThat(waitForElementVisible(cssSelector(
                ".adi-components .visualization-column .highcharts-legend-item tspan"), browser).getText(),
                containsString("# of Activities - previous year"));
        expectFind(".adi-components .visualization-column .highcharts-series [fill=\"rgb(00,131,255)\"]");

        expectMissing(".s-recommendation-comparison-with-period");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_honor_period_change_for__period_over_period() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectMissing(".s-recommendation-comparison-with-period");
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        select(".s-recommendation-comparison-with-period .s-attribute-switch", "GDC.time.month");
        click(".s-recommendation-comparison-with-period .s-apply-recommendation");

        expectFind(".adi-components .visualization-column .s-property-where" + monthYearActivityLabel);
        expectFind(".adi-components .visualization-column .s-property-where.s-where-___between___0_0__");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_widget_after_apply() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectMissing(".s-recommendation-metric-with-period");
        click(".s-recommendation-trending .s-apply-recommendation");

        expectMissing(".adi-components .visualization-column .highcharts-series [fill=\"rgb(172,220,254)\"]");

        click(".s-recommendation-metric-with-period .s-apply-recommendation");

        assertThat(waitForElementVisible(cssSelector(
                ".adi-components .visualization-column .highcharts-legend-item tspan"), browser).getText(),
                containsString("# of Activities - previous year"));
        expectFind(".adi-components .visualization-column .highcharts-series [fill=\"rgb(00,131,255)\"]");

        expectMissing(".s-recommendation-metric-with-period");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_pop_checkbox_if_date_and_attribute_are_moved_to_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        expectMissing(".s-show-pop:disabled");

        drag(activityTypeAttr, CATEGORIES_BUCKET + " " + yearActivityLabel);

        expectFind(".s-show-pop:disabled");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_the_recommendation_if_something_in_stack_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);

        expectMissing(".s-recommendation-metric-with-period");
        expectMissing(".s-recommendation-comparison-with-period");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_the_recommendation_if_date_in_categories_and_something_in_stack_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        expectMissing(".s-recommendation-metric-with-period");
        expectMissing(".s-recommendation-comparison-with-period");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_recommendations_if_categories_empty_and_something_in_stack_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        expectMissing(".s-recommendation-metric-with-period");
        expectMissing(".s-recommendation-comparison-with-period");
        expectFind(".s-recommendation-trending");
        expectFind(".s-recommendation-comparison");
    }
}
