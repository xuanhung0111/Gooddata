package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class BucketsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Buckets-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void allow_metric_properties_to_be_set_in_chart_configuration_buckets() {
        MetricConfiguration metricConfiguration = analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPageReact.waitForReportComputing();
        assertTrue(metricConfiguration.showPercents().isShowPercentSelected());
        assertTrue(metricConfiguration.showPop().isPopSelected());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_visible_for_line_colunm_and_bar_charts() {
        analysisPageReact.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));

        analysisPageReact.changeReportType(ReportType.LINE_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_hidden_for_table_visualization() {
        analysisPageReact.changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_attributes() {
        WebElement metric = analysisPageReact.getCataloguePanel().searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);
        analysisPageReact.drag(metric, analysisPageReact.getStacksBucket().getInvitation());
        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        WebElement date = analysisPageReact.getCataloguePanel().getDate();
        analysisPageReact.drag(date, analysisPageReact.getStacksBucket().getInvitation());
        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        assertFalse(analysisPageReact.addStack(ACTIVITY_TYPE).getStacksBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_automatically_adds_new_attribute_filter() {
        assertFalse(analysisPageReact.addStack(ACTIVITY_TYPE).getFilterBuckets().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_one_attribute_at_the_time() {
        WebElement invitation = analysisPageReact.getStacksBucket().getInvitation();
        analysisPageReact.addStack(ACTIVITY_TYPE);
        assertFalse(invitation.isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_prevent_dropping_if_two_metrics_are_active() {
        StacksBucket stacksBucket = analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getStacksBucket();
        assertFalse(stacksBucket.getWarningMessage().isEmpty());
        assertFalse(stacksBucket.getInvitation().isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_stack_by_to_category() {
        analysisPageReact.addStack(ACTIVITY_TYPE)
            .drag(analysisPageReact.getStacksBucket().get(), analysisPageReact.getAttributesBucket().getInvitation());

        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        assertFalse(analysisPageReact.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_category_to_stack_by() {
        analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().getInvitation());

        assertFalse(analysisPageReact.getStacksBucket().isEmpty());
        assertTrue(analysisPageReact.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_swap_items_between_category_and_stack_by() {
        analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .addStack(ACCOUNT)
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().get());

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ACCOUNT));
        assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), ACTIVITY_TYPE);

        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ACCOUNT));
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_swap_if_date_dimension_is_present() {
        analysisPageReact.addDate()
            .addStack(ACTIVITY_TYPE)
            // Drag date to stack by
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().get());

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(DATE));
        assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), ACTIVITY_TYPE);

        // Drag stacking attribute to x-axis category
        analysisPageReact.drag(analysisPageReact.getStacksBucket().get(), analysisPageReact.getAttributesBucket().getFirst());

        assertTrue(analysisPageReact.getStacksBucket().isEmpty());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_metrics() {
        MetricConfiguration configuration = analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());

        configuration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_attributes() {
        MetricConfiguration configuration = analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .addStack(ACCOUNT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_trending_recommendation_and_stacking_are_applied() {
        MetricConfiguration configuration = analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPageReact.waitForReportComputing();
        assertTrue(configuration.showPercents().isShowPercentSelected());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 1);

        assertTrue(configuration.showPop().isPopSelected());

        analysisPageReact.addStack(ACTIVITY_TYPE);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_two_autogenerated_filters() {
        assertEquals(analysisPageReact.addAttribute(ACTIVITY_TYPE)
                .addStack(ACCOUNT)
                .addMetric(NUMBER_OF_ACTIVITIES)
                .getFilterBuckets()
                .getFiltersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_generate_filter_for_date_item() {
        analysisPageReact.addDate()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getAttributesBucket()
            .changeGranularity("Month");

        assertTrue(analysisPageReact.removeMetric(NUMBER_OF_ACTIVITIES)
            .getFilterBuckets()
            .isDateFilterVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_replace_filter_attribute_when_replacing_attribute_in_configuration() {
        assertTrue(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing()
            .getFilterBuckets()
            .isFilterVisible(ACTIVITY_TYPE));

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));

        analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(ACCOUNT, FieldType.ATTRIBUTE),
                () -> waitForElementPresent(className("s-shortcut-metric-attribute"), browser));

        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ACCOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_add_date_filter_when_adding_date_to_configuration_bucket() {
        assertTrue(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_date_filter_when_replacing_date_in_configuration_bucket_with_date() {
        assertTrue(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));

        assertTrue(analysisPageReact.replaceAttributeWithDate(DATE)
            .getFilterBuckets()
            .isDateFilterVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_the_selected_date_dimension_second_time_dropped() {
        assertEquals(analysisPageReact.addDate()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: All time");

        assertTrue(analysisPageReact.removeAttribute(DATE)
            .getAttributesBucket()
            .isEmpty());

        assertEquals(analysisPageReact.addDate()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: All time");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_metric_series_in_correct_order() {
        analysisPageReact.addDate()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();

        assertEquals(analysisPageReact.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_metrics() {
        assertEquals(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_ACTIVITIES, NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPageReact.replaceMetric(NUMBER_OF_ACTIVITIES, NUMBER_OF_OPEN_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_OPEN_OPPS, NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPageReact.replaceMetric(NUMBER_OF_LOST_OPPS, NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_OPEN_OPPS, NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_categories() {
        assertEquals(analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .replaceAttribute(ACTIVITY_TYPE, ACCOUNT)
            .getAttributesBucket()
            .getItemNames(), asList(ACCOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_category_with_date() {
        assertEquals(analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .replaceAttributeWithDate(ACTIVITY_TYPE)
            .getAttributesBucket()
            .getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_stacks() {
        assertEquals(analysisPageReact.addStack(ACTIVITY_TYPE)
            .replaceStack(ACCOUNT)
            .getStacksBucket()
            .getAttributeName(), ACCOUNT);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_replace_filters() {
        assertTrue(analysisPageReact.addFilter(ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ACTIVITY_TYPE));

        assertTrue(analysisPageReact.addFilter(ACCOUNT)
            .getFilterBuckets()
            .isFilterVisible(ACCOUNT));

        assertEquals(analysisPageReact.getFilterBuckets().getFiltersCount(), 2);

        // drag onto existing item in the filter should still add the filter, not replace
        assertTrue(analysisPageReact.drag(analysisPageReact.getCataloguePanel().getDate(),
                analysisPageReact.getFilterBuckets().getFilter(ACTIVITY_TYPE))
            .getFilterBuckets()
            .isDateFilterVisible());
        assertEquals(analysisPageReact.getFilterBuckets().getFiltersCount(), 3);
    }
}
