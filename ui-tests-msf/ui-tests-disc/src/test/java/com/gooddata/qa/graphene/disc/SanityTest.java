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
import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage.OverviewState;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.AbstractProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectsPage.FilterOption;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class SanityTest extends AbstractProcessTest {

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectsPage() {
        String processName = generateProcessName();

        initDiscProjectsPage()
                .markProjectCheckbox(projectTitle)
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());
        assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectDetailPage() {
        String processName = generateProcessName();

        initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithDifferentPackage() {
        String processName = generateProcessName();

        initDiscProjectDetailPage()
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
            initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectExecutable(Executable.SUCCESSFUL_GRAPH)
                    .schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(process.getName())
                    .hasSchedule(Executable.SUCCESSFUL_GRAPH.getName()),
                    "Schedule is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void executeSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.executeSchedule().waitForExecutionFinish();

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
            Schedule schedule = createSchedule(goodData, process, Executable.RUBY_SCRIPT_3,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.executeSchedule().waitForExecutionFinish();

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
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    parseTimeToCronExpression(autoExecutionStartTime));

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.waitForAutoExecute(autoExecutionStartTime).waitForExecutionFinish();

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
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

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
            assertEquals(notifyItem.getSelectedEvent(), notificationRule.getEvent());
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());
            assertEquals(notifyItem.getMessage(), notificationRule.getMessage());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "specificStatesProvider")
    public Object[][] getSpecificStatesProvider() {
        return new Object[][] {
            {OverviewState.FAILED, Executable.ERROR_GRAPH},
            {OverviewState.SUCCESSFUL, Executable.SUCCESSFUL_GRAPH},
            {OverviewState.RUNNING, Executable.LONG_TIME_RUNNING_GRAPH},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "specificStatesProvider")
    public void checkOverviewSpecificState(OverviewState state, Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetailFragment = initScheduleDetail(schedule).executeSchedule();
            if (state == OverviewState.RUNNING) {
                scheduleDetailFragment.waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetailFragment.waitForExecutionFinish();
            }

            initDiscOverviewPage().selectState(state);
            takeScreenshot(browser, "State-" + state + "-shows-correctly", getClass());
            assertEquals(overviewPage.getStateNumber(state), 1);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "filterProvider")
    public Object[][] getFilterProvider() {
        return new Object[][] {
            {FilterOption.SUCCESSFUL, Executable.SUCCESSFUL_GRAPH},
            {FilterOption.FAILED, Executable.ERROR_GRAPH},
            {FilterOption.RUNNING, Executable.LONG_TIME_RUNNING_GRAPH},
            {FilterOption.DISABLED, Executable.SUCCESSFUL_GRAPH}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "filterProvider")
    public void checkProjectFilterWorkCorrectly(FilterOption filterOption, Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            if (filterOption == FilterOption.DISABLED) {
                scheduleDetail.disableSchedule();
            } else if (filterOption == FilterOption.RUNNING) {
                scheduleDetail.executeSchedule().waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetail.executeSchedule().waitForExecutionFinish();
            }

            initDiscProjectsPage().selectFilterOption(filterOption);
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
        initDiscProjectsPage().searchProject(projectTitle);
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");

        initDiscProjectsPage().searchProject(testParams.getProjectId());
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");
    }
}
