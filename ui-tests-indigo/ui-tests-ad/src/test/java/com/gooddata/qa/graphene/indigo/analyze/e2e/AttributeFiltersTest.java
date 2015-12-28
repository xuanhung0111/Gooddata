package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class AttributeFiltersTest extends AbstractGoodSalesE2ETest {

    private static final String CONTEXT = "#gd-overlays";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Attribute-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_search_results_after_closing() {
        beforeEach();

        click(".s-filter-button");

        fillIn(".s-filter-picker .searchfield-input", "asdf");
        expectFind(".gd-list-noResults");

        click(".s-btn-cancel");
        click(".s-filter-button");

        expectElementCount(".s-filter-item", 4);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_add_and_remove_attribute_from_filters_bucket() {
        beforeEach();

        expectFind(FILTERS_BUCKET + " .s-attr-filter" + activityTypeAttrLabel);

        // try to drag a duplicate attribute filter
        dragFromCatalogue(activityTypeAttr, FILTERS_BUCKET);

        drag(FILTERS_BUCKET + " .s-attr-filter" + activityTypeAttrLabel, TRASH);
        expectMissing(FILTERS_BUCKET + " .s-attr-filter");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_allow_moving_other_buckets_items_to_filters_bucket() {
        beforeEach();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        drag(FILTERS_BUCKET + " .s-attr-filter" + activityTypeAttrLabel, TRASH);
        expectMissing(FILTERS_BUCKET + " .s-attr-filter" + activityTypeAttrLabel);

        drag(CATEGORIES_BUCKET + " " + activityTypeAttr, FILTERS_BUCKET);
        expectMissing(FILTERS_BUCKET + " .s-attr-filter" + activityTypeAttrLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_set_in_filter_where_clause() {
        beforeEach();

        click(".s-filter-button");
        clearFilter(CONTEXT);
        String id = Stream.of(waitForElementVisible(cssSelector(".s-filter-item[title=Email]"), browser)
                .getAttribute("class")
                .split(" "))
                .filter(e -> e.startsWith("s-id-"))
                .findFirst()
                .get()
                .split("-")[2];
        click(".s-filter-item.s-id-" + id, CONTEXT);
        click(".s-apply", CONTEXT);

        expectFind(".adi-components .adi-component .s-property-where.s-where-___in_____id__" + id + "___");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_set_not_in_filter_where_clause() {
        beforeEach();

        click(".s-filter-button");

        String id = Stream.of(waitForElementVisible(cssSelector(".s-filter-item[title=Email]"), browser)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];
        click(".s-filter-item.s-id-" + id, CONTEXT);
        click(".s-apply", CONTEXT);

        expectFind(".adi-components .adi-component .s-property-where.s-where-___not_____in_____id__" + id + "____");
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_nothing_changed() {
        beforeEachDisablingApplyButton();

        expectFind(".s-apply.disabled", CONTEXT);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_nothing_is_selected() {
        beforeEachDisablingApplyButton();

        clearFilter(CONTEXT);
        expectFind(".s-apply.disabled", CONTEXT);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_everything_is_unselected() {
        beforeEachDisablingApplyButton();

        clearFilter(CONTEXT);
        expectFind(".s-apply.disabled", CONTEXT);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_not_disable_apply_button_if_selection_is_inverted() {
        beforeEachDisablingApplyButton();

        click(".s-filter-item[title=Email]", CONTEXT);
        click(".s-apply", CONTEXT);

        click(".s-filter-button");
        click(".s-clear", CONTEXT);
        click(".s-filter-item[title=Email]", CONTEXT);
        expectFind(".s-apply:not(.disabled)", CONTEXT);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_not_disable_apply_button_if_single_item_is_filtered() {
        beforeEachDisablingApplyButton();

        clearFilter(CONTEXT);
        fillIn(".s-filter-picker .searchfield-input", "Email");
        click(".s-filter-item[title=Email]", CONTEXT);
        expectFind(".s-apply:not(.disabled)", CONTEXT);
        click(".s-apply", CONTEXT);
        assertThat(waitForElementVisible(className("s-attribute-filter-label"), browser).getText(),
                containsString("Activity Type: Email"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_selection_is_in_different_order() {
        beforeEachDisablingApplyButton();

        clearFilter(CONTEXT);
        click(".s-filter-item[title=Email]", CONTEXT);
        click(".s-filter-item[title='In Person Meeting']", CONTEXT);
        click(".s-apply", CONTEXT);

        click(".s-filter-button");

        clearFilter(CONTEXT);
        click(".s-filter-item[title='In Person Meeting']", CONTEXT);
        click(".s-filter-item[title=Email]", CONTEXT);

        expectFind(".s-apply.disabled", CONTEXT);
    }

    private void beforeEach() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, FILTERS_BUCKET);
    }

    private void beforeEachDisablingApplyButton() {
        beforeEach();

        click(".s-filter-button");
        expectFind(".s-filter-picker", CONTEXT);
    }
}
