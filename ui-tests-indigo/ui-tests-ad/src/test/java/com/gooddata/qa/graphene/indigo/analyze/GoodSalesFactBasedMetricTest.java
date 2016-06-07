package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesFactBasedMetricTest extends GoodSalesAbstractAnalyseTest {

    private static final String SUM_OF_AMOUNT = "Sum of " + AMOUNT;
    private static final String DURATION = "Duration";
    private static final String ACTIVITY_DATE = "Activity (Date)";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Fact-Based-Metric-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFact() {
        final MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

        assertEquals(analysisPage.addMetric(AMOUNT, FieldType.FACT)
                .getMetricsBucket()
                .getMetricConfiguration(SUM_OF_AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        analysisPage.waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.undo();
        assertTrue(metricsBucket.isEmpty());

        analysisPage.redo();
        assertFalse(metricsBucket.isEmpty());

        analysisPage.addAttribute(STAGE_NAME).waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_AMOUNT);
        checkingOpenAsReport("createSimpleMetricFromFact");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricAggregations() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration(SUM_OF_AMOUNT)
            .expandConfiguration();
        assertEquals(metricConfiguration.getAggregation(), "Sum");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_AMOUNT);

        assertTrue(isEqualCollection(metricConfiguration.getAllAggregations(),
                asList("Sum", "Minimum", "Maximum", "Average", "Running sum", "Median")));

        Map<String, String> aggregations = new HashMap<String, String>();
        aggregations.put("Maximum", "Max ");
        aggregations.put("Minimum", "Min ");
        aggregations.put("Average", "Avg ");
        aggregations.put("Running sum", "Runsum of ");
        aggregations.put("Median", "Median ");
        String metricFromAmountTitle = SUM_OF_AMOUNT;

        for (Map.Entry<String, String> entry: aggregations.entrySet()) {
            metricConfiguration.changeAggregation(entry.getKey());
            analysisPage.waitForReportComputing();
            metricFromAmountTitle = entry.getValue() + AMOUNT;
            assertEquals(analysisPage.getChartReport().getYaxisTitle(), metricFromAmountTitle);
        }

        assertEquals(analysisPage.addMetric(AMOUNT, FieldType.FACT)
                .getMetricsBucket()
                .getMetricConfiguration(SUM_OF_AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.undo()
            .addDate()
            .waitForReportComputing();

        metricConfiguration.expandConfiguration().showPercents();
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        metricConfiguration.showPop();
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("testMetricAggregations");
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-7777")
    public void testAggregationFunctionList() {
        analysisPage.addMetric(AMOUNT, FieldType.FACT);

        assertEquals(analysisPage.getMetricsBucket()
                .getMetricConfiguration("Sum of " + AMOUNT)
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

    @Test(dependsOnGroups = {"init"}, dataProvider = "factMetricCombination")
    public void shouldNotCreateDuplicateMetricFromFact(boolean pop, boolean percent) {
        MetricConfiguration configuration = analysisPage.addDate()
            .addMetric(ACTIVITY_DATE, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + ACTIVITY_DATE)
            .expandConfiguration();

        if (pop) configuration.showPop();
        if (percent) configuration.showPercents();

        // css class of metric from fact will be somehow like this:
        // class="s-bucket-item s-id-dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968 ..."
        // and identifier will be dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968
        final String identifier = Stream.of(analysisPage.getMetricsBucket()
            .get((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];

        configuration = analysisPage.removeMetric((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .addMetric(ACTIVITY_DATE, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + ACTIVITY_DATE)
            .expandConfiguration();

        if (pop) configuration.showPop();
        if (percent) configuration.showPercents();

        assertTrue(analysisPage.getMetricsBucket()
            .get((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .getAttribute("class")
            .contains(identifier));

        if (!pop && !percent) {
            analysisPage.addMetric(ACTIVITY_DATE, FieldType.FACT);
            assertEquals(browser.findElements(className("s-id-" + identifier)).size(), 2);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromFact() {
        String sumOfAmount = "Sum of " + AMOUNT;
        String sumOfDuration = "Sum of " + DURATION;
        String averageAmount = "Avg " + AMOUNT;
        String runningSumOfDuration = "Runsum of " + DURATION;

        analysisPage.addMetric(AMOUNT, FieldType.FACT);
        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfAmount);
        assertTrue(amountConfiguration.isConfigurationCollapsed());

        amountConfiguration.expandConfiguration().changeAggregation("Average");
        assertTrue(isEqualCollection(analysisPage.getMetricsBucket().getItemNames(), singleton(averageAmount)));

        analysisPage.addMetric(DURATION, FieldType.FACT);
        MetricConfiguration durationConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfDuration);
        assertTrue(durationConfiguration.isConfigurationCollapsed());

        durationConfiguration.expandConfiguration().changeAggregation("Running sum");
        assertTrue(isEqualCollection(analysisPage.getMetricsBucket().getItemNames(),
                asList(averageAmount, runningSumOfDuration)));

        assertTrue(amountConfiguration.isConfigurationCollapsed());
        assertFalse(durationConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("testMetricFromFact");
    }
}
