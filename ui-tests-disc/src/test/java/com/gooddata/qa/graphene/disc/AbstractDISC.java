package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.google.common.base.Predicate;

public class AbstractDISC extends AbstractNotificationTest {

    protected void checkFilteredOutOverviewProject(OverviewProjectStates state,
            ProjectInfo projectInfo) {
        discOverview.selectOverviewState(state);
        waitForElementVisible(discOverviewProjects.getRoot());
        if (discOverview.getStateNumber(state).equals("0"))
            discOverviewProjects.assertOverviewEmptyState(state);
        else
            assertNull(discOverviewProjects.getOverviewProjectWithAdminRole(projectInfo));
    }

    protected void checkOtherOverviewStates(OverviewProjectStates state, ProjectInfo projectInfo) {
        List<OverviewProjectStates> projectStateToCheck =
                Arrays.asList(OverviewProjectStates.FAILED, OverviewProjectStates.RUNNING,
                        OverviewProjectStates.SCHEDULED, OverviewProjectStates.SUCCESSFUL);
        for (OverviewProjectStates projectState : projectStateToCheck) {
            if (projectState == state)
                continue;
            if (state != OverviewProjectStates.SCHEDULED) {
                checkFilteredOutOverviewProject(projectState, projectInfo);
                continue;
            }
            if (projectState != OverviewProjectStates.RUNNING)
                checkFilteredOutOverviewProject(projectState, projectInfo);
        }
    }

    protected void openOverviewPage() {
        openUrl(DISC_OVERVIEW_PAGE);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !discOverview.getStateNumber(OverviewProjectStates.FAILED).isEmpty();
            }
        });
        waitForElementVisible(discOverviewProjects.getRoot());
    }

    protected void prepareDataForOverviewScheduledStateTests(List<ProjectInfo> additionalProjects,
            OverviewProjectDetails overviewProject) {
        prepareDataForAdditionalProjects(additionalProjects);
        prepareDataForScheduledProject(overviewProject);
    }

    protected void checkOverviewStateNumber(OverviewProjectStates projectState) {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Project Number");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Schedule");
            overviewProcess.addSchedule(overviewSchedule);
            overviewProject.addProcess(overviewProcess);
            prepareDataForCheckingOverviewState(projectState, overviewProject);

            openOverviewPage();
            discOverview.selectOverviewState(projectState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverview.assertOverviewStateNumber(projectState,
                    discOverviewProjects.getOverviewProjectNumber());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }

    }

    protected OverviewProjectDetails prepareDataForCheckingOverviewState(
            OverviewProjectStates state, OverviewProjectDetails overviewProject) {
        Executables executable = null;
        switch (state) {
            case FAILED:
                executable = Executables.FAILED_GRAPH;
                break;
            case SUCCESSFUL:
                executable = Executables.SUCCESSFUL_GRAPH;
                break;
            case RUNNING:
                executable = Executables.LONG_TIME_RUNNING_GRAPH;
                break;
            default:
                executable = Executables.SUCCESSFUL_GRAPH;
        }

        openProjectDetailByUrl(overviewProject.getProjectInfo().getProjectId());
        for (OverviewProcess overviewProcess : overviewProject.getOverviewProcesses()) {
            String processUrl =
                    deployInProjectDetailPage(DeployPackages.BASIC,
                            overviewProcess.getProcessName());
            overviewProcess.setProcessUrl(processUrl);

            for (OverviewSchedule overviewSchedule : overviewProcess.getOverviewSchedules()) {
                createAndAssertSchedule(new ScheduleBuilder()
                        .setProcessName(overviewProcess.getProcessName()).setExecutable(executable)
                        .setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());

                scheduleDetail.manualRun();
                assertTrue(scheduleDetail.isInRunningState());
                overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());

                if (state == OverviewProjectStates.RUNNING) {
                    overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                    continue;
                }

                if (state == OverviewProjectStates.FAILED)
                    scheduleDetail.assertFailedExecution(executable);
                else if (state == OverviewProjectStates.SUCCESSFUL)
                    scheduleDetail.assertSuccessfulExecution();

                overviewSchedule.setExecutionDescription(scheduleDetail.getExecutionDescription());
                overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                overviewSchedule.setLastExecutionRunTime(scheduleDetail.getExecutionRuntime());
            }
        }
        return overviewProject;
    }

    protected void checkOverviewProjectWithoutAdminRole(OverviewProjectStates projectState)
            throws ParseException, IOException, JSONException, ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName(
                            "Check Overview Project With Non-Admin Role");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Schedule");
            overviewProcess.addSchedule(overviewSchedule);
            overviewProject.addProcess(overviewProcess);
            prepareDataForCheckingOverviewState(projectState, overviewProject);

            addUsersWithOtherRolesToProject();
            openUrl(PAGE_PROJECTS);
            logout();

            signIn(false, UserRoles.VIEWER);
            openUrl(DISC_OVERVIEW_PAGE);
            discOverview.selectOverviewState(projectState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.checkProjectNotAdmin(projectState, overviewProject);
            openUrl(PAGE_PROJECTS);
            logout();

            signIn(false, UserRoles.EDITOR);
            openUrl(DISC_OVERVIEW_PAGE);
            discOverview.selectOverviewState(projectState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.checkProjectNotAdmin(projectState, overviewProject);
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.ADMIN);
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    protected void disableProjectInOverviewPage(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.disableAction();
        checkFilteredOutOverviewProject(projectState, getWorkingProject());
        checkOtherOverviewStates(projectState, getWorkingProject());

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    protected OverviewProjectDetails bulkActionsProjectInOverviewPage(
            OverviewProjectStates projectState, OverviewProjectDetails overviewProject) {
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(projectState);
        checkFilteredOutOverviewProject(projectState, getWorkingProject());

        return overviewProject;
    }

    protected void disableScheduleInOverviewPage(OverviewProjectStates projectState)
            throws ParseException {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Schedule 1");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        OverviewProcess disabledProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Schedule 2");
        OverviewSchedule disabledSchedule =
                disabledProcess.newSchedule().setScheduleName("Disabled Schedule");
        disabledProcess.addSchedule(disabledSchedule);
        overviewProject.addProcess(overviewProcess).addProcess(disabledProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        openOverviewPage();
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnOverviewSchedules(new OverviewProjectDetails().setProjectInfo(
                getWorkingProject()).addProcess(disabledProcess));
        discOverviewProjects.disableAction();
        overviewProject.removeProcess(disabledProcess);
        discOverview.getStateNumber(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.assertOverviewProject(projectState, overviewProject);

        browser.get(disabledSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    protected void bulkActionsScheduleInOverviewPage(OverviewProjectStates projectState)
            throws ParseException {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess()
                        .setProcessName("Check Bulk Action Overview Schedule 1");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        OverviewProcess selectedProcess =
                overviewProject.newProcess()
                        .setProcessName("Check Bulk Action Overview Schedule 2");
        OverviewSchedule selectedSchedule =
                selectedProcess.newSchedule().setScheduleName("Selected Schedule");
        selectedProcess.addSchedule(selectedSchedule);
        overviewProject.addProcess(overviewProcess).addProcess(selectedProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        OverviewProjectDetails selectedProjectSchedule =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject()).addProcess(
                        selectedProcess);
        discOverviewProjects.checkOnOverviewSchedules(selectedProjectSchedule);
        discOverviewProjects.bulkAction(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.assertOverviewProject(projectState, new OverviewProjectDetails()
                .setProjectInfo(getWorkingProject()).addProcess(overviewProcess));

        browser.get(selectedSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        if (projectState != OverviewProjectStates.RUNNING) {
            try {
                assertTrue(scheduleDetail.isStarted());
                scheduleDetail.waitForExecutionFinish();
            } catch (NoSuchElementException ex) {
                assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
            }
        } else {
            scheduleDetail.assertManualStoppedExecution();
            selectedSchedule.setExecutionDescription(scheduleDetail.getExecutionDescription());
            selectedSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
            selectedSchedule.setLastExecutionRunTime(scheduleDetail.getExecutionRuntime());
        }
    }

    protected void cleanupProcessesAndProjects(boolean deleteProjects,
            List<ProjectInfo> additionalProjects) {
        cleanProcessesInProjectDetail(testParams.getProjectId());
        if (deleteProjects)
            deleteProjects(additionalProjects);
    }

    protected void prepareDataForProjectsPageTest(ProjectStateFilters projectFilter,
            ProjectInfo workingProject, String processName, Executables executable) {
        openProjectDetailByUrl(workingProject.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        if (projectFilter == ProjectStateFilters.UNSCHEDULED)
            return;

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(executable)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59");
        createAndAssertSchedule(scheduleBuilder);
        scheduleDetail.manualRun();
        if (projectFilter == ProjectStateFilters.SCHEDULED)
            return;

        assertTrue(scheduleDetail.isInRunningState());
        if (projectFilter == ProjectStateFilters.RUNNING)
            return;

        if (projectFilter == ProjectStateFilters.FAILED)
            scheduleDetail.assertFailedExecution(executable);
        else
            scheduleDetail.assertSuccessfulExecution();

        if (projectFilter == ProjectStateFilters.DISABLED)
            scheduleDetail.disableSchedule();
    }

    protected void checkSearchProjectInSpecificState(ProjectStateFilters projectFilter,
            ProjectInfo project, String processName, Executables executable) {
        try {
            prepareDataForProjectsPageTest(projectFilter, project, processName, executable);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.searchProjectInSpecificState(projectFilter, project);
        } finally {
            openProjectDetailByUrl(project.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }


    protected void prepareDataForAdditionalProjects(List<ProjectInfo> additionalProjects) {
        String additionalProcessName = "Process for additional projects";
        for (ProjectInfo project : additionalProjects) {
            openProjectDetailByUrl(project.getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, additionalProcessName);
            prepareAdditionalSchedulesForScheduledState(additionalProcessName);
        }
    }

    private void prepareAdditionalSchedulesForScheduledState(String additionalProcessName) {
        for (int i = 1; i < 7; i++) {
            createAndAssertSchedule(new ScheduleBuilder().setProcessName(additionalProcessName)
                    .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                    .setScheduleName("Schedule " + i).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                    .setHourInDay("23").setMinuteInHour("59"));
            scheduleDetail.manualRun();
            scheduleDetail.clickOnCloseScheduleButton();
        }
    }

    private void prepareDataForScheduledProject(OverviewProjectDetails overviewProject) {
        for (OverviewProcess overviewProcess : overviewProject.getOverviewProcesses()) {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processUrl =
                    deployInProjectDetailPage(DeployPackages.BASIC,
                            overviewProcess.getProcessName());
            overviewProcess.setProcessUrl(processUrl);
            for (OverviewSchedule overviewSchedule : overviewProcess.getOverviewSchedules()) {
                createAndAssertSchedule(new ScheduleBuilder()
                        .setProcessName(overviewProcess.getProcessName())
                        .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                        .setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());
                scheduleDetail.manualRun();
            }
        }
    }
}
