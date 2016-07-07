package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesContributionRecommendationTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Contribution-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleContribution() {
        ChartReport report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPageReact.waitForReportComputing().isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 4);

        MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration("% " + METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("testSimpleContribution");
    }

    @Test(dependsOnGroups = {"init"})
    public void testAnotherApproachToShowContribution() {
        ChartReport report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACTIVITY_TYPE).apply();
        analysisPageReact.waitForReportComputing();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        checkingOpenAsReport("testAnotherApproachToShowContribution");
    }
}
