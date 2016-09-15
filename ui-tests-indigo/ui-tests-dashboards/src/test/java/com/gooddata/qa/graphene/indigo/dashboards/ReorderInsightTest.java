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
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidgetWrap;

public class ReorderInsightTest extends AbstractDashboardTest {

    private static final String FIRST_INSIGHT = "First-Insight";
    private static final String SECOND_INSIGHT = "Second-Insight";
    private static final String THIRD_INSIGHT = "Third-Insight";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "Reorder-Insight-Test";
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void testAddingInsightsToDashboard() throws JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), asList(
                createBlankInsightWrapUsingRest(FIRST_INSIGHT),
                createBlankInsightWrapUsingRest(SECOND_INSIGHT),
                createBlankInsightWrapUsingRest(THIRD_INSIGHT)
        ));

        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(FIRST_INSIGHT, SECOND_INSIGHT, THIRD_INSIGHT, "Adding-Insight-To-Dashboard");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToFirstPosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 2),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 0), DropZone.PREV);
        checkInsightOrder(THIRD_INSIGHT, FIRST_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-First-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToLastPosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 1),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 2), DropZone.NEXT);
        checkInsightOrder(FIRST_INSIGHT, THIRD_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-Last-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToMiddlePosition() {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Test-Moving-Insight-To-Middle-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, dependsOnGroups = {"reorder-test"})
    public void testInsightOrderAfterSaving() throws JSONException, IOException {
        initIndigoDashboardsPage().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-Before-Saving");
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-After-Saving");
    }

    @Test(dependsOnMethods = {"testInsightOrderAfterSaving"})
    public void testInsightOrderAfterSwitchingPage() throws JSONException, IOException {
        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-Before-Switching-Page");
        initAnalysePage();
        assertTrue(browser.getCurrentUrl().contains("/reportId/edit"));
        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-After-Switching-Page");
    }

    private String createBlankInsightWrapUsingRest(final String insightTitle) throws JSONException, IOException {
        String insightUri = createVisualizationWidget(getRestApiClient(), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(insightTitle)
                        .type(ReportType.BAR_CHART.getLabel())
                        .build());

        return createVisualizationWidgetWrap(getRestApiClient(), testParams.getProjectId(), insightUri, insightTitle);
    }

    private void checkInsightOrder(final String firstWidget, final String secondWidget, final String thirdWidget,
            final String screenShot) {
        takeScreenshot(browser,  screenShot, getClass());
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0).getHeadline(), firstWidget);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 1).getHeadline(), secondWidget);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 2).getHeadline(), thirdWidget);
    }
}
