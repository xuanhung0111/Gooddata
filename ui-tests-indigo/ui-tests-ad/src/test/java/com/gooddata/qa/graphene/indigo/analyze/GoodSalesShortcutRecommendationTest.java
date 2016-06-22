package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.function.Supplier;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesShortcutRecommendationTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Shortcut-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testColumnChartShortcut() {
        WebElement metric = analysisPageReact.getCataloguePanel()
                .searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        ChartReportReact report = analysisPageReact.drag(metric, recommendation)
                    .getChartReport();

        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertThat(report.getTrackersCount(), equalTo(4));
        checkingOpenAsReport("dragMetricToColumnChartShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"})
    public void testTrendShortcut() {
        WebElement metric = analysisPageReact.getCataloguePanel()
                .searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        ChartReportReact report = analysisPageReact.drag(metric, trendRecommendation)
                .getChartReport();

        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible("Activity"));
        assertThat(analysisPageReact.getFilterBuckets().getFilterText("Activity"), equalTo("Activity: Last 4 quarters"));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("dragMetricToTrendShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"})
    public void displayWhenDraggingFirstMetric() {
        WebElement metric = analysisPageReact.getCataloguePanel()
                .searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPageReact.drag(metric, trendRecommendation)
            .waitForReportComputing();

        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("displayWhenDraggingFirstMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFactUsingShortcut() {
        WebElement fact = analysisPageReact.getCataloguePanel()
                .searchAndGet(FACT_AMOUNT, FieldType.FACT);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        assertEquals(analysisPageReact.drag(fact, recommendation)
                .waitForReportComputing()
                .getMetricsBucket()
                .getMetricConfiguration("Sum of " + FACT_AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        assertEquals(analysisPageReact.getChartReport().getYaxisTitle(), "Sum of " + FACT_AMOUNT);

        analysisPageReact.resetToBlankState();

        recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPageReact.drag(fact, recommendation)
            .waitForReportComputing();
        assertEquals(analysisPageReact.getFilterBuckets().getDateFilterText(), "Closed: Last 4 quarters");
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
        assertEquals(analysisPageReact.getAttributesBucket().getSelectedGranularity(), "Quarter");
        checkingOpenAsReport("createSimpleMetricFromFactUsingShortcut");
    }
}
