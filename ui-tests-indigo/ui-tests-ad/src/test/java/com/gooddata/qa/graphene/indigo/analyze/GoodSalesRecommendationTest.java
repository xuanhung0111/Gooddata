package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesRecommendationTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimplePoP() {
        initAnalysePage();

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
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testAnotherApproachToShowPoP");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleContribution() {
        initAnalysePage();

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
    public void testAnotherApproachToShowContribution() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ACTIVITY_TYPE).apply();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        checkingOpenAsReport("testAnotherApproachToShowContribution");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleComparison() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ACTIVITY_TYPE).apply();
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.replaceAttribute(ACTIVITY_TYPE, DEPARTMENT);
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(DEPARTMENT), DEPARTMENT + ": All");
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testSimpleComparison");
    }

    @Test(dependsOnGroups = {"init"})
    public void testComparisonAndPoPAttribute() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(ACTIVITY_TYPE));
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: This month");
        if (analysisPage.isExplorerMessageVisible()) {
            System.out.print("Error message: ");
            System.out.println(analysisPage.getExplorerMessage());
            System.out.println("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));

        analysisPage.replaceAttribute(ACTIVITY_TYPE, DEPARTMENT);
        assertEquals(filtersBucket.getFilterText(DEPARTMENT), DEPARTMENT + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testComparisonAndPoPAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void supportParameter() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .getChartReport();
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        TrendingRecommendation trendingRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND);
        trendingRecommendation.select("Month").apply();
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isPopEnabled());
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("supportParameter");
    }

    @Test(dependsOnGroups = {"init"})
    public void displayInColumnChartWithOnlyMetric() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.addFilter(ACTIVITY_TYPE);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("displayInColumnChartWithOnlyMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();

        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPage.drag(metric, trendRecommendation);

        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("displayWhenDraggingFirstMetric");
    }
}
