package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.AttributeElement;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_INCOMPUTABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NO_DATA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_SALES_SEASONALITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOO_LARGE;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.MIDDLE;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ExportDashboardXLSXTest extends AbstractDashboardWidgetTest {

    private static final String METRIC_AVAILABLE = "Metric available";
    private static final String REPORT_AMOUNT_BY_F_STAGE_NAME = "Amount by f stage name";
    private static final String SHORT_LIST_ID = "168751";
    private static final String RISK_ASSESSMENT_ID = "166425";
    private static final String CONVICTION_ID = "166442";
    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String NEGOTIATION = "Negotiation";
    private static final String CLOSED_WON = "Closed Won";
    private static final String CLOSED_LOST = "Closed Lost";
    private static final String F_STAGE_NAME = "FStage Name";
    private static final String MUF_NAME = "Muf";

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private static final int FROM = 2010 - CURRENT_YEAR;
    private static final int TO = 2011 - CURRENT_YEAR;

    private static final String YEAR_2012 = String.valueOf(CURRENT_YEAR - 2012);
    private static final String APPLIED_FILTER = String.format("Year (Snapshot) BETWEEN THIS-%s AND THIS-%s", abs(FROM), abs(TO));
    private String dashboardTitle;
    private String promptUri;
    private DashboardRestRequest dashboardRequest;
    private String userUri;

    @Override
    protected void customizeProject() throws Throwable {
        Reports report = getReportCreator();
        report.createAmountByProductReport();
        report.createActivitiesByTypeReport();
        report.createSalesSeasonalityReport();
        report.createEmptyReport();
        report.createIncomputableReport();
        report.createTooLargeReport();
        report.createAmountByStageNameReport();

        promptUri = getVariableCreator().createFilterVariable(F_STAGE_NAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT, CONVICTION));
        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_F_STAGE_NAME,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("[%s]", promptUri)))));

        // *** create metric available ***
        getMdService().createObj(getProject(), new Metric(METRIC_AVAILABLE,
                buildFirstMetricExpression(getMetricByTitle(METRIC_AMOUNT).getUri(),
                        getAttributeByTitle(ATTR_STAGE_NAME).getUri()), "#,##0.00"));
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        userUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(), testParams.getUser());
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasTableReportsToXLSX() throws IOException {
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT),
                Pair.of(REPORT_ACTIVITIES_BY_TYPE, MIDDLE));
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Table_Reports", getClass());
        //Table reports
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(asList(ATTR_PRODUCT, METRIC_AMOUNT),
                asList("CompuSci", "2.722289964E7"), asList("Educationly", "2.294689547E7"),
                asList("Explorer", "3.859619486E7"), asList("Grammar Plus", "8042031.92"),
                asList("PhoenixSoft", "9525857.91"), asList("WonderKid", "1.029157674E7")));
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 1), asList(
                asList(ATTR_ACTIVITY_TYPE, METRIC_NUMBER_OF_ACTIVITIES),
                asList("Email", "33920.0"), asList("In Person Meeting", "35975.0"), asList("Phone Call", "50780.0"),
                asList("Web Meeting", "33596.0")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasChartReportsToXLSX() throws IOException {
        String lineReport = "Line Report";
        String headlineReport = "Headline Report";
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.HEADLINE).saveAsReport(headlineReport);
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.LINE).saveAsReport(lineReport);
        openDashboardHasReports(Pair.of(headlineReport, ItemPosition.LEFT), Pair.of(lineReport, MIDDLE));
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Chart_Reports", getClass());
        //Headline report
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0),
                asList(asList("", METRIC_AMOUNT), asList(METRIC_AMOUNT, "1.1662545654E8")));
        //Line report
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 1), asList(
                asList(ATTR_PRODUCT, "CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid"),
                asList("", METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT),
                asList(METRIC_AMOUNT, "2.722289964E7", "2.294689547E7", "3.859619486E7",
                        "8042031.92", "9525857.91", "1.029157674E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasReportsIncludeTotalsToXLSX() throws IOException {
        String reportHasTotals = "Report has totals";
        TableReport tableReport = initReportsPage().openReport(REPORT_AMOUNT_BY_F_STAGE_NAME).getTableReport().waitForLoaded();
        tableReport.openContextMenuFrom(METRIC_AMOUNT, TableReport.CellType.METRIC_HEADER)
                .aggregateTableData(ContextMenu.AggregationType.MINIMUM, "Of All Rows");
        tableReport.waitForLoaded();
        reportPage.saveAsReport(reportHasTotals);
        openDashboardHasReports(Pair.of(reportHasTotals, ItemPosition.LEFT));
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Reports_Include_Totals", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest, Discovery, Short List, Risk Assessment, Conviction)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"),
                asList(SHORT_LIST, "5612062.6"), asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"),
                asList("Minimum", "2606293.46")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasEmptyReportsToXLSX() throws IOException {
        String headlineReportWithSpecialFormat = "Headline Report with special format";
        dashboardRequest.changeMetricFormat(getMetricByTitle(METRIC_AMOUNT).getUri(), "[=NULL]empty;#,##0.00");
        try {
            initReportsPage().openReport(REPORT_NO_DATA)
                    .selectReportVisualisation(ReportTypes.HEADLINE).saveAsReport(headlineReportWithSpecialFormat);
            openDashboardHasReports(Pair.of(REPORT_NO_DATA, ItemPosition.LEFT),
                    Pair.of(headlineReportWithSpecialFormat, MIDDLE));
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Empty_Reports", getClass());
            //Report no data
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList("Applied filters:", "(SELECT Amount) < 0"),
                    singletonList("Export failed: No data for your filter selection. " +
                            "Try adjusting or removing some of the filters.")));
            //Headline Report with special format
            //BUG-VIZ-1338 Specific format of headline empty report is not taken in exported file when exporting to XLSX
            //assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 1), asList(
            //        asList("Applied filters:, (SELECT Amount) < 0", asList("", "Amount"), asList("Amount"))));
        } finally {
            dashboardRequest.changeMetricFormat(getMetricByTitle(METRIC_AMOUNT).getUri(), "#,##0.00");
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasBigDataReportsToXLSX() throws IOException {
        openDashboardHasReports(Pair.of(REPORT_TOO_LARGE, ItemPosition.LEFT),
                Pair.of(REPORT_SALES_SEASONALITY, MIDDLE));
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Big_Data_Reports", getClass());
        //Too large report
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), singletonList(
                singletonList("Export failed: Sorry, we can't export this report. Contact your administrator.")));
        //Available area too small to display report
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 1), asList(
                asList(ATTR_MONTH_SNAPSHOT, METRIC_NUMBER_OF_WON_OPPS, METRIC_AVG_WON, METRIC_LOST, METRIC_WON),
                asList("Jan", "2647.0", "11964.951627907", "3.337639528E7", "3.08695752E7"),
                asList("Feb", "2804.0", "11940.1780665205", "3.475341911E7", "3.266832719E7"),
                asList("Mar", "2943.0", "12020.8805781958", "3.833287155E7", "3.451194814E7"),
                asList("Apr", "3117.0", "12004.5818626807", "4.151645531E7", "3.654194719E7"),
                asList("May", "3311.0", "11849.9082740489", "4.247057116E7", "3.831075345E7"),
                asList("Jun", "1632.0", "11323.1050722816", "1.957261698E7", "1.801506017E7"),
                asList("Jul", "1772.0", "11402.8459269988", "2.047968E7", "1.968131207E7"),
                asList("Aug", "1944.0", "11505.4718551797", "2.184085214E7", "2.176835275E7"),
                asList("Sep", "2044.0", "11719.0356913022", "2.250077496E7", "2.330916199E7"),
                asList("Oct", "2189.0", "11791.6305347092", "2.382107383E7", "2.51397563E7"),
                asList("Nov", "2346.0", "12229.4960113835", "2.603976942E7", "2.793216889E7"),
                asList("Dec", "2487.0", "12083.3418324391", "2.824990059E7", "2.927793726E7")));
    }

    @Test(dependsOnGroups = "createProject", enabled = false)
    public void exportDashboardHasReportContainsRestrictedDataToXLSX() throws IOException {
        FactRestRequest factRestRequest = new FactRestRequest(getAdminRestClient(), testParams.getProjectId());
        factRestRequest.setFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());
        try {
            openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT));
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Report_Contains_Restricted_Data", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), singletonList(singletonList("Export failed: " +
                    "Your report can't be exported because it contains restricted data. " +
                    "For more detailed information contact your administrator.")));
        } finally {
            factRestRequest.unsetFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasNotComputedReportToXLSX() throws IOException {
        openDashboardHasReports(Pair.of(REPORT_INCOMPUTABLE, ItemPosition.LEFT));
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Not_Computed_Report", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), singletonList(
                singletonList("Export failed: Sorry, we can't export this report. Contact your administrator.")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasAttributeSingleFilterToXLSX() throws IOException {
        FilterItemContent filterContent = createSingleValueFilter(getAttributeByTitle(ATTR_PRODUCT));
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_PRODUCT, filterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Attribute_Single_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Product IN (CompuSci)"),
                asList(ATTR_PRODUCT, METRIC_AMOUNT), asList("CompuSci", "2.722289964E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasAttributeMultipleFilterToXLSX() throws IOException {
        FilterItemContent filterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_PRODUCT));
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_PRODUCT, filterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Attribute_Multiple_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(asList(ATTR_PRODUCT, METRIC_AMOUNT),
                asList("CompuSci", "2.722289964E7"), asList("Educationly", "2.294689547E7"),
                asList("Explorer", "3.859619486E7"), asList("Grammar Plus", "8042031.92"),
                asList("PhoenixSoft", "9525857.91"), asList("WonderKid", "1.029157674E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasFilterAppliedUseAvailableToXLSX() throws IOException {
        FilterItemContent filterContent = createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
        dashboardRequest.addUseAvailableMetricToDashboardFilters(dashboardTitle, METRIC_AVAILABLE);
        initDashboardsPage().selectDashboard(dashboardTitle);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Filter_Applied_Use_Available", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Short List)"),
                asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList("2010", SHORT_LIST, "1347427.16"), asList("2011", SHORT_LIST, "3903521.33"),
                asList("2012", SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasPromptSingleFilterToXLSX() throws IOException {
        FilterItemContent filterContent = createSingleValuesFilterBy(promptUri);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_F_STAGE_NAME, filterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Prompt_Single_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasPromptMultipleFilterToXLSX() throws IOException {
        FilterItemContent filterContent = createMultipleValuesFilterBy(promptUri);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_F_STAGE_NAME, filterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Prompt_Multiple_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest, Discovery, Short List, Risk Assessment, Conviction)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"),
                asList(SHORT_LIST, "5612062.6"), asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasPromptFilterAppliedUseAvailableToXLSX() throws IOException {
        FilterItemContent filterContent = createSingleValuesFilterBy(promptUri);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_F_STAGE_NAME, filterContent);
        dashboardRequest.addUseAvailableMetricToDashboardFilters(dashboardTitle, METRIC_AVAILABLE);
        initDashboardsPage().selectDashboard(dashboardTitle);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Prompt_Filter_Applied_Use_Available", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Short List)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasDateRangeFilterToXLSX() throws IOException {
        FilterItemContent filterContent =
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), FROM, TO);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Date_Range_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", APPLIED_FILTER),
                asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT), asList("2010", INTEREST, "1185127.28"),
                asList(DISCOVERY, "2080448.83"), asList(SHORT_LIST, "1347427.16"), asList(RISK_ASSESSMENT, "1222172.3"),
                asList(CONVICTION, "494341.51"), asList(NEGOTIATION, "647612.26"), asList(CLOSED_WON, "8886381.82"),
                asList(CLOSED_LOST, "1.105885084E7"), asList("2011", INTEREST, "1.642738857E7"),
                asList(DISCOVERY, "3436167.7"), asList(SHORT_LIST, "3903521.33"), asList(RISK_ASSESSMENT, "2021556.99"),
                asList(CONVICTION, "2513393.4"), asList(NEGOTIATION, "348745.87"),
                asList(CLOSED_WON, "2.927793726E7"), asList(CLOSED_LOST, "2.824990059E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasGroupFilterToXLSX() throws IOException {
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent promptFilterContent = createSingleValuesFilterBy(promptUri);
        FilterItemContent dateFilterContent =
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), FROM, TO);
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, attributeFilterContent,
                promptFilterContent, dateFilterContent);
        dashboardsPage.editDashboard()
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, F_STAGE_NAME, "Date dimension (Snapshot)").saveDashboard();
        dashboardsPage.getReport(REPORT_AMOUNT_BY_F_STAGE_NAME, TableReport.class).waitForLoaded();
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Group_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"),
                singletonList(APPLIED_FILTER),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasTypeFiltersToXLSX() throws IOException {
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent promptFilterContent = createSingleValuesFilterBy(promptUri);
        FilterItemContent dateFilterContent =
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), FROM, TO);
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, attributeFilterContent,
                promptFilterContent, dateFilterContent);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Type_Filters", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"),
                singletonList(APPLIED_FILTER),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedOnAttributeFilterToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> INTEREST.equals(e.getTitle())).findFirst().get();
        final String expression = format("[%s] = [%s]",
                getAttributeByTitle(ATTR_STAGE_NAME).getUri(), stageNameValue.getUri());
        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            FilterItemContent filterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
            openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Muf_Applied_On_Attribute_Filter", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT), asList("2010", INTEREST, "1185127.28"),
                    asList("2011", INTEREST, "1.642738857E7"), asList("2012", INTEREST, "1.844726614E7")
            ));
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedOnNegativeAttributeFilterToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> INTEREST.equals(e.getTitle())).findFirst().get();
        final String expression = format("[%s] != [%s]", getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                stageNameValue.getUri());
        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            FilterItemContent filterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
            openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser,
                    "Dashboard_Has_Muf_Applied_On_Negative_Attribute_Filter", getClass());
            assertFalse(XlsxUtils.excelFileToRead(xlsxUrl, 0)
                    .contains(asList("2011", INTEREST, "1.642738857E7")), "Should be applied MUF");
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedOnPromptFilterToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> DISCOVERY.equals(e.getTitle())).findFirst().get();
        final String expression = format("[%s] = [%s]",
                getAttributeByTitle(ATTR_STAGE_NAME).getUri(), stageNameValue.getUri());
        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            FilterItemContent filterContent = createMultipleValuesFilterBy(promptUri);
            openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Muf_Applied_On_Prompt_Filter", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT), asList("2010", DISCOVERY, "2080448.83"),
                    asList("2011", DISCOVERY, "3436167.7"), asList("2012", DISCOVERY, "4249027.88")));
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedOnNegativePromptFilterToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> DISCOVERY.equals(e.getTitle())).findFirst().get();
        final String expression = format("[%s] != [%s]",
                getAttributeByTitle(ATTR_STAGE_NAME).getUri(), stageNameValue.getUri());
        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            FilterItemContent filterContent = createMultipleValuesFilterBy(promptUri);
            openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser,
                    "Dashboard_Has_Muf_Applied_On_Negative_Prompt_Filter", getClass());
            assertFalse(XlsxUtils.excelFileToRead(xlsxUrl, 0)
                    .contains(asList("2011", DISCOVERY, "3436167.7")), "Should be applied MUF");
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedOnDateFilterToXLSX() throws IOException {
        final String expression = format("[%s] >= THIS - " + YEAR_2012,
                getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri());
        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_STAGE_NAME, ItemPosition.LEFT));
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Muf_Applied_On_Date_Filter", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT), asList("2012", INTEREST, "1.844726614E7"),
                    asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"), asList(RISK_ASSESSMENT, "2606293.46"),
                    asList(CONVICTION, "3067466.12"), asList(NEGOTIATION, "1862015.73"),
                    asList(CLOSED_WON, "3.831075345E7"), asList(CLOSED_LOST, "4.247057116E7")));
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedFiltersToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> INTEREST.equals(e.getTitle())).findFirst().get();
        final String expression = format("([%s] >= THIS - %s) AND ([%s] = [%s])",
                getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri(), YEAR_2012,
                getAttributeByTitle(ATTR_STAGE_NAME).getUri(), stageNameValue.getUri());

        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_STAGE_NAME, ItemPosition.LEFT));
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Muf_Applied_Filters", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT),
                    asList("2012", INTEREST, "1.844726614E7")));
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasMufAppliedNegativeFiltersToXLSX() throws IOException {
        AttributeElement stageNameValue = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME))
                .stream().filter(e -> INTEREST.equals(e.getTitle())).findFirst().get();
        final String expression = format("([%s] >= THIS - %s) AND ([%s] != [%s])",
                getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri(), YEAR_2012,
                getAttributeByTitle(ATTR_STAGE_NAME).getUri(), stageNameValue.getUri());

        dashboardRequest.addMufToUser(userUri, dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
        try {
            openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_STAGE_NAME, ItemPosition.LEFT));
            String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Muf_Applied_Negative_Filters", getClass());
            assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                    asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT), asList("2012", DISCOVERY, "4249027.88"),
                    asList(SHORT_LIST, "5612062.6"), asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"),
                    asList(NEGOTIATION, "1862015.73"), asList(CLOSED_WON, "3.831075345E7"),
                    asList(CLOSED_LOST, "4.247057116E7")));
        } finally {
            dashboardRequest.removeAllMufFromUser(userUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasAnotherLabelFilterToXLSX() throws IOException {
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_STAGE_NAME, ItemPosition.LEFT));
        dashboardsPage.editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME, "Order")
                .saveDashboard();
        TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class).waitForLoaded();
        dashboardsPage.getFirstFilter().changeAttributeFilterValues("101");
        tableReport.waitForLoaded();
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Another_Label_Filter", getClass());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"),
                asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList("2010", INTEREST, "1185127.28"), asList("2011", INTEREST, "1.642738857E7"),
                asList("2012", INTEREST, "1.844726614E7")));
    }

    private void openDashboardHasReports(Pair<String, ItemPosition>... reports) throws IOException {
        dashboardTitle = generateDashboardName();
        List<TabItem> tabItems = Stream.of(reports).map(this::createReportItem).collect(Collectors.toList());
        Dashboard dashboard = new Dashboard().setName(dashboardTitle).addTab(new Tab().addItems(tabItems));

        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private ReportItem createReportItem(Pair<String, ItemPosition> report) {
        ReportItem reportItem = createReportItem(getReportByTitle(report.getLeft()).getUri());
        reportItem.setPosition(report.getRight());
        return reportItem;
    }

    private void openDashboardHasReportAndFilter(String titleReport, FilterItemContent filterItemContent)
            throws IOException {
        dashboardTitle = generateDashboardName();
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, singletonList(
                        Pair.of(filterItemContent, RIGHT))))
                .addFilter(filterItemContent);

        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private void openDashboardHasReportAndFilters(String titleReport, FilterItemContent attributeFilterContent,
            FilterItemContent promptFilterContent, FilterItemContent dateFilterContent) throws IOException {
        dashboardTitle = generateDashboardName();
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, asList(
                        Pair.of(attributeFilterContent, MIDDLE),
                        Pair.of(promptFilterContent, TOP_RIGHT),
                        Pair.of(dateFilterContent, RIGHT))))
                .addFilter(attributeFilterContent)
                .addFilter(promptFilterContent)
                .addFilter(dateFilterContent);

        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private Tab initTab(String report, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream()
                .map(this::buildFilterItem)
                .collect(Collectors.toList());
        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));
        return new Tab().addItems(singletonList(reportItem)).addItems(filterItems);
    }

    private FilterItem buildFilterItem(Pair<FilterItemContent, ItemPosition> filterItem) {
        FilterItem filterItemContent = new FilterItem().setContentId(filterItem.getLeft().getId());
        filterItemContent.setPosition(filterItem.getRight());
        return filterItemContent;
    }

    private String buildFirstMetricExpression(String amountUri, String stageNameUri) {
        String expressionTemplate = "SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])";
        return format(expressionTemplate, amountUri, stageNameUri,
                buildAttributeElementUri(stageNameUri, SHORT_LIST_ID),
                buildAttributeElementUri(stageNameUri, RISK_ASSESSMENT_ID),
                buildAttributeElementUri(stageNameUri, CONVICTION_ID));
    }

    private String buildAttributeElementUri(String attributeUri, String elementId) {
        return attributeUri + "/elements?id=" + elementId;
    }
}
