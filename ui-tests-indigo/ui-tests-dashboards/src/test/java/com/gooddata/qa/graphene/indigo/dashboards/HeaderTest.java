package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteDashboardsUsingCascade;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class HeaderTest extends AbstractDashboardTest {

    @Override
    protected void setDashboardFeatureFlags() {
        // do nothing because feature flag has been set inside tests
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiLinkMissingIfFeatureFlagOff() throws JSONException {
        try {
            ProjectRestUtils.setFeatureFlagInProjectAndCheckResult(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);

            // ensure that feature flag is applied
            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            ProjectRestUtils.setFeatureFlagInProjectAndCheckResult(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiLinkExistingAlthoughFeatureFlagOff() throws JSONException, IOException {
        try {
            ProjectRestUtils.setFeatureFlagInProjectAndCheckResult(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);

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
                ProjectRestUtils.setFeatureFlagInProjectAndCheckResult(getGoodDataClient(), 
                        testParams.getProjectId(), ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);

                // ensure that feature flag is applied
                initDashboardsPage();
                browser.navigate().refresh();
                waitForDashboardPageLoaded(browser);

                takeScreenshot(browser, "KPI-header-display-when-FF-is-off-and-there-is-saved-KPI", getClass());
                assertTrue(ApplicationHeaderBar.isKpisLinkVisible(browser));
            } finally {
                deleteDashboardsUsingCascade(getRestApiClient(), testParams.getProjectId());
            }

            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off-and-KPI-is-removed", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            ProjectRestUtils.setFeatureFlagInProjectAndCheckResult(getGoodDataClient(), testParams.getProjectId(),
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
