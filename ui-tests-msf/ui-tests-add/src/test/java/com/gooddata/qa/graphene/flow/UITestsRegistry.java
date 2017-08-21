package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.add.DataloadProcessTest;
import com.gooddata.qa.graphene.add.DataloadResourcesPermissionTest;
import com.gooddata.qa.graphene.add.SqlDiffTest;
import com.gooddata.qa.graphene.add.schedule.CreateScheduleTest;
import com.gooddata.qa.graphene.add.schedule.execution.dialog.LoadDatasetWithoutTSColumnTest;
import com.gooddata.qa.graphene.add.schedule.IncrementalLoadTest;
import com.gooddata.qa.graphene.add.schedule.LoadDatasetTest;
import com.gooddata.qa.graphene.add.schedule.PresenceOfTimestampColumnTest;
import com.gooddata.qa.graphene.add.schedule.ScheduleDetailTest;
import com.gooddata.qa.graphene.add.schedule.execution.dialog.DefaultLoadTest;
import com.gooddata.qa.graphene.add.schedule.execution.dialog.ForceIncrementalLoadTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            CreateScheduleTest.class,
            ScheduleDetailTest.class,
            LoadDatasetWithoutTSColumnTest.class,
            LoadDatasetTest.class,
            SqlDiffTest.class,
            DataloadProcessTest.class,
            DataloadResourcesPermissionTest.class,
            IncrementalLoadTest.class,
            DefaultLoadTest.class,
            ForceIncrementalLoadTest.class,
            PresenceOfTimestampColumnTest.class,
            "testng-imap-notification.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
