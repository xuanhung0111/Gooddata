package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static org.testng.Assert.assertTrue;

public class ExportDashboardAndComparePDFBasicTest extends AbstractDashboardWidgetTest {
    private String exportedDashboardName;
    private boolean resultComparePDF;

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "Export Dashboard to PDF and compare ";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition().setProjectName("%PROJECT_NAME")
                .setDashboardName("%DASHBOARD_NAME").setReportName("%REPORT_NAME").setTabName("%TAB_NAME");
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .exportDashboardSetting(exportDashboardDefinition);
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateProjectConfiguration("newUIEnabled", "classic");

    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareTableReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
                        .withName("TABLE CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "TABLE CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "TABLE" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("TABLE CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareAreaReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.AREA)
                        .withName("AREA CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "AREA CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "AREA" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("AREA CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareHeadLineReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.HEADLINE)
                        .withName("HEADLINE CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "HEADLINE CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "HEADLINE" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("HEADLINE CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareLineReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.LINE)
                        .withName("LINE CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "LINE CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "LINE" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("LINE CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareStackedAreaReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.STACKED_AREA)
                        .withName("STACKED AREA CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "STACKED AREA CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "STACKED AREA" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("STACKED AREA CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareBarReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.BAR)
                        .withName("BAR CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "BAR CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "BAR" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("BAR CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareStackedBarReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.STACKED_BAR)
                        .withName("STACKED BAR CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "STACKED BAR CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "STACKED BAR" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("STACKED BAR CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareBulletReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.BULLET)
                        .withName("BULLET CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "BULLET CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "BULLET" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("BULLET CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareWaterFallReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.WATERFALL)
                        .withName("WATERFALL CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "WATERFALL CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "WATERFALL" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("WATERFALL CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareFunnelReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.FUNNEL)
                        .withName("FUNNEL CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "FUNNEL CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "FUNNEL" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("FUNNEL CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testComparePieReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.PIE)
                        .withName("PIE CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "PIE CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "PIE" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("PIE CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareScatterReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.SCATTER)
                        .withName("SCATTER CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "SCATTER CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "SCATTER" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("SCATTER CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareBubbleReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.BUBBLE)
                        .withName("BUBBLE CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "BUBBLE CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "BUBBLE" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("BUBBLE CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnGroups = "createProject")
    public void testCompareDonutReport() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.DONUT)
                        .withName("DONUT CHART REPORT")
                        .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES), new WhatItem(METRIC_OPP_FIRST_SNAPSHOT))
                        .withHows(new HowItem(ATTR_ACTIVITY_TYPE), new HowItem(ATTR_DEPARTMENT)),
                "DONUT CHART REPORT");
        initDashboardsPage().addNewDashboard("Dashboard Test").renameTab(0, "DONUT" + "_" + generateHashString())
                .editDashboard().addReportToDashboard("DONUT CHART REPORT").saveDashboard();
        exportedDashboardName = dashboardsPage.exportDashboardTabToPDF();
        resultComparePDF = comparePDF(exportedDashboardName);
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }
}
