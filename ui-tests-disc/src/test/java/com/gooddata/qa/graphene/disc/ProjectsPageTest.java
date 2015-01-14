package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterMethod;
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

public class ProjectsPageTest extends AbstractOverviewProjectsTest {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-projects-page";
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
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
        checkProjectsFilter(ProjectStateFilters.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledProjectsFilterOptions() {
        ProjectInfo projectInfo =
                new ProjectInfo().setProjectName("Disc-test-scheduled-filter-option");
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            prepareDataForAdditionalProjects(additionalProjects);
            prepareDataForProjectsPageTest(ProjectStateFilters.SCHEDULED, getWorkingProject());

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SCHEDULED, getProjects());
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkUnscheduledProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.UNSCHEDULED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDisabledProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.DISABLED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataLoadingProcess() {
        String processName1 = "Check Data Loading Processes 1";
        String processName2 = "Check Data Loading Processes 2";
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, processName1);
        deployInProjectDetailPage(DeployPackages.BASIC, processName2);

        ScheduleBuilder scheduleBuilder1 =
                new ScheduleBuilder().setProcessName(processName1).setExecutable(
                        Executables.LONG_TIME_RUNNING_GRAPH);
        createSchedule(scheduleBuilder1);
        ScheduleBuilder scheduleBuilder2 =
                new ScheduleBuilder().setProcessName(processName1).setExecutable(
                        Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder2);
        ScheduleBuilder scheduleBuilder3 =
                new ScheduleBuilder().setProcessName(processName2).setExecutable(
                        Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder3);

        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertDataLoadingProcesses(2, 3, getProjects());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulExecution() {
        String processName = "Check Last Successful Execution";
        openProjectDetailByUrl(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        ScheduleBuilder scheduleBuilder1 =
                new ScheduleBuilder().setProcessName(processName).setExecutable(
                        Executables.SUCCESSFUL_GRAPH);
        createSchedule(scheduleBuilder1);
        scheduleDetail.manualRun();
        scheduleDetail.assertSuccessfulExecution();
        String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
        String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();
        scheduleDetail.clickOnCloseScheduleButton();

        ScheduleBuilder scheduleBuilder2 =
                new ScheduleBuilder().setProcessName(processName).setExecutable(
                        Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder2);
        scheduleDetail.manualRun();
        scheduleDetail.assertFailedExecution(scheduleBuilder2.getExecutable());

        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsList.getRoot());
        System.out.println("Successful Execution Date: " + lastSuccessfulExecutionDate);
        discProjectsList.assertLastLoaded(lastSuccessfulExecutionDate,
                lastSuccessfulExecutionTime.substring(14), getProjects());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsNotAdmin() {
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
        } catch (ParseException e) {
            System.out.println("There is problem during adding user to project: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("There is problem during adding user to project: ");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("There is problem during adding user to project or signIn: ");
            e.printStackTrace();
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            try {
                signIn(false, UserRoles.ADMIN);
            } catch (JSONException e) {
                System.out.println("There is problem when signIn: ");
                e.printStackTrace();
            }
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectsPagingOptions();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingProjectsPage() {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        waitForCollectionIsNotEmpty(projectsPage.getProjectsElements());
        int projectsNumber =
                projectsPage.getProjectsElements().size()
                        + projectsPage.getDemoProjectsElements().size();
        if (projectsNumber <= 20) {
            List<ProjectInfo> additionalProjects = new ArrayList<ProjectInfo>();
            for (int i = 0; i < 20 - projectsNumber + 1; i++) {
                ProjectInfo projectInfo =
                        new ProjectInfo().setProjectName("Disc-test-paging-projects-page-" + i);
                additionalProjects.add(projectInfo);
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
        checkSearchWorkingProjectByName();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectById() {
        checkSearchWorkingProjectById();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInSuccessfulState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInFailedState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInRunningState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInDisabledState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.DISABLED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInUnscheduledState() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectInSpecificState(ProjectStateFilters.UNSCHEDULED,
                getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchUnicodeProjectName() {
        String unicodeProjectName = "Tiếng Việt ພາສາລາວ  résumé";
        ProjectInfo projectInfo = new ProjectInfo().setProjectName(unicodeProjectName);
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
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
