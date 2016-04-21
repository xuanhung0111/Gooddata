package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.VisualizationsList;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidget;

import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import java.io.IOException;

import com.gooddata.qa.browser.DragAndDropUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class VisualizationsTest extends DashboardWithWidgetsTest {

    private final String VISUALIZATION_TITLE = "last_dummy_viz";

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void setupVisualizations() throws JSONException, IOException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS, true);

        createVisualizationWidget(getRestApiClient(), testParams.getProjectId(),
                new VisualizationMDConfiguration.Builder()
                        .title(VISUALIZATION_TITLE)
                        .build());
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void addVisualizationToLastPositionUsingDoubleClick() {
        WebElement visualizationItem = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .getVisualizationsList()
                .getVisualizationsListItems()
                .get(0);

        Actions action = new Actions(browser);
        action.doubleClick(visualizationItem).perform();

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getVisualizationsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPositionUsingDoubleClick-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastVisualization()
                .clickDeleteButton();

        indigoDashboardsPage
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getVisualizationsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPositionUsingDoubleClick-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void checkaddVisualizationFromList() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .getVisualizationsList()
                .getVisualizationsListItems()
                .get(0);

        String fromSelector = "." + VisualizationsList.MAIN_CLASS + ".s-" + VISUALIZATION_TITLE;
        String toSelector = ".dash-item-0 .dropzone." + DASH_WIDGET_PREV_DROPZONE_CLASS;

        DragAndDropUtils.dragAndDrop(browser, fromSelector, toSelector);

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getVisualizationsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastVisualization()
                .clickDeleteButton();

        indigoDashboardsPage
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getVisualizationsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }
}
