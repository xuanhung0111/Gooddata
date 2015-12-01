package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;

import java.util.stream.IntStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class UndoTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Undo-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_one_version_per_user_action() {
        visitEditor();

        // 1st version
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        // 2nd version
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        // 3rd version
        click(METRICS_BUCKET + " .s-show-pop");
        expectFind(METRICS_BUCKET + " .s-show-pop:checked");

        // 4th version -- switch category and turn off pop
        drag(activityTypeAttr, CATEGORIES_BUCKET + " " + yearActivityLabel);

        expectMissing(METRICS_BUCKET + " .s-show-pop:checked");

        IntStream.rangeClosed(0, 3).forEach(i -> undo());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_metric_over_time_shortcut_followed_by_filter_change() {
        visitEditor();

        // D&D the first metric to the metric overtime recommendation
        drag(activitiesMetric, ".s-recommendation-metric-over-time-canvas");

        click(".adi-filter-button");
        click(".s-filter-last_12_months");

        undo();

        assertThat(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText(),
                containsString("Last 4 quarters"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_redo_single_visualization_type_change() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        switchVisualization("line");

        undo();
        expectFind(".vis-type-column.is-selected");

        redo();
        expectFind(".vis-type-line.is-selected");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_visualization_type_change_for_complex_configuration() {
        visitEditor();

        switchVisualization("table");
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);

        switchVisualization("bar");

        undo();

        expectFind(CATEGORIES_BUCKET + " " + activityTypeAttr);
        expectFind(CATEGORIES_BUCKET + " " + departmentAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_auto_generated_filters() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        resetReport();
        undo();

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, TRASH);
        expectMissing(FILTERS_BUCKET + " " + activityTypeAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_modified_filters() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        // modify filter
        click(".s-filter-button");
        click(".s-filter-item[title=Email]");
        click(".s-filter-picker .s-btn-apply");

        undo();
        redo();

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, TRASH);
        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);
    }
}
