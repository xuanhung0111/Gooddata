package com.gooddata.qa.graphene.dashboards;

import com.gooddata.sdk.model.md.AttributeElement;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class ExportEmbeddedDashboardXLSXTest extends AbstractEmbeddedModeTest {

    private static final String REPORT_APPLIES_POSITIVE_FILTER = "Report applies positive filter";
    private static final String REPORT_APPLIES_NEGATIVE_FILTER = "Report applies negative filter";
    private static final String REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE  = "Report amount by stage name without date";
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
    private EmbeddedDashboard embeddedDashboard;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
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
    public void exportEmbeddedDashboardHasReportsApplyFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String firstReport = createReportAppliesPositiveFilter(ATTR_STAGE_NAME, asList(INTEREST, DISCOVERY));
        String secondReport = createReportAppliesNegativeFilter(ATTR_STAGE_NAME, asList(INTEREST, DISCOVERY,
                SHORT_LIST, RISK_ASSESSMENT, CONVICTION, NEGOTIATION));
        String expression = "[%s]" + format(" BETWEEN This-%d AND This-%d",
                currentYear - 2010, currentYear - 2011);
        String thirdReport = createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression);
        String fourthReport = createReportAppliesPromptFilter(positivePromptUri);
        openDashboardHasReports(Pair.of(firstReport, ItemPosition.TOP_RIGHT), Pair.of(fourthReport, ItemPosition.MIDDLE),
                Pair.of(secondReport, ItemPosition.RIGHT), Pair.of(thirdReport, ItemPosition.TOP_LEFT));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "Dashboard has reports apply filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        String pathFile = exportFile.getPath();
        //Date filter
        assertEquals(XlsxUtils.excelFileToRead(pathFile, 0), asList(
                asList("Applied filters:", "Year (Snapshot)" + format(" BETWEEN This-%d AND This-%d",
                        currentYear - 2010, currentYear - 2011)),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(INTEREST, "1.642738857E7"),
                asList(DISCOVERY, "3436167.7"), asList(SHORT_LIST, "3903521.33"),
                asList(RISK_ASSESSMENT, "2021556.99"), asList(CONVICTION, "2513393.4"),
                asList(NEGOTIATION, "348745.87"), asList(CLOSED_WON, "2.927793726E7"),
                asList(CLOSED_LOST, "2.824990059E7")));
        //Prompt filter
        assertEquals(XlsxUtils.excelFileToRead(pathFile, 1), asList(
                asList("Applied filters:", "Stage Name IN (Short List, Closed Lost)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(SHORT_LIST, "5612062.6"), asList(CLOSED_LOST, "4.247057116E7")));
        //Attribute filter
        assertEquals(XlsxUtils.excelFileToRead(pathFile, 2), asList(
                asList("Applied filters:", "Stage Name IN (Interest, Discovery)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88")));
        assertEquals(XlsxUtils.excelFileToRead(pathFile, 3), asList(
                asList("Applied filters:", "NOT (Stage Name IN " +
                        "(Interest, Discovery, Short List, Risk Assessment, Conviction, Negotiation))"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(CLOSED_WON, "3.831075345E7"),
                asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideValueFilterByAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME), SHORT_LIST));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, INTEREST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override value filter by attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideAllFilterByAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, INTEREST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override all filter by attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideAllFilterByDateAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2012").getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override all filter by date attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Year (Snapshot) IN (2012)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"), asList(NEGOTIATION, "1862015.73"),
                asList(CLOSED_WON, "3.831075345E7"), asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideValueFilterByDateAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), "2011"));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2012").getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override value filter by date attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Year (Snapshot) IN (2012)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"), asList(NEGOTIATION, "1862015.73"),
                asList(CLOSED_WON, "3.831075345E7"), asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideDateFilterByDateAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 0, 0));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override date filter by date attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Year (Snapshot) = THIS"), singletonList("Year (Snapshot) IN (2010)"),
                singletonList("Export failed: No data for your filter selection. " +
                        "Try adjusting or removing some of the filters.")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideDateRangeFilterByDateAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_WITHOUT_DATE,
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2010 - currentYear, 2012 - currentYear));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override date range filter by date attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Year (Snapshot)" + format(" BETWEEN THIS-%d AND THIS-%d",
                        currentYear - 2010, currentYear - 2012)),
                singletonList("Year (Snapshot) IN (2010)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1185127.28"), asList(DISCOVERY, "2080448.83"), asList(SHORT_LIST, "1347427.16"),
                asList(RISK_ASSESSMENT, "1222172.3"), asList(CONVICTION, "494341.51"), asList(NEGOTIATION, "647612.26"),
                asList(CLOSED_WON, "8886381.82"), asList(CLOSED_LOST, "1.105885084E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overridePromptFilterByAttributeParameterFilters() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilterBy(positivePromptUri));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, INTEREST, SHORT_LIST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override prompt filter by attribute parameter filters", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest,Short List)"), singletonList("Stage Name IN (Short List)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overridePromptNegativeFilterByAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(negativePromptUri),
                createMultipleValuesFilterBy(negativePromptUri));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, INTEREST, SHORT_LIST, CLOSED_LOST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override prompt negative filter by attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        //todo - will be reverted after XSH-31 was fixed
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Closed Lost,Interest,Short List)"),
                singletonList("Stage Name IN (Interest)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overridePromptFilterByAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilterBy(positivePromptUri));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, SHORT_LIST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override prompt filter by attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Short List)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overridePromptFilterByOutOfAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReportAndFilter(createReportAppliesPromptFilter(positivePromptUri),
                createMultipleValuesFilterBy(positivePromptUri));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, INTEREST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser,
                "override prompt filter by out of attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Stage Name IN (Interest)"), singletonList("Stage Name IN (Short List, Closed Lost)"),
                singletonList("Export failed: No data for your filter selection. Try adjusting or removing some of the filters.")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportNegativeFilterByAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(createReportAppliesNegativeFilter(ATTR_STAGE_NAME,
                singletonList(INTEREST)), ItemPosition.TOP));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, SHORT_LIST).getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override report negative filter by attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "NOT (Stage Name IN (Interest))"),
                singletonList("Stage Name IN (Short List)"),
                asList(ATTR_STAGE_NAME, METRIC_AMOUNT), asList(SHORT_LIST, "5612062.6")));
    }

    @Test(dependsOnGroups = "createProject")
    public void overrideReportDateFilterDateAttributeParameterFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        String expression = "[%s] = " + format("[%s/elements?id=%d]",
                getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri(), 2010);
        openDashboardHasReportAndFilter(createReportAppliesDateRangeFilter(ATTR_YEAR_SNAPSHOT, expression),
                createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), "2010"));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2012").getPreviewURI();
        embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "override report date filter date attribute parameter filter", getClass());
        final File exportFile = new File(testParams.getDownloadFolder(), embeddedDashboard.exportDashboardToXLSX(dashboardTitle));
        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
                asList("Applied filters:", "Year (Snapshot) IN (2012)"), asList(ATTR_STAGE_NAME, METRIC_AMOUNT),
                asList(INTEREST, "1.844726614E7"), asList(DISCOVERY, "4249027.88"), asList(SHORT_LIST, "5612062.6"),
                asList(RISK_ASSESSMENT, "2606293.46"), asList(CONVICTION, "3067466.12"), asList(NEGOTIATION, "1862015.73"),
                asList(CLOSED_WON, "3.831075345E7"), asList(CLOSED_LOST, "4.247057116E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportReportToXLSXWithUnmergedCellEmbeddedDashboard() throws IOException {
        String report = "Report" + generateHashString();
        dashboardTitle = generateDashboardName();

        createReport(GridReportDefinitionContent.create(report,
            singletonList(METRIC_GROUP),
            asList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME)),
                new AttributeInGrid(getAttributeByTitle(ATTR_DEPARTMENT))),
            singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        openDashboardHasReportAndFilter(report,
            createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME), SHORT_LIST));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        embeddedDashboard = initEmbeddedDashboard();

        Screenshots.takeScreenshot(browser, report, getClass());
        final File exportFile = new File(testParams.getDownloadFolder(),
            embeddedDashboard.exportDashboardToXLSXWithUnMergedCell(dashboardTitle));
        waitForExporting(exportFile);
        List<String> xlsxContent = XlsxUtils.excelFileToRead(exportFile.getPath(), 0).stream()
            .flatMap(List::stream).collect(Collectors.toList());
        log.info(report + ": " + xlsxContent);
        assertEquals(Collections.frequency(xlsxContent, SHORT_LIST), 2);
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

    private void openDashboardHasReports(Pair<String, ItemPosition>... reports) throws IOException {
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
