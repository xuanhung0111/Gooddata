package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class FactBasedMetricsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Fact-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_fact_on_the_metrics_bucket() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");

        expectFind(METRICS_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_created_metric() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");

        drag(METRICS_BUCKET + " .adi-bucket-item", TRASH);

        expectFind(METRICS_BUCKET + EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_change_aggregation_function() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");

        expectAggregationSelected("SUM", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Sum of Amount");

        selectAggregation("AVG", "opportunitysnapshot_amount");
        expectAggregationSelected("AVG", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Avg Amount");
        expectBucketItemTitle("Avg Amount");

        selectAggregation("MAX", "opportunitysnapshot_amount");
        expectAggregationSelected("MAX", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Max Amount");
        expectBucketItemTitle("Max Amount");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_the_same_fact_multiple_times() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        dragFromCatalogue(amountFact, METRICS_BUCKET);
        dragFromCatalogue(amountFact, METRICS_BUCKET);

        expectElementCount(METRICS_BUCKET + " .adi-bucket-item", 3);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_allow_to_have_two_different_metrics_from_one_fact() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item:nth-of-type(2)");
        selectAggregation("MAX", "opportunitysnapshot_amount");
        selectAggregation("MIN", "opportunitysnapshot_amount", 1);

        expectChartLegend(asList("Max Amount", "Min Amount"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_aggregation_change() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        selectAggregation("MAX", "opportunitysnapshot_amount");

        undo();
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        expectAggregationSelected("SUM", "opportunitysnapshot_amount");

        undo();
        redo();
        expectFind(METRICS_BUCKET + NOT_EMPTY_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        expectAggregationSelected("SUM", "opportunitysnapshot_amount");
        expectFind(".s-show-pop");
        expectFind(".s-show-in-percent");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_fact_based_metric_via_single_metric_shortcut() {
        visitEditor();

        drag(amountFact, ".s-recommendation-metric-canvas");
        expectElementCount(METRICS_BUCKET + " .adi-bucket-item", 1);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_fact_based_metric_via_trending_shortcut() {
        visitEditor();

        drag(amountFact, ".s-recommendation-metric-over-time-canvas");
        expectElementCount(METRICS_BUCKET + " .adi-bucket-item", 1);
        expectElementCount(CATEGORIES_BUCKET + " .adi-bucket-item", 1);
    }

    private void expectBucketItemTitle(String title) {
        assertEquals(waitForElementVisible(cssSelector(".adi-bucket-item .s-bucket-item-header .s-title"),
                browser).getText(), title);
    }

    private void selectAggregation(String aggregation, String factMetricGenerated) {
        select(".adi-bucket-item[class*=fact_" + factMetricGenerated + "_generated] .s-fact-aggregation-switch",
                aggregation);
    }

    private void selectAggregation(String aggregation, String factMetricGenerated, int index) {
        select(".adi-bucket-item[class*=fact_" + factMetricGenerated + "_generated] .s-fact-aggregation-switch",
                aggregation, index);
    }

    private void expectAggregationAxisLabel(String label) {
        assertEquals(waitForElementVisible(cssSelector(".highcharts-axis tspan"), browser).getText(), label);
    }

    private void expectAggregationSelected(String aggregation, String factMetricGenerated) {
        String locator = ".adi-bucket-item[class*=fact_" + factMetricGenerated + "_generated] .s-fact-aggregation-switch";

        expectFind(locator);

        assertEquals(new Select(waitForElementVisible(cssSelector(locator), browser))
            .getFirstSelectedOption().getAttribute("value"), aggregation);
    }
}
