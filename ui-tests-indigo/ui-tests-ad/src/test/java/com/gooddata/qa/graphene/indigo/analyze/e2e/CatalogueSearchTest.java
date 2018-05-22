package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
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
        assertFalse(initAnalysePage().getCataloguePanel().search("xyz"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_only_matched_items() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search("Opps.");
        assertTrue(panel.getFieldNamesInViewPort()
                .containsAll(asList(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_WON_OPPS)));

        assertFalse(panel.search("Dada"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_case_insensitive() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search("opps.");
        assertTrue(panel.getFieldNamesInViewPort()
                .containsAll(asList(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_WON_OPPS)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_cancel_search() {
        CataloguePanel panel = initAnalysePage().getCataloguePanel();

        panel.search(METRIC_NUMBER_OF_LOST_OPPS);
        assertFalse(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));

        panel.clearInputText();

        // catalog contains all items again, including activity type
        assertTrue(panel.getFieldNamesInViewPort().contains(ATTR_ACTIVITY_TYPE));
    }
}
