package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class CatalogueDescriptionTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Catalogue-Description-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_displayinfo_icon_when_hovering_a_metric() {
        visitEditor();

        hover(".s-catalogue " + activitiesMetric);

        expectFind(".inlineBubbleHelp", ".s-catalogue " + activitiesMetric);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_metric_info_bubble_when_hovering_the_info_icon() {
        visitEditor();

        showBubble(activitiesMetric);

        assertEquals(waitForElementVisible(cssSelector(".s-catalogue-bubble .adi-metric-maql"), browser)
                .getText().trim(), "SELECT COUNT(Activity)");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_attribute_info_bubble_when_hovering_the_info_icon() {
        visitEditor();

        showBubble(activityTypeAttr);

        assertTrue(getElementTexts(browser.findElements(cssSelector(".s-catalogue-bubble .s-attribute-element")))
                .contains("Email"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_dataset_of_fact() {
        visitEditor();

        showBubble(amountFact);

        assertEquals(waitForElementVisible(cssSelector(".s-catalogue-bubble .s-dataset-name"), browser)
                .getText().trim(), "OpportunitySnapshot");
    }

    private void showBubble(String catalogueItem) {
        hover(".s-catalogue " + catalogueItem);
        hover(".inlineBubbleHelp", ".s-catalogue  " + catalogueItem);
    }
}
