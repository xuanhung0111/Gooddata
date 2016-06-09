package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.IntStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class UndoTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Undo-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_one_version_per_user_action() {
        // 1st version
        MetricConfiguration configuration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        // 2nd version
        analysisPageReact.addDate();

        // 3rd version
        configuration.showPop();

        // 4th version -- switch category and turn off pop
        analysisPageReact.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);

        assertFalse(configuration.isPopSelected());

        IntStream.rangeClosed(0, 3).forEach(i -> analysisPageReact.undo());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_metric_over_time_shortcut_followed_by_filter_change() {
        // D&D the first metric to the metric overtime recommendation
        analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
                .waitForReportComputing()
                .getFilterBuckets()
                .configDateFilter("Last 12 months");

        assertEquals(analysisPageReact.undo()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: Last 4 quarters");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_redo_single_visualization_type_change() {
        assertTrue(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.LINE_CHART)
            .undo()
            .isReportTypeSelected(ReportType.COLUMN_CHART));

        assertTrue(analysisPageReact.redo()
            .isReportTypeSelected(ReportType.LINE_CHART));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_visualization_type_change_for_complex_configuration() {
        assertEquals(analysisPageReact.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .changeReportType(ReportType.BAR_CHART)
            .undo()
            .getAttributesBucket()
            .getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_auto_generated_filters() {
        assertTrue(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .resetToBlankState()
            .undo()
            .removeAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_modified_filters() {
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email");

        assertTrue(analysisPageReact.undo()
            .redo()
            .removeAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ATTR_ACTIVITY_TYPE));
    }
}
