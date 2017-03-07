package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.time.LocalTime;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.common.AbstractDiscTest;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class LongRunTimeTest extends AbstractDiscTest {

    private static final String DISABLE_RECOMMEND_MESSAGE = "This schedule has failed for the 5th time. "
            + "We highly recommend disable this schedule until the issue is addressed. "
            + "If you want to disable the schedule, click here or read troubleshooting article for more information.";

    private static final String AUTO_DISABLED_MESSAGE = "This schedule has been automatically disabled following "
            + "its 30th consecutive failure. If you addressed the issue, you can enable it.";

    @DataProvider(name = "executableProvider")
    public Object[][] getExecutableProvider() {
        return new Object[][] {
            {Executable.SUCCESSFUL_GRAPH, ScheduleStatus.OK.toString()},
            {Executable.ERROR_GRAPH, ScheduleStatus.ERROR.toString()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "executableProvider")
    public void autoExecuteSchedule(Executable executable, String status) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalTime autoExecutionStartTime = LocalTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, executable, parseTimeToCronExpression(autoExecutionStartTime));

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule)
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
            LocalTime autoExecutionStartTime = LocalTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, Executable.ERROR_GRAPH,
                    parseTimeToCronExpression(autoExecutionStartTime));

            int retryInMinute = 15;
            ScheduleDetail scheduleDetail = initScheduleDetail(schedule)
                    .addRetryDelay(retryInMinute)
                    .saveChanges()
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.ERROR.toString());

            scheduleDetail.waitForAutoExecute(LocalTime.now().plusMinutes(retryInMinute)).waitForExecutionFinish();
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
            LocalTime autoExecutionStartTime = LocalTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, Executable.LONG_TIME_RUNNING_GRAPH,
                    parseTimeToCronExpression(autoExecutionStartTime));

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule)
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
            Schedule schedule = createSchedule(process, Executable.LONG_TIME_RUNNING_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule)
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
            LocalTime autoExecutionStartTime = LocalTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    parseTimeToCronExpression(autoExecutionStartTime));

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule).disableSchedule();
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

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleFailForManyTimes() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalTime executionStartTime = LocalTime.now().plusHours(1);
            Schedule schedule = createSchedule(process, Executable.SHORT_TIME_ERROR_GRAPH,
                    parseTimeToCronExpression(executionStartTime));

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
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
            Schedule schedule1 = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule2 = createSchedule(process, Executable.SUCCESSFUL_GRAPH, schedule1);

            initScheduleDetail(schedule1).executeSchedule().waitForExecutionFinish();

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule2);
            assertFalse(scheduleDetail.canAutoTriggered(LocalTime.now()), "Schedule is still triggered");
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
            Schedule schedule1 = createSchedule(process1, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule2 = createSchedule(process1, "Schedule2", Executable.SUCCESSFUL_GRAPH, schedule1);
            Schedule schedule3 = createSchedule(process2, "Schedule3", Executable.ERROR_GRAPH, schedule2);

            initScheduleDetail(schedule1).executeSchedule().close();
            projectDetailPage.getProcess(process1.getName()).openSchedule(schedule2.getName())
                    .waitForAutoExecute(LocalTime.now()).close();

            ScheduleDetail scheduleDetail = projectDetailPage.getProcess(process2.getName())
                    .openSchedule(schedule3.getName())
                    .waitForAutoExecute(LocalTime.now())
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
            Schedule schedule1 = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule2 = createSchedule(process, Executable.ERROR_GRAPH, schedule1);

            initScheduleDetail(schedule2).disableSchedule();
            initScheduleDetail(schedule1).executeSchedule().waitForExecutionFinish();

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule2);
            assertFalse(scheduleDetail.canAutoTriggered(LocalTime.now()), "Schedule is still triggered");
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 0);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }
}
