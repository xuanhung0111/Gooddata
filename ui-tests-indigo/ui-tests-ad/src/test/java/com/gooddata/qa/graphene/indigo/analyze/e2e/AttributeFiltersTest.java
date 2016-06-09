package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class AttributeFiltersTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Attribute-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_reset_search_results_after_closing() {
        beforeEach();

        WebElement filter = analysisPageReact.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();

        AttributeFilterPickerPanel panel = AttributeFilterPickerPanel.getInstance(browser);
        panel.searchForText("asdf");

        sleepTightInSeconds(1);
        waitForElementVisible(cssSelector(".gd-list-noResults"), browser);

        panel.discard();

        filter.click();

        sleepTightInSeconds(3); // need buffer time for Selenium to refresh element values
        assertEquals(AttributeFilterPickerPanel.getInstance(browser).getItemNames().size(), 4);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_add_and_remove_attribute_from_filters_bucket() {
        beforeEach();

        analysisPageReact.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE);

        // try to drag a duplicate attribute filter
        assertTrue(analysisPageReact.addFilter(ATTR_ACTIVITY_TYPE)
            .removeFilter(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_allow_moving_other_buckets_items_to_filters_bucket() {
        beforeEach();

        assertTrue(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .removeFilter(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());

        assertTrue(analysisPageReact.drag(analysisPageReact.getAttributesBucket().getFirst(),
                analysisPageReact.getFilterBuckets().getInvitation())
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_nothing_changed() {
        assertTrue(beforeEachDisablingApplyButton()
                .getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_nothing_is_selected() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.getClearButton().click();
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_everything_is_unselected() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.getClearButton().click();
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_not_disable_apply_button_if_selection_is_inverted() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.select("Email");

        analysisPageReact.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        panel.getClearButton().click();
        panel.searchForText("Email");
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_not_disable_apply_button_if_single_item_is_filtered() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();

        panel.getClearButton().click();
        panel.selectItem("Email");
        assertFalse(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));

        panel.getApplyButton().click();
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": Email");
    }

    @Test(dependsOnGroups = {"init"}, groups = {"disabling-Apply-button"})
    public void should_disable_apply_button_if_selection_is_in_different_order() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.select("Email", "In Person Meeting");

        analysisPageReact.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        panel.getClearButton().click();
        panel.searchForText("Email");
        panel.searchForText("In Person Meeting");
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    private void beforeEach() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addFilter(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();
    }

    private AttributeFilterPickerPanel beforeEachDisablingApplyButton() {
        beforeEach();

        analysisPageReact.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        return AttributeFilterPickerPanel.getInstance(browser);
    }
}
