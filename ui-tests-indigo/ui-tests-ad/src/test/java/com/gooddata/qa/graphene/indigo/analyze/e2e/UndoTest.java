package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class UndoTest extends AbstractAdE2ETest {

    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Undo-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_create_one_version_per_user_action() {
        // 1st version
        MetricConfiguration configuration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        // 2nd version
        analysisPage.addDate();

        // 3rd version
        configuration.showPercents();

        // 4th version
        analysisPage.addAttribute(ATTR_DEPARTMENT);

        // 5th version -- switch category and turn off pop
        analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);

        IntStream.rangeClosed(0, 4).forEach(i -> analysisPage.undo());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_undo_metric_over_time_shortcut_followed_by_filter_change() {
        // D&D the first metric to the metric overtime recommendation
        initAnalysePage().drag(analysisPage.getCatalogPanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
                .waitForReportComputing()
                .getFilterBuckets()
                .configDateFilter("Last 12 months");

        assertEquals(parseFilterText(analysisPage.undo()
            .getFilterBuckets()
            .getDateFilterText()), Arrays.asList("Activity", "Last 4 quarters"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_redo_single_visualization_type_change() {
        assertTrue(initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.LINE_CHART)
            .undo()
            .isReportTypeSelected(ReportType.COLUMN_CHART));

        assertTrue(analysisPage.redo()
            .isReportTypeSelected(ReportType.LINE_CHART));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_undo_visualization_type_change_for_complex_configuration() {
        assertEquals(initAnalysePage().changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .changeReportType(ReportType.BAR_CHART)
            .undo()
            .getAttributesBucket()
            .getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_properly_deserialize_auto_generated_filters() {
        assertTrue(initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addAttribute(ATTR_ACTIVITY_TYPE)
            .resetToBlankState()
            .undo()
            .removeAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_properly_deserialize_modified_filters() {
        initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email");

        assertTrue(analysisPage.undo()
            .redo()
            .removeAttribute(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ATTR_ACTIVITY_TYPE));
    }
}
