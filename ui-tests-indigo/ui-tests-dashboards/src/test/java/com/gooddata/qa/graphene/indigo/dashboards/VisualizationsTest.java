package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidget;
import static java.util.Collections.singletonList;

import java.io.IOException;

public class VisualizationsTest extends AbstractDashboardTest {

    private final String VISUALIZATION_TITLE = "last_dummy_viz";
    private final String VISUALIZATION_TYPE_BAR = "bar";

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void setupVisualizations() throws JSONException, IOException {
        createVisualizationWidget(getRestApiClient(), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(VISUALIZATION_TITLE)
                        .type(VISUALIZATION_TYPE_BAR)
                        .build());
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void addVisualizationToLastPosition() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsight(VISUALIZATION_TITLE);

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPosition-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastWidget(Insight.class)
                .clickDeleteButton();

        indigoDashboardsPage
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPosition-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void checkaddVisualizationFromList() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsight(VISUALIZATION_TITLE);

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastWidget(Insight.class)
                .clickDeleteButton();

        indigoDashboardsPage
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }
}
