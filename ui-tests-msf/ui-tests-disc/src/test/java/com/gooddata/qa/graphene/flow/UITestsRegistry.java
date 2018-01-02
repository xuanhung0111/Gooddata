package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.disc.LongRunTimeTest;
import com.gooddata.qa.graphene.disc.OverviewPageTest;
import com.gooddata.qa.graphene.disc.SanityTest;
import com.gooddata.qa.graphene.disc.notification.NotificationsTest;
import com.gooddata.qa.graphene.disc.process.DeployEtlProcessTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByGitStoreTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByGraphTest;
import com.gooddata.qa.graphene.disc.process.DeployProcessByRubyScriptTest;
import com.gooddata.qa.graphene.disc.process.DeploySqlExecutorProcessTest;
import com.gooddata.qa.graphene.disc.project.ProjectsDetailTest;
import com.gooddata.qa.graphene.disc.project.ProjectsPageTest;
import com.gooddata.qa.graphene.disc.schedule.CreateEtlProcessScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.CreateScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.EditEtlProcessScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.EditScheduleTest;
import com.gooddata.qa.graphene.disc.schedule.EtlProcessScheduleDetailTest;
import com.gooddata.qa.graphene.disc.schedule.ScheduleDetailTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            OverviewPageTest.class,
            ProjectsPageTest.class,
            ProjectsDetailTest.class,
            LongRunTimeTest.class,
            DeployProcessByGitStoreTest.class,
            DeployProcessByGraphTest.class,
            DeployProcessByRubyScriptTest.class,
            DeployEtlProcessTest.class,
            DeploySqlExecutorProcessTest.class,
            CreateScheduleTest.class,
            CreateEtlProcessScheduleTest.class,
            EditScheduleTest.class,
            EditEtlProcessScheduleTest.class,
            ScheduleDetailTest.class,
            EtlProcessScheduleDetailTest.class,
            NotificationsTest.class,
            "testng-imap-notification.xml"
        });

        suites.put("orchestrator", new Object[] {
            OverviewPageTest.class,
            ProjectsPageTest.class,
            ProjectsDetailTest.class,
            DeployProcessByGitStoreTest.class,
            DeployProcessByGraphTest.class,
            DeployProcessByRubyScriptTest.class,
            DeployEtlProcessTest.class,
            DeploySqlExecutorProcessTest.class,
            CreateScheduleTest.class,
            CreateEtlProcessScheduleTest.class,
            EditScheduleTest.class,
            EditEtlProcessScheduleTest.class,
            ScheduleDetailTest.class,
            EtlProcessScheduleDetailTest.class,
            NotificationsTest.class
        });

        suites.put("sanity", new Object[] {
            SanityTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
