package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardContent;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.net.URL;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DashboardFiscalDateFilterConfigurationTest extends AbstractDashboardWidgetTest {

    private static final int MIN_YEAR = 1901;

    private static final String THIS = "this";
    private static final String REPORT_NAME = "Sum Of Payments";
    private static final String METRIC_NAME = "Sum Of Payments";

    @Override
    public void initProperties() {
        // use empty project
        projectTitle = "Fiscal-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        URL maqlResource = getClass().getResource("/fiscal-date/maql.txt");
        setupMaql(IOUtils.toString(maqlResource));

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);
        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);

        createMetric(METRIC_NAME,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))),
                "#,##0.00");

        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_NAME)
                .withHows("Year (Salary)"),
                REPORT_NAME);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMovingOnTimelines() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard()
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        TimeFilterPanel timeFilterPanel = dashboardsPage.getFirstFilter().openEditPanel()
                .getTimeFilterPanel().moveRightOnTimelines();
        assertEquals(timeFilterPanel.getVisibleTimelines(), asList("8 ago", "7 ago", "6 ago", "5 ago", "4 ago",
                "3 ago", "2 ago", "last", "this", "next", "2 ahead"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), asList(THIS));

        timeFilterPanel.moveLeftOnTimelines();
        assertEquals(timeFilterPanel.getVisibleTimelines(), asList("9 ago", "8 ago", "7 ago", "6 ago", "5 ago", "4 ago",
                "3 ago", "2 ago", "last", "this", "next"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), asList(THIS));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void computeReportCorrectlyWithFiscalDateFilter() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard()
                .addReportToDashboard(REPORT_NAME)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS)
                .addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_NAME);

        DashboardContent dashboardContent = dashboardsPage.getContent();
        TableReport tableReport = dashboardContent.getLatestReport(TableReport.class);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(tableReport.getRoot());
        FilterWidget timeFilter = getFilter("DATE (SALARY)");
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(timeFilter.getRoot());

        timeFilter.changeTimeFilterValueByClickInTimeLine("FY2006");
        dashboardContent.waitForLastKeyMetricUpdated();
        assertEquals(timeFilter.getCurrentValue(), "FY2006");
        assertEquals(tableReport.getAttributeValues(), asList("FY2006"),
                "Report data is not rendered with date filter correctly");
        assertEquals(dashboardContent.getLastKeyMetricValue(), "2,677,188.00");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelSavingFiscalDateFilter() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard()
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS)
                .saveDashboard();

        dashboardsPage.editDashboard();

        String valueFirstFilter = dashboardsPage.getFirstFilter().openEditPanel().getTimeFilterPanel().getPreviewValue();
        dashboardsPage
                .addNewTab("TARGET")
                .addAttributeFilterToDashboard(AddDashboardFilterPanel.DashAttributeFilterTypes.ATTRIBUTE, "Year (Salary)")
                .cancelDashboard();

        assertEquals(dashboardsPage.getFirstFilter().getCurrentValue(), valueFirstFilter);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void limitedFiscalTimelineTest() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard()
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS)
                .saveDashboard();

        assertFalse(dashboardsPage.getFirstFilter().openPanel().getTimeFilterPanel()
                .selectTimeLine("FY1901").canMoveLeftOnTimeline(), MIN_YEAR + " is not limited of fiscal year");
    }
}
