package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ComparisonRecommendationTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Comparison-Recommendation-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_apply_first_attribute_and_hide_recommendation() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ATTR_ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison"), browser),
                "Recommendation comparision shouldn't be present");
        assertThat(analysisPage.getAttributesBucket().getItemNames(), hasItem(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_apply_first_attribute_and_show_other_recommendations() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ATTR_ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();

        waitForElementVisible(RecommendationContainer.LOCATOR, browser);

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison"), browser),
                "Recommendation comparision shouldn't be present");
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ATTR_ACTIVITY_TYPE));
        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
        assertTrue(isElementPresent(cssSelector(".s-recommendation-contribution"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_have_initial_value_selected_after_resetting_report() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        String initialValue = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ATTR_ACTIVITY_TYPE);
        String value = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        analysisPage.resetToBlankState();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        String resetValue = getValueFrom(".s-recommendation-comparison .s-attribute-switch");
        assertEquals(resetValue, initialValue);
        assertNotEquals(resetValue, value);
    }

    private String getValueFrom(String locator) {
        return new Select(waitForElementVisible(cssSelector(locator), browser))
            .getFirstSelectedOption()
            .getAttribute("value");
    }
}
