package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;
import com.gooddata.qa.utils.http.RestUtils;

public class SchedulesTest extends AbstractSchedulesTest {

    private static final String TRIGGER_SCHEDULE_MISSING = "Trigger schedule missing!";
    private final static String EXECUTION_HISTORY_EMPTY_STATE_MESSAGE =
            "No history available. This schedule has not been run yet.";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-schedule";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomInput() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
                            .setParameters(paramList).setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleForSpecificExecutable() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule for Specific Executable";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.getExecutableTabByProcessName(processName).click();
            waitForElementVisible(
                    projectDetailPage.getExecutableScheduleLink(Executables.DWHS2
                            .getExecutableName())).click();
            waitForElementVisible(scheduleForm.getRoot());
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(true);
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(
                    Executables.DWHS2));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleFromSchedulesList() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule from Schedule List";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.getNewScheduleLinkInSchedulesList(processName).click();
            waitForElementVisible(scheduleForm.getRoot());
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(true);
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(
                    Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEveryWeekCronTime() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit Cron Time of Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYWEEK).setDayInWeek("Monday")
                            .setHourInDay("14").setMinuteInHour("30");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEveryDayCronTime() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Schedule every day";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("10")
                            .setMinuteInHour("30");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCronExpression() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Schedule with cron expression";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                            .setCronTimeExpression("*/20 * * * *");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkManualExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkStopManualExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Stop Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState());
            /*
             * Wait for schedule execution is in running state for a few seconds to make sure that
             * the runtime field will be shown well
             */
            sleepTight(5000);
            scheduleDetail.manualStop();
            browser.navigate().refresh();
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeExecutableOfSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deleteSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeScheduleCronTime() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleParameters() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void addNewParametersForSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
                            .setParameters(paramList).setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);

            List<Parameter> newParams =
                    Arrays.asList(
                            new Parameter().setParamName("param 2").setParamValue("value 2"),
                            new Parameter().setParamName("secure param 2")
                                    .setParamValue("secure value 2").setSecureParam());
            scheduleDetail.addNewParams(newParams, Confirmation.SAVE_CHANGES);
            browser.navigate().refresh();
            waitForFragmentVisible(scheduleDetail);
            assertSchedule(scheduleBuilder.setParameters(newParams));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithIncorrectCron() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());

        deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, "Create Schedule With Error Cron");

        CronTimeBuilder cronTimeBuilder =
                new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                        .setCronTimeExpression("* * *");
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.checkScheduleWithIncorrectCron(cronTimeBuilder);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithIncorrectCron() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit Schedule With Error Cron";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);

            CronTimeBuilder cronTimeBuilder =
                    new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                            .setCronTimeExpression("* * *");
            scheduleDetail.checkScheduleWithIncorrectCron(cronTimeBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBrokenSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Broken Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setConfirmed(true);
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
            
            scheduleDetail.manualRun();
            scheduleDetail.assertFailedExecution(newExecutable);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBrokenScheduleWithRenamedGraph() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Broken Schedule With Deleted Graph";
            deployInProjectDetailPage(DeployPackages.ONE_GRAPH, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE);
            createAndAssertSchedule(scheduleBuilder);
            scheduleDetail.clickOnCloseScheduleButton();

            String redeployedProcessName = "Redeployed Process";
            redeployProcess(processName, DeployPackages.ONE_GRAPH_RENAMED,
                    redeployedProcessName, scheduleBuilder);

            projectDetailPage.checkBrokenScheduleSection(redeployedProcessName);
            assertBrokenSchedule(scheduleBuilder);

            brokenSchedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
            waitForElementVisible(scheduleDetail.getRoot());
            Executables newExecutable = Executables.SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE_RENAMED;
            scheduleDetail.checkMessageInBrokenScheduleDetail(scheduleBuilder.getScheduleName());

            scheduleDetail.fixBrokenSchedule(newExecutable);
            assertSchedule(scheduleBuilder.setProcessName(redeployedProcessName)
                    .setScheduleName(newExecutable.getExecutableName())
                    .setExecutable(newExecutable));
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteScheduleParams() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteScheduleParams() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkIncorrectRetryDelay() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Incorrect Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);
            scheduleDetail.addInvalidRetry("5");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelCreateSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Cancel Create Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(false);
            createSchedule(scheduleBuilder);
            waitForElementNotPresent(scheduleForm.getRoot());

            waitForElementVisible(projectDetailPage.getRoot());
            projectDetailPage.assertActiveProcessInList(processName, DeployPackages.CLOUDCONNECT);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelChangeScheduleExecutable() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Cancel Change Executable";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeExecutable(Executables.FAILED_GRAPH, Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelChangeScheduleCronTime() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Cancel Change Cron Time of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeCronTime(
                    new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_15_MINUTES),
                    Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelAddRetryDelay() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.checkRescheduleMessageAndDefault();
            scheduleDetail.addValidRetry("15", Confirmation.CANCEL_CHANGES);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelEditScheduleParams() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteSchedule() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Cancel Delete Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.deleteSchedule(Confirmation.CANCEL_CHANGES);
            waitForElementVisible(scheduleDetail.getRoot());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRemoveRetry() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.addValidRetry("15", Confirmation.SAVE_CHANGES);
            scheduleDetail.removeRetryDelay(Confirmation.CANCEL_CHANGES);
            scheduleDetail.removeRetryDelay(Confirmation.SAVE_CHANGES);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionHistoryEmptyState() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Execution History Empty State";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertNotNull(scheduleDetail.getExecutionHistoryEmptyState());
            assertEquals(EXECUTION_HISTORY_EMPTY_STATE_MESSAGE, scheduleDetail
                    .getExecutionHistoryEmptyState().getText());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleExecutionState() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.FAILED_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            openProjectDetailPage(getWorkingProject());
            createAndAssertSchedule(scheduleBuilder2);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulExecutionGroup() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.repeatManualRunSuccessfulSchedule(3);
            scheduleDetail.checkOkExecutionGroup(3, 0);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName("Create Schedule With Custom Name")
                            .setScheduleName("Custom Schedule Name")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithCustomName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            String newScheduleName = "Custom Schedule Name";
            scheduleDetail.changeValidScheduleName(newScheduleName, Confirmation.SAVE_CHANGES);
            scheduleDetail.clickOnCloseScheduleButton();

            assertSchedule(scheduleBuilder.setScheduleName(newScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEmptyCustomName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule With Empty Custom Name";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            String validScheduleName = "Custom Schedule Name";
            ScheduleBuilder scheduleWithEmptyScheduleName =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName("")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            scheduleForm.createScheduleWithInvalidScheduleName(scheduleWithEmptyScheduleName,
                    validScheduleName);
            assertSchedule(scheduleWithEmptyScheduleName.setScheduleName(validScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithEmptyCustomName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit Schedule With Empty Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeInvalidScheduleName("");
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleNotUniqueName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule With Not Unique Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithNotUniqueName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Create Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName("Custom Schedule Name")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            createSchedule(scheduleBuilder2);
            assertSchedule(scheduleBuilder2);
            scheduleDetail.changeInvalidScheduleName(Executables.SUCCESSFUL_GRAPH
                    .getExecutableName());
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelEditScheduleName() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeValidScheduleName("Custom Schedule Name",
                    Confirmation.CANCEL_CHANGES);
            scheduleDetail.clickOnCloseScheduleButton();
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomNameForRubyScript() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule With Custom Name For Ruby Script";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName("Schedule of Ruby script")
                            .setExecutable(Executables.RUBY1).setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInFailedOverview() {
        checkScheduleNameInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInSuccessfulOverview() {
        checkScheduleNameInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCustomScheduleNameInRunningOverview() {
        checkScheduleNameInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectWithOneSchedule() {
        cleanProcessesInWorkingProject();

        try {
            String processName = "Check Schedule Trigger With 1 Schedule In Project";

            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkNoTriggerScheduleOptions();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerBySuccessfulSchedule() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            scheduleDetail.assertFailedExecution(dependentScheduleBuilder.getExecutable());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerInLoop() {
        cleanProcessesInWorkingProject();

        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            browser.get(triggerScheduleBuilder.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkNoTriggerScheduleOptions();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerByItself() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = Executables.SUCCESSFUL_GRAPH.getExecutableName();
            Executables triggerScheduleExecutable = Executables.SUCCESSFUL_GRAPH;
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setScheduleName(triggerScheduleName)
                            .setExecutable(triggerScheduleExecutable).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkScheduleTriggerOptions(Arrays.asList(scheduleBuilder));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkMissingScheduleTrigger() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionLog() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Execution Log";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(successfulSchedule);

            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            RestUtils.verifyValidLink(getRestApiClient(), scheduleDetail.getLastExecutionLogLink());
            scheduleDetail.clickOnCloseScheduleButton();

            ScheduleBuilder failedSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            createSchedule(failedSchedule);

            scheduleDetail.manualRun();
            scheduleDetail.assertFailedExecution(failedSchedule.getExecutable());
            RestUtils.verifyValidLink(getRestApiClient(), scheduleDetail.getLastExecutionLogLink());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleNameWithPencilIcon() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Edit schedule name by pencil icon";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            String newScheduleName = "Schedule name by pencil";
            scheduleDetail.editScheduleNameByPencilIcon(newScheduleName, Confirmation.SAVE_CHANGES);
            scheduleDetail.assertSchedule(scheduleBuilder.setScheduleName(newScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEffectiveUserOfSchedule() throws JSONException, ParseException, IOException {
        try {
            addUserToProject(testParams.getEditorProfileUri(), UserRoles.ADMIN);
            addUserToProject(testParams.getViewerProfileUri(), UserRoles.ADMIN);

            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Effective User of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertTrue(scheduleDetail.getEffectiveUser().contains(testParams.getUser()),
                    "Incorrect effective user: " + scheduleDetail.getEffectiveUser());

            logout();
            signInAtGreyPages(testParams.getEditorUser(), testParams.getEditorPassword());

            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            assertTrue(scheduleDetail.getEffectiveUser().contains(testParams.getUser()),
                    "Incorrect effective user: " + scheduleDetail.getEffectiveUser());
            scheduleDetail.clickOnCloseScheduleButton();

            redeployProcess(processName, DeployPackages.BASIC, "Re-deploy to Check Effective User",
                    scheduleBuilder);
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            assertTrue(scheduleDetail.getEffectiveUser().contains(testParams.getEditorUser()),
                    "Incorrect effective user: " + scheduleDetail.getEffectiveUser());

            logout();
            signInAtGreyPages(testParams.getViewerUser(), testParams.getViewerPassword());

            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            assertTrue(scheduleDetail.getEffectiveUser().contains(testParams.getEditorUser()),
                    "Incorrect effective user: " + scheduleDetail.getEffectiveUser());
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionTooltipOnTimeLine() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Execution Tooltip On Timeline";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(successfulSchedule);

            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            assertTrue(scheduleDetail.isCorrectSuccessfulExecutionTooltip(), "Incorrect tooltip!");
            scheduleDetail.clickOnCloseScheduleButton();

            ScheduleBuilder failedSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            createSchedule(failedSchedule);
            scheduleDetail.manualRun();
            scheduleDetail.assertFailedExecution(failedSchedule.getExecutable());
            assertTrue(scheduleDetail.isCorrectFailedExecutionTooltip(failedSchedule.getExecutable()),
                    "Incorrect tooltip!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void closeScheduleDetail() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Close schedule detail";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            prepareScheduleWithBasicPackage(successfulSchedule);
            projectDetailPage.getExecutableTabByProcessName(processName).click();
            waitForFragmentNotVisible(scheduleDetail);

            openScheduleViaUrl(successfulSchedule.getScheduleUrl());
            projectDetailPage.getExecutableTabByProcessName(processName).click();
            waitForFragmentNotVisible(scheduleDetail);

            ScheduleBuilder failedSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
            createSchedule(failedSchedule);
            failedSchedule.setScheduleUrl(browser.getCurrentUrl());
            scheduleDetail.clickOnCloseScheduleButton();
            waitForFragmentNotVisible(scheduleDetail);

            openScheduleViaUrl(failedSchedule.getScheduleUrl());
            scheduleDetail.clickOnCloseScheduleButton();
            waitForFragmentNotVisible(scheduleDetail);

            ScheduleBuilder longTimeSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.LONG_TIME_RUNNING_GRAPH);
            createSchedule(longTimeSchedule);
            longTimeSchedule.setScheduleUrl(browser.getCurrentUrl());
            waitForFragmentVisible(schedulesTable).getScheduleTitle(successfulSchedule.getScheduleName()).click();
            Sleeper.sleepTightInSeconds(3);
            waitForFragmentVisible(scheduleDetail);
            assertSchedule(successfulSchedule);

            openScheduleViaUrl(longTimeSchedule.getScheduleUrl());
            waitForFragmentVisible(schedulesTable).getScheduleTitle(failedSchedule.getScheduleName()).click();
            Sleeper.sleepTightInSeconds(3);
            waitForFragmentVisible(scheduleDetail);
            assertSchedule(failedSchedule);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }
}
