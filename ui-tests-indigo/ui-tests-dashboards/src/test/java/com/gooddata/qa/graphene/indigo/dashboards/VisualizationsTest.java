package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import static java.util.Collections.singletonList;

import java.io.IOException;

public class VisualizationsTest extends AbstractDashboardTest {

    private final String VISUALIZATION_TITLE = "last_dummy_viz";
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void setupVisualizations() throws JSONException, IOException {
        indigoRestRequest.createInsight(new InsightMDConfiguration(VISUALIZATION_TITLE, ReportType.BAR_CHART));
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void addVisualizationToLastPosition() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsightNext(VISUALIZATION_TITLE)
                .waitForSaveButtonEnabled();

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPosition-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastWidget(Insight.class)
                .clickDeleteButton();

        indigoDashboardsPage
                .waitForSaveButtonEnabled()
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkAddVisualizationToLastPosition-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }

    @Test(dependsOnMethods = {"setupVisualizations"}, groups = {"desktop"})
    public void checkaddVisualizationFromList() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsightNext(VISUALIZATION_TITLE)
                .waitForSaveButtonEnabled();

        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();

        int visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-added_1_visualization", getClass());
        assertEquals(visualizationsCount, 1);

        indigoDashboardsPage
                .switchToEditMode()
                .selectLastWidget(Insight.class)
                .clickDeleteButton();

        indigoDashboardsPage
                .waitForSaveButtonEnabled()
                .leaveEditMode();

        visualizationsCount = indigoDashboardsPage.getInsightsCount();

        takeScreenshot(browser, "checkaddVisualizationFromList-removed_visualization", getClass());
        assertEquals(visualizationsCount, 0);
    }
}
