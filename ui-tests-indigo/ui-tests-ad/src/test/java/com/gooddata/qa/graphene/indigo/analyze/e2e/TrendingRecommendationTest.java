package com.gooddata.qa.graphene.indigo.analyze.e2e;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.*;

public class TrendingRecommendationTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Trending-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_add_date_item_with_proper_granularity_to_category_bucket() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_month_in_trending_widget_and_hide_it() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).select("Month").apply();
        analysisPageReact.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void ashould_have_quarter_selected_after_resetting_a_widget() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).select("Month");

        analysisPageReact.resetToBlankState()
                .addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        assertEquals(new Select(waitForElementVisible(cssSelector(".s-date-granularity-switch"), browser))
                .getFirstSelectedOption().getAttribute("value"), "GDC.time.quarter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_select_last_4_quarters_on_date_filter_when_trending() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPageReact.waitForReportComputing();
        assertTrue(analysisPageReact.getFilterBuckets().isDateFilterVisible());
        assertThat(waitForElementVisible(cssSelector(
                ".s-date-filter label"), browser).getText(),
                containsString("Last 4 quarters"));
    }
}
