package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AbstractOverviewProjectsTest extends AbstractDISCTest {

    private static final int NUMBER_OF_ADDITIONAL_SCHEDULES = 30;

    protected void checkFilteredOutOverviewProject(OverviewProjectStates state, final String projectId) {
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        if (discOverview.getStateNumber(state).equals("0"))
            assertEquals(discOverviewProjects.getOverviewEmptyStateMessage(), state.getOverviewEmptyState(),
                    "Incorrect overview empty state message!");
        else {
            try {
                Predicate<WebDriver> projectsFilteredOut = 
                        webDriver -> discOverviewProjects.getOverviewProjectWithAdminRole(projectId) == null;
                Graphene.waitGui().until(projectsFilteredOut);
            } catch (TimeoutException e) {
                fail("Project is not filtered out on overview page! " + e);
            }
        }
    }

    protected void checkOtherOverviewStates(OverviewProjectStates state, String projectId) {
        List<OverviewProjectStates> projectStateToCheck =
                Arrays.asList(OverviewProjectStates.FAILED, OverviewProjectStates.RUNNING,
                        OverviewProjectStates.SUCCESSFUL);

        for (OverviewProjectStates projectState : projectStateToCheck) {
            if (projectState == state)
                continue;
            if (state != OverviewProjectStates.SCHEDULED) {
                checkFilteredOutOverviewProject(projectState, projectId);
                continue;
            }
            if (projectState != OverviewProjectStates.RUNNING)
                checkFilteredOutOverviewProject(projectState, projectId);
        }
    }

    protected void checkOverviewStateNumber(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
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

        openProjectDetailPage(overviewProject.getProjectId());
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

    protected void checkOverviewProjectWithoutAdminRole(OverviewProjectStates projectState)
            throws JSONException, ParseException, IOException {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails()
            .setProjectId(testParams.getProjectId())
            .setProjectName(projectTitle);
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
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    protected void disableProjectInOverviewPage(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
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
        checkFilteredOutOverviewProject(projectState, testParams.getProjectId());
        checkOtherOverviewStates(projectState, testParams.getProjectId());

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
        checkFilteredOutOverviewProject(projectState, testParams.getProjectId());

        return overviewProject;
    }

    protected void disableScheduleInOverviewPage(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
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
        discOverviewProjects.checkOnOverviewSchedules(new OverviewProjectDetails().setProjectId(
                testParams.getProjectId()).addProcess(disabledProcess));
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
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
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
                new OverviewProjectDetails().setProjectId(testParams.getProjectId()).addProcess(selectedProcess);
        discOverviewProjects.checkOnOverviewSchedules(selectedProjectSchedule);
        discOverviewProjects.bulkAction(projectState);
        browser.navigate().refresh();
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewProject(projectState, new OverviewProjectDetails().setProjectId(testParams.getProjectId())
                .addProcess(overviewProcess));

        browser.get(selectedSchedule.getScheduleUrl());
        waitForFragmentVisible(scheduleDetail);
        if (projectState != OverviewProjectStates.RUNNING) {
            if (scheduleDetail.isStarted()) {
                scheduleDetail.waitForExecutionFinish();
            }
            if (projectState == OverviewProjectStates.FAILED) {
                assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                checkOkExecutionGroup(2, 0);
            }
        } else {
            assertManualStoppedExecution();
            selectedSchedule.setExecutionDescription(scheduleDetail.getLastExecutionDescription());
            selectedSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
            selectedSchedule.setLastExecutionRunTime(scheduleDetail.getLastExecutionRuntime());
        }
    }

    protected void prepareDataForProjectsPageTest(ProjectStateFilters projectFilter, String workingProjectId) {
        openProjectDetailPage(workingProjectId);
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
        prepareDataForProjectsPageTest(projectState, testParams.getProjectId());
        initDISCProjectsPage();
        checkProjectFilter(projectState, testParams.getProjectId());
    }

    protected void checkSearchProjectInSpecificState(ProjectStateFilters projectFilter) {
        prepareDataForProjectsPageTest(projectFilter, testParams.getProjectId());
        initDISCProjectsPage();
        searchProjectInSpecificState(projectFilter, testParams.getProjectId(), projectTitle);
    }

    protected void checkSearchWorkingProjectByName() {
        initDISCProjectsPage();
        searchProjectByName(projectTitle);
    }

    protected void checkSearchWorkingProjectById() {
        initDISCProjectsPage();
        searchProjectById(testParams.getProjectId());
    }

    protected void prepareDataForProject(String projectId) {
        String processName = "Process for preparing data";
        openProjectDetailPage(projectId);
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        prepareAdditionalSchedulesForScheduledState(processName);
    }

    protected void searchProjectByName(String searchKey) {
        discProjectsPage.enterSearchKey(searchKey);
        discProjectsPage.waitForSearchingProgress();
        waitForFragmentVisible(discProjectsList);
        assertTrue(discProjectsList.isCorrectSearchResultByName(searchKey), "Incorrect search result by name!");
    }

    protected void searchProjectById(String projectId) {
        discProjectsPage.enterSearchKey(projectId);
        waitForFragmentVisible(discProjectsList);
        Predicate<WebDriver> projectSearched = webDriver -> discProjectsList.getNumberOfRows() == 1;
        try {
            Graphene.waitGui().until(projectSearched);
        } catch (TimeoutException e) {
            fail("Incorrect number of projects in search result: " + discProjectsList.getNumberOfRows());
        }
        assertNotNull(discProjectsList.selectProjectWithAdminRole(projectId), "Cannot find project id" + projectId);
    }

    protected void searchProjectInSpecificState(ProjectStateFilters projectFilter, String projectId, String projectName) {
        discProjectsPage.selectFilterOption(projectFilter);
        searchProjectByName(projectName);
        searchProjectById(projectId);
    }

    protected void checkProjectFilter(final ProjectStateFilters filterOption, String projectId) {
        List<ProjectStateFilters> filterOutOptions = Stream.of(ProjectStateFilters.DISABLED, ProjectStateFilters.FAILED, 
                ProjectStateFilters.RUNNING,ProjectStateFilters.SCHEDULED, ProjectStateFilters.SUCCESSFUL, 
                ProjectStateFilters.UNSCHEDULED)
                    .filter(filter -> {
                        if (filter == filterOption)
                            return false;
                        if (filterOption == ProjectStateFilters.DISABLED)
                            return filter != ProjectStateFilters.UNSCHEDULED;
                        return true;
                    })
                    .collect(toList());
        checkFilteredProject(filterOption, projectId);
        if (filterOption == ProjectStateFilters.DISABLED)
            checkFilteredProject(ProjectStateFilters.UNSCHEDULED, projectId);
        for (ProjectStateFilters filterOutOption : filterOutOptions) {
            checkFilteredOutProject(filterOutOption, projectId);
  }
    }

    protected void assertOverviewProject(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                discOverviewProjects.getOverviewProjectWithAdminRole(expectedOverviewProject.getProjectId());
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
            WebElement overviewSchedule = overviewSchedules.stream()
                    .filter(input -> discOverviewProjects.getOverviewScheduleName(input)
                            .equals(expectedSchedule.getScheduleName()))
                    .findFirst()
                    .get();
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

    private void checkFilteredOutProject(ProjectStateFilters filterOutOption, String projectId) {
        discProjectsPage.selectFilterOption(filterOutOption);
        waitForFragmentVisible(discProjectsList);
        assertNull(discProjectsList.selectProjectWithAdminRole(projectId),
                "Project isn't filtered out!");
        System.out.println("Project id = "+ projectId + " is filtered out.");
    }

    private void checkFilteredProject(ProjectStateFilters filterOption, String projectId) {
        System.out.println("Check filter option:" + filterOption);
        discProjectsPage.selectFilterOption(filterOption);
        waitForFragmentVisible(discProjectsList);
        assertNotNull(discProjectsList.selectProjectWithAdminRole(projectId),
                "Project doesn't present in filtered list!");
        System.out.println("Project id "+ projectId + " is in filtered list.");
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
}
