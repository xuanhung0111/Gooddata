package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;

public class PartialExportDashboardsTest extends AbstractProjectTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "OnboardingWalkme-partial-export-test";
        projectTemplate = "/projectTemplates/OnboardingWalkMe/3";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"desktop"})
    public void setupFeatureFlag() throws JSONException {
        setupFeatureFlagInProject(testParams.getProjectId(), ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS);
    }

    @Test(dependsOnMethods = {"setupFeatureFlag"}, groups = {"desktop"})
    public void createKpiLinkToDashboardTab() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets();

        indigoDashboardsPage
            .waitForDashboardLoad()
            .addWidget(new KpiConfiguration.Builder()
                .metric("Sales")
                .dateDimension("Date")
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .drillTo("Sales Forecast")
                .build())
            .saveEditModeWithKpis();
    }

    @Test(dependsOnMethods = {"setupFeatureFlag"}, groups = {"desktop"})
    public void exportDashboardsToAnotherProject() throws JSONException, IOException {
        final String oldPid = testParams.getProjectId();

        final String dashboardUri = IndigoRestUtils.getAnalyticalDashboards(getRestApiClient(), oldPid).get(0);
        final String token = exportPartialProject(dashboardUri, DEFAULT_PROJECT_CHECK_LIMIT);

        final String newPid = RestUtils.createProject(getRestApiClient(), "Copy of " + projectTitle, "", projectTemplate,
                testParams.getAuthorizationToken(), testParams.getDwhDriver(), testParams.getProjectEnvironment());

        try {
            testParams.setProjectId(newPid);
            importPartialProject(token, DEFAULT_PROJECT_CHECK_LIMIT);

            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "Imported dashboard", getClass());

            indigoDashboardsPage.getLastKpi()
                .clickKpiValue();
            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "Dashboard tab: 'Sales Forecast' is selected", getClass());
            assertTrue(dashboardsPage.getTabs().isTabSelected(2));
        } finally {
            testParams.setProjectId(oldPid);

            if (nonNull(newPid)) {
                RestUtils.deleteProject(getRestApiClient(), newPid);
            }
        }
    }
}
