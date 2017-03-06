package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalTime;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.common.__AbstractDISCTest;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import com.gooddata.qa.graphene.fragments.disc.overview.__DiscOverviewPage.OverviewState;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.projects.__ProjectsPage.FilterOption;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class __SanityTest extends __AbstractDISCTest {

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectsPage() {
        String processName = generateProcessName();

        __initDiscProjectsPage()
                .markProjectCheckbox(projectTitle)
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());
        assertTrue(__initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectDetailPage() {
        String processName = generateProcessName();

        __initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithDifferentPackage() {
        String processName = generateProcessName();

        __initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");

        ProcessDetail process = projectDetailPage.getProcess(processName);
        assertEquals(process.getTabTitle(Tab.EXECUTABLE), "4 graphs total");

        process.redeployWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.ONE_GRAPH.loadFile());
        assertTrue(process.getTabTitle(Tab.EXECUTABLE).matches("1 graphs? total"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectExecutable(__Executable.SUCCESSFUL_GRAPH))
                    .schedule();
            assertTrue(projectDetailPage.getProcess(process.getName())
                    .hasSchedule(__Executable.SUCCESSFUL_GRAPH.getName()),
                    "Schedule is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void executeSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule().waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkRubyExecution() {
        GoodData goodData = null;
        if (testParams.getDomainUser() != null) {
            goodData = getGoodDataClient(testParams.getDomainUser(), testParams.getPassword());
        } else {
            goodData = getGoodDataClient();
        }

        DataloadProcess process = createProcess(goodData, generateProcessName(), PackageFile.RUBY,
                ProcessType.RUBY_SCRIPTS);

        try {
            Schedule schedule = createSchedule(goodData, process, __Executable.RUBY_SCRIPT_3,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule().waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(goodData, process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void autoExecuteSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalTime autoExecutionStartTime = LocalTime.now().plusMinutes(2);
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    parseTimeToCronExpression(autoExecutionStartTime));

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNotificationRule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = __initDiscProjectDetailPage().getProcess(process.getName());

            final NotificationRule notificationRule = new NotificationRule()
                    .withEmail(testParams.getUser())
                    .withEvent(NotificationEvent.SUCCESS)
                    .withSubject("Subject " + generateHashString())
                    .withMessage("Message " + generateHashString());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            NotificationRuleItem notifyItem = processDetail.openNotificationRuleDialog()
                    .getLastNotificationRuleItem()
                    .expand();

            assertEquals(notifyItem.getEmail(), notificationRule.getEmail());
            assertEquals(notifyItem.getEvent(), notificationRule.getEvent());
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());
            assertEquals(notifyItem.getMessage(), notificationRule.getMessage());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "specificStatesProvider")
    public Object[][] getSpecificStatesProvider() {
        return new Object[][] {
            {OverviewState.FAILED, __Executable.ERROR_GRAPH},
            {OverviewState.SUCCESSFUL, __Executable.SUCCESSFUL_GRAPH},
            {OverviewState.RUNNING, __Executable.LONG_TIME_RUNNING_GRAPH},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "specificStatesProvider")
    public void checkOverviewSpecificState(OverviewState state, __Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetailFragment = initScheduleDetail(schedule).executeSchedule();
            if (state == OverviewState.RUNNING) {
                scheduleDetailFragment.waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetailFragment.waitForExecutionFinish();
            }

            __initDiscOverviewPage().selectState(state);
            takeScreenshot(browser, "State-" + state + "-shows-correctly", getClass());
            assertEquals(overviewPage.getStateNumber(state), 1);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "filterProvider")
    public Object[][] getFilterProvider() {
        return new Object[][] {
            {FilterOption.SUCCESSFUL, __Executable.SUCCESSFUL_GRAPH},
            {FilterOption.FAILED, __Executable.ERROR_GRAPH},
            {FilterOption.RUNNING, __Executable.LONG_TIME_RUNNING_GRAPH},
            {FilterOption.DISABLED, __Executable.SUCCESSFUL_GRAPH}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "filterProvider")
    public void checkProjectFilterWorkCorrectly(FilterOption filterOption, __Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);

            if (filterOption == FilterOption.DISABLED) {
                scheduleDetail.disableSchedule();
            } else if (filterOption == FilterOption.RUNNING) {
                scheduleDetail.executeSchedule().waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetail.executeSchedule().waitForExecutionFinish();
            }

            __initDiscProjectsPage().selectFilterOption(filterOption);
            takeScreenshot(browser, "Filter-" + filterOption + "-work-correctly", getClass());
            assertTrue(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");

            projectsPage.selectFilterOption(FilterOption.UNSCHEDULED);

            if (filterOption == FilterOption.DISABLED) {
                assertTrue(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");
            } else {
                assertFalse(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");
            }

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchProject() {
        __initDiscProjectsPage().searchProject(projectTitle);
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");

        __initDiscProjectsPage().searchProject(testParams.getProjectId());
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");
    }
}
