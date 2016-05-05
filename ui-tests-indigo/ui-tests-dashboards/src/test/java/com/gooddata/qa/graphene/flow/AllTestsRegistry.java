package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.dashboards.AddKpiWithoutDataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.DataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.DragWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.HeaderTest;
import com.gooddata.qa.graphene.indigo.dashboards.KpiAlertTest;
import com.gooddata.qa.graphene.indigo.dashboards.ManipulateWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.MetricsDropdownTest;
import com.gooddata.qa.graphene.indigo.dashboards.PartialExportDashboardsTest;
import com.gooddata.qa.graphene.indigo.dashboards.VisualizationsTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class AllTestsRegistry {

    public static void main(String[] args) throws Throwable {
        TestsRegistry.getInstance()
                .register(DataSetTest.class)
                .register(HeaderTest.class)
                .register(KpiAlertTest.class)
                .register(ManipulateWidgetsTest.class)
                .register(DragWidgetsTest.class)
                .register(PartialExportDashboardsTest.class)
                .register(AddKpiWithoutDataSetTest.class)
                .register(MetricsDropdownTest.class)
                .register(VisualizationsTest.class)
                .register("testng-desktop-AttributeFiltering.xml")
                .register("testng-desktop-DateFiltering.xml")
                .register("testng-desktop-permissions-EditMode.xml")
                .register("testng-desktop-imap-KpiAlertEvaluate.xml")
                .register("testng-desktop-permissions-KpiDrillTo.xml")
                .register("testng-desktop-KpiPop.xml")
                .register("testng-desktop-permissions-SplashScreen.xml")
                .register("testng-desktop-MetricFormatting.xml")
                .register("testng-desktop-ResponsiveNavigation.xml")
                .register("testng-desktop-KpiPopChangeValueExceedLimit.xml")
                .register("testng-desktop-permissions-MetricsAccessibility.xml")
                .register("testng-desktop-ProjectSwitch.xml")
                .register("testng-desktop-imap-KpiAlertNullValue.xml")
                .register("testng-desktop-imap-KpiValueFormatInAlertEmail.xml")
                .register("testng-desktop-imap-KpiAlertSpecialCaseTest.xml")
                .register("testng-mobile-AttributeFiltering.xml")
                .register("testng-mobile-DateFiltering.xml")
                .register("testng-mobile-EditMode.xml")
                .register("testng-mobile-KpiDrillTo.xml")
                .register("testng-mobile-KpiPop.xml")
                .register("testng-mobile-MetricFormatting.xml")
                .register("testng-mobile-ResponsiveNavigation.xml")
                .register("testng-mobile-SplashScreen.xml")
                .register("testng-mobile-KpiPopChangeValueExceedLimit.xml")
                .register("testng-mobile-ProjectSwitch.xml")
                .toTextFile();
    }
}

