package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class TrashTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Trash-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_clear_all_items_by_dragging_them_to_the_trash() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        drag(METRICS_BUCKET + " " + activitiesMetric, TRASH);
        expectMissing(METRICS_BUCKET + " " + activitiesMetric);

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, TRASH);
        expectMissing(CATEGORIES_BUCKET + " " + activityTypeAttr);
        expectFind(".s-reset-report.disabled");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_trash_item_by_throwing_it_anyplace_other_than_trash() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectMissing(".s-reset-report.disabled");
        drag(METRICS_BUCKET + " " + activitiesMetric, ".s-reset-report");
        expectFind(METRICS_BUCKET + " " + activitiesMetric);
        expectMissing(".s-reset-report.disabled");
    }
}
