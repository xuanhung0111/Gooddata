package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ResetButtonTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Reset-Button-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_bar_visualization() {
        // Render bar chart
        analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_DEPARTMENT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-bar"), browser));

        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_table_visualization_properly() {
        // Render table
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-table"), browser));

        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_stay_clear_when_dragged_to_a_non_accepting_bucket() {
        // The category bucket DOES NOT accept a metric
        assertTrue(analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                analysisPageReact.getAttributesBucket().getInvitation())
                .isBlankState());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_selected_date_dimension() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPageReact.resetToBlankState()
            .addDate();
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-dataset-switch"), browser))
            .getFirstSelectedOption().getAttribute("value"), "activity.dataset.dt");
    }
}
