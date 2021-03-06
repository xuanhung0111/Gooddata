package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class GoodSalesComparisonRecommendationTest extends AbstractAnalyseTest {


    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Comparison-Recommendation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOverrideDateFilter() throws ParseException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addDateFilter()
            .getFilterBuckets()
            .configDateFilter("01/01/2016", "01/01/2017");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText("Activity")),
                Arrays.asList("Activity\n:\nThis month\nCompare (all) to", "Same period (SP) previous year"));
        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPage.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testComparisonAndAttribute() {
        final FiltersBucket filtersBucketReact = initAnalysePage().changeReportType(ReportType.COLUMN_CHART).getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();
        assertThat(analysisPage.getAttributesBucket().getItemNames(), hasItem(ATTR_ACTIVITY_TYPE));

        List<String> parsedFilterTexts = parseFilterText(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE));
        assertEquals(parsedFilterTexts, Arrays.asList(ATTR_ACTIVITY_TYPE, "All"));
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        analysisPage.waitForReportComputing();

        List<String> dateFilterTexts = parseFilterText(filtersBucketReact.getDateFilterText());
        assertEquals(dateFilterTexts, Arrays.asList("Activity\n:\nThis month\nCompare (all) to",
                "Same period (SP) previous year"));
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Error message: " + analysisPage.getExplorerMessage());
            log.info("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(ATTR_DEPARTMENT)), Arrays.asList(ATTR_DEPARTMENT, "All"));
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("testComparisonAndAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSimpleSamePeriodComparison() throws ParseException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .getFilterBuckets()
                .configDateFilter("01/01/2012", "12/31/2012");

        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ATTR_ACTIVITY), ATTR_ACTIVITY + " filter should display");
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY)),
                Arrays.asList("Activity", "Jan 1, 2012 - Dec 31, 2012"));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        legends = report.getLegends();
        assertEquals(legends.size(), 4);
        assertEquals(legends, asList(
                METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO,
                METRIC_NUMBER_OF_ACTIVITIES,
                METRIC_SNAPSHOT_BOP_YEAR_AGO,
                METRIC_SNAPSHOT_BOP
        ));
        checkingOpenAsReport("testSimpleSamePeriodComparison");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAnotherApproachToShowCompare() {
        ChartReport report = initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_SNAPSHOT_BOP)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();
        analysisPage.waitForReportComputing();

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"), "Filter should displays");
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText("Activity")), Arrays.asList("Activity", "Last 4 quarters"));

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");
        checkingOpenAsReport("testAnotherApproachToShowCompare");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRecommendationDisplayingWithDateFilter() throws ParseException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate().waitForReportComputing();
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
