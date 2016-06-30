package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPageReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeaderReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanelReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanelReact;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesUndoTest extends GoodSalesAbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Undo-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddMetric() {
        ReportState baseState = ReportState.getCurrentState(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES));

        checkUndoRedoForEmptyState(true);
        checkUndoRedoForReport(baseState, false);

        analysisPageReact.addMetric(METRIC_AMOUNT);
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddAtribute() {
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE);

        checkUndoRedoForEmptyState(true);

        analysisPageReact.redo();
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterRemoveMetricAndAttribute() {
        ReportState baseState = ReportState.getCurrentState(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES));

        analysisPageReact.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertFalse(analysisPageReact.getMetricsBucket().getItemNames().contains(METRIC_NUMBER_OF_ACTIVITIES));

        checkUndoRedoForReport(baseState, true);
        checkUndoRedoForEmptyState(false);

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE);
        ReportState baseStateWithAttribute = ReportState.getCurrentState(analysisPageReact);

        analysisPageReact.removeAttribute(ATTR_ACTIVITY_TYPE);

        checkUndoRedoForReport(baseStateWithAttribute, true);
        checkUndoRedoForReport(baseState, false);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeDateDimensionOnBucket() {
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().undo();
        assertTrue(categoriesBucket.isEmpty());
        analysisPageReact.redo();
        assertFalse(categoriesBucket.isEmpty());

        categoriesBucket.changeDateDimension(CREATED);
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
        analysisPageReact.undo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), ACTIVITY);
        analysisPageReact.redo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeDateDimensionInFilter() {
        final FiltersBucketReact FiltersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter().undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ACTIVITY));

        analysisPageReact.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ACTIVITY));

        WebElement filter = FiltersBucketReact.getFilter(ACTIVITY);
        FiltersBucketReact.changeDateDimension(ACTIVITY, CREATED);

        analysisPageReact.undo();
        filter.click();
        DateFilterPickerPanelReact panel = Graphene.createPageFragment(DateFilterPickerPanelReact.class,
              waitForElementVisible(DateFilterPickerPanelReact.LOCATOR, browser));
        assertEquals(panel.getSelectedDimensionSwitch(), ACTIVITY);

        analysisPageReact.redo();
        filter.click();
        waitForElementVisible(panel.getRoot());
        assertEquals(panel.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddFilter() {
        int actionsCount = 0;
        final FiltersBucketReact FiltersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE); actionsCount++;
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES); actionsCount++;
        analysisPageReact.addFilter(ATTR_DEPARTMENT); actionsCount++;

        analysisPageReact.undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.removeFilter(ATTR_DEPARTMENT);
        actionsCount++;
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));
        takeScreenshot(browser, "Indigo_remove_filter", this.getClass());

        analysisPageReact.undo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.redo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        AnalysisPageHeaderReact pageHeader = analysisPageReact.getPageHeader();
        // Check that the undo must go back to the start of his session
        assertTrue(pageHeader.isUndoButtonEnabled());
        assertFalse(pageHeader.isRedoButtonEnabled());
        for (int i = 1; i <= actionsCount; i++) {
            analysisPageReact.undo();
        }
        assertFalse(pageHeader.isUndoButtonEnabled());
        assertTrue(pageHeader.isRedoButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeReportType() {
        analysisPageReact.changeReportType(ReportType.TABLE);
        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.TABLE));

        analysisPageReact.undo();
        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.COLUMN_CHART));

        analysisPageReact.redo();
        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.TABLE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterReset() {
        ReportState baseState = ReportState.getCurrentState(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE));
        analysisPageReact.resetToBlankState();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoNotApplicableOnNonActiveSession() {
        ReportState baseState = ReportState.getCurrentState(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES));
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE);

        final CataloguePanelReact cataloguePanel = analysisPageReact.getCataloguePanel();
        cataloguePanel.search(ATTR_DEPARTMENT);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(ATTR_DEPARTMENT));
        checkUndoRedoForReport(baseState, true);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(ATTR_DEPARTMENT));

        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE).exportReport();
        checkUndoRedoForReport(baseState, true);
    }

    private void checkUndoRedoForEmptyState(boolean isUndo) {
        checkUndoRedoForReport(null, isUndo);
    }

    private void checkUndoRedoForReport(ReportState expectedState, boolean isUndo) {
        if (isUndo) {
            analysisPageReact.undo();
        } else {
            analysisPageReact.redo();
        }

        if (expectedState == null) {
            assertTrue(analysisPageReact.getMetricsBucket().isEmpty());
            assertTrue(analysisPageReact.getAttributesBucket().isEmpty());
            assertTrue(analysisPageReact.getFilterBuckets().isEmpty());
            assertTrue(analysisPageReact.getMainEditor().isEmpty());
        } else {
            ReportState currentState = ReportState.getCurrentState(analysisPageReact);
            assertTrue(currentState.equals(expectedState));
        }
      }

    private static class ReportState {
        private AnalysisPageReact analysisPage;

        private int reportTrackerCount;
        private List<String> addedAttributes;
        private List<String> addedMetrics;
        private List<String> reportDataLables;
        private List<String> reportAxisLables;

        public static ReportState getCurrentState(AnalysisPageReact analysisPage) {
            return new ReportState(analysisPage).saveCurrentState();
        }

        private ReportState(AnalysisPageReact analysisPage) {
            this.analysisPage = analysisPage;
        }

        private ReportState saveCurrentState() {
            analysisPage.waitForReportComputing();
            ChartReportReact report = analysisPage.getChartReport();

            reportTrackerCount = report.getTrackersCount();
            addedMetrics = analysisPage.getMetricsBucket().getItemNames();
            addedAttributes = analysisPage.getAttributesBucket().getItemNames();

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
