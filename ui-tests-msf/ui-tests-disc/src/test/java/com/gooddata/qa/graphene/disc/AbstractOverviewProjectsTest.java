package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.google.common.base.Predicate;

public class AbstractOverviewProjectsTest extends AbstractDISCTest {

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

                if (state == OverviewProjectStates.FAILED) {
                    assertFailedExecution(executable);
                    overviewSchedule.setExecutionDescription(scheduleDetail.getExecutionErrorDescription());
                } else if (state == OverviewProjectStates.SUCCESSFUL) {
                    assertSuccessfulExecution();
                    overviewSchedule.setExecutionDescription(scheduleDetail.getLastExecutionDescription());
                }
                overviewSchedule.setLastExecutionDate(scheduleDetail.getLastExecutionDate());
                overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                overviewSchedule.setLastExecutionRunTime(scheduleDetail.getLastExecutionRuntime());
            }
        }
        return overviewProject;
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

    protected void checkSearchWorkingProjectByName() {
        initDISCProjectsPage();
        searchProjectByName(projectTitle);
    }

    protected void checkSearchWorkingProjectById() {
        initDISCProjectsPage();
        searchProjectById(testParams.getProjectId());
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

    private void checkProjectFilter(final ProjectStateFilters filterOption, String projectId) {
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

    private void assertOverviewStateNumber(OverviewProjectStates state, int number) {
        assertTrue(state.getOption().equalsIgnoreCase(discOverview.getState(state)), "Incorrect state: "
                + discOverview.getState(state));
        assertEquals(discOverview.getStateNumber(state), String.valueOf(number),
                "Incorrect number of project in state " + state.getOption());
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
}
