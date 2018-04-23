package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class KpiDashboardsParamsTest extends AbstractDashboardTest {
    private static final String INSIGHT_ACTIVITIES = "Insight Activites";
    private static final String INSIGHT_LOST = "Insight Lost";
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activites";
    private static final String DASHBOARD_LOST = "Dashboard Lost";

    @Override
    protected void customizeProject() throws Throwable {
        createAnalyticalDashboard(getRestApiClient(),
                testParams.getProjectId(), singletonList(createNumOfActivitiesKpi()), DASHBOARD_ACTIVITIES);
        createAnalyticalDashboard(getRestApiClient(),
                testParams.getProjectId(), singletonList(createLostKpi()), DASHBOARD_LOST);
        createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES,
                ReportType.COLUMN_CHART).setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(
                getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES))))));
        createInsightWidget(new InsightMDConfiguration(INSIGHT_LOST,
                ReportType.COLUMN_CHART).setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(
                getMdService().getObj(getProject(), Metric.class, title(METRIC_LOST))))));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
        createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
    }

    @DataProvider(name = "viewPermissionProviderEmbeddedMode")
    public Object[][] getViewPermissionProviderEmbeddedMode() {
        return new Object[][]{
                {UserRoles.EDITOR, EmbeddedType.IFRAME},
                {UserRoles.EDITOR, EmbeddedType.URL},
                {UserRoles.VIEWER, EmbeddedType.IFRAME},
                {UserRoles.VIEWER, EmbeddedType.URL},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderEmbeddedMode")
    public void testDefaultBehaviorEmbeddedMode(UserRoles role, EmbeddedType type) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad();
            assertFalse(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should not present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "viewPermissionProviderNonEmbeddedMode")
    public Object[][] getViewPermissionProviderNonEmbeddedMode() {
        return new Object[][]{
                {UserRoles.EDITOR},
                {UserRoles.VIEWER},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderNonEmbeddedMode")
    public void testDefaultBehaviorNonEmbeddedMode(UserRoles role) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage()
                    .waitForDashboardLoad();
            assertTrue(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should be present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "viewPermissionProviderEmbeddedModeWithHidingParams")
    public Object[][] getViewPermissionProviderEmbeddedModeWithHidingParams() {
        return new Object[][]{
                {UserRoles.EDITOR, EmbeddedType.IFRAME, "showNavigation=false"},
                {UserRoles.EDITOR, EmbeddedType.IFRAME, "showNavigation=null"},
                {UserRoles.EDITOR, EmbeddedType.IFRAME, "showNavigation=0"},
                {UserRoles.EDITOR, EmbeddedType.URL, "showNavigation=false"},
                {UserRoles.EDITOR, EmbeddedType.URL, "showNavigation=null"},
                {UserRoles.EDITOR, EmbeddedType.URL, "showNavigation=0"},
                {UserRoles.VIEWER, EmbeddedType.IFRAME, "showNavigation=false"},
                {UserRoles.VIEWER, EmbeddedType.IFRAME, "showNavigation=null"},
                {UserRoles.VIEWER, EmbeddedType.IFRAME, "showNavigation=0"},
                {UserRoles.VIEWER, EmbeddedType.URL, "showNavigation=false"},
                {UserRoles.VIEWER, EmbeddedType.URL, "showNavigation=null"},
                {UserRoles.VIEWER, EmbeddedType.URL, "showNavigation=0"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME, "showNavigation=false"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME, "showNavigation=null"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME, "showNavigation=0"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL, "showNavigation=false"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL, "showNavigation=null"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL, "showNavigation=0"},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderEmbeddedModeWithHidingParams")
    public void testHidingNavigationEmbeddedModeWithParams(UserRoles role, EmbeddedType type, String params) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type, params)
                    .waitForDashboardLoad();
            assertFalse(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should not present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "viewPermissionProviderNonEmbeddedModeWithHidingParams")
    public Object[][] getViewPermissionProviderNonEmbeddedModeWithHidingParams() {
        return new Object[][]{
                {UserRoles.EDITOR, "showNavigation=false"},
                {UserRoles.EDITOR, "showNavigation=null"},
                {UserRoles.EDITOR, "showNavigation=0"},
                {UserRoles.VIEWER, "showNavigation=false"},
                {UserRoles.VIEWER, "showNavigation=null"},
                {UserRoles.VIEWER, "showNavigation=0"},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderNonEmbeddedModeWithHidingParams")
    public void testHidingNavigationNonEmbeddedModeWithParams(UserRoles role, String params) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(params)
                    .waitForDashboardLoad();
            assertFalse(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should not present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "viewPermissionProviderEmbeddedModeWithShowingParams")
    public Object[][] getViewPermissionProviderEmbeddedModeWithShowingParams() {
        return new Object[][]{
                {UserRoles.EDITOR, EmbeddedType.IFRAME, "showNavigation=true"},
                {UserRoles.EDITOR, EmbeddedType.IFRAME, "showNavigation=1"},
                {UserRoles.EDITOR, EmbeddedType.URL, "showNavigation=true"},
                {UserRoles.EDITOR, EmbeddedType.URL, "showNavigation=1"},
                {UserRoles.VIEWER, EmbeddedType.IFRAME, "showNavigation=true"},
                {UserRoles.VIEWER, EmbeddedType.IFRAME, "showNavigation=1"},
                {UserRoles.VIEWER, EmbeddedType.URL, "showNavigation=true"},
                {UserRoles.VIEWER, EmbeddedType.URL, "showNavigation=1"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME, "showNavigation=true"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME, "showNavigation=1"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL, "showNavigation=true"},
                {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL, "showNavigation=1"},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderEmbeddedModeWithShowingParams")
    public void testShowingNavigationEmbeddedModeWithParams(UserRoles role, EmbeddedType type, String params) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type, params)
                    .waitForDashboardLoad();
            assertTrue(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should not present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "viewPermissionProviderNonEmbeddedModeWithShowingParams")
    public Object[][] getViewPermissionProviderNonEmbeddedModeWithShowingParams() {
        return new Object[][]{
                {UserRoles.EDITOR, "showNavigation=true"},
                {UserRoles.EDITOR, "showNavigation=1"},
                {UserRoles.VIEWER, "showNavigation=true"},
                {UserRoles.VIEWER, "showNavigation=1"},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderNonEmbeddedModeWithShowingParams")
    public void testShowingNavigationNonEmbeddedModeWithParams(UserRoles role, String params) {
        logoutAndLoginAs(false, role);
        try {
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(params)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();
            assertTrue(indigoDashboardsPage.isNavigationBarPresent(),
                    "Navigation bar should not present by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
