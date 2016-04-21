package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class FactBasedMetricsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Fact-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_fact_on_the_metrics_bucket() {
        analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_created_metric() {
        analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();

        assertTrue(analysisPageReact.removeMetric("Sum of " + AMOUNT)
            .getMetricsBucket()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_change_aggregation_function() {
        MetricConfiguration configuration = analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();

        expectAggregationSelected("SUM", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Sum of " + AMOUNT);

        configuration.changeAggregation("Average");
        expectAggregationSelected("AVG", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Avg " + AMOUNT);
        expectBucketItemTitle("Avg " + AMOUNT);

        configuration.changeAggregation("Maximum");
        expectAggregationSelected("MAX", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Max " + AMOUNT);
        expectBucketItemTitle("Max " + AMOUNT);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_the_same_fact_multiple_times() {
        assertEquals(analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .addMetric(AMOUNT, FieldType.FACT)
            .addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getItemNames()
            .size(), 3);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_allow_to_have_two_different_metrics_from_one_fact() {
        analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration()
            .changeAggregation("Maximum");

        analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration()
            .changeAggregation("Minimum");

        assertEquals(analysisPageReact.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Max " + AMOUNT, "Min " + AMOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_aggregation_change() {
        analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration()
            .changeAggregation("Maximum");

        analysisPageReact.undo()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();
        expectAggregationSelected("SUM", "opportunitysnapshot_amount");

        assertFalse(analysisPageReact.undo()
            .redo()
            .getMetricsBucket()
            .isEmpty());

        analysisPageReact.getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();
        expectAggregationSelected("SUM", "opportunitysnapshot_amount");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_fact_based_metric_via_single_metric_shortcut() {
        assertEquals(analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(AMOUNT, FieldType.FACT),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-canvas"), browser))
            .waitForReportComputing()
            .getMetricsBucket()
            .getItemNames()
            .size(), 1);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_fact_based_metric_via_trending_shortcut() {
        assertEquals(analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(AMOUNT, FieldType.FACT),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
            .waitForReportComputing()
            .getMetricsBucket()
            .getItemNames()
            .size(), 1);
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 1);
    }

    private void expectBucketItemTitle(String title) {
        assertEquals(waitForElementVisible(cssSelector(".adi-bucket-item .s-bucket-item-header .s-title"),
                browser).getText(), title);
    }

    private void expectAggregationAxisLabel(String label) {
        assertEquals(waitForElementVisible(cssSelector(".highcharts-axis tspan"), browser).getText(), label);
    }

    private void expectAggregationSelected(String aggregation, String factMetricGenerated) {
        String locator = ".adi-bucket-item[class*=fact_" + factMetricGenerated + "_generated] .s-fact-aggregation-switch";

        assertTrue(isElementPresent(cssSelector(locator), browser));

        assertEquals(new Select(waitForElementVisible(cssSelector(locator), browser))
            .getFirstSelectedOption().getAttribute("value"), aggregation);
    }
}
