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
        initAnalysePageByUrl();

        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();
        assertTrue(metricConfiguration.showPercents().isShowPercentSelected());
        assertTrue(metricConfiguration.showPop().isPopSelected());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_visible_for_line_colunm_and_bar_charts() {
        initAnalysePageByUrl();

        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertTrue(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_is_hidden_for_table_visualization() {
        initAnalysePageByUrl();

        analysisPage.changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_attributes() {
        initAnalysePageByUrl();

        WebElement metric = analysisPage.getCataloguePanel().searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);
        analysisPage.drag(metric, analysisPage.getStacksBucket().getInvitation());
        assertTrue(analysisPage.getStacksBucket().isEmpty());

        WebElement date = analysisPage.getCataloguePanel().getDate();
        analysisPage.drag(date, analysisPage.getStacksBucket().getInvitation());
        assertTrue(analysisPage.getStacksBucket().isEmpty());

        assertFalse(analysisPage.addStack(ACTIVITY_TYPE).getStacksBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_automatically_adds_new_attribute_filter() {
        initAnalysePageByUrl();

        assertFalse(analysisPage.addStack(ACTIVITY_TYPE).getFilterBuckets().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_accept_only_one_attribute_at_the_time() {
        initAnalysePageByUrl();

        WebElement invitation = analysisPage.getStacksBucket().getInvitation();
        analysisPage.addStack(ACTIVITY_TYPE);
        assertFalse(invitation.isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void test_stack_bucket_should_prevent_dropping_if_two_metrics_are_active() {
        initAnalysePageByUrl();

        StacksBucket stacksBucket = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .getStacksBucket();
        assertFalse(stacksBucket.getWarningMessage().isEmpty());
        assertFalse(stacksBucket.getInvitation().isDisplayed());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_stack_by_to_category() {
        initAnalysePageByUrl();

        analysisPage.addStack(ACTIVITY_TYPE);

        analysisPage.drag(analysisPage.getStacksBucket().get(), analysisPage.getAttributesBucket().getInvitation());

        assertTrue(analysisPage.getStacksBucket().isEmpty());

        assertFalse(analysisPage.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_item_from_category_to_stack_by() {
        initAnalysePageByUrl();

        analysisPage.addAttribute(ACTIVITY_TYPE);

        analysisPage.drag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().getInvitation());

        assertFalse(analysisPage.getStacksBucket().isEmpty());
        assertTrue(analysisPage.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_swap_items_between_category_and_stack_by() {
        initAnalysePageByUrl();

        analysisPage.addAttribute(ACTIVITY_TYPE)
            .addStack(ACCOUNT);

        analysisPage.drag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().get());

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACCOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ACTIVITY_TYPE);

        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ACCOUNT));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_swap_if_date_dimension_is_present() {
        initAnalysePageByUrl();

        analysisPage.addDate()
            .addStack(ACTIVITY_TYPE)
            // Drag date to stack by
            .drag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().get());

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(DATE));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ACTIVITY_TYPE);

        // Drag stacking attribute to x-axis category
        analysisPage.drag(analysisPage.getStacksBucket().get(), analysisPage.getAttributesBucket().getFirst());

        assertTrue(analysisPage.getStacksBucket().isEmpty());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_metrics() {
        initAnalysePageByUrl();

        MetricConfiguration activitiesConfig = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();


        MetricConfiguration lostConfig = analysisPage.addMetric(NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_LOST_OPPS)
            .expandConfiguration();

        assertFalse(activitiesConfig.isPopEnabled());
        assertFalse(activitiesConfig.isShowPercentEnabled());
        assertFalse(lostConfig.isPopEnabled());
        assertFalse(lostConfig.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_there_are_two_attributes() {
        initAnalysePageByUrl();

        MetricConfiguration configuration = analysisPage.addAttribute(ACTIVITY_TYPE)
            .addStack(ACCOUNT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_metric_properties_when_trending_recommendation_and_stacking_are_applied() {
        initAnalysePageByUrl();

        MetricConfiguration configuration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();
        assertTrue(configuration.showPercents().isShowPercentSelected());
        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);

        assertTrue(configuration.showPop().isPopSelected());

        analysisPage.addStack(ACTIVITY_TYPE);
        assertFalse(configuration.isPopEnabled());
        assertFalse(configuration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_two_autogenerated_filters() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
                .addStack(ACCOUNT)
                .addMetric(NUMBER_OF_ACTIVITIES)
                .getFilterBuckets()
                .getFiltersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_generate_filter_for_date_item() {
        initAnalysePageByUrl();

        analysisPage.addDate()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getAttributesBucket()
            .changeGranularity("Month");

        assertTrue(analysisPage.removeMetric(NUMBER_OF_ACTIVITIES)
            .getFilterBuckets()
            .isDateFilterVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_replace_filter_attribute_when_replacing_attribute_in_configuration() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ACTIVITY_TYPE));

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));

        analysisPage.drag(analysisPage.getCataloguePanel().searchAndGet(ACCOUNT, FieldType.ATTRIBUTE),
                () -> waitForElementPresent(className("s-shortcut-metric-attribute"), browser));

        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ACCOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_add_date_filter_when_adding_date_to_configuration_bucket() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_date_filter_when_replacing_date_in_configuration_bucket_with_date() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing()
            .getFilterBuckets()
            .isDateFilterVisible());

        assertTrue(isElementPresent(cssSelector(".adi-components .s-property-y.s-id-metricvalues"), browser));

        assertTrue(analysisPage.replaceAttributeWithDate(DATE)
            .getFilterBuckets()
            .isDateFilterVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_keep_the_selected_date_dimension_second_time_dropped() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addDate()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: All time");

        assertTrue(analysisPage.removeAttribute(DATE)
            .getAttributesBucket()
            .isEmpty());

        assertEquals(analysisPage.addDate()
            .getFilterBuckets()
            .getDateFilterText(), "Activity: All time");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_metric_series_in_correct_order() {
        initAnalysePageByUrl();

        analysisPage.addDate()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();

        assertEquals(analysisPage.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_metrics() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_ACTIVITIES, NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, NUMBER_OF_OPEN_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_OPEN_OPPS, NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPage.replaceMetric(NUMBER_OF_LOST_OPPS, NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_OPEN_OPPS, NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_categories() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
            .replaceAttribute(ACTIVITY_TYPE, ACCOUNT)
            .getAttributesBucket()
            .getItemNames(), asList(ACCOUNT));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_category_with_date() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
            .replaceAttributeWithDate(ACTIVITY_TYPE)
            .getAttributesBucket()
            .getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_replace_stacks() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addStack(ACTIVITY_TYPE)
            .replaceStack(ACCOUNT)
            .getStacksBucket()
            .getAttributeName(), ACCOUNT);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_replace_filters() {
        initAnalysePageByUrl();

        assertTrue(analysisPage.addFilter(ACTIVITY_TYPE)
            .getFilterBuckets()
            .isFilterVisible(ACTIVITY_TYPE));

        assertTrue(analysisPage.addFilter(ACCOUNT)
            .getFilterBuckets()
            .isFilterVisible(ACCOUNT));

        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 2);

        // drag onto existing item in the filter should still add the filter, not replace
        assertTrue(analysisPage.drag(analysisPage.getCataloguePanel().getDate(),
                analysisPage.getFilterBuckets().getFilter(ACTIVITY_TYPE))
            .getFilterBuckets()
            .isDateFilterVisible());
        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 3);
    }
}
