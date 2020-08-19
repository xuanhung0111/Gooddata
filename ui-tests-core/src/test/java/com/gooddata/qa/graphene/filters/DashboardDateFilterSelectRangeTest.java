package com.gooddata.qa.graphene.filters;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardDateFilterSelectRangeTest extends AbstractDashboardWidgetTest {
    private static final String FILTER_NAME = "DATE (SALARY)";
    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;
    private static final int FIFTEEN_YEARS_AGO = 15;
    private static final int THREE_YEARS_AGO = 3;
    private static final String SUM_OF_PAYMENTS = "Sum Of Payments";
    private static final String DATE_TYPES_REPORT = "Date Types Report";
    private static final String YEAR_SALARY = "Year (Salary)";
    private static final String WEEK_SALARY = "Week (Sun-Sat) (Salary)";
    private static final String MONTH_SALARY = "Month (Salary)";

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

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportXLSXUnMergedCellWithDateTypes() throws IOException {
        createPaymentMetric();

        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(DATE_TYPES_REPORT)
            .withWhats(SUM_OF_PAYMENTS)
            .withHows(new HowItem(YEAR_SALARY, HowItem.Position.LEFT), new HowItem(WEEK_SALARY, HowItem.Position.LEFT),
                new HowItem(MONTH_SALARY, HowItem.Position.LEFT)), DATE_TYPES_REPORT);

        ReportPage.getInstance(browser).saveAsReport();
        DashboardsPage dashboardsPage = initDashboardsPage()
            .addReportToDashboard(DATE_TYPES_REPORT);
        
        dashboardsPage.getReport(DATE_TYPES_REPORT, TableReport.class)
            .resizeFromTopLeftButton(-300, 0)
            .resizeFromBottomRightButton(200, 400);

        dashboardsPage.saveDashboard();

        final File exportFile = new File(testParams.getDownloadFolder(),
            dashboardsPage.exportDashboardToXLSXWithUnMergedCell());

        List<String> xlsxContent = XlsxUtils.excelFileToRead(exportFile.getPath(), 0).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        takeScreenshot(browser, DATE_TYPES_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "FY2006"), 12);
        assertEquals(Collections.frequency(xlsxContent, "FY2007"), 12);
    }

    private void createPaymentMetric() {
        createMetric(SUM_OF_PAYMENTS,
            format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))),
            "#,##0.00");
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
