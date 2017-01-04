package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class MetricModifiersTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Modifiers-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_turned_off_when_second_metric_is_added() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing();

        analysisPage.getFilterBuckets().configDateFilter("Last year");

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        assertFalse(analysisPage.waitForReportComputing()
            .addMetric(METRIC_QUOTA)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .isPopSelected());
    }
}
