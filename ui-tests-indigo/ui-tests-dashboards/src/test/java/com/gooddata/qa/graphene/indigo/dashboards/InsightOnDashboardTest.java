package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.InsightItem;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;

public class InsightOnDashboardTest extends AbstractDashboardTest {

    private static final String TEST_INSIGHT = "Test-Insight";
    private static final String RENAMED_TEST_INSIGHT = "Renamed-Test-Insight";
    private static final String INSIGHT_CREATED_BY_EDITOR = "Insight-Created-By-Editor";
    private static final String INSIGHT_CREATED_BY_MAIN_USER = "Insight-Created-By-Main-User";
    private static final String INSIGHT_FOR_CHECK_NO_DATA = "Insight-For-Check-No-Data";
    private static final List<String> INSIGHTS_FOR_FILTER_TEST = asList(INSIGHT_CREATED_BY_EDITOR,
            INSIGHT_CREATED_BY_MAIN_USER);
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Insight-On-Dashboard-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupDashboardENV"})
    public void testBlankStateInEditMode() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        // ignore checking existing of insight panel
        // because below assertions definitely fail in that case
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.ALL),
                "Created by me tab is not default filter");
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterVisible(FilterType.ALL),
                "All tab is not visible in filter section");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"createInsight"})
    public void testCreatingSimpleInsightUsingAd() throws JSONException, IOException {
        // need an insight having real data, so we can't use REST API
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(TEST_INSIGHT);
        assertTrue(indigoRestRequest.getAllInsightNames().contains(TEST_INSIGHT),
                TEST_INSIGHT + " is not created");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testAddInsight() {
        assertTrue(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .getLastWidget(Insight.class)
            .isDeleteButtonVisible(), "Added insight is not focused");

        assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), TEST_INSIGHT);
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testSavedDashboardContainingInsight() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();
        try {
            assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), TEST_INSIGHT);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(
                    indigoRestRequest.getAnalyticalDashboards().get(0));
        }
    }

    @DataProvider(name = "insightNameProvider")
    public Object[][] insightNameProvider() {
        return new Object[][]{
            { RENAMED_TEST_INSIGHT },
            { "<button>hello</button>" },
            { "????????????" }
        };
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"}, dataProvider = "insightNameProvider")
    public void testRenameInsightOnDashboard(String newInsightName) throws JSONException, IOException {
        IndigoDashboardsPage idp = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();

        try {
            idp.switchToEditMode()
                    .getLastWidget(Insight.class)
                    .clickOnContent()
                    .setHeadline(newInsightName);
            idp.saveEditModeWithWidgets();

            String headline = initIndigoDashboardsPageWithWidgets()
                    .getLastWidget(Insight.class)
                    .getHeadline();

            takeScreenshot(browser, "testRenameInsightOnDashboard-renamed", getClass());
            assertEquals(headline, newInsightName, "Insight not properly renamed");
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(
                    indigoRestRequest.getAnalyticalDashboards().get(0));
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightNamePlaceholder() throws JSONException, IOException {
        IndigoDashboardsPage idp = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();

        try {
            String headlinePlaceholder = idp.switchToEditMode()
                .getLastWidget(Insight.class)
                .clickOnContent()
                .clearHeadline()
                .getHeadlinePlaceholder();
            assertEquals(headlinePlaceholder, TEST_INSIGHT, "Insight placeholder not properly correct."
                    + "Expected: " + TEST_INSIGHT + " but: " + headlinePlaceholder);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(
                    indigoRestRequest.getAnalyticalDashboards().get(0));
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightRenderInViewModeAfterSwitchingPage() throws JSONException, IOException {
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
                singletonList(
                        indigoRestRequest.createVisualizationWidget(
                                indigoRestRequest.getInsightUri(TEST_INSIGHT),
                                TEST_INSIGHT
                        )
                ));
        try {
            AnalysisPage analysisPage = initAnalysePage();
            assertTrue(browser.getCurrentUrl().contains("/reportId/edit"), "AD page is not loaded");
            analysisPage.openInsight(TEST_INSIGHT).changeReportType(ReportType.COLUMN_CHART).saveInsight();
            checkInsightRender(initIndigoDashboardsPageWithWidgets().getLastWidget(Insight.class),
                    TEST_INSIGHT, 4);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleOnDashboardAfterRenamedInAD() throws JSONException, IOException {
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
                singletonList(
                        indigoRestRequest.createVisualizationWidget(
                                indigoRestRequest.getInsightUri(TEST_INSIGHT),
                                TEST_INSIGHT
                        )
                ));

        try {
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-beforeRename", getClass());

            initAnalysePage().openInsight(TEST_INSIGHT).waitForReportComputing().setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedBeforeRenameTitle = initIndigoDashboardsPageWithWidgets()
                .getLastWidget(Insight.class)
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-afterRename", getClass());
            assertEquals(insightInsertedBeforeRenameTitle, TEST_INSIGHT);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleOnDashboardAddedAfterRename() throws JSONException, IOException {
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
                singletonList(
                        indigoRestRequest.createVisualizationWidget(
                                indigoRestRequest.getInsightUri(TEST_INSIGHT),
                                TEST_INSIGHT
                        )
                ));

        try {
            initAnalysePage().openInsight(TEST_INSIGHT).waitForReportComputing().setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedAfterRenameTitle = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsightNext(RENAMED_TEST_INSIGHT)
                .getLastWidget(Insight.class)
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAddedAfterRename", getClass());
            assertEquals(insightInsertedAfterRenameTitle, RENAMED_TEST_INSIGHT);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleInADAfterRenamedOnDashboard() throws JSONException, IOException {
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
                singletonList(
                        indigoRestRequest.createVisualizationWidget(
                                indigoRestRequest.getInsightUri(TEST_INSIGHT),
                                TEST_INSIGHT
                        )
                ));

        try {
            IndigoDashboardsPage idp = initIndigoDashboardsPageWithWidgets();
            idp.switchToEditMode()
                .getLastWidget(Insight.class)
                .clickOnContent()
                .setHeadline(RENAMED_TEST_INSIGHT);
            idp.saveEditModeWithWidgets();

            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightRenamedInDashboards", getClass());

            AnalysisPage ap = initAnalysePage();

            assertTrue(ap.searchInsight(TEST_INSIGHT), TEST_INSIGHT + " should exist");
            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightWithOriginalNameFound", getClass());
            assertFalse(ap.searchInsight(RENAMED_TEST_INSIGHT), RENAMED_TEST_INSIGHT + " shouldn't exist");
            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightWithNewNameNotFound", getClass());
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV"})
    public void testCreatingInsightsForFilterTest() throws ParseException, IOException, JSONException {
        new IndigoRestRequest(new RestClient(
                new RestProfile(testParams.getHost(), testParams.getEditorUser(), testParams.getPassword(), true)),
                testParams.getProjectId())
                .createInsight(new InsightMDConfiguration(INSIGHT_CREATED_BY_EDITOR, ReportType.BAR_CHART));

        indigoRestRequest.createInsight(new InsightMDConfiguration(INSIGHT_CREATED_BY_MAIN_USER, ReportType.BAR_CHART));

        // need refresh to make sure the insights are added to working project
        initAnalysePage();
        assertEquals(
                indigoRestRequest.getAllInsightNames().stream()
                        .filter(INSIGHTS_FOR_FILTER_TEST::contains).count(),
                2, "The number of created insights is not correct");
    }

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"})
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

        // ONE-1653: List of insights created by me show as all insights on KPIs
        assertFalse(insights.stream().anyMatch(e -> e.matchesTitle(INSIGHT_CREATED_BY_EDITOR)),
                INSIGHT_CREATED_BY_EDITOR + " exists on result list");
    }

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"})
    public void testInsightListWithAllFilter() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .switchFilter(FilterType.ALL)
            .searchInsight("Insight-Created-By");

        assertEquals(
                indigoDashboardsPage.getInsightSelectionPanel()
                        .waitForInsightListVisible()
                        .getInsightItems()
                        .stream()
                        .filter(insight -> INSIGHTS_FOR_FILTER_TEST.stream()
                                .anyMatch(insight::matchesTitle))
                        .count(),
                2, "The expected insights are not displayed");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
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

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"}, dataProvider = "specialInsightNameProvider")
    public void testSearchUsingSpecialValues(final String searchValue) {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(searchValue);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
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

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"},
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

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"}, dataProvider = "filterTypeProvider")
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

    @Test(dependsOnGroups = {"setupDashboardENV"},
            description = "CL-10262: Save&Publish button is enabled right when selecting insight")
    public void disableSaveIfHavingNoChange() throws JSONException, IOException {
        String insight = "Insight-Created-From-Metric";
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().saveInsight(insight);

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(insight)
                .saveEditModeWithWidgets();

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            assertFalse(indigoDashboardsPage.isSaveEnabled(),
                    "Save button is enabled when dashboard has no change");
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(getWorkingDashboardUri());
        }
    }

    @Test(dependsOnGroups = {"createProject", "setupDashboardENV"})
    public void checkRenderingOfNoDataInsight() {
        indigoRestRequest.createInsight(new InsightMDConfiguration(INSIGHT_FOR_CHECK_NO_DATA, ReportType.BAR_CHART).
                setMeasureBucket(singletonList(MeasureBucket.
                        createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))).
                setCategoryBucket(singletonList(CategoryBucket.
                        createCategoryBucket(getAttributeByTitle(ATTR_ACTIVITY_TYPE), CategoryBucket.Type.VIEW))));

        initIndigoDashboardsPage().createDashboard().addInsight(INSIGHT_FOR_CHECK_NO_DATA).
                selectDateFilterByName(DateRange.LAST_7_DAYS.toString()).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getContentEmptyInsight(),
                "No data for your filter selection");
    }

    private void checkInsightRender(final Insight insight, final String expectedHeadline,
            final int expectedTracker) {
        assertEquals(insight.getHeadline(), expectedHeadline);
        assertEquals(insight.getChartReport().getTrackersCount(), expectedTracker,
                "The chart is not rendered correctly");
    }
}
