package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.List;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectsPageTests extends AbstractSchedulesTests {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-projects-page";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectFilterOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectFilterOptions();
        assertEquals(ProjectStateFilters.ALL.getOption(), discProjectsPage
                .getSelectedFilterOption().getText());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFailedProjectsFilterOption() {
        try {
            String processName = "Check Failed Projects Filter Option";
            prepareDataWithBasicPackage(ProjectStateFilters.FAILED, getWorkingProject(),
                    processName, Executables.FAILED_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.FAILED, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulProjectsFilterOptions() {
        try {
            String processName = "Check Successful Projects Filter Option";
            prepareDataWithBasicPackage(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SUCCESSFUL, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningProjectsFilterOptions() {
        try {
            String processName = "Check Running Projects Filter Option";
            prepareDataWithBasicPackage(ProjectStateFilters.RUNNING, getWorkingProject(),
                    processName, Executables.LONG_TIME_RUNNING_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.RUNNING, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledProjectsFilterOptions() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                createMultipleProjects("Disc-test-scheduled-filter-option", 1);
        openProjectDetailPage(getWorkingProject());
        try {
            String processName = "Process for additional projects";
            for (ProjectInfo project : additionalProjects) {
                prepareDataWithBasicPackage(ProjectStateFilters.SCHEDULED, project, processName,
                        Executables.LONG_TIME_RUNNING_GRAPH);
                openProjectDetailPage(project);
                for (int i = 1; i < 7; i++) {
                    ScheduleBuilder scheduleBuilder =
                            new ScheduleBuilder()
                                    .setProcessName(processName)
                                    .setScheduleName(
                                            Executables.LONG_TIME_RUNNING_GRAPH.getExecutableName()
                                                    + i)
                                    .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH);
                    createAndAssertSchedule(scheduleBuilder);
                    scheduleDetail.manualRun();
                }
            }
            prepareDataWithBasicPackage(ProjectStateFilters.SCHEDULED, getWorkingProject(),
                    processName, Executables.LONG_TIME_RUNNING_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SCHEDULED, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkUnscheduledProjectsFilterOptions() {
        try {
            String processName = "Check Unscheduled Projects Filter Option";
            Executables executable = null;
            prepareDataWithBasicPackage(ProjectStateFilters.UNSCHEDULED, getWorkingProject(),
                    processName, executable);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.UNSCHEDULED, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDisabledProjectsFilterOptions() {
        try {
            String processName = "Check Disabled Projects Filter Option";
            prepareDataWithBasicPackage(ProjectStateFilters.DISABLED, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.DISABLED, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataLoadingProcess() {
        try {
            String processName1 = "Check Data Loading Processes 1";
            String processName2 = "Check Data Loading Processes 2";
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, processName1);
            deployInProjectDetailPage(DeployPackages.BASIC, processName2);

            ScheduleBuilder scheduleBuilder1 =
                    new ScheduleBuilder().setProcessName(processName1).setExecutable(
                            Executables.LONG_TIME_RUNNING_GRAPH);
            createAndAssertSchedule(scheduleBuilder1);
            ScheduleBuilder scheduleBuilder2 =
                    new ScheduleBuilder().setProcessName(processName1).setExecutable(
                            Executables.FAILED_GRAPH);
            createAndAssertSchedule(scheduleBuilder2);
            ScheduleBuilder scheduleBuilder3 =
                    new ScheduleBuilder().setProcessName(processName2).setExecutable(
                            Executables.FAILED_GRAPH);
            createAndAssertSchedule(scheduleBuilder3);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsList.getRoot());
            discProjectsList.assertDataLoadingProcesses(2, 3, getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulExecution() {
        try {
            String processName = "Check Last Successful Execution";
            prepareDataWithBasicPackage(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);
            String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
            String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(
                            Executables.FAILED_GRAPH);
            createAndAssertSchedule(scheduleBuilder);
            scheduleDetail.manualRun();
            scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsList.getRoot());
            System.out.println("Successful Execution Date: " + lastSuccessfulExecutionDate);
            discProjectsList.assertLastLoaded(lastSuccessfulExecutionDate,
                    lastSuccessfulExecutionTime.substring(14), getProjects());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsNotAdmin() throws ParseException, IOException, JSONException,
            InterruptedException {
        try {
            addUsersWithOtherRolesToProject();
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.VIEWER);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsList.assertProjectNotAdmin(projectTitle, testParams.getProjectId());
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.EDITOR);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsList.assertProjectNotAdmin(projectTitle, testParams.getProjectId());
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectsPagingOptions();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingProjectsPage() throws JSONException, InterruptedException {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        waitForCollectionIsNotEmpty(projectsPage.getProjectsElements());
        int projectsNumber =
                projectsPage.getProjectsElements().size()
                        + projectsPage.getDemoProjectsElements().size();
        if (projectsPage.getProjectsElements().size() <= 20)
            createMultipleProjects("Disc-test-paging-projects-page-", 20 - projectsNumber + 1);
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkPagingProjectsPage("20");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySearchResult() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkEmptySearchResult("no search result");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectByName() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectByName(projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectById() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectById(getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInSuccessfulState() throws InterruptedException, JSONException {
        String processName = "Check Search Project In Successful State";
        checkSearchProjectInSpecificState(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
                processName, Executables.SUCCESSFUL_GRAPH);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInFailedState() {
        String processName = "Check Search Project In Failed State";
        checkSearchProjectInSpecificState(ProjectStateFilters.FAILED, getWorkingProject(),
                processName, Executables.FAILED_GRAPH);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInRunningState() {
        String processName = "Check Search Project In Running State";
        checkSearchProjectInSpecificState(ProjectStateFilters.RUNNING, getWorkingProject(),
                processName, Executables.LONG_TIME_RUNNING_GRAPH);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInDisabledState() {
        String processName = "Check Search Project In Failed State";
        checkSearchProjectInSpecificState(ProjectStateFilters.DISABLED, getWorkingProject(),
                processName, Executables.SUCCESSFUL_GRAPH);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInUnscheduledState() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectInSpecificState(ProjectStateFilters.UNSCHEDULED,
                getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchUnicodeProjectName() throws JSONException, InterruptedException {
        String unicodeProjectName = "Tiếng Việt ພາສາລາວ  résumé";
        List<ProjectInfo> additionalProjects = createMultipleProjects(unicodeProjectName, 1);
        try {
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.searchProjectByUnicodeName(unicodeProjectName);
        } finally {
            deleteProjects(additionalProjects);
        }

    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultSearchBox() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkDefaultSearchBox();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteSearchKey() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkDeleteSearchKey("no search result");
    }

    private void prepareDataWithBasicPackage(ProjectStateFilters projectFilter,
            ProjectInfo workingProject, String processName, Executables executable) {
        openProjectDetailPage(workingProject);
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

    private void checkSearchProjectInSpecificState(ProjectStateFilters projectFilter,
            ProjectInfo project, String processName, Executables executable) {
        try {
            prepareDataWithBasicPackage(projectFilter, project, processName, executable);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.searchProjectInSpecificState(projectFilter, project);
        } finally {
            openProjectDetailByUrl(project.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }
}
