package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.className;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class AvailableItemsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Available-Items-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_attributes_when_metric_is_added() {
        visitEditor();

        expectFind(activityTypeAttr);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        expectMissing(activityTypeAttr);

        drag(METRICS_BUCKET + " " + lostOppsMetric, TRASH);
        expectFind(activityTypeAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_categories() {
        visitEditor();

        expectFind(lostOppsMetric);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectMissing(lostOppsMetric);

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, TRASH);
        expectFind(lostOppsMetric);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_metrics_when_attribute_is_in_stacks() {
        visitEditor();

        expectFind(lostOppsMetric);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectMissing(lostOppsMetric);

        drag(STACKS_BUCKET + " " + activityTypeAttr, TRASH);
        expectFind(lostOppsMetric);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_unavailable_facts() {
        visitEditor();

        expectFind(amountFact);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectMissing(amountFact);

        drag(STACKS_BUCKET + " " + activityTypeAttr, TRASH);
        expectFind(amountFact);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_all_items_on_reset() {
        String productAttr = ".s-id-" + getMdService().getObj(getProject(), Attribute.class,
                title("Product")).getIdentifier().toLowerCase().replace(".", "_");

        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        switchTabCatalogue(".s-filter-attributes");
        expectMissing(productAttr);
        switchTabCatalogue(".s-filter-metrics");
        expectMissing(lostOppsMetric);

        resetReport();
        switchTabCatalogue(".s-filter-attributes");
        expectFind(productAttr);
        switchTabCatalogue(".s-filter-metrics");
        expectFind(lostOppsMetric);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_metric_after_undo() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectMissing(lostOppsMetric);

        resetReport();
        expectFind(lostOppsMetric);

        undo();
        expectMissing(lostOppsMetric);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_attribute_after_undo() {
        visitEditor();

        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        expectMissing(activityTypeAttr);

        resetReport();
        expectFind(activityTypeAttr);

        undo();
        expectMissing(activityTypeAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_special_message_if_only_unavailable_items_matched() {
        visitEditor();

        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        searchCatalogue("Activity Type");
        assertThat(waitForElementVisible(className("s-unavailable-items-matched"), browser).getText(),
                containsString("1 unrelated data item hidden"));
    }
}
