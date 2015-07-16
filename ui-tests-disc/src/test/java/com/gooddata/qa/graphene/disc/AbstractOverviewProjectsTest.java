package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
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
import com.google.common.collect.Lists;

public class AbstractOverviewProjectsTest extends AbstractDISCTest {

    private static final int NUMBER_OF_ADDITIONAL_SCHEDULES = 30;

    protected void checkFilteredOutOverviewProject(OverviewProjectStates state,
            final ProjectInfo projectInfo) {
        discOverview.selectOverviewState(state);
        waitForElementVisible(discOverviewProjects.getRoot());
        if (discOverview.getStateNumber(state).equals("0"))
            discOverviewProjects.assertOverviewEmptyState(state);
        else {
            try {
                Graphene.waitGui().until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver arg0) {
                        return discOverviewProjects.getOverviewProjectWithAdminRole(projectInfo) == null;
                    }
                });
            } catch (TimeoutException e) {
                fail("Project is not filtered out on overview page! " + e);
            }
        }
    }

    protected void checkOtherOverviewStates(OverviewProjectStates state, ProjectInfo projectInfo) {
        List<OverviewProjectStates> projectStateToCheck =
                Arrays.asList(OverviewProjectStates.FAILED, OverviewProjectStates.RUNNING,
                        OverviewProjectStates.SUCCESSFUL);
        /*
         * Remove checking step in SCHEDULED state until MSF-7415 is fixed
         * 
         * List<OverviewProjectStates> projectStateToCheck =
         * Arrays.asList(OverviewProjectStates.FAILED, OverviewProjectStates.RUNNING,
         * OverviewProjectStates.SCHEDULED, OverviewProjectStates.SUCCESSFUL);
         */
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

    protected void prepareDataForOverviewScheduledStateTests(List<ProjectInfo> additionalProjects,
            OverviewProjectDetails overviewProject) {
        prepareDataForAdditionalProjects(additionalProjects);
        prepareDataForScheduledProject(overviewProject);
    }

    protected void checkOverviewStateNumber(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Project Number");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverview.assertOverviewStateNumber(projectState,
                discOverviewProjects.getOverviewProjectNumber());
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
                createSchedule(new ScheduleBuilder()
                        .setProcessName(overviewProcess.getProcessName()).setExecutable(executable)
                        .setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());

                scheduleDetail.manualRun();

                if (state == OverviewProjectStates.RUNNING) {
                    assertTrue(scheduleDetail.isInRunningState());
                    overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());
                    overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                    continue;
                }

                if (state == OverviewProjectStates.FAILED)
                    scheduleDetail.assertFailedExecution(executable);
                else if (state == OverviewProjectStates.SUCCESSFUL)
                    scheduleDetail.assertSuccessfulExecution();

                overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());
                overviewSchedule.setExecutionDescription(scheduleDetail.getExecutionDescription());
                overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                overviewSchedule.setLastExecutionRunTime(scheduleDetail.getExecutionRuntime());
            }
        }
        return overviewProject;
    }

    protected void checkOverviewProjectWithoutAdminRole(OverviewProjectStates projectState) {
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

        try {
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
        } catch (ParseException e) {
            fail("There is problem when adding user to project: " + e);
        } catch (IOException e) {
            fail("There is problem when adding user to project: " + e);
        } catch (JSONException e) {
            fail("There is problem when adding user to project or signIn: " + e);
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            try {
                signIn(false, UserRoles.ADMIN);
            } catch (JSONException e) {
                fail("There is problem when signIn: " + e);
            }
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

        initDISCOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.disableAction();
        browser.navigate().refresh();
        waitForElementVisible(discOverviewProjects.getRoot());
        checkFilteredOutOverviewProject(projectState, getWorkingProject());
        checkOtherOverviewStates(projectState, getWorkingProject());

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    protected OverviewProjectDetails bulkActionsProjectInOverviewPage(
            OverviewProjectStates projectState, OverviewProjectDetails overviewProject) {
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(projectState);
        browser.navigate().refresh();
        checkFilteredOutOverviewProject(projectState, getWorkingProject());

        return overviewProject;
    }

    protected void disableScheduleInOverviewPage(OverviewProjectStates projectState) {
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

        initDISCOverviewPage();
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

    protected void bulkActionsScheduleInOverviewPage(OverviewProjectStates projectState) {
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

        initDISCOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        OverviewProjectDetails selectedProjectSchedule =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject()).addProcess(
                        selectedProcess);
        discOverviewProjects.checkOnOverviewSchedules(selectedProjectSchedule);
        discOverviewProjects.bulkAction(projectState);
        browser.navigate().refresh();
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
                if (projectState == OverviewProjectStates.FAILED)
                    assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
                else if (projectState == OverviewProjectStates.SUCCESSFUL)
                    scheduleDetail.checkOkExecutionGroup(2, 0);
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
        cleanProcessesInWorkingProject();
        if (deleteProjects)
            deleteProjects(additionalProjects);
    }

    protected void prepareDataForProjectsPageTest(ProjectStateFilters projectFilter,
            ProjectInfo workingProject) {
        openProjectDetailByUrl(workingProject.getProjectId());
        String processName = "Process for projects page tests";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        if (projectFilter == ProjectStateFilters.UNSCHEDULED)
            return;

        Executables executable = null;
        switch (projectFilter) {
            case FAILED:
                executable = Executables.FAILED_GRAPH;
                break;
            case SUCCESSFUL:
                executable = Executables.SUCCESSFUL_GRAPH;
                break;
            case SCHEDULED:
            case RUNNING:
                executable = Executables.LONG_TIME_RUNNING_GRAPH;
                break;
            default:
                executable = Executables.SUCCESSFUL_GRAPH;
        }

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(executable)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59");
        createSchedule(scheduleBuilder);
        scheduleDetail.manualRun();
        if (projectFilter == ProjectStateFilters.SCHEDULED)
            return;

        if (projectFilter == ProjectStateFilters.RUNNING) {
            assertTrue(scheduleDetail.isInRunningState());
            return;
        }

        if (projectFilter == ProjectStateFilters.FAILED)
            scheduleDetail.assertFailedExecution(executable);
        else
            scheduleDetail.assertSuccessfulExecution();

        if (projectFilter == ProjectStateFilters.DISABLED)
            scheduleDetail.disableSchedule();
    }

    protected void checkProjectsFilter(ProjectStateFilters projectState) {
        prepareDataForProjectsPageTest(projectState, getWorkingProject());
        initDISCProjectsPage();
        discProjectsPage.checkProjectFilter(projectState, getProjects());
    }

    protected void checkSearchProjectInSpecificState(ProjectStateFilters projectFilter) {
        prepareDataForProjectsPageTest(projectFilter, getWorkingProject());
        initDISCProjectsPage();
        discProjectsPage.searchProjectInSpecificState(projectFilter, getWorkingProject());
    }

    protected void checkSearchWorkingProjectByName() {
        initDISCProjectsPage();
        discProjectsPage.searchProjectByName(projectTitle);
    }

    protected void checkSearchWorkingProjectById() {
        initDISCProjectsPage();
        discProjectsPage.searchProjectById(getWorkingProject());
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
        List<String> scheduleUrls = Lists.newArrayList();
        for (int i = 1; i < NUMBER_OF_ADDITIONAL_SCHEDULES; i++) {
            createSchedule(new ScheduleBuilder().setProcessName(additionalProcessName)
                    .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                    .setScheduleName("Schedule " + i).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                    .setHourInDay("23").setMinuteInHour("59"));
            scheduleUrls.add(browser.getCurrentUrl());
            scheduleDetail.clickOnCloseScheduleButton();
        }

        for (String scheduleUrl : scheduleUrls) {
            openScheduleViaUrl(scheduleUrl);
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
                createSchedule(new ScheduleBuilder()
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
