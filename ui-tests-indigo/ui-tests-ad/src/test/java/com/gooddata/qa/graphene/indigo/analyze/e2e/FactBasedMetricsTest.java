package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class FactBasedMetricsTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Fact-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_drop_fact_on_the_metrics_bucket() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_remove_created_metric() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();

        assertTrue(analysisPage.removeMetric("Sum of " + FACT_AMOUNT)
            .getMetricsBucket()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_change_aggregation_function() {
        MetricConfiguration configuration = analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();

        expectAggregationSelected("sum", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Sum of " + FACT_AMOUNT);

        configuration.changeAggregation("Average");
        expectAggregationSelected("avg", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Avg " + FACT_AMOUNT);
        expectBucketItemTitle("Avg " + FACT_AMOUNT);

        configuration.changeAggregation("Maximum");
        expectAggregationSelected("max", "opportunitysnapshot_amount");
        expectAggregationAxisLabel("Max " + FACT_AMOUNT);
        expectBucketItemTitle("Max " + FACT_AMOUNT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_drop_the_same_fact_multiple_times() {
        assertEquals(analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .addMetric(FACT_AMOUNT, FieldType.FACT)
            .addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getItemNames()
            .size(), 3);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_allow_to_have_two_different_metrics_from_one_fact() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration()
            .changeAggregation("Maximum");

        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration()
            .changeAggregation("Minimum");

        assertEquals(analysisPage.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Max " + FACT_AMOUNT, "Min " + FACT_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_undo_aggregation_change() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration()
            .changeAggregation("Maximum");

        analysisPage.undo()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();
        expectAggregationSelected("sum", "opportunitysnapshot_amount");

        assertFalse(analysisPage.undo()
            .redo()
            .getMetricsBucket()
            .isEmpty());

        analysisPage.getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();
        expectAggregationSelected("sum", "opportunitysnapshot_amount");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_create_fact_based_metric_via_single_metric_shortcut() {
        assertEquals(analysisPage.drag(analysisPage.getCataloguePanel().searchAndGet(FACT_AMOUNT, FieldType.FACT),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-canvas"), browser))
            .waitForReportComputing()
            .getMetricsBucket()
            .getItemNames()
            .size(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_create_fact_based_metric_via_trending_shortcut() {
        assertEquals(analysisPage.drag(analysisPage.getCataloguePanel().searchAndGet(FACT_AMOUNT, FieldType.FACT),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
            .waitForReportComputing()
            .getMetricsBucket()
            .getItemNames()
            .size(), 1);
        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);
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
