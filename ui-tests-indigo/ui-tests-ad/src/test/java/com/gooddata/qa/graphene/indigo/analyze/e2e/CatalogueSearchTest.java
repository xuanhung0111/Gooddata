package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanelReact;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueSearchTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Catalogue-Search-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_empty_catalogue_if_no_catalogue_item_is_matched() {
        assertFalse(analysisPageReact.getCataloguePanel().search("xyz"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_only_matched_items() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        panel.search("Opps.");
        assertTrue(panel.getFieldNamesInViewPort()
                .containsAll(asList(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS, NUMBER_OF_WON_OPPS)));

        assertFalse(panel.search("Dada"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_case_insensitive() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        panel.search("opps.");
        assertTrue(panel.getFieldNamesInViewPort()
                .containsAll(asList(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS, NUMBER_OF_WON_OPPS)));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_cancel_search() {
        CataloguePanelReact panel = analysisPageReact.getCataloguePanel();

        panel.search(NUMBER_OF_LOST_OPPS);
        assertFalse(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));

        panel.clearInputText();

        // catalog contains all items again, including activity type
        assertTrue(panel.getFieldNamesInViewPort().contains(ACTIVITY_TYPE));
    }
}
