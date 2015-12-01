package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static java.util.Arrays.asList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class AttributeBasedMetricsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Attribute-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_attribute_to_the_metrics_bucket() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);

        expectFind(METRICS_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_created_metric() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        drag(METRICS_BUCKET + " .adi-bucket-item", TRASH);

        expectFind(METRICS_BUCKET + EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_same_attribute_multiple_time_to_metrics() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);

        expectElementCount(METRICS_BUCKET + " .adi-bucket-item", 3);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_and_visualize_attribute_based_metrics_with_correct_titles() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);

        expectChartLegend(asList("Count of Activity Type", "Count of Activity Type"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_combine_attribute_and_fact_based_metrics() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);
        dragFromCatalogue(amountFact, METRICS_BUCKET);

        expectChartLegend(asList("Count of Activity Type", "Sum of Amount"));
    }
}
