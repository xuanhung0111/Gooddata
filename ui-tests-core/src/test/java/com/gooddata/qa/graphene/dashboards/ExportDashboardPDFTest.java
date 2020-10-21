package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
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
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.MIDDLE;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ExportDashboardPDFTest extends AbstractEmbeddedModeTest {

    private static final String FIRST_TAB = "First Tab";
    private static final String SECOND_TAB = "Second Tab";
    private static final String COMPUSCI = "CompuSci";
    private static final String EDUCATIONLY = "Educationly";
    private static final String REPORT_AMOUNT_BY_F_STAGE_NAME = "Amount by f stage name";
    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String F_STAGE_NAME = "FStage Name";
    private static final String DASHBOARD_TEST = "Dashboard Test";
    private static final String DASHBOARD_HAVING_REPORT_AND_FILTER = "Dashboard Having Report And Filter";

    private static final int currentYear = DateRange.now().getYear();
    private String today;

    private String exportedDashboardName;
    private String dashboardTitle;
    private String firstTab;
    private DashboardRestRequest dashboardRequest;
    private String promptUri;

    @Override
    public void init(@Optional("maximize") String windowSize) throws JSONException {
        if (testParams.isClientDemoEnvironment()) {
            log.info("There isn't exported feature in client demo environment");
            validateAfterClass = false;
            throw new SkipException("There isn't exported feature in client demo environment");
        }
        super.init(windowSize);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.EDITOR_AND_INVITATIONS);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
        createAndAddUserToProject(UserRoles.EXPLORER);
        createAndAddUserToProject(UserRoles.VIEWER);
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
    public void prepareDashboard() {
        initDashboardsPage().addNewDashboard(DASHBOARD_TEST);
        dashboardsPage.editDashboard()
                .addReportToDashboard(REPORT_AMOUNT_BY_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_PRODUCT).getRoot());
        dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT))
                .changeAttributeFilterValues(COMPUSCI, EDUCATIONLY);
        dashboardsPage.saveDashboard();
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasTableReportToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Table_Report", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount",
                "CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "$27,222,899.64", "$22,946,895.47", "$38,596,194.86", "$8,042,031.92", "$9,525,857.91", "$10,291,576.74",
                firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasChartReportToPdf() throws IOException {
        String headlineReport = "Headline Report";
        dashboardTitle = generateDashboardName();
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.HEADLINE).saveAsReport(headlineReport);
        openDashboardHasReports(Pair.of(headlineReport, ItemPosition.LEFT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Chart_Report", getClass());
        assertEquals(contents, asList("$11", "Amou", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasLineReportToPdf() throws IOException {
        String lineReport = "Line Report";
        dashboardTitle = generateDashboardName();
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .selectReportVisualisation(ReportTypes.LINE).waitForReportExecutionProgress().saveAsReport(lineReport);
        openDashboardHasReports(Pair.of(lineReport, ItemPosition.LEFT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Line_Report", getClass());
        assertThat(contents, hasItems(lineReport, "Product", "$0.00", "$20,000,000.00", "$40,000,000.00",
                firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasAttributeSingleFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent = createSingleValueFilter(getAttributeByTitle(ATTR_PRODUCT));
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_PRODUCT, Pair.of(filterContent, RIGHT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Attribute_Single_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount", "CompuSci $27,222,899.64",
                "PRODUCT", "CompuSci", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasPromptSingleFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent = createSingleValuesFilterBy(promptUri);
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, Pair.of(filterContent, RIGHT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Prompt_Single_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount",
                "Interest $18,447,266.14", "FSTAGE NAME", "Interest", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasDateRangeFilterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent filterContent =
                createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT), 2010 - currentYear, 2011 - currentYear);
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, Pair.of(filterContent, RIGHT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Date_Range_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "$16,427,388.57", "$3,436,167.70", "$3,903,521.33", "$2,021,556.99",
                "$2,513,393.40 DATE DIMENSION (SNAPSHOT)", "2010 - 2011", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasTextToPdf() throws IOException {
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
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Text", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, REPORT_AMOUNT_BY_STAGE_NAME,
                REPORT_AMOUNT_BY_PRODUCT, "FStage Name", "Interest, Discovery, Short List, Risk Assessment,",
                "Conviction", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasKeyMetricToPdf() throws IOException {
        firstTab = generateHashString();
        initDashboardsPage().addNewDashboard(generateDashboardName()).renameTab(0, firstTab)
                .addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_AMOUNT).saveDashboard();
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_Has_Key_Metric", getClass());
        assertEquals(contents, asList("No data", "Title", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportHugeDashboardToPdf() throws IOException {
        firstTab = generateHashString();
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.RIGHT), Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_LEFT),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT), Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.MIDDLE),
                Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_RIGHT));
        dashboardsPage.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Huge_Dashboard", getClass());
        assertThat(contents, hasItems(firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportDashboardHasWebContentToPdf() throws IOException {
        firstTab = generateHashString();
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.TOP_LEFT));
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        String embeddedUri = embedDashboardDialog.getPreviewURI();
        embedDashboardDialog.close();
        dashboardsPage.editDashboard().addWebContentToDashboard(embeddedUri).saveDashboard();
        dashboardsPage.exportDashboardTabToPDF();
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
    public void exportDashboardWithPageBreakMarkerToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT),
                Pair.of(REPORT_AMOUNT_BY_F_STAGE_NAME, ItemPosition.NEXT_PAGE));
        dashboardsPage.exportDashboardTabToPDF();
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
    public void exportEmbeddedDashboardWithURLParameterToPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent dateFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT));
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, Pair.of(attributeFilterContent, MIDDLE),
                Pair.of(dateFilterContent, TOP_RIGHT));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().selectFilterAttribute(ATTR_STAGE_NAME, INTEREST)
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Embedded_Dashboard_With_URL_Parameter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Interest $1,185,127.28",
                "STAGE NAME", INTEREST, "YEAR (SNAPSHOT)", "2010", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportEmbeddedDashboardAfterChangingFilterPdf() throws IOException {
        dashboardTitle = generateDashboardName();
        FilterItemContent attributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent dateFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT));
        openDashboardHasReportAndFilters(REPORT_AMOUNT_BY_F_STAGE_NAME, Pair.of(attributeFilterContent, MIDDLE),
                Pair.of(dateFilterContent, TOP_RIGHT));
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().selectFilterAttribute(ATTR_STAGE_NAME, INTEREST)
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.getFilterWidgetByName(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        embeddedDashboard.exportDashboardTabToPDF();
        today = DateRange.getCurrentDate();
        List<String> contents = asList(getContentFrom(firstTab).split("\n"));
        Screenshots.takeScreenshot(browser, "Embedded_Dashboard_After_Changing_Filter", getClass());
        assertEquals(contents, asList(REPORT_AMOUNT_BY_F_STAGE_NAME, "Stage Name Amount", "Discovery $2,080,448.83",
                "STAGE NAME", DISCOVERY, "YEAR (SNAPSHOT)", "2010", firstTab + " " + today, "Page 1/1"));
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void verifyExportedDashboardPDF() throws IOException {
        initDashboardsPage().selectDashboard(DASHBOARD_TEST);
        waitForDashboardPageLoaded(browser);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, FIRST_TAB);

            List<String> contents = asList(getContentFrom(FIRST_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(ATTR_PRODUCT + " " + METRIC_AMOUNT));
            //verify content
            assertThat(contents, hasItems(COMPUSCI, EDUCATIONLY));
            assertThat(contents, hasItems("$27,222,899.64", "$22,946,895.47"));
            //verify filter
            assertThat(contents, not(hasItems("Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid")));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName));
        }
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void exportCopiedDashboard() throws IOException {
        String copiedDashboard = "Copied Dashboard";
        initDashboardsPage().selectDashboard(DASHBOARD_TEST)
                .editDashboard()
                .saveAsDashboard(copiedDashboard, false, PermissionType.USE_EXISTING_PERMISSIONS);
        waitForDashboardPageLoaded(browser);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, FIRST_TAB);

            List<String> contents = asList(getContentFrom(FIRST_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(format("%s %s", ATTR_PRODUCT, METRIC_AMOUNT)));
            //verify content
            assertThat(contents, hasItems(COMPUSCI, EDUCATIONLY));
            assertThat(contents, hasItems("$27,222,899.64", "$22,946,895.47"));
            //verify filter
            assertThat(contents, not(hasItems("Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid")));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName));
        }
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void exportDashboardWithDuplicateFilter() throws IOException {
        createDashboardWithDuplicateFilter();
        initDashboardsPage().selectDashboard(DASHBOARD_HAVING_REPORT_AND_FILTER)
                .getFirstFilter().changeAttributeFilterValues(COMPUSCI);
        exportedDashboardName = dashboardsPage.printDashboardTab();
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, SECOND_TAB);

            List<String> contents = asList(getContentFrom(SECOND_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(format("%s %s", ATTR_PRODUCT, METRIC_AMOUNT)));
            //verify content
            assertThat(contents, hasItem("CompuSci $27,222,899.64"));
            //verify filter
            assertThat(contents, not(hasItems("Educationly $22,946,895.47")));
            assertThat(contents, hasItem("PRODUCT"));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName));
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void cannotExportBlankEmbeddedDashboard() {
        dashboardTitle = generateDashboardName();
        embeddedUri = initDashboardsPage().addNewDashboard(dashboardTitle).openEmbedDashboardDialog().getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.openEditExportEmbedMenu();
        assertFalse(embeddedDashboard.isSettingExportToPdfButtonVisible(),
            "Dashboard setting export to pdf should be disabled");
        assertFalse(embeddedDashboard.isSettingExportToXLSXButtonVisible(),
            "Dashboard setting export to XLSX should be disabled");
    }

    @AfterClass(alwaysRun = true)
    public void exportBlankDashboard() {
        initDashboardsPage().addNewDashboard("Empty Dashboard");

        assertTrue(dashboardsPage.isPrintButtonDisabled(),
            "Dashboard setting Print button should be disabled");
        dashboardsPage.openEditExportEmbedMenu();
        assertFalse(dashboardsPage.isSettingExportToPdfButtonVisible(),
            "Dashboard setting export to pdf should be disabled");
        assertFalse(dashboardsPage.isSettingExportToXLSXButtonVisible(),
            "Dashboard setting export to XLSX should be disabled");

        dashboardsPage.getDashboardsNames().stream().forEach(name -> tryDeleteDashboard(name));

        assertTrue(dashboardsPage.isPrintButtonDisabled(),
            "Dashboard setting Print button should be disabled");

        dashboardsPage.openEditExportEmbedMenu();
        assertFalse(dashboardsPage.isSettingExportToPdfButtonVisible(),
            "Dashboard setting export to pdf should be disabled");
        assertFalse(dashboardsPage.isSettingExportToXLSXButtonVisible(),
            "Dashboard setting export to XLSX should be disabled");

        initDashboardsPage().addNewDashboard("Dashboard has objects exclude report");
        dashboardsPage.editDashboard().addAttributeFilterToDashboard(
            DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT).saveDashboard();

        dashboardsPage.openEditExportEmbedMenu();
        assertFalse(dashboardsPage.isSettingExportToXLSXButtonVisible(),
            "Dashboard setting export to XLSX should be disabled");

        dashboardsPage.closeEditExportEmbedMenu();
        dashboardsPage.exportDashboardTabToPDF();
        List<String> contents = asList(getContentFrom(FIRST_TAB).split("\n"));
        Screenshots.takeScreenshot(browser, "Dashboard_has_objects_exclude_report", getClass());
        today = DateRange.getCurrentDate();
        assertEquals(contents, asList("DEPARTMENT" , "All", FIRST_TAB + " " + today, "Page 1/1"));
        takeScreenshot(browser, "Export_Blank_Dashboard", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getAllRolesUser")
    public void exportDashboardWithAllRoles(UserRoles roles) throws IOException{
        logoutAndLoginAs(true, roles);
        try {
            dashboardTitle = generateDashboardName();
            openDashboardHasReports(Pair.of(REPORT_AMOUNT_BY_PRODUCT, ItemPosition.LEFT));
            dashboardsPage.exportDashboardTabToPDF();
            today = DateRange.getCurrentDate();
            List<String> contents = asList(getContentFrom(firstTab).split("\n"));
            Screenshots.takeScreenshot(browser, "Dashboard_Has_Table_Report_With_All_Roles", getClass());
            assertEquals(contents, asList(REPORT_AMOUNT_BY_PRODUCT, "Product Amount",
                "CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "$27,222,899.64", "$22,946,895.47", "$38,596,194.86", "$8,042,031.92", "$9,525,857.91", "$10,291,576.74",
                firstTab + " " + today, "Page 1/1"));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "getAllRolesUser")
    public Object[][] getAllRolesUser() {
        return new Object[][]{
            {UserRoles.EDITOR},
            {UserRoles.EDITOR_AND_INVITATIONS},
            {UserRoles.EDITOR_AND_USER_ADMIN},
            {UserRoles.EXPLORER},
            {UserRoles.VIEWER},
        };
    }

    private void tryDeleteDashboard(String name) {
        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard(name);
            dashboardsPage.deleteDashboard();
        } catch (AssertionError e) {
            // sometime we get RED BAR: Dashboard no longer exists
            // in this case, we also want to delete this dashboard so ignore this issue
        }
    }

    private void createDashboardWithDuplicateFilter() throws IOException {
        FilterItemContent productFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_PRODUCT));

        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_REPORT_AND_FILTER);
            dash.addTab(Builder.of(Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(productFilter.getId());
                            item.setPosition(TabItem.ItemPosition.RIGHT);
                        }).build();

                        tab.setTitle(SECOND_TAB);
                        tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                            reportItem.setObjUri(getReportByTitle(REPORT_AMOUNT_BY_PRODUCT).getUri());
                            reportItem.setPosition(TabItem.ItemPosition.LEFT);
                            reportItem.setAppliedFilterIds(singletonList(filterItem.getId()));
                        }).build());
                        tab.addItem(filterItem);
                    })
                    .build());
            dash.addFilter(productFilter).addFilter(productFilter);
        }).build();
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(dashboard.getMdObject());
    }

    private void openDashboardHasReportAndFilters(String titleReport,
            Pair<FilterItemContent, ItemPosition>... filterContents) throws IOException {
        Dashboard dashboard = new Dashboard()
                .setName(dashboardTitle)
                .addTab(initTab(titleReport, filterContents));
        for (Pair<FilterItemContent, ItemPosition> filterContent : filterContents) {
            dashboard.addFilter(filterContent.getKey());
        }
        dashboardRequest.createDashboard(dashboard.getMdObject());
        initDashboardsPage().selectDashboard(dashboardTitle);
    }

    private void openDashboardHasReports(Pair<String, ItemPosition>... reports) throws IOException {
        firstTab = format("First tab - %s", dashboardTitle);
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

    private Tab initTab(String report, Pair<FilterItemContent, ItemPosition>... appliedFilters) {
        firstTab = format("First tab - %s", dashboardTitle);
        List<FilterItem> filterItems = Stream.of(appliedFilters)
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
