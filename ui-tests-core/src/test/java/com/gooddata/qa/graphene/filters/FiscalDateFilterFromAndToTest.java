package com.gooddata.qa.graphene.filters;

import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.DayTimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.DayTimeFilterPanel.DayAgo;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import static org.testng.Assert.assertEquals;

public class FiscalDateFilterFromAndToTest extends AbstractDashboardWidgetTest {
    private static final String FILTER_NAME = "DATE (SALARY)";
    private static final String BROADCAST_FISCAL_SALARY_FILTER_NAME = "Date (Salary)";

    private static final String FY2016 = "FY2016";
    private static final String FY2016_FROM_VALUE = "12/28/2015";
    private static final String FY2016_FROM_DESC = "Mon, Dec 28 2015";
    private static final String FY2016_TO_VALUE = "12/25/2016";
    private static final String FY2016_TO_DESC = "Sun, Dec 25 2016";

    private static final String FY2014 = "FY2014";
    private static final String FY2015 = "FY2015";
    private static final String FY2014_FY2015_WIDGET_VALUE = "FY2014 - FY2015";
    private static final String FY2014_FY2015_FROM_VALUE = "12/30/2013";
    private static final String FY2014_FY2015_FROM_DESC = "Mon, Dec 30 2013";
    private static final String FY2014_FY2015_TO_VALUE = "12/27/2015";
    private static final String FY2014_FY2015_TO_DESC = "Sun, Dec 27 2015";

    private static final String FROM_WEB_CONTENT = "https://urlecho.appspot.com/echo?" +
            "status=200&body=%DATE_FILTER_VALUE(salary.broadcast_year,FROM)%";
    private static final String EXPECTED_FROM_WEB_CONTENT_BODY = "2015-12-28";
    private static final String TO_WEB_CONTENT = "https://urlecho.appspot.com/echo?" +
            "status=200&body=%DATE_FILTER_VALUE(salary.broadcast_year,TO)%";
    private static final String EXPECTED_TO_WEB_CONTENT_BODY = "2016-12-25";
    private static final String FROM_HEADLINE_TEXT = "%DATE_FILTER_VALUE(salary.broadcast_year,FROM)%";
    private static final String TO_HEADLINE_TEXT = "%DATE_FILTER_VALUE(salary.broadcast_year,TO)%";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private int currentFiscalYear;

    @Override
    public void prepareProject() throws Throwable {
        setupMaql(LdmModel.loadFromFile("/fiscal-date/maql.txt"));

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);

        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.DASHBOARD_ACCESS_CONTROL, true);
        currentFiscalYear = getCurrentFiscalYear();
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @DataProvider
    public Object[][] getUserRoles() {
        return new Object[][] {
            {UserRoles.EDITOR},
            {UserRoles.VIEWER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testSelectingSingleTimeLineValue(UserRoles role) throws JSONException {
        final int yearFrom2016 = currentFiscalYear - 2016;
        final String timeline = String.format("%s ago", yearFrom2016);
        final String dashboard = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboard).publishDashboard(true);
        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME, DateGranularity.YEAR, timeline);
        dashboardsPage.saveDashboard();

        try {
            logoutAndLoginAs(false, role);
            initDashboardsPage().selectDashboard(dashboard);
            if (role == UserRoles.EDITOR) {
                dashboardsPage.editDashboard();
            }

            FilterWidget filterWidget = getFilter(FILTER_NAME);
            TimeFilterPanel timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
            timeFilterPanel.selectTimeLine(FY2016);

            assertEquals(timeFilterPanel.getFromValue(), FY2016_FROM_VALUE);
            assertEquals(timeFilterPanel.getFromDateDescription(), FY2016_FROM_DESC);
            assertEquals(timeFilterPanel.getToValue(), FY2016_TO_VALUE);
            assertEquals(timeFilterPanel.getToDateDescription(), FY2016_TO_DESC);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testSelectingRangeOfTimeLineValue(UserRoles role) throws JSONException {
        final int yearFrom2016 = currentFiscalYear - 2016;
        final String timeline = String.format("%s ago", yearFrom2016);
        final String dashboard = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboard).publishDashboard(true);
        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME, DateGranularity.YEAR, timeline);
        dashboardsPage.saveDashboard();

        try {
            logoutAndLoginAs(false, role);
            initDashboardsPage().selectDashboard(dashboard);
            if (role == UserRoles.EDITOR) {
                dashboardsPage.editDashboard();
            }

            FilterWidget filterWidget = getFilter(FILTER_NAME);
            TimeFilterPanel timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
            timeFilterPanel.selectRange(FY2014, FY2015);
            assertEquals(timeFilterPanel.getFromValue(), FY2014_FY2015_FROM_VALUE);
            assertEquals(timeFilterPanel.getFromDateDescription(), FY2014_FY2015_FROM_DESC);
            assertEquals(timeFilterPanel.getToValue(), FY2014_FY2015_TO_VALUE);
            assertEquals(timeFilterPanel.getToDateDescription(), FY2014_FY2015_TO_DESC);

            timeFilterPanel.submit();
            assertEquals(filterWidget.getCurrentValue(), FY2014_FY2015_WIDGET_VALUE);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testSelectPredefinedDayFilter(UserRoles role) throws JSONException {
        final String dashboard = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboard).publishDashboard(true);
        dashboardsPage.editDashboard()
                .addDayTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME,
                        DayAgo.LAST_60_DAYS).saveDashboard();

        try {
            logoutAndLoginAs(false, role);
            initDashboardsPage().selectDashboard(dashboard);
            if (role == UserRoles.EDITOR) {
                dashboardsPage.editDashboard();
            }

            FilterWidget filterWidget = getFilter(FILTER_NAME);
            DayTimeFilterPanel dayTimeFilterPanel = filterWidget.openPanel().getDayTimeFilterPanel();
            dayTimeFilterPanel.selectLast(DayAgo.LAST_30_DAYS);
            String toDay = dayTimeFilterPanel.getToDateValue();
            LocalDate toDate = LocalDate.parse(toDay, FORMATTER);
            String fromDay = dayTimeFilterPanel.getFromDateValue();
            LocalDate fromDate = LocalDate.parse(fromDay, FORMATTER);

            assertEquals(dayTimeFilterPanel.getSelectedDay(), DayAgo.LAST_30_DAYS);
            assertEquals(ChronoUnit.DAYS.between(fromDate, toDate) + 1, DayAgo.LAST_30_DAYS.value());
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getUserRoles")
    public void testSelectCustomDayFilter(UserRoles role) throws JSONException {
        final String dashboard = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboard).publishDashboard(true);
        dashboardsPage.editDashboard()
                .addDayTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME,
                        DayAgo.LAST_60_DAYS).saveDashboard();

        try {
            logoutAndLoginAs(false, role);
            initDashboardsPage().selectDashboard(dashboard);
            if (role == UserRoles.EDITOR) {
                dashboardsPage.editDashboard();
            }

            FilterWidget filterWidget = getFilter(FILTER_NAME);
            DayTimeFilterPanel dayTimeFilterPanel = filterWidget.openPanel().getDayTimeFilterPanel();
            dayTimeFilterPanel.setCustomDays(45);

            String toDay = dayTimeFilterPanel.getToDateValue();
            LocalDate toDate = LocalDate.parse(toDay, FORMATTER);
            String fromDay = dayTimeFilterPanel.getFromDateValue();
            LocalDate fromDate = LocalDate.parse(fromDay, FORMATTER);

            assertEquals(ChronoUnit.DAYS.between(fromDate, toDate) + 1, 45);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAddWidgetToDashboard() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        final int yearFrom2016 = currentFiscalYear - 2016;
        final String timeline = String.format("%s ago", yearFrom2016);
        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME, DateGranularity.YEAR, timeline);
        dashboardsPage.saveDashboard();

        EmbeddedWidget fromWidget = dashboardsPage.addWebContentToDashboard(FROM_WEB_CONTENT)
                .saveDashboard().getLastEmbeddedWidget();
        assertEquals(fromWidget.getContentBodyAsText(), EXPECTED_FROM_WEB_CONTENT_BODY);
        EmbeddedWidget toWidget = dashboardsPage.addWebContentToDashboard(TO_WEB_CONTENT)
                .saveDashboard().getLastEmbeddedWidget();
        assertEquals(toWidget.getContentBodyAsText(), EXPECTED_TO_WEB_CONTENT_BODY);

        DashboardEditBar editBar = dashboardsPage.editDashboard();
        editBar.addTextToDashboard(TextObject.HEADLINE, "", FROM_HEADLINE_TEXT);
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), EXPECTED_FROM_WEB_CONTENT_BODY);
        editBar.addTextToDashboard(TextObject.HEADLINE, "", TO_HEADLINE_TEXT);
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), EXPECTED_TO_WEB_CONTENT_BODY);
    }

    private int getCurrentFiscalYear() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(BROADCAST_FISCAL_SALARY_FILTER_NAME, DateGranularity.YEAR, "this");
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        String thisYear = timeFilter.getCurrentValue();
        return Integer.parseInt(thisYear.replaceAll("FY", ""));
    }
}
