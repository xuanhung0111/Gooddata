package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class AvailableItemsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Available-Items-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_attributes_when_metric_is_added() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
        analysisPage.addMetric(NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPage.removeMetric(NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_categories() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPage.removeAttribute(ACTIVITY_TYPE);
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_stacks() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
        analysisPage.addStack(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPage.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_facts() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(AMOUNT));
        analysisPage.addStack(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(AMOUNT));

        analysisPage.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(AMOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_all_items_on_reset() {
        CataloguePanel panel = analysisPage.addStack(ACTIVITY_TYPE)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getCataloguePanel()
            .filterCatalog(CatalogFilterType.ATTRIBUTES);

        assertFalse(panel.getFieldNamesInViewPort().contains(PRODUCT));
        assertFalse(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(NUMBER_OF_LOST_OPPS));

        analysisPage.resetToBlankState();
        assertTrue(panel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getFieldNamesInViewPort()
                .contains(PRODUCT));
        assertTrue(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_metric_after_undo() {
        CataloguePanel panel = analysisPage.addAttribute(ACTIVITY_TYPE).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPage.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPage.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_attribute_after_undo() {
        CataloguePanel panel = analysisPage.addMetric(NUMBER_OF_LOST_OPPS).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPage.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPage.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_special_message_if_only_unavailable_items_matched() {
        CataloguePanel panel = analysisPage.addMetric(NUMBER_OF_LOST_OPPS).getCataloguePanel();
        assertFalse(panel.search(ACTIVITY_TYPE));
        assertEquals(panel.getUnrelatedItemsHiddenCount(), 1);
    }
}
