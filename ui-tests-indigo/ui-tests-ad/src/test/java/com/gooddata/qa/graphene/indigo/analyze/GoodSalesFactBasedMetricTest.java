package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_ACTIVITY_DATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_DURATION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class GoodSalesFactBasedMetricTest extends AbstractAnalyseTest {

    private static final String SUM_OF_AMOUNT = "Sum of " + FACT_AMOUNT;
    private static final String SUM_OF_ACTIVITY_DATE = "Sum of " + FACT_ACTIVITY_DATE;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Fact-Based-Metric-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSimpleMetricFromFact() {
        final MetricsBucket metricsBucket = initAnalysePage().getMetricsBucket();

        assertEquals(analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
                .getMetricsBucket()
                .getMetricConfiguration(SUM_OF_AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        analysisPage.waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should dislpay");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should dislpay");

        analysisPage.undo();
        assertTrue(metricsBucket.isEmpty(), "Metrics bucket should be empty");

        analysisPage.redo();
        assertFalse(metricsBucket.isEmpty(), "Metrics bucket shouldn't be empty");

        analysisPage.addAttribute(ATTR_STAGE_NAME).waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_AMOUNT);
        checkingOpenAsReport("createSimpleMetricFromFact");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMetricAggregations() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(FACT_ACTIVITY_DATE, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration(SUM_OF_ACTIVITY_DATE)
            .expandConfiguration();
        assertEquals(metricConfiguration.getAggregation(), "Sum");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_ACTIVITY_DATE);

        assertThat(metricConfiguration.getAllAggregations(),
                hasItems("Sum", "Minimum", "Maximum", "Average", "Running sum", "Median"));

        Map<String, String> aggregations = new HashMap<String, String>();
        aggregations.put("Maximum", "Max ");
        aggregations.put("Minimum", "Min ");
        aggregations.put("Average", "Avg ");
        aggregations.put("Running sum", "Runsum of ");
        aggregations.put("Median", "Median ");
        String metricFromAmountTitle;

        for (Map.Entry<String, String> entry: aggregations.entrySet()) {
            metricConfiguration.changeAggregation(entry.getKey());
            analysisPage.waitForReportComputing();
            metricFromAmountTitle = entry.getValue() + FACT_ACTIVITY_DATE;
            assertEquals(analysisPage.getChartReport().getYaxisTitle(), metricFromAmountTitle);
        }

        assertEquals(analysisPage.addMetric(FACT_ACTIVITY_DATE, FieldType.FACT)
                .getMetricsBucket()
                .getMetricConfiguration(SUM_OF_ACTIVITY_DATE)
                .expandConfiguration()
                .getAggregation(), "Sum");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Tracker should display");

        analysisPage.undo()
            .addDate()
            .waitForReportComputing();

        metricConfiguration.expandConfiguration().showPercents();
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Tracker should display");

        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1, "Tracker should display");
        checkingOpenAsReport("testMetricAggregations");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "https://jira.intgdc.com/browse/CL-7777")
    public void testAggregationFunctionList() {
        initAnalysePage().addMetric(FACT_AMOUNT, FieldType.FACT);

        assertEquals(analysisPage.getMetricsBucket()
                .getMetricConfiguration("Sum of " + FACT_AMOUNT)
                .expandConfiguration()
                .getAllAggregations(),
            asList("Sum", "Average", "Minimum", "Maximum", "Median", "Running sum"));
    }

    @DataProvider(name = "factMetricCombination")
    public Object[][] factMetricCombination() {
        return new Object[][] {
            {false, false},
            {true, false},
            {false, true},
            {true, true}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "factMetricCombination")
    public void shouldNotCreateDuplicateMetricFromFact(boolean samePeriodComparison, boolean percent) {

        initAnalysePage().addDate()
            .addMetric(FACT_ACTIVITY_DATE, FieldType.FACT);

        if (samePeriodComparison) {
            analysisPage.getFilterBuckets()
                    .openDateFilterPickerPanel()
                    .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                    .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

            analysisPage.waitForReportComputing();
        }

        MetricConfiguration configuration = expandMetricConfiguration(SUM_OF_ACTIVITY_DATE);

        if (percent) {
            configuration.showPercents();
        }

        // css class of metric from fact will be somehow like this:
        // class="s-bucket-item s-id-dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968 ..."
        // and identifier will be dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968
        final String identifier = Stream.of(analysisPage.getMetricsBucket()
            .get((percent ? "% " : "") + SUM_OF_ACTIVITY_DATE)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];

        analysisPage.removeMetric((percent ? "% " : "") + SUM_OF_ACTIVITY_DATE)
            .addMetric(FACT_ACTIVITY_DATE, FieldType.FACT);


        if (samePeriodComparison) {
            analysisPage.getFilterBuckets()
                    .openDateFilterPickerPanel()
                    .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

            analysisPage.waitForReportComputing();
        }

        configuration = expandMetricConfiguration(SUM_OF_ACTIVITY_DATE);

        if (percent) configuration.showPercents();

        assertThat(analysisPage.getMetricsBucket().get((percent ? "% " : "") + SUM_OF_ACTIVITY_DATE)
                .getAttribute("class"), containsString(identifier));

        if (!samePeriodComparison && !percent) {
            analysisPage.addMetric(FACT_ACTIVITY_DATE, FieldType.FACT);
            assertEquals(browser.findElements(className("s-id-" + identifier)).size(), 2);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMetricFromFact() {
        String sumOfAmount = "Sum of " + FACT_AMOUNT;
        String sumOfDuration = "Sum of " + FACT_DURATION;
        String averageAmount = "Avg " + FACT_AMOUNT;
        String runningSumOfDuration = "Runsum of " + FACT_DURATION;

        initAnalysePage().addMetric(FACT_AMOUNT, FieldType.FACT);
        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfAmount);
        assertTrue(amountConfiguration.isConfigurationCollapsed(), "Amount configuration should collapse");

        amountConfiguration.expandConfiguration().changeAggregation("Average");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singleton(averageAmount));

        analysisPage.addMetric(FACT_DURATION, FieldType.FACT);
        MetricConfiguration durationConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfDuration);
        assertTrue(durationConfiguration.isConfigurationCollapsed(), "Duration configuration should collapse");

        durationConfiguration.expandConfiguration().changeAggregation("Running sum");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(averageAmount, runningSumOfDuration));

        assertTrue(amountConfiguration.isConfigurationCollapsed(), "Amount configuration should collapse");
        assertFalse(durationConfiguration.isConfigurationCollapsed(), "Configuration should expand");
        checkingOpenAsReport("testMetricFromFact");
    }

    private MetricConfiguration expandMetricConfiguration(String metricId) {
        return analysisPage.getMetricsBucket().getMetricConfiguration(metricId).expandConfiguration();
    }
}
