package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.google.common.collect.Ordering;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KpiDashboardsTest extends AbstractDashboardTest {

    private static final String ALL_TIME = "All time";

    @Override
    protected void customizeProject() {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(createNumOfActivitiesKpi()), METRIC_NUMBER_OF_ACTIVITIES);
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createLostKpi()), METRIC_LOST);
    }
	
    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }
	
    @DataProvider
    public Object[][] getUserRoles() {
        return new Object[][] { { UserRoles.ADMIN }, { UserRoles.EDITOR } };
    }
	
    @Test(dependsOnGroups = { "createProject" }, groups = { "desktop" }, dataProvider = "getUserRoles")
    public void selectKpiDashboardsTest(UserRoles role) throws JSONException, IOException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(indigoDashboardsPage.isNavigationBarVisible(), "Navigation bar is not display");

            indigoDashboardsPage.selectKpiDashboard(METRIC_LOST);
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "Select-KPI-dashboard-test-with-" + role.getName(), getClass());
			
            assertFalse(indigoDashboardsPage.isOnEditMode(), "KpiDashboard doesn't open in view mode");
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(METRIC_LOST)));
            assertEquals(indigoDashboardsPage.getDashboardTitle(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
	
    @Test(dependsOnGroups = { "createProject" }, groups = { "mobile" }, dataProvider = "getUserRoles")
    public void selectKpiDashboardsOnMobileTest(UserRoles role) throws JSONException, IOException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(METRIC_LOST);
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "Select-KPI-dashboard-on-mobile-test-with-" + role.getName(), getClass());
	
            assertEquals(indigoDashboardsPage.getDashboardTitle(), METRIC_LOST);
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(METRIC_LOST)));
            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
    	    logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
	
    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "getUserRoles")
    public void kpiDashboardsSortByAlphabetTest(UserRoles role) throws JSONException {
        String urlAmountKpiDashboard;
        logoutAndLoginAs(true, role);
        urlAmountKpiDashboard = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()), METRIC_AMOUNT);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertFalse(indigoDashboardsPage.isOnEditMode(), "should be on View Mode");
            takeScreenshot(browser, "KPI-dashboard-sort-by-alphabet-test-with-" + role.getName(), getClass());
            assertTrue(Ordering.natural().isOrdered(indigoDashboardsPage.getDashboardTitles()),
                    "New kpi dashboards should be sorted by alphabet in list");
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), urlAmountKpiDashboard);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "desktop", "mobile" }, dataProvider = "getUserRoles")
    public void openKpiDashboardByUrlTest(UserRoles role) throws IOException, JSONException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertThat(browser.getCurrentUrl(),
                    containsString(getKpiDashboardIdentifierByTitle(METRIC_NUMBER_OF_ACTIVITIES)));

            openUrl(PAGE_INDIGO_DASHBOARDS + "#/project/" + testParams.getProjectId() + "/dashboard/"
                    + getKpiDashboardIdentifierByTitle(METRIC_LOST));
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "open-KPI-dashboard-by-Url-test-with-" + role.getName(), getClass());

            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
        	logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
