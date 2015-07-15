package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesSimpleMetricCreationTest extends AnalyticalDesignerAbstractTest {

    private static final String AMOUNT = "Amount";
    private static final String STAGE_NAME = "Stage Name";
    private static final String SUM_OF_AMOUNT = "Sum of " + AMOUNT;

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Simple-Metric-Creation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFact() {
        initAnalysePage();
        analysisPage.addMetricFromFact(AMOUNT)
            .expandMetricConfiguration(SUM_OF_AMOUNT);
        assertEquals(analysisPage.getMetricAggregation(SUM_OF_AMOUNT), "Sum");
        analysisPage.waitForReportComputing();
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.undo();
        assertTrue(analysisPage.isMetricBucketEmpty());

        analysisPage.redo();
        assertFalse(analysisPage.isMetricBucketEmpty());

        analysisPage.addCategory(STAGE_NAME).waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_AMOUNT);
        checkingOpenAsReport("createSimpleMetricFromFact");
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFactUsingShortcut() {
        initAnalysePage();
        analysisPage.dragAndDropFactToShortcutPanel(AMOUNT, ShortcutPanel.AS_A_COLUMN_CHART)
            .waitForReportComputing()
            .expandMetricConfiguration(SUM_OF_AMOUNT);
        assertEquals(analysisPage.getMetricAggregation(SUM_OF_AMOUNT), "Sum");
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), SUM_OF_AMOUNT);

        analysisPage.resetToBlankState();

        analysisPage.dragAndDropFactToShortcutPanel(AMOUNT, ShortcutPanel.TRENDED_OVER_TIME)
            .waitForReportComputing();
        assertEquals(analysisPage.getDateFilterText(), "Activity: Last 4 quarters");
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertEquals(analysisPage.getSelectedGranularity(), "Quarter");
        checkingOpenAsReport("createSimpleMetricFromFactUsingShortcut");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricAggregations() {
        initAnalysePage();
        analysisPage.addMetricFromFact(AMOUNT)
            .expandMetricConfiguration(SUM_OF_AMOUNT);
        assertEquals(analysisPage.getMetricAggregation(SUM_OF_AMOUNT), "Sum");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getYaxisTitle(), SUM_OF_AMOUNT);

        assertTrue(isEqualCollection(analysisPage.getAllMetricAggregations(SUM_OF_AMOUNT),
                asList("Sum", "Minimum", "Maximum", "Average", "Running sum", "Median")));

        Map<String, String> aggregations = new HashMap<String, String>();
        aggregations.put("Maximum", "Max ");
        aggregations.put("Minimum", "Min ");
        aggregations.put("Average", "Avg ");
        aggregations.put("Running sum", "Runsum of ");
        aggregations.put("Median", "Median ");
        String metricFromAmountTitle = SUM_OF_AMOUNT;

        for (Map.Entry<String, String> entry: aggregations.entrySet()) {
            analysisPage.changeMetricAggregation(metricFromAmountTitle, entry.getKey())
                .waitForReportComputing();
            metricFromAmountTitle = entry.getValue() + AMOUNT;
            assertEquals(analysisPage.getChartReport().getYaxisTitle(), metricFromAmountTitle);
        }

        analysisPage.addMetricFromFact(AMOUNT)
            .expandMetricConfiguration(SUM_OF_AMOUNT);
        assertEquals(analysisPage.getMetricAggregation(SUM_OF_AMOUNT), "Sum");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.undo()
            .expandMetricConfiguration(metricFromAmountTitle)
            .addCategory(DATE)
            .waitForReportComputing()
            .turnOnShowInPercents()
            .waitForReportComputing();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.compareToSamePeriodOfYearBefore()
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("testMetricAggregations");
    }
}
