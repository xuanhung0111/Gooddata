package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;

public class LongTimeRunningSchedulesTests extends AbstractSchedulesTests {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-long-time-running-schedule";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkScheduleAutoRun() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Auto Run Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(),
                            Arrays.asList("${minute}"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Auto Run Schedule", "/graph/successfulGraph.grf", cronTime, null);
            assertNewSchedule("Check Auto Run Schedule", "successfulGraph.grf", cronTime, null);
            scheduleDetail.waitForAutoRunSchedule(2);
            scheduleDetail.assertLastExecutionDetails(true, false, false, null, null, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkErrorExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Error Execution of Schedule", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(),
                            Arrays.asList("${minute}"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Error Execution of Schedule", "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule("Check Error Execution of Schedule", "errorGraph.grf", cronTime, null);
            scheduleDetail.waitForAutoRunSchedule(2);
            scheduleDetail.assertLastExecutionDetails(false, false, false,
                    "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkRetryExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Retry Schedule", Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(),
                            Arrays.asList("${minute}"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Retry Schedule", "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule("Check Retry Schedule", "errorGraph.grf", cronTime, null);
            scheduleDetail.addRetryDelay("15", true, true);
            scheduleDetail.waitForAutoRunSchedule(2);
            scheduleDetail.assertLastExecutionDetails(false, false, false,
                    "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
            scheduleDetail.waitForAutoRunSchedule(15);
            scheduleDetail.assertLastExecutionDetails(false, false, false,
                    "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkStopAutoExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Stop Auto Execution", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(),
                            Arrays.asList("${minute}"));
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Stop Auto Execution", "/graph/longTimeRunningGraph.grf", cronTime, null);
            assertNewSchedule("Check Stop Auto Execution", "longTimeRunningGraph.grf", cronTime,
                    null);
            scheduleDetail.waitForAutoRunSchedule(2);
            scheduleDetail.manualStop();
            scheduleDetail.assertLastExecutionDetails(false, false, true,
                    "Basic/graph/longTimeRunningGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkLongTimeExecution() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Long Time Execution", Arrays.asList(
                            "errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                    true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(),
                    "Check Long Time Execution", "/graph/longTimeRunningGraph.grf", cronTime, null);
            assertNewSchedule("Check Long Time Execution", "longTimeRunningGraph.grf", cronTime,
                    null);
            scheduleDetail.manualRun();
            scheduleDetail.assertLastExecutionDetails(true, true, false,
                    "Basic/graph/longTimeRunningGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void disableSchedule() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Disable Schedule", Arrays.asList("errorGraph.grf",
                            "longTimeRunningGraph.grf", "successfulGraph.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), "Disable Schedule",
                    "/graph/successfulGraph.grf", cronTime, null);
            assertNewSchedule("Disable Schedule", "successfulGraph.grf", cronTime, null);
            scheduleDetail.disableSchedule();
            Assert.assertTrue(scheduleDetail.isDisabledSchedule(15, 0));
            scheduleDetail.manualRun();
            scheduleDetail.assertLastExecutionDetails(true, true, false,
                    "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
            scheduleDetail.enableSchedule();
            Assert.assertFalse(scheduleDetail.isDisabledSchedule(15, 1));
            scheduleDetail.assertLastExecutionDetails(true, false, false,
                    "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"long-time-schedule"})
    public void checkScheduleFailForManyTimes() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, "Check Failed Schedule",
                Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                true);
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
        createScheduleForProcess(projectTitle, testParams.getProjectId(), "Check Failed Schedule",
                null, cronTime, null);
        assertNewSchedule("Check Failed Schedule", "errorGraph.grf", cronTime, null);
        scheduleDetail.checkRepeatedFailureSchedule("Basic/graph/errorGraph.grf",
                DISCProcessTypes.GRAPH);
        Assert.assertTrue(scheduleDetail.isDisabledSchedule(15, 30));
    }

    @Test(dependsOnGroups = {"long-time-schedule"}, groups = {"tests"})
    public void test() throws JSONException {
        successfulTest = true;
    }
}
