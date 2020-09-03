package com.gooddata.qa.graphene.add;

import com.gooddata.qa.graphene.AbstractGeoPushpinTest;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.fragments.common.StatusBar;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_SUCCESS_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.*;

public class OnDashboardGeoPushpinTest extends AbstractGeoPushpinTest {

    private static final String ATTR_POPULATION = "population";
    private static final String ATTR_COUNTRY = "state";
    private static final String ATTR_GEO_CHART = "city";
    private static final String GEO_INSIGHT = "GeoPushpinInsight";
    private final String GEO_DASHBOARD = "GeoPushpinDashboard_" + generateHashString();
    private final String GEO_DASHBOARD_WITH_MORE_INSIGHT = "GeoDashboardWithMoreGeoInsight_" + generateHashString();
    private AnalysisPage analysisPage;
    private DashboardRestRequest dashboardRequest;

    private GeoPushpinChartPicker geoChart;
    private IndigoDashboardsPage indigoDashboard;
    private boolean resultComparePDF;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_GEO_CHART_ON_DASHBOARD_Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition()
                .setDashboardName(" ").setTabName(" ").setProjectName(" ").setReportName(" ");
        dashboardRequest.exportDashboardSetting(exportDashboardDefinition);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"geoPushpinProject"})
    public void createNewGeoPushpinChart() {
        analysisPage = initAnalysePage().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT)
                .addStack(ATTR_COUNTRY).waitForReportComputing()
                .saveInsight(GEO_INSIGHT);
    }

    @Test(dependsOnMethods = {"createNewGeoPushpinChart"}, groups = {"geoPushpinProject"})
    public void drillToGeoPushpinsChartOnKPIDashboards() {
        indigoDashboard = initIndigoDashboardsPage();
        indigoDashboard.addDashboard().changeDashboardTitle(GEO_DASHBOARD)
                .addInsight(GEO_INSIGHT);
        ConfigurationPanel configurationPanel =  indigoDashboard.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.disableDateFilter();
        configurationPanel.drillIntoInsight("Sum of " + ATTR_POPULATION, GEO_INSIGHT);
        indigoDashboard.saveEditModeWithWidgets().waitForWidgetsLoading();
        geoChart = GeoPushpinChartPicker.getInstance(browser);
        log.info("---Canvas map have the width is: " + geoChart.returnCanvasChart().getSize().getWidth());
        log.info("---Canvas map have the height is: " + geoChart.returnCanvasChart().getSize().getHeight());
        geoChart.hoverOnGeoPushpin(-250, -70);
        log.info("List tooltip value: "  + geoChart.getGeoPopupTooltipValue());
        List<String> tooltipValue = geoChart.getGeoPopupTooltipValue();
        geoChart.hoverAndClickOnGeoPushpin(-250, -70);
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        log.info("Title insight in dashboard: "  + drillModalDialog.getTitleInsight());
        log.info("List filter category legend: "  + drillModalDialog.getCategoryPushpinLegend());
        assertThat(tooltipValue, hasItems(drillModalDialog.getCategoryPushpinLegend()));
        assertEquals(drillModalDialog.getTitleInsight(), GEO_INSIGHT);
        drillModalDialog.close();
    }

    @Test(dependsOnMethods = {"drillToGeoPushpinsChartOnKPIDashboards"}, groups = {"geoPushpinProject"})
    public void getTooltipGeoChart() {
        geoChart.hoverOnGeoPushpin(-250, -70);
        assertTrue(geoChart.isGeoRenderChartDisplayed(), "Pushpin map should be have all data");
        assertTrue(geoChart.isGeoLegendDisplayed(), "Geo legend should be show on the top of map");
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
        assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
        assertTrue(geoChart.isZoomInAndZoomOutDisplayed(), "ZoomIn and ZoomOut btn should be displayed");
        geoChart.doubleClickOnZoomInBtn();
        geoChart.hoverOnGeoPushpin(-250, -70);
        assertFalse(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be hidden");
        geoChart.doubleClickOnZoomOutBtn();
        sleepTightInSeconds(2);
        indigoDashboard.switchToEditMode().waitForWidgetsLoading();
        geoChart.hoverOnGeoPushpin(-250, -70);
        assertFalse(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be hidden");
        assertFalse(geoChart.isZoomInAndZoomOutDisplayed(), "ZoomIn and ZoomOut btn should be hidden");
    }

    @Test(dependsOnMethods = {"getTooltipGeoChart"}, groups = {"geoPushpinProject"})
    public void applyFilterIntoGeoPushpinOnKD() {
        indigoDashboard.addAttributeFilter(ATTR_COUNTRY, "Delaware").waitForWidgetsLoading();
        assertEquals(geoChart.getAttrFilterOnPushpinLegend(), asList("Delaware"));
        indigoDashboard.deleteAttributeFilter(ATTR_COUNTRY).waitForWidgetsLoading();
        assertEquals(geoChart.getAttrFilterOnPushpinLegend(), asList("Delaware", "District of Columbia", "Washington",
                "Wisconsin"));
    }

    @Test(dependsOnMethods = {"applyFilterIntoGeoPushpinOnKD"}, groups = {"geoPushpinProject"})
    public void exportGeoPushpinToPdfFile() throws IOException {
        indigoDashboard.cancelEditModeWithoutChange().waitForWidgetsLoading();
        initIndigoDashboardsPage();
        indigoDashboard.exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(GEO_DASHBOARD).split("\n"));
        log.info("PDF: " + contents.toString());
        assertThat(contents, Matchers.hasItems("Delaware District of Columbia Washington Wisconsin",
                "Sum of population: 0 ... 18k ... 5.3M", GEO_INSIGHT));
        resultComparePDF = comparePDF(GEO_DASHBOARD + ".pdf");
        assertTrue(resultComparePDF, "Export new PDF file have been changed");
    }

    @Test(dependsOnMethods = {"exportGeoPushpinToPdfFile"}, groups = {"geoPushpinProject"})
    public void exportGeoPushpinToCsvFile() throws IOException {
        try {
            geoChart.hoverOnCanvasMapToExportFile();
            indigoDashboard.waitForWidgetsLoading().selectFirstWidget(Insight.class)
                    .exportTo(OptionalExportMenu.File.CSV);
            File exportCsvFile =getAndWaitForImportingFile(GEO_INSIGHT + ".csv");
            assertEquals(exportCsvFile.getName(), GEO_INSIGHT + ".csv");
            assertEquals(CSVUtils.readCsvFile(exportCsvFile),
                    CSVUtils.readCsvFile(getResourceAsFile("/" + PAYROLL_CSV + "/geoPushpinChart.csv")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(GEO_INSIGHT + ".csv")));
        }
    }

    @Test(dependsOnMethods = {"exportGeoPushpinToCsvFile"}, groups = {"geoPushpinProject"})
    public void exportGeoPushpinToXlsxFile() throws IOException {
        try {
            geoChart.hoverOnCanvasMapToExportFile();
            indigoDashboard.waitForWidgetsLoading().selectFirstWidget(Insight.class)
                    .exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).uncheckOption(ExportXLSXDialog.OptionalExport.CELL_MERGED)
                    .confirmExport();
            final File xlsxFile = getAndWaitForImportingFile(GEO_INSIGHT + ".xlsx");
            assertEquals(xlsxFile.getName(), GEO_INSIGHT + ".xlsx");
            assertEquals(XlsxUtils.excelFileToRead(xlsxFile.getPath(), 0), XlsxUtils.excelFileToRead(
                    getFilePathFromResource("/" + PAYROLL_CSV + "/geoPushpinChart.xlsx"), 0));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(GEO_INSIGHT + ".xlsx")));
        }
    }

    @Test(dependsOnMethods = {"exportGeoPushpinToXlsxFile"}, groups = {"geoPushpinProject"})
    public void exportGeoPushpinToCsvFileWithAttributeFilter() throws IOException {
        try {
            indigoDashboard.switchToEditMode().addAttributeFilter(ATTR_COUNTRY, "Delaware")
                    .saveEditModeWithWidgets();
            geoChart.hoverOnCanvasMapToExportFile();
            indigoDashboard.waitForWidgetsLoading().selectFirstWidget(Insight.class)
                    .exportTo(OptionalExportMenu.File.CSV);
            final File csvFile = getAndWaitForImportingFile(GEO_INSIGHT + ".csv");
            assertEquals(csvFile.getName(), GEO_INSIGHT + ".csv");
            assertEquals(CSVUtils.readCsvFile(csvFile),
                    CSVUtils.readCsvFile(getResourceAsFile("/" + PAYROLL_CSV + "/filterGeoPushpin.csv")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(GEO_INSIGHT + ".csv")));
        }
    }

    @Test(dependsOnMethods = {"exportGeoPushpinToCsvFileWithAttributeFilter"}, groups = {"geoPushpinProject"})
    public void exportGeoPushpinToXlsxFileWithAttributeFilter() throws IOException {
        try {
            geoChart.hoverOnCanvasMapToExportFile();
            indigoDashboard.waitForWidgetsLoading().selectFirstWidget(Insight.class)
                    .exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).uncheckOption(
                    ExportXLSXDialog.OptionalExport.CELL_MERGED).confirmExport();
            final File xlsxFile = getAndWaitForImportingFile(GEO_INSIGHT + ".xlsx");
            assertEquals(xlsxFile.getName(), GEO_INSIGHT + ".xlsx");
            assertEquals(XlsxUtils.excelFileToRead(xlsxFile.getPath(), 0), XlsxUtils.excelFileToRead(
                    getFilePathFromResource("/" + PAYROLL_CSV + "/filterGeoPushpin.xlsx"), 0));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(GEO_INSIGHT + ".xlsx")));
        }
    }

    @Test(dependsOnMethods = {"exportGeoPushpinToXlsxFileWithAttributeFilter"}, groups = {"geoPushpinProject"})
    public void testResizeInsightOnDashBoard(){
         indigoDashboard.switchToEditMode().resizeWidthOfWidget(ResizeBullet.TWELVE);
        assertEquals(waitForElementVisible(BY_SUCCESS_MESSAGE_BAR, browser).getText(),
                "The geo chart was automatically resized");
        indigoDashboard.saveEditModeWithWidgets();
        assertTrue(indigoDashboard.getWidgetFluidLayout(GEO_INSIGHT).getAttribute("class")
                        .contains("s-fluid-layout-column-width-" + ResizeBullet.TWELVE.getNumber()),
                "The width widget should be resized with maximum value");

        indigoDashboard.switchToEditMode().resizeWidthOfWidget(ResizeBullet.SIX).saveEditModeWithWidgets();
        assertTrue(indigoDashboard.getWidgetFluidLayout(GEO_INSIGHT).getAttribute("class")
                        .contains("s-fluid-layout-column-width-" + ResizeBullet.SIX.getNumber()),
                "The width widget should be resized with minimal value");
    }

    @Test(dependsOnMethods = {"testResizeInsightOnDashBoard"}, groups = {"geoPushpinProject"})
    public void exportDashboardHaveMoreGeoInsight() throws IOException {
        try {
            indigoDashboard.switchToEditMode().deleteAttributeFilter(ATTR_COUNTRY)
                    .changeDashboardTitle(GEO_DASHBOARD_WITH_MORE_INSIGHT).resizeWidthOfWidget(ResizeBullet.TWELVE)
                    .addInsight(GEO_INSIGHT).addInsight(GEO_INSIGHT).addInsight(GEO_INSIGHT).saveEditModeWithWidgets();
            indigoDashboard.exportDashboardToPDF();
            resultComparePDF = comparePDF(GEO_DASHBOARD_WITH_MORE_INSIGHT + ".pdf");
            assertTrue(resultComparePDF, "Export new PDF file have been changed");
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(GEO_DASHBOARD_WITH_MORE_INSIGHT + ".pdf")));
        }
    }

    private File getAndWaitForImportingFile(String fileName) {
        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + fileName);
        waitForExporting(exportFile);
        return exportFile;
    }
}
