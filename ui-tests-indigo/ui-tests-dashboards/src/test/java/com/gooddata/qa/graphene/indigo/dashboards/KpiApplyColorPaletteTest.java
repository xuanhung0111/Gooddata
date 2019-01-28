package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_AND_OPP_FIRST_SNAPSHOT_BY_TYPE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.gooddata.qa.browser.BrowserUtils;

import java.util.Arrays;
import java.util.List;

public class KpiApplyColorPaletteTest extends GoodSalesAbstractTest {
    private static final String KPI_DASHBOARD = "KPI_Dashboard";
    private static final String SINGLE_METRIC_APPLY_COLOR_PALETTE = "Single_Metric_Apply_Color_Palette";
    private static final String MULTI_METRIC_APPLY_COLOR_PALETTE = "Multi_Metric_Apply_Color_Palette";
    private static final String RENAMED_TEST_INSIGHT = "Renamed_Test_Insight";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private static List<Pair<String, ColorPalette>> listColorPalettes = Arrays.asList(Pair.of("guid1", ColorPalette.RED),
            Pair.of("guid2", ColorPalette.GREEN), Pair.of("guid3", ColorPalette.BLUE), Pair.of("guid4", ColorPalette.YELLOW));

    private String sourceProjectId;
    private String targetProjectId;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Kpi-Apply-Color-Palette-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createOppFirstSnapshotMetric();
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));
    }

    public void deleteIndigoRestRequestColorsPalette() {
        IndigoRestRequest indigoRestDeleteRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestDeleteRequest.deleteColorsPalette();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsightsApplyColorsPalette() {
        AnalysisPage analysisPage = initAnalysePage();
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getMetricsBucket();
        analysisPage.saveInsight(SINGLE_METRIC_APPLY_COLOR_PALETTE);

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.saveInsight(MULTI_METRIC_APPLY_COLOR_PALETTE);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testKpiAddInsightWithSingleMetricApplyColorsPalette() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(SINGLE_METRIC_APPLY_COLOR_PALETTE)
                .waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .clickDashboardBody();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.RED.toString());
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testKpiAddInsightWithMultiMetricApplyColorsPalette() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(MULTI_METRIC_APPLY_COLOR_PALETTE)
                .waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .clickDashboardBody();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testKpiAddInsightFilterApplyColorsPalette() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(MULTI_METRIC_APPLY_COLOR_PALETTE).waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME).clickDashboardBody()
                .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales").waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));

    }

    @Test(dependsOnMethods = {"testKpiAddInsightFilterApplyColorsPalette"})
    public void testKpiChangeNameInsightApplyColorsPalette() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard()
                .addInsight(MULTI_METRIC_APPLY_COLOR_PALETTE)
                .waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .clickDashboardBody()
                .saveEditModeWithWidgets();
        indigoDashboardsPage.switchToEditMode()
                .getLastWidget(Insight.class)
                .clickOnContent()
                .setHeadline(RENAMED_TEST_INSIGHT);
        indigoDashboardsPage.saveEditModeWithWidgets();
        indigoDashboardsPage.switchToEditMode()
                .changeDashboardTitle(KPI_DASHBOARD)
                .saveEditModeWithWidgets();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"testKpiChangeNameInsightApplyColorsPalette"})
    public void testExportAndImportProject() {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
                true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                    .selectKpiDashboard(KPI_DASHBOARD).waitForWidgetsLoading();
            Screenshots.takeScreenshot(browser, "export import project apply color palette in "
                    + RENAMED_TEST_INSIGHT, getClass());
            assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = "testExportAndImportProject")
    public void testPartialExportAndImportReport() throws Throwable {
        String exportToken = exportPartialProject(
                getReportCreator().createActivitiesAndOppFirstSnapshotByTypeReport(), DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        deleteIndigoRestRequestColorsPalette();
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            BrowserUtils.zoomBrowser(browser);
            browser.navigate().refresh();
            ReportPage reportPage = initReportsPage().openReport(REPORT_ACTIVITIES_AND_OPP_FIRST_SNAPSHOT_BY_TYPE)
                    .waitForReportExecutionProgress();
            reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress();
            takeScreenshot(browser, "testPartialExportAndImportReport", getClass());
            assertNotEquals(reportPage.checkColorColumn(0), ColorPalette.RED.toString());
            assertNotEquals(reportPage.getReportLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
            BrowserUtils.resetZoomBrowser(browser);
        }
    }
}
