package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

import java.util.Arrays;

public class GoodSalesTrendingRecommendationTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Trending-Recommendation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOverrideDateFilter() {
        final FiltersBucket FiltersBucketReact = initAnalysePage().getFilterBuckets();

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP)
            .addDateFilter();
        assertEquals(parseFilterText(FiltersBucketReact.getFilterText("Activity")), Arrays.asList("Activity", "All time"));
        FiltersBucketReact.configDateFilter("Last 12 months");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();
        analysisPage.waitForReportComputing();
        assertEquals(parseFilterText(FiltersBucketReact.getFilterText("Activity")), Arrays.asList("Activity", "Last 4 quarters"));
        assertTrue(report.getTrackersCount() >= 1, "Trackers should display");
        checkingOpenAsReport("testOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyParameter() {
        ChartReport report = initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP)
                .waitForReportComputing().getChartReport();
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_SNAPSHOT_BOP)
                .expandConfiguration();

        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");

        TrendingRecommendation trendingRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND);
        trendingRecommendation.select("Month").apply();
        analysisPage.waitForReportComputing();
        assertThat(analysisPage.getAttributesBucket().getItemNames(), hasItem(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"), "Filter should display");
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText("Activity")), Arrays.asList("Activity", "Last 4 quarters"));
        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation shouldn't display");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        checkingOpenAsReport("applyParameter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void displayInColumnChartWithOnlyMetric() {
        ChartReport report = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");

        analysisPage.addFilter(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(browser.findElements(RecommendationContainer.LOCATOR).size(), 0);

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation shouldn't display");
        checkingOpenAsReport("displayInColumnChartWithOnlyMetric");
    }
}
