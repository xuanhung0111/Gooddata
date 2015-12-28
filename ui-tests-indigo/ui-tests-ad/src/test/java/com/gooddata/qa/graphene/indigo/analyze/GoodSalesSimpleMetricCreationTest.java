package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesSimpleMetricCreationTest extends AnalyticalDesignerAbstractTest {

    private static final String SUM_OF_AMOUNT = "Sum of " + AMOUNT;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Simple-Metric-Creation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFact() {
        initAnalysePage();
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
    public void createSimpleMetricFromFactUsingShortcut() {
        initAnalysePage();

        WebElement fact = analysisPage.getCataloguePanel()
                .searchAndGet(AMOUNT, FieldType.FACT);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        assertEquals(analysisPage.drag(fact, recommendation)
                .waitForReportComputing()
                .getMetricsBucket()
                .getMetricConfiguration(SUM_OF_AMOUNT)
                .expandConfiguration()
                .getAggregation(), "Sum");
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), SUM_OF_AMOUNT);

        analysisPage.resetToBlankState();

        recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPage.drag(fact, recommendation)
            .waitForReportComputing();
        assertEquals(analysisPage.getFilterBuckets().getDateFilterText(), "Closed: Last 4 quarters");
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DATE));
        assertEquals(analysisPage.getCategoriesBucket().getSelectedGranularity(), "Quarter");
        checkingOpenAsReport("createSimpleMetricFromFactUsingShortcut");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricAggregations() {
        initAnalysePage();
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
}
