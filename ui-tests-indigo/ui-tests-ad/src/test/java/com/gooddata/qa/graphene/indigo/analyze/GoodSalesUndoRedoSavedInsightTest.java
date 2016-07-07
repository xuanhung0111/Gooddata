package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static java.util.Collections.singleton;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesUndoRedoSavedInsightTest extends GoodSalesAbstractAnalyseTest {

    private static final String INSIGHT_TEST = "Insight-Test";
    private static final String UNTITLED_INSIGHT = "Untitled insight";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Undo-And-Save-Insight-Test";
    }

    @Test(dependsOnGroups = { "init" })
    public void addMetricAndAttribute() throws JSONException, IOException {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .saveInsight(INSIGHT_TEST);
        checkUndoRedoAfterSaveInsight(UNTITLED_INSIGHT);
        assertEquals(analysisPageReact.openInsight(INSIGHT_TEST)
                .waitForReportComputing().getChartReport().getTrackersCount(), 4, "Chart content is not as expected");
    }

    @Test(dependsOnGroups = { "init" })
    public void addFilterToMetric() throws JSONException, IOException {
        final String insight = "Test-Saved-Insight-After-Adding-Filtered-Metric";
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting", "Web Meeting");
        analysisPageReact.waitForReportComputing().saveInsight(insight);
        checkUndoRedoAfterSaveInsight(UNTITLED_INSIGHT);
        assertEquals(
                analysisPageReact.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getDataLabels(),
                singleton("69,571"));
    }

    @Test(dependsOnMethods = { "addMetricAndAttribute" })
    public void removeAttribute() throws JSONException, IOException {
        final String insight = "Test-Saved-Insight-After-Removing-Attribute";
        analysisPageReact.openInsight(INSIGHT_TEST)
                .saveInsightAs(insight)
                .removeAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight(insight);
        assertEquals(analysisPageReact.openInsight(insight)
                .waitForReportComputing().getChartReport().getTrackersCount(), 1, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "addMetricAndAttribute" })
    public void replaceAttribute() throws JSONException, IOException {
        final String insight = "Test-Saved-Insight-After-Replacing-Attribute";
        analysisPageReact.openInsight(INSIGHT_TEST)
                .saveInsightAs(insight)
                .replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_REGION)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight(insight);
        assertEquals(analysisPageReact.openInsight(insight)
                .waitForReportComputing().getChartReport().getTrackersCount(), 2, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "addMetricAndAttribute" })
    public void changeTimeFilter() throws JSONException, IOException, ParseException {
        final String insight = "Test-Saved-Insight-After-Changing-Time-Filter";
        analysisPageReact.openInsight(INSIGHT_TEST)
                .saveInsightAs(insight)
                .addDateFilter()
                .getFilterBuckets()
                .configDateFilter("01/01/2015", "12/31/2015");
        analysisPageReact
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight(insight);
        assertEquals(analysisPageReact.openInsight(insight)
                .waitForReportComputing().getChartReport().getTrackersCount(), 3, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "changeTimeFilter" })
    public void changeAttributeFilter() throws JSONException, IOException {
        final String insight = "Test-Saved-Insight-After-Changing-Attribute-Filter";
        analysisPageReact.openInsight(INSIGHT_TEST)
                .saveInsightAs(insight)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        analysisPageReact
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight(insight);
        assertEquals(analysisPageReact.openInsight(insight)
                .waitForReportComputing().getChartReport().getTrackersCount(), 2, "Chart content is not as expected");
    }

    @Test(dependsOnMethods = { "changeAttributeFilter" })
    public void changeChartType() throws JSONException, IOException {
        final String insight = "Test-Saved-Insight-After-Changing-Chart-Type";
        analysisPageReact.openInsight(INSIGHT_TEST)
                .saveInsightAs(insight)
                .changeReportType(ReportType.BAR_CHART)
                .waitForReportComputing()
                .saveInsight();
        checkUndoRedoAfterSaveInsight(insight);
        assertEquals(
                analysisPageReact.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getChartType(),
                ReportType.BAR_CHART.getLabel(), "Chart type is not as expected");
    }

    private void checkUndoRedoAfterSaveInsight(final String expectedPreviousTitle) throws JSONException, IOException {
        final AnalysisPageHeader header = analysisPageReact.getPageHeader();
        final String savedTitle = header.getInsightTitle();
        final int numberOfInsights = getAllInsightNames(getRestApiClient(), testParams.getProjectId()).size();
        
        analysisPageReact.undo();
        assertEquals(header.getInsightTitle(), expectedPreviousTitle,
                "The expected previous title is not displayed");
        assertTrue(header.isUnsavedMessagePresent(), "Unsave notification is not displayed after undo");
        assertTrue(header.isSaveButtonEnabled(), "Save button is not enable after undo");
        assertEquals(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).size(),
                numberOfInsights, "The insight does not exist after undo");
        
        analysisPageReact.redo();
        assertEquals(header.getInsightTitle(), savedTitle,
                "The expected title is displayed after redo");
        assertFalse(header.isUnsavedMessagePresent(), "Unsave notification is displayed after redo");
        assertFalse(header.isSaveButtonEnabled(), "Save button is enable after undo");
    }
}
