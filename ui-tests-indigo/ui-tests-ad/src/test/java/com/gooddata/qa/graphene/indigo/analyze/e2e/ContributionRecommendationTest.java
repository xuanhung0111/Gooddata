package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ContributionRecommendationTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Contribution-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_in_percent_recommendation() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        click(".s-recommendation-contribution .s-apply-recommendation");

        expectFind(".adi-components .visualization-bar .s-property-y.s-id-metricvalues");

        browser.findElements(cssSelector(".adi-components .visualization-bar .highcharts-axis"))
            .stream()
            .map(e -> e.findElement(tagName("tspan")))
            .map(WebElement::getText)
            .filter(text -> "% # of Activities".equals(text))
            .findFirst()
            .get();

        expectMissing(".s-recommendation-contribution");
    }
}
