package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.text.ParseException;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class OverviewPageTest extends AbstractSchedulesTests {

    private static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-overview-page";
        startPage = DISC_OVERVIEW_PAGE;
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkDefaultOverviewState() {
        openOverviewPage();
        assertTrue(discOverview.isActive(OverviewProjectStates.FAILED));
        assertFalse(discOverview.isActive(OverviewProjectStates.RUNNING));
        assertFalse(discOverview.isActive(OverviewProjectStates.SCHEDULED));
        assertFalse(discOverview.isActive(OverviewProjectStates.SUCCESSFUL));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkEmptyFailedState() {
        openOverviewPage();
        checkFilteredOutProject(OverviewProjectStates.FAILED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkEmptyRunningState() {
        openOverviewPage();
        checkFilteredOutProject(OverviewProjectStates.RUNNING, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkEmptyScheduledState() {
        openOverviewPage();
        checkFilteredOutProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkEmptySucessfulState() {
        openOverviewPage();
        checkFilteredOutProject(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkFailedStateNumber() {
        checkStateNumber(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkRunningStateNumber() {
        checkStateNumber(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkSuccessfulStateNumber() throws JSONException, InterruptedException {
        checkStateNumber(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkScheduledStateNumber() {
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Scheduled State Number");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForScheduledStateTests(getProjects(),
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkCombinedStatesNumber() {
        try {
            String processName = "Check Combined States Number";
            openProjectDetailPage(getWorkingProject());
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
            checkFilteredOutProject(OverviewProjectStates.RUNNING, getWorkingProject());
            checkFilteredOutProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkOverviewFailedProjects() throws ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

            prepareDataForCheckingProjectState(OverviewProjectStates.FAILED, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.FAILED,
                    overviewProject);

            checkOtherStates(OverviewProjectStates.FAILED, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
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

            prepareDataForCheckingProjectState(OverviewProjectStates.SUCCESSFUL, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.SUCCESSFUL,
                    overviewProject);

            checkOtherStates(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkOverviewScheduledProject() throws ParseException, JSONException,
            InterruptedException {
        List<ProjectInfo> additionalProjects =
                createMultipleProjects("Disc-test-scheduled-state", 1);
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForScheduledStateTests(additionalProjects, overviewProject);

            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.SCHEDULED,
                    overviewProject);
            checkOtherStates(OverviewProjectStates.SCHEDULED, getWorkingProject());
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkOverviewRunningProject() throws ParseException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Running Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Running Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

            prepareDataForCheckingProjectState(OverviewProjectStates.RUNNING, overviewProject);
            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.RUNNING);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.RUNNING,
                    overviewProject);

            checkOtherStates(OverviewProjectStates.RUNNING, getWorkingProject());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void restartFailedProjects() throws JSONException, InterruptedException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            checkBulkActionsOnProject(OverviewProjectStates.FAILED, overviewProject);

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

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void restartFailedSchedule() throws ParseException {
        try {
            checkBulkActionsOnSchedule(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableFailedProjects() throws JSONException, InterruptedException {
        try {
            checkDisableProject(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableFailedSchedule() throws JSONException, InterruptedException, ParseException {
        try {
            checkDisableSchedule(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void runSuccessfulProjects() throws JSONException, InterruptedException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName(
                            "Check Run Overview Successful Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Successful Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            checkBulkActionsOnProject(OverviewProjectStates.SUCCESSFUL, overviewProject);

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            assertTrue(scheduleDetail.isStarted());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void runSuccessfulSchedule() throws ParseException {
        try {
            checkBulkActionsOnSchedule(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableSuccessfulProjects() throws JSONException, InterruptedException {
        try {
            checkDisableProject(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableSuccessfulSchedule() throws ParseException {
        try {
            checkDisableSchedule(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void stopRunningProjects() throws JSONException, InterruptedException {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName(
                            "Check Stop Overview Running Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Running Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            checkBulkActionsOnProject(OverviewProjectStates.RUNNING, overviewProject);

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void stopRunningSchedule() throws ParseException {
        try {
            checkBulkActionsOnSchedule(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableRunningProjects() throws JSONException, InterruptedException {
        try {
            checkDisableProject(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableRunningSchedule() throws ParseException {
        try {
            checkDisableSchedule(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void disableScheduledProjects() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                createMultipleProjects("Disc-test-scheduled-state", 1);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.disableAction();
            checkOtherStates(OverviewProjectStates.ALL, getWorkingProject());
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void stopScheduledProjects() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                createMultipleProjects("Disc-test-scheduled-state", 1);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.bulkAction(OverviewProjectStates.SCHEDULED);
            checkOtherStates(OverviewProjectStates.FAILED, getWorkingProject());

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            cleanupProcessesAndProjects(true, additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProjectsNotAdminInFailedState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkProjectNotAdmin(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProjectsNotAdminInSucessfulState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkProjectNotAdmin(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProjectsNotAdminInRunningState() throws ParseException, IOException,
            JSONException, InterruptedException {
        checkProjectNotAdmin(OverviewProjectStates.RUNNING);
    }

    private void checkFilteredOutProject(OverviewProjectStates state, ProjectInfo projectInfo) {
        discOverview.selectOverviewState(state);
        waitForElementVisible(discOverviewProjects.getRoot());
        if (discOverview.getStateNumber(state).equals("0"))
            discOverviewProjects.assertOverviewEmptyState(state);
        else
            assertNull(discOverviewProjects.getOverviewProjectWithAdminRole(projectInfo));
    }

    private void checkOtherStates(OverviewProjectStates state, ProjectInfo projectInfo) {
        List<OverviewProjectStates> projectStateToCheck =
                Arrays.asList(OverviewProjectStates.FAILED, OverviewProjectStates.RUNNING,
                        OverviewProjectStates.SCHEDULED, OverviewProjectStates.SUCCESSFUL);
        for (OverviewProjectStates projectState : projectStateToCheck) {
            if (projectState != state) {
                if (state != OverviewProjectStates.SCHEDULED)
                    checkFilteredOutProject(projectState, projectInfo);
                if (state == OverviewProjectStates.SCHEDULED
                        && projectState != OverviewProjectStates.RUNNING) {
                    checkFilteredOutProject(projectState, projectInfo);
                }
            }
        }
    }

    private void openOverviewPage() {
        openUrl(DISC_OVERVIEW_PAGE);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !discOverview.getStateNumber(OverviewProjectStates.FAILED).isEmpty();
            }
        });
        waitForElementVisible(discOverviewProjects.getRoot());
    }

    private void prepareDataForScheduledStateTests(List<ProjectInfo> additionalProjects,
            OverviewProjectDetails overviewProject) {
        for (ProjectInfo project : additionalProjects) {
            openProjectDetailPage(project);
            String additionalProcessName = "Process";
            deployInProjectDetailPage(DeployPackages.BASIC, additionalProcessName);
            for (int i = 1; i < 7; i++) {
                createAndAssertSchedule(new ScheduleBuilder().setProcessName(additionalProcessName)
                        .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                        .setScheduleName("Schedule " + i)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59"));
                scheduleDetail.manualRun();
                scheduleDetail.clickOnCloseScheduleButton();
            }
        }

        for (OverviewProcess overviewProcess : overviewProject.getOverviewProcesses()) {
            openProjectDetailPage(getWorkingProject());
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

    private void checkStateNumber(OverviewProjectStates projectState) {
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Project Number");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Schedule");
            overviewProcess.addSchedule(overviewSchedule);
            overviewProject.addProcess(overviewProcess);
            prepareDataForCheckingProjectState(projectState, overviewProject);

            openOverviewPage();
            discOverview.selectOverviewState(projectState);
            discOverview.assertOverviewStateNumber(projectState,
                    discOverviewProjects.getOverviewProjectNumber());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }

    }

    private OverviewProjectDetails prepareDataForCheckingProjectState(OverviewProjectStates state,
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

        openProjectDetailPage(overviewProject.getProjectInfo());
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
                if (state == OverviewProjectStates.RUNNING)
                    overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                else {
                    if (state == OverviewProjectStates.FAILED)
                        scheduleDetail.assertFailedExecution(executable);
                    else if (state == OverviewProjectStates.SUCCESSFUL)
                        scheduleDetail.assertSuccessfulExecution();
                    overviewSchedule.setExecutionDescription(scheduleDetail
                            .getExecutionDescription());
                    overviewSchedule.setLastExecutionTime(scheduleDetail.getLastExecutionTime());
                    overviewSchedule.setLastExecutionRunTime(scheduleDetail.getExecutionRuntime());
                }
            }
        }
        return overviewProject;
    }

    private void checkProjectNotAdmin(OverviewProjectStates projectState) throws ParseException,
            IOException, JSONException, ParseException {
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
            prepareDataForCheckingProjectState(projectState, overviewProject);

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


    private void checkDisableProject(OverviewProjectStates projectState) {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Disable Overview Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingProjectState(projectState, overviewProject);

        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.disableAction();
        checkFilteredOutProject(projectState, getWorkingProject());
        checkOtherStates(projectState, getWorkingProject());

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    private OverviewProjectDetails checkBulkActionsOnProject(OverviewProjectStates projectState,
            OverviewProjectDetails overviewProject) {
        prepareDataForCheckingProjectState(projectState, overviewProject);

        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(projectState);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(projectState);
        checkFilteredOutProject(projectState, getWorkingProject());

        return overviewProject;
    }

    private void checkDisableSchedule(OverviewProjectStates projectState) throws ParseException {
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
        prepareDataForCheckingProjectState(projectState, overviewProject);

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

    private void checkBulkActionsOnSchedule(OverviewProjectStates projectState)
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
        prepareDataForCheckingProjectState(projectState, overviewProject);

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

    private void cleanupProcessesAndProjects(boolean deleteProjects,
            List<ProjectInfo> additionalProjects) {
        openProjectDetailByUrl(testParams.getProjectId());
        projectDetailPage.deleteAllProcesses();
        if (deleteProjects)
            deleteProjects(additionalProjects);
    }
}
