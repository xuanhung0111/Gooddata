package com.gooddata.qa.graphene.disc;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;

import static org.testng.Assert.*;

public class LongTimeRunningSchedulesTests extends AbstractSchedulesTests {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-long-time-running-schedule";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleAutoRun() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Auto Run Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR)
                            .setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkErrorExecution() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Error Execution of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR)
                            .setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRetryExecution() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR)
                            .setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleBuilder.setRetryDelayInMinute(15);
            scheduleDetail.addValidRetry(String.valueOf(scheduleBuilder.getRetryDelay()),
                    Confirmation.SAVE_CHANGES);
            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());
            scheduleDetail.waitForRetrySchedule(scheduleBuilder);
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkStopAutoExecution() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Stop Auto Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR)
                            .setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.manualStop();
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLongTimeExecution() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Long Time Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSchedule() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Disable Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.disableSchedule();
            assertTrue(scheduleDetail.isDisabledSchedule(scheduleBuilder.getCronTimeBuilder()));
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            scheduleDetail.enableSchedule();
            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleFailForManyTimes() {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Failed Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.checkRepeatedFailureSchedule(scheduleBuilder.getCronTimeBuilder(),
                    scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

}
