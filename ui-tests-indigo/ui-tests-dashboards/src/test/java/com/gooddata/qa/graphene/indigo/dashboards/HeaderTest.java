package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class HeaderTest extends AbstractDashboardTest {
    private ProjectRestRequest projectRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiLinkMissingIfFeatureFlagOff() throws JSONException {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);

            // ensure that feature flag is applied
            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiLinkExistingAlthoughFeatureFlagOff() throws JSONException, IOException {
        try {
            // load old dashboards first to avoid redirect to projects.html
            initDashboardsPage();
            initIndigoDashboardsPage().getSplashScreen()
                .startEditingWidgets()
                .addKpi(
                    new KpiConfiguration.Builder()
                        .metric(METRIC_AMOUNT)
                        .dataSet(DATE_DATASET_CREATED)
                        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                        .build())
                .saveEditModeWithWidgets();

            // need another try finally block because not deleting kpi affects to other tests.
            try {
                projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                        ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);

                // ensure that feature flag is applied
                initDashboardsPage();
                browser.navigate().refresh();
                waitForDashboardPageLoaded(browser);

                takeScreenshot(browser, "KPI-header-display-when-FF-is-off-and-there-is-saved-KPI", getClass());
                assertTrue(ApplicationHeaderBar.isKpisLinkVisible(browser));
            } finally {
                new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                        .deleteDashboardsUsingCascade();
            }

            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off-and-KPI-is-removed", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkLogout() throws JSONException {
        try {
            // load old dashboards first to avoid redirect to projects.html
            initDashboardsPage();
            initIndigoDashboardsPage()
                    .logout();

            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }
}
