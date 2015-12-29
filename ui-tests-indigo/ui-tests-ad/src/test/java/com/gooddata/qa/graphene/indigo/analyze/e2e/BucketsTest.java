package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class BucketsTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Buckets-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void allow_metric_properties_to_be_set_in_chart_configuration_buckets() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");
        expectElementCount(METRICS_BUCKET + " input[disabled]", 2);

        click(".s-recommendation-trending .s-apply-recommendation");

        click(METRICS_BUCKET + " .s-show-in-percent");
        expectFind(METRICS_BUCKET + " .s-show-in-percent:checked");

        click(METRICS_BUCKET + " .s-show-pop");
        expectFind(METRICS_BUCKET + " .s-show-pop:checked");

        expectElementCount(METRICS_BUCKET + " input:checked", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_visible_for_line_colunm_and_bar_charts() {
        visitEditor();

        click(".vis-type-column");
        expectFind(STACKS_BUCKET);

        click(".vis-type-bar");
        expectFind(STACKS_BUCKET);

        click(".vis-type-line");
        expectFind(STACKS_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_hidden_for_table_visualization() {
        visitEditor();

        click(".vis-type-table");
        expectMissing(STACKS_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_attributes() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, STACKS_BUCKET);
        expectFind(STACKS_BUCKET + EMPTY_BUCKET);

        dragFromCatalogue(DATE, STACKS_BUCKET);
        expectFind(STACKS_BUCKET + EMPTY_BUCKET);

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectFind(STACKS_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_automatically_adds_new_attribute_filter() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectFind(".s-attr-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_one_attribute_at_the_time() {
        String notEmptyStackInvitationLocator = STACKS_BUCKET + NOT_EMPTY_BUCKET + " .adi-bucket-invitation";
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectFind(notEmptyStackInvitationLocator);
        assertFalse(waitForElementPresent(cssSelector(notEmptyStackInvitationLocator), browser)
            .isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_prevent_dropping_if_two_metrics_are_active() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);

        expectFind(".s-stack-warn");
        expectMissing(STACKS_BUCKET + " .s-bucket-invitation");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_stack_by_to_category() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        drag(STACKS_BUCKET + " " + activityTypeAttr, CATEGORIES_BUCKET);

        expectFind(STACKS_BUCKET + EMPTY_BUCKET);

        expectFind(CATEGORIES_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_category_to_stack_by() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, STACKS_BUCKET);

        expectFind(CATEGORIES_BUCKET + EMPTY_BUCKET);
        expectFind(STACKS_BUCKET + NOT_EMPTY_BUCKET);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_swap_items_between_category_and_stack_by() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, STACKS_BUCKET);

        expectFind(STACKS_BUCKET + " " + activityTypeAttr);
        expectFind(CATEGORIES_BUCKET + " " + accountAttr);

        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);
        expectFind(FILTERS_BUCKET + " " + accountAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_swap_if_date_dimension_is_present() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        // Drag date to stack by
        drag(CATEGORIES_BUCKET + " " + yearActivityLabel, STACKS_BUCKET);

        expectFind(CATEGORIES_BUCKET + " " + yearActivityLabel);
        expectFind(STACKS_BUCKET + " " + activityTypeAttr);

        // Drag stacking attribute to x-axis category
        drag(STACKS_BUCKET + " " + activityTypeAttr, CATEGORIES_BUCKET);

        expectFind(STACKS_BUCKET + EMPTY_BUCKET);
        expectFind(CATEGORIES_BUCKET + " " + activityTypeAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_metrics() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + lostOppsMetric);

        expectElementCount(METRICS_BUCKET + " input[disabled]", 4);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_attributes() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        expectElementCount(CATEGORIES_BUCKET + " .adi-bucket-item", 1);
        expectElementCount(METRICS_BUCKET + " input[disabled]", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_trending_recommendation_and_stacking_are_applied() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        expectElementCount(METRICS_BUCKET + " input[disabled]", 2);

        click(".s-recommendation-trending .s-apply-recommendation");

        click(METRICS_BUCKET + " .s-show-in-percent");
        expectFind(METRICS_BUCKET + " .s-show-in-percent:checked");
        expectElementCount(CATEGORIES_BUCKET + " .adi-bucket-item", 1);

        click(METRICS_BUCKET + " .s-show-pop");
        expectFind(METRICS_BUCKET + " .s-show-pop:checked");

        expectElementCount(METRICS_BUCKET + " input:checked", 2);

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectElementCount(METRICS_BUCKET + " input[disabled]", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_two_autogenerated_filters() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectElementCount(FILTERS_BUCKET + " .s-attr-filter", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_generate_filter_for_date_item() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        select(".s-date-granularity-switch", "GDC.time.month");
        drag(METRICS_BUCKET + " " + activitiesMetric, TRASH);
        expectFind(".s-date-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_replace_filter_attribute_when_replacing_attribute_in_configuration() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        expectFind(".adi-components .s-property-y.s-id-metricvalues");
        expectFind(".adi-components .s-property-x" + activityTypeAttrLabel);
        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);

        drag(accountAttr, ".s-shortcut-metric-attribute");
        expectFind(FILTERS_BUCKET + " " + accountAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_add_date_filter_when_adding_date_to_configuration_bucket() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        expectFind(".adi-components .s-property-y.s-id-metricvalues");
        expectFind(CATEGORIES_BUCKET + " .s-date-dimension-switch");
        expectFind(FILTERS_BUCKET + " .s-date-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_date_filter_when_replacing_date_in_configuration_bucket_with_date() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        expectFind(".adi-components .s-property-y.s-id-metricvalues");
        expectFind(CATEGORIES_BUCKET + " .s-date-dimension-switch");
        expectFind(FILTERS_BUCKET + " .s-date-filter");


        drag(DATE, CATEGORIES_BUCKET + " " + yearActivityLabel);
        expectFind(CATEGORIES_BUCKET + " .s-date-dimension-switch");
        expectFind(FILTERS_BUCKET + " .s-date-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_the_selected_date_dimension_second_time_dropped() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        expectFind(FILTERS_BUCKET + " .s-btn-activity__all_time");
        expectFind(CATEGORIES_BUCKET + " " + yearActivityLabel);

        drag(CATEGORIES_BUCKET + " " + DATE, TRASH);

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);

        expectFind(FILTERS_BUCKET + " .s-btn-activity__all_time");
        expectFind(CATEGORIES_BUCKET + " " + yearActivityLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_metric_series_in_correct_order() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        click(METRICS_BUCKET + " .s-show-pop");

        expectChartLegend(asList("# of Activities - previous year", "# of Activities"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_metrics() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        expectElementTexts(METRICS_BUCKET + " .s-bucket-item-header .s-title",
                asList("# of Activities", "# of Lost Opps."));

        drag(".s-id-aayh6voua2yj", METRICS_BUCKET + " " + activitiesMetric);
        expectElementTexts(METRICS_BUCKET + " .s-bucket-item-header .s-title",
                asList("# of Open Opps.", "# of Lost Opps."));

        drag(activitiesMetric, METRICS_BUCKET + " " + lostOppsMetric);
        expectElementTexts(METRICS_BUCKET + " .s-bucket-item-header .s-title",
                asList("# of Open Opps.", "# of Activities"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_categories() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        drag(accountAttr, CATEGORIES_BUCKET + " " + activityTypeAttr);

        expectMissing(CATEGORIES_BUCKET + " " + activityTypeAttr);
        expectFind(CATEGORIES_BUCKET + " " + accountAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_category_with_date() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        drag(DATE, CATEGORIES_BUCKET + " " + activityTypeAttr);

        expectFind(CATEGORIES_BUCKET + " " + yearActivityLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_stacks() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);

        drag(accountAttr, STACKS_BUCKET + " " + activityTypeAttr);

        expectFind(STACKS_BUCKET + " " + accountAttr);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_replace_filters() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);

        dragFromCatalogue(accountAttr, FILTERS_BUCKET);
        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);
        expectFind(FILTERS_BUCKET + " " + accountAttrLabel);

        // drag onto existing item in the filter should still add the filter, not replace
        drag(DATE, FILTERS_BUCKET + " " + activityTypeAttrLabel);
        expectFind(FILTERS_BUCKET + " " + activityTypeAttrLabel);
        expectFind(FILTERS_BUCKET + " " + accountAttrLabel);
        expectFind(FILTERS_BUCKET + " .s-date-filter");
    }
}
