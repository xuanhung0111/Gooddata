package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.List;
import java.text.ParseException;

import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class OverviewPageTest extends AbstractDISC {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-overview-page";
        startPage = DISC_OVERVIEW_PAGE;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultOverviewState() {
        openOverviewPage();
        assertTrue(discOverview.isActive(OverviewProjectStates.FAILED));
        assertFalse(discOverview.isActive(OverviewProjectStates.RUNNING));
        assertFalse(discOverview.isActive(OverviewProjectStates.SCHEDULED));
        assertFalse(discOverview.isActive(OverviewProjectStates.SUCCESSFUL));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyFailedState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.FAILED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyRunningState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyScheduledState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySucessfulState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFailedStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledStateNumber() {
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Scheduled State Number");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForOverviewScheduledStateTests(getProjects(),
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCombinedStatesNumber() {
        try {
            String processName = "Check Combined States Number";
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            createAndAssertSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.FAILED_GRAPH)
                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                    .setMinuteInHour("59"));
            scheduleDetail.manualRun();
            scheduleDetail.assertFailedExecution(Executables.FAILED_GRAPH);
            scheduleDetail.clickOnCloseScheduleButton();

            createAndAssertSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.SUCCESSFUL_GRAPH)
                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                    .setMinuteInHour("59"));
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.FAILED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.FAILED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SUCCESSFUL,
                    discOverviewProjects.getOverviewProjectNumber());
            checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, getWorkingProject());
            checkFilteredOutOverviewProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewFailedProjects() throws ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

            prepareDataForCheckingOverviewState(OverviewProjectStates.FAILED, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.FAILED,
                    overviewProject);

            checkOtherOverviewStates(OverviewProjectStates.FAILED, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewSuccessfulProject() throws ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess()
                            .setProcessName("Check Overview Successful Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Successful Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

            prepareDataForCheckingOverviewState(OverviewProjectStates.SUCCESSFUL, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.SUCCESSFUL,
                    overviewProject);

            checkOtherOverviewStates(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkOverviewScheduledProject() throws ParseException, JSONException,
            InterruptedException {
        List<ProjectInfo> additionalProjects =
                Arrays.asList(new ProjectInfo().setProjectName("Disc-test-scheduled-state"));
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForOverviewScheduledStateTests(additionalProjects, overviewProject);

            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.SCHEDULED,
                    overviewProject);
            checkOtherOverviewStates(OverviewProjectStates.SCHEDULED, getWorkingProject());
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewRunningProject() throws ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Running Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Running Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

            prepareDataForCheckingOverviewState(OverviewProjectStates.RUNNING, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.RUNNING);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.RUNNING,
                    overviewProject);

            checkOtherOverviewStates(OverviewProjectStates.RUNNING, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedProjects() {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            bulkActionsProjectInOverviewPage(OverviewProjectStates.FAILED, overviewProject);

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            try {
                assertTrue(scheduleDetail.isStarted());
            } catch (NoSuchElementException ex) {
                assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
            }
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedSchedule() throws ParseException {
        try {
            bulkActionsScheduleInOverviewPage(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedProjects() {
        try {
            disableProjectInOverviewPage(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedSchedule() throws JSONException, InterruptedException, ParseException {
        try {
            disableScheduleInOverviewPage(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulProjects() {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName(
                            "Check Run Overview Successful Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Successful Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            bulkActionsProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL, overviewProject);

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            assertTrue(scheduleDetail.isStarted());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulSchedule() throws ParseException {
        try {
            bulkActionsScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulProjects() {
        try {
            disableProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulSchedule() throws ParseException {
        try {
            disableScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningProjects() {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName(
                            "Check Stop Overview Running Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Running Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            bulkActionsProjectInOverviewPage(OverviewProjectStates.RUNNING, overviewProject);

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningSchedule() throws ParseException {
        try {
            bulkActionsScheduleInOverviewPage(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningProjects() {
        try {
            disableProjectInOverviewPage(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningSchedule() throws ParseException {
        try {
            disableScheduleInOverviewPage(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void disableScheduledProjects() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                Arrays.asList(new ProjectInfo().setProjectName("Disc-test-scheduled-state"));
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForOverviewScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.disableAction();
            checkOtherOverviewStates(OverviewProjectStates.ALL, getWorkingProject());
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void stopScheduledProjects() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                Arrays.asList(new ProjectInfo().setProjectName("Disc-test-scheduled-state"));
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForOverviewScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.bulkAction(OverviewProjectStates.SCHEDULED);
            checkOtherOverviewStates(OverviewProjectStates.FAILED, getWorkingProject());

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }
}
