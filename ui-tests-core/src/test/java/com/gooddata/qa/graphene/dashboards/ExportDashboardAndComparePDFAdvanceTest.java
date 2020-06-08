package com.gooddata.qa.graphene.dashboards;

import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.ItemSize;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.WebContent;
import com.gooddata.qa.mdObjects.dashboard.tab.Widget;
import com.gooddata.qa.mdObjects.dashboard.tab.Text;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertTrue;

public class ExportDashboardAndComparePDFAdvanceTest extends AbstractDashboardWidgetTest {

    private String exportedDashboardName;
    private boolean resultComparePDF;
    private DashboardRestRequest dashboardRestRequest;
    private static final String DF_VARIABLE = "DF-Variable";
    private static final String EMAIL = "Email";
    private static final String PHONE_CALL = "Phone Call";
    private static final String ICON_EMBEDED_URL = "https://s3.amazonaws.com/gd-images.gooddata.com/customtext/" +
            "magic.html?text=MY%20ICON&size=20&color=1C1CFF&background=FFE6B3&url=https%3A%2F%2Fwww.google.com%2Fmaps%2F";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "Export Dashboard and Compare PDF Advanced ";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition().setDashboardName("%DASHBOARD_NAME")
                .setReportName("%REPORT_NAME").setTabName("%TAB_NAME");
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId()).exportDashboardSetting(exportDashboardDefinition);
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateProjectConfiguration("newUIEnabled", "classic");
        creteReportViaUI("AREA CHART APPLY FILTER", ReportTypes.AREA);
        creteReportViaUI("PIE CHART APPLY FILTER", ReportTypes.PIE);
        creteReportViaUI("BUBBLE CHART APPLY FILTER", ReportTypes.BUBBLE);
    }

    @Test(dependsOnGroups = "createProject")
    public void testComparePDFHaveAreaChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - AREA CHART REPORT";
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "AREA CHART APPLY FILTER", "AREA CHART APPLY FILTER" + "_" + generateHashString(),
                "AREA CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard("DASHBOARD TEST - AREA CHART REPORT");
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveAreaChartAndGEOChart")
    public void testComparePDFHaveTableChartAndWebContent() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - TABLE CHART REPORT";
        createReportViaRest("TABLE CHART APPLY FILTER");
        createDashboardHaveReportAndWebContentViaRest(DASHBOARD_NAME,
                "TABLE CHART APPLY FILTER", "TABLE CHART APPLY FILTER" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "TABLE CHART REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveTableChartAndWebContent")
    public void testComparePDFHaveAreaAndTableChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - AREA AND TABLE CHART REPORT";
        createDashboardHaveAreaAndTableChartViaRest(DASHBOARD_NAME, "AREA CHART APPLY FILTER",
                "TABLE CHART APPLY FILTER", "AREA AND TABLE CHART APPLY FILTER" + "_" + generateHashString());
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveAreaAndTableChart")
    public void testComparePDFPaging() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - PDF PAGING REPORT";
        createDashboardHaveReportAndWebContentApplyPagingViaRest(DASHBOARD_NAME,
                "AREA CHART APPLY FILTER", "AREA APPLY PAGING" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "AREA APPLY PAGING");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName, 1, 2);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFPaging")
    public void testComparePDFHaveStackedAreaChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - STACKED AREA CHART REPORT";
        createReportViaRest("AREA CHART APPLY FILTER", "STACKED AREA CHART APPLY FILTER", "stackedArea");
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "STACKED AREA CHART APPLY FILTER", "STACKED AREA CHART APPLY FILTER" + "_" + generateHashString(),
                "STACKED AREA CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveStackedAreaChartAndGEOChart")
    public void testComparePDFHaveBarChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - BAR CHART REPORT";
        createReportViaRest("STACKED AREA CHART APPLY FILTER", "BAR CHART APPLY FILTER", "bar");
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "BAR CHART APPLY FILTER", "BAR CHART APPLY FILTER" + "_" + generateHashString(),
                "BAR CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveBarChartAndGEOChart")
    public void testComparePDFHaveStackedBarChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - STACKED BAR CHART REPORT";
        createReportViaRest("BAR CHART APPLY FILTER", "STACKED BAR CHART APPLY FILTER", "stackedBar");
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "STACKED BAR CHART APPLY FILTER", "STACKED BAR CHART APPLY FILTER" + "_" + generateHashString(),
                "STACKED BAR CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveStackedBarChartAndGEOChart")
    public void testComparePDFHaveLineChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - LINE CHART REPORT";
        createReportViaRest("STACKED BAR CHART APPLY FILTER", "LINE CHART APPLY FILTER", "line");
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "LINE CHART APPLY FILTER", "LINE CHART APPLY FILTER" + "_" + generateHashString(),
                "LINE CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveLineChartAndGEOChart")
    public void testComparePDFHaveBulletChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - BULLET CHART REPORT";
        createReportViaRest("LINE CHART APPLY FILTER", "BULLET CHART APPLY FILTER", "thermometer");// bullet chart
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "BULLET CHART APPLY FILTER", "BULLET CHART APPLY FILTER" + "_" + generateHashString(),
                "BULLET CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveBulletChartAndGEOChart")
    public void testComparePDFHaveFunnelChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - FUNNEL CHART REPORT";
        createReportViaRest("BULLET CHART APPLY FILTER", "FUNNEL CHART APPLY FILTER", "funnel");// bullet chart
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "FUNNEL CHART APPLY FILTER", "FUNNEL CHART APPLY FILTER" + "_" + generateHashString(),
                "FUNNEL CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveFunnelChartAndGEOChart")
    public void testComparePDFHaveWaterfallChartAndGEOChart() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - WATERFALL CHART REPORT";
        createReportViaRest("FUNNEL CHART APPLY FILTER", "WATERFALL CHART APPLY FILTER", "waterfall");// bullet chart
        createDashboardHaveReportAndGeoChartViaRest(DASHBOARD_NAME,
                "WATERFALL CHART APPLY FILTER", "WATERFALL CHART APPLY FILTER" + "_" + generateHashString(),
                "WATERFALL CHART APPLY FILTER REPORT");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testComparePDFHavePieChartAndWebContent() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - PIE CHART REPORT";
        createDashboardHaveReportAndWebContentViaRest(DASHBOARD_NAME,
                "PIE CHART APPLY FILTER", "PIE CHART APPLY FILTER" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "PIE CHART APPLY FILTER");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHavePieChartAndWebContent")
    public void testComparePDFHaveDonutChartAndWebContent() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - DONUT CHART REPORT";
        createReportViaRest("PIE CHART APPLY FILTER", "DONUT CHART APPLY FILTER", "donut");
        createDashboardHaveReportAndWebContentViaRest(DASHBOARD_NAME,
                "DONUT CHART APPLY FILTER", "DONUT CHART APPLY FILTER" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "DONUT CHART APPLY FILTER");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testComparePDFHaveBubbleChartAndWebContent() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - BUBBLE CHART REPORT";
        createDashboardHaveReportAndWebContentViaRest(DASHBOARD_NAME,
                "BUBBLE CHART APPLY FILTER", "BUBBLE CHART APPLY FILTER" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "BUBBLE CHART APPLY FILTER");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = "testComparePDFHaveBubbleChartAndWebContent")
    public void testComparePDFHaveScatteredChartAndWebContent() throws IOException {
        String DASHBOARD_NAME = "DASHBOARD TEST - SCATTERED CHART REPORT";
        createReportViaRest("BUBBLE CHART APPLY FILTER", "SCATTERED CHART APPLY FILTER", "scattered");
        createDashboardHaveReportAndWebContentViaRest(DASHBOARD_NAME,
                "SCATTERED CHART APPLY FILTER", "SCATTERED CHART APPLY FILTER" + "_" + generateHashString(),
                ICON_EMBEDED_URL, "SCATTERED CHART APPLY FILTER");
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    private void createReportViaRest(String reportName, String renameTitle, String chartType) throws IOException {
        ReportRestRequest reportRestRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        reportRestRequest.changeChartType(reportName, chartType).updateTitleReport(reportName, renameTitle);
    }

    private void createDashboardHaveReportAndGeoChartViaRest(String dashboardName, String reportName, String tabName,
                                                             String text) throws IOException {
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTabHaveWidgetAndText(tabName, reportName, text))
                .getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);
    }

    private void createDashboardHaveReportAndWebContentApplyPagingViaRest(String dashboardName, String reportName, String tabName,
                                                             String url, String text) throws IOException {
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTabHavaWebContentAndTextForPaging(tabName, reportName, url, text))
                .getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);
    }

    private void createDashboardHaveReportAndWebContentViaRest(String dashboardName, String reportName, String tabName,
                                                               String url, String text) throws IOException {
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTabHaveWebContentAndText(tabName, reportName, url, text))
                .getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);
    }

    private void createDashboardHaveAreaAndTableChartViaRest(String dashboardName, String areaReportName, String tableReportName,
                                                             String tabName) throws IOException {
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTab(tabName, areaReportName, tableReportName))
                .getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);
    }

    private Tab initTabHaveWidgetAndText(String name, String reportName, String headline) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name).addItems(asList(
                    Builder.of(ReportItem::new).with(report -> report.setItemSize(ItemSize.REPORT_ITEM_CUSTOMIZE)
                            .setObjUri(getReportByTitle(reportName).getUri())).build(),
                    Builder.of(Widget::new).with(widget -> widget.setWidgetTopMiddle()).build(),
                    Builder.of(Text::new).with(text -> text.setHeadline(headline)).build()));
        }).build();
    }

    private Tab initTabHaveWebContentAndText(String name, String reportName, String url, String headline) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name).addItems(asList(
                    Builder.of(ReportItem::new).with(report -> report.setItemSize(ItemSize.REPORT_ITEM_CUSTOMIZE)
                            .setObjUri(getReportByTitle(reportName).getUri())).build(),
                    Builder.of(WebContent::new).with(content -> content.setWebContent(url)).build(),
                    Builder.of(Text::new).with(text -> text.setHeadline(headline)).build()));
        }).build();
    }

    private Tab initTabHavaWebContentAndTextForPaging(String name, String reportName, String url, String headline) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name).addItems(asList(
                    Builder.of(ReportItem::new).with(report -> report.setItemSize(ItemSize.REPORT_ITEM_CUSTOMIZE)
                            .setObjUri(getReportByTitle(reportName).getUri())).build(),
                    Builder.of(WebContent::new).with(content -> content.setWebContentForPaging(url)).build(),
                    Builder.of(Text::new).with(text -> text.setHeadline(headline)).build()));
        }).build();
    }

    private Tab initTab(String name, String areaReport, String tableReport) {
        ReportItem tableReportItem = createReportItem(getReportByTitle(areaReport).getUri());
        tableReportItem.setItemSize(ItemSize.REPORT_ITEM_CUSTOMIZE);
        ReportItem areaReportItem = createReportItem(getReportByTitle(tableReport).getUri());
        areaReportItem.setItemSize(ItemSize.REPORT_ITEM_CUSTOMIZE);
        areaReportItem.setPosition(TabItem.ItemPosition.TOP_RIGHT);
        return new Tab().setTitle(name).addItems(Stream.of(asList(areaReportItem, tableReportItem)).flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private void createReportViaRest(String reportName) {
        VariableRestRequest request = new VariableRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String promptFilterUri = request.createFilterVariable(DF_VARIABLE,
                request.getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri(),
                asList(EMAIL, PHONE_CALL));
        createReportViaRest(GridReportDefinitionContent.create(reportName,
                singletonList(METRIC_GROUP),
                asList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_DEPARTMENT))),
                asList(new MetricElement(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)),
                        new MetricElement(getMetricByTitle(METRIC_OPP_FIRST_SNAPSHOT))),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));
    }

    private void creteReportViaUI(String reportName, ReportTypes reportTypes) {
        initReportsPage().startCreateReport().initPage()
                .openWhatPanel().selectItems(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).done();
        reportPage.waitForReportExecutionProgress();
        reportPage.openHowPanel().selectItems(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).done();
        reportPage.waitForReportExecutionProgress();
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_ACTIVITY_TYPE, EMAIL, PHONE_CALL))
                .waitForReportExecutionProgress();
        reportPage.selectReportVisualisation(reportTypes).setReportName(reportName)
                .finishCreateReport().waitForReportExecutionProgress();
        takeScreenshot(browser, reportName, getClass());
    }
}
