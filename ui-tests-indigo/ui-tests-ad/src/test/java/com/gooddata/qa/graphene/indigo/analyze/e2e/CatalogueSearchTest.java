package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueSearchTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Catalogue-Search-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpenOppsMetric();
        metricCreator.createNumberOfWonOppsMetric();
        metricCreator.createAvgAmountMetric();
        metricCreator.createWinRateMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_empty_catalogue_if_no_catalogue_item_is_matched() {
        CataloguePanel cataloguePanel = initAnalysePage().getCataloguePanel().search("xyz");
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"xyz\"");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_only_matched_items() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search("Opps.");
        assertThat(panel.getFieldNamesInViewPort() ,
                hasItems(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_WON_OPPS));

        panel.search("Dada");
        assertTrue(panel.isEmpty(), "Catalogue panel should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_case_insensitive() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search("opps.");
        assertThat(panel.getFieldNamesInViewPort(),
                hasItems(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_WON_OPPS));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_cancel_search() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search(METRIC_NUMBER_OF_LOST_OPPS);
        assertThat(panel.getFieldNamesInViewPort(), not(hasItem(ATTR_ACTIVITY_TYPE)));

        panel.clearInputText();

        // catalog contains all items again, including activity type
        assertThat(panel.getFieldNamesInViewPort(), hasItem(ATTR_ACTIVITY_TYPE));
    }
}
