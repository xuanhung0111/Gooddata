package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricFormat;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesMetricNumberFormatTest extends AbstractAnalyseTest {

    private String percentOfGoalUri;
    private String oldPercentOfGoalMetricFormat;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Metric-Number-Format-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createPercentOfGoalMetric();
        percentOfGoalUri = getMdService().getObjUri(getProject(), Metric.class, title(METRIC_PERCENT_OF_GOAL));
        oldPercentOfGoalMetricFormat = getMetricFormat(METRIC_PERCENT_OF_GOAL);
    }

    @DataProvider(name = "formattingProvider")
    public Object[][] formattingProvider() {
        return new Object[][] {
            {Formatter.BARS, null, true},
            {Formatter.GDC, "GDC11.61", false},
            {Formatter.DEFAULT, "11.61", false},
            {Formatter.TRUNCATE_NUMBERS, "$12", false},
            {Formatter.COLORS, "$11.61", false},
            {Formatter.UTF_8, Formatter.UTF_8.toString(), false}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "formattingProvider", groups = {"metricFormat"})
    public void testMetricNumberFormat(Formatter format, String expectedValue, boolean compareFormat)
            throws ParseException, JSONException, IOException {
        changeMetricFormat(getRestApiClient(), percentOfGoalUri, format.toString());

        try {
            verifyFormatInAdReport(format, expectedValue, compareFormat);

            analysisPage.exportReport();
            String currentWindowHandle = browser.getWindowHandle();
            for (String handle : browser.getWindowHandles()) {
                if (!handle.equals(currentWindowHandle))
                    browser.switchTo().window(handle);
            }
            waitForAnalysisPageLoaded(browser);
            waitForFragmentVisible(reportPage);
            checkRedBar(browser);

            verifyFormatInReportPage(format, expectedValue, compareFormat);

            String report = format.name() + " Report";
            reportPage.setReportName(report)
                .finishCreateReport();
            sleepTightInSeconds(3);

            verifyFormatInDashboard(report, format, expectedValue, compareFormat);

            browser.close();
            browser.switchTo().window(currentWindowHandle);
        } finally {
            changeMetricFormat(getRestApiClient(), percentOfGoalUri, oldPercentOfGoalMetricFormat);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "formattingProvider", groups = {"chartLabel"})
    public void checkDataLabelShowOnBarChart(Formatter format, String expectedValue, boolean compareFormat)
            throws ParseException, JSONException, IOException {
        changeMetricFormat(getRestApiClient(), percentOfGoalUri, format.toString());

        try {
            String dataLabel = analysisPage.addMetric(METRIC_PERCENT_OF_GOAL)
                    .addAttribute(ATTR_IS_WON)
                    .waitForReportComputing()
                    .changeReportType(ReportType.BAR_CHART)
                    .waitForReportComputing()
                    .getChartReport()
                    .getDataLabels()
                    .get(0);

            takeScreenshot(browser,
                    "Check data label on bar chart with metric format " + format.name(), getClass());

            if (compareFormat) {
                assertThat(format.toString(), containsString(dataLabel));
            } else {
                assertEquals(dataLabel, expectedValue);
            }
        } finally {
            changeMetricFormat(getRestApiClient(), percentOfGoalUri, oldPercentOfGoalMetricFormat);
        }
    }

    private String getMetricFormat(String metric) {
        return initMetricPage().openMetricDetailPage(metric).getMetricFormat();
    }

    private void verifyFormatInAdReport(Formatter format, String expectedValue, boolean compareFormat) {
        List<List<String>> tooltip = analysisPage.addMetric(METRIC_PERCENT_OF_GOAL)
            .addAttribute(ATTR_IS_WON)
            .waitForReportComputing()
            .getChartReport()
            .getTooltipTextOnTrackerByIndex(0);

        assertEquals(tooltip.get(0), asList(ATTR_IS_WON, "true"));
        assertEquals(tooltip.get(1).get(0), METRIC_PERCENT_OF_GOAL);
        if (compareFormat) {
            assertTrue(format.toString().contains(tooltip.get(1).get(1)));
        } else {
            assertEquals(tooltip.get(1).get(1), expectedValue);
        }
    }

    private void verifyFormatInReportPage(Formatter format, String expectedValue, boolean compareFormat) {
        reportPage.selectReportVisualisation(ReportTypes.TABLE);
        waitForAnalysisPageLoaded(browser);
        String actualValue = reportPage.getTableReport().getRawMetricValues().get(0);
        if (compareFormat) {
            assertTrue(format.toString().contains(actualValue));
        } else {
            assertEquals(actualValue, expectedValue);
        }
    }

    private void verifyFormatInDashboard(String reportName, Formatter format, String expectedValue,
            boolean compareFormat) {
        String dashboard = format.name() + " Dashboard";

        try {
            initDashboardsPage();
            dashboardsPage.addNewDashboard(dashboard);

            try {
                dashboardsPage.editDashboard();
                dashboardsPage.getDashboardEditBar().addReportToDashboard(reportName);
                dashboardsPage.getDashboardEditBar().saveDashboard();
                String actualValue = dashboardsPage.getContent()
                        .getLatestReport(TableReport.class).getRawMetricValues().get(0);
                if (compareFormat) {
                    assertTrue(format.toString().contains(actualValue));
                } else {
                    assertEquals(actualValue, expectedValue);
                }
            } finally {
                dashboardsPage.selectDashboard(dashboard);
                dashboardsPage.deleteDashboard();
            }
        } finally {
            initReportsPage()
                .deleteReports(reportName);
        }
    }
}
