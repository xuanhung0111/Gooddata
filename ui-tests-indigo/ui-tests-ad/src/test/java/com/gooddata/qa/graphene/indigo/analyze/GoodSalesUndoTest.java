package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class GoodSalesUndoTest extends AbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Undo-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterAddMetric() {
        ReportState baseState = ReportState.getCurrentState(initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing());

        checkUndoRedoForEmptyState(true);
        checkUndoRedoForReport(baseState, false);

        analysisPage.addMetric(METRIC_AMOUNT).waitForReportComputing();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterAddAtribute() {
        initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();

        checkUndoRedoForEmptyState(true);

        analysisPage.redo();
        assertThat(analysisPage.getAttributesBucket().getItemNames(), hasItem(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterRemoveMetricAndAttribute() {
        ReportState baseState = ReportState.getCurrentState(initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertThat(analysisPage.getMetricsBucket().getItemNames(), not(hasItem(METRIC_NUMBER_OF_ACTIVITIES)));

        checkUndoRedoForReport(baseState, true);
        checkUndoRedoForEmptyState(false);

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE);
        ReportState baseStateWithAttribute = ReportState.getCurrentState(analysisPage);

        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);

        checkUndoRedoForReport(baseStateWithAttribute, true);
        checkUndoRedoForReport(baseState, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterChangeDateDimensionOnBucket() {
        final AttributesBucket categoriesBucket = initAnalysePage().getAttributesBucket();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().undo();
        assertTrue(categoriesBucket.isEmpty(), "Categories bucket should be empty");
        analysisPage.redo();
        assertFalse(categoriesBucket.isEmpty(), "Categories bucket shouldn't be empty");

        categoriesBucket.changeDateDimension(CREATED);
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
        analysisPage.undo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), ACTIVITY);
        analysisPage.redo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterChangeDateDimensionInFilter() {
        final FiltersBucket FiltersBucketReact = initAnalysePage().getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter().undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ACTIVITY), ACTIVITY + " filter shouldn't display");

        analysisPage.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ACTIVITY), ACTIVITY + " filter should display");

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

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterAddFilter() {
        int actionsCount = 0;
        final FiltersBucket FiltersBucketReact = initAnalysePage().getFilterBuckets();

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE); actionsCount++;
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES); actionsCount++;
        analysisPage.addFilter(ATTR_DEPARTMENT); actionsCount++;

        analysisPage.undo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter shouldn't display");

        analysisPage.redo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter should display");

        analysisPage.removeFilter(ATTR_DEPARTMENT);
        actionsCount++;
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter shouldn't display");
        takeScreenshot(browser, "Indigo_remove_filter", this.getClass());

        analysisPage.undo();
        assertTrue(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter should display");

        analysisPage.redo();
        assertFalse(FiltersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter shouldn't display");

        AnalysisPageHeader pageHeader = analysisPage.getPageHeader();
        // Check that the undo must go back to the start of his session
        assertTrue(pageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertFalse(pageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
        for (int i = 1; i <= actionsCount; i++) {
            analysisPage.undo();
        }
        assertFalse(pageHeader.isUndoButtonEnabled(), "Undo button should be disabled");
        assertTrue(pageHeader.isRedoButtonEnabled(), "Redo button should be enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterChangeReportType() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART), "Should be column chart");

        analysisPage.undo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE), "Should be table report");

        analysisPage.redo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART), "Should be column chart");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAfterReset() {
        ReportState baseState = ReportState.getCurrentState(initAnalysePage()
                .changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE));
        analysisPage.resetToBlankState();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testUndoNotApplicableOnNonActiveSession() {
        ReportState baseState = ReportState.getCurrentState(initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES));
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);

        final CatalogPanel catalogPanel = analysisPage.getCatalogPanel();
        catalogPanel.search(ATTR_DEPARTMENT);
        assertEquals(catalogPanel.getFieldNamesInViewPort(), singletonList(ATTR_DEPARTMENT));
        checkUndoRedoForReport(baseState, true);
        assertEquals(catalogPanel.getFieldNamesInViewPort(), singletonList(ATTR_DEPARTMENT));

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing().exportReport();
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
            assertTrue(analysisPage.getMetricsBucket().isEmpty(), "Metrics bucket should be empty");
            assertTrue(analysisPage.getAttributesBucket().isEmpty(), "Attributes bucket should be empty");
            assertTrue(analysisPage.getFilterBuckets().isEmpty(), "Filter bucket should be empty");
            assertTrue(analysisPage.getMainEditor().isEmpty(), "Main editor should be empty");
        } else {
            ReportState currentState = ReportState.getCurrentState(analysisPage);
            assertEquals(currentState, expectedState);
        }
      }

    private static class ReportState {
        private AnalysisPage analysisPage;

        private int reportTrackerCount;
        private List<String> addedAttributes;
        private List<String> addedMetrics;
        private List<String> reportDataLabels;
        private List<String> reportAxisLabels;

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

            reportDataLabels = report.getDataLabels();
            reportAxisLabels = report.getAxisLabels();

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
                !this.reportDataLabels.equals(state.reportDataLabels) ||
                !this.reportAxisLabels.equals(state.reportAxisLabels))
                return false;

            return true;
        }
    }
}
