package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

import java.text.ParseException;

public class MetricModifiersTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Metric-Modifiers-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createQuotaMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_turned_off_when_second_metric_is_added() throws ParseException {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing();

        analysisPage.getFilterBuckets().configDateFilter("01/01/2016", "01/01/2017");

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
