package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidget;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class ReorderInsightTest extends DashboardsTest {

    private static final String FIRST_INSIGHT = "First-Insight";
    private static final String SECOND_INSIGHT = "Second-Insight";
    private static final String THIRD_INSIGHT = "Third-Insight";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "Reoder-Insight-Test";
    }

    @Test(dependsOnMethods = { "initDashboardTests" }, groups = { "dashboardsInit" })
    public void enableInsightFlag() throws JSONException, IOException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS, true);
    }

    @Test(dependsOnGroups = { "dashboardsInit" })
    public void testAddingInsightsToDashboard() throws JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                asList(createBlankInsightUsingRest(FIRST_INSIGHT),
                        createBlankInsightUsingRest(SECOND_INSIGHT),
                        createBlankInsightUsingRest(THIRD_INSIGHT)));

        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(FIRST_INSIGHT, SECOND_INSIGHT, THIRD_INSIGHT, "Adding-Insight-To-Dashboard");
    }

    @Test(dependsOnMethods = { "testAddingInsightsToDashboard" }, groups = { "reoder-test" })
    public void testMovingInsightToFirstPosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.reoderWidget(indigoDashboardsPage.getVisualizationByIndex(2),
                indigoDashboardsPage.getVisualizationByIndex(0), DropZone.PREV);
        checkInsightOrder(THIRD_INSIGHT, FIRST_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-First-Position");
    }

    @Test(dependsOnMethods = { "testAddingInsightsToDashboard" }, groups = { "reoder-test" })
    public void testMovingInsightToLastPosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.reoderWidget(indigoDashboardsPage.getVisualizationByIndex(1),
                indigoDashboardsPage.getVisualizationByIndex(2), DropZone.NEXT);
        checkInsightOrder(FIRST_INSIGHT, THIRD_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-Last-Position");
    }

    @Test(dependsOnMethods = { "testAddingInsightsToDashboard" }, groups = { "reoder-test" })
    public void testMovingInsightToMiddlePosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.reoderWidget(indigoDashboardsPage.getVisualizationByIndex(0),
                indigoDashboardsPage.getVisualizationByIndex(1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Test-Moving-Insight-To-Middle-Position");
    }

    @Test(dependsOnMethods = { "testAddingInsightsToDashboard" }, dependsOnGroups = { "reoder-test" })
    public void testInsightOrderAfterSaving() throws JSONException, IOException {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.reoderWidget(indigoDashboardsPage.getVisualizationByIndex(0),
                indigoDashboardsPage.getVisualizationByIndex(1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Oder-Before-Saving");
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Oder-After-Saving");
    }

    @Test(dependsOnMethods = { "testInsightOrderAfterSaving" })
    public void testInsightOrderAfterSwitchingPage() throws JSONException, IOException {
        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Oder-Before-Switching-Page");
        initAnalysePage();
        assertTrue(browser.getCurrentUrl().contains("/reportId/edit"));
        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Oder-After-Switching-Page");
    }

    private String createBlankInsightUsingRest(final String insightTitle) throws JSONException, IOException {
        return createVisualizationWidget(getRestApiClient(), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(insightTitle)
                        .type(ReportType.BAR_CHART.getLabel())
                        .build());
    }

    private void checkInsightOrder(final String firstWidget, final String secondWidget, final String thirdWidget,
            final String screenShot) {
        takeScreenshot(browser,  screenShot, getClass());
        assertEquals(indigoDashboardsPage.getVisualizationByIndex(0).getHeadline(), firstWidget);
        assertEquals(indigoDashboardsPage.getVisualizationByIndex(1).getHeadline(), secondWidget);
        assertEquals(indigoDashboardsPage.getVisualizationByIndex(2).getHeadline(), thirdWidget);
    }
}
