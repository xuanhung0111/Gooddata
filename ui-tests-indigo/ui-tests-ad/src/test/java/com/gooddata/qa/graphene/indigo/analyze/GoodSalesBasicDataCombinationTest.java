package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.collect.Lists;

public class GoodSalesBasicDataCombinationTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Basic-Data-Combination-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void checkSeriesStateTransitions() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addDate()
                .getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addMetric(QUOTA).waitForReportComputing();
        sleepTight(3000);
        assertTrue(report.getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertEquals(report.getLegends(), asList(NUMBER_OF_ACTIVITIES, QUOTA));
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(NUMBER_OF_ACTIVITIES, QUOTA));

        analysisPage.addMetric(SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(NUMBER_OF_ACTIVITIES, QUOTA, SNAPSHOT_BOP));
        checkingOpenAsReport("checkSeriesStateTransitions");
    }

    @Test(dependsOnGroups = {"init"})
    public void checkMetricFormating() throws ParseException, JSONException, IOException {
        initMetricPage();

        waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();
        String metricUri = format("/gdc/md/%s/obj/14636", testParams.getProjectId());
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat + "[red]");

        try {
            initAnalysePage();

            ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
            assertEquals(report.getTrackersCount(), 1);
            sleepTight(2000);
            List<String> dataLabels = report.getDataLabels();
            assertEquals(dataLabels.size(), 1);

            TableReport tableReport = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
            assertEquals(tableReport.getFormatFromValue(), "color: rgb(255, 0, 0);");
        } finally {
            DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat);
            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
            assertEquals(metricDetailPage.getMetricFormat(), oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkReportContentWhenAdd3Metrics1Attribute() {
        initAnalysePage();

        TableReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addMetric(QUOTA)
                .addMetric(SNAPSHOT_BOP)
                .addAttribute(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getTableReport();
        sleepTight(3000);
        List<List<String>> analysisContent = report.getContent();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        assertEquals(analysisContent, getTableContentFromReportPage(Graphene.createPageFragment(
                com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                waitForElementVisible(id("gridContainerTab"), browser))));
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void checkShowPercentAndLegendColor() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .getChartReport();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration()
                .showPercents();
        sleepTight(5000);
        assertTrue(report.getDataLabels().get(0).endsWith("%"));

        analysisPage.addMetric(QUOTA);
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isShowPercentSelected());

        assertEquals(report.getLegendColors(), asList("rgb(0, 131, 255)", "rgb(0, 192, 142)"));
        checkingOpenAsReport("checkShowPercentAndLegendColor");
    }

    private List<List<String>> getTableContentFromReportPage(
            com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport) {
        List<List<String>> content = Lists.newArrayList();
        List<String> attributes = tableReport.getAttributeElements();
        List<String> metrics = tableReport.getRawMetricElements();
        int totalAttributes = attributes.size();
        int i = 0;
        for (String attr: attributes) {
            List<String> row = Lists.newArrayList(attr);
            for (int k = i; k < metrics.size(); k += totalAttributes) {
                row.add(metrics.get(k));
            }
            content.add(row);
            i++;
        }

        return content;
    }
}
