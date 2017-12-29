package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesInsightTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TEST = "Insight-Test";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Insight-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_TEST);
    }

    @Test(dependsOnGroups = {"createProject"})
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
        return new Object[][]{
                {"report !@#$"},
                {"<a href=\"http://www.w3schools.com\">Visit W3Schools.com!</a>"},
                {UUID.randomUUID().toString()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "renameInsightDataProvider")
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
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

    @DataProvider(name = "chartTypeDataProvider")
    public Object[][] chartTypeDataProvider() {
        return new Object[][]{
                {ReportType.COLUMN_CHART},
                {ReportType.BAR_CHART},
                {ReportType.LINE_CHART}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartTypeDataProvider")
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
    public void testDefaultFilterOnInsightList() {
        assertTrue(analysisPage.getPageHeader().expandInsightSelection().isFilterActive(FilterType.BY_ME),
                "Default filter is not created by me tab");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testNoInsightMessageOnInsightListForBlankProject() {
        final String blankProject = "Blank-Project-For-Insight-Test";
        final String blankProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), blankProject,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams
                        .getProjectEnvironment());
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
    public void createInsightByEditor() throws JSONException {
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

    @Test(dependsOnMethods = {"testInsightListWithCreatedByMeFilter", "createInsightByEditor"})
    public void testInsightListWithAllFilter() {
        final AnalysisInsightSelectionPanel insightSelectionPanel = analysisPage.getPageHeader()
                .expandInsightSelection();

        insightSelectionPanel.switchFilter(FilterType.ALL).searchInsight("Insight-List-Test-With-Filter");
        assertEquals(insightSelectionPanel.getInsightItems().size(), 2, "The number of insights is not correct");
    }

    @DataProvider(name = "chartIconDataProvider")
    public Object[][] chartIconDataProvider() {
        return new Object[][]{
                {ReportType.COLUMN_CHART},
                {ReportType.BAR_CHART},
                {ReportType.LINE_CHART},
                {ReportType.TABLE}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartIconDataProvider")
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

    @Test(dependsOnGroups = {"createProject"})
    public void testDefaultInsightTitle() {
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), "Untitled insight",
                "The default title is not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testBlankInsightAfterSwitchingToOtherPage() {
        assertFalse(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().isBlankState(),
                "Workspace is blank before switching page");
        initDashboardsPage();
        assertTrue(initAnalysePage().isBlankState(), "AD does not show blank state after switching page");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testBlankInsightAfterSwitchingProject() {
        final String blankProject = "Blank-Project-For-Insight-Test";
        final String blankProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(),
                blankProject, testParams.getAuthorizationToken(),
                testParams.getProjectDriver(), testParams.getProjectEnvironment());

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

    @Test(dependsOnGroups = {"createProject"})
    public void openAsReportAfterSaveInsight() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .saveInsight("Open-As-Report-After-Save-Insight");

        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            List<String> attributes = reportPage.getTableReport().waitForLoaded().getAttributeValues();
            takeScreenshot(browser, "openAsReportAfterSaveInsight", getClass());
            assertEquals(attributes, asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"),
                    "Report is not rendered correctly");
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
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
