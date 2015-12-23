package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class VisualizationTypesTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Visualization-Types-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_table_visualization() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("table");
        expectFind(".adi-components .dda-table-component");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_line_chart_visualization() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("line");
        expectFind(".adi-components .dda-line-component");

        expectFind(".adi-components .visualization-line .s-property-y.s-id-metricvalues");
        expectFind(".adi-components .visualization-line .s-property-x" + activityTypeAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_bar_chart_visualization() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("bar");
        expectFind(".adi-components .dda-bar-component");

        expectFind(".adi-components .visualization-bar .s-property-y.s-id-metricvalues");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_column_chart_visualization() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("column");
        expectFind(".adi-components .dda-column-component");

        expectFind(".adi-components .visualization-column .s-property-y.s-id-metricvalues");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_changing_the_visualization_type() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("table");
        expectFind(".adi-components .dda-table-component");

        switchVisualization("line");
        expectFind(".adi-components .dda-line-component");
        expectFind(".adi-components .visualization-line .s-property-y.s-id-metricvalues");
        expectFind(".adi-components .visualization-line .s-property-x" + activityTypeAttrLabel);

        switchVisualization("bar");
        expectFind(".adi-components .dda-bar-component");
        expectFind(".adi-components .visualization-bar .s-property-y.s-id-metricvalues");

        switchVisualization("column");
        expectFind(".adi-components .dda-column-component");
        expectFind(".adi-components .visualization-column .s-property-y.s-id-metricvalues");

        switchVisualization("table");
        expectFind(".adi-components .dda-table-component");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_sort_bar_chart() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("bar");

        expectFind(".s-property-orderBy");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_sorted_in_line_chart() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("line");
        expectMissing(".s-property-orderBy");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_sorted_in_column_chart() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("column");
        expectMissing(".s-property-orderBy");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_sorted_in_table() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        switchVisualization("table");
        expectMissing(".s-property-orderBy");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_update_visualization_upon_config_change() {
        visitEditor();

        switchVisualization("bar");

        // add a metric to configuration
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        // check whether a rendered highcharts component is indeed present
        // with a single metric configuration
        expectFind(".adi-components .dda-bar-component .highcharts-container");
        expectFind(".adi-components .visualization-bar .s-property-y.s-id-metricvalues");

        // add an attribute
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectFind(".adi-components .visualization-bar .s-property-x" + activityTypeAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_if_one_attribute_is_dragged_in() {
        visitEditor();

        switchVisualization("column");
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectFind(".adi-editor-canvas .adi-canvas-message.adi-canvas-message-missing-metric");

        switchVisualization("table");
        expectMissing(".adi-editor-canvas .adi-canvas-message");

        switchVisualization("line");
        expectFind(".adi-editor-canvas .adi-canvas-message.adi-canvas-message-missing-metric");

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        expectMissing(".adi-editor-canvas .adi-canvas-message");
    }
}
