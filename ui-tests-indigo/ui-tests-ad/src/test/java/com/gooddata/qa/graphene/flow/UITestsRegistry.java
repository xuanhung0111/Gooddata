package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.indigo.analyze.AnalyticalDesignerSanityTest;
import com.gooddata.qa.graphene.indigo.analyze.BackwardCompatibilityTest;
import com.gooddata.qa.graphene.indigo.analyze.DateFilterADMeasureTest;
import com.gooddata.qa.graphene.indigo.analyze.DateFilterADMeasureExtendedTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesPivotTableReportTest;
import com.gooddata.qa.graphene.indigo.analyze.PivotTableAggregationTest;
import com.gooddata.qa.graphene.indigo.analyze.RenamingMeasuresAndAttributesTest;
import com.gooddata.qa.graphene.indigo.analyze.RedesigningDateFilterDialogOnFilterBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.PivotTableTest;
import com.gooddata.qa.graphene.indigo.analyze.PivotTableAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.TreeMapChartReportTest;
import com.gooddata.qa.graphene.indigo.analyze.TreeMapChartReportAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.HeatMapTest;
import com.gooddata.qa.graphene.indigo.analyze.HeatMapAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.ReferencePointAndVisSwitchingTest;
import com.gooddata.qa.graphene.indigo.analyze.SecondaryAxisTest;
import com.gooddata.qa.graphene.indigo.analyze.CustomDateDimensionsTest;
import com.gooddata.qa.graphene.indigo.analyze.EmbeddedAdTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesChartLegendTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesComparisonRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesContributionRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.DateFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDescriptionTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDropAttributeTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesFactBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricVisibilityTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesNotRenderedInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesOvertimeComparisonTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesProjectNavigationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesRelatedAndUnrelatedDateDimensionsTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesSaveInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesShortcutRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesTrendingRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoRedoSavedInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesVisualizationTest;
import com.gooddata.qa.graphene.indigo.analyze.InsightCompareToPreviousPeriodTest;
import com.gooddata.qa.graphene.indigo.analyze.InsightCompareToSamePreviousPeriodYearTest;
import com.gooddata.qa.graphene.indigo.analyze.MultipleDatasetsTest;
import com.gooddata.qa.graphene.indigo.analyze.NonProductionDatasetInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.SpecialCasesTest;
import com.gooddata.qa.graphene.indigo.analyze.TotalsResultWithInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.ApplyColorPaletteOnAnalyzePageTest;
import com.gooddata.qa.graphene.indigo.analyze.ColorPalettePickerBasicInsightAndKPITest;
import com.gooddata.qa.graphene.indigo.analyze.ColorPalettePickerAdvancedInsightAndKPITest;
import com.gooddata.qa.graphene.indigo.analyze.ColorPalettePickerUILayoutInsightAndKPITest;
import com.gooddata.qa.graphene.indigo.analyze.CustomColorPaletteUILayoutInsightAndKPITest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeFiltersTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.BucketsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.E2ePivotTableTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.EmptyCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.FactBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.RecommendationsWithoutDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.ResetButtonTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.StackedChartsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.UndoTest;
import com.gooddata.qa.graphene.indigo.analyze.eventing.ContributionAndComparisionTest;
import com.gooddata.qa.graphene.indigo.analyze.eventing.EventingBasicFiltersTest;
import com.gooddata.qa.graphene.indigo.analyze.eventing.EventingBasicTest;
import com.gooddata.qa.graphene.indigo.analyze.eventing.EventingFiltersUnderMetric;
import com.gooddata.qa.graphene.indigo.analyze.eventing.EventingSpecialCaseTest;
import com.gooddata.qa.graphene.indigo.analyze.eventing.VisualizationMeasureAttributeTest;
import com.gooddata.qa.graphene.indigo.analyze.OptionalStackingTest;
import com.gooddata.qa.graphene.indigo.analyze.OrganisingMeasureAndAttributeCatalogue;
import com.gooddata.qa.graphene.indigo.analyze.OptionalStackingAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.ExportVisualizedDataToXLSXAndCSVTest;
import com.gooddata.qa.graphene.indigo.analyze.ExportVisualizedDataToXLSXAndCSVAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.PieChartReportTest;
import com.gooddata.qa.graphene.indigo.analyze.ComboChartReportTest;
import com.gooddata.qa.graphene.indigo.analyze.GroupingAndSubTotalsPivotTableTest;
import com.gooddata.qa.graphene.indigo.analyze.GroupingAndSubTotalsPivotTableAdvancedTest;
import com.gooddata.qa.graphene.indigo.analyze.ADFilterBarFlowTest;
import com.gooddata.qa.graphene.indigo.analyze.DateFilterADMeasureCombineDateTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("e2e", new Object[] {
            EmptyCatalogueTest.class,
            RecommendationsWithoutDateDimensionTest.class,
            AttributeFiltersTest.class,
            BucketsTest.class,
            E2ePivotTableTest.class,
            UndoTest.class,
            AttributeBasedMetricsTest.class,
            FactBasedMetricsTest.class,
            StackedChartsTest.class,
            ResetButtonTest.class,
            "testng-ad-e2e-metrics-test.xml",
            "testng-ad-e2e-visualization-test.xml",
            "testng-ad-e2e-recommendation-test.xml",
            "testng-ad-e2e-catalogue-test.xml",
            "testng-ad-e2e-date-test.xml"
        });

        suites.put("sanity", new Object[] {
            AnalyticalDesignerSanityTest.class,
            VisualizationMeasureAttributeTest.class,
            EventingBasicTest.class,
            BackwardCompatibilityTest.class,
            DateFilterADMeasureTest.class,
            ApplyColorPaletteOnAnalyzePageTest.class,
            ColorPalettePickerBasicInsightAndKPITest.class,
            ADFilterBarFlowTest.class
        });

        suites.put("all", new Object[] {
            GoodSalesAttributeBasedMetricTest.class,
            GoodSalesAttributeBucketTest.class,
            GoodSalesAttributeFilterTest.class,
            GoodSalesCatalogueTest.class,
            GoodSalesChartLegendTest.class,
            GoodSalesComparisonRecommendationTest.class,
            GoodSalesContributionRecommendationTest.class,
            CustomDateDimensionsTest.class,
            GoodSalesDateDimensionTest.class,
            DateFilterTest.class,
            GoodSalesDescriptionTest.class,
            GoodSalesDropAttributeTest.class,
            GoodSalesVisualizationTest.class,
            GoodSalesFactBasedMetricTest.class,
            GoodSalesMetricBucketTest.class,
            GoodSalesMetricFilterTest.class,
            MultipleDatasetsTest.class,
            GoodSalesShortcutRecommendationTest.class,
            SpecialCasesTest.class,
            GoodSalesPivotTableReportTest.class,
            GoodSalesTrendingRecommendationTest.class,
            GoodSalesUndoTest.class,
            GoodSalesUndoRedoSavedInsightTest.class,
//          WalkmeTest.class, CL-9704: Walkme is not available on new AD
            AnalyticalDesignerSanityTest.class,
            NonProductionDatasetInsightTest.class,
            GoodSalesNotRenderedInsightTest.class,
//          WalkmeOnEmbeddedAdTest.class, Will be unblocked by ticket QA-9656
            GoodSalesRelatedAndUnrelatedDateDimensionsTest.class,
            EmbeddedAdTest.class,
            GoodSalesInsightTest.class,
            GoodSalesSaveInsightTest.class,
            GoodSalesMetricVisibilityTest.class,
            GoodSalesProjectNavigationTest.class,
            GoodSalesOvertimeComparisonTest.class,
            TotalsResultWithInsightTest.class,
            VisualizationMeasureAttributeTest.class,
            EventingBasicTest.class,
            ContributionAndComparisionTest.class,
            EventingBasicFiltersTest.class,
            EventingFiltersUnderMetric.class,
            EventingSpecialCaseTest.class,
            DateFilterADMeasureExtendedTest.class,
            DateFilterADMeasureCombineDateTest.class,
            InsightCompareToPreviousPeriodTest.class,
            RenamingMeasuresAndAttributesTest.class,
            InsightCompareToSamePreviousPeriodYearTest.class,
            RedesigningDateFilterDialogOnFilterBucketTest.class,
            PivotTableTest.class,
            PivotTableAggregationTest.class,
            SecondaryAxisTest.class,
            PivotTableAdvancedTest.class,
            TreeMapChartReportTest.class,
            TreeMapChartReportAdvancedTest.class,
            HeatMapTest.class,
            HeatMapAdvancedTest.class,
            ReferencePointAndVisSwitchingTest.class,
            ApplyColorPaletteOnAnalyzePageTest.class,
            ColorPalettePickerBasicInsightAndKPITest.class,
            ColorPalettePickerAdvancedInsightAndKPITest.class,
            ColorPalettePickerUILayoutInsightAndKPITest.class,
            CustomColorPaletteUILayoutInsightAndKPITest.class,
            OptionalStackingTest.class,
            OptionalStackingAdvancedTest.class,
            ExportVisualizedDataToXLSXAndCSVTest.class,
            ExportVisualizedDataToXLSXAndCSVAdvancedTest.class,
            PieChartReportTest.class,
            ComboChartReportTest.class,
            GroupingAndSubTotalsPivotTableTest.class,
            GroupingAndSubTotalsPivotTableAdvancedTest.class,
            OrganisingMeasureAndAttributeCatalogue.class,
            "testng-ad-ChartLabelFormat.xml",
            "testng-ad-MetricNumberFormat.xml"
        });

        suites.put("crud", new Object[] {
            GoodSalesAttributeBasedMetricTest.class,
            GoodSalesAttributeBucketTest.class,
            GoodSalesCatalogueTest.class,
            GoodSalesDropAttributeTest.class,
            GoodSalesFactBasedMetricTest.class,
            GoodSalesMetricBucketTest.class,
            GoodSalesMetricFilterTest.class,
            GoodSalesUndoTest.class,
            GoodSalesUndoRedoSavedInsightTest.class,
            GoodSalesInsightTest.class,
            GoodSalesSaveInsightTest.class
        });

        suites.put("filters", new Object[] {
            GoodSalesAttributeFilterTest.class,
            DateFilterTest.class,
            GoodSalesMetricFilterTest.class,
            InsightCompareToPreviousPeriodTest.class
        });

        suites.put("total-results", new Object[] {
            TotalsResultWithInsightTest.class,
        });

        suites.put("drill-eventing", new Object[] {
            VisualizationMeasureAttributeTest.class,
            EventingBasicTest.class,
            EventingBasicFiltersTest.class,
            EventingFiltersUnderMetric.class,
            ContributionAndComparisionTest.class,
            EventingSpecialCaseTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
