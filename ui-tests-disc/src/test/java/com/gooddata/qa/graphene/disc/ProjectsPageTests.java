package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.DISCProjectFilters;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.UserRoles;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectsPageTests extends AbstractSchedulesTests {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-projects-page";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkProjectFilterOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectFilterOptions();
        assertEquals(DISCProjectFilters.ALL.getOption(), discProjectsPage.getSelectedFilterOption()
                .getText());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkFailedProjectsFilterOption() throws JSONException, InterruptedException {
        try {
            String processName = "Check Failed Projects Filter Option";
            String graphName = "errorGraph.grf";
            prepareDataWithBasicPackage(DISCProjectFilters.FAILED, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.FAILED.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSuccessfulProjectsFilterOptions() throws JSONException, InterruptedException {
        try {
            String processName = "Check Successful Projects Filter Option";
            String graphName = "successfulGraph.grf";
            prepareDataWithBasicPackage(DISCProjectFilters.SUCCESSFUL, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.SUCCESSFUL.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkRunningProjectsFilterOptions() throws JSONException, InterruptedException {
        try {
            String processName = "Check Running Projects Filter Option";
            String graphName = "longTimeRunningGraph.grf";
            prepareDataWithBasicPackage(DISCProjectFilters.RUNNING, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.RUNNING.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkScheduledProjectsFilterOptions() throws JSONException, InterruptedException {
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
        Map<String, String> additionalProjects =
                createMultipleProjects("Disc-test-scheduled-filter-option", 1);
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        try {
            String processName = "Process for additional projects";
            String graphName = "longTimeRunningGraph.grf";
            for (Entry<String, String> project : additionalProjects.entrySet()) {
                prepareDataWithBasicPackage(DISCProjectFilters.SCHEDULED, project.getKey(),
                        project.getValue(), processName, graphName);
                openProjectDetailPage(project.getKey(), project.getValue());
                for (int i = 1; i < 7; i++) {
                    createScheduleForProcess(project.getKey(), project.getValue(), processName,
                            "longTimeRunningGraph.grf" + i, "/graph/longTimeRunningGraph.grf",
                            cronTime, null);
                    assertNewSchedule(processName, "longTimeRunningGraph.grf" + i,
                            "/graph/longTimeRunningGraph.grf", cronTime, null);
                    scheduleDetail.manualRun();
                }
            }
            prepareDataWithBasicPackage(DISCProjectFilters.SCHEDULED, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.SCHEDULED.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkUnscheduledProjectsFilterOptions() throws JSONException, InterruptedException {
        try {
            String processName = "Check Unscheduled Projects Filter Option";
            String graphName = null;
            prepareDataWithBasicPackage(DISCProjectFilters.UNSCHEDULED, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.UNSCHEDULED.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkDisabledProjectsFilterOptions() throws JSONException, InterruptedException {
        try {
            String processName = "Check Disabled Projects Filter Option";
            String graphName = "successfulGraph.grf";
            prepareDataWithBasicPackage(DISCProjectFilters.DISABLED, projectTitle,
                    testParams.getProjectId(), processName, graphName);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(DISCProjectFilters.DISABLED.getOption(),
                    getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkDataLoadingProcess() throws JSONException, InterruptedException {
        try {
            String processName1 = "Check Data Loading Processes 1";
            String processName2 = "Check Data Loading Processes 2";
            deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH, processName1,
                    Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
                            "successfulGraph.grf"), true);
            deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH, processName2,
                    Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
                            "successfulGraph.grf"), true);
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName1, null,
                    "/graph/longTimeRunningGraph.grf", cronTime, null);
            assertNewSchedule(processName1, "longTimeRunningGraph.grf",
                    "/graph/longTimeRunningGraph.grf", cronTime, null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName1, null,
                    "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule(processName1, "errorGraph.grf", "/graph/errorGraph.grf", cronTime,
                    null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName2, null,
                    "/graph/errorGraph.grf", cronTime, null);
            Thread.sleep(2000);
            assertNewSchedule(processName2, "errorGraph.grf", "/graph/errorGraph.grf", cronTime,
                    null);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsList.getRoot());
            discProjectsList.assertDataLoadingProcesses(2, 3, getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();

        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkLastSuccessfulExecution() throws JSONException, InterruptedException {
        try {
            String processName = "Check Last Successful Execution";
            String graphName = "successfulGraph.grf";
            prepareDataWithBasicPackage(DISCProjectFilters.SUCCESSFUL, projectTitle,
                    testParams.getProjectId(), processName, graphName);
            String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
            String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();
            Pair<String, List<String>> cronTime =
                    Pair.of(ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
            createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                    "/graph/errorGraph.grf", cronTime, null);
            assertNewSchedule(processName, "errorGraph.grf", "/graph/errorGraph.grf", cronTime,
                    null);
            scheduleDetail.manualRun();
            scheduleDetail.assertLastExecutionDetails(false, true, false,
                    "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsList.getRoot());
            System.out.println("Successful Execution Date: " + lastSuccessfulExecutionDate);
            discProjectsList.assertLastLoaded(lastSuccessfulExecutionDate,
                    lastSuccessfulExecutionTime.substring(14), getProjectsMap());
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteAllProcesses();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
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

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkPagingOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectsPagingOptions();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkPagingProjectsPage() throws JSONException, InterruptedException {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        Thread.sleep(5000);
        int projectsNumber =
                projectsPage.getProjectsElements().size()
                        + projectsPage.getDemoProjectsElements().size();
        if (projectsPage.getProjectsElements().size() <= 20)
            createMultipleProjects("Disc-test-paging-projects-page-", 20 - projectsNumber + 1);
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkPagingProjectsPage("20");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkEmptySearchResult() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkEmptySearchResult("no search result");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectByName() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectByName(projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectById() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectById(testParams.getProjectId(), projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectInSuccessfulState() throws InterruptedException, JSONException {
        String processName = "Check Search Project In Successful State";
        checkSearchProjectInSpecificState(DISCProjectFilters.SUCCESSFUL, projectTitle,
                testParams.getProjectId(), processName, "successfulGraph.grf");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectInFailedState() throws JSONException, InterruptedException {
        String processName = "Check Search Project In Failed State";
        checkSearchProjectInSpecificState(DISCProjectFilters.FAILED, projectTitle,
                testParams.getProjectId(), processName, "errorGraph.grf");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectInRunningState() throws JSONException, InterruptedException {
        String processName = "Check Search Project In Running State";
        checkSearchProjectInSpecificState(DISCProjectFilters.RUNNING, projectTitle,
                testParams.getProjectId(), processName, "longTimeRunningGraph.grf");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectInDisabledState() throws JSONException, InterruptedException {
        String processName = "Check Search Project In Failed State";
        checkSearchProjectInSpecificState(DISCProjectFilters.DISABLED, projectTitle,
                testParams.getProjectId(), processName, "successfulGraph.grf");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectInUnscheduledState() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectInSpecificState(DISCProjectFilters.UNSCHEDULED, projectTitle,
                testParams.getProjectId());
    }
    
    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchUnicodeProjectName() throws JSONException, InterruptedException {
        String unicodeProjectName = "Tiếng Việt ພາສາລາວ  résumé";
        Map<String, String> additionalProjects = createMultipleProjects(unicodeProjectName, 1);
        try {
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.searchProjectByUnicodeName(unicodeProjectName);
        } finally {
            deleteProjects(additionalProjects);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkDefaultSearchBox() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkDefaultSearchBox();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkDeleteSearchKey() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkDeleteSearchKey("no search result");
    }

    @Test(dependsOnGroups = {"projects-page"}, groups = {"tests"})
    public void test() throws JSONException {
        successfulTest = true;
    }

    private void prepareDataWithBasicPackage(DISCProjectFilters projectFilter, String projectName,
            String projectId, String processName, String graphName) throws JSONException,
            InterruptedException {
        openProjectDetailPage(projectName, projectId);
        deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
                processName,
                Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
                true);
        if (projectFilter.equals(DISCProjectFilters.UNSCHEDULED))
            return;

        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        createScheduleForProcess(projectName, projectId, processName, null, "/graph/" + graphName,
                cronTime, null);
        assertNewSchedule(processName, graphName, "/graph/" + graphName, cronTime, null);
        scheduleDetail.manualRun();
        if (projectFilter.equals(DISCProjectFilters.SCHEDULED))
            return;

        assertTrue(scheduleDetail.isInRunningState());
        if (projectFilter.equals(DISCProjectFilters.RUNNING))
            return;

        boolean isSuccessful = !projectFilter.equals(DISCProjectFilters.FAILED);
        scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
                + graphName, DISCProcessTypes.GRAPH, 5);

        if (!projectFilter.equals(DISCProjectFilters.DISABLED))
            return;
        scheduleDetail.disableSchedule();
    }

    private void checkSearchProjectInSpecificState(DISCProjectFilters projectFilter,
            String projectName, String projectId, String processName, String graphName)
            throws JSONException, InterruptedException {
        try {
            prepareDataWithBasicPackage(projectFilter, projectName, projectId, processName,
                    graphName);
            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.searchProjectInSpecificState(projectFilter, projectName, projectId);
        } finally {
            openProjectDetailByUrl(projectId);
            projectDetailPage.deleteAllProcesses();

        }
    }
}
