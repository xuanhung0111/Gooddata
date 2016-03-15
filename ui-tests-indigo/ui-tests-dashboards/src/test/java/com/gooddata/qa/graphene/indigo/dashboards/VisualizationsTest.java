package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class VisualizationsTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void setupVisualizationsFeatureFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS, true);
    }

    @Test(dependsOnMethods = {"setupVisualizationsFeatureFlag"}, groups = {"desktop"})
    public void checkNoVisualizationOnDashboard() {
        int visualizationsCount = initIndigoDashboardsPageWithWidgets().getVisualizationsCount();

        takeScreenshot(browser, "checkNoVisualizationOnDashboard", getClass());
        assertEquals(visualizationsCount, 0);
    }

    @Test(dependsOnMethods = {"setupVisualizationsFeatureFlag"}, groups = {"desktop"})
    public void checkVisualizationsListItemsCount() {
        int visualizationsListItemsCount = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .getVisualizationsList()
                .getVisualizationsListItems()
                .size();

        takeScreenshot(browser, "checkVisualizationsListItemsCount", getClass());
        assertEquals(visualizationsListItemsCount, 2);
    }

    @Test(dependsOnMethods = {"setupVisualizationsFeatureFlag"}, groups = {"desktop"})
    public void checkAddVisualizationToLastPositionUsingDoubleClick() {
        WebElement visualizationItem = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .getVisualizationsList()
                .getVisualizationsListItems()
                .get(0);

        Actions action = new Actions(browser);
        action.doubleClick(visualizationItem).perform();

        indigoDashboardsPage.leaveEditMode();

        int visualizationsCount = initIndigoDashboardsPageWithWidgets().getVisualizationsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPositionUsingDoubleClick-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        initIndigoDashboardsPage()
                .switchToEditMode()
                .selectLastVisualization()
                .clickDeleteButton();

        indigoDashboardsPage
                .leaveEditMode();

        visualizationsCount = initIndigoDashboardsPageWithWidgets().getVisualizationsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPositionUsingDoubleClick-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }
}
