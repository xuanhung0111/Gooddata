package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesComparisonRecommendationTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Comparison-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testOverrideDateFilter() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .addDateFilter()
            .getFilterBuckets()
            .configDateFilter("Last year");
        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: This month");
        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPage.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testOverrideDateFilter");
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
    public void testComparisonAndPoPAttribute() {
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
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ACTIVITY_TYPE));
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: This month");
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPage.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
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
    public void testAnotherApproachToShowPoP() {
        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testAnotherApproachToShowPoP");
    }
}
