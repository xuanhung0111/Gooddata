package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesChartLegendTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Chart-Legend-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void checkShowPercentAndLegendColor() {
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getChartReport();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration()
                .showPercents();
        sleepTight(5000);
        assertTrue(report.getDataLabels().get(0).endsWith("%"));

        analysisPage.addMetric(METRIC_QUOTA).waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isShowPercentSelected());

        assertEquals(report.getLegendColors(), asList("rgb(20, 178, 226)", "rgb(0, 193, 141)"));
        checkingOpenAsReport("checkShowPercentAndLegendColor");
    }

    @Test(dependsOnGroups = {"init"})
    public void dontShowLegendWhenOnlyOneMetric() {
        ChartReport report = analysisPage.addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_STAGE_NAME)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 8);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertFalse(report.isLegendVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInChartHasManyMetrics() {
        ChartReport report = analysisPage.addMetric(METRIC_AMOUNT).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsHorizontal());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInStackBy() {
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
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
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_WON_OPPS)
                .addStack(ATTR_STAGE_NAME)
                .waitForReportComputing()
                .getChartReport();
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
}
