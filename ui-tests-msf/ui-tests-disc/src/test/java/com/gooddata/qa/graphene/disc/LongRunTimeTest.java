package com.gooddata.qa.graphene.disc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;

public class LongRunTimeTest extends AbstractSchedulesTest {

    private static final String FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE =
            "This schedule has failed for the %dth time. We highly recommend disable this schedule until the issue is addressed. If you want to disable the schedule, click here or read troubleshooting article for more information.";
    private static final String AUTO_DISABLED_SCHEDULE_MESSAGE =
            "This schedule has been automatically disabled following its %dth consecutive failure. If you addressed the issue, you can enable it.";
    private static final String AUTO_DISABLED_SCHEDULE_MORE_INFO =
            "For more information read Automatic Disabling of Failed Schedules article at our support portal.";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Disc-test-long-time-running-schedule";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkScheduleAutoRun() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Auto Run Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkErrorExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Error Execution of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertFailedExecution(scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkRetryExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleBuilder.setRetryDelayInMinute(15);
            scheduleDetail.addValidRetry(String.valueOf(scheduleBuilder.getRetryDelay()),
                    Confirmation.SAVE_CHANGES);
            assertEquals(scheduleDetail.getRescheduleTime(), String.valueOf(scheduleBuilder.getRetryDelay()));
            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertFailedExecution(scheduleBuilder.getExecutable());
            assertTrue(scheduleDetail.waitForRetrySchedule(scheduleBuilder),
                    "Schedule is not re-run automatically well!");
            assertFailedExecution(scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkStopAutoExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Stop Auto Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            /*
             * Wait for schedule execution is in running state for a few seconds to make sure that
             * the runtime field will be shown well
             */
            Sleeper.sleepTightInSeconds(5);
            scheduleDetail.manualStop();
            assertManualStoppedExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkLongTimeExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Long Time Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"disabledSchedule"})
    public void disableSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Disable Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.disableSchedule();
            assertTrue(scheduleDetail.isDisabledSchedule(scheduleBuilder.getCronTimeBuilder()));
            scheduleDetail.manualRun();
            assertSuccessfulExecution();
            scheduleDetail.enableSchedule();
            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"repeatedFailures"})
    public void checkScheduleFailForManyTimes() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Failed Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SHORT_TIME_FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            // scheduleDetail.checkRepeatedFailureSchedule(scheduleBuilder.getCronTimeBuilder(),
            // scheduleBuilder.getExecutable());

            repeatManualRunFailedSchedule(5, scheduleBuilder.getExecutable());
            System.out.println("Schedule failed for the 5th time...");
            assertEquals(
                    String.format(FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE, scheduleDetail.getExecutionItemsNumber()),
                    scheduleDetail.getRepeatedFailureInfo());
            repeatManualRunFailedSchedule(25, scheduleBuilder.getExecutable());
            System.out.println("Schedule failed for the 30th time...");
            assertEquals(String.format(AUTO_DISABLED_SCHEDULE_MESSAGE, scheduleDetail.getExecutionItemsNumber()),
                    scheduleDetail.getAutoDisabledScheduleMessage());
            assertEquals(AUTO_DISABLED_SCHEDULE_MORE_INFO, scheduleDetail.getAutoDisabledScheduleMoreInfo());
            assertTrue(scheduleDetail.isDisabledSchedule(scheduleBuilder.getCronTimeBuilder()));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkScheduleTriggerByFailedSchedule() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            manualRunTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            assertFailedExecution(triggerScheduleBuilder.getExecutable());
            int dependentScheduleExecutionNumber = waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertEquals(dependentScheduleExecutionNumber, 0);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"autoRun"})
    public void checkMultipleScheduleTriggers() {
        try {
            String processName1 = "Check Schedule With Trigger Schedule 1";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName1).setScheduleName("Trigger schedule")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder1 =
                    new ScheduleBuilder().setProcessName(processName1).setScheduleName("Dependent schedule 1")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder1);

            String processName2 = "Check Schedule With Trigger Schedule 2";
            ScheduleBuilder dependentScheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName2).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.AFTER)
                            .setTriggerScheduleGroup(dependentScheduleBuilder1.getProcessName())
                            .setTriggerScheduleOption(dependentScheduleBuilder1.getScheduleName());
            prepareScheduleWithBasicPackage(dependentScheduleBuilder2);

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder1);
            assertSuccessfulExecution();
            waitForAutoRunDependentSchedule(dependentScheduleBuilder2);
            assertFailedExecution(dependentScheduleBuilder2.getExecutable());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"disabledSchedule"})
    public void checkDisableDependentSchedule() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertFailedExecution(dependentScheduleBuilder.getExecutable());
            scheduleDetail.disableSchedule();

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            int dependentScheduleExecutionNumber = waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertEquals(dependentScheduleExecutionNumber, 1);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }
}
