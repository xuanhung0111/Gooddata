package com.gooddata.qa.graphene.disc.schedule;

import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.RestUtils.getResource;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.__AbstractDISCTest;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.schedule.CronEditor;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment.__ExecutionHistoryItem;

public class ScheduleDetailTest extends __AbstractDISCTest {

    private static final String BROKEN_SCHEDULE_MESSAGE = "The schedules cannot be executed. Its process has been"
            + " re-deployed with modified graphs or a different folder structure.";

    private static final String BROKEN_MESSAGE_IN_SCHEDULE_DETAIL = "The graph errorGraph.grf doesn't exist"
            + " because it has been changed (renamed or deleted). It isn't possible to execute this schedule"
            + " because there is no graph to execute.";

    private static final String TRIGGERING_SCHEDULE_ERROR_MESSAGE = "The schedule that triggers this schedule"
            + " is missing. To run this schedule, set a new trigger or select a cron frequency.";

    private static final String SCHEDULE_IN_LOOP_MESSAGE = "Schedules cannot be scheduled in a loop";

    @DataProvider(name = "rubyGitStoreProvider")
    public Object[][] rubyGitStoreProvider() {
        return new Object[][] {
            {"${PUBLIC_APPSTORE}:branch/prodigy-testing:/vietnam/default/ReadFile"},
            {"${PRIVATE_APPSTORE}:branch/prodigy-testing:/vietnam/default/ReadFile"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "rubyGitStoreProvider")
    public void executeScheduleWithRubyInGitStore(String gitStorePath) {
        String processName = generateProcessName();
        __initDiscProjectDetailPage().deployProcessWithGitStorePath(processName, gitStorePath);

        try {
            projectDetailPage.openCreateScheduleForm().schedule();

        __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser)
                .executeSchedule().waitForExecutionFinish();

        takeScreenshot(browser, "Execute-schedule-from-process-" + processName, getClass());
        assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                ScheduleStatus.OK.toString());

        } finally {
            __initDiscProjectDetailPage().deleteProcess(processName);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stopExecution() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.LONG_TIME_RUNNING_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule()
                    .stopExecution();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), "MANUALLY STOPPED");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkBrokenSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = __initDiscProjectDetailPage().getProcess(process.getName());
            processDetail.redeployWithZipFile(process.getName(), ProcessType.CLOUD_CONNECT, PackageFile.ONE_GRAPH.loadFile());

            takeScreenshot(browser, "Broken-schedule-message-shows", getClass());
            assertEquals(processDetail.getBrokenScheduleMessage(), BROKEN_SCHEDULE_MESSAGE);

            __ScheduleDetailFragment scheduleDetail = processDetail.openSchedule(schedule.getName());
            takeScreenshot(browser, "Broken-schedule-message-shows-in-schedule-detail", getClass());
            assertEquals(scheduleDetail.getBrokenScheduleMessage(), BROKEN_MESSAGE_IN_SCHEDULE_DETAIL);

            ((__ScheduleDetailFragment) scheduleDetail.replaceBrokenExecutableWith(__Executable.SUCCESSFUL_GRAPH))
                    .saveChanges().executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionHistoryEmptyState() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            assertEquals(initScheduleDetail(schedule).getExecutionHistoryEmptyMessage(),
                    "No history available. This schedule has not been run yet.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSuccessfulExecutionGroup() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            executeScheduleWithSpecificTimes(scheduleDetail, 3);

            __ExecutionHistoryItem item = scheduleDetail.getLastExecutionHistoryItem();
            assertTrue(item.isItemGroup(), "All sucessful executions are not grouped");
            assertEquals(item.getStatusDescription(), "OK 3×");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleCannotTriggerByItself() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            CronEditor cronEditor = __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .getCronEditor()
                    .selectRunTime(__ScheduleCronTime.AFTER);
            assertEquals(cronEditor.getEmptyTriggeringScheduleMessage(), SCHEDULE_IN_LOOP_MESSAGE);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleTriggerInLoop() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            createSchedule(process, __Executable.SUCCESSFUL_GRAPH, schedule);

            CronEditor cronEditor = initScheduleDetail(schedule)
                    .getCronEditor().selectRunTime(__ScheduleCronTime.AFTER);
            assertEquals(cronEditor.getEmptyTriggeringScheduleMessage(), SCHEDULE_IN_LOOP_MESSAGE);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkMissingScheduleTrigger() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule1 = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule2 = createSchedule(process, __Executable.ERROR_GRAPH, schedule1);

            initScheduleDetail(schedule1).deleteSchedule();

            ProcessDetail processDetail = projectDetailPage.getProcess(process.getName());
            assertEquals(processDetail.getScheduleCronTime(schedule2.getName()), "Trigger schedule missing!");

            __ScheduleDetailFragment scheduleDetail = processDetail.openSchedule(schedule2.getName());
            assertEquals(scheduleDetail.getTriggeringScheduleErrorMessage(), TRIGGERING_SCHEDULE_ERROR_MESSAGE);
            assertEquals(scheduleDetail.getCronEditor().getCronExpression(), "0 * * * *");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionLog() throws IOException, JSONException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish();
            assertNotNull(getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK));

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEffectiveUserOfSchedule() throws ParseException, JSONException, IOException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());
        String otherUser = createAndAddUserToProject(UserRoles.ADMIN);

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            assertEquals(initScheduleDetail(schedule).getEffectiveUser(), testParams.getUser());

            logout();
            signInAtGreyPages(otherUser, testParams.getPassword());

            assertEquals(initScheduleDetail(schedule).getEffectiveUser(), testParams.getUser());

            __ScheduleDetailFragment.getInstance(browser).close();
            __ScheduleDetailFragment scheduleDetail = projectDetailPage.getProcess(process.getName())
                    .redeployWithZipFile(process.getName(), ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile())
                    .openSchedule(schedule.getName());
            assertEquals(scheduleDetail.getEffectiveUser(), otherUser);

        } finally {
            deteleProcess(getGoodDataClient(), process);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionTooltipOnTimeLine() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule successfulSchedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule failedSchedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            String executionTimelineTooltip = initScheduleDetail(successfulSchedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionTimelineTooltip();
            takeScreenshot(browser, "Successful-execution-timeline-tooltip", getClass());
            assertThat(executionTimelineTooltip, containsString("Successful execution"));

            executionTimelineTooltip = initScheduleDetail(failedSchedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionTimelineTooltip();
            takeScreenshot(browser, "Failed-execution-timeline-tooltip", getClass());
            assertThat(executionTimelineTooltip, containsString("Failed execution"));

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void closeScheduleDetail() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = __initDiscProjectDetailPage().getProcess(process.getName());
            processDetail.openTab(Tab.SCHEDULE).openSchedule(schedule.getName());
            processDetail.openTab(Tab.EXECUTABLE);
            assertFalse(__ScheduleDetailFragment.isVisible(browser), "Schedule detail is not close");

            processDetail.openTab(Tab.SCHEDULE).openSchedule(schedule.getName()).close();
            assertFalse(__ScheduleDetailFragment.isVisible(browser), "Schedule detail is not close");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.clickDeleteScheduleButton().discard();
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(schedule.getName()),
                    "Schedule is deleted");

            scheduleDetail.deleteSchedule();
            assertFalse(projectDetailPage.getProcess(process.getName()).hasSchedule(schedule.getName()),
                    "Schedule is not deleted");

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
