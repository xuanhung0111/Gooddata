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
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesComparisonRecommendationTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Comparison-Recommendation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfActivitiesMetric();
        createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOverrideDateFilter() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addDateFilter()
            .getFilterBuckets()
            .configDateFilter("Last year");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
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
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSimpleComparison() {
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
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
        assertTrue(analysisPage.waitForReportComputing().getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nAll");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ATTR_DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ":\nAll");
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testSimpleComparison");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testComparisonAndPoPAttribute() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
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
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nAll");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucketReact.getDateFilterText(), "Activity: This month");
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPage.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ":\nAll");
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testComparisonAndPoPAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSimplePoP() throws ParseException {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .getFilterBuckets()
                .configDateFilter("01/01/2012", "12/31/2012");

        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ATTR_ACTIVITY));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY),
                "Activity: Jan 1, 2012 - Dec 31, 2012");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer = 
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("testSimplePoP");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAnotherApproachToShowPoP() {
        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();
        analysisPage.waitForReportComputing();

        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testAnotherApproachToShowPoP");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRecommendationDisplayingWithDateFilter() throws ParseException {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();
        takeScreenshot(browser, "No-Recommendation-Displaying-With-All-Time-Filter", getClass());
        assertFalse(isElementPresent(RecommendationContainer.LOCATOR, browser),
                "Compare Recommendation step is displayed");

        analysisPage.getFilterBuckets().configDateFilter("01/01/2010", "12/31/2010");
        analysisPage.waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));

        takeScreenshot(browser, "Recommendation-Displaying-With-Edited-Date-Filter", getClass());
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare Recommendation step is not displayed");
    }
}
