package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesTrendingRecommendationTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Trending-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testOverrideDateFilter() {
        final FiltersBucket FiltersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDateFilter();
        assertEquals(FiltersBucketReact.getFilterText("Activity"), "Activity: All time");
        FiltersBucketReact.configDateFilter("Last 12 months");
        ChartReport report = analysisPageReact.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();
        analysisPageReact.waitForReportComputing();
        assertEquals(FiltersBucketReact.getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("testOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyParameter() {
        ChartReport report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        final MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
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
        analysisPageReact.waitForReportComputing();
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isPopEnabled());
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("applyParameter");
    }

    @Test(dependsOnGroups = {"init"})
    public void displayInColumnChartWithOnlyMetric() {
        ChartReport report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPageReact.addFilter(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPageReact.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPageReact.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("displayInColumnChartWithOnlyMetric");
    }
}
