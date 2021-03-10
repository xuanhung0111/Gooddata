package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.text.ParseException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesUndoRedoSavedInsightTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TEST = "Insight-Test";
    private static final String INSIGHT_TEST_WITH_METRIC_ONLY = "Insight-With-Metric-Only";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Undo-And-Save-Insight-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = { "createProject" })
    public void prepareSavedInsightsForUndoRedoTest() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .saveInsight(INSIGHT_TEST);
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_SNAPSHOT_BOP)
                .waitForReportComputing()
                .saveInsight(INSIGHT_TEST_WITH_METRIC_ONLY);
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void addMetric() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Adding-Metric";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .addMetric(METRIC_SNAPSHOT_BOP)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName)
                .waitForReportComputing().getChartReport().getTrackersCount(), 8, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void addAttribute() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Adding-Attribute";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST_WITH_METRIC_ONLY)
                .saveInsightAs(insightName)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName).changeReportType(ReportType.COLUMN_CHART)
                .waitForReportComputing().getChartReport().getTrackersCount(), 2, "Chart content is not as expected");
    }
    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void addFilterToMetric() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Adding-Filtered-Metric";
        initAnalysePage().openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting", "Web Meeting");
        analysisPage.waitForReportComputing().saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(
                analysisPage.openInsight(insightName)
                        .waitForReportComputing()
                        .getChartReport()
                        .getDataLabels(),
                asList("35,975", "33,596"));
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void removeAttribute() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Removing-Attribute";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .removeAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName)
                .waitForReportComputing().getChartReport().getTrackersCount(), 1, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void replaceAttribute() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Replacing-Attribute";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .addAttribute(ATTR_DEPARTMENT)
                .replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_REGION)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName).waitForReportComputing().getChartReport()
            .getTrackersCount(), 4, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void changeTimeFilter() throws JSONException, IOException, ParseException {
        final String insightName = "Test-Saved-Insight-After-Changing-Time-Filter";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .addDateFilter()
                .getFilterBuckets()
                .configDateFilter("01/01/2015", "12/31/2015");
        analysisPage
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName)
                .waitForReportComputing().getChartReport().getTrackersCount(), 3, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void changeAttributeFilter() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Changing-Attribute-Filter";
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        analysisPage
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(analysisPage.openInsight(insightName)
                .waitForReportComputing().getChartReport().getTrackersCount(), 2, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" })
    public void changeChartType() throws JSONException, IOException {
        final String insightName = "Test-Saved-Insight-After-Changing-Chart-Type";
        initAnalysePage().openInsight(INSIGHT_TEST)
                .saveInsightAs(insightName)
                .changeReportType(ReportType.BAR_CHART)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight();
        assertEquals(
                analysisPage.openInsight(insightName)
                        .waitForReportComputing()
                        .getChartReport()
                        .getChartType(),
                ReportType.BAR_CHART.getLabel(), "Chart type is not as expected");
    }

    @Test(dependsOnMethods = { "prepareSavedInsightsForUndoRedoTest" },
            description = "This test is to cover the bug: https://jira.intgdc.com/browse/ONE-4857")
    public void disabledUndoRedoButtonAfterPressingClearButton() throws JSONException, IOException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).openInsight(INSIGHT_TEST)
            .resetToBlankState()
            .addMetric(METRIC_SNAPSHOT_BOP)
            .waitForReportComputing()
            .undo();

        assertFalse(analysisPage.isUndoInsightEnabled(), "Undo button should be disabled");
        analysisPage.redo().resetToBlankState();
        assertFalse(analysisPage.isUndoInsightEnabled(), "Undo button should be disabled");
        assertTrue(analysisPage.isRedoInsightDisabled(), "Redo button should be disabled");
    }

    private void checkUndoRedoAfterSaveInsight() throws JSONException, IOException {
        final AnalysisPageHeader header = analysisPage.getPageHeader();
        final String savedTitle = header.getInsightTitle();
        final IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        final int numberOfInsights = indigoRestRequest.getAllInsightNames().size();
        
        analysisPage.undo().waitForReportComputing();
        assertEquals(header.getInsightTitle(), savedTitle,
                "The expected title is NOT displayed after undo");
        assertTrue(header.isUnsavedMessagePresent(), "Unsaved notification is not displayed after undo");
        assertTrue(header.isSaveButtonEnabled(), "Save button is not enabled after undo");
        assertEquals(indigoRestRequest.getAllInsightNames().size(),
                numberOfInsights, "The insight does not exist after undo");

        analysisPage.redo().waitForReportComputing();
        assertEquals(header.getInsightTitle(), savedTitle,
                "The expected title is NOT displayed after redo");
        assertFalse(header.isUnsavedMessagePresent(), "Unsave notification is displayed after redo");
        assertFalse(header.isSaveButtonEnabled(), "Save button is enabled after undo");
    }
}
