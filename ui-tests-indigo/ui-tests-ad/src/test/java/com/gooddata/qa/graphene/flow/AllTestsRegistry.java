package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.analyze.AnalyticalDesignerSanityTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesAttributeFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesChartLegendTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesComparisonRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesContributionRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesCustomDateDimensionsTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDateFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDescriptionTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesDropAttributeTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesFactBasedMetricTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricBucketTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMetricFilterTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesMultipleDatasetsTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesShortcutRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesTableReportTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesTrendingRecommendationTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoRedoSavedInsightTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesUndoTest;
import com.gooddata.qa.graphene.indigo.analyze.GoodSalesVisualizationTest;
import com.gooddata.qa.graphene.indigo.analyze.SpecialCasesTest;
//import com.gooddata.qa.graphene.indigo.analyze.WalkmeTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class AllTestsRegistry {

    public static void main(String[] args) throws Throwable {
        TestsRegistry.getInstance()
            .register(GoodSalesAttributeBasedMetricTest.class)
            .register(GoodSalesAttributeBucketTest.class)
            .register(GoodSalesAttributeFilterTest.class)
            .register(GoodSalesCatalogueTest.class)
            .register(GoodSalesChartLegendTest.class)
            .register(GoodSalesComparisonRecommendationTest.class)
            .register(GoodSalesContributionRecommendationTest.class)
            .register(GoodSalesCustomDateDimensionsTest.class)
            .register(GoodSalesDateDimensionTest.class)
            .register(GoodSalesDateFilterTest.class)
            .register(GoodSalesDescriptionTest.class)
            .register(GoodSalesDropAttributeTest.class)
            .register(GoodSalesVisualizationTest.class)
            .register(GoodSalesFactBasedMetricTest.class)
            .register(GoodSalesMetricBucketTest.class)
            .register(GoodSalesMetricFilterTest.class)
            .register(GoodSalesMultipleDatasetsTest.class)
            .register(GoodSalesShortcutRecommendationTest.class)
            .register(SpecialCasesTest.class)
            .register(GoodSalesTableReportTest.class)
            .register(GoodSalesTrendingRecommendationTest.class)
            .register(GoodSalesUndoTest.class)
            .register(GoodSalesUndoRedoSavedInsightTest.class)
//           .register(WalkmeTest.class) CL-9704: Walkme is not available on new AD
            .register(AnalyticalDesignerSanityTest.class)
            .register("testng-ad-permissions-MetricVisibility.xml")
            .register("testng-ad-permissions-ProjectNavigation.xml")
            .register("testng-ad-permissions-InsightTest.xml")
            .register("testng-ad-ChartLabelFormat.xml")
            .register("testng-ad-MetricNumberFormat.xml")
            .toTextFile();
    }
}
