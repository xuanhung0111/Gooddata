package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KpiDashboardCreationTest extends AbstractDashboardTest {

    private static final String DEFAULT_TITLE = "Untitled";
    private static final String INSIGHT_ACTIVITIES = "Insight Activites";
    private static final String INSIGHT_LOST = "Insight Lost";
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activites";
    private static final String DASHBOARD_LOST = "Dashboard Lost";
    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws IOException, JSONException {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createNumOfActivitiesKpi()), DASHBOARD_ACTIVITIES);
        indigoRestRequest.createAnalyticalDashboard(singletonList(createLostKpi()), DASHBOARD_LOST);
        createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES,
                ReportType.COLUMN_CHART).setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(
                getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES))))));
        createInsightWidget(new InsightMDConfiguration(INSIGHT_LOST,
                ReportType.COLUMN_CHART).setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(
                getMdService().getObj(getProject(), Metric.class, title(METRIC_LOST))))));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAndSaveNewKpiDashboard() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets().addDashboard();
        takeScreenshot(browser, "New-Dashboard", getClass());

        assertEquals(indigoDashboardsPage.getDashboardTitle(), DEFAULT_TITLE);
        assertTrue(indigoDashboardsPage.isOnEditMode(), "Dashboard cannot be edited");

        indigoDashboardsPage.addInsight(INSIGHT_ACTIVITIES).saveEditModeWithWidgets();
        try {
            assertEquals(indigoDashboardsPage.getSelectedKpiDashboard(), DEFAULT_TITLE);
            assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on view mode");
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(DEFAULT_TITLE)));
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(indigoRestRequest.getAnalyticalDashboardUri(DEFAULT_TITLE));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveEditedKpiDashboard() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(DASHBOARD_ACTIVITIES)
                .switchToEditMode()
                .addInsight(INSIGHT_ACTIVITIES)
                .saveEditModeWithWidgets();

        takeScreenshot(browser, "Saving-Edited-Dashboard", getClass());
        assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on view mode");
        assertEquals(indigoDashboardsPage.getSelectedKpiDashboard(), DASHBOARD_ACTIVITIES);
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(INSIGHT_ACTIVITIES));
        assertEquals(indigoDashboardsPage.getKpiTitles(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), DASHBOARD_ACTIVITIES);
        assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(DASHBOARD_ACTIVITIES)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void discardChangeNewKpiDashboard() {
        initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(DASHBOARD_LOST)
                .addDashboard()
                .addInsight(INSIGHT_LOST)
                .cancelEditModeWithChanges();

        takeScreenshot(browser, "Discard-change-new-Kpi-dashboard", getClass());
        assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on View Mode");
        assertEquals(indigoDashboardsPage.getDashboardTitles(), asList(DASHBOARD_ACTIVITIES, DASHBOARD_LOST));
        assertEquals(indigoDashboardsPage.getSelectedKpiDashboard(), DASHBOARD_LOST);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void discardChangeExistingKpiDashboard() {
        initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(DASHBOARD_LOST)
                .switchToEditMode()
                .addInsight(INSIGHT_LOST)
                .cancelEditModeWithChanges();

        takeScreenshot(browser, "Discard-change-existing-Kpi-dashboard", getClass());
        assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on View Mode");
        assertEquals(indigoDashboardsPage.getKpiTitles(), asList(METRIC_LOST));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryCancelingEditModeByClickCloseButton() {
        initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(DASHBOARD_LOST)
                .switchToEditMode()
                .addInsight(INSIGHT_LOST)
                .tryCancelingEditModeByClickCloseButton();

        takeScreenshot(browser, "Try-canceling-edit-mode-by-click-close-button", getClass());
        assertTrue(indigoDashboardsPage.isOnEditMode(), "Should be on View Mode");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryCreateNewKpiDashboardWithViewer() throws IOException, JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "Try-create-a-new-dashboard-with-Viewer", getClass());
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(DASHBOARD_ACTIVITIES)));

            assertEquals(indigoDashboardsPage.getDashboardTitles(), asList(DASHBOARD_ACTIVITIES, DASHBOARD_LOST));
            assertFalse(indigoDashboardsPage.isAddDashboardVisible(), "View cannot add dashboard");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
