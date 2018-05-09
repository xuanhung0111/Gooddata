package com.gooddata.qa.graphene.filters;

import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static org.testng.Assert.assertEquals;

import java.net.URL;

public class DashboardDateFilterSelectRangeTest extends AbstractDashboardWidgetTest {
    private static final String FILTER_NAME = "DATE (SALARY)";
    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;
    private static final int FIFTEEN_YEARS_AGO = 15;
    private static final int THREE_YEARS_AGO = 3;

    @Override
    public void initProperties() {
        projectTitle = "select date range test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        URL maqlResource = getClass().getResource("/fiscal-date/maql.txt");
        postMAQL(IOUtils.toString(maqlResource), STATUS_POLLING_CHECK_ITERATIONS);

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);

        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectDateInInitialRange() {
        final int thisYear = addTimeFilterToDashboard();
        final int yearOfThreeYearAgo = thisYear - THREE_YEARS_AGO;

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();
        TimeFilterPanel timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();

        timeFilterPanel.selectRange("FY" + yearOfThreeYearAgo, "FY" + thisYear).submit();

        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertEquals(timeFilter.getCurrentValue(), String.format("FY%d - FY%d",
                (thisYear - THREE_YEARS_AGO), thisYear));

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        timeFilterPanel.selectRange("FY" + thisYear, "FY" + yearOfThreeYearAgo).submit();
        assertEquals(timeFilter.getCurrentValue(), String.format("FY%d - FY%d", yearOfThreeYearAgo, thisYear));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectYearRangeNotInInitialRangeInViewMode() {
        final int thisYear = addTimeFilterToDashboard();
        final int yearOfFifteenYearAgo = thisYear - FIFTEEN_YEARS_AGO;

        TimeFilterPanel timeFilterPanel = dashboardsPage
                .getContent().getFirstFilter().openPanel().getTimeFilterPanel();

        timeFilterPanel.selectRange("FY" + yearOfFifteenYearAgo, "FY" + thisYear).submit();

        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertEquals(timeFilter.getCurrentValue(), String.format("FY%d - FY%d", yearOfFifteenYearAgo, thisYear));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectYearRangeNotInInitialRangeInEditMode() {
        final int thisYear = addTimeFilterToDashboard();
        final int yearOfFifteenYearAgo = thisYear - FIFTEEN_YEARS_AGO;

        dashboardsPage.editDashboard();
        TimeFilterPanel timeFilterPanel = dashboardsPage
                .getContent().getFirstFilter().openEditPanel().getTimeFilterPanel();
        timeFilterPanel.selectRange(String.format("%d ago", FIFTEEN_YEARS_AGO), "this").submit();

        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertEquals(timeFilter.getCurrentValue(), String.format("FY%d - FY%d", yearOfFifteenYearAgo, thisYear));
    }

    private int addTimeFilterToDashboard() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        dashboardsPage.editDashboard()
                .addTimeFilterToDashboard(TimeFilterPanel.DateGranularity.YEAR, "this").saveDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        String thisYear = timeFilter.getCurrentValue();
        return Integer.parseInt(thisYear.replaceAll("FY", ""));
    }
}
