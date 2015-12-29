package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ComparisonRecommendationTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Comparison-Recommendation-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_first_attribute_and_hide_recommendation() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        select(".s-recommendation-comparison .s-attribute-switch", "attr.activity.activitytype");
        click(".s-recommendation-comparison .s-apply-recommendation");
        expectMissing(".s-recommendation-comparison");
        expectFind(CATEGORIES_BUCKET + " " + activityTypeAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply_first_attribute_and_show_other_recommendations() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        select(".s-recommendation-comparison .s-attribute-switch", "attr.activity.activitytype");
        click(".s-recommendation-comparison .s-apply-recommendation");
        expectMissing(".s-recommendation-comparison");
        expectFind(CATEGORIES_BUCKET + " " + activityTypeAttr);
        expectFind(".s-recommendation-comparison-with-period");
        expectFind(".s-recommendation-contribution");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_initial_value_selected_after_resetting_report() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        String initialValue = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        select(".s-recommendation-comparison .s-attribute-switch", "attr.activity.activitytype");
        String value = getValueFrom(".s-recommendation-comparison .s-attribute-switch");

        resetReport();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
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
