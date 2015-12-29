package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ResetButtonTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Reset-Button-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_bar_visualization() {
        visitEditor();

        // Render bar chart
        switchVisualization("bar");
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        expectFind(".adi-components .dda-bar-component");

        resetReport();

        expectClean();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_table_visualization_properly() {
        visitEditor();

        // Render table
        switchVisualization("table");
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectFind(".adi-components .dda-table-component");

        resetReport();

        expectClean();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_stay_clear_when_dragged_to_a_non_accepting_bucket() {
        visitEditor();

        // The category bucket DOES NOT accept a metric
        dragFromCatalogue(activitiesMetric, CATEGORIES_BUCKET);

        expectClean();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_selected_date_dimension() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        select(".s-date-dimension-switch", "created.dim_date");

        resetReport();

        expectClean();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-dimension-switch"), browser))
            .getFirstSelectedOption().getAttribute("value"), "activity.dim_date");
    }
}
