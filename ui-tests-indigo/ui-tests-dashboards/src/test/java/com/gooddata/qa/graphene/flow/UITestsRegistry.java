package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.indigo.dashboards.CommonDateFilteringTest;
import com.gooddata.qa.graphene.indigo.dashboards.DataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.DateDatasetRecommendationTest;
import com.gooddata.qa.graphene.indigo.dashboards.DateFilterOnCategoryBucketTest;
import com.gooddata.qa.graphene.indigo.dashboards.DateFilteringOnInsightTest;
import com.gooddata.qa.graphene.indigo.dashboards.DeleteAttributeFilterTest;
import com.gooddata.qa.graphene.indigo.dashboards.DragWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.FilteringWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.EmbeddingDashboardPostMessageTest;
import com.gooddata.qa.graphene.indigo.dashboards.EmbeddingSingleDashboardTest;
import com.gooddata.qa.graphene.indigo.dashboards.HeaderTest;
import com.gooddata.qa.graphene.indigo.dashboards.InsightOnDashboardTest;
import com.gooddata.qa.graphene.indigo.dashboards.InvalidDateDataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.ManipulateWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.MetricsDropdownTest;
import com.gooddata.qa.graphene.indigo.dashboards.NonProductionDatasetTest;
import com.gooddata.qa.graphene.indigo.dashboards.PartialExportDashboardsTest;
import com.gooddata.qa.graphene.indigo.dashboards.ReorderInsightTest;
import com.gooddata.qa.graphene.indigo.dashboards.RoutingTest;
import com.gooddata.qa.graphene.indigo.dashboards.VisualizationsTest;
import com.gooddata.qa.graphene.indigo.dashboards.AttributeFilterManipulationTest;
import com.gooddata.qa.graphene.indigo.dashboards.AttributeFilterMiscTest;
import com.gooddata.qa.graphene.indigo.dashboards.KpiPermissionsTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity", new Object[] {
            ManipulateWidgetsTest.class,
            "testng-desktop-EditMode.xml",
            "testng-desktop-imap-KpiAlertEvaluate.xml",
            "testng-desktop-SplashScreen.xml"
        });

        suites.put("pull-request", new Object[] {
            DataSetTest.class,
            HeaderTest.class,
            ManipulateWidgetsTest.class,
            DragWidgetsTest.class,
            PartialExportDashboardsTest.class,
            MetricsDropdownTest.class,
            VisualizationsTest.class,
            ReorderInsightTest.class,
            NonProductionDatasetTest.class,
            InsightOnDashboardTest.class,
            RoutingTest.class,
            DateFilteringOnInsightTest.class,
            CommonDateFilteringTest.class,
            DateDatasetRecommendationTest.class,
            FilteringWidgetsTest.class,
            EmbeddingSingleDashboardTest.class,
            EmbeddingDashboardPostMessageTest.class,
            InvalidDateDataSetTest.class,
            DateFilterOnCategoryBucketTest.class,
            AttributeFilterManipulationTest.class,
            AttributeFilterMiscTest.class,
            DeleteAttributeFilterTest.class,
            KpiPermissionsTest.class,
            "testng-desktop-AttributeFiltering.xml",
            "testng-desktop-DateFiltering.xml",
            "testng-desktop-EditMode.xml",
            "testng-desktop-KpiDrillTo.xml",
            "testng-desktop-KpiPop.xml",
            "testng-desktop-SplashScreen.xml",
            "testng-desktop-MetricFormatting.xml",
            "testng-desktop-ResponsiveNavigation.xml",
            "testng-desktop-KpiPopChangeValueExceedLimit.xml",
            "testng-desktop-MetricsAccessibility.xml",
            "testng-desktop-ProjectSwitch.xml",
            "testng-desktop-EmptyErrorKpiValue.xml",
            "testng-desktop-imap-KpiAlert.xml",
            "testng-mobile-AttributeFiltering.xml",
            "testng-mobile-DateFiltering.xml",
            "testng-mobile-EditMode.xml",
            "testng-mobile-KpiDrillTo.xml",
            "testng-mobile-KpiPop.xml",
            "testng-mobile-MetricFormatting.xml",
            "testng-mobile-ResponsiveNavigation.xml",
            "testng-mobile-SplashScreen.xml",
            "testng-mobile-KpiPopChangeValueExceedLimit.xml",
            "testng-mobile-ProjectSwitch.xml"
        });

        suites.put("all", new Object[] {
            DataSetTest.class,
            HeaderTest.class,
            ManipulateWidgetsTest.class,
            DragWidgetsTest.class,
            PartialExportDashboardsTest.class,
            MetricsDropdownTest.class,
            VisualizationsTest.class,
            ReorderInsightTest.class,
            NonProductionDatasetTest.class,
            InsightOnDashboardTest.class,
            RoutingTest.class,
            DateFilteringOnInsightTest.class,
            CommonDateFilteringTest.class,
            DateDatasetRecommendationTest.class,
            FilteringWidgetsTest.class,
            EmbeddingSingleDashboardTest.class,
            EmbeddingDashboardPostMessageTest.class,
            InvalidDateDataSetTest.class,
            DateFilterOnCategoryBucketTest.class,
            AttributeFilterManipulationTest.class,
            AttributeFilterMiscTest.class,
            DeleteAttributeFilterTest.class,
            KpiPermissionsTest.class,
            "testng-desktop-AttributeFiltering.xml",
            "testng-desktop-DateFiltering.xml",
            "testng-desktop-EditMode.xml",
            "testng-desktop-imap-KpiAlertEvaluate.xml",
            "testng-desktop-KpiDrillTo.xml",
            "testng-desktop-KpiPop.xml",
            "testng-desktop-SplashScreen.xml",
            "testng-desktop-MetricFormatting.xml",
            "testng-desktop-ResponsiveNavigation.xml",
            "testng-desktop-KpiPopChangeValueExceedLimit.xml",
            "testng-desktop-MetricsAccessibility.xml",
            "testng-desktop-ProjectSwitch.xml",
            "testng-desktop-imap-KpiAlertNullValue.xml",
            "testng-desktop-imap-KpiValueFormatInAlertEmail.xml",
            "testng-desktop-imap-KpiAlertSpecialCaseTest.xml",
            "testng-desktop-EmptyErrorKpiValue.xml",
            "testng-desktop-imap-KpiAlert.xml",
            "testng-mobile-AttributeFiltering.xml",
            "testng-mobile-DateFiltering.xml",
            "testng-mobile-EditMode.xml",
            "testng-mobile-KpiDrillTo.xml",
            "testng-mobile-KpiPop.xml",
            "testng-mobile-MetricFormatting.xml",
            "testng-mobile-ResponsiveNavigation.xml",
            "testng-mobile-SplashScreen.xml",
            "testng-mobile-KpiPopChangeValueExceedLimit.xml",
            "testng-mobile-ProjectSwitch.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
