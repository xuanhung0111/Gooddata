package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class SchedulesTests extends AbstractSchedulesTests {

    private static final String TRIGGER_SCHEDULE_MISSING = "Trigger schedule missing!";
    private final static String EXECUTION_HISTORY_EMPTY_STATE_MESSAGE =
            "No history available. This schedule has not been run yet.";
    private static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-schedule";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomInput() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule with Custom Input";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            List<Parameter> paramList =
                    Arrays.asList(
                            new Parameter().setParamName("param").setParamValue("value"),
                            new Parameter().setParamName("secure param")
                                    .setParamValue("secure value").setSecureParam());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(paramList).isConfirm();
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleForSpecificExecutable() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule for Specific Executable";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.getExecutableTabByProcessName(processName).click();
            waitForElementVisible(
                    projectDetailPage.getExecutableScheduleLink(Executables.DWHS2
                            .getExecutableName())).click();
            waitForElementVisible(scheduleForm.getRoot());
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().isConfirm();
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(
                    Executables.DWHS2));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleFromSchedulesList() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule from Schedule List";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.getNewScheduleLinkInSchedulesList(processName).click();
            waitForElementVisible(scheduleForm.getRoot());
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().isConfirm();
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(
                    Executables.DWHS1));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEveryWeekCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit Cron Time of Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYWEEK).setDayInWeek("Monday")
                            .setHourInDay("14").setMinuteInHour("30");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEveryDayCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Schedule every day";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("10")
                            .setMinuteInHour("30");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCronExpression() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Schedule with cron expression";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                            .setCronTimeExpression("*/20 * * * *");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkManualExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            scheduleDetail.assertManualRunExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkStopManualExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Stop Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.manualStop();
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeExecutableOfSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Change Executable of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeExecutable(Executables.FAILED_GRAPH, Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.setExecutable(Executables.FAILED_GRAPH).setScheduleName(
                    Executables.FAILED_GRAPH.getExecutableName()));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deleteSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Delete Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.deleteSchedule(ScheduleDetail.Confirmation.SAVE_CHANGES);
            waitForElementVisible(projectDetailPage.getRoot());
            waitForElementVisible(projectDetailPage.getScheduleTabByProcessName(processName))
                    .click();
            waitForElementVisible(projectDetailPage.checkEmptySchedulesList(processName));
            Assert.assertTrue(projectDetailPage.checkEmptySchedulesList(processName).isDisplayed());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeScheduleCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Change Cron Time of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleBuilder.setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            scheduleDetail.changeCronTime(scheduleBuilder.getCronTimeBuilder(),
                    Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleParameters() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit schedule parameters";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 = new Parameter().setParamName("param").setParamValue("value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value")
                            .setSecureParam();
            List<Parameter> paramList = Arrays.asList(param1, param2);
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(paramList);
            createAndAssertSchedule(scheduleBuilder);

            Parameter editedParam1 =
                    new Parameter().setParamName("param new name").setParamValue("value new");
            Parameter editedParam2 =
                    new Parameter().setParamName("secure param new").setParamValue(
                            "secure value new");
            scheduleDetail.editParameter(param1, editedParam1);
            scheduleDetail.editParameter(param2, editedParam2);
            scheduleDetail.confirmParamsChange(Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.editParam(param1, editedParam1).editParam(param2,
                    editedParam2));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void addNewParametersForSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Add New Parameters for Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            List<Parameter> paramList =
                    Arrays.asList(
                            new Parameter().setParamName("param 1").setParamValue("value 1"),
                            new Parameter().setParamName("secure param")
                                    .setParamValue("secure value").setSecureParam());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(paramList).isConfirm();
            createAndAssertSchedule(scheduleBuilder);

            List<Parameter> newParams =
                    Arrays.asList(
                            new Parameter().setParamName("param 2").setParamValue("value 2"),
                            new Parameter().setParamName("secure param 2")
                                    .setParamValue("secure value 2").setSecureParam());
            scheduleDetail.addNewParams(newParams, Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.setParameters(newParams));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithIncorrectCron() throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, "Create Schedule With Error Cron");

        CronTimeBuilder cronTimeBuilder =
                new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                        .setCronTimeExpression("* * *");
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.checkScheduleWithIncorrectCron(cronTimeBuilder);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithIncorrectCron() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit Schedule With Error Cron";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).isConfirm();
            createAndAssertSchedule(scheduleBuilder);

            CronTimeBuilder cronTimeBuilder =
                    new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                            .setCronTimeExpression("* * *");
            scheduleDetail.checkScheduleWithIncorrectCron(cronTimeBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBrokenSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Broken Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).isConfirm();
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
            scheduleDetail.clickOnCloseScheduleButton();

            String redeployedProcessName = "Redeployed Process";
            redeployProcess(processName, DeployPackages.BASIC, redeployedProcessName,
                    scheduleBuilder);

            projectDetailPage.checkBrokenScheduleSection(redeployedProcessName);
            assertBrokenSchedule(scheduleBuilder);

            brokenSchedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
            waitForElementVisible(scheduleDetail.getRoot());
            Executables newExecutable = Executables.FAILED_GRAPH;
            scheduleDetail.checkMessageInBrokenScheduleDetail(scheduleBuilder.getScheduleName());

            scheduleDetail.fixBrokenSchedule(newExecutable);
            assertSchedule(scheduleBuilder.setProcessName(redeployedProcessName)
                    .setScheduleName(Executables.FAILED_GRAPH.getExecutableName())
                    .setExecutable(newExecutable));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Delete Schedule Parameter";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 =
                    new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value")
                            .setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail
                    .removeParameter(Arrays.asList(param1, param2), Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.removeParam(param1).removeParam(param2));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Delete Schedule Parameter";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 =
                    new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value")
                            .setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.removeParameter(Arrays.asList(param1, param2),
                    Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkIncorrectRetryDelay() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Incorrect Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);
            scheduleDetail.addInvalidRetry("5");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelCreateSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Create Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().isCancel();
            createSchedule(scheduleBuilder);
            waitForElementNotPresent(scheduleForm.getRoot());

            waitForElementVisible(projectDetailPage.getRoot());
            projectDetailPage.assertActiveProcessInList(processName, DeployPackages.CLOUDCONNECT);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelChangeScheduleExecutable() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Change Executable";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeExecutable(Executables.FAILED_GRAPH, Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelChangeScheduleCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Change Cron Time of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeCronTime(
                    new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_15_MINUTES),
                    Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelAddRetryDelay() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.checkRescheduleMessageAndDefault();
            scheduleDetail.addValidRetry("15", Confirmation.CANCEL_CHANGES);;
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelEditScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Edit schedule parameters";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 =
                    new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value")
                            .setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.editParameter(param1, new Parameter().setParamName("edited param name")
                    .setParamValue("edited param value"));
            scheduleDetail.confirmParamsChange(Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Cancel Delete Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.deleteSchedule(Confirmation.CANCEL_CHANGES);
            waitForElementVisible(scheduleDetail.getRoot());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRemoveRetry() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.addValidRetry("15", Confirmation.SAVE_CHANGES);
            scheduleDetail.removeRetryDelay(Confirmation.CANCEL_CHANGES);
            scheduleDetail.removeRetryDelay(Confirmation.SAVE_CHANGES);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionHistoryEmptyState() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Execution History Empty State";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertNotNull(scheduleDetail.getExecutionHistoryEmptyState());
            assertEquals(EXECUTION_HISTORY_EMPTY_STATE_MESSAGE, scheduleDetail
                    .getExecutionHistoryEmptyState().getText());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleExecutionState() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInScheduledState());
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            openProjectDetailPage(getWorkingProject());
            createAndAssertSchedule(scheduleBuilder2);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInScheduledState());
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulExecutionGroup() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.repeatManualRunSuccessfulSchedule(3);
            scheduleDetail.checkOkExecutionGroup(3, 0);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName("Create Schedule With Custom Name")
                            .setScheduleName("Custom Schedule Name")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            String newScheduleName = "Custom Schedule Name";
            scheduleDetail.changeValidScheduleName(newScheduleName, Confirmation.SAVE_CHANGES);
            scheduleDetail.clickOnCloseScheduleButton();

            assertSchedule(scheduleBuilder.setScheduleName(newScheduleName));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEmptyCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule With Empty Custom Name";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            String validScheduleName = "Custom Schedule Name";
            ScheduleBuilder scheduleWithEmptyScheduleName =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName("")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            scheduleForm.createScheduleWithInvalidScheduleName(scheduleWithEmptyScheduleName,
                    validScheduleName);
            assertSchedule(scheduleWithEmptyScheduleName.setScheduleName(validScheduleName));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithEmptyCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit Schedule With Empty Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeInvalidScheduleName("");
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleNotUniqueName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule With Not Unique Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            String validScheduleName = "Custom Schedule Name";
            scheduleForm.createScheduleWithInvalidScheduleName(scheduleBuilder2, validScheduleName);
            assertSchedule(scheduleBuilder2.setScheduleName(validScheduleName));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithNotUniqueName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Create Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName("Custom Schedule Name")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            createSchedule(scheduleBuilder2);
            assertSchedule(scheduleBuilder2);
            scheduleDetail.changeInvalidScheduleName(Executables.SUCCESSFUL_GRAPH
                    .getExecutableName());
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelEditScheduleName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeValidScheduleName("Custom Schedule Name",
                    Confirmation.CANCEL_CHANGES);
            scheduleDetail.clickOnCloseScheduleButton();
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomNameForRubyScript() throws InterruptedException,
            JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Create Schedule With Custom Name For Ruby Script";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName("Schedule of Ruby script")
                            .setExecutable(Executables.RUBY1).isConfirm();
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInFailedOverview() throws InterruptedException,
            JSONException {
        checkScheduleNameInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInSuccessfulOverview() throws InterruptedException,
            JSONException {
        checkScheduleNameInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInRunningOverview() throws InterruptedException,
            JSONException {
        checkScheduleNameInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectWithOneSchedule() throws InterruptedException, JSONException {
        cleanProcessesInProjectDetail(testParams.getProjectId());

        try {
            String processName = "Check Schedule Trigger With 1 Schedule In Project";

            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkNoTriggerScheduleOptions();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerBySuccessfulSchedule() throws InterruptedException,
            JSONException {
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
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerByFailedSchedule() throws InterruptedException, JSONException {
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

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerInLoop() throws InterruptedException, JSONException {
        cleanProcessesInProjectDetail(testParams.getProjectId());

        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(processName, triggerScheduleBuilder,
                    dependentScheduleBuilder);

            browser.get(triggerScheduleBuilder.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkNoTriggerScheduleOptions();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerByItself() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = Executables.SUCCESSFUL_GRAPH.getExecutableName();
            Executables triggerScheduleExecutable = Executables.SUCCESSFUL_GRAPH;
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName(triggerScheduleName)
                            .setExecutable(triggerScheduleExecutable).isConfirm();
            prepareScheduleWithBasicPackage(scheduleBuilder);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkScheduleTriggerOptions(Arrays.asList(scheduleBuilder));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDisableDependentSchedule() throws InterruptedException, JSONException {
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

    @Test(dependsOnMethods = {"createProject"})
    public void checkMissingScheduleTrigger() throws InterruptedException, JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(processName, triggerScheduleBuilder,
                    dependentScheduleBuilder);

            browser.get(triggerScheduleBuilder.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.deleteSchedule(Confirmation.SAVE_CHANGES);

            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.selectScheduleTab(processName);
            assertEquals(schedulesTable.getScheduleCron(dependentScheduleBuilder.getScheduleName())
                    .getText(), TRIGGER_SCHEDULE_MISSING);
            browser.get(dependentScheduleBuilder.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkTriggerScheduleMissing();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
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

    private void checkScheduleNameInOverviewPage(OverviewProjectStates overviewState)
            throws JSONException {
        try {
            openProjectDetailPage(getWorkingProject());

            String processName = "Check Custom Schedule Name In Overview Page";
            String scheduleName = "Custom Schedule Name";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            Executables graph = null;
            switch (overviewState) {
                case FAILED:
                    graph = Executables.FAILED_GRAPH;
                    break;
                case SUCCESSFUL:
                    graph = Executables.SUCCESSFUL_GRAPH;
                    break;
                case RUNNING:
                    graph = Executables.LONG_TIME_RUNNING_GRAPH;
                    break;
                default:
                    graph = Executables.SUCCESSFUL_GRAPH;
                    break;
            }
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName(scheduleName)
                            .setExecutable(graph).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                            .setHourInDay("23").setMinuteInHour("59").isConfirm();
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.manualRun();
            if (overviewState.equals(OverviewProjectStates.RUNNING))
                assertTrue(scheduleDetail.isInRunningState());
            else {
                if (overviewState.equals(OverviewProjectStates.FAILED))
                    scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());
                else
                    scheduleDetail.assertSuccessfulExecution();
            }
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            openUrl(DISC_OVERVIEW_PAGE);
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(overviewState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewScheduleName(overviewState, getWorkingProject(),
                    scheduleBuilder.getScheduleUrl(), scheduleName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    private void prepareDataForTriggerScheduleTest(String processName,
            ScheduleBuilder triggerScheduleBuilder, ScheduleBuilder dependentScheduleBuilder)
            throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        triggerScheduleBuilder.setProcessName(processName)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                .setMinuteInHour("59");
        createAndAssertSchedule(triggerScheduleBuilder);
        triggerScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        dependentScheduleBuilder
                .setProcessName(processName)
                .setCronTime(ScheduleCronTimes.AFTER)
                .setTriggerScheduleGroup(processName)
                .setTriggerScheduleOption(
                        triggerScheduleBuilder.getExecutable().getExecutablePath());
        createAndAssertSchedule(dependentScheduleBuilder);
        dependentScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    private void manualRunTriggerSchedule(String scheduleUrl) throws InterruptedException {
        browser.get(scheduleUrl);
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.manualRun();
    }

    private void runSuccessfulTriggerSchedule(String scheduleUrl) throws InterruptedException {
        manualRunTriggerSchedule(scheduleUrl);
        scheduleDetail.assertSuccessfulExecution();
    }

    private int waitForAutoRunDependentSchedule(ScheduleBuilder scheduleBuilder)
            throws InterruptedException {
        browser.get(scheduleBuilder.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.tryToWaitForAutoRun(scheduleBuilder.getCronTimeBuilder());
        return scheduleDetail.getExecutionItemsNumber();
    }
}
