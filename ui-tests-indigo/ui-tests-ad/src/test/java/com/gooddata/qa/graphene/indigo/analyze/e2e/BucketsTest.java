package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
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
        MetricConfiguration metricConfiguration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
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
        WebElement metric = analysisPageReact.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);
        analysisPageReact.drag(metric, analysisPageReact.getStacksBucket().getInvitation());
        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        WebElement date = analysisPageReact.getCataloguePanel().getDate();
        analysisPageReact.drag(date, analysisPageReact.getStacksBucket().getInvitation());
        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        assertFalse(analysisPageReact.addStack(ATTR_ACTIVITY_TYPE).getStacksBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_automatically_adds_new_attribute_filter() {
        assertFalse(analysisPageReact.addStack(ATTR_ACTIVITY_TYPE).getFilterBuckets().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_one_attribute_at_the_time() {
        WebElement invitation = analysisPageReact.getStacksBucket().getInvitation();
        analysisPageReact.addStack(ATTR_ACTIVITY_TYPE);
        assertFalse(invitation.isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_prevent_dropping_if_two_metrics_are_active() {
        StacksBucket stacksBucket = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getStacksBucket();
        assertFalse(stacksBucket.getWarningMessage().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_stack_by_to_category() {
        analysisPageReact.addStack(ATTR_ACTIVITY_TYPE)
            .drag(analysisPageReact.getStacksBucket().get(), analysisPageReact.getAttributesBucket().getInvitation());

        assertTrue(analysisPageReact.getStacksBucket().isEmpty());

        assertFalse(analysisPageReact.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_category_to_stack_by() {
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().getInvitation());

        assertFalse(analysisPageReact.getStacksBucket().isEmpty());
        assertTrue(analysisPageReact.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_swap_items_between_category_and_stack_by() {
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().get());

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACCOUNT));
        assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);

        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ATTR_ACCOUNT));
        assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_swap_if_date_dimension_is_present() {
        analysisPageReact.addDate()
            .addStack(ATTR_ACTIVITY_TYPE)
            // Drag date to stack by
            .drag(analysisPageReact.getAttributesBucket().getFirst(), analysisPageReact.getStacksBucket().get());

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(DATE));
        assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);

        // Drag stacking attribute to x-axis category
        analysisPageReact.drag(analysisPageReact.getStacksBucket().get(), analysisPageReact.getAttributesBucket().getFirst());

        assertTrue(analysisPageReact.getStacksBucket().isEmpty());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_metrics() {
        MetricConfiguration configuration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());

        configuration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_attributes() {
        MetricConfiguration configuration = analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_trending_recommendation_and_stacking_are_applied() {
        MetricConfiguration configuration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
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

        analysisPageReact.addStack(ATTR_ACTIVITY_TYPE);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_two_autogenerated_filters() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_ACCOUNT)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .getFilterBuckets()
                .getFiltersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_generate_filter_for_date_item() {
        analysisPageReact.addDate()
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getAttributesBucket()
            .changeGranularity("Month");

        assertTrue(analysisPageReact.removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getFilterBuckets()
            .isDateFilterVisible());
    }


    @Test(dependsOnGroups = {"init"})
    public void should_add_date_filter_when_adding_date_to_configuration_bucket() {
        assertTrue(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_date_filter_when_replacing_date_in_configuration_bucket_with_date() {
        assertTrue(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

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
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();

        assertEquals(analysisPageReact.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_metrics() {
        assertEquals(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPageReact.replaceMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPEN_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPageReact.replaceMetric(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_categories() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_ACCOUNT)
            .getAttributesBucket()
            .getItemNames(), asList(ATTR_ACCOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_category_with_date() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .replaceAttributeWithDate(ATTR_ACTIVITY_TYPE)
            .getAttributesBucket()
            .getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_stacks() {
        assertEquals(analysisPageReact.addStack(ATTR_ACTIVITY_TYPE)
            .replaceStack(ATTR_ACCOUNT)
            .getStacksBucket()
            .getAttributeName(), ATTR_ACCOUNT);
    }
}
