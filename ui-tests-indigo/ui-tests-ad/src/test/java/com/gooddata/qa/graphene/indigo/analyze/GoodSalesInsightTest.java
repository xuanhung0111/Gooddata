package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_STAGE_VELOCITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class GoodSalesInsightTest extends GoodSalesAbstractAnalyseTest {

    private static final String INSIGHT_TEST = "Insight-Test";
    private static final String CLOSED = "Closed";
    private static final String CREATED = "Created";
    private static final String DATE_CLOSED_DIMENSION_INSIGHT = "Save-Insight-Containing-Date-Closed-Dimension";
    private static final String DATE_CREATED_DIMENSION_INSIGHT = "Save-Insight-Containing-Date-Created-Dimension";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Save-And-Edit-Recent-Insight-Test";
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = { "init" })
    public void testSaveUntitledInsight() throws JSONException, IOException {
        final String insight = "Untitled-Insight-Test";
        final List<String> expectedLabels = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getChartReport()
                    .getDataLabels();
        analysisPage.saveInsight(insight);

        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " does not exist in Saved Insight list");
        assertEquals(
                analysisPage.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getDataLabels(),
                expectedLabels);
    }

    @Test(dependsOnGroups = { "init" })
    public void trySavingUntitledInsightButCancel() throws JSONException, IOException {
        final String insight = "No-Saved-Insight-After-Canceling-Test";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getPageHeader()
                .saveWithoutSubmitting(insight)
                .cancel();

        assertFalse(isElementPresent(className(SaveInsightDialog.ROOT_CLASS), browser),
                "Save dialog does not exist");
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " exists in Saved Insight list");
    }

    @Test(dependsOnGroups = { "init" })
    public void testSaveInsight() throws JSONException, IOException {
        final int expectedTrackerCount = analysisPage
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        analysisPage.setInsightTitle(INSIGHT_TEST).saveInsight();
        assertFalse(isElementVisible(className(SaveInsightDialog.ROOT_CLASS), browser),
                "Save dialog exists");
        //make sure data is cleared before open insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(analysisPage.openInsight(INSIGHT_TEST).getChartReport().getTrackersCount(),
                expectedTrackerCount);
    }

    @Test(dependsOnGroups = { "init" })
    public void testEditSavedInsight() throws JSONException, IOException {
        final String insight = "Editing-Saved-Insight-Test";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .saveInsight(insight);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                "The Insight is not created");

        final List<String> expectedLabels = analysisPage
                .removeAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getDataLabels();
        analysisPage.saveInsight();
        takeScreenshot(browser, "Edit-Insight-Test", getClass());

        //make sure data is cleared before open insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(analysisPage.openInsight(insight).waitForReportComputing().getChartReport()
                .getDataLabels(), expectedLabels);
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void testRenameInsight() throws JSONException, IOException {
        final String copyOfInsightTest = "Copy-Of-Insight-Test";
        final String renamedInsight = "Renamed-Insight";
        analysisPage.openInsight(INSIGHT_TEST)
                .waitForReportComputing()
                .saveInsightAs(copyOfInsightTest);
        final int numberOfInsights = getAllInsightNames(getRestApiClient(), testParams.getProjectId()).size();
        analysisPage.setInsightTitle(renamedInsight).saveInsight();
        checkRenamedInsight(numberOfInsights, copyOfInsightTest, renamedInsight);
    }

    @DataProvider(name = "renameInsightDataProvider")
    public Object[][] renameInsightDataProvider() {
        return new Object[][] {
                { "report !@#$" },
                { "<a href=\"http://www.w3schools.com\">Visit W3Schools.com!</a>" },
                { UUID.randomUUID().toString() }
        };
    }

    @Test(dependsOnMethods = { "testSaveInsight" }, dataProvider = "renameInsightDataProvider")
    public void renameInsightUsingSpecicalName(final String name) throws JSONException, IOException {
        final String insight = "Renaming-Saved-Insight-Test-Using-Special-Name"
                + UUID.randomUUID().toString().substring(0, 3);
        analysisPage.openInsight(INSIGHT_TEST)
                .waitForReportComputing()
                .saveInsightAs(insight);

        final int numberOfInsights = getAllInsightNames(getRestApiClient(), testParams.getProjectId()).size();
        analysisPage.setInsightTitle(name).saveInsight();
        takeScreenshot(browser, insight, getClass());
        checkRenamedInsight(numberOfInsights, insight, name);
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void testOpenInsight() {
        final ChartReport chart = analysisPage
                .openInsight(INSIGHT_TEST)
                .waitForReportComputing()
                .getChartReport();

        takeScreenshot(browser, "Open-Insight-test", getClass());
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), INSIGHT_TEST);
        assertEquals(chart.getTrackersCount(), 4);
        assertEquals(chart.getChartType(), ReportType.COLUMN_CHART.getLabel(), "Chart data type is not correct");
    }

    @DataProvider(name = "chartTypeDataProvider")
    public Object[][] chartTypeDataProvider() {
        return new Object[][] {
                { ReportType.COLUMN_CHART },
                { ReportType.BAR_CHART },
                { ReportType.LINE_CHART }
        };
    }

    @Test(dependsOnGroups = { "init" }, dataProvider = "chartTypeDataProvider")
    public void openVariousChartTypes(ReportType type) {
        final String insight = "Open-Various-Chart-Types-" + type.toString();
        final int expectedTrackersCount = analysisPage
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(type)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();
        analysisPage.saveInsight(insight);
        takeScreenshot(browser, type.toString() + " is created", getClass());

        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(analysisPage.openInsight(insight).waitForReportComputing().getChartReport().getChartType(),
                type.getLabel(), "The expected chart type is not displayed");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), expectedTrackersCount,
                "Chart content is not correct");
    }

    @Test(dependsOnGroups = { "init" })
    public void testOpenTableReport() {
        final String insight = "Open-Table-Report";
        final List<List<String>> expectedContent = analysisPage
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getTableReport()
                .getContent();
        analysisPage.saveInsight(insight);
        takeScreenshot(browser, "Table-Chart-is-created", getClass());

        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(
                analysisPage.openInsight(insight)
                        .waitForReportComputing()
                        .getTableReport()
                        .getContent(),
                expectedContent, "Table content is not correct");
        assertTrue(analysisPage.getTableReport().isHeaderSortedUp(ATTR_ACTIVITY_TYPE),
                ATTR_ACTIVITY_TYPE + " is not sorted up");
    }

    @Test(dependsOnGroups = { "init" })
    public void testDefaultFilterOnInsightList() {
        assertTrue(analysisPage.getPageHeader().expandInsightSelection().isFilterActive(FilterType.BY_ME),
                "Default filter is not created by me tab");
    }

    @Test(dependsOnGroups = { "init" })
    public void testNoInsightMessageOnInsightListForBlankProject() {
        final String blankProject = "Blank-Project-For-Insight-Test";
        final String blankProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), blankProject,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        final String insightTestProjectId = testParams.getProjectId();
        testParams.setProjectId(blankProjectId);
        try {
            assertTrue(initAnalysePage().getPageHeader().expandInsightSelection().isEmpty(),
                    "No insight message is not displayed");
        } finally {
            testParams.setProjectId(insightTestProjectId);
            ProjectRestUtils.deleteProject(getGoodDataClient(), blankProjectId);
        }
    }

    @Test(dependsOnGroups = { "init" })
    public void testInsightListWithCreatedByMeFilter() throws JSONException, IOException {
        final String insight = "Insight-List-Test-With-Filter-Created-By-Me";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " exists before saving");
        analysisPage.saveInsight(insight);
        final AnalysisInsightSelectionPanel insightSelectionPanel = analysisPage
                .getPageHeader()
                .expandInsightSelection();
        insightSelectionPanel.switchFilter(FilterType.BY_ME).searchInsight(insight);
        assertEquals(insightSelectionPanel.getInsightItems().size(), 1, "The number of insights is not correct");
    }

    @Test(dependsOnGroups = { "init" })
    public void testEditorCanSaveInsight() throws JSONException {
        //using this name helps us remove redundant code in testInsightListWithAllFilter
        final String insight = "Insight-List-Test-With-Filter-All";
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            final List<String> expectedLabels = initAnalysePage()
                    .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getChartReport()
                    .getDataLabels();
            analysisPage.saveInsight(insight);
            //make sure the workspace is blank before opening insight 
            assertTrue(analysisPage.resetToBlankState().isBlankState(), "The workspace is not blank");
            assertEquals(
                    analysisPage.openInsight(insight)
                            .waitForReportComputing()
                            .getChartReport()
                            .getDataLabels(),
                    expectedLabels);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"testInsightListWithCreatedByMeFilter", "testEditorCanSaveInsight"})
    public void testInsightListWithAllFilter() {
        final AnalysisInsightSelectionPanel insightSelectionPanel = analysisPage
                    .getPageHeader()
                    .expandInsightSelection();
        insightSelectionPanel.switchFilter(FilterType.ALL).searchInsight("Insight-List-Test-With-Filter");
        assertEquals(insightSelectionPanel.getInsightItems().size(), 2, "The number of insights is not correct");
    }

    @DataProvider(name = "chartIconDataProvider")
    public Object[][] chartIconDataProvider() {
        return new Object[][] {
                { ReportType.COLUMN_CHART },
                { ReportType.BAR_CHART },
                { ReportType.LINE_CHART },
                { ReportType.TABLE }
        };
    }

    @Test(dependsOnGroups = { "init" }, dataProvider = "chartIconDataProvider")
    public void testChartIconOnInsightList(ReportType type) {
        final String insight = "Chart-Icon-On-Insight-List-Test-" + type.getLabel();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(type)
                .getPageHeader()
                .saveInsight(insight);

        assertEquals(
                analysisPage.getPageHeader()
                        .expandInsightSelection()
                        .getInsightItem(insight)
                        .getVizType(),
                type.getLabel());
    }

    @Test(dependsOnGroups = { "init" })
    public void testDefaultInsightTitle() {
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), "Untitled insight",
                "The default title is not correct");
    }

    @Test(dependsOnGroups = { "init" })
    public void testSaveAsButtonNotPresentInBlankState() {
        assertFalse(analysisPage.getPageHeader().isSaveAsPresent(),
                "Save As button is dispayed at start state");
    }

    @Test(dependsOnGroups = { "init" })
    public void cannotSaveAsInsightCreatedFromBlankState() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertFalse(analysisPage.getPageHeader().isSaveAsPresent(),
                "Save As button is dispayed after a few changes");
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void testSaveAsUsingExistingInsightWithoutChange() {
        final String insight = "Save-As-Using-Existing-Insight-Without-Change";
        final int expectedTrackersCount = analysisPage
                .openInsight(INSIGHT_TEST)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        //make sure the workspace is blank before opening insight 
        assertTrue(analysisPage.saveInsightAs(insight).resetToBlankState().isBlankState(),
                "The workspace is not blank");
        assertEquals(analysisPage.openInsight(insight).getChartReport().getTrackersCount(), expectedTrackersCount);
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void testSaveAsUsingExistingInsightWithChange() {
        final String insight = "Save-As-Using-Existing-Insight-With-Change";
        analysisPage.openInsight(INSIGHT_TEST)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting");
        final int expectedTrackersCount = analysisPage
                    .waitForReportComputing()
                    .getChartReport()
                    .getTrackersCount();

        //make sure the workspace is blank before opening insight 
        assertTrue(analysisPage.saveInsightAs(insight).resetToBlankState().isBlankState(),
                "The workspace is not blank");
        assertEquals(
                analysisPage.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getTrackersCount(),
                expectedTrackersCount);

        //check original insight is not affected by save as
        assertEquals(analysisPage.openInsight(INSIGHT_TEST).waitForReportComputing().getChartReport()
                .getTrackersCount(), 4);
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void deleteUnsavedChangesInsight() throws JSONException, IOException {
        final String insight = "Delete-Unsaved-Change-Insight";
        assertTrue(
                analysisPage.openInsight(INSIGHT_TEST)
                        .saveInsightAs(insight)
                        .removeAttribute(ATTR_ACTIVITY_TYPE)
                        .waitForReportComputing()
                        .getPageHeader()
                        .isUnsavedMessagePresent(),
                "Unsaved notification is not dispayed");
        assertFalse(analysisPage.isBlankState(), "Workspace is cleared before deleting insight");
        analysisPage.getPageHeader()
                .expandInsightSelection()
                .getInsightItem(insight)
                .delete();
        assertTrue(analysisPage.isBlankState(), "Workspace has not been cleared");
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId())
                .contains(insight), insight + " has not been deleted");
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void deleteCurrentlyOpenedInsight() throws JSONException, IOException {
        final String insight = "Delete-Currently-Opened-Insight";
        assertFalse(analysisPage.openInsight(INSIGHT_TEST).saveInsightAs(insight).isBlankState(),
                "Workspace is cleared before deleting insight");
        analysisPage.getPageHeader()
                .expandInsightSelection()
                .getInsightItem(insight)
                .delete();
        assertTrue(analysisPage.isBlankState(), "Workspace has not been cleared");
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId())
                .contains(insight), insight + " has not been deleted");
    }

    @Test(dependsOnMethods = { "testSaveInsight" })
    public void deleteNotCurrentlyOpenedInsight() throws JSONException, IOException {
        final String insight = "Delete-Currently-Opened-Insight";
        analysisPage.openInsight(INSIGHT_TEST).saveInsightAs(insight);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId())
                .contains(insight), insight + " does not exist");
        analysisPage.openInsight(INSIGHT_TEST)
                .getPageHeader()
                .expandInsightSelection()
                .getInsightItem(insight)
                .delete();
        assertFalse(analysisPage.isBlankState(), "Workspace is cleared");
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId())
                .contains(insight), insight + " has not been deleted");
    }

    @Test(dependsOnGroups = { "init" })
    public void testBlankInsightAfterSwitchingToOtherPage() {
        assertFalse(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().isBlankState(),
                "Workspace is blank before switching page");
        initDashboardsPage();
        assertTrue(initAnalysePage().isBlankState(), "AD does not show blank state after switching page");
    }

    @Test(dependsOnGroups = { "init" })
    public void testBlankInsightAfterSwitchingProject() {
        final String blankProject = "Blank-Project-For-Insight-Test";
        final String blankProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), blankProject,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        assertFalse(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().isBlankState(),
                "Workspace is blank before switching project");

        final String mainProjectId = testParams.getProjectId();
        testParams.setProjectId(blankProjectId);
        try {
            initAnalysePage();
            assertTrue(browser.getCurrentUrl().contains(blankProjectId));
        } finally {
            testParams.setProjectId(mainProjectId);
            initAnalysePage();
            ProjectRestUtils.deleteProject(getGoodDataClient(), blankProjectId);
            assertTrue(browser.getCurrentUrl().contains(mainProjectId));
            assertTrue(analysisPage.isBlankState(), "AD does not show blank state after switching project");
        }
    }

    @Test(dependsOnGroups = { "init" }, groups = { "save-insight-containing-date-dimension" })
    public void testSaveInsightContainingDateClosedDimension() {
        final int expectedTrackers = analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "The selected dimension was not displayed");

        analysisPage.saveInsight(DATE_CLOSED_DIMENSION_INSIGHT);
        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState(), "Working workspace was not been cleared");
        analysisPage.openInsight(DATE_CLOSED_DIMENSION_INSIGHT).waitForReportComputing();

        takeScreenshot(browser, "Save-Insight-Containing-Date-Closed-Dimension", getClass());
        assertEquals(analysisPage.getChartReport().getTrackersCount(), expectedTrackers, "Chart render was not correct");
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "The selected dimension was not displayed");
    }

    @Test(dependsOnGroups = { "init" }, groups = { "save-insight-containing-date-dimension" })
    public void testSaveInsightContainingDateCreatedDimension() {
        final int expectedTrackers = analysisPage.addMetric(METRIC_STAGE_VELOCITY)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "The selected dimension was not displayed");

        analysisPage.saveInsight(DATE_CREATED_DIMENSION_INSIGHT);
        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState(), "Working workspace was not been cleared");
        analysisPage.openInsight(DATE_CREATED_DIMENSION_INSIGHT).waitForReportComputing();

        takeScreenshot(browser, "Save-Insight-Containing-Date-Created-Dimension", getClass());
        assertEquals(analysisPage.getChartReport().getTrackersCount(), expectedTrackers, "Chart render was not correct");
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "The selected dimension was not displayed");
    }

    @Test(dependsOnGroups = { "save-insight-containing-date-dimension" },
            description = "CL-9969: Date is remembered from previous viz")
    public void testDateDimensionOnSavedInsights() {
        //make sure data is cleared before opening first insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        analysisPage.openInsight(DATE_CLOSED_DIMENSION_INSIGHT).waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "Selected date dimension of " + DATE_CLOSED_DIMENSION_INSIGHT + "was not correct");

        //make sure the first insight is loaded 
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), DATE_CLOSED_DIMENSION_INSIGHT);
        //the second insight should be opened right after the first one 
        assertEquals(analysisPage.openInsight(DATE_CREATED_DIMENSION_INSIGHT).waitForReportComputing()
                .getPageHeader().getInsightTitle(), DATE_CREATED_DIMENSION_INSIGHT);
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "Selected date dimension of " + DATE_CREATED_DIMENSION_INSIGHT + "was not correct");
    }

    private void checkRenamedInsight(final int expectedNumberOfInsights, final String oldInsight,
            final String newInsight) throws JSONException, IOException {
        final List<String> savedInsightNames = getAllInsightNames(getRestApiClient(), testParams.getProjectId());
        assertEquals(savedInsightNames.size(), expectedNumberOfInsights);
        assertEquals(savedInsightNames.stream().filter(e -> e.equals(newInsight)).count(), 1,
                "There is more than 1 insight or no insight named" + newInsight);
        assertFalse(savedInsightNames.contains(oldInsight), oldInsight + " has not been renamed");
    }
}
