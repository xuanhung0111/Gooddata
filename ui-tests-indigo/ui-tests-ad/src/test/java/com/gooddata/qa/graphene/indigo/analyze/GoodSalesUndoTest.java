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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesUndoTest extends GoodSalesAbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Undo-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddMetric() {
        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES));

        checkUndoRedoForEmptyState(true);
        checkUndoRedoForReport(baseState, false);

        analysisPage.addMetric(METRIC_AMOUNT);
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddAtribute() {
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);

        checkUndoRedoForEmptyState(true);

        analysisPage.redo();
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterRemoveMetricAndAttribute() {
        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertFalse(analysisPage.getMetricsBucket().getItemNames().contains(METRIC_NUMBER_OF_ACTIVITIES));

        checkUndoRedoForReport(baseState, true);
        checkUndoRedoForEmptyState(false);

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE);
        ReportState baseStateWithAttribute = ReportState.getCurrentState(analysisPage);

        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);

        checkUndoRedoForReport(baseStateWithAttribute, true);
        checkUndoRedoForReport(baseState, false);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeDateDimensionOnBucket() {
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().undo();
        assertTrue(categoriesBucket.isEmpty());
        analysisPage.redo();
        assertFalse(categoriesBucket.isEmpty());

        categoriesBucket.changeDateDimension(CREATED);
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
        analysisPage.undo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), ACTIVITY);
        analysisPage.redo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeDateDimensionInFilter() {
        final FiltersBucket FiltersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter().undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ACTIVITY));

        analysisPage.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ACTIVITY));

        WebElement filter = FiltersBucketReact.getFilter(ACTIVITY);
        FiltersBucketReact.changeDateDimension(ACTIVITY, CREATED);

        analysisPage.undo();
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
              waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertEquals(panel.getSelectedDimensionSwitch(), ACTIVITY);

        analysisPage.redo();
        filter.click();
        waitForElementVisible(panel.getRoot());
        assertEquals(panel.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterAddFilter() {
        int actionsCount = 0;
        final FiltersBucket FiltersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE); actionsCount++;
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES); actionsCount++;
        analysisPage.addFilter(ATTR_DEPARTMENT); actionsCount++;

        analysisPage.undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.removeFilter(ATTR_DEPARTMENT);
        actionsCount++;
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));
        takeScreenshot(browser, "Indigo_remove_filter", this.getClass());

        analysisPage.undo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.redo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        AnalysisPageHeader pageHeader = analysisPage.getPageHeader();
        // Check that the undo must go back to the start of his session
        assertTrue(pageHeader.isUndoButtonEnabled());
        assertFalse(pageHeader.isRedoButtonEnabled());
        for (int i = 1; i <= actionsCount; i++) {
            analysisPage.undo();
        }
        assertFalse(pageHeader.isUndoButtonEnabled());
        assertTrue(pageHeader.isRedoButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterChangeReportType() {
        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));

        analysisPage.undo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART));

        analysisPage.redo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testAfterReset() {
        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE));
        analysisPage.resetToBlankState();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoNotApplicableOnNonActiveSession() {
        ReportState baseState = ReportState.getCurrentState(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES));
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);

        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();
        cataloguePanel.search(ATTR_DEPARTMENT);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(ATTR_DEPARTMENT));
        checkUndoRedoForReport(baseState, true);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(ATTR_DEPARTMENT));

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).exportReport();
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
            assertTrue(analysisPage.getMetricsBucket().isEmpty());
            assertTrue(analysisPage.getAttributesBucket().isEmpty());
            assertTrue(analysisPage.getFilterBuckets().isEmpty());
            assertTrue(analysisPage.getMainEditor().isEmpty());
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
