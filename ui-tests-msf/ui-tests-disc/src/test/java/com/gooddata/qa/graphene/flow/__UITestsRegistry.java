package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.disc.__LongRunTimeTest;
import com.gooddata.qa.graphene.disc.__OverviewPageTest;
import com.gooddata.qa.graphene.disc.__ProjectsDetailTest;
import com.gooddata.qa.graphene.disc.__ProjectsPageTest;
import com.gooddata.qa.graphene.disc.__SanityTest;
import com.gooddata.qa.graphene.disc.notification.__NotificationsTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByGitStoreTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByGraphTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByRubyScriptTest;
import com.gooddata.qa.graphene.disc.schedule.CreateScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.EditScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.ScheduleDetailTest;
import com.gooddata.qa.graphene.disc.schedule.dataload.CreateDataloadScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.dataload.DataloadDatasetDetailTest;
import com.gooddata.qa.graphene.disc.schedule.dataload.DataloadScheduleDetailTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class __UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            __OverviewPageTest.class,
            __ProjectsPageTest.class,
            __ProjectsDetailTest.class,
            __LongRunTimeTest.class,
            DeployProcessByGitStoreTest.class,
            DeployProcessByGraphTest.class,
            DeployProcessByRubyScriptTest.class,
            CreateScheduleTest.class,
            EditScheduleTest.class,
            ScheduleDetailTest.class,
            CreateDataloadScheduleTest.class,
            DataloadDatasetDetailTest.class,
            DataloadScheduleDetailTest.class,
            __NotificationsTest.class,
            "__testng-imap-notification.xml"
        });

        suites.put("orchestrator", new Object[] {
            __OverviewPageTest.class,
            __ProjectsPageTest.class,
            __ProjectsDetailTest.class,
            DeployProcessByGitStoreTest.class,
            DeployProcessByGraphTest.class,
            DeployProcessByRubyScriptTest.class,
            CreateScheduleTest.class,
            EditScheduleTest.class,
            ScheduleDetailTest.class,
            CreateDataloadScheduleTest.class,
            DataloadDatasetDetailTest.class,
            DataloadScheduleDetailTest.class,
            __NotificationsTest.class
        });

        suites.put("sanity", new Object[] {
            __SanityTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
