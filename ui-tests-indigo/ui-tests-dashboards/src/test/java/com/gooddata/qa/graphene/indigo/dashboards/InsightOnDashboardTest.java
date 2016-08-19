package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidget;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getInsightUri;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Visualization;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.InsightItem;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidgetWrap;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class InsightOnDashboardTest extends DashboardsTest {

    private static final String TEST_INSIGHT = "Test-Insight";
    private static final String RENAMED_TEST_INSIGHT = "Renamed-Test-Insight";
    private static final String INSIGHT_CREATED_BY_EDITOR = "Insight-Created-By-Editor";
    private static final String INSIGHT_CREATED_BY_MAIN_USER = "Insight-Created-By-Main-User";
    private static final List<String> INSIGHTS_FOR_FILTER_TEST = asList(INSIGHT_CREATED_BY_EDITOR,
            INSIGHT_CREATED_BY_MAIN_USER);

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "Insight-On-Dashboard-Test";
    }

    @Test(dependsOnMethods = { "initDashboardTests" }, groups = { "dashboardsInit" })
    public void enableInsightFlag() throws JSONException, IOException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS, true);
    }

    @Test(dependsOnMethods = { "enableInsightFlag" }, groups = { "dashboardsInit" })
    public void testBlankStateInEditMode() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        //ignore checking existing of insight panel
        //because below assertions definitely fail in that case
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.BY_ME),
                "Created by me tab is not default filter");
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterVisible(FilterType.ALL),
                "All tab is not visible in filter section");
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "createInsight" })
    public void testCreatingSimpleInsightUsingAd() throws JSONException, IOException {
        //need an insight having real data, so we can't use REST API
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(TEST_INSIGHT);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(TEST_INSIGHT),
                TEST_INSIGHT + " is not created");
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testAddInsight() {
        assertTrue(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsightToLastPosition(TEST_INSIGHT)
            .getLastVisualization()
            .isDeleteButtonVisible(), "Added insight is not focused");
        checkInsightRender(indigoDashboardsPage.getLastVisualization(), TEST_INSIGHT, 4);
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testSavedDashboardContainingInsight() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsightToLastPosition(TEST_INSIGHT)
            .saveEditModeWithWidgets();
        try {
            checkInsightRender(indigoDashboardsPage.getLastVisualization(), TEST_INSIGHT, 4);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(),
                    getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0));
        }
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testInsightRenderInViewModeAfterSwitchingPage() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidgetWrap(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));
        try {
            initAnalysePage();
            assertTrue(browser.getCurrentUrl().contains("/reportId/edit"), "AD page is not loaded");
            checkInsightRender(initIndigoDashboardsPageWithWidgets().waitForAllInsightWidgetContentLoaded().getLastVisualization(),
                    TEST_INSIGHT, 4);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testInsightTitleOnDashboardAfterRenamedInAD() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidgetWrap(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));

        try {
            initIndigoDashboardsPageWithWidgets().waitForAllInsightWidgetContentLoaded();
            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-beforeRename", getClass());

            initAnalysePage().openInsight(TEST_INSIGHT).setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedBeforeRenameTitle = initIndigoDashboardsPageWithWidgets()
                .waitForAllInsightWidgetContentLoaded()
                .getLastVisualization()
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-afterRename", getClass());
            assertEquals(insightInsertedBeforeRenameTitle, TEST_INSIGHT);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testInsightTitleOnDashboardAddedAfterRename() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidgetWrap(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));

        try {
            initAnalysePage().openInsight(TEST_INSIGHT).setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedAfterRenameTitle = initIndigoDashboardsPageWithWidgets()
                .waitForAllInsightWidgetContentLoaded()
                .switchToEditMode()
                .addInsightToLastPosition(RENAMED_TEST_INSIGHT)
                .getLastVisualization()
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAddedAfterRename", getClass());
            assertEquals(insightInsertedAfterRenameTitle, RENAMED_TEST_INSIGHT);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = { "dashboardsInit" })
    public void testCreatingInsightsForFilterTest() throws ParseException, IOException, JSONException {
        addEditorUserToProject();
        initProjectsPage();

        createVisualizationWidget(
                getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword()), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(INSIGHT_CREATED_BY_EDITOR)
                        .type(ReportType.BAR_CHART.getLabel())
                        .build());

        createVisualizationWidget(
                getRestApiClient(), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(INSIGHT_CREATED_BY_MAIN_USER)
                        .type(ReportType.BAR_CHART.getLabel())
                        .build());

        //need refresh to make sure the insights are added to working project
        browser.navigate().refresh();
        waitForProjectsPageLoaded(browser);

        assertEquals(
                getAllInsightNames(getRestApiClient(), testParams.getProjectId()).stream()
                        .filter(e -> INSIGHTS_FOR_FILTER_TEST.contains(e)).count(),
                2, "The number of created insights is not correct");
    }

    @Test(dependsOnMethods = { "testCreatingInsightsForFilterTest" })
    public void testInsightListWithCreatedByMeFilter() {
        final List<InsightItem> insights = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .waitForInsightListVisible()
            .getInsightItems();
        takeScreenshot(browser, "Test-Insight-List-With-Created-By-Me-Filter", getClass());
        assertTrue(insights.stream().anyMatch(e -> e.matchesTitle(INSIGHT_CREATED_BY_MAIN_USER)),
                INSIGHT_CREATED_BY_MAIN_USER + " does not exist on result list");

        //ONE-1653: List of insights created by me show as all insights on KPIs
        assertFalse(insights.stream().anyMatch(e -> e.matchesTitle(INSIGHT_CREATED_BY_EDITOR)),
                INSIGHT_CREATED_BY_EDITOR + " exists on result list");
    }

    @Test(dependsOnMethods = { "testCreatingInsightsForFilterTest" })
    public void testInsightListWithAllFilter() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .switchFilter(FilterType.ALL)
            .searchInsight("Insight-Created-By");

        assertEquals(
                indigoDashboardsPage.getInsightSelectionPanel().waitForInsightListVisible().getInsightItems().stream()
                        .filter(e -> INSIGHTS_FOR_FILTER_TEST.stream().anyMatch(t -> e.matchesTitle(t))).count(),
                2, "The expected insights are not displayed");
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testNoMatchingSearch() {
        final String nonExistingInsight = "Non-Existing-Insight";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(nonExistingInsight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(), "No data message is not displayed");
    }

    @DataProvider(name = "specialInsightNameProvider")
    public Object[][] specialInsightNameProvider() {
        return new Object[][] {
            { "report !@#$" },
            { "<a href=\"http://www.w3schools.com\">Visit W3Schools.com!</a>" },
            { "                     " }
        };
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" }, dataProvider = "specialInsightNameProvider")
    public void testSearchUsingSpecialValues(final String searchValue) {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(searchValue);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" })
    public void testClearSearchInput() {
        final String insight = "Test-Clear-Search-Input";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(insight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");

        indigoDashboardsPage.getInsightSelectionPanel().clearInputText();
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isSearchTextBoxEmpty(),
                "Search text box is not empty");
        assertFalse(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message still exists after clicking on clear icon");
    }

    @Test(dependsOnGroups = { "dashboardsInit", "createInsight" },
            description = "ONE-1671: Search insights in dashboard always switch to created by me tab after clear search keyword")
    public void testSelectedFilterAfterClearingInput() {
        final String insight = "Test-Selected-Filter-After-Clearing-Search-Input";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .switchFilter(FilterType.ALL);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.ALL),
                "All tab was not selected ");

        indigoDashboardsPage.searchInsight(insight);
        indigoDashboardsPage.getInsightSelectionPanel().clearInputText();
        takeScreenshot(browser, "Test-Selected-Filter-After-Clearing-Input", getClass());
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.ALL),
                "All tab was not selected");
    }

    @DataProvider(name = "filterTypeProvider")
    public Object[][] filterTypeProvider() {
        return new Object[][] {
            {FilterType.ALL, INSIGHTS_FOR_FILTER_TEST},
            {FilterType.BY_ME, singletonList(INSIGHT_CREATED_BY_MAIN_USER)}
        };
    }

    @Test(dependsOnMethods = { "testCreatingInsightsForFilterTest" }, dataProvider = "filterTypeProvider")
    public void testInsightListWithFilterAfterClearingSearchInput(FilterType type,
            final List<String> expectedInsights) {
        final String nonExistingInsight = "Non-Existing-Insight";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(nonExistingInsight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");

        indigoDashboardsPage.getInsightSelectionPanel().clearInputText().switchFilter(type);

        final List<String> expectedInsightClasses = expectedInsights.stream()
                .map(e -> "s-" + simplifyText(e))
                .collect(toList());

        final List<String> insights = indigoDashboardsPage.getInsightSelectionPanel()
                .waitForInsightListVisible()
                .getInsightItems().stream()
                        .filter(e -> expectedInsightClasses.stream().anyMatch(t -> e.getCSSClass().contains(t)))
                        .map(InsightItem::getCSSClass)
                        .collect(toList());

        assertEquals(insights.size(), expectedInsights.size(), "The expected insights are not displayed");
    }

    private void checkInsightRender(final Visualization insight, final String expectedHeadline,
            final int expectedTracker) {
        assertEquals(insight.getHeadline(), expectedHeadline);
        assertEquals(insight.getChartReport().getTrackersCount(), expectedTracker,
                "The chart is not rendered correctly");
    }
}
