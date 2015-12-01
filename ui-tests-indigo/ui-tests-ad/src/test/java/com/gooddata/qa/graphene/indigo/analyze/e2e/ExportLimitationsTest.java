package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ExportLimitationsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Export-Limitations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_export_button_if_stacking_and_slicing_by_the_same_attribute() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        // Drag exactly the same attribute to the category and stack
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_export_if_the_same_metric_is_in_metric_bucket_twice() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectExportDisabled();
    }
}
