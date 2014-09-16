package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkProjectFilterOptions() {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsPage.getRoot());
		discProjectsPage.checkProjectFilterOptions();
		assertEquals(DISCProjectFilters.ALL.getOption(), discProjectsPage.getSelectedFilterOption()
				.getText());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkFailedProjectsFilterOption() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Failed Projects Filter Option", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Failed Projects Filter Option", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Failed Projects Filter Option", "errorGraph.grf", cronTime,
					null);
			scheduleDetail.manualRun();
			scheduleDetail.assertLastExecutionDetails(false, true, false,
					"Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.FAILED.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();

		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkSuccessfulProjectsFilterOptions() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Successful Projects Filter Option", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Successful Projects Filter Option", "/graph/successfulGraph.grf",
					cronTime, null);
			assertNewSchedule("Check Successful Projects Filter Option", "successfulGraph.grf",
					cronTime, null);
			scheduleDetail.manualRun();
			scheduleDetail.assertLastExecutionDetails(true, true, false,
					"Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.SUCCESSFUL.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();

		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkRunningProjectsFilterOptions() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Running Projects Filter Option", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Running Projects Filter Option", "/graph/longTimeRunningGraph.grf",
					cronTime, null);
			assertNewSchedule("Check Running Projects Filter Option", "longTimeRunningGraph.grf",
					cronTime, null);
			scheduleDetail.manualRun();
			assertTrue(scheduleDetail.isExecutionInRunningState());
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.RUNNING.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();

		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkScheduledProjectsFilterOptions() throws JSONException, InterruptedException {
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
		Map<String, String> addtionalProjects = createMultipleProjects("Disc-test-scheduled-filter-option", 1);
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		try {
			for (Entry<String, String> project : addtionalProjects.entrySet()) {
				openProjectDetailPage(project.getKey(), project.getValue());
				for (int i = 1; i < 10; i++) {
					deployInProjectDetailPage(project.getKey(), project.getValue(), "Basic",
							DISCProcessTypes.GRAPH, "Check Scheduled Projects Filter Option "
									+ i, Arrays.asList("errorGraph.grf",
									"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
				}
			}
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Scheduled Projects Filter Option",
					Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
							"successfulGraph.grf"), true);
			for (Entry<String, String> project : addtionalProjects.entrySet()) {
				openProjectDetailPage(project.getKey(), project.getValue());
				for (int i = 1; i < 10; i++) {
					createScheduleForProcess(project.getKey(), project.getValue(),
							"Check Scheduled Projects Filter Option " + i,
							"/graph/longTimeRunningGraph.grf", cronTime, null);
					assertNewSchedule("Check Scheduled Projects Filter Option " + i,
							"longTimeRunningGraph.grf", cronTime, null);
					scheduleDetail.manualRun();
				}
			}
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Scheduled Projects Filter Option", "/graph/longTimeRunningGraph.grf",
					cronTime, null);
			assertNewSchedule("Check Scheduled Projects Filter Option", "longTimeRunningGraph.grf",
					cronTime, null);
			scheduleDetail.manualRun();
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.SCHEDULED.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
			deleteProjects(addtionalProjects);
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkUnscheduledProjectsFilterOptions() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Unscheduled Projects Filter Option", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.UNSCHEDULED.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkDisabledProjectsFilterOptions() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Disabled Projects Filter Option", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Disabled Projects Filter Option", "/graph/longTimeRunningGraph.grf",
					cronTime, null);
			assertNewSchedule("Check Disabled Projects Filter Option", "longTimeRunningGraph.grf",
					cronTime, null);
			scheduleDetail.disableSchedule();
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsPage.getRoot());
			discProjectsPage.checkProjectFilter(DISCProjectFilters.DISABLED.getOption(),
					getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();

		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkDataLoadingProcess() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Data Loading Processes 1", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Data Loading Processes 2", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Data Loading Processes 1", "/graph/longTimeRunningGraph.grf", cronTime,
					null);
			assertNewSchedule("Check Data Loading Processes 1", "longTimeRunningGraph.grf",
					cronTime, null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Data Loading Processes 1", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Data Loading Processes 1", "errorGraph.grf", cronTime, null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Data Loading Processes 2", "/graph/errorGraph.grf", cronTime, null);
			Thread.sleep(2000);
			assertNewSchedule("Check Data Loading Processes 2", "errorGraph.grf", cronTime, null);
			openUrl(DISC_PROJECTS_PAGE_URL);
			waitForElementVisible(discProjectsList.getRoot());
			discProjectsList.assertDataLoadingProcesses(2, 3, getProjectsMap());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();

		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkLastSuccessfulExecution() throws JSONException, InterruptedException {
		try {
			deployInProjectsPage(getProjectsMap(), "Basic", DISCProcessTypes.GRAPH,
					"Check Last Successful Execution", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Last Successful Execution", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Check Last Successful Execution", "successfulGraph.grf", cronTime,
					null);
			scheduleDetail.manualRun();
			scheduleDetail.assertLastExecutionDetails(true, true, false,
					"Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
			String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
			String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Last Successful Execution", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Failed Projects Filter Option", "errorGraph.grf", cronTime,
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

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkProjectsNotAdmin() throws ParseException, IOException, JSONException, InterruptedException {
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

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkPagingOptions() {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsPage.getRoot());
		discProjectsPage.checkProjectsPagingOptions();
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "projects-page" })
	public void checkPagingProjectsPage() throws JSONException, InterruptedException {
		openUrl(PAGE_PROJECTS);
		waitForElementVisible(projectsPage.getRoot());
		Thread.sleep(5000);
		int projectsNumber = projectsPage.getProjectsElements().size()
				+ projectsPage.getDemoProjectsElements().size();
		if (projectsPage.getProjectsElements().size() <= 20) {
			createMultipleProjects("Disc-test-paging-projects-page-", 20 - projectsNumber + 1);
			projectsNumber = projectsNumber + 2;
		}
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsPage.getRoot());
		if (projectsNumber > 20)
			discProjectsPage.checkPagingProjectsPage("20", projectsNumber);
		deleteProjects(getProjectsMap());
	}

	@Test(dependsOnGroups = { "projects-page" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}

	protected Map<String, String> createMultipleProjects(String projectNamePrefix, int projectNumber)
			throws JSONException, InterruptedException {
		Map<String, String> additionalProjects = new HashMap<String, String>(1);
		for (int i = 0; i < projectNumber; i++) {
			openUrl(PAGE_GDC_PROJECTS);
			waitForElementVisible(gpProject.getRoot());
			additionalProjects.put(
					projectNamePrefix + String.valueOf(i),
					gpProject.createProject(projectNamePrefix + i, projectNamePrefix + i, null,
							testParams.getAuthorizationToken(), testParams.getDwhDriver(),
							projectCreateCheckIterations));
		}
		return additionalProjects;
	}

	protected void deleteProjects(Map<String, String> projectsToDelete) throws InterruptedException {
		for (Entry<String, String> projectToDelete : projectsToDelete.entrySet()) {
			if (projectToDelete.getValue() != testParams.getProjectId()) {
				deleteProject(projectToDelete.getValue());
				getProjectsMap().remove(projectToDelete.getKey());
			}
		}
	}
}