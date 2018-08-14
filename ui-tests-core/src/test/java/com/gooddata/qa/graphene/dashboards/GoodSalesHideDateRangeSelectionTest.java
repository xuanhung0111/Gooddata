package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CLOSED;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.SelectionConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;

public class GoodSalesHideDateRangeSelectionTest extends GoodSalesAbstractTest {

    private final static String TEST_DASHBOARD = "Test-Dashboard";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Hide-Date-Range-Selection-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.DASHBOARD_ACCESS_CONTROL, true);
        initDashboardsPage().addNewDashboard(TEST_DASHBOARD).publishDashboard(true);

        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(DATE_DIMENSION_ACTIVITY, TimeFilterPanel.DateGranularity.YEAR, "this")
                .saveDashboard();
    }

    @Test(dependsOnGroups = "createProject", groups = "need-fresh-env")
    public void testHideDateRangeOptionDefaultState() {
        SelectionConfigPanel panel = initDashboardsPage().selectDashboard(TEST_DASHBOARD)
                .editDashboard().getDashboardEditFilter()
                .openWidgetConfigPanel(DATE_DIMENSION_ACTIVITY)
                .getTab(WidgetConfigPanel.Tab.SELECTION, SelectionConfigPanel.class);

        takeScreenshot(browser, "testHideDateRangeOptionDefaultState", getClass());
        assertTrue(panel.isHideFromToOptionVisible(), "Hide date range selection does not exist");
        assertFalse(panel.isHideFromToOptionSelected(), "The default state is not unchecked");
    }

    @Test(dependsOnGroups = "createProject", groups = "need-fresh-env")
    public void cancelDashboardApplyingHideDateRange() {
        DashboardEditBar editBar = initDashboardsPage().selectDashboard(TEST_DASHBOARD).editDashboard();
        hideDateRangeSelection(DATE_DIMENSION_ACTIVITY, true);

        editBar.cancelDashboard();

        assertTrue(dashboardsPage.getContent().getFirstFilter().openPanel().getTimeFilterPanel().isDateRangeVisible(),
                "Canceling dashboard is not applied on hide date range selection");

    }

    @Test(dependsOnGroups = "createProject", groups = "need-fresh-env")
    public void cancelCheckedHideDateRangeSelection() {
        initDashboardsPage().selectDashboard(TEST_DASHBOARD).editDashboard();
        WidgetConfigPanel configPanel = openFilterConfigPanel(DATE_DIMENSION_ACTIVITY);
        configPanel.getTab(WidgetConfigPanel.Tab.SELECTION, SelectionConfigPanel.class).setHideFromToOption(true);
        configPanel.discardConfiguration();

        assertTrue(dashboardsPage.getContent().getFirstFilter().openPanel().getTimeFilterPanel().isDateRangeVisible(),
                "Canceling filter configuration is not applied on hide date range selection");
    }

    @Test(dependsOnGroups = "createProject", groups = "need-fresh-env")
    public void applyHideDateRange() {
        initDashboardsPage().selectDashboard(TEST_DASHBOARD).editDashboard();
        hideDateRangeSelection(DATE_DIMENSION_ACTIVITY, true);

        TimeFilterPanel timeFilterPanel = dashboardsPage
                .getContent().getFirstFilter().openPanel().getTimeFilterPanel();
        takeScreenshot(browser, "applyHideDateRange", getClass());
        assertFalse(timeFilterPanel.isDateRangeVisible(),
                "Date Range Selection is displayed");
    }

    @Test(dependsOnGroups = "createProject", groups = "need-fresh-env")
    public void hideDateRangeSelectionWhenHavingMultipleDateFilters() {
        DashboardEditBar editBar = initDashboardsPage().selectDashboard(TEST_DASHBOARD).editDashboard()
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, TimeFilterPanel.DateGranularity.YEAR, "this");
        FilterWidget closedFilter = dashboardsPage.getContent().getFilterWidgetByName(DATE_DIMENSION_CLOSED);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(closedFilter.getRoot());
        hideDateRangeSelection(DATE_DIMENSION_CLOSED, true);

        TimeFilterPanel closedFilterPanel = dashboardsPage
                .getContent().getFilterWidgetByName(DATE_DIMENSION_CLOSED).openPanel().getTimeFilterPanel();
        takeScreenshot(browser, "hideDateRangeSelectionWhenHavingMultipleDateFilters-"
                + DATE_DIMENSION_CLOSED, getClass());

        assertFalse(closedFilterPanel.isDateRangeVisible(), "Date Range Selection is displayed");
        closedFilter.closePanel();

        editBar.getDashboardEditFilter().focusOnFilter(DATE_DIMENSION_ACTIVITY);
        TimeFilterPanel activityFilterPanel = dashboardsPage
                .getContent().getFilterWidgetByName(DATE_DIMENSION_ACTIVITY).openPanel().getTimeFilterPanel();
        takeScreenshot(browser, "hideDateRangeSelectionWhenHavingMultipleDateFilters-"
                + DATE_DIMENSION_ACTIVITY, getClass());
        assertTrue(activityFilterPanel.isDateRangeVisible(),"Hide date range selection affects to other date filter");
    }

    @Test(dependsOnGroups = {"need-fresh-env"})
    public void saveDashboardApplyingHideDateRange() {
        DashboardEditBar editBar = initDashboardsPage().selectDashboard(TEST_DASHBOARD).editDashboard();
        hideDateRangeSelection(DATE_DIMENSION_ACTIVITY, true);
        editBar.saveDashboard();

        assertFalse(dashboardsPage.getContent().getFirstFilter().openPanel().getTimeFilterPanel()
                        .isDateRangeVisible(), "Date Range Selection is displayed");
    }

    @Test(dependsOnMethods = {"saveDashboardApplyingHideDateRange"})
    public void openFilterByViewerAfterApplyingHideDateRange() throws JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            assertFalse(
                    initDashboardsPage().selectDashboard(TEST_DASHBOARD)
                            .getFilterWidgetByName(DATE_DIMENSION_ACTIVITY)
                            .openPanel()
                            .getTimeFilterPanel()
                            .isDateRangeVisible(),
                    "Date range selection is not applied when logging in with viewer role");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"openFilterByViewerAfterApplyingHideDateRange"})
    public void openFilterByViewerAfterRecheckingHideDateRange() throws JSONException {
        DashboardEditBar editBar = initDashboardsPage().editDashboard();

        hideDateRangeSelection(DATE_DIMENSION_ACTIVITY, false);
        editBar.saveDashboard();

        logoutAndLoginAs(true, UserRoles.VIEWER);
        try{
            assertTrue(initDashboardsPage().selectDashboard(TEST_DASHBOARD)
                            .getFilterWidgetByName(DATE_DIMENSION_ACTIVITY)
                            .openPanel().getTimeFilterPanel()
                            .isDateRangeVisible(),
                    "Turning date range selection on again is not applied when logging in with viewer role");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private WidgetConfigPanel openFilterConfigPanel(String filterName) {
        return dashboardsPage.getDashboardEditBar()
                .getDashboardEditFilter()
                .focusOnFilter(filterName)
                .openWidgetConfigPanel(filterName);
    }

    private void hideDateRangeSelection(String filterName, boolean hideDate) {
        WidgetConfigPanel configPanel = openFilterConfigPanel(filterName);
        configPanel.getTab(WidgetConfigPanel.Tab.SELECTION, SelectionConfigPanel.class).setHideFromToOption(hideDate);
        configPanel.saveConfiguration();
    }
}
