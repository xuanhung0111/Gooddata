package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.filter.TimeRange;
import com.gooddata.qa.graphene.entity.filter.TimeRange.Time;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.postEtlPullIntegration;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class DashboardFiscalCalendarTest extends AbstractDashboardWidgetTest {

    private static final String METRIC_NAME = "Sum Of Payments";
    private static final String FISCAL_CALENDAR_REPORT = "Fiscal Calendar Report";
    private static final String THIS = "this";

    private Metric metric;
    private String weekAtFiscalDate;
    private String monthAtFiscalDate;
    private String quarterAtFiscalDate;
    private int yearAtFiscalDate;

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

        postEtlPullIntegration(getRestApiClient(), testParams.getProjectId(),
                webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));

        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initData() {
        metric = createMetric(METRIC_NAME,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))), "#,##0.00");

        Metric date = createMetric("#Date (Salary)",
                format("SELECT COUNT([%s])", getAttributeByTitle("Date (Salary)").getUri()), "#,##0");

        createReport(
                GridReportDefinitionContent.create(
                        FISCAL_CALENDAR_REPORT,
                        singletonList(METRIC_GROUP),
                        Arrays.asList(
                                new AttributeInGrid(getAttributeByTitle("Date (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Week (Sun-Sat)/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Month/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Quarter/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Year (Salary)"))),
                        singletonList(new MetricElement(date)),
                        singletonList(new Filter(format("[%s] = This", getAttributeByTitle("Date (Salary)").getUri())))));

        List<String> attributeValues = initReportsPage().openReport(FISCAL_CALENDAR_REPORT).getTableReport().getAttributeValues();

        weekAtFiscalDate = attributeValues.get(1);
        monthAtFiscalDate = attributeValues.get(2);
        quarterAtFiscalDate = attributeValues.get(3);
        yearAtFiscalDate = Integer.parseInt(attributeValues.get(4).substring(2));
    }

    @DataProvider
    public Object[][] dateTypeProvider() {
        return new Object[][]{
                {DateGranularity.YEAR, "FY" + yearAtFiscalDate},
                {DateGranularity.QUARTER, quarterAtFiscalDate},
                {DateGranularity.MONTH, monthAtFiscalDate},
                {DateGranularity.WEEK, weekAtFiscalDate},
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dateTypeProvider")
    public void checkTimeRangePreviewShowCorrectly(DateGranularity dateGranularity, String expectedPreviewValue) {
        initDashboardsPage()
                .editDashboard()
                .addTimeFilterToDashboard(dateGranularity, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel();
        takeScreenshot(browser, "setting-fiscal-" + dateGranularity + "-filter", getClass());
        assertEquals(timeFilterPanel.getPreviewValue(), expectedPreviewValue);
    }

    @Test(dependsOnMethods = {"initData"})
    public void applyFiscalDateFilter() {
        String reportName = "Sum Of Payments (Years)";
        createReport(
                GridReportDefinitionContent.create(
                        reportName,
                        singletonList(METRIC_GROUP),
                        Arrays.asList(
                                new AttributeInGrid(getAttributeByTitle("Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Quarter/Year (Salary)"))),
                        singletonList(new MetricElement(metric))));

        initDashboardsPage()
                .editDashboard()
                .addReportToDashboard(reportName)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        TableReport tableReport = dashboardsPage.getContent().getReport(reportName, TableReport.class);
        moveElementToPlace(tableReport.getRoot(), DashboardWidgetDirection.LEFT);
        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();
        moveElementToPlace(filterWidget.getRoot(), DashboardWidgetDirection.RIGHT);

        filterWidget.openEditPanel().getTimeFilterPanel().submit();

        filterWidget.changeTimeFilterValueByClickInTimeLine("FY2007");
        tableReport.waitForLoaded();
        takeScreenshot(browser, "fiscal-year-filter", getClass());
        assertEquals(filterWidget.getCurrentValue(), "FY2007");
        assertEquals(tableReport.getAttributeValues(), asList("FY2007", "1/2007", "2/2007", "3/2007", "4/2007"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeTypeFiscalDateFilter() {
        //Edit Mode
        initDashboardsPage().editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        TimeFilterPanel timeFilterPanel = dashboardsPage.getContent().getFirstFilter().openEditPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getVisibleTimelines(),
                asList("9 ago", "8 ago", "7 ago", "6 ago", "5 ago", "4 ago", "3 ago", "2 ago", "last", "this", "next"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), asList("this"));

        timeFilterPanel.selectDateGranularity(DateGranularity.MONTH);
        assertEquals(timeFilterPanel.getVisibleTimelines(),
                asList("12 ago", "11 ago", "10 ago", "9 ago", "8 ago", "7 ago",
                        "6 ago", "5 ago", "4 ago", "3 ago", "2 ago", "last", "this", "next"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), asList("last"));
    }

    @Test(dependsOnMethods = {"initData"})
    public void selectTimeRangeInFiscalDateFilter() {
        //Edit Mode
        initDashboardsPage().editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();
        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel();
        timeFilterPanel.selectDateGranularity(DateGranularity.YEAR)
                .selectRange((yearAtFiscalDate - 2010) + " ago", (yearAtFiscalDate - 2015) + " ago");
        assertEquals(timeFilterPanel.getPreviewValue(), "FY2010 - FY2015");

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), "FY2010 - FY2015");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/28/2009");
        assertEquals(timeFilterPanel.getToValue(), "12/27/2015");

        //View Mode
        dashboardsPage.saveDashboard();
        try {
            assertEquals(filterWidget.getCurrentValue(), "FY2010 - FY2015");

            timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
            assertEquals(timeFilterPanel.getFromValue(), "12/28/2009");
            assertEquals(timeFilterPanel.getToValue(), "12/27/2015");
        } finally {
            DashboardsRestUtils.deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void outOfRangeFiscalDateFilterTest() {
        //Edit Mode
        initDashboardsPage().editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel();
        timeFilterPanel.selectDateGranularity(DateGranularity.YEAR);
        timeFilterPanel.setValueFromDateAndToDateByAdvance(TimeRange.from(150, Time.YEARS_AGO),
                TimeRange.to(50, Time.YEARS_IN_THE_FUTURE));
        assertEquals(timeFilterPanel.getPreviewValue(), "NaN - NaN");

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), "FY1901 - FY2050");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/31/1900");
        assertEquals(timeFilterPanel.getToValue(), "12/25/2050");

        //View Mode
        dashboardsPage.saveDashboard();
        try {
            assertEquals(filterWidget.getCurrentValue(), "FY1901 - FY2050");

            timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
            assertEquals(timeFilterPanel.getFromValue(), "12/31/1900");
            assertEquals(timeFilterPanel.getToValue(), "12/25/2050");
        } finally {
            DashboardsRestUtils.deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    private void moveElementToPlace(WebElement element, DashboardWidgetDirection direction) {
        element.click();
        direction.moveElementToRightPlace(element);
    }
}
