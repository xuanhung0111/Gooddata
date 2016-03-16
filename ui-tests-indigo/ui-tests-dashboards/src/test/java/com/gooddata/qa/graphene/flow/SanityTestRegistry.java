package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.dashboards.ManipulateWidgetsTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class SanityTestRegistry {

    public static void main(String[] args) throws Throwable {
        TestsRegistry.getInstance()
            .register(ManipulateWidgetsTest.class)
            .register("testng-desktop-permissions-EditMode.xml")
            .register("testng-desktop-imap-KpiAlertEvaluate.xml")
            .register("testng-desktop-permissions-SplashScreen.xml")
            .toTextFile();
    }
}
