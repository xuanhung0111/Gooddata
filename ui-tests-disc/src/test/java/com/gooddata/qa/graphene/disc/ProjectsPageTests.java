package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectsPageTests extends AbstractDISC {

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
            prepareDataForProjectsPageTest(ProjectStateFilters.FAILED, getWorkingProject(),
                    processName, Executables.FAILED_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.FAILED, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulProjectsFilterOptions() {
        try {
            String processName = "Check Successful Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SUCCESSFUL, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningProjectsFilterOptions() {
        try {
            String processName = "Check Running Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.RUNNING, getWorkingProject(),
                    processName, Executables.LONG_TIME_RUNNING_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.RUNNING, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledProjectsFilterOptions() throws JSONException, InterruptedException {
        List<ProjectInfo> additionalProjects =
                Arrays.asList(new ProjectInfo().setProjectName("Disc-test-scheduled-filter-option"));
        createMultipleProjects(additionalProjects);
        try {
            prepareDataForAdditionalProjects(additionalProjects);
            String processName = "Process for additional projects";
            prepareDataForProjectsPageTest(ProjectStateFilters.SCHEDULED, getWorkingProject(),
                    processName, Executables.LONG_TIME_RUNNING_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SCHEDULED, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkUnscheduledProjectsFilterOptions() {
        try {
            String processName = "Check Unscheduled Projects Filter Option";
            Executables executable = null;
            prepareDataForProjectsPageTest(ProjectStateFilters.UNSCHEDULED, getWorkingProject(),
                    processName, executable);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.UNSCHEDULED, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDisabledProjectsFilterOptions() {
        try {
            String processName = "Check Disabled Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.DISABLED, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.DISABLED, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
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
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulExecution() {
        try {
            String processName = "Check Last Successful Execution";
            prepareDataForProjectsPageTest(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
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
            cleanProcessesInProjectDetail(testParams.getProjectId());
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
            discProjectsList.assertProjectNotAdmin(getWorkingProject().getProjectName());
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.EDITOR);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsList.assertProjectNotAdmin(getWorkingProject().getProjectName());
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
        if (projectsNumber <= 20) {
            List<ProjectInfo> additionalProjects = new ArrayList<ProjectInfo>();
            for (int i = 0; i < 20 - projectsNumber + 1; i++) {
                additionalProjects.add(new ProjectInfo().setProjectName("Disc-test-paging-projects-page-" + i));
            }
            createMultipleProjects(additionalProjects);
        }
        
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
        List<ProjectInfo> additionalProjects =
                Arrays.asList(new ProjectInfo().setProjectName(unicodeProjectName));
        createMultipleProjects(additionalProjects);
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
}
