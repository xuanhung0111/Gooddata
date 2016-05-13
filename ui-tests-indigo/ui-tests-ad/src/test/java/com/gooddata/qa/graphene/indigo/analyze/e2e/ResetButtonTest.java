package com.gooddata.qa.graphene.indigo.analyze.e2e;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResetButtonTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Reset-Button-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_bar_visualization() {
        // Render bar chart
        analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .addStack(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-bar"), browser));

        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_clear_table_visualization_properly() {
        // Render table
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-table"), browser));

        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_stay_clear_when_dragged_to_a_non_accepting_bucket() {
        // The category bucket DOES NOT accept a metric
        assertTrue(analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                analysisPageReact.getAttributesBucket().getInvitation())
                .isBlankState());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_selected_date_dimension() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPageReact.resetToBlankState()
            .addDate();
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-dimension-switch"), browser))
            .getFirstSelectedOption().getAttribute("value"), "activity.dim_date");
    }
}
