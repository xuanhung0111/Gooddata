package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class DragRecommendationsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Drag-Recommendations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_column_chart_after_a_metric_is_dragged_to_main_recommendation() {
        visitEditor();

        // D&D the first metric to the initial metric recommendation
        drag(activitiesMetric, ".s-recommendation-metric-canvas");

        // should get a single column chart (in switchable visualization "column/bar" ~ "bar")
        expectFind(".adi-components .visualization-column .s-property-y.s-id-metricvalues");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_date_sliced_metric_column_chart_after_a_metric_is_dragged_to_the_overtime_recommendation() {
        visitEditor();

        // D&D the first metric to the metric overtime recommendation
        drag(activitiesMetric, ".s-recommendation-metric-over-time-canvas");

        // Check bucket items
        expectFind(METRICS_BUCKET + " " + activitiesMetric);
        expectFind(CATEGORIES_BUCKET + " " + quarterYearActivityLabel);

        // should get a column sliced by the Quarter attribute on the X axis
        expectFind(".adi-components .visualization-column .s-property-x" + quarterYearActivityLabel);
        expectFind(".adi-components .visualization-column .s-property-y.s-id-metricvalues");
        expectFind(".adi-components .visualization-column .s-property-where" + quarterYearActivityLabel);
        expectFind(".adi-components .visualization-column .s-property-where.s-where-___between____3_0__");

        resetReport();

        // check that filter to the last four quarters is now disabled again
        expectMissing(".adi-components .visualization-column .s-property-where");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_attribute_elements_table_after_an_attribute_is_dragged_to_main_recommendation() {
        visitEditor();

        // D&D the first metric to the metric overtime recommendation
        drag(DATE, ".s-recommendation-attribute-canvas");

        // Check bucket items
        expectFind(CATEGORIES_BUCKET + " " + yearActivityLabel);

        // should get a table sliced by the Quarter attribute on the X axis
        expectFind(".adi-components .dda-table-component " + yearActivityLabel);
    }
}
