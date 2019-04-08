package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import static org.openqa.selenium.By.cssSelector;

public class BucketsTest extends AbstractAdE2ETest {

    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Buckets-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
        getMetricCreator().createNumberOfOpenOppsMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void allow_metric_properties_to_be_set_in_chart_configuration_buckets() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent should be disabled");

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();

        assertTrue(metricConfiguration.showPercents().isShowPercentSelected(), "Show percent should be selected");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_is_visible_for_line_colunm_and_bar_charts() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART);
        assertTrue(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser), "Stacks bucket should display");

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser), "Stacks bucket should display");

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertTrue(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser), "Stacks bucket should display");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_is_hidden_for_table_visualization() {
        initAnalysePage().changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser), "Stacks bucket shouldn't display");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_should_accept_only_attributes() {
        WebElement metric = initAnalysePage().getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);
        analysisPage.tryToDrag(metric, analysisPage.getStacksBucket().getInvitation());
        assertTrue(analysisPage.getStacksBucket().isEmpty(), "Stacks bucket should be empty");

        WebElement date = analysisPage.getCataloguePanel().getDate();
        analysisPage.tryToDrag(date, analysisPage.getStacksBucket().getInvitation());
        assertTrue(analysisPage.getStacksBucket().isEmpty(), "Stacks bucket should be empty");

        assertFalse(analysisPage.addStack(ATTR_ACTIVITY_TYPE).getStacksBucket().isEmpty(),
                "Stacks bucket shouldn't be empty");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_automatically_adds_new_attribute_filter() {
        assertFalse(initAnalysePage().addStack(ATTR_ACTIVITY_TYPE).getFilterBuckets().isEmpty(),
                "Filter buckets shouldn't be empty");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_should_accept_only_one_attribute_at_the_time() {
        WebElement invitation = initAnalysePage().getStacksBucket().getInvitation();
        analysisPage.addStack(ATTR_ACTIVITY_TYPE);
        assertFalse(invitation.isDisplayed(), "Invitation shouldn't display");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void test_stack_bucket_should_prevent_dropping_if_two_metrics_are_active() {
        StacksBucket stacksBucket = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getStacksBucket();
        assertFalse(stacksBucket.getWarningMessage().isEmpty(), "Warning message should display");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_drag_item_from_stack_by_to_category() {
        initAnalysePage().addStack(ATTR_ACTIVITY_TYPE)
            .drag(analysisPage.getStacksBucket().get(), analysisPage.getAttributesBucket().getInvitation());

        assertTrue(analysisPage.getStacksBucket().isEmpty(), "Stacks bucket should be empty");

        assertFalse(analysisPage.getAttributesBucket().isEmpty(), "Attributes bucket shouldn't be empty");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_drag_item_from_category_to_stack_by() {
        initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
            .drag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().getInvitation());

        assertFalse(analysisPage.getStacksBucket().isEmpty(), "Stacks bucket shouldn't be empty");
        assertTrue(analysisPage.getAttributesBucket().isEmpty(), "Attributes bucket should be empty");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_swap_items_between_category_and_stack_by() {
        initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .drag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().get());

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACCOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);

        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ATTR_ACCOUNT),
                ATTR_ACCOUNT + " filter should display");
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ATTR_ACTIVITY_TYPE),
                ATTR_ACTIVITY_TYPE + " filter should display");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_not_swap_if_date_dimension_is_present() {
        try {
            setExtendedStackingFlag(false);
            initAnalysePage().addDate()
                .addStack(ATTR_ACTIVITY_TYPE)
                // Drag date to stack by
                .tryToDrag(analysisPage.getAttributesBucket().getFirst(), analysisPage.getStacksBucket().get());

            assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(DATE));
            assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);

            // Drag stacking attribute to x-axis category
            analysisPage.tryToDrag(analysisPage.getStacksBucket().get(), analysisPage.getAttributesBucket().getFirst());

            assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(DATE));
            assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_ACTIVITY_TYPE);
        } finally {
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_disable_metric_properties_when_there_are_two_metrics() {
        MetricConfiguration configuration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(configuration.isShowPercentEnabled(), "Show percent should be disabled");

        configuration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertFalse(configuration.isShowPercentEnabled(), "Show percent should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_disable_metric_properties_when_there_are_two_attributes() {
        MetricConfiguration configuration = initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(configuration.isShowPercentEnabled(), "Show percent should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_disable_stack_bucket_when_trending_recommendation_and_compare_are_applied() {
        try {
            setExtendedStackingFlag(false);
            MetricConfiguration configuration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .getMetricsBucket()
                    .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                    .expandConfiguration();

            assertFalse(configuration.isShowPercentEnabled(), "Show percent should be disabled");

            Graphene.createPageFragment(RecommendationContainer.class,
                    waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                    .getRecommendation(RecommendationStep.SEE_TREND).apply();

            analysisPage.waitForReportComputing();
            assertTrue(configuration.showPercents().isShowPercentSelected());
            assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);

            analysisPage.getFilterBuckets()
                    .openDateFilterPickerPanel()
                    .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
            analysisPage.waitForReportComputing();

            assertTrue(analysisPage.getStacksBucket().isDisabled(), "Stack by bucket should be greyed out and disabled for adding measures");
            assertEquals(analysisPage.getStacksBucket().getWarningMessage(), "TO STACK BY, AN INSIGHT CAN HAVE ONLY ONE MEASURE");
        } finally {
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_add_metric_after_another_one() {
        assertEquals(initAnalysePage()
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_LOST_OPPS));

        assertEquals(analysisPage.addMetricAfter(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPEN_OPPS)
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_replace_categories() {
        try {
            setExtendedStackingFlag(false);
            assertEquals(initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
                    .replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_ACCOUNT)
                    .getAttributesBucket()
                    .getItemNames(), asList(ATTR_ACCOUNT));
        } finally {
            setExtendedStackingFlag(true);
        }

    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_replace_category_with_date() {
        try {
            setExtendedStackingFlag(false);
            assertEquals(initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
                .replaceAttributeWithDate(ATTR_ACTIVITY_TYPE)
                .getAttributesBucket()
                .getItemNames(), asList(DATE));
        } finally {
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_replace_stacks() {
        assertEquals(initAnalysePage().addStack(ATTR_ACTIVITY_TYPE)
            .replaceStack(ATTR_ACCOUNT)
            .getStacksBucket()
            .getAttributeName(), ATTR_ACCOUNT);
    }

    private void setExtendedStackingFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXTENDED_STACKING, status);
    }
}
