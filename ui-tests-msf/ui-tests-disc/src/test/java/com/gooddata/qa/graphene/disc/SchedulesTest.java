package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;
import com.gooddata.qa.graphene.utils.Sleeper;

public class SchedulesTest extends AbstractSchedulesTest {

    private static final String TRIGGER_SCHEDULE_MISSING = "Trigger schedule missing!";
    private static final String TRIGGER_SCHEDULE_MISSING_MESSAGE =
            "The schedule that triggers this schedule is missing. To run this schedule, set a new trigger or select a cron frequency.";
    private final static String EXECUTION_HISTORY_EMPTY_STATE_MESSAGE =
            "No history available. This schedule has not been run yet.";
    private final static String BROKEN_SCHEDULE_SECTION_MESSAGE = "The schedules cannot be executed. "
            + "Its process has been re-deployed with modified graphs or a different folder structure.";
    private static final String DEFAULT_RETRY_DELAY_VALUE = "30";
    private static final String RESCHEDULE_FORM_MESSAGE =
            "Restart every minutes until success (or 30th consecutive failure)";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-schedule";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomInput() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule with Custom Input";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            List<Parameter> paramList =
                    Arrays.asList(new Parameter().setParamName("param").setParamValue("value"), new Parameter()
                            .setParamName("secure param").setParamValue("secure value").setSecureParam());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setParameters(paramList)
                            .setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleForSpecificExecutable() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule for Specific Executable";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.clickOnExecutableTab();
            projectDetailPage.clickOnExecutableScheduleLink(Executables.DWHS2.getExecutableName());
            waitForFragmentVisible(scheduleForm);
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(true);
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(Executables.DWHS2));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleFromSchedulesList() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule from Schedule List";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            projectDetailPage.clickOnNewScheduleLinkInScheduleTab();
            waitForFragmentVisible(scheduleForm);
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(true);
            scheduleForm.createNewSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setProcessName(processName).setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithEveryWeekCronTime() {
        try {
            openProjectDetailPage(testParams.getProjectId());

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
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Schedule every day";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                            .setHourInDay("10").setMinuteInHour("30");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCronExpression() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Schedule with cron expression";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setCronTime(ScheduleCronTimes.CRON_EXPRESSION).setCronTimeExpression("*/20 * * * *");
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkStopManualExecution() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Stop Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState(), "Schedule is not in RUNNING state!");
            /*
             * Wait for schedule execution is in running state for a few seconds to make sure that
             * the runtime field will be shown well
             */
            sleepTight(5000);
            scheduleDetail.manualStop();
            browser.navigate().refresh();
            waitForFragmentVisible(scheduleDetail);
            assertManualStoppedExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeExecutableOfSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Change Executable of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
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
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Delete Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.deleteSchedule(ScheduleDetail.Confirmation.SAVE_CHANGES);
            waitForFragmentVisible(projectDetailPage);
            projectDetailPage.activeProcess(processName).clickOnScheduleTab();
            assertTrue(projectDetailPage.activeProcess(processName).isEmptyScheduleList(),
                    "Schedule list is not empty!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeScheduleCronTime() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Change Cron Time of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleBuilder.setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            scheduleDetail.changeCronTime(scheduleBuilder.getCronTimeBuilder(), Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleParameters() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit schedule parameters";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 = new Parameter().setParamName("param").setParamValue("value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value").setSecureParam();
            List<Parameter> paramList = Arrays.asList(param1, param2);
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setParameters(paramList);
            createAndAssertSchedule(scheduleBuilder);

            Parameter editedParam1 = new Parameter().setParamName("param new name").setParamValue("value new");
            Parameter editedParam2 =
                    new Parameter().setParamName("secure param new").setParamValue("secure value new");
            scheduleDetail.editParameter(param1, editedParam1);
            scheduleDetail.editParameter(param2, editedParam2);
            scheduleDetail.confirmParamsChange(Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.editParam(param1, editedParam1).editParam(param2, editedParam2));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void addNewParametersForSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Add New Parameters for Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            List<Parameter> paramList =
                    Arrays.asList(new Parameter().setParamName("param 1").setParamValue("value 1"),
                            new Parameter().setParamName("secure param").setParamValue("secure value")
                                    .setSecureParam());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setParameters(paramList)
                            .setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);

            List<Parameter> newParams =
                    Arrays.asList(new Parameter().setParamName("param 2").setParamValue("value 2"),
                            new Parameter().setParamName("secure param 2").setParamValue("secure value 2")
                                    .setSecureParam());
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
        openProjectDetailPage(testParams.getProjectId());

        deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, "Create Schedule With Error Cron");

        CronTimeBuilder cronTimeBuilder =
                new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                        .setCronTimeExpression("* * *");
        projectDetailPage.clickOnNewScheduleButton();

        waitForFragmentVisible(scheduleForm);
        scheduleForm.selectCron(cronTimeBuilder);
        scheduleForm.getRoot().click();
        scheduleForm.getCronExpressionTextBox().click();
        assertTrue(scheduleForm.cronExpressionTextHasError(), "Error is not shown for incorrect cron expression!");
        assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(),
                "Inserted cron format is invalid. Please verify and try again.");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithIncorrectCron() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit Schedule With Error Cron";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);

            CronTimeBuilder cronTimeBuilder =
                    new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION).setCronTimeExpression(
                            "* * *");

            waitForFragmentVisible(scheduleDetail);
            scheduleDetail.selectCron(cronTimeBuilder);
            scheduleDetail.getRoot().click();
            scheduleDetail.getCronExpressionTextBox().click();
            assertTrue(scheduleDetail.cronExpressionTextHasError(),
                    "Error is not shown for incorrect cron expression!");
            assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(),
                    "Inserted cron format is invalid. Please verify and try again.");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBrokenSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Broken Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(processName).setConfirmed(true);
            createSchedule(scheduleBuilder);
            assertSchedule(scheduleBuilder.setExecutable(Executables.DWHS1));
            scheduleDetail.clickOnCloseScheduleButton();

            String redeployedProcessName = "Redeployed Process";
            redeployProcess(processName, DeployPackages.BASIC, redeployedProcessName, scheduleBuilder);

            checkBrokenScheduleSection(redeployedProcessName);
            assertBrokenSchedule(scheduleBuilder);

            brokenSchedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
            waitForFragmentVisible(scheduleDetail);
            Executables newExecutable = Executables.FAILED_GRAPH;
            assertTrue(scheduleDetail.isCorrectMessageOnBrokenScheduleDetail(scheduleBuilder.getScheduleName()),
                    "Incorrect message on broken schedule!");

            scheduleDetail.fixBrokenSchedule(newExecutable);
            assertSchedule(scheduleBuilder.setProcessName(redeployedProcessName)
                    .setScheduleName(Executables.FAILED_GRAPH.getExecutableName()).setExecutable(newExecutable));

            scheduleDetail.manualRun();
            assertFailedExecution(newExecutable);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBrokenScheduleWithRenamedGraph() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Broken Schedule With Deleted Graph";
            deployInProjectDetailPage(DeployPackages.ONE_GRAPH, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE);
            createAndAssertSchedule(scheduleBuilder);
            scheduleDetail.clickOnCloseScheduleButton();

            String redeployedProcessName = "Redeployed Process";
            redeployProcess(processName, DeployPackages.ONE_GRAPH_RENAMED, redeployedProcessName, scheduleBuilder);

            checkBrokenScheduleSection(redeployedProcessName);
            assertBrokenSchedule(scheduleBuilder);

            brokenSchedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
            waitForFragmentVisible(scheduleDetail);
            Executables newExecutable = Executables.SUCCESSFUL_GRAPH_FOR_BROKEN_SCHEDULE_RENAMED;
            assertTrue(scheduleDetail.isCorrectMessageOnBrokenScheduleDetail(scheduleBuilder.getScheduleName()),
                    "Incorrect message on broken schedule!");

            scheduleDetail.fixBrokenSchedule(newExecutable);
            assertSchedule(scheduleBuilder.setProcessName(redeployedProcessName)
                    .setScheduleName(newExecutable.getExecutableName()).setExecutable(newExecutable));
            scheduleDetail.manualRun();
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteScheduleParams() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Delete Schedule Parameter";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 = new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value").setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.removeParameter(Arrays.asList(param1, param2), Confirmation.SAVE_CHANGES);
            assertSchedule(scheduleBuilder.removeParam(param1).removeParam(param2));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteScheduleParams() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Delete Schedule Parameter";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 = new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value").setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.removeParameter(Arrays.asList(param1, param2), Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkIncorrectRetryDelay() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Incorrect Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);
            scheduleDetail.addRetry("5");
            scheduleDetail.saveRetryDelay();
            assertTrue(scheduleDetail.isRetryErrorDisplayed(), "Error is not shown for invalid retry time!");
            String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
            System.out.println("Error retry delay: " + errorBubbleMessage);
            assertTrue(
                    errorBubbleMessage
                            .matches("The minimal delay is every 15 minutes.([\\n]*[\\r]*)Use numbers only."),
                    "Incorrect error bubble message!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelCreateSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Create Schedule";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setConfirmed(false);
            createSchedule(scheduleBuilder);
            waitForFragmentNotVisible(scheduleForm);

            waitForFragmentVisible(projectDetailPage);
            assertActiveProcessInList(processName, DeployPackages.CLOUDCONNECT);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelChangeScheduleExecutable() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Change Executable";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
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
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Change Cron Time of Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeCronTime(new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_15_MINUTES),
                    Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelAddRetryDelay() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_30_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.clickOnAddRetryButton();
            assertEquals(scheduleDetail.getRescheduleFormMessage(), RESCHEDULE_FORM_MESSAGE,
                    "Incorrect reschedule form message!");
            assertEquals(scheduleDetail.getRescheduleTime(), DEFAULT_RETRY_DELAY_VALUE,
                    "Incorrect default reschedule time value!");
            scheduleDetail.cancelAddRetryDelay();
            scheduleDetail.addValidRetry("15", Confirmation.CANCEL_CHANGES);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelEditScheduleParams() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Edit schedule parameters";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            Parameter param1 = new Parameter().setParamName("param name").setParamValue("param value");
            Parameter param2 =
                    new Parameter().setParamName("secure param").setParamValue("secure value").setSecureParam();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(Arrays.asList(param1, param2));
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.editParameter(param1,
                    new Parameter().setParamName("edited param name").setParamValue("edited param value"));
            scheduleDetail.confirmParamsChange(Confirmation.CANCEL_CHANGES);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelDeleteSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Cancel Delete Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.deleteSchedule(Confirmation.CANCEL_CHANGES);
            waitForFragmentVisible(scheduleDetail);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRemoveRetry() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Retry Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.addValidRetry("15", Confirmation.SAVE_CHANGES);
            assertEquals(scheduleDetail.getRescheduleTime(), "15", "Incorrect reschedule time!");
            scheduleDetail.removeRetryDelay(Confirmation.CANCEL_CHANGES);
            scheduleDetail.removeRetryDelay(Confirmation.SAVE_CHANGES);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionHistoryEmptyState() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Execution History Empty State";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertNotNull(scheduleDetail.getExecutionHistoryEmptyState(),
                    "Empty execution history is not shown for new created schedule!");
            assertEquals(scheduleDetail.getExecutionHistoryEmptyState().getText(),
                    EXECUTION_HISTORY_EMPTY_STATE_MESSAGE);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleExecutionState() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState(), "Schedule is not in RUNNING state!");
            assertFailedExecution(scheduleBuilder.getExecutable());

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            openProjectDetailPage(testParams.getProjectId());
            createAndAssertSchedule(scheduleBuilder2);

            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInRunningState(), "Schedule is not in RUNNING state!");
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulExecutionGroup() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Schedule Execution State";

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            for (int i = 0; i < 3; i++) {
                scheduleDetail.manualRun();
                assertSuccessfulExecution();
            }
            checkOkExecutionGroup(3, 0);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomName() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName("Create Schedule With Custom Name")
                            .setScheduleName("Custom Schedule Name").setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithCustomName() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
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
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule With Empty Custom Name";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForFragmentVisible(scheduleForm);
            String validScheduleName = "Custom Schedule Name";
            ScheduleBuilder scheduleWithEmptyScheduleName =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName("")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            createScheduleWithInvalidScheduleName(scheduleWithEmptyScheduleName, validScheduleName);
            assertSchedule(scheduleWithEmptyScheduleName.setScheduleName(validScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithEmptyCustomName() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit Schedule With Empty Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            changeInvalidScheduleName("");
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleNotUniqueName() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule With Not Unique Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            projectDetailPage.clickOnNewScheduleButton();
            waitForFragmentVisible(scheduleForm);
            String validScheduleName = "Custom Schedule Name";
            createScheduleWithInvalidScheduleName(scheduleBuilder2, validScheduleName);
            assertSchedule(scheduleBuilder2.setScheduleName(validScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleWithNotUniqueName() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Create Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName("Custom Schedule Name")
                            .setExecutable(Executables.SUCCESSFUL_GRAPH).setConfirmed(true);
            createSchedule(scheduleBuilder2);
            assertSchedule(scheduleBuilder2);
            changeInvalidScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName());
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelEditScheduleName() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit Schedule With Custom Name";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.changeValidScheduleName("Custom Schedule Name", Confirmation.CANCEL_CHANGES);
            scheduleDetail.clickOnCloseScheduleButton();
            assertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createScheduleWithCustomNameForRubyScript() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule With Custom Name For Ruby Script";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName("Schedule of Ruby script")
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

            openProjectDetailPage(testParams.getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.clickOnNewScheduleButton();
            checkNoTriggerScheduleOptions();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerBySuccessfulSchedule() {
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
            scheduleDetail.clickOnCloseScheduleButton();
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
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            browser.get(triggerScheduleBuilder.getScheduleUrl());

            waitForFragmentVisible(scheduleDetail);
            scheduleDetail.selectCronType(ScheduleCronTimes.AFTER);
            assertEquals(scheduleDetail.getTriggerScheduleMessage(), EMPTY_SCHEDULE_TRIGGER_MESSAGE,
                    "Incorrect empty schedule trigger message!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduleTriggerByItself() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = Executables.SUCCESSFUL_GRAPH.getExecutableName();
            Executables triggerScheduleExecutable = Executables.SUCCESSFUL_GRAPH;
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName(triggerScheduleName)
                            .setExecutable(triggerScheduleExecutable).setConfirmed(true);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            projectDetailPage.clickOnNewScheduleButton();
            waitForFragmentVisible(scheduleForm);
            scheduleForm.selectCronType(ScheduleCronTimes.AFTER);
            assertTrue(scheduleForm.isCorrectTriggerScheduleList(Arrays.asList(scheduleBuilder)),
                    "Incorrect trigger schedule list!");
            scheduleForm.clickOnCancelLink();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkMissingScheduleTrigger() {
        try {
            String processName = "Check Schedule With Trigger Schedule";

            ScheduleBuilder triggerScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            ScheduleBuilder dependentScheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
            prepareDataForTriggerScheduleTest(triggerScheduleBuilder, dependentScheduleBuilder);

            browser.get(triggerScheduleBuilder.getScheduleUrl());
            waitForFragmentVisible(scheduleDetail);
            scheduleDetail.deleteSchedule(Confirmation.SAVE_CHANGES);

            openProjectDetailPage(testParams.getProjectId());
            projectDetailPage.activeProcess(processName).clickOnScheduleTab();
            assertEquals(schedulesTable.getScheduleCron(dependentScheduleBuilder.getScheduleName()).getText(),
                    TRIGGER_SCHEDULE_MISSING, "Incorrect missing trigger schedule message on project detail page!");
            browser.get(dependentScheduleBuilder.getScheduleUrl());
            waitForFragmentVisible(scheduleDetail);
            assertEquals(scheduleDetail.getTriggerScheduleMissingMessage(), TRIGGER_SCHEDULE_MISSING_MESSAGE,
                    "Incorrect missing trigger schedule message on schedule detail page!");
            assertTrue(
                    scheduleDetail.isCorrectCronTime(new CronTimeBuilder().setCronTime(
                            ScheduleCronTimes.CRON_EXPRESSION).setCronTimeExpression("0 * * * *")),
                    "Incorrect cron time is set for missing trigger schedule!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutionLog() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Execution Log";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(successfulSchedule);

            scheduleDetail.manualRun();
            assertSuccessfulExecution();
            verifyValidLink(getRestApiClient(), scheduleDetail.getLastExecutionLogLink());
            scheduleDetail.clickOnCloseScheduleButton();

            ScheduleBuilder failedSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            createSchedule(failedSchedule);

            scheduleDetail.manualRun();
            assertFailedExecution(failedSchedule.getExecutable());
            verifyValidLink(getRestApiClient(), scheduleDetail.getLastExecutionLogLink());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editScheduleNameWithPencilIcon() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Edit schedule name by pencil icon";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            String newScheduleName = "Schedule name by pencil";
            scheduleDetail.editScheduleNameByPencilIcon(newScheduleName, Confirmation.SAVE_CHANGES);
            assertScheduleDetails(scheduleBuilder.setScheduleName(newScheduleName));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEffectiveUserOfSchedule() throws JSONException, ParseException, IOException {
        try {
            addUserToProject(testParams.getEditorUser(), UserRoles.ADMIN);
            addUserToProject(testParams.getViewerUser(), UserRoles.ADMIN);

            openProjectDetailPage(testParams.getProjectId());

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
            assertSuccessfulExecution();
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
            assertSuccessfulExecution();
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
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Execution Tooltip On Timeline";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            prepareScheduleWithBasicPackage(successfulSchedule);

            scheduleDetail.manualRun();
            assertSuccessfulExecution();
            assertTrue(scheduleDetail.isCorrectSuccessfulExecutionTooltip(), "Incorrect tooltip!");
            scheduleDetail.clickOnCloseScheduleButton();

            ScheduleBuilder failedSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
            createSchedule(failedSchedule);
            scheduleDetail.manualRun();
            assertFailedExecution(failedSchedule.getExecutable());
            assertTrue(scheduleDetail.isCorrectFailedExecutionTooltip(failedSchedule.getExecutable()),
                    "Incorrect tooltip!");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void closeScheduleDetail() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Close schedule detail";
            ScheduleBuilder successfulSchedule =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
            prepareScheduleWithBasicPackage(successfulSchedule);
            projectDetailPage.clickOnExecutableTab();
            waitForFragmentNotVisible(scheduleDetail);

            openScheduleViaUrl(successfulSchedule.getScheduleUrl());
            projectDetailPage.clickOnExecutableTab();
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

    private void checkBrokenScheduleSection(String processName) {
        projectDetailPage.activeProcess(processName);
        projectDetailPage.clickOnScheduleTab();
        System.out.println("Broken schedule message in project detail page: "
                + projectDetailPage.getBrokenScheduleMessage());
        assertEquals(projectDetailPage.getBrokenScheduleMessage(), BROKEN_SCHEDULE_SECTION_MESSAGE,
                "Incorrect broken schedule message!");
    }
}
