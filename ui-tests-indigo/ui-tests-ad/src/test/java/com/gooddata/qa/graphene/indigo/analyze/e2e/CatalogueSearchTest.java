package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class CatalogueSearchTest extends AbstractGoodSalesE2ETest {

    private String openOppsMetric;
    private String wonOppsMetric;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Catalogue-Search-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_empty_catalogue_if_no_catalogue_item_is_matched() {
        visitEditor();

        searchCatalogue("xyz");

        expectMatchedItems(emptyList());
        expectFind(".adi-no-items");
    }

    @Test(dependsOnGroups = {"init"})
    public void loadMetricIds() {
        openOppsMetric = ".s-id-" + getMdService().getObj(getProject(), Metric.class,
                title("# of Open Opps.")).getIdentifier().toLowerCase();
        wonOppsMetric = ".s-id-" + getMdService().getObj(getProject(), Metric.class,
                title("# of Won Opps.")).getIdentifier().toLowerCase();
    }

    @Test(dependsOnMethods = {"loadMetricIds"})
    public void should_show_only_matched_items() {
        visitEditor();

        searchCatalogue("Opps.");
        expectMatchedItems(asList(lostOppsMetric, openOppsMetric, wonOppsMetric));

        searchCatalogue("Da");
        expectMatchedItems(emptyList());
    }

    @Test(dependsOnMethods = {"loadMetricIds"})
    public void should_be_case_insensitive() {
        visitEditor();

        searchCatalogue("opps.");
        expectMatchedItems(asList(lostOppsMetric, openOppsMetric, wonOppsMetric));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_cancel_search() {
        visitEditor();

        searchCatalogue("# of Lost Opps.");
        expectMissingItems(asList(activityTypeAttr));

        cancelSearch();

        // catalog contains all items again, including activity type
        expectFind(".s-catalogue " + activityTypeAttr);
    }

    private void expectMissingItems(List<String> expectedMissingItems) {
        expectedMissingItems.stream()
            .forEach(expectedMissingItem -> expectMissing(".s-catalogue " + expectedMissingItem));
    }

    private void expectMatchedItems(List<String> expectedItems) {
        expectMatchedItemsCount(expectedItems.size());

        expectedItems.stream().forEach(expectedItem -> expectFind(".s-catalogue " + expectedItem));
    }

    private void expectMatchedItemsCount(int expectedCount) {
        expectElementCount(".adi-catalogue-item:not(.type-header)", expectedCount);
    }
}
