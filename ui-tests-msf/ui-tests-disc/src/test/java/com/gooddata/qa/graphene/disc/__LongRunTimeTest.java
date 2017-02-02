package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class __LongRunTimeTest extends __AbstractDISCTest {

    private static final String DISABLE_RECOMMEND_MESSAGE = "This schedule has failed for the 5th time. "
            + "We highly recommend disable this schedule until the issue is addressed. "
            + "If you want to disable the schedule, click here or read troubleshooting article for more information.";

    private static final String AUTO_DISABLED_MESSAGE = "This schedule has been automatically disabled following "
            + "its 30th consecutive failure. If you addressed the issue, you can enable it.";

    @DataProvider(name = "executableProvider")
    public Object[][] getExecutableProvider() {
        return new Object[][] {
            {__Executable.SUCCESSFUL_GRAPH, ScheduleStatus.OK.toString()},
            {__Executable.ERROR_GRAPH, ScheduleStatus.ERROR.toString()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "executableProvider")
    public void autoExecuteWithSuccessfulGraph(__Executable executable, String status) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            DateTime autoExecutionStartTime = DateTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, executable, parseDateToCronExpression(autoExecutionStartTime));

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish();

            takeScreenshot(browser, "Auto-execution-triggered-successfully-with-" + executable, getClass());
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), status);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkRetryExecution() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            DateTime autoExecutionStartTime = DateTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    parseDateToCronExpression(autoExecutionStartTime));

            int retryInMinute = 15;
            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .addRetryDelay(retryInMinute)
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.ERROR.toString());

            scheduleDetail.waitForAutoExecute(DateTime.now().plusMinutes(retryInMinute)).waitForExecutionFinish();
            takeScreenshot(browser, "Schedule-auto-retry-after-" + retryInMinute + "-minutes", getClass());
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 2);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.ERROR.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkStopAutoExecution() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            DateTime autoExecutionStartTime = DateTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, __Executable.LONG_TIME_RUNNING_GRAPH,
                    parseDateToCronExpression(autoExecutionStartTime));

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .waitForAutoExecute(autoExecutionStartTime)
                    .stopExecution();

            takeScreenshot(browser, "Auto-execution-stopped-successfully", getClass());
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), "MANUALLY STOPPED");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLongTimeExecution() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.LONG_TIME_RUNNING_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTE.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            DateTime autoExecutionStartTime = DateTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    parseDateToCronExpression(autoExecutionStartTime));

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule).disableSchedule();
            takeScreenshot(browser, "Schedule-is-disabled", getClass());
            assertTrue(scheduleDetail.isDisabled(), "Schedule is not disabled");
            assertEquals(scheduleDetail.getDisabledMessage(),
                    "This schedule is disabled and it will not run according to schedule until re-enabled.");
            assertFalse(scheduleDetail.canAutoTriggered(autoExecutionStartTime),
                    "Schedule execution can auto trigger although disabled");

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

            autoExecutionStartTime = DateTime.now().plusMinutes(2);
            scheduleDetail.enableSchedule()
                    .setRunTimeByCronExpression(parseDateToCronExpression(autoExecutionStartTime))
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 2);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleFailForManyTimes() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            DateTime executionStartTime = DateTime.now().plusHours(1);
            Schedule schedule = createSchedule(process, __Executable.SHORT_TIME_ERROR_GRAPH,
                    parseDateToCronExpression(executionStartTime));

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            executeScheduleWithSpecificTimes(scheduleDetail, 5);
            assertEquals(scheduleDetail.getDisableRecommendMessage(), DISABLE_RECOMMEND_MESSAGE);

            executeScheduleWithSpecificTimes(scheduleDetail, 25);
            assertEquals(scheduleDetail.getAutoDisabledMessage(), AUTO_DISABLED_MESSAGE);
            assertTrue(scheduleDetail.isDisabled(), "Schedule is not disabled");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleTriggeredByFailedSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule1 = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTE.getExpression());
            Schedule schedule2 = createSchedule(process, __Executable.SUCCESSFUL_GRAPH, schedule1);

            initScheduleDetail(schedule1).executeSchedule().waitForExecutionFinish();

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule2);
            assertFalse(scheduleDetail.canAutoTriggered(DateTime.now()), "Schedule is still triggered");
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 0);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkMultipleScheduleTriggers() {
        DataloadProcess process1 = createProcessWithBasicPackage(generateProcessName());
        DataloadProcess process2 = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule1 = createSchedule(process1, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTE.getExpression());
            Schedule schedule2 = createSchedule(process1, __Executable.SUCCESSFUL_GRAPH, schedule1);
            Schedule schedule3 = createSchedule(process2, __Executable.ERROR_GRAPH, schedule2);

            initScheduleDetail(schedule1).executeSchedule().waitForExecutionFinish();
            initScheduleDetail(schedule2).waitForAutoExecute(DateTime.now()).waitForExecutionFinish();

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule3)
                    .waitForAutoExecute(DateTime.now())
                    .waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.ERROR.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process1);
            deteleProcess(getGoodDataClient(), process2);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDisableDependentSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule1 = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTE.getExpression());
            Schedule schedule2 = createSchedule(process, __Executable.ERROR_GRAPH, schedule1);

            initScheduleDetail(schedule2).disableSchedule();
            initScheduleDetail(schedule1).executeSchedule().waitForExecutionFinish();

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule2);
            assertFalse(scheduleDetail.canAutoTriggered(DateTime.now()), "Schedule is still triggered");
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 0);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    private void executeScheduleWithSpecificTimes(__ScheduleDetailFragment scheduleDetail, int times) {
        for (int i = 1; i <= times; i++) {
            scheduleDetail.executeSchedule().waitForExecutionFinish();
        }
    }
}
