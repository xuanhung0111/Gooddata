package com.gooddata.qa.graphene.dashboards;

import com.gooddata.sdk.model.md.AttributeElement;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.DateRange;
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
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class ExportDashboardXLSXAdvancedTest extends AbstractDashboardWidgetTest {

    private static final String REPORT_APPLIES_POSITIVE_FILTER = "Report applies positive filter";
    private static final String REPORT_APPLIES_NEGATIVE_FILTER = "Report applies negative filter";
    private static final String F_STAGE_NAME = "FStage Name";
    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String NEGOTIATION = "Negotiation";
    private static final String CLOSED_WON = "Closed Won";
    private static final String CLOSED_LOST = "Closed Lost";
    private final static int currentYear = DateRange.now().getYear();

    private String dashboardTitle;
    private DashboardRestRequest dashboardRequest;
    private String positivePromptUri;
    private String negativePromptUri;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        positivePromptUri = getVariableCreator().createFilterVariable(F_STAGE_NAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                format("[%s] IN (%s)", getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                        getAttributeElements(ATTR_STAGE_NAME, asList(SHORT_LIST, CLOSED_LOST)).stream()
                                .map(element -> "[" + element.getUri() + "]")
                                .collect(Collectors.joining(", "))));
        negativePromptUri = getVariableCreator().createFilterVariable(F_STAGE_NAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                format("NOT ([%s] IN (%s))", getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                        getAttributeElements(ATTR_STAGE_NAME, asList(SHORT_LIST, CLOSED_LOST)).stream()
                                .map(element -> "[" + element.getUri() + "]")
                                .collect(Collectors.joining(", "))));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasReportsApplyFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String firstReport = createReportAppliesPositiveFilter(ATTR_STAGE_NAME, asList(INTEREST, DISCOVERY));
        String secondReport = createReportAppliesNegativeFilter(ATTR_STAGE_NAME, asList(INTEREST, DISCOVERY,
                SHORT_LIST, RISK_ASSESSMENT, CONVICTION, NEGOTIATION));
        String expression = "[%s]" + format(" BETWEEN This-%d AND This-%d",
                currentYear - 2010, currentYear - 2011);
        String thirdReport = createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression);
        openDashboardHasReports(Pair.of(firstReport, ItemPosition.TOP_RIGHT),
                Pair.of(secondReport, ItemPosition.RIGHT), Pair.of(thirdReport, ItemPosition.TOP_LEFT));
        dashboardsPage.getReport(firstReport, TableReport.class).waitForLoaded();
        Screenshots.takeScreenshot(browser, "Dashboard has reports apply filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Year (Snapshot)" + format(" BETWEEN This-%d AND This-%d",
                        currentYear - 2010, currentYear - 2011)),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7"),
                asList(DISCOVERY, "3436167.7"), asList(SHORT_LIST, "3903521.33"),
                asList(RISK_ASSESSMENT, "2021556.99"), asList(CONVICTION, "2513393.4"),
                asList(NEGOTIATION, "348745.87"), asList(CLOSED_WON, "2.927793726E7"),
                asList(CLOSED_LOST, "2.824990059E7")));
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 1), asList(
                asList("Applied filters:", "Stage Name IN (Interest, Discovery)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88")));
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 2), asList(
                asList("Applied filters:", "NOT (Stage Name IN " +
                        "(Interest, Discovery, Short List, Risk Assessment, Conviction, Negotiation))"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(CLOSED_WON, "3.831075345E7"),
                asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByPartialAttributeValueFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPositiveFilter(ATTR_STAGE_NAME, asList(INTEREST, SHORT_LIST)),
                createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME), INTEREST));
        Screenshots.takeScreenshot(browser, "Override by attribute single filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByAllAttributeValuesFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPositiveFilter(ATTR_STAGE_NAME, asList(INTEREST, SHORT_LIST)),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)));
        Screenshots.takeScreenshot(browser, "Override by all attribute values filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.844726614E7"),
                asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"),
                asList(NEGOTIATION, "1862015.73"), asList(CLOSED_WON, "3.831075345E7"),
                asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByOtherAttributeValueFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPositiveFilter(ATTR_STAGE_NAME, singletonList(CLOSED_WON)),
                createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME), INTEREST));
        Screenshots.takeScreenshot(browser, "Override by other attribute value filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportByAttributeNegativeFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesNegativeFilter(ATTR_STAGE_NAME, singletonList(INTEREST)),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME), INTEREST, SHORT_LIST));
        Screenshots.takeScreenshot(browser, "Override report by attribute negative filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "NOT (Stage Name IN (Interest))"),
                singletonList("Stage Name IN (Interest,Short List)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByDateAttributeFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPositiveFilter(ATTR_YEAR_SNAPSHOT, singletonList("2010")),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)));
        Screenshots.takeScreenshot(browser, "Override by date attribute filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.844726614E7"),
                asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"),
                asList(NEGOTIATION, "1862015.73"), asList(CLOSED_WON, "3.831075345E7"),
                asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByDateAttributeValueFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPositiveFilter(ATTR_YEAR_SNAPSHOT, singletonList("2010")),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), "2012"));
        Screenshots.takeScreenshot(browser, "Override by date attribute value filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Year (Snapshot) IN (2012)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"), asList(NEGOTIATION, "1862015.73"),
                asList(CLOSED_WON, "3.831075345E7"), asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByDateFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String expression = "[%s]" + format(" BETWEEN This-%d AND This-%d",
                currentYear - 2011, currentYear - 2012);
        openDashboardHasReportAndFilter(createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression),
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2010 - currentYear, 2011 - currentYear));
        Screenshots.takeScreenshot(browser, "Override report filter by date filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Year (Snapshot)" + format(" BETWEEN THIS-%d AND THIS-%d",
                        currentYear - 2010, currentYear - 2011)),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7"),
                asList(DISCOVERY, "3436167.7"), asList(SHORT_LIST, "3903521.33"),
                asList(RISK_ASSESSMENT, "2021556.99"), asList(CONVICTION, "2513393.4"),
                asList(NEGOTIATION, "348745.87"), asList(CLOSED_WON, "2.927793726E7"),
                asList(CLOSED_LOST, "2.824990059E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportDateFilterByDateFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String expression = format("[%s] <> This", getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri());
        openDashboardHasReportAndFilter(createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression),
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2011 - currentYear, 2011 - currentYear));
        Screenshots.takeScreenshot(browser, "Override report filter by date filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        sleepTightInSeconds(3);
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Year (Snapshot) = THIS-" + (currentYear - 2011)),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7"), asList(DISCOVERY, "3436167.7"),
                asList(SHORT_LIST, "3903521.33"), asList(RISK_ASSESSMENT, "2021556.99"), asList(CONVICTION, "2513393.4"),
                asList(NEGOTIATION, "348745.87"), asList(CLOSED_WON, "2.927793726E7"), asList(CLOSED_LOST, "2.824990059E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportNegativeFilterByDateFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String expression = "NOT ([%s]" + format(" BETWEEN This-%d AND This-%d)",
                currentYear - 2011, currentYear - 2012);
        openDashboardHasReportAndFilter(createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression),
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2010 - currentYear, 2011 - currentYear));
        Screenshots.takeScreenshot(browser, "Override report negative filter by date filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "NOT (Year (Snapshot)" + format(" BETWEEN This-%d AND This-%d)",
                        currentYear - 2011, currentYear - 2012)),
                singletonList("Year (Snapshot)" + format(" BETWEEN THIS-%d AND THIS-%d",
                        currentYear - 2010, currentYear - 2011)), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1185127.28"), asList(DISCOVERY, "2080448.83"), asList(SHORT_LIST, "1347427.16"),
                asList(RISK_ASSESSMENT, "1222172.3"), asList(CONVICTION, "494341.51"),
                asList(NEGOTIATION, "647612.26"), asList(CLOSED_WON, "8886381.82"), asList(CLOSED_LOST, "1.105885084E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByPromptFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilterBy(positivePromptUri));
        Screenshots.takeScreenshot(browser, "Override by prompt filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Short List, Closed Lost)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(SHORT_LIST, "5612062.6"),
                asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByPromptValueFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilterBy(positivePromptUri));
        Screenshots.takeScreenshot(browser, "Override by prompt value filter", getClass());
        dashboardsPage.getFirstFilter().changeAttributeFilterValues(SHORT_LIST);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Short List)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportFilterByPromptNegativeFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(negativePromptUri),
                createMultipleValuesFilterBy(negativePromptUri));
        Screenshots.takeScreenshot(browser, "Override by prompt negative filter", getClass());
        dashboardsPage.getFirstFilter().changeAttributeFilterValues(INTEREST);
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void notOverrideReportFilterByDifferentTypeFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME), INTEREST, SHORT_LIST));
        Screenshots.takeScreenshot(browser, "Not override by different type filter", getClass());
        String xlsxUrl = testParams.getExportFilePath(dashboardsPage.exportDashboardToXLSX());
        assertEquals(XlsxUtils.excelFileToRead(xlsxUrl, 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest,Short List)"),
                singletonList("Stage Name IN (Short List, Closed Lost)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(SHORT_LIST, "5612062.6")));
    }

    private void openDashboardHasReportAndFilter(String titleReport, FilterItemContent filterItemContent)
            throws IOException {
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, singletonList(
                        Pair.of(filterItemContent, RIGHT))))
                .addFilter(filterItemContent);

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

    private void openDashboardHasReports(Pair<String, TabItem.ItemPosition>... reports) throws IOException {
        List<TabItem> tabItems = Stream.of(reports).map(this::createReportItem).collect(Collectors.toList());
        Dashboard dashboard = new Dashboard().setName(dashboardTitle).addTab(new Tab().addItems(tabItems));

        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private ReportItem createReportItem(Pair<String, TabItem.ItemPosition> report) {
        ReportItem reportItem = createReportItem(getReportByTitle(report.getLeft()).getUri());
        reportItem.setPosition(report.getRight());
        return reportItem;
    }

    private String createReportAppliesPromptFilter(String promptUri) {
        String reportTitle = REPORT_APPLIES_POSITIVE_FILTER + " " + generateHashString();
        createReportAppliesFilter(reportTitle,
                new Filter(format("[%s]", promptUri)));
        return reportTitle;
    }

    private String createReportAppliesDateRangeFilter(String attribute, String expression) {
        String reportTitle = "Report applies date range filter " + generateHashString();
        createReportAppliesFilter(reportTitle,
                new Filter(format(expression, getAttributeByTitle(attribute).getUri())));
        return reportTitle;
    }

    private String createReportAppliesPositiveFilter(String attribute, List<String> filteredValues) {
        String reportTitle = REPORT_APPLIES_POSITIVE_FILTER + " " + generateHashString();
        createReportAppliesFilter(reportTitle,
                new Filter(format("[%s] IN (%s)", getAttributeByTitle(attribute).getUri(),
                        getAttributeElements(attribute, filteredValues).stream()
                                .map(element -> "[" + element.getUri() + "]")
                                .collect(Collectors.joining(", ")))));
        return reportTitle;
    }

    private String createReportAppliesNegativeFilter(String attribute, List<String> filteredValues) {
        String reportTitle = REPORT_APPLIES_NEGATIVE_FILTER + " " + generateHashString();
        createReportAppliesFilter(reportTitle,
                new Filter(format("NOT ([%s] IN (%s))", getAttributeByTitle(attribute).getUri(),
                        getAttributeElements(attribute, filteredValues).stream()
                                .map(element -> "[" + element.getUri() + "]")
                                .collect(Collectors.joining(", ")))));
        return reportTitle;
    }

    private void createReportAppliesFilter(String reportTitle, Filter filter) {
        createReport(GridReportDefinitionContent.create(reportTitle,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(filter)));
    }

    private List<AttributeElement> getAttributeElements(String attribute, List<String> filteredValues) {
        return getMdService().getAttributeElements(getAttributeByTitle(attribute)).stream()
                .filter(element -> filteredValues.contains(element.getTitle()))
                .collect(Collectors.toList());
    }
}
