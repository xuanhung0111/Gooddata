package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCOverviewProjectStates;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class SchedulesTests extends AbstractSchedulesTests {

    private static final String TRIGGER_SCHEDULE_MISSING = "Trigger schedule missing!";
    private final static String EXECUTION_HISTORY_EMPTY_STATE_MESSAGE =
            "No history available. This schedule has not been run yet.";
    private static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";
    private Pair<String, List<String>> defaultCronTime = Pair.of(
            ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);;

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-schedule";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithCustomInput() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Create Schedule with Custom Input",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param", Arrays.asList("", "value"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Create Schedule with Custom Input", null, "/graph/DWHS2.grf", cronTime,
                    parameters);
            assertNewSchedule("Create Schedule with Custom Input", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleForSpecificExecutable() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Create Schedule for Specific Executable",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            projectDetailPage.getExecutableTabByProcessName(
                    "Create Schedule for Specific Executable").click();
            waitForElementVisible(projectDetailPage.getExecutableScheduleLink("DWHS2.grf")).click();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.createNewSchedule(null, null, null, null, null, true);
            waitForElementPresent(scheduleDetail.getRoot());
            scheduleDetail.clickOnCloseScheduleButton();
            waitForElementVisible(schedulesTable.getRoot());
            assertNewSchedule("Create Schedule for Specific Executable", "DWHS2.grf",
                    "/graph/DWHS2.grf", defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleFromSchedulesList() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Create Schedule from Schedule List",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            projectDetailPage.getNewScheduleLinkInSchedulesList(
                    "Create Schedule from Schedule List").click();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.createNewSchedule(null, null, null, null, null, true);
            waitForElementPresent(scheduleDetail.getRoot());
            scheduleDetail.clickOnCloseScheduleButton();
            waitForElementNotPresent(scheduleDetail.getRoot());
            assertNewSchedule("Create Schedule from Schedule List", "DWHS1.grf",
                    "/graph/DWHS1.grf", defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithEveryWeekCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Edit Cron Time of Schedule",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYWEEK.getCronTime(), Arrays.asList("00",
                            "00", ScheduleCronTimes.CRON_EVERYWEEK.getDaysInWeek().get(1)));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Edit Cron Time of Schedule", null, null, cronTime, null);
            assertNewSchedule("Edit Cron Time of Schedule", "DWHS1.grf", "/graph/DWHS1.grf",
                    cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithEveryDayCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Schedule every day",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(),
                            Arrays.asList("30", "10"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(), "Schedule every day",
                    null, null, cronTime, null);
            assertNewSchedule("Schedule every day", "DWHS1.grf", "/graph/DWHS1.grf", cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithCronExpression() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Schedule with cron expression",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EXPRESSION.getCronTime(),
                            Arrays.asList("*/20 * * * *"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Schedule with cron expression", null, null, cronTime, null);
            assertNewSchedule("Schedule with cron expression", "DWHS1.grf", "/graph/DWHS1.grf",
                    cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkManualExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Manual Execution", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Manual Execution", null, "/graph/successfulGraph.grf", cronTime, null);
            assertNewSchedule("Check Manual Execution", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            scheduleDetail.manualRun();
            scheduleDetail.assertLastExecutionDetails(true, true, false,
                    "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkStopManualExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Stop Manual Execution", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Stop Manual Execution", null, "/graph/longTimeRunningGraph.grf",
                    cronTime, null);
            assertNewSchedule("Check Stop Manual Execution", "longTimeRunningGraph.grf",
                    "/graph/longTimeRunningGraph.grf", cronTime, null);
            scheduleDetail.manualRun();
            Thread.sleep(5000);
            scheduleDetail.manualStop();
            scheduleDetail.assertLastExecutionDetails(false, true, true,
                    "Basic/graph/longTimeRunningGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void changeExecutableOfSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Change Executable of Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Change Executable of Schedule", null, "/graph/successfulGraph.grf", cronTime,
                    null);
            assertNewSchedule("Change Executable of Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            scheduleDetail.changeExecutable("/graph/errorGraph.grf", true);
            assertNewSchedule("Change Executable of Schedule", "errorGraph.grf",
                    "/graph/errorGraph.grf", cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void deleteSchedule() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, "Delete Schedule",
                Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                true);
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
        createScheduleForProcess(projectTitle, testParams.getProjectId(), "Delete Schedule", null,
                "/graph/successfulGraph.grf", cronTime, null);
        assertNewSchedule("Delete Schedule", "successfulGraph.grf", "/graph/successfulGraph.grf",
                cronTime, null);
        scheduleDetail.deleteSchedule(true);
        waitForElementVisible(projectDetailPage.getRoot());
        waitForElementVisible(projectDetailPage.getScheduleTabByProcessName("Delete Schedule"))
                .click();
        waitForElementVisible(projectDetailPage.checkEmptySchedulesList("Delete Schedule"));
        Assert.assertTrue(projectDetailPage.checkEmptySchedulesList("Delete Schedule")
                .isDisplayed());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void changeScheduleCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Change Cron Time of Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Change Cron Time of Schedule", null, "/graph/successfulGraph.grf", cronTime,
                    null);
            assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            Pair<String, List<String>> newCronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            scheduleDetail.changeCronTime(newCronTime, true);
            assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", newCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void editScheduleParameters() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Edit schedule parameters",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param 1", Arrays.asList("", "value 1"));
            parameters.put("param 2", Arrays.asList("", "value 2"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Edit schedule parameters", null, "/graph/DWHS2.grf", cronTime, parameters);
            assertNewSchedule("Edit schedule parameters", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
            Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
            changedParameters.put("param 1 new name", Arrays.asList("", "value 1 new"));
            changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
            changedParameters.put("secure param new name",
                    Arrays.asList("secure", "secure value new"));
            scheduleDetail.editScheduleParameters(changedParameters, false, true);
            assertNewSchedule("Edit schedule parameters", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, changedParameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void addNewParametersForSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Add New Parameters for Schedule",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param 1", Arrays.asList("", "value 1"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Add New Parameters for Schedule", null, "/graph/DWHS2.grf", cronTime,
                    parameters);
            assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
            Map<String, List<String>> newParameters = new LinkedHashMap<String, List<String>>();
            newParameters.put("param 2", Arrays.asList("", "value 2"));
            newParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
            Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
            changedParameters.put("param 1", Arrays.asList("", "value 1"));
            changedParameters.put("param 2", Arrays.asList("", "value 2"));
            changedParameters.put("secure param", Arrays.asList("secure", "secure value"));
            changedParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
            scheduleDetail.editScheduleParameters(newParameters, true, true);
            assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, changedParameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithIncorrectCron() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                DISCProcessTypes.GRAPH, "Create Schedule With Error Cron",
                Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
        Pair<String, List<String>> incorrectCronTime =
                Pair.of(ScheduleCronTimes.CRON_EXPRESSION.getCronTime(), Arrays.asList("* * *"));
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.checkScheduleWithIncorrectCron(incorrectCronTime,
                scheduleForm.getConfirmScheduleButton());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void editScheduleWithIncorrectCron() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Edit Schedule With Error Cron",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Edit Schedule With Error Cron", null, "/graph/DWHS2.grf", cronTime, null);
            assertNewSchedule("Edit Schedule With Error Cron", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, null);
            Pair<String, List<String>> incorrectCronTime =
                    Pair.of("cron expression", Arrays.asList("* * *"));
            scheduleDetail.checkScheduleWithIncorrectCron(incorrectCronTime,
                    scheduleDetail.getSaveChangedCronTimeButton());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkBrokenSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Check Broken Schedule",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Broken Schedule", null, null, null, null);
            assertNewSchedule("Check Broken Schedule", "DWHS1.grf", "/graph/DWHS1.grf",
                    defaultCronTime, null);
            redeployProcess(projectTitle, testParams.getProjectId(), "Check Broken Schedule",
                    "Basic", "Redeployed Process", DISCProcessTypes.GRAPH, Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            projectDetailPage.checkBrokenScheduleSection("Redeployed Process");
            assertBrokenSchedule("DWHS1.grf", "/graph/DWHS1.grf", defaultCronTime);
            brokenSchedulesTable.getScheduleTitle("DWHS1.grf").click();
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkBrokenSchedule("DWHS1.grf", "/graph/errorGraph.grf");
            assertNewSchedule("Redeployed Process", "errorGraph.grf", "/graph/errorGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkDeleteScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Delete Schedule Parameter",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param 1", Arrays.asList("", "value 1"));
            parameters.put("param 2", Arrays.asList("", "value 2"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Delete Schedule Parameter", null, "/graph/DWHS2.grf", cronTime, parameters);
            assertNewSchedule("Delete Schedule Parameter", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
            Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
            changedParameters.put("param 1 new name", null);
            changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
            changedParameters.put("secure param new name",
                    Arrays.asList("secure", "secure value new"));
            scheduleDetail.editScheduleParameters(changedParameters, false, true);
            changedParameters.remove("param 1 new name");
            assertNewSchedule("Delete Schedule Parameter", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, changedParameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelDeleteScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Cancel Delete Schedule Parameter",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param 1", Arrays.asList("", "value 1"));
            parameters.put("param 2", Arrays.asList("", "value 2"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Cancel Delete Schedule Parameter", "DWHS2.grf", "/graph/DWHS2.grf", cronTime,
                    parameters);
            assertNewSchedule("Cancel Delete Schedule Parameter", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
            Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
            changedParameters.put("param 1", null);
            changedParameters.put("param 2", Arrays.asList("", "value 2"));
            changedParameters.put("secure param", Arrays.asList("secure", "secure value"));
            scheduleDetail.editScheduleParameters(changedParameters, false, false);
            assertNewSchedule("Cancel Delete Schedule Parameter", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkIncorrectRetryDelay() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Incorrect Retry Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Incorrect Retry Schedule", null, "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule("Check Incorrect Retry Schedule", "errorGraph.grf",
                    "/graph/errorGraph.grf", cronTime, null);
            scheduleDetail.addRetryDelay("5", true, false);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelCreateSchedule() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                DISCProcessTypes.GRAPH, "Cancel Create Schedule from Schedule List",
                Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.createNewSchedule(null, null, null, null, null, false);
        waitForElementNotPresent(scheduleForm.getRoot());
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.assertProcessInList("Cancel Create Schedule from Schedule List",
                DISCProcessTypes.GRAPH, Arrays.asList("DWHS1.grf", "DWHS2.grf"));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelChangeScheduleExecutable() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Cancel Change Executable", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Cancel Change Executable", null, "/graph/successfulGraph.grf", cronTime, null);
            assertNewSchedule("Cancel Change Executable", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            scheduleDetail.changeExecutable("/graph/errorGraph.grf", false);
            assertNewSchedule("Cancel Change Executable", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelChangeScheduleCronTime() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Cancel Change Cron Time of Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Cancel Change Cron Time of Schedule", null, "/graph/successfulGraph.grf",
                    cronTime, null);
            assertNewSchedule("Cancel Change Cron Time of Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            Pair<String, List<String>> newCronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            scheduleDetail.changeCronTime(newCronTime, false);
            assertNewSchedule("Cancel Change Cron Time of Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelAddRetryDelay() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Retry Schedule", Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Retry Schedule", null, "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule("Check Retry Schedule", "errorGraph.grf", "/graph/errorGraph.grf",
                    cronTime, null);
            scheduleDetail.addRetryDelay("15", false, true);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelEditScheduleParams() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
                    DISCProcessTypes.GRAPH, "Cancel Edit schedule parameters",
                    Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
            Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
            parameters.put("param 1", Arrays.asList("", "value 1"));
            parameters.put("param 2", Arrays.asList("", "value 2"));
            parameters.put("secure param", Arrays.asList("secure", "secure value"));
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Cancel Edit schedule parameters", null, "/graph/DWHS2.grf", cronTime,
                    parameters);
            assertNewSchedule("Cancel Edit schedule parameters", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
            Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
            changedParameters.put("param 1 new name", Arrays.asList("", "value 1 new"));
            changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
            changedParameters.put("secure param new name",
                    Arrays.asList("secure", "secure value new"));
            scheduleDetail.editScheduleParameters(changedParameters, false, false);
            assertNewSchedule("Cancel Edit schedule parameters", "DWHS2.grf", "/graph/DWHS2.grf",
                    cronTime, parameters);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCancelDeleteSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Cancel Delete Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Cancel Delete Schedule", null, "/graph/successfulGraph.grf", cronTime, null);
            assertNewSchedule("Cancel Delete Schedule", "successfulGraph.grf",
                    "/graph/successfulGraph.grf", cronTime, null);
            scheduleDetail.deleteSchedule(false);
            waitForElementVisible(scheduleDetail.getRoot());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkRemoveRetry() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Retry Schedule", Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Retry Schedule", null, "/graph/errorGraph.grf", defaultCronTime, null);
            assertNewSchedule("Check Retry Schedule", "errorGraph.grf", "/graph/errorGraph.grf",
                    defaultCronTime, null);
            scheduleDetail.addRetryDelay("15", true, true);
            scheduleDetail.removeRetryDelay(false);
            scheduleDetail.removeRetryDelay(true);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkExecutionHistoryEmptyState() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Execution History Empty State", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Execution History Empty State", null, "/graph/errorGraph.grf",
                    defaultCronTime, null);
            assertNewSchedule("Check Execution History Empty State", "errorGraph.grf",
                    "/graph/errorGraph.grf", defaultCronTime, null);
            assertNotNull(scheduleDetail.getExecutionHistoryEmptyState());
            assertEquals(EXECUTION_HISTORY_EMPTY_STATE_MESSAGE, scheduleDetail
                    .getExecutionHistoryEmptyState().getText());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkScheduleExecutionState() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Check Schedule Execution State";
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                    "/graph/errorGraph.grf", defaultCronTime, null);
            assertNewSchedule(processName, "errorGraph.grf", "/graph/errorGraph.grf",
                    defaultCronTime, null);
            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInScheduledState());
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertLastExecutionDetails(false, true, false,
                    "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                    "/graph/successfulGraph.grf", defaultCronTime, null);
            assertNewSchedule(processName, "successfulGraph.grf", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
            scheduleDetail.manualRun();
            assertTrue(scheduleDetail.isInScheduledState());
            assertTrue(scheduleDetail.isInRunningState());
            scheduleDetail.assertLastExecutionDetails(true, true, false,
                    "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkSuccessfulExecutionGroup() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Check Schedule Execution State";
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                    "/graph/successfulGraph.grf", defaultCronTime, null);
            assertNewSchedule(processName, "successfulGraph.grf", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
            scheduleDetail.repeatManualRun(3, "/graph/successfulGraph.grf", DISCProcessTypes.GRAPH,
                    true);
            scheduleDetail.checkOkExecutionGroup(3, 0);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkProjectWithOneSchedule() throws InterruptedException, JSONException {
        cleanProcessesInProjectDetail(testParams.getProjectId());

        try {
            String processName = "Check Schedule Trigger With 1 Schedule In Project";
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.AFTER.getCronTime(), null);
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkScheduleTriggerOptions(cronTime, true, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Create Schedule With Custom Name";
            String scheduleName = "Custom Schedule Name";
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void editScheduleWithCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Edit Schedule With Custom Name";
            String scheduleName = null;
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);

            String newScheduleName = "Custom Schedule Name";
            scheduleDetail.changeScheduleTitle(newScheduleName, true, true);
            scheduleDetail.clickOnCloseScheduleButton();
            assertNewSchedule(processName, newScheduleName, "/graph/" + executableName,
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithEmptyCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Create Schedule With Empty Custom Name";
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.createScheduleWithInvalidScheduleName(processName,
                    "/graph/successfulGraph.grf", "", "Custom Schedule Name");
            assertNewSchedule(processName, "Custom Schedule Name", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void editScheduleWithEmptyCustomName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Edit Schedule With Empty Custom Name";
            String scheduleName = null;
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);

            scheduleDetail.changeScheduleTitle("", true, false);
            scheduleDetail.clickOnCloseScheduleButton();
            assertNewSchedule(processName, "successfulGraph.grf", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleNotUniqueName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Create Schedule With Not Unique Name";
            String scheduleName = null;
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);

            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.createScheduleWithInvalidScheduleName(processName,
                    "/graph/successfulGraph.grf", "successfulGraph.grf", "Custom Schedule Name");
            assertNewSchedule(processName, "Custom Schedule Name", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void editScheduleWithNotUniqueName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Create Schedule With Custom Name";
            String scheduleName = null;
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);

            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
                    "Custom Schedule Name", "/graph/successfulGraph.grf", defaultCronTime, null);
            assertNewSchedule(processName, "Custom Schedule Name", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
            scheduleDetail.changeScheduleTitle("successfulGraph.grf", true, false);
            scheduleDetail.clickOnCloseScheduleButton();
            assertNewSchedule(processName, "Custom Schedule Name", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void cancelEditScheduleName() throws InterruptedException, JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Edit Schedule With Custom Name";
            String scheduleName = null;
            String executableName = "successfulGraph.grf";
            prepareScheduleWithCustomName(processName, scheduleName, executableName);

            scheduleDetail.changeScheduleTitle("Custom Schedule Name", false, true);
            scheduleDetail.clickOnCloseScheduleButton();
            assertNewSchedule(processName, "successfulGraph.grf", "/graph/successfulGraph.grf",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithCustomNameForRubyScript() throws InterruptedException,
            JSONException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            String processName = "Create Schedule With Custom Name For Ruby Script";
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "ruby",
                    DISCProcessTypes.RUBY, processName, Arrays.asList("ruby1.rb", "ruby2.rb"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
                    "Custom Schedule Name", "/script/ruby1.rb", defaultCronTime, null);
            assertNewSchedule(processName, "Custom Schedule Name", "/script/ruby1.rb",
                    defaultCronTime, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCustomScheduleNameInFailedOverview() throws InterruptedException,
            JSONException {
        String graphName = "errorGraph.grf";
        checkScheduleNameInOverviewPage(DISCOverviewProjectStates.FAILED, projectTitle,
                testParams.getProjectId(), graphName);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCustomScheduleNameInSuccessfulOverview() throws InterruptedException,
            JSONException {
        String graphName = "successfulGraph.grf";
        checkScheduleNameInOverviewPage(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
                testParams.getProjectId(), graphName);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkCustomScheduleNameInRunningOverview() throws InterruptedException,
            JSONException {
        String graphName = "longTimeRunningGraph.grf";
        checkScheduleNameInOverviewPage(DISCOverviewProjectStates.RUNNING, projectTitle,
                testParams.getProjectId(), graphName);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkScheduleTriggerBySuccessfulSchedule() throws InterruptedException,
            JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "successfulGraph.grf";
            String triggerScheduleExecutable = "/graph/successfulGraph.grf";
            String dependentScheduleExecutable = "/graph/errorGraph.grf";
            String dependentScheduleName = "errorGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName, triggerScheduleName,
                            triggerScheduleExecutable, dependentScheduleName,
                            dependentScheduleExecutable);

            manualRunTriggerSchedule(scheduleUrls.get("triggerScheduleUrl"), true,
                    "Basic/graph/successfulGraph.grf");
            waitForAutoRunDependentSchedule(scheduleUrls.get("dependentScheduleUrl"),
                    "Basic/graph/errorGraph.grf", 1, true, false, 1);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkScheduleTriggerByFailedSchedule() throws InterruptedException, JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "errorGraph.grf";
            String triggerScheduleExecutable = "/graph/errorGraph.grf";
            String dependentScheduleExecutable = "/graph/successfulGraph.grf";
            String dependentScheduleName = "successfulGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName, triggerScheduleName,
                            triggerScheduleExecutable, dependentScheduleName,
                            dependentScheduleExecutable);

            manualRunTriggerSchedule(scheduleUrls.get("triggerScheduleUrl"), false,
                    "Basic/graph/errorGraph.grf");
            waitForAutoRunDependentSchedule(scheduleUrls.get("dependentScheduleUrl"),
                    "Basic/graph/successfulGraph.grf", 1, false, false, 0);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkScheduleTriggerInLoop() throws InterruptedException, JSONException {
        cleanProcessesInProjectDetail(testParams.getProjectId());

        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "successfulGraph.grf";
            String triggerScheduleExecutable = "/graph/successfulGraph.grf";
            String dependentScheduleExecutable = "/graph/errorGraph.grf";
            String dependentScheduleName = "errorGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName, triggerScheduleName,
                            triggerScheduleExecutable, dependentScheduleName,
                            dependentScheduleExecutable);

            Pair<String, List<String>> dependentScheduleCronTime =
                    Pair.of(ScheduleCronTimes.AFTER.getCronTime(),
                            Arrays.asList(processName, triggerScheduleExecutable));
            browser.get(scheduleUrls.get("triggerScheduleUrl"));
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkScheduleTriggerOptions(dependentScheduleCronTime, true, null);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkScheduleTriggerByItself() throws InterruptedException, JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "successfulGraph.grf";
            String triggerScheduleExecutable = "/graph/successfulGraph.grf";
            Pair<String, List<String>> triggerScheduleCronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                    triggerScheduleExecutable, triggerScheduleCronTime, null);
            assertNewSchedule(processName, triggerScheduleName, triggerScheduleExecutable,
                    triggerScheduleCronTime, null);

            Pair<String, List<String>> dependentSchduleCronTime =
                    Pair.of(ScheduleCronTimes.AFTER.getCronTime(),
                            Arrays.asList(processName, triggerScheduleExecutable));
            Map<String, String> expectedScheduleTriggerOptions = new HashMap<String, String>();
            expectedScheduleTriggerOptions.put(processName, triggerScheduleExecutable);
            projectDetailPage.clickOnNewScheduleButton();
            waitForElementVisible(scheduleForm.getRoot());
            scheduleForm.checkScheduleTriggerOptions(dependentSchduleCronTime, false,
                    expectedScheduleTriggerOptions);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkDisableDependentSchedule() throws InterruptedException, JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "successfulGraph.grf";
            String triggerScheduleExecutable = "/graph/successfulGraph.grf";
            String dependentScheduleExecutable = "/graph/errorGraph.grf";
            String dependentScheduleName = "errorGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName, triggerScheduleName,
                            triggerScheduleExecutable, dependentScheduleName,
                            dependentScheduleExecutable);

            manualRunTriggerSchedule(scheduleUrls.get("triggerScheduleUrl"), true,
                    "Basic/graph/successfulGraph.grf");
            waitForAutoRunDependentSchedule(scheduleUrls.get("dependentScheduleUrl"),
                    "Basic/graph/errorGraph.grf", 1, true, false, 1);
            scheduleDetail.disableSchedule();

            manualRunTriggerSchedule(scheduleUrls.get("triggerScheduleUrl"), true,
                    "Basic/graph/successfulGraph.grf");
            waitForAutoRunDependentSchedule(scheduleUrls.get("dependentScheduleUrl"),
                    "Basic/graph/errorGraph.grf", 1, false, false, 1);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkMissingScheduleTrigger() throws InterruptedException, JSONException {
        try {
            String processName = "Check Schedule With Trigger Schedule";
            String triggerScheduleName = "successfulGraph.grf";
            String triggerScheduleExecutable = "/graph/successfulGraph.grf";
            String dependentScheduleExecutable = "/graph/errorGraph.grf";
            String dependentScheduleName = "errorGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName, triggerScheduleName,
                            triggerScheduleExecutable, dependentScheduleName,
                            dependentScheduleExecutable);

            browser.get(scheduleUrls.get("triggerScheduleUrl"));
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.deleteSchedule(true);

            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.selectScheduleTab(processName);
            assertEquals(schedulesTable.getScheduleCron(dependentScheduleName).getText(),
                    TRIGGER_SCHEDULE_MISSING);
            browser.get(scheduleUrls.get("dependentScheduleUrl"));
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.checkTriggerScheduleMissing();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule-trigger"})
    public void checkMultipleScheduleTriggers() throws JSONException, InterruptedException {
        try {
            String processName1 = "Check Schedule With Trigger Schedule 1";
            String triggerScheduleName1 = "successfulGraph.grf";
            String triggerScheduleExecutable1 = "/graph/successfulGraph.grf";
            String dependentScheduleExecutable1 = "/graph/longTimeRunningGraph.grf";
            String dependentScheduleName1 = "longTimeRunningGraph.grf";
            Map<String, String> scheduleUrls =
                    prepareDataForTriggerScheduleTest(processName1, triggerScheduleName1,
                            triggerScheduleExecutable1, dependentScheduleName1,
                            dependentScheduleExecutable1);

            String processName2 = "Check Schedule With Trigger Schedule 2";
            String triggerScheduleExecutable2 = dependentScheduleExecutable1;
            String dependentScheduleExecutable2 = "/graph/errorGraph.grf";
            String dependentScheduleName2 = "errorGraph.grf";
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, processName2, Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            Pair<String, List<String>> dependentScheduleCronTime =
                    Pair.of(ScheduleCronTimes.AFTER.getCronTime(),
                            Arrays.asList(processName1, triggerScheduleExecutable2));
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName2,
                    dependentScheduleName2, dependentScheduleExecutable2,
                    dependentScheduleCronTime, null);
            assertNewSchedule(processName2, dependentScheduleName2, dependentScheduleExecutable2,
                    dependentScheduleCronTime, null);
            String dependentScheduleUrl2 = browser.getCurrentUrl();

            manualRunTriggerSchedule(scheduleUrls.get("triggerScheduleUrl"), true,
                    "Basic/graph/successfulGraph.grf");
            waitForAutoRunDependentSchedule(scheduleUrls.get("dependentScheduleUrl"),
                    "Basic/graph/successfulGraph.grf", 1, true, true, 1);
            waitForAutoRunDependentSchedule(dependentScheduleUrl2, "Basic/graph/errorGraph.grf", 1,
                    true, false, 1);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"schedule", "schedule-trigger"}, groups = {"tests"})
    public void test() throws JSONException {
        successfulTest = true;
    }

    private void checkScheduleNameInOverviewPage(DISCOverviewProjectStates overviewState,
            String projectName, String projectId, String graphName) throws JSONException,
            InterruptedException {
        try {
            String processName = "Check Custom Schedule Name In Overview Page";
            String scheduleName = "Custom Schedule Name";
            openProjectDetailPage(projectName, projectId);
            deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
                    processName, Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
                            "successfulGraph.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(),
                            Arrays.asList("59", "23"));
            createScheduleForProcess(projectName, projectId, processName, scheduleName, "/graph/"
                    + graphName, cronTime, null);
            assertNewSchedule(processName, scheduleName, "/graph/" + graphName, cronTime, null);
            scheduleDetail.manualRun();

            if (overviewState.equals(DISCOverviewProjectStates.RUNNING))
                assertTrue(scheduleDetail.isInRunningState());
            else {
                boolean isSuccessful = !overviewState.equals(DISCOverviewProjectStates.FAILED);
                scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
                        + graphName, DISCProcessTypes.GRAPH, 5);
            }
            String scheduleUrl = browser.getCurrentUrl();

            openUrl(DISC_OVERVIEW_PAGE);
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(overviewState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewScheduleName(overviewState, projectName, projectId,
                    true, scheduleUrl, scheduleName);
        } finally {
            cleanProcessesInProjectDetail(projectId);
        }
    }

    private void prepareScheduleWithCustomName(String processName, String scheduleName,
            String executableName) throws JSONException, InterruptedException {
        scheduleName = scheduleName == null ? executableName : scheduleName;
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, processName,
                Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                true);
        createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
                scheduleName, "/graph/" + executableName, defaultCronTime, null);
        assertNewSchedule(processName, scheduleName, "/graph/" + executableName, defaultCronTime,
                null);
    }

    private Map<String, String> prepareDataForTriggerScheduleTest(String processName,
            String triggerScheduleName, String triggerScheduleExecutable,
            String dependentSheduleName, String dependentScheduleExecutable) throws JSONException,
            InterruptedException {
        Map<String, String> scheduleUrls = new HashMap<String, String>();
        Pair<String, List<String>> triggerScheduleCronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, processName,
                Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                true);
        createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
                triggerScheduleName, triggerScheduleExecutable, triggerScheduleCronTime, null);
        assertNewSchedule(processName, triggerScheduleName, triggerScheduleExecutable,
                triggerScheduleCronTime, null);
        scheduleUrls.put("triggerScheduleUrl", browser.getCurrentUrl());

        Pair<String, List<String>> dependentScheduleCronTime =
                Pair.of(ScheduleCronTimes.AFTER.getCronTime(),
                        Arrays.asList(processName, triggerScheduleExecutable));
        createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
                dependentSheduleName, dependentScheduleExecutable, dependentScheduleCronTime, null);
        assertNewSchedule(processName, dependentSheduleName, dependentScheduleExecutable,
                dependentScheduleCronTime, null);
        scheduleUrls.put("dependentScheduleUrl", browser.getCurrentUrl());
        return scheduleUrls;
    }

    private void manualRunTriggerSchedule(String scheduleUrl, boolean isSuccessful,
            String executablePath) throws InterruptedException {
        browser.get(scheduleUrl);
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, executablePath,
                DISCProcessTypes.GRAPH, 5);
    }

    private void waitForAutoRunDependentSchedule(String scheduleUrl, String executablePath,
            int waitingTimeInMinutes, boolean isAutoRun, boolean isSuccessful,
            int expectedExecutionNumber) throws InterruptedException {
        browser.get(scheduleUrl);
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.waitForAutoRunSchedule(waitingTimeInMinutes);
        if (isAutoRun)
            scheduleDetail.assertLastExecutionDetails(isSuccessful, false, false, executablePath,
                    DISCProcessTypes.GRAPH, 5);
        assertEquals(scheduleDetail.getExecutionItemsNumber(), expectedExecutionNumber);
    }
}
