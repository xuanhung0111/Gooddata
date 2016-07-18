package com.gooddata.qa.graphene.flow;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import com.gooddata.qa.graphene.indigo.analyze.AnalyticalDesignerSanityTest;
import com.gooddata.qa.graphene.indigo.analyze.CustomDateDimensionsTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesChartLegendTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesComparisonRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesContributionRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDateFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDescriptionTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDropAttributeTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesFactBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesShortcutRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesTableReportTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesTrendingRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoRedoSavedInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesVisualizationTest;
import com.gooddata.qa.graphene.indigo.analyze.MultipleDatasetsTest;
import com.gooddata.qa.graphene.indigo.analyze.NonProductionDatasetInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.SpecialCasesTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeFiltersTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.BucketsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.EmptyCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.ErrorStatesTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.FactBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.RecommendationsWithoutDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.ResetButtonTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.StackedChartsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.TableTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.UndoTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Set<Object> tests = new HashSet<>();

        for (String suite: args) {
            if ("e2e".equals(suite)) {
                tests.addAll(asList(
                    EmptyCatalogueTest.class,
                    RecommendationsWithoutDateDimensionTest.class,
                    AttributeFiltersTest.class,
                    BucketsTest.class,
                    ErrorStatesTest.class,
                    TableTest.class,
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
                ));
            } else if ("sanity".equals(suite)) {
                tests.addAll(asList(
                    AnalyticalDesignerSanityTest.class
                ));
            } else if ("all".equals(suite)) {
                tests.addAll(asList(
                    GoodSalesAttributeBasedMetricTest.class,
                    GoodSalesAttributeBucketTest.class,
                    GoodSalesAttributeFilterTest.class,
                    GoodSalesCatalogueTest.class,
                    GoodSalesChartLegendTest.class,
                    GoodSalesComparisonRecommendationTest.class,
                    GoodSalesContributionRecommendationTest.class,
                    CustomDateDimensionsTest.class,
                    GoodSalesDateDimensionTest.class,
                    GoodSalesDateFilterTest.class,
                    GoodSalesDescriptionTest.class,
                    GoodSalesDropAttributeTest.class,
                    GoodSalesVisualizationTest.class,
                    GoodSalesFactBasedMetricTest.class,
                    GoodSalesMetricBucketTest.class,
                    GoodSalesMetricFilterTest.class,
                    MultipleDatasetsTest.class,
                    GoodSalesShortcutRecommendationTest.class,
                    SpecialCasesTest.class,
                    GoodSalesTableReportTest.class,
                    GoodSalesTrendingRecommendationTest.class,
                    GoodSalesUndoTest.class,
                    GoodSalesUndoRedoSavedInsightTest.class,
//                  WalkmeTest.class, CL-9704: Walkme is not available on new AD
                    AnalyticalDesignerSanityTest.class,
                    NonProductionDatasetInsightTest.class,
                    "testng-ad-permissions-MetricVisibility.xml",
                    "testng-ad-permissions-ProjectNavigation.xml",
                    "testng-ad-permissions-InsightTest.xml",
                    "testng-ad-ChartLabelFormat.xml",
                    "testng-ad-MetricNumberFormat.xml"
                ));
            }
        }

        TestsRegistry.getInstance()
            .register(tests)
            .toTextFile();
    }
}
