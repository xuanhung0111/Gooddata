package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static java.util.Objects.nonNull;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertTrue;

public class PartialExportDashboardsTest extends AbstractDashboardTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "OnboardingWalkme-partial-export-test";
        projectTemplate = "/projectTemplates/OnboardingWalkMe/3";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void createKpiLinkToDashboardTab() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .waitForDashboardLoad()
            .addKpi(new KpiConfiguration.Builder()
                .metric("Sales")
                .dataSet("Date")
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .drillTo("Sales Forecast")
                .build())
            .saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void exportDashboardsToAnotherProject() throws JSONException, IOException {
        final String oldPid = testParams.getProjectId();

        final String dashboardUri = IndigoRestUtils.getAnalyticalDashboards(getRestApiClient(), oldPid).get(0);
        final String token = exportPartialProject(dashboardUri, DEFAULT_PROJECT_CHECK_LIMIT);

        final String newPid = ProjectRestUtils.createProject(getGoodDataClient(), "Copy of " + projectTitle, projectTemplate,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());

        try {
            testParams.setProjectId(newPid);
            importPartialProject(token, DEFAULT_PROJECT_CHECK_LIMIT);

            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "Imported dashboard", getClass());

            IndigoDashboardsPage.getInstance(browser).getLastWidget(Kpi.class).clickKpiValue();
            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "Dashboard tab: 'Sales Forecast' is selected", getClass());
            assertTrue(dashboardsPage.getTabs().isTabSelected(2));
        } finally {
            testParams.setProjectId(oldPid);

            if (nonNull(newPid)) {
                ProjectRestUtils.deleteProject(getGoodDataClient(), newPid);
            }
        }
    }
}
