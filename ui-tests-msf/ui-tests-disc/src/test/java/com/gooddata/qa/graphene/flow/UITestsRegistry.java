package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.disc.DataloadSchedulesTest;
import com.gooddata.qa.graphene.disc.DeployProcessTest;
import com.gooddata.qa.graphene.disc.OverviewPageTest;
import com.gooddata.qa.graphene.disc.ProjectDetailTest;
import com.gooddata.qa.graphene.disc.ProjectsPageTest;
import com.gooddata.qa.graphene.disc.SanityTest;
import com.gooddata.qa.graphene.disc.SchedulesTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            OverviewPageTest.class,
            ProjectsPageTest.class,
            DeployProcessTest.class,
            SchedulesTest.class,
            ProjectDetailTest.class,
            DataloadSchedulesTest.class,
            "testng-imap-notification.xml",
            "testng-disc-auto-run.xml",
            "testng-disc-disabled-schedule.xml",
            "testng-disc-repeated-failures.xml"
        });

        suites.put("sanity", new Object[] {
            SanityTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
