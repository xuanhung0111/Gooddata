package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
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

        assertTrue(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));
        analysisPage.addMetric(METRIC_NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));

        analysisPage.removeMetric(METRIC_NUMBER_OF_LOST_OPPS).waitForReportComputing();
        assertTrue(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_categories() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));

        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_stacks() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));
        analysisPage.addStack(ATTR_ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));

        analysisPage.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_facts() {
        CataloguePanel panel = analysisPage.getCataloguePanel();

        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_AMOUNT));
        analysisPage.addStack(ATTR_ACTIVITY_TYPE);
        assertFalse(panel.clearInputText().getFieldNamesInViewPort().contains(METRIC_AMOUNT));

        analysisPage.removeStack();
        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_all_items_on_reset() {
        CataloguePanel panel = analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getCataloguePanel()
            .filterCatalog(CatalogFilterType.ATTRIBUTES);

        assertFalse(panel.getFieldNamesInViewPort().contains(ATTR_PRODUCT));
        assertFalse(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(METRIC_NUMBER_OF_LOST_OPPS));

        analysisPage.resetToBlankState();
        assertTrue(panel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getFieldNamesInViewPort()
                .contains(ATTR_PRODUCT));
        assertTrue(panel.filterCatalog(CatalogFilterType.MEASURES)
                .getFieldNamesInViewPort()
                .contains(METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_metric_after_undo() {
        CataloguePanel panel = analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));

        analysisPage.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));

        analysisPage.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_attribute_after_undo() {
        CataloguePanel panel = analysisPage.addMetric(METRIC_NUMBER_OF_LOST_OPPS).getCataloguePanel().clearInputText();
        assertFalse(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));

        analysisPage.resetToBlankState();
        assertTrue(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));

        analysisPage.undo();
        assertFalse(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_special_message_if_only_unavailable_items_matched() {
        CataloguePanel panel = analysisPage.addMetric(METRIC_NUMBER_OF_LOST_OPPS).getCataloguePanel();
        assertFalse(panel.search(ATTR_ACTIVITY_TYPE));
        assertEquals(panel.getUnrelatedItemsHiddenCount(), 1);
    }
}
