package com.gooddata.qa.graphene.indigo.analyze;

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
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesShortcutRecommendationTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Shortcut-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testColumnChartShortcut() {
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
    public void testTrendShortcut() {
        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        ChartReport report = analysisPage.drag(metric, trendRecommendation)
                .getChartReport();

        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertThat(analysisPage.getFilterBuckets().getFilterText("Activity"), equalTo("Activity: Last 4 quarters"));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("dragMetricToTrendShortcutPanel");
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
    public void createSimpleMetricFromFactUsingShortcut() {
        WebElement fact = analysisPage.getCataloguePanel()
                .searchAndGet(AMOUNT, FieldType.FACT);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        assertEquals(analysisPage.drag(fact, recommendation)
                .waitForReportComputing()
                .getMetricsBucket()
                .getMetricConfiguration("Sum of " + AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), "Sum of " + AMOUNT);

        analysisPage.resetToBlankState();

        recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPage.drag(fact, recommendation)
            .waitForReportComputing();
        assertEquals(analysisPage.getFilterBuckets().getDateFilterText(), "Closed: Last 4 quarters");
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
        assertEquals(analysisPage.getAttributesBucket().getSelectedGranularity(), "Quarter");
        checkingOpenAsReport("createSimpleMetricFromFactUsingShortcut");
    }
}