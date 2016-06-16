package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class VisualizationTypesTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Visualization-Types-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_table_visualization() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-table"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_line_chart_visualization() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.LINE_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-line"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_bar_chart_visualization() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-bar"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_column_chart_visualization() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.COLUMN_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-column"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_changing_the_visualization_type() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-table"), browser));

        analysisPageReact.changeReportType(ReportType.LINE_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-line"), browser));

        analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-bar"), browser));

        analysisPageReact.changeReportType(ReportType.COLUMN_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-column"), browser));

        analysisPageReact.changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-report-visualization.s-visualization-table"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_if_one_attribute_is_dragged_in() {
        analysisPageReact.changeReportType(ReportType.COLUMN_CHART)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(
                ".adi-editor-canvas .adi-canvas-message.adi-canvas-message-missing-metric"), browser));

        analysisPageReact.changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(
                ".adi-editor-canvas .adi-canvas-message"), browser));

        analysisPageReact.changeReportType(ReportType.LINE_CHART)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(
                ".adi-editor-canvas .adi-canvas-message.adi-canvas-message-missing-metric"), browser));

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(
                ".adi-editor-canvas .adi-canvas-message"), browser));
    }
}
