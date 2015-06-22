package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
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

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Simple-Metric-Creation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFact() {
        initAnalysePage();
        analysisPage.addMetricFromFact(AMOUNT);
        assertEquals(analysisPage.getFactAggregation(AMOUNT), "SUM");
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
        assertEquals(report.getYaxisTitle(), "Sum of " + AMOUNT);
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromFactUsingShortcut() {
        initAnalysePage();
        analysisPage.dragAndDropFactToShortcutPanel(AMOUNT, ShortcutPanel.AS_A_COLUMN_CHART)
            .waitForReportComputing();
        assertEquals(analysisPage.getFactAggregation(AMOUNT), "SUM");
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), "Sum of " + AMOUNT);

        analysisPage.resetToBlankState();

        analysisPage.dragAndDropFactToShortcutPanel(AMOUNT, ShortcutPanel.TRENDED_OVER_TIME)
            .waitForReportComputing();
        assertEquals(analysisPage.getDateFilterText(), "Activity: Last 4 quarters");
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertEquals(analysisPage.getSelectedGranularity(), "Quarter");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricAggregations() {
        initAnalysePage();
        analysisPage.addMetricFromFact(AMOUNT);
        assertEquals(analysisPage.getFactAggregation(AMOUNT), "SUM");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getYaxisTitle(), "Sum of " + AMOUNT);

        assertTrue(isEqualCollection(analysisPage.getAllFactAggregations(AMOUNT),
                asList("SUM", "MAX", "MIN", "AVG", "RUNSUM", "MEDIAN")));

        Map<String, String> aggregations = new HashMap<String, String>();
        aggregations.put("MAX", "Maximum ");
        aggregations.put("MIN", "Minimum ");
        aggregations.put("AVG", "Average ");
        aggregations.put("RUNSUM", "Runsum of ");
        aggregations.put("MEDIAN", "Median ");

        for (Map.Entry<String, String> entry: aggregations.entrySet()) {
            analysisPage.changeAggregationOfFact(AMOUNT, entry.getKey()).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().getYaxisTitle(), entry.getValue() + AMOUNT);
        }

        analysisPage.addMetricFromFact(AMOUNT);
        assertEquals(analysisPage.getFactAggregationByIndex(AMOUNT, 1), "SUM");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.undo().addCategory(DATE).waitForReportComputing();
        analysisPage.turnOnShowInPercents().waitForReportComputing();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.compareToSamePeriodOfYearBefore().waitForReportComputing();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
    }
}
