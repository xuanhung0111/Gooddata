package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;

import static org.testng.Assert.*;

public class LongRunTimeTests extends AbstractDISC {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-long-time-running-schedule";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleAutoRun() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Failed Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SHORT_TIME_FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.checkRepeatedFailureSchedule(scheduleBuilder.getCronTimeBuilder(),
                    scheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkScheduleTriggerByFailedSchedule() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.FAILED_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.SUCCESSFUL_GRAPH);
            prepareDataForTriggerScheduleTest(processName, triggerScheduleBuilder,
                    dependentScheduleBuilder);

            manualRunTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            scheduleDetail.assertFailedExecution(triggerScheduleBuilder.getExecutable());
            int dependentScheduleExecutionNumber =
                    waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertEquals(dependentScheduleExecutionNumber, 0);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkMultipleScheduleTriggers() throws JSONException, InterruptedException {
        try {
            String processName1 = "Check Schedule With Trigger Schedule 1";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setScheduleName("Trigger schedule").setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder1 =
                    new ScheduleBuilder().setScheduleName("Dependent schedule 1").setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            prepareDataForTriggerScheduleTest(processName1, triggerScheduleBuilder,
                    dependentScheduleBuilder1);

            String processName2 = "Check Schedule With Trigger Schedule 2";
            ScheduleBuilder dependentScheduleBuilder2 =
                    new ScheduleBuilder()
                            .setProcessName(processName2)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.AFTER)
                            .setTriggerScheduleGroup(dependentScheduleBuilder1.getProcessName())
                            .setTriggerScheduleOption(
                                    dependentScheduleBuilder1.getExecutable().getExecutablePath());
            prepareScheduleWithBasicPackage(dependentScheduleBuilder2);

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder1);
            scheduleDetail.assertSuccessfulExecution();
            waitForAutoRunDependentSchedule(dependentScheduleBuilder2);
            scheduleDetail.assertFailedExecution(dependentScheduleBuilder2.getExecutable());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkDisableDependentSchedule() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(processName, triggerScheduleBuilder,
                    dependentScheduleBuilder);

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            scheduleDetail.assertFailedExecution(dependentScheduleBuilder.getExecutable());
            scheduleDetail.disableSchedule();

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            int dependentScheduleExecutionNumber =
                    waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertEquals(dependentScheduleExecutionNumber, 1);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInFailedState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInSucessfulState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInRunningState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.RUNNING);
    }
}
