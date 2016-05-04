package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class AnalyticalDesignerSanityTest extends AnalyticalDesignerAbstractTest {

    private static final String EXPECTED = "Expected";
    private static final String REMAINING_QUOTA = "Remaining Quota";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Sanity-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testWithAttribute() {
        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(asList(ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragMetricToColumnChartShortcutPanel() {
        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        ChartReport report = analysisPage.drag(metric, recommendation)
                    .getChartReport();

        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));
        checkingOpenAsReport("dragMetricToColumnChartShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleContribution() {
        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .getChartReport();
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 4);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration("% " + NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        analysisPage.replaceAttribute(ACTIVITY_TYPE, DEPARTMENT);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("testSimpleContribution");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleComparison() {
        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ACTIVITY_TYPE).apply();
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.replaceAttribute(ACTIVITY_TYPE, DEPARTMENT);
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(DEPARTMENT), DEPARTMENT + ": All");
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testSimpleComparison");
    }

    @Test(dependsOnGroups = {"init"})
    public void displayWhenDraggingFirstMetric() {
        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPage.drag(metric, trendRecommendation)
            .waitForReportComputing();

        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("displayWhenDraggingFirstMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void exportCustomDiscovery() {
        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getPageHeader()
                .isExportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        sleepTight(2000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        assertThat(content, equalTo(analysisContent));

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
        }
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnDateAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .addDateFilter()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");

        assertEquals(filtersBucket.configDateFilter("This year")
                .getFilterText("Activity"), "Activity: This year");
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("filterOnDateAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimplePoP() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate();
        assertTrue(analysisPage.getFilterBuckets()
                .isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: All time");
        ChartReport report = analysisPage.getChartReport();
        assertThat(report.getTrackersCount(), equalTo(6));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));

        analysisPage.addMetric(SNAPSHOT_BOP);
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES, SNAPSHOT_BOP));
        checkingOpenAsReport("testSimplePoP");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceMetricByNewOne() {
        final MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(AMOUNT)));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES, AMOUNT)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, EXPECTED);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(EXPECTED, AMOUNT)));

        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, REMAINING_QUOTA);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));

        analysisPage.undo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.redo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));
        checkingOpenAsReport("replaceMetricByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void dropAttributeToReportHaveOneMetric() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE).waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPage.addStack(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }
}
