package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.*;

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
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.google.common.collect.Lists;

public class ProjectsPageTest extends AbstractOverviewProjectsTest {

    private static final String DISC_HEADER_TITLE = "Data Integration Console";
    private static final String PROJECTS_BUTTON_TITLE = "Projects";
    private static final String OVERVIEW_BUTTON_TITLE = "Overview";
    private static final int MINIMUM_NUMBER_OF_PROJECTS = 25;

    @BeforeClass
    public void initProperties() {
        // Created time is used to identify the working project in case user has no admin role
        projectTitle = "Disc-test-projects-page-" + System.currentTimeMillis();
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectFilterOptions() {
        initDISCProjectsPage();
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

            initDISCProjectsPage();
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

        initDISCProjectsPage();
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertDataLoadingProcesses(2, 3, getProjects());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulExecution() {
        String processName = "Check Last Successful Execution";
        openProjectDetailByUrl(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        initDISCProjectsPage();
        waitForElementVisible(discProjectsList.getRoot());
        assertTrue(discProjectsList.getLastSuccessfulExecutionInfo(getWorkingProject()).isEmpty());

        ScheduleBuilder scheduleBuilder1 =
                new ScheduleBuilder().setProcessName(processName).setExecutable(
                        Executables.SUCCESSFUL_GRAPH);
        openProjectDetailByUrl(testParams.getProjectId());
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

        initDISCProjectsPage();
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertLastLoaded(lastSuccessfulExecutionDate,
                lastSuccessfulExecutionTime.substring(14), getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulAutoExecution() {
        String processName = "Check Last Successful Auto Execution";
        openProjectDetailByUrl(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(processName)
                        .setExecutable(Executables.SUCCESSFUL_GRAPH)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
        createSchedule(scheduleBuilder);

        scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
        scheduleDetail.assertSuccessfulExecution();
        String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
        String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();

        initDISCProjectsPage();
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertLastLoaded(lastSuccessfulExecutionDate,
                lastSuccessfulExecutionTime.substring(14), getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsNotAdmin() {
        try {
            addUsersWithOtherRolesToProject();
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.VIEWER);
            initDISCProjectsPage();
            discProjectsList.assertProjectNotAdmin(getWorkingProject().getProjectName());
            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.EDITOR);
            initDISCProjectsPage();
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
        initDISCProjectsPage();
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
        List<ProjectInfo> additionalProjects = new ArrayList<ProjectInfo>();
        if (projectsNumber <= MINIMUM_NUMBER_OF_PROJECTS) {
            for (int i = 0; i < 25 - projectsNumber + 1; i++) {
                ProjectInfo projectInfo =
                        new ProjectInfo().setProjectName("Disc-test-paging-projects-page-" + i);
                additionalProjects.add(projectInfo);
            }
            createMultipleProjects(additionalProjects);
        }

        try {
            initDISCProjectsPage();
            discProjectsPage.checkPagingProjectsPage("20");
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySearchResult() {
        initDISCProjectsPage();
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
        initDISCProjectsPage();
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
            initDISCProjectsPage();
            discProjectsPage.searchProjectByUnicodeName(unicodeProjectName);
        } finally {
            deleteProjects(additionalProjects);
        }

    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultSearchBox() {
        initDISCProjectsPage();
        discProjectsPage.checkDefaultSearchBox();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteSearchKey() {
        initDISCProjectsPage();
        discProjectsPage.checkDeleteSearchKey("no search result");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectListAfterLeaveAProject() {
        List<ProjectInfo> additionalProjects = Lists.newArrayList();
        String secondAdmin = testParams.getEditorUser();
        String secondAdminPassword = testParams.getEditorPassword();
        String secondAdminUri = testParams.getEditorProfileUri();
        try {
            addUserToProject(secondAdminUri, UserRoles.ADMIN);
            logout();
            signInAtUI(secondAdmin, secondAdminPassword);

            additionalProjects.add(new ProjectInfo().setProjectName("Additional Project"));
            createMultipleProjects(additionalProjects);

            initProjectsAndUsersPage();
            projectAndUsersPage.leaveProject();
            waitForProjectsPageLoaded(browser);

            initDISCProjectsPage();
            waitForElementVisible(discProjectsList.getRoot());
            for (ProjectInfo project : additionalProjects) {
                assertNotNull(discProjectsList.selectProjectWithAdminRole(project));
            }
            assertNull(discProjectsList.selectProjectWithAdminRole(getWorkingProject()));
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when adding user to project!", e);
        } finally {
            deleteProjects(additionalProjects);
            logout();
            signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsPageHeader() {
        initDISCProjectsPage();
        waitForFragmentVisible(discNavigation);
        assertEquals(discNavigation.getHeaderTitle(), DISC_HEADER_TITLE);
        assertEquals(discNavigation.getOverviewButtonTitle(), OVERVIEW_BUTTON_TITLE);
        assertEquals(discNavigation.getProjectsButtonTitle(), PROJECTS_BUTTON_TITLE);
        discNavigation.clickOnOverviewButton();
        waitForFragmentVisible(discOverview);
        discNavigation.clickOnProjectsButton();
        waitForFragmentVisible(discProjectsPage);
    }
}
