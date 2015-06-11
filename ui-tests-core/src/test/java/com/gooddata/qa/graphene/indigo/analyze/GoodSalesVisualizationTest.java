package com.gooddata.qa.graphene.indigo.analyze;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesVisualizationTest extends AnalyticalDesignerAbstractTest {

    private static final String AMOUNT = "Amount";
    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    private static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    private static final String STAGE_NAME = "Stage Name";
    private static final String ACCOUNT = "Account";
    private static final String ACTIVITY_TYPE = "Activity Type";
    private static final String DEPARTMENT = "Department";

    private static final String EXPORT_ERROR_MESSAGE = "Visualization is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Demo-Visualization-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void disableExportForUnexportableVisualization() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(analysisPage.isExportToReportButtonEnabled());

        analysisPage.addCategory(STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStackBy(STAGE_NAME);
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(analysisPage.isExportToReportButtonEnabled());
        assertEquals(analysisPage.getExportToReportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void dontShowLegendWhenOnlyOneMetric() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT).withCategories(STAGE_NAME));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 8);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertFalse(report.isLegendVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInChartHasManyMetrics() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT, NUMBER_OF_ACTIVITIES));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsHorizontal());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInStackBy() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE)).addStackBy(DEPARTMENT);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());
    }

    @Test(dependsOnGroups = {"init"})
    public void showLegendForStackedChartWithOneSeries() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_WON_OPPS)).addStackBy(STAGE_NAME);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        report = analysisPage.getChartReport();
        assertTrue(report.isLegendVisible());
        legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");
    }

    @Test(dependsOnGroups = {"init"})
    public void resetSpecialReports() {
        initAnalysePage();
        analysisPage.resetToBlankState();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACCOUNT));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Too many data points to display");
        analysisPage.resetToBlankState();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(STAGE_NAME));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Visualization cannot be displayed");
        analysisPage.resetToBlankState();
    }
}
