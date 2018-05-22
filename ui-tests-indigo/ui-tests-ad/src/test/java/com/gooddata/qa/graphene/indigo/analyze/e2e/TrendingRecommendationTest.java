package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class TrendingRecommendationTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Trending-Recommendation-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_add_date_item_with_proper_granularity_to_category_bucket() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_apply_month_in_trending_widget_and_hide_it() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).select("Month").apply();
        analysisPage.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void ashould_have_quarter_selected_after_resetting_a_widget() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).select("Month");

        analysisPage.resetToBlankState()
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-granularity-switch"), browser))
                .getFirstSelectedOption().getAttribute("value"), "GDC.time.quarter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_select_last_4_quarters_on_date_filter_when_trending() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getFilterBuckets().isDateFilterVisible());
        assertThat(waitForElementVisible(cssSelector(
                ".s-date-filter .s-attribute-filter-label"), browser).getText(),
                containsString("Last 4 quarters"));
    }
}
