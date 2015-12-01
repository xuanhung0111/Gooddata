package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class MetricBucketTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Bucket-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_metric_details() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        hover(".inlineBubbleHelp", METRICS_BUCKET);
        expectFind(".s-catalogue-bubble-loaded");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_fact_details() {
        visitEditor();

        dragFromCatalogue(amountFact, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        hover(".inlineBubbleHelp", METRICS_BUCKET);
        expectFind(".s-catalogue-bubble-loaded");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_attribute_details() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        hover(".inlineBubbleHelp", METRICS_BUCKET);
        expectFind(".s-catalogue-bubble-loaded");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_open_and_close_configuration() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        expectMissing(METRICS_BUCKET + " input[type=checkbox]");

        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        expectFind(METRICS_BUCKET + " input[type=checkbox]");

        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        expectMissing(METRICS_BUCKET + " input[type=checkbox]");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_able_to_drop_second_metric_into_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);

        expectFind(".adi-components .visualization-column .s-property-y");
        expectFind(".adi-components .visualization-column .s-property-color");

        expectChartLegend(asList("# of Activities", "# of Lost Opps."));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_show_in_percent_correctly() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        expectFind(".is-disabled .s-show-in-percent");

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectMissing(".is-disabled .s-show-in-percent");

        dragFromCatalogue(quotaMetric, METRICS_BUCKET);
        expectFind(".is-disabled .s-show-in-percent");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_show_PoP_correctly() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        expectFind(".is-disabled .s-show-pop");

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        expectMissing(".is-disabled .s-show-pop");

        dragFromCatalogue(quotaMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + quotaMetric);
        expectFind(".is-disabled .s-show-pop");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_PoP_after_second_metric_is_added() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        click(METRICS_BUCKET + " .s-show-pop");
        expectChartLegend(asList("# of Activities - previous year", "# of Activities"));

        dragFromCatalogue(quotaMetric, METRICS_BUCKET);
        expectChartLegend(asList("# of Activities", "Quota"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_percent_if_2_metric_is_added() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        click(METRICS_BUCKET + " .s-show-in-percent");

        dragFromCatalogue(quotaMetric, METRICS_BUCKET);
        expectChartLegend(asList("# of Activities", "Quota"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_second_metric_if_user_wants() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);

        expectChartLegend(asList("# of Activities", "# of Lost Opps."));

        drag(METRICS_BUCKET + " " + activitiesMetric, TRASH);
        expectChartLegend(emptyList());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_allow_to_add_second_instance_of_metric_already_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectElementCount(METRICS_BUCKET + " " + activitiesMetric, 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_legend_for_only_one_metric() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        expectChartLegend(emptyList());
    }
}
