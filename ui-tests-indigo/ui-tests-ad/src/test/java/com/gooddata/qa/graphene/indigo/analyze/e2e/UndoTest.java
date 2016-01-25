package com.gooddata.qa.graphene.indigo.analyze.e2e;

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
        initAnalysePageByUrl();

        // 1st version
        MetricConfiguration configuration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        // 2nd version
        analysisPage.addDate();

        // 3rd version
        configuration.showPop();

        // 4th version -- switch category and turn off pop
        analysisPage.replaceAttribute(DATE, ACTIVITY_TYPE);

        assertFalse(configuration.isPopSelected());

        IntStream.rangeClosed(0, 3).forEach(i -> analysisPage.undo());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_metric_over_time_shortcut_followed_by_filter_change() {
        initAnalysePageByUrl();

        // D&D the first metric to the metric overtime recommendation
        analysisPage.drag(analysisPage.getCataloguePanel().searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
                .waitForReportComputing()
                .getFilterBuckets()
                .configDateFilter("Last 12 months");

        assertEquals(analysisPage.undo()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: Last 4 quarters");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_redo_single_visualization_type_change() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.LINE_CHART)
            .undo()
            .isReportTypeSelected(ReportType.COLUMN_CHART));

        assertTrue(analysisPage.redo()
            .isReportTypeSelected(ReportType.LINE_CHART));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_undo_visualization_type_change_for_complex_configuration() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .changeReportType(ReportType.BAR_CHART)
            .undo()
            .getAttributesBucket()
            .getItemNames(), asList(ACTIVITY_TYPE, DEPARTMENT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_auto_generated_filters() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addAttribute(ACTIVITY_TYPE)
            .resetToBlankState()
            .undo()
            .removeAttribute(ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_properly_deserialize_modified_filters() {
        initAnalysePageByUrl();

        analysisPage.addAttribute(ACTIVITY_TYPE)
            .getFilterBuckets()
            .configAttributeFilter(ACTIVITY_TYPE, "Email");

        assertTrue(analysisPage.undo()
            .redo()
            .removeAttribute(ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ACTIVITY_TYPE));
    }
}
