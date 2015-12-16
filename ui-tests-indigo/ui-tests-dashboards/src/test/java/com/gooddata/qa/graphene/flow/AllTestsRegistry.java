package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.dashboards.AddKpiWithoutDateDimensionTest;
import com.gooddata.qa.graphene.indigo.dashboards.DateDimensionTest;
import com.gooddata.qa.graphene.indigo.dashboards.HeaderTest;
import com.gooddata.qa.graphene.indigo.dashboards.KpiAlertTest;
import com.gooddata.qa.graphene.indigo.dashboards.ManipulateWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.MetricFormattingTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class AllTestsRegistry {

    public static void main(String[] args) throws Throwable {
        TestsRegistry.getInstance()
                .register(DateDimensionTest.class)
                .register(HeaderTest.class)
                .register(KpiAlertTest.class)
                .register(ManipulateWidgetsTest.class)
                .register(MetricFormattingTest.class)
                .register(AddKpiWithoutDateDimensionTest.class)
                .register("testng-desktop-DateFiltering.xml")
                .register("testng-desktop-permissions-EditMode.xml")
                .register("testng-desktop-imap-KpiAlertEvaluate.xml")
                .register("testng-desktop-permissions-KpiDrillTo.xml")
                .register("testng-desktop-KpiPop.xml")
                .register("testng-desktop-permissions-SplashScreen.xml")
                .register("testng-desktop-ResponsiveNavigation.xml")
                .register("testng-mobile-DateFiltering.xml")
                .register("testng-mobile-EditMode.xml")
                .register("testng-mobile-KpiDrillTo.xml")
                .register("testng-mobile-KpiPop.xml")
                .register("testng-mobile-ResponsiveNavigation.xml")
                .register("testng-mobile-SplashScreen.xml")
                .toTextFile();
    }
}

