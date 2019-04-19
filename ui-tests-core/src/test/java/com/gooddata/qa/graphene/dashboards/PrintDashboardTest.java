package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.MIDDLE;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;

public class PrintDashboardTest extends AbstractEmbeddedModeTest {

    private static final String REPORT_AMOUNT_BY_F_STAGE_NAME = "Amount by f stage name";
    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String F_STAGE_NAME = "FStage Name";
    private static final String ICON_EMBEDED_URL = "https://s3.amazonaws.com/gd-images.gooddata.com/customtext/" +
            "magic.html?text=MY%20ICON&size=20&color=1C1CFF&background=FFE6B3&url=https%3A%2F%2Fwww.google.com%2Fmaps%2F";

    private static final int currentYear = DateRange.now().getYear();
    private String today;

    private String dashboardTitle;
    private String firstTab;
    private DashboardRestRequest dashboardRequest;
    private String promptUri;

    @Override
    public void init(@Optional("maximize") String windowSize) throws JSONException {
        //In client demo and PI environment, test sometimes throws timeout exception relates to worker
        //Skipping while finding solution
        if (testParams.isClientDemoEnvironment() || testParams.isPIEnvironment()) {
            log.info("There isn't exported feature in client demo or PI environment");
            validateAfterClass = false;
            throw new SkipException("There isn't exported feature in client demo or PI environment");
        }
        super.init(windowSize);
    }

    @Override
    protected void customizeProject() throws Throwable {
        Reports reports = getReportCreator();
        reports.createAmountByProductReport();
        reports.createAmountByStageNameReport();
        promptUri = getVariableCreator().createFilterVariable(F_STAGE_NAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT, CONVICTION));
        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_F_STAGE_NAME,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("[%s]", promptUri)))));

        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateProjectConfiguration("newUIEnabled", "classic");
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasTableReportToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Table_Report", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount",
                "CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "$27,222,899.64", "$22,946,895.47", "$38,596,194.86", "$8,042,031.92", "$9,525,857.91", "$10,291,576.74",
                firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasChartReportToPdf() throws IOException {
        String headlineReport = "Headline Report";
        dashboardTitle = generateDashboardName();
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.HEADLINE).saveAsReport(headlineReport);
        openDashboardHasReports(Pair.of(headlineReport, ItemPosition.LEFT));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Chart_Report", getClass());
        assertEquals(contents, asList("$11", "Amou", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasLineReportToPdf() throws IOException {
        String lineReport = "Line Report";
        dashboardTitle = generateDashboardName();
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.LINE).waitForReportExecutionProgress().saveAsReport(lineReport);
        openDashboardHasReports(Pair.of(lineReport, ItemPosition.LEFT));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Line_Report", getClass());
        assertThat(contents, hasItems(lineReport, "Product", "$0.00", "$20,000,000.00", "$40,000,000.00",
                firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasAttributeSingleFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent = createSingleValueFilter(getAttributeByTitle(ATTR_PRODUCT));
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_PRODUCT, filterContent);
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Attribute_Single_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci $27,222,899.64",
                "PRODUCT", "CompuSci", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasPromptSingleFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent = createSingleValuesFilterBy(promptUri);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_F_STAGE_NAME, filterContent);
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Prompt_Single_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount",
                "Interest $18,447,266.14", "FSTAGE NAME", "Interest", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasDateRangeFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent =
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2010 - currentYear, 2011 - currentYear);
        openDashboardHasReportAndFilter(REPORT_AMOUNT_BY_F_STAGE_NAME, filterContent);
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Date_Range_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "$16,427,388.57", "$3,436,167.70", "$3,903,521.33", "$2,021,556.99",
                "$2,513,393.40 DATE DIMENSION (SNAPSHOT)", "2010 - 2011", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasTextToPdf() {
        String googleUri = "google.com";
        firstTab = generateHashString();
        initDashboardsPage().addNewDashboard(generateDashboardName())
                .renameTab(0, firstTab)
                .editDashboard()
                .addTextToDashboard(TextObject.HEADLINE, REPORT_AMOUNT_BY_F_STAGE_NAME, googleUri)
                .addTextToDashboard(TextObject.SUB_HEADLINE, REPORT_AMOUNT_BY_STAGE_NAME, googleUri)
                .addTextToDashboard(TextObject.DESCRIPTION, REPORT_AMOUNT_BY_PRODUCT, googleUri)
                .addVariableStatusToDashboard(F_STAGE_NAME)
                .saveDashboard();
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Text", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, REPORT_AMOUNT_BY_STAGE_NAME,
                REPORT_AMOUNT_BY_PRODUCT, "FStage Name", "Interest, Discovery, Short List, Risk Assessment,",
                "Conviction", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasKeyMetricToPdf() {
        firstTab = generateHashString();
        initDashboardsPage().addNewDashboard(generateDashboardName()).renameTab(0, firstTab)
                .addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_AMOUNT).saveDashboard();
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Key_Metric", getClass());
        assertEquals(contents, asList("No data", "Title", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printHugeDashboardToPdf() throws IOException {
        firstTab = generateHashString();
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.RIGHT), Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_LEFT),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT), Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.MIDDLE),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_RIGHT));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Huge_Dashboard", getClass());
        assertThat(contents, hasItems(firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardHasWebContentToPdf() throws IOException {
        firstTab = generateHashString();
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_LEFT));
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        String embeddedUri = embedDashboardDialog.getPreviewURI();
        embedDashboardDialog.close();
        dashboardsPage.editDashboard().addWebContentToDashboard(embeddedUri).saveDashboard();
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_WebContent", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", "$9,525,857.91", "$10,291,576.74", REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci",
                "Educationly", "Explorer", "Grammar Plus", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardWithPageBreakMarkerToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT),
                Pair.of(REPORT_AMOUNT_BY_F_STAGE_NAME, ItemPosition.NEXT_PAGE));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_With_Page_Break_Marker", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", "$9,525,857.91", "$10,291,576.74", firstTab + " " + today, "Page 1/2",
                REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "$18,447,266.14", "$4,249,027.88", "$5,612,062.60", "$2,606,293.46",
                "$3,067,466.12", "Page 2/2"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardWithWebContentsNearPageBreakMarkerToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT));
        WebElement embeddedWidget = dashboardsPage.addWebContentToDashboard(ICON_EMBEDED_URL).getLastEmbeddedWidget().getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(embeddedWidget);
        DashboardWidgetDirection.NEXT_PAGE.moveElementToRightPlaceOutViewPort(embeddedWidget);
        embeddedWidget = dashboardsPage.addWebContentToDashboard(ICON_EMBEDED_URL).getLastEmbeddedWidget().getRoot();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget);
        DashboardWidgetDirection.NEXT_PAGE.moveElementToRightPlaceOutViewPort(embeddedWidget);
        Screenshots.takeScreenshot(browser, "Dashboard_With_Web_Contents_Near_Page_Break_Maker", getClass());
        dashboardsPage.saveDashboard();
        
        initDashboardsPage().printDashboardTab(); //Have to initialize Page to rollback top of site
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", "$9,525,857.91", "$10,291,576.74", "MY ICON MY ICON", firstTab + " " + today, "Page 1/2",
                "MY ICON MY ICON", "Page 2/2"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printDashboardWithReportsNearPageBreakMarkerToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT),
                Pair.of(REPORT_AMOUNT_BY_F_STAGE_NAME, ItemPosition.NEXT_PAGE),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT_OF_NEXT_PAGE));
        dashboardsPage.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_With_Reports_Near_Page_Break_Maker", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", "$9,525,857.91", "$10,291,576.74", firstTab + " " + today, "Page 1/2",
                REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "$18,447,266.14", "$4,249,027.88", "$5,612,062.60", "$2,606,293.46",
                "$3,067,466.12", REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
                "$8,042,031.92", "$9,525,857.91", "$10,291,576.74", "Page 2/2"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printEmbeddedDashboardWithURLParameterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent dateFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT));
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, attributeFilterContent, dateFilterContent);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().selectFilterAttribute(ATTR_STAGE_NAME, INTEREST)
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Embedded_Dashboard_With_URL_Parameter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest $1,185,127.28",
                "STAGE NAME", INTEREST, "YEAR (SNAPSHOT)", "2010", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void printEmbeddedDashboardAfterChangingFilterPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent dateFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT));
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, attributeFilterContent, dateFilterContent);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().selectFilterAttribute(ATTR_STAGE_NAME, INTEREST)
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.getFilterWidgetByName(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        embeddedDashboard.printDashboardTab();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Embedded_Dashboard_After_Changing_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Discovery $2,080,448.83",
                "STAGE NAME", DISCOVERY, "YEAR (SNAPSHOT)", "2010", firstTab + " " + today, "Page 1/1"));
    }

    private void openDashboardHasReportAndFilters(String titleReport, FilterItemContent attributeFilterContent,
                                                  FilterItemContent dateFilterContent) throws IOException {
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, asList(
                        Pair.of(attributeFilterContent, MIDDLE),
                        Pair.of(dateFilterContent, TOP_RIGHT))))
                .addFilter(attributeFilterContent)
                .addFilter(dateFilterContent);
        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private void openDashboardHasReports(Pair<String, ItemPosition>... reports) throws IOException {
        firstTab = format("First Tab - %s", dashboardTitle);
        List<TabItem> tabItems = Stream.of(reports).map(this::createReportItem).collect(Collectors.toList());
        Dashboard dashboard = new Dashboard().setName(dashboardTitle)
                .addTab(new Tab().setTitle(firstTab).addItems(tabItems));

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
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, singletonList(
                        Pair.of(filterItemContent, RIGHT))))
                .addFilter(filterItemContent);

        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private Tab initTab(String report, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        firstTab = format("First tab - %s", dashboardTitle);
        List<FilterItem> filterItems = appliedFilters.stream()
                .map(this::buildFilterItem)
                .collect(Collectors.toList());
        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));
        return new Tab().setTitle(firstTab).addItems(singletonList(reportItem)).addItems(filterItems);
    }

    private FilterItem buildFilterItem(Pair<FilterItemContent, ItemPosition> filterItem) {
        FilterItem filterItemContent = new FilterItem().setContentId(filterItem.getLeft().getId());
        filterItemContent.setPosition(filterItem.getRight());
        return filterItemContent;
    }
}
