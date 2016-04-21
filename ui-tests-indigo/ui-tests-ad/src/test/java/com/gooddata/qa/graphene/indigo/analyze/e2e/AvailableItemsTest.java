package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanelReact;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class AvailableItemsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Available-Items-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_attributes_when_metric_is_added() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
        analysisPageReact.addMetric(NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPageReact.removeMetric(NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_categories() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
        analysisPageReact.addAttribute(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPageReact.removeAttribute(ACTIVITY_TYPE);
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_stacks() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
        analysisPageReact.addStack(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPageReact.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_facts() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(AMOUNT));
        analysisPageReact.addStack(ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(AMOUNT));

        analysisPageReact.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(AMOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_all_items_on_reset() {
        CataloguePanelReact panel = analysisPageReact.addStack(ACTIVITY_TYPE)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getCataloguePanel()
            .filterCatalog(CatalogFilterType.ATTRIBUTES);

        assertFalse(panel.getFieldNamesInViewPort().contains(PRODUCT));
        assertFalse(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(NUMBER_OF_LOST_OPPS));

        analysisPageReact.resetToBlankState();
        assertTrue(panel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getFieldNamesInViewPort()
                .contains(PRODUCT));
        assertTrue(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_metric_after_undo() {
        CataloguePanelReact panel = analysisPageReact.addAttribute(ACTIVITY_TYPE).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPageReact.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));

        analysisPageReact.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_attribute_after_undo() {
        CataloguePanelReact panel = analysisPageReact.addMetric(NUMBER_OF_LOST_OPPS).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPageReact.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        analysisPageReact.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_special_message_if_only_unavailable_items_matched() {
        CataloguePanelReact panel = analysisPageReact.addMetric(NUMBER_OF_LOST_OPPS).getCataloguePanel();
        assertFalse(panel.search(ACTIVITY_TYPE));
        assertEquals(panel.getUnrelatedItemsHiddenCount(), 1);
    }
}
