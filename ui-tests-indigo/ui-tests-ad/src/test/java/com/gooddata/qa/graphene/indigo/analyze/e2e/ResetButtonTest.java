package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ResetButtonTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Reset-Button-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_clear_bar_visualization() {
        // Render bar chart
        analysisPage.changeReportType(ReportType.BAR_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_DEPARTMENT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-bar"), browser));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_clear_table_visualization_properly() {
        // Render table
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-table"), browser));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_stay_clear_when_dragged_to_a_non_accepting_bucket() {
        // The category bucket DOES NOT accept a metric
        assertTrue(analysisPage.drag(analysisPage.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                analysisPage.getAttributesBucket().getInvitation())
                .isBlankState());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_reset_selected_date_dimension() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPage.resetToBlankState()
            .addDate();

        takeScreenshot(browser, "Date-dimension-reset", getClass());
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), "Activity");
    }
}
