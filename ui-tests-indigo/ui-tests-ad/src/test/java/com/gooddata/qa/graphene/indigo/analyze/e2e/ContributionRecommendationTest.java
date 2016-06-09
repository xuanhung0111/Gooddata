package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertFalse;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.SeeingPercentsRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ContributionRecommendationTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Contribution-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_in_percent_recommendation() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<SeeingPercentsRecommendation>getRecommendation(RecommendationStep.SEE_PERCENTS).apply();

        analysisPageReact.waitForReportComputing();
//        assertTrue(isElementPresent(cssSelector(
//                ".adi-components .visualization-bar .s-property-y.s-id-metricvalues"), browser));

        browser.findElements(cssSelector(".adi-chart-container .highcharts-axis"))
            .stream()
            .map(e -> e.findElement(tagName("tspan")))
            .map(WebElement::getText)
            .filter(text -> ("% " + METRIC_NUMBER_OF_ACTIVITIES).equals(text))
            .findFirst()
            .get();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-contribution"), browser));
    }
}
