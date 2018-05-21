package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.filter.TimeRange;
import com.gooddata.qa.graphene.entity.filter.TimeRange.Time;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest.FormatLabel;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DashboardFiscalCalendarTest extends AbstractDashboardWidgetTest {

    private static final String METRIC_NAME = "Sum Of Payments";
    private static final String FISCAL_CALENDAR_REPORT = "Fiscal Calendar Report";
    private static final String THIS = "this";
    private static final String SUM_OF_PAYMENTS_REPORT = "Sum Of Payments (Years)";
    private static final String DATE_SALARY = "Date (Salary)";

    private String weekAtFiscalDate;
    private String monthAtFiscalDate;
    private String quarterAtFiscalDate;
    private int yearAtFiscalDate;
    private DashboardRestRequest dashboardRequest;

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

        RestClient restClient = new RestClient(getProfile(ADMIN));
        new RolapRestRequest(restClient, testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);

        dashboardRequest = new DashboardRestRequest(restClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initData() {
        Metric metric = createMetric(METRIC_NAME,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))), "#,##0.00");
        Metric date = createMetric("#Date (Salary)",
                format("SELECT COUNT([%s])", getAttributeByTitle(DATE_SALARY).getUri()), "#,##0");

        createReport(
                GridReportDefinitionContent.create(
                        SUM_OF_PAYMENTS_REPORT,
                        singletonList(METRIC_GROUP),
                        Arrays.asList(
                                new AttributeInGrid(getAttributeByTitle("Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Quarter/Year (Salary)"))),
                        singletonList(new MetricElement(metric))));
        createReport(
                GridReportDefinitionContent.create(
                        FISCAL_CALENDAR_REPORT,
                        singletonList(METRIC_GROUP),
                        Arrays.asList(
                                new AttributeInGrid(getAttributeByTitle(DATE_SALARY)),
                                new AttributeInGrid(getAttributeByTitle("Week (Sun-Sat)/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Month/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Quarter/Year (Salary)")),
                                new AttributeInGrid(getAttributeByTitle("Year (Salary)"))),
                        singletonList(new MetricElement(date)),
                        singletonList(new Filter(format("[%s] = This", getAttributeByTitle(DATE_SALARY).getUri())))));

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
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(dateGranularity, THIS);
        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel();
        takeScreenshot(browser, "setting-fiscal-" + dateGranularity + "-filter", getClass());
        assertEquals(timeFilterPanel.getPreviewValue(), expectedPreviewValue);
    }

    @Test(dependsOnMethods = {"initData"})
    public void applyFiscalDateFilter() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .addReportToDashboard(SUM_OF_PAYMENTS_REPORT, DashboardWidgetDirection.LEFT)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS, DashboardWidgetDirection.RIGHT);
        FilterWidget filterWidget = getFilter(DATE_SALARY).changeTimeFilterValueByClickInTimeLine("FY2007");

        takeScreenshot(browser, "fiscal-year-filter", getClass());
        assertEquals(filterWidget.getCurrentValue(), "FY2007");
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).getAttributeValues(),
                asList("FY2007", "1/2007", "2/2007", "3/2007", "4/2007"));
    }

    @Test(dependsOnMethods = {"initData"})
    public void exportDashboardHasFiscalDateFilter() throws IOException {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .addReportToDashboard(SUM_OF_PAYMENTS_REPORT, DashboardWidgetDirection.LEFT)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS, DashboardWidgetDirection.RIGHT);
        getFilter(DATE_SALARY).changeTimeFilterValueByClickInTimeLine("FY2007");
        takeScreenshot(browser, "exported-dashboard", getClass());
        String exportedDashboardName = dashboardsPage.saveDashboard().printDashboardTab(0);
        try {
            checkRedBar(browser);
            if (testParams.isClientDemoEnvironment())
                return;
            verifyDashboardExport(exportedDashboardName, "First Tab", 45000L);
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName + "." + ExportFormat.PDF.getName()));
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void embedDashboardHasFiscalDateFilter() throws IOException {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .addReportToDashboard(SUM_OF_PAYMENTS_REPORT, DashboardWidgetDirection.LEFT)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS, DashboardWidgetDirection.RIGHT);
        String previewUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();
        browser.get(previewUri);
        EmbeddedDashboard embededDashboard =
                Graphene.createPageFragment(EmbeddedDashboard.class, waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));

        embededDashboard.getContent().getFirstFilter().changeTimeFilterValueByClickInTimeLine("FY2007");
        takeScreenshot(browser, "embedded-dashboard", getClass());
        assertEquals(embededDashboard.getContent().getFirstFilter().getCurrentValue(), "FY2007");
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).getAttributeValues(),
                asList("FY2007", "1/2007", "2/2007", "3/2007", "4/2007"));
        String exportedDashboardName = embededDashboard.printDashboardTab(0);
        try {
            checkRedBar(browser);
            if (testParams.isClientDemoEnvironment())
                return;
            verifyDashboardExport(exportedDashboardName, "First Tab", 45000L);
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName + "." + ExportFormat.PDF.getName()));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeTypeFiscalDateFilter() {
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        TimeFilterPanel timeFilterPanel = dashboardsPage.getContent().getFirstFilter().openEditPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getVisibleTimelines(),
                asList("9 ago", "8 ago", "7 ago", "6 ago", "5 ago", "4 ago", "3 ago", "2 ago", "last", "this", "next"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), singletonList(THIS));

        timeFilterPanel.selectDateGranularity(DateGranularity.MONTH);
        assertEquals(timeFilterPanel.getVisibleTimelines(),
                asList("12 ago", "11 ago", "10 ago", "9 ago", "8 ago", "7 ago",
                        "6 ago", "5 ago", "4 ago", "3 ago", "2 ago", "last", "this", "next"));
        assertEquals(timeFilterPanel.getSelectedTimelineItemNames(), singletonList("last"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void selectTimeRangeInFiscalDateFilter() {
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();
        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel()
                .selectRange((yearAtFiscalDate - 2010) + " ago", (yearAtFiscalDate - 2015) + " ago");
        assertEquals(timeFilterPanel.getPreviewValue(), "FY2010 - FY2015");

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), "FY2010 - FY2015");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/28/2009");
        assertEquals(timeFilterPanel.getToValue(), "12/27/2015");

        //View Mode
        dashboardsPage.saveDashboard();
        assertEquals(filterWidget.getCurrentValue(), "FY2010 - FY2015");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/28/2009");
        assertEquals(timeFilterPanel.getToValue(), "12/27/2015");
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dateTypeProvider")
    public void selectTimeInRangeFiscalDateFilterTest(DateGranularity dateGranularity, String expectedValue) {
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(dateGranularity, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getPreviewValue(), expectedValue);

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), expectedValue);

        //View Mode
        dashboardsPage.saveDashboard();
        assertEquals(filterWidget.getCurrentValue(), expectedValue);
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dateTypeProvider")
    public void outOfLeftRangeFiscalDateFilterTest(DateGranularity dateGranularity, String expectedValue) {
        String expected = format("NaN - %s", expectedValue);
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(dateGranularity, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel()
                .setValueFromDateAndToDateByAdvance(TimeRange.from(7000, Time.AGO), TimeRange.to(Time.THIS));
        assertEquals(timeFilterPanel.getPreviewValue(), expected);

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), expected);

        //View Mode
        dashboardsPage.saveDashboard();
        assertEquals(filterWidget.getCurrentValue(), expected);
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dateTypeProvider")
    public void outOfRightRangeFiscalDateFilterTest(DateGranularity dateGranularity, String expectedValue) {
        String expected = format("%s - NaN", expectedValue);
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(dateGranularity, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel()
                .setValueFromDateAndToDateByAdvance(TimeRange.from(Time.THIS), TimeRange.to(2000, Time.IN_THE_FUTURE));
        assertEquals(timeFilterPanel.getPreviewValue(), expected);

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), expected);

        //View Mode
        dashboardsPage.saveDashboard();
        assertEquals(filterWidget.getCurrentValue(), expected);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void outOfRangeFiscalDateFilterTest() {
        //Edit Mode
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard().addTimeFilterToDashboard(DateGranularity.YEAR, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();

        TimeFilterPanel timeFilterPanel = filterWidget.openEditPanel().getTimeFilterPanel()
                .setValueFromDateAndToDateByAdvance(TimeRange.from(150, Time.AGO),
                        TimeRange.to(50, Time.IN_THE_FUTURE));
        assertEquals(timeFilterPanel.getPreviewValue(), "NaN - NaN");

        timeFilterPanel.submit();
        assertEquals(filterWidget.getCurrentValue(), "FY1901 - FY2050");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/31/1900");
        assertEquals(timeFilterPanel.getToValue(), "12/25/2050");

        //View Mode
        dashboardsPage.saveDashboard();
        assertEquals(filterWidget.getCurrentValue(), "FY1901 - FY2050");

        timeFilterPanel = filterWidget.openPanel().getTimeFilterPanel();
        assertEquals(timeFilterPanel.getFromValue(), "12/31/1900");
        assertEquals(timeFilterPanel.getToValue(), "12/25/2050");
    }

    @Test(dependsOnMethods = {"initData"})
    public void drillInForReportWithFiscalDateFilterTest() {
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .addReportToDashboard(SUM_OF_PAYMENTS_REPORT, DashboardWidgetDirection.LEFT)
                .addTimeFilterToDashboard(DateGranularity.YEAR, THIS, DashboardWidgetDirection.RIGHT)
                .getContent().getFirstFilter().openEditPanel().getTimeFilterPanel()
                .setValueFromDateAndToDateByAdvance(TimeRange.from(yearAtFiscalDate - 2006, Time.AGO),
                        TimeRange.to(yearAtFiscalDate - 2007, Time.AGO)).submit();
        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
        tableReport.addDrilling(Pair.of(singletonList("Year (Salary)"), "Salary"));
        tableReport.drillOnFirstValue(TableReport.CellType.ATTRIBUTE_VALUE);

        DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
        assertEquals(drillDialog.getBreadcrumbsString(),
                StringUtils.join(Arrays.asList(SUM_OF_PAYMENTS_REPORT, "FY2006"), ">>"));
    }

    @Test(dependsOnMethods = {"initData"})
    public void setDefaultFormatFiscalDate() throws IOException {
        String attribute = "Month/Year (Salary)";
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .editDashboard()
                .addTimeFilterToDashboard(DateGranularity.MONTH, THIS);

        FilterWidget filterWidget = dashboardsPage.getContent().getFirstFilter();
        dashboardsPage.saveDashboard();
        dashboardRequest.setDefaultFormatDateFilter(attribute, FormatLabel.LONG_LABEL);
        try {
            //refresh browser to apply new rest api
            BrowserUtils.refreshCurrentPage(browser);
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "set-default-format-fiscal-date", getClass());
            verifyFormatDate(filterWidget.getCurrentValue(), monthAtFiscalDate);

            dashboardsPage.editDashboard();
            DashboardEditWidgetToolbarPanel.removeWidget(filterWidget.getRoot(), browser);
            dashboardsPage.getDashboardEditBar().addTimeFilterToDashboard(DateGranularity.MONTH, THIS);
            verifyFormatDate(filterWidget.getCurrentValue(), monthAtFiscalDate);

            dashboardsPage.addNewTab("New Tabs");
            dashboardsPage.getDashboardEditBar().addTimeFilterToDashboard(DateGranularity.MONTH, THIS);
            verifyFormatDate(filterWidget.getCurrentValue(), monthAtFiscalDate);
        } finally {
            dashboardRequest.setDefaultFormatDateFilter(attribute, FormatLabel.SHORT_LABEL);
        }
    }

    private void verifyFormatDate(String longLabel, String shortLabel) {
        // With May (special case) cannot detect short or long label
        if (shortLabel.contains("May")) {
            assertEquals(longLabel, shortLabel);
            return;
        }
        assertTrue(longLabel.length() > shortLabel.length(), "Month should be formatted to long");
        assertTrue(longLabel.contains(shortLabel.substring(4, 8)), "Year is changed");
        assertTrue(longLabel.contains(shortLabel.substring(0, 3)), "Month is changed");
    }
}
