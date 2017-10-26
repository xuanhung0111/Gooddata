package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesPopComparisonTest extends AbstractAnalyseTest {

    private static final String WEEK_GRANULARITY = "Week (Sun-Sat)";
    private static final String MONTH_GRANULARITY = "Month";

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfActivitiesMetric();
        createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyWeekGranularityToHidePopComparison() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();

        AttributesBucket attributeBucket = analysisPage.getAttributesBucket();
        attributeBucket.changeGranularity(WEEK_GRANULARITY);

        MetricsBucket metricsBucket = analysisPage.waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration();
        Screenshots.takeScreenshot(browser, "applyWeekGranularityToHidePopComparison-apply-week-granularity", getClass());

        assertFalse(metricsBucket.getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                        .expandConfiguration().isPopEnabled(),
                "Pop comparison state is not disabled after changing to week granularity");

        attributeBucket.changeGranularity(MONTH_GRANULARITY);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser, "applyWeekGranularityToHidePopComparison-apply-month-granularity", getClass());

        assertTrue(metricsBucket.getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                        .expandConfiguration().isPopEnabled(),
                "Pop comparison state is not enabled after removing week granularity");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyWeekGranularityToHideCompareRecommendation() {
        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP);

        RecommendationContainer container = Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        container.getRecommendation(RecommendationStep.SEE_TREND).apply();

        AttributesBucket attributesBucket = analysisPage.waitForReportComputing().getAttributesBucket();
        attributesBucket.changeGranularity(WEEK_GRANULARITY);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser,
                "applyWeekGranularityToHideCompareRecommendation-hide-recommendation", getClass());

        assertFalse(isElementVisible(RecommendationContainer.LOCATOR, browser), "Recommendation container is not empty");

        attributesBucket.changeGranularity(MONTH_GRANULARITY);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser,
                "applyWeekGranularityToHideCompareRecommendation-show-recommendation", getClass());

        assertTrue(container.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation is not visible after changing to month granularity");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enablePopToHideWeekGranularity() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration().showPop();

        assertFalse(analysisPage.waitForReportComputing().getAttributesBucket().getAllGranularities()
                .stream().anyMatch(WEEK_GRANULARITY::equals), "week granularity is not hidden");

        metricConfiguration.hidePop();
        assertTrue(analysisPage.waitForReportComputing().getAttributesBucket().getAllGranularities()
                .stream().anyMatch(WEEK_GRANULARITY::equals), "week granularity is not displayed");
    }
}