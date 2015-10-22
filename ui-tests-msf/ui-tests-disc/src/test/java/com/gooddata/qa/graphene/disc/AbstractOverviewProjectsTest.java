package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AbstractOverviewProjectsTest extends AbstractDISCTest {

    private static final int NUMBER_OF_ADDITIONAL_SCHEDULES = 30;

    protected void checkFilteredOutOverviewProject(OverviewProjectStates state, final ProjectInfo projectInfo) {
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        if (discOverview.getStateNumber(state).equals("0"))
            assertEquals(discOverviewProjects.getOverviewEmptyStateMessage(), state.getOverviewEmptyState(),
                    "Incorrect overview empty state message!");
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
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Project Number");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        discOverview.selectOverviewState(projectState);
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewStateNumber(projectState, discOverviewProjects.getOverviewProjectNumber());
    }

    protected OverviewProjectDetails prepareDataForCheckingOverviewState(OverviewProjectStates state,
            OverviewProjectDetails overviewProject) {
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
            String processUrl = deployInProjectDetailPage(DeployPackages.BASIC, overviewProcess.getProcessName());
            overviewProcess.setProcessUrl(processUrl);

            for (OverviewSchedule overviewSchedule : overviewProcess.getOverviewSchedules()) {
                createSchedule(new ScheduleBuilder().setProcessName(overviewProcess.getProcessName())
                        .setExecutable(executable).setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());

                scheduleDetail.manualRun();

                if (state == OverviewProjectStates.RUNNING) {
                    assertTrue(scheduleDetail.isInRunningState());
                    overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());
                    overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                    continue;
                }

                if (state == OverviewProjectStates.FAILED)
                    assertFailedExecution(executable);
                else if (state == OverviewProjectStates.SUCCESSFUL)
                    assertSuccessfulExecution();

                overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());
                overviewSchedule.setExecutionDescription(scheduleDetail.getLastExecutionDescription());
                overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                overviewSchedule.setLastExecutionRunTime(scheduleDetail.getLastExecutionRuntime());
            }
        }
        return overviewProject;
    }

    protected void checkOverviewProjectWithoutAdminRole(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Project With Non-Admin Role");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
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
            waitForFragmentVisible(discOverviewProjects);
            checkProjectNotAdmin(projectState, overviewProject);
            openUrl(PAGE_PROJECTS);
            logout();

            signIn(false, UserRoles.EDITOR);
            openUrl(DISC_OVERVIEW_PAGE);
            discOverview.selectOverviewState(projectState);
            waitForFragmentVisible(discOverviewProjects);
            checkProjectNotAdmin(projectState, overviewProject);
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
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(projectState);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.disableAction();
        browser.navigate().refresh();
        waitForFragmentVisible(discOverviewProjects);
        checkFilteredOutOverviewProject(projectState, getWorkingProject());
        checkOtherOverviewStates(projectState, getWorkingProject());

        browser.get(overviewSchedule.getScheduleUrl());
        waitForFragmentVisible(scheduleDetail);
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    protected OverviewProjectDetails bulkActionsProjectInOverviewPage(OverviewProjectStates projectState,
            OverviewProjectDetails overviewProject) {
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(projectState);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(projectState);
        browser.navigate().refresh();
        checkFilteredOutOverviewProject(projectState, getWorkingProject());

        return overviewProject;
    }

    protected void disableScheduleInOverviewPage(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Schedule 1");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        OverviewProcess disabledProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Schedule 2");
        OverviewSchedule disabledSchedule = disabledProcess.newSchedule().setScheduleName("Disabled Schedule");
        disabledProcess.addSchedule(disabledSchedule);
        overviewProject.addProcess(overviewProcess).addProcess(disabledProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        discOverview.selectOverviewState(projectState);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnOverviewSchedules(new OverviewProjectDetails().setProjectInfo(
                getWorkingProject()).addProcess(disabledProcess));
        discOverviewProjects.disableAction();
        overviewProject.removeProcess(disabledProcess);
        discOverview.getStateNumber(projectState);
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewProject(projectState, overviewProject);

        browser.get(disabledSchedule.getScheduleUrl());
        waitForFragmentVisible(scheduleDetail);
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    protected void bulkActionsScheduleInOverviewPage(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Bulk Action Overview Schedule 1");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        OverviewProcess selectedProcess =
                overviewProject.newProcess().setProcessName("Check Bulk Action Overview Schedule 2");
        OverviewSchedule selectedSchedule = selectedProcess.newSchedule().setScheduleName("Selected Schedule");
        selectedProcess.addSchedule(selectedSchedule);
        overviewProject.addProcess(overviewProcess).addProcess(selectedProcess);
        prepareDataForCheckingOverviewState(projectState, overviewProject);

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(projectState);
        waitForFragmentVisible(discOverviewProjects);
        OverviewProjectDetails selectedProjectSchedule =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject()).addProcess(selectedProcess);
        discOverviewProjects.checkOnOverviewSchedules(selectedProjectSchedule);
        discOverviewProjects.bulkAction(projectState);
        browser.navigate().refresh();
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewProject(projectState, new OverviewProjectDetails().setProjectInfo(getWorkingProject())
                .addProcess(overviewProcess));

        browser.get(selectedSchedule.getScheduleUrl());
        waitForFragmentVisible(scheduleDetail);
        if (projectState != OverviewProjectStates.RUNNING) {
            try {
                assertTrue(scheduleDetail.isStarted(), "Schedule execution is not started!");
                scheduleDetail.waitForExecutionFinish();
            } catch (NoSuchElementException ex) {
                if (projectState == OverviewProjectStates.FAILED)
                    assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
                else if (projectState == OverviewProjectStates.SUCCESSFUL)
                    checkOkExecutionGroup(2, 0);
            }
        } else {
            assertManualStoppedExecution();
            selectedSchedule.setExecutionDescription(scheduleDetail.getLastExecutionDescription());
            selectedSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
            selectedSchedule.setLastExecutionRunTime(scheduleDetail.getLastExecutionRuntime());
        }
    }

    protected void cleanupProcessesAndProjects(boolean deleteProjects, List<ProjectInfo> additionalProjects) {
        cleanProcessesInWorkingProject();
        if (deleteProjects)
            deleteProjects(additionalProjects);
    }

    protected void prepareDataForProjectsPageTest(ProjectStateFilters projectFilter, ProjectInfo workingProject) {
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
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59");
        createSchedule(scheduleBuilder);
        scheduleDetail.manualRun();
        if (projectFilter == ProjectStateFilters.SCHEDULED)
            return;

        if (projectFilter == ProjectStateFilters.RUNNING) {
            assertTrue(scheduleDetail.isInRunningState(), "Schedule execution is not in RUNNING state!");
            return;
        }

        if (projectFilter == ProjectStateFilters.FAILED)
            assertFailedExecution(executable);
        else
            assertSuccessfulExecution();

        if (projectFilter == ProjectStateFilters.DISABLED)
            scheduleDetail.disableSchedule();
    }

    protected void checkProjectsFilter(ProjectStateFilters projectState) {
        prepareDataForProjectsPageTest(projectState, getWorkingProject());
        initDISCProjectsPage();
        checkProjectFilter(projectState, getProjects());
    }

    protected void checkSearchProjectInSpecificState(ProjectStateFilters projectFilter) {
        prepareDataForProjectsPageTest(projectFilter, getWorkingProject());
        initDISCProjectsPage();
        searchProjectInSpecificState(projectFilter, getWorkingProject());
    }

    protected void checkSearchWorkingProjectByName() {
        initDISCProjectsPage();
        searchProjectByName(projectTitle);
    }

    protected void checkSearchWorkingProjectById() {
        initDISCProjectsPage();
        searchProjectById(getWorkingProject());
    }

    protected void prepareDataForAdditionalProjects(List<ProjectInfo> additionalProjects) {
        String additionalProcessName = "Process for additional projects";
        for (ProjectInfo project : additionalProjects) {
            openProjectDetailByUrl(project.getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, additionalProcessName);
            prepareAdditionalSchedulesForScheduledState(additionalProcessName);
        }
    }

    protected void searchProjectByName(String searchKey) {
        discProjectsPage.enterSearchKey(searchKey);
        discProjectsPage.waitForSearchingProgress();
        waitForFragmentVisible(discProjectsList);
        assertTrue(discProjectsList.isCorrectSearchResultByName(searchKey), "Incorrect search result by name!");
    }

    protected void searchProjectById(ProjectInfo project) {
        discProjectsPage.enterSearchKey(project.getProjectId());
        waitForFragmentVisible(discProjectsList);
        try {
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return discProjectsList.getNumberOfRows() == 1;
                }
            });
        } catch (TimeoutException e) {
            fail("Incorrect number of projects in search result: " + discProjectsList.getNumberOfRows());
        }
        assertNotNull(discProjectsList.selectProjectWithAdminRole(project),
                "Cannot find project " + project.getProjectName());
    }

    protected void searchProjectInSpecificState(ProjectStateFilters projectFilter, ProjectInfo project) {
        discProjectsPage.selectFilterOption(projectFilter);
        searchProjectByName(project.getProjectName());
        searchProjectById(project);
    }

    protected void checkProjectFilter(final ProjectStateFilters filterOption, List<ProjectInfo> projects) {
        List<ProjectStateFilters> filters =
                Arrays.asList(ProjectStateFilters.DISABLED, ProjectStateFilters.FAILED,
                        ProjectStateFilters.RUNNING, ProjectStateFilters.SCHEDULED,
                        ProjectStateFilters.SUCCESSFUL, ProjectStateFilters.UNSCHEDULED);
        Iterable<ProjectStateFilters> filterOutOptions =
                Iterables.filter(filters, new Predicate<ProjectStateFilters>() {

                    @Override
                    public boolean apply(ProjectStateFilters filter) {
                        if (filter == filterOption)
                            return false;
                        if (filterOption == ProjectStateFilters.DISABLED)
                            return filter != ProjectStateFilters.UNSCHEDULED;
                        return true;
                    }
                });

        checkFilteredProjects(filterOption, projects);
        if (filterOption == ProjectStateFilters.DISABLED)
            checkFilteredProjects(ProjectStateFilters.UNSCHEDULED, projects);

        for (ProjectStateFilters filterOutOption : filterOutOptions) {
            checkFilteredOutProjects(filterOutOption, projects);
        }
    }

    protected void assertOverviewProject(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                discOverviewProjects.getOverviewProjectWithAdminRole(expectedOverviewProject.getProjectInfo());
        assertNotNull(overviewProjectDetail, "Cannot find working project on overview page!");
        assertTrue(discOverviewProjects.getOverviewProjectExpandButton(overviewProjectDetail).isEnabled(),
                "Overview project expand button is not enabled!");
        if (overviewProjectDetail.getAttribute("class").contains("expanded-border"))
            discOverviewProjects.getOverviewProjectExpandButton(overviewProjectDetail).click();
        int projectScheduleNumber = expectedOverviewProject.getProjectScheduleNumber();
        if (projectScheduleNumber == 1) {
            System.out.println("Overview Schedule Number: " + projectScheduleNumber);
            OverviewSchedule overviewSchedule =
                    expectedOverviewProject.getOverviewProcesses().get(0).getOverviewSchedules().get(0);
            assertProjectInfoWithOnlyOneSchedule(projectState, overviewProjectDetail, overviewSchedule);
        } else if (projectScheduleNumber > 1) {
            System.out.println("Overview Schedule Number: " + projectScheduleNumber);
            assertProjectInfoWithMultipleSchedule(projectState, overviewProjectDetail, projectScheduleNumber);
        }
        discOverviewProjects.getOverviewProjectExpandButton(overviewProjectDetail).click();
        discOverviewProjects.waitForOverviewProcessesLoaded();
        assertOverviewProcesses(projectState, expectedOverviewProject.getOverviewProcesses());
    }

    protected void checkProjectNotAdmin(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProject =
                discOverviewProjects.getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProject, "Cannot find project without admin role!");
        try {
            discOverviewProjects.getOverviewProjectName(overviewProject).click();
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until().element(projectDetailPage.getRoot()).is()
                    .visible();
        } catch (NoSuchElementException ex) {
            System.out.println("Non-admin user cannot access project detail page!");
        }
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewProjectWithoutAdminRole(projectState, expectedOverviewProject);
    }

    protected void assertOverviewStateNumber(OverviewProjectStates state, int number) {
        assertTrue(state.getOption().equalsIgnoreCase(discOverview.getState(state)), "Incorrect state: "
                + discOverview.getState(state));
        assertEquals(discOverview.getStateNumber(state), String.valueOf(number),
                "Incorrect number of project in state " + state.getOption());
    }

    private void assertOverviewProjectWithoutAdminRole(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                discOverviewProjects.getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProjectDetail, "Cannot find project without admin role!");
        WebElement overviewProjectLogLinkElement =
                discOverviewProjects.getOverviewProjectLog(overviewProjectDetail);
        int projectScheduleNumber = expectedOverviewProject.getProjectScheduleNumber();
        String overviewProjectRuntime =
                discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText();
        String overviewProjectDate = discOverviewProjects.getOverviewProjectDate(overviewProjectDetail).getText();
        if (projectScheduleNumber == 1) {
            OverviewSchedule expectedOverviewSchedule =
                    expectedOverviewProject.getOverviewProcesses().get(0).getOverviewSchedules().get(0);
            if (projectState != OverviewProjectStates.SCHEDULED)
                assertTrue(overviewProjectLogLinkElement.getAttribute("class").contains("action-unavailable-icon"));
            assertProjectInfoWithOnlyOneSchedule(projectState, overviewProjectDetail, expectedOverviewSchedule);
        } else if (projectScheduleNumber > 1) {
            if (projectState != OverviewProjectStates.SCHEDULED) {
                assertTrue(overviewProjectRuntime.isEmpty(), "Overview project runtime is not empty!");
                assertTrue(overviewProjectDate.isEmpty(), "Overview project date is not empty!");
                if (projectState == OverviewProjectStates.FAILED) {
                    String errorMessage = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(discOverviewProjects.getOverviewProjectErrorMessage(overviewProjectDetail)
                            .getText(), errorMessage, "Incorrect error message");
                } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                    String okInfo = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(discOverviewProjects.getOverviewProjectOKInfo(overviewProjectDetail).getText(),
                            okInfo, "Incorrect OK info!");
                }
            }
        }
    }

    private void assertProjectInfoWithOnlyOneSchedule(OverviewProjectStates projectState,
            WebElement overviewProjectDetail, OverviewSchedule expectedOverviewSchedule) {
        if (projectState != OverviewProjectStates.SCHEDULED) {
            assertTrue(discOverviewProjects.getOverviewProjectLog(overviewProjectDetail).isEnabled(),
                    "Log link is not enabled!");
            assertFalse(discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText().isEmpty(),
                    "Execution runtime is empty!");
            System.out.println("Project schedule runtime: "
                    + discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText());
            if (projectState != OverviewProjectStates.RUNNING) {
                assertEquals(expectedOverviewSchedule.getLastExecutionRunTime(), discOverviewProjects
                        .getOverviewProjectRuntime(overviewProjectDetail).getText(),
                        "Incorrect execution runtime!");
                assertEquals(expectedOverviewSchedule.getOverviewExecutionDateTime(), discOverviewProjects
                        .getOverviewProjectDate(overviewProjectDetail).getText(), "Incorrect execution date!");
            } else
                assertTrue(
                        expectedOverviewSchedule.getOverviewExecutionDateTime().contains(
                                discOverviewProjects.getOverviewProjectDate(overviewProjectDetail).getText()),
                        "Incorrect execution date!");
            if (projectState == OverviewProjectStates.FAILED) {
                assertEquals(expectedOverviewSchedule.getExecutionDescription(), discOverviewProjects
                        .getOverviewProjectErrorMessage(overviewProjectDetail).getText(),
                        "Incorrect error message!");
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                assertEquals("1 schedule", discOverviewProjects.getOverviewProjectOKInfo(overviewProjectDetail)
                        .getText(), "Incorrect successful info!");
            }
        }
    }

    private void assertProjectInfoWithMultipleSchedule(OverviewProjectStates projectState,
            WebElement overviewProjectDetail, int projectScheduleNumber) {
        if (projectState != OverviewProjectStates.SCHEDULED) {
            assertTrue(discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText().isEmpty(),
                    "Execution runtime is empty!");
            assertTrue(discOverviewProjects.getOverviewProjectDate(overviewProjectDetail).getText().isEmpty(),
                    "Execution date is empty!");
            if (projectState == OverviewProjectStates.FAILED) {
                String errorMessage = String.format("%d schedules", projectScheduleNumber);
                assertEquals(discOverviewProjects.getOverviewProjectErrorMessage(overviewProjectDetail).getText(),
                        errorMessage, "Incorrect error message!");
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                String okInfo = String.format("%d schedules", projectScheduleNumber);
                assertEquals(discOverviewProjects.getOverviewProjectOKInfo(overviewProjectDetail).getText(),
                        okInfo, "Incorrect OK info!");
            }
        }
    }

    private void assertOverviewProcesses(OverviewProjectStates projectState,
            List<OverviewProcess> expectedOverviewProcesses) {
        assertEquals(expectedOverviewProcesses.size(), discOverviewProjects.getProcessNumber());
        for (final OverviewProcess expectedProcess : expectedOverviewProcesses) {
            WebElement overviewProcess = discOverviewProjects.getOverviewProcess(expectedProcess);
            String processDetailUrl = expectedProcess.getProcessUrl();
            System.out.println("processDetailUrl: " + processDetailUrl);
            assertEquals(discOverviewProjects.getOverviewProcessName(overviewProcess).getAttribute("href"),
                    processDetailUrl, "Incorrect process detail link!");
            assertOverviewSchedules(projectState, expectedProcess.getOverviewSchedules(),
                    discOverviewProjects.getOverviewSchedules(overviewProcess));
        }
    }

    private void assertOverviewSchedules(OverviewProjectStates state, List<OverviewSchedule> expectedSchedules,
            List<WebElement> overviewSchedules) {
        for (final OverviewSchedule expectedSchedule : expectedSchedules) {
            WebElement overviewSchedule = Iterables.find(overviewSchedules, new Predicate<WebElement>() {
                @Override
                public boolean apply(WebElement input) {
                    return discOverviewProjects.getOverviewScheduleName(input).equals(
                            expectedSchedule.getScheduleName());
                }
            });
            assertEquals(discOverviewProjects.getOverviewScheduleLink(state, overviewSchedule),
                    expectedSchedule.getScheduleUrl(), "Incorrect schedule detail link!");

            if (state == OverviewProjectStates.SCHEDULED)
                continue;
            assertTrue(discOverviewProjects.getOverviewProjectLog(overviewSchedule).isEnabled(),
                    "Log link is not enabled!");
            assertFalse(discOverviewProjects.getOverviewProjectRuntime(overviewSchedule).getText().isEmpty(),
                    "Execution runtime id not empty!");
            if (state != OverviewProjectStates.RUNNING) {
                assertEquals(discOverviewProjects.getOverviewProjectDate(overviewSchedule).getText(),
                        expectedSchedule.getOverviewExecutionDateTime(), "Incorrect execution date!");
                assertEquals(discOverviewProjects.getOverviewProjectRuntime(overviewSchedule).getText(),
                        expectedSchedule.getLastExecutionRunTime(), "Incorrect execution runtime!");
                if (state == OverviewProjectStates.FAILED)
                    assertEquals(discOverviewProjects.getOverviewProjectErrorMessage(overviewSchedule).getText(),
                            expectedSchedule.getExecutionDescription(), "Incorrect execution error message!");
            } else
                assertEquals(discOverviewProjects.getOverviewProjectDate(overviewSchedule).getText(),
                        expectedSchedule.getOverviewStartTime(), "Incorrect execution start time!");
        }
    }

    private void checkFilteredOutProjects(ProjectStateFilters filterOutOption,
            List<ProjectInfo> filteredOutProjects) {
        discProjectsPage.selectFilterOption(filterOutOption);
        waitForFragmentVisible(discProjectsList);
        for (ProjectInfo filteredOutProject : filteredOutProjects) {
            assertNull(discProjectsList.selectProjectWithAdminRole(filteredOutProject),
                    "Project isn't filtered out!");
            System.out.println("Project " + filteredOutProject.getProjectName() + "(id = "
                    + filteredOutProject.getProjectId() + ") is filtered out.");
        }
    }

    private void checkFilteredProjects(ProjectStateFilters filterOption, List<ProjectInfo> filteredProjects) {
        System.out.println("Check filter option:" + filterOption);
        discProjectsPage.selectFilterOption(filterOption);
        waitForFragmentVisible(discProjectsList);
        for (ProjectInfo filteredProject : filteredProjects) {
            assertNotNull(discProjectsList.selectProjectWithAdminRole(filteredProject),
                    "Project doesn't present in filtered list!");
            System.out.println("Project " + filteredProject.getProjectName() + " (id = "
                    + filteredProject.getProjectId() + ") is in filtered list.");
        }
    }

    private void prepareAdditionalSchedulesForScheduledState(String additionalProcessName) {
        List<String> scheduleUrls = Lists.newArrayList();
        for (int i = 1; i < NUMBER_OF_ADDITIONAL_SCHEDULES; i++) {
            createSchedule(new ScheduleBuilder().setProcessName(additionalProcessName)
                    .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH).setScheduleName("Schedule " + i)
                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
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
            String processUrl = deployInProjectDetailPage(DeployPackages.BASIC, overviewProcess.getProcessName());
            overviewProcess.setProcessUrl(processUrl);
            for (OverviewSchedule overviewSchedule : overviewProcess.getOverviewSchedules()) {
                createSchedule(new ScheduleBuilder().setProcessName(overviewProcess.getProcessName())
                        .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                        .setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());
                scheduleDetail.manualRun();
            }
        }
    }
}
