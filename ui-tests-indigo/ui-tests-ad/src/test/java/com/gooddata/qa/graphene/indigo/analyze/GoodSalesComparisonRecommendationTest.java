package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesComparisonRecommendationTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Comparison-Recommendation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testOverrideDateFilter() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addDateFilter()
            .getFilterBuckets()
            .configDateFilter("Last year");
        ChartReportReact report = analysisPageReact.waitForReportComputing().getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText("Activity"), "Activity: This month");
        analysisPageReact.waitForReportComputing();
        if (analysisPageReact.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPageReact.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimpleComparison() {
        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACTIVITY_TYPE).apply();
        assertTrue(analysisPageReact.waitForReportComputing().getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(ATTR_DEPARTMENT));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ": All");
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testSimpleComparison");
    }

    @Test(dependsOnGroups = {"init"})
    public void testComparisonAndPoPAttribute() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACTIVITY_TYPE).apply();
        analysisPageReact.waitForReportComputing();
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        analysisPageReact.waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: This month");
        if (analysisPageReact.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPageReact.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testComparisonAndPoPAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSimplePoP() throws ParseException {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .getFilterBuckets()
                .configDateFilter("01/01/2012", "12/31/2012");

        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ATTR_ACTIVITY));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(ATTR_ACTIVITY),
                "Activity: Jan 1, 2012 - Dec 31, 2012");
        ChartReportReact report = analysisPageReact.waitForReportComputing().getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer = 
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPageReact.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        analysisPageReact.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("testSimplePoP");
    }

    @Test(dependsOnGroups = {"init"})
    public void testAnotherApproachToShowPoP() {
        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();
        analysisPageReact.waitForReportComputing();

        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testAnotherApproachToShowPoP");
    }

    @Test(dependsOnGroups = {"init"})
    public void testRecommendationDisplayingWithDateFilter() throws ParseException {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();
        takeScreenshot(browser, "No-Recommendation-Displaying-With-All-Time-Filter", getClass());
        assertFalse(isElementPresent(RecommendationContainer.LOCATOR, browser),
                "Compare Recommendation step is displayed");

        analysisPageReact.getFilterBuckets().configDateFilter("01/01/2010", "12/31/2010");
        analysisPageReact.waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));

        takeScreenshot(browser, "Recommendation-Displaying-With-Edited-Date-Filter", getClass());
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare Recommendation step is not displayed");
    }
}
