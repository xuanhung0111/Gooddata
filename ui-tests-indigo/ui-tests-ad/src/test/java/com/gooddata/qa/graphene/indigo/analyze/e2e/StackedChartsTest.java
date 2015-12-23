package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static java.util.Arrays.asList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class StackedChartsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Stacked-Charts-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_put_stack_by_attribute_into_color_series() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectChartLegend(asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_totals_for_stacked_columns() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectElementCount(".highcharts-stack-labels text", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_stack_warn_msg_when_there_is_something_in_stack_by_bucket() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectFind(METRICS_BUCKET + " .s-stack-warn");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_stack_warn_msg_if_there_is_more_than_1_metrics() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        dragFromCatalogue(accountAttr, CATEGORIES_BUCKET);

        expectFind(STACKS_BUCKET + " .s-stack-warn");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disappear_when_visualization_is_switched_to_table_and_should_be_empty_when_going_back() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        switchVisualization("table");
        expectMissing(STACKS_BUCKET);

        switchVisualization("bar");
        expectFind(METRICS_BUCKET + NOT_EMPTY_BUCKET);
        expectFind(STACKS_BUCKET + NOT_EMPTY_BUCKET);
        expectFind(CATEGORIES_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disappear_when_switched_to_table_via_result_too_large_link() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(accountAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        click(".s-error-too-many-data-points .s-switch-to-table");

        switchVisualization("bar");
        expectFind(METRICS_BUCKET + NOT_EMPTY_BUCKET);
        expectFind(STACKS_BUCKET + NOT_EMPTY_BUCKET);
        expectFind(CATEGORIES_BUCKET + NOT_EMPTY_BUCKET);
    }
}
