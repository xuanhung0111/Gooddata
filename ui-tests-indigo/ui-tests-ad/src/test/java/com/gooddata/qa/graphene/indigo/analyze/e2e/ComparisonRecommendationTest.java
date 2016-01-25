package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ComparisonRecommendationTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Comparison-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_first_attribute_and_hide_recommendation() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_first_attribute_and_show_other_recommendations() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ACTIVITY_TYPE).apply();
        analysisPage.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ACTIVITY_TYPE));
        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
        assertTrue(isElementPresent(cssSelector(".s-recommendation-contribution"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_initial_value_selected_after_resetting_report() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        String initialValue = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select(ACTIVITY_TYPE);
        String value = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        analysisPage.resetToBlankState();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
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
