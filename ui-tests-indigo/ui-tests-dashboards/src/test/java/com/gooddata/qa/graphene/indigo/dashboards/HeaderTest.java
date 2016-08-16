package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

public class HeaderTest extends DashboardsTest {

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkKpiLinkMissingIfFeatureFlagOff() throws JSONException {
        try {
            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);
            initDashboardsPage();
            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkKpiLinkExistingAlthoughFeatureFlagOff() throws JSONException {
        try {
            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
            KpiConfiguration kpi = new KpiConfiguration.Builder()
                    .metric(METRIC_AMOUNT)
                    .dataSet(DATE_CREATED)
                    .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                    .drillTo(DASH_TAB_OUTLOOK)
                    .build();
            setupKpisFromSplashScreen(Arrays.asList(kpi));

            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);
            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "KPI-header-display-when-FF-is-off-and-there-is-saved-KPI", getClass());
            assertTrue(ApplicationHeaderBar.isKpisLinkVisible(browser));

            teardownKpiWithDashboardDelete();
            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "KPI-header-not-display-when-FF-is-off-and-KPI-is-removed", getClass());
            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkLogout() throws JSONException {
        try {
            initIndigoDashboardsPage()
                    .logout();

            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }
}
