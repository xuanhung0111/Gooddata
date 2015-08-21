package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesBasicUndoRedoTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Basic-Undo-Redo-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedoAfterAddMetric() {
        initAnalysePage();

        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(NUMBER_OF_ACTIVITIES));

        checkUndoRedoForEmptyState(true);
        checkUndoRedoForReport(baseState, false);

        analysisPage.addMetric(AMOUNT);
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedoAfterAddAtribute() {
        initAnalysePage();

        analysisPage.addCategory(ACTIVITY_TYPE);

        checkUndoRedoForEmptyState(true);

        analysisPage.redo();
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedoAfterRemoveMetricAndAttribute() {
        initAnalysePage();

        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(NUMBER_OF_ACTIVITIES));

        analysisPage.removeMetric(NUMBER_OF_ACTIVITIES);
        assertFalse(analysisPage.getAllAddedMetricNames().contains(NUMBER_OF_ACTIVITIES));

        checkUndoRedoForReport(baseState, true);
        checkUndoRedoForEmptyState(false);

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addCategory(ACTIVITY_TYPE);
        ReportState baseStateWithAttribute = ReportState.getCurrentState(analysisPage);

        analysisPage.removeCategory(ACTIVITY_TYPE);

        checkUndoRedoForReport(baseStateWithAttribute, true);
        checkUndoRedoForReport(baseState, false);
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedoAfterAddFilter() {
        int actionsCount = 0;
        initAnalysePage();

        analysisPage.addCategory(ACTIVITY_TYPE); actionsCount++;
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES); actionsCount++;
        analysisPage.addFilter(DEPARTMENT); actionsCount++;

        analysisPage.undo();
        assertFalse(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.redo();
        assertTrue(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.removeFilter(DEPARTMENT);
        actionsCount++;
        assertFalse(analysisPage.isFilterVisible(DEPARTMENT));
        takeScreenshot(browser, "Indigo_remove_filter", this.getClass());

        analysisPage.undo();
        assertTrue(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.redo();
        assertFalse(analysisPage.isFilterVisible(DEPARTMENT));

        // Check that the undo must go back to the start of his session
        assertTrue(analysisPage.isUndoButtonEnabled());
        assertFalse(analysisPage.isRedoButtonEnabled());
        for (int i = 1; i <= actionsCount; i++) {
            analysisPage.undo();
        }
        assertFalse(analysisPage.isUndoButtonEnabled());
        assertTrue(analysisPage.isRedoButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedoAfterChangeReportType() {
        initAnalysePage();

        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));

        analysisPage.undo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART));

        analysisPage.redo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoAfterReset() {
        initAnalysePage();

        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addCategory(ACTIVITY_TYPE));
        analysisPage.resetToBlankState();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoNotApplicableOnNonActiveSession() {
        initAnalysePage();

        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(NUMBER_OF_ACTIVITIES));
        analysisPage.addCategory(ACTIVITY_TYPE)
            .searchBucketItem(DEPARTMENT);
        assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList(DEPARTMENT));
        checkUndoRedoForReport(baseState, true);
        assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList(DEPARTMENT));

        analysisPage.addCategory(ACTIVITY_TYPE).exportReport();
        checkUndoRedoForReport(baseState, true);
    }

    private void checkUndoRedoForEmptyState(boolean isUndo) {
        checkUndoRedoForReport(null, isUndo);
    }

    private void checkUndoRedoForReport(ReportState expectedState, boolean isUndo) {
        if (isUndo) {
            analysisPage.undo();
        } else {
            analysisPage.redo();
        }

        if (expectedState == null) {
            assertTrue(analysisPage.isBucketBlankState());
            assertTrue(analysisPage.isMainEditorBlankState());
        } else {
            ReportState currentState = ReportState.getCurrentState(analysisPage);
            assertTrue(currentState.equals(expectedState));
        }
      }

    private static class ReportState {
        private AnalysisPage analysisPage;

        private int reportTrackerCount;
        private List<String> addedAttributes;
        private List<String> addedMetrics;
        private List<String> reportDataLables;
        private List<String> reportAxisLables;

        public static ReportState getCurrentState(AnalysisPage analysisPage) {
            return new ReportState(analysisPage).saveCurrentState();
        }

        private ReportState(AnalysisPage analysisPage) {
            this.analysisPage = analysisPage;
        }

        private ReportState saveCurrentState() {
            analysisPage.waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();

            reportTrackerCount = report.getTrackersCount();
            addedMetrics = analysisPage.getAllAddedMetricNames();
            addedAttributes = analysisPage.getAllAddedCategoryNames();

            reportDataLables = report.getDataLabels();
            reportAxisLables = report.getAxisLabels();

            return this;
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof ReportState))
                return false;

            ReportState state = (ReportState)obj;

            if (this.reportTrackerCount != state.reportTrackerCount ||
                !this.addedAttributes.equals(state.addedAttributes) ||
                !this.addedMetrics.equals(state.addedMetrics) ||
                !this.reportDataLables.equals(state.reportDataLables) ||
                !this.reportAxisLables.equals(state.reportAxisLables))
                return false;

            return true;
        }
    }
}
