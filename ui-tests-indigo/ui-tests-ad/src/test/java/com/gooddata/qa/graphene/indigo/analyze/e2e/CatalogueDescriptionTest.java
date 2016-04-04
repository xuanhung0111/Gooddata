package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueDescriptionTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Catalogue-Description-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_metric_info_bubble_when_hovering_the_info_icon() {
        assertTrue(analysisPage.getCataloguePanel()
                .getMetricDescription(NUMBER_OF_ACTIVITIES)
                .contains("SELECT COUNT(Activity)"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_attribute_info_bubble_when_hovering_the_info_icon() {
        assertTrue(analysisPage.getCataloguePanel()
                .getAttributeDescription(ACTIVITY_TYPE)
                .contains("Email"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_dataset_of_fact() {
        assertTrue(analysisPage.getCataloguePanel()
                .getFactDescription(AMOUNT)
                .contains("OpportunitySnapshot"));
    }
}
