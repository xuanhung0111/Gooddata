package com.gooddata.qa.graphene.disc;

import java.util.Arrays;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectDetailTests extends AbstractSchedulesTests {

	private String PROJECT_EMPTY_STATE_TITLE = "You don’t have any deployed data loading processes.";
	private String PROJECT_EMPTY_STATE_MESSAGE = "How to deploy a process? Read Preparing a Data Loading Process article";
	private static final long expectedDownloadedProcessSize = 64000L;

	private String downloadFolder;

	@BeforeClass
	public void initProperties() {
		zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
		downloadFolder = testParams.loadProperty("browserDownloadFolder") + testParams.getFolderSeparator();
		projectTitle = "disc-project-detail-test";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkProjectTitle() {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		waitForElementVisible(projectDetailPage.getRoot());
		assertEquals(projectTitle, projectDetailPage.getDisplayedProjectTitle());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkGoToDashboardsLink() {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.goToDashboards();
		waitForDashboardPageLoaded(browser);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkEmptyProjectState() {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		waitForElementVisible(projectDetailPage.getRoot());
		assertEquals(PROJECT_EMPTY_STATE_TITLE,
				projectDetailPage.getProjectEmptyStateTitle());
		assertEquals(PROJECT_EMPTY_STATE_MESSAGE,
				projectDetailPage.getProjectEmptyStateMessage());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void downloadProcess() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Download Process Test", Arrays.asList("errorGraph.grf",
									"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			System.out.println("Download folder: " + downloadFolder);
			projectDetailPage.checkDownloadProcess("Download Process Test", downloadFolder,
					testParams.getProjectId(), expectedDownloadedProcessSize);
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkSortedProcesses() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-Z", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-B", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-P", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			projectDetailPage.checkSortedProcesses();
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkSortedProcessesAfterRedeploy() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-Z", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-B", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-P", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			redeployProcess(projectTitle, testParams.getProjectId(), "Process-B", "Basic",
					"Process-R", DISCProcessTypes.GRAPH, Arrays.asList("errorGraph.grf",
									"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			projectDetailPage.checkSortedProcesses();
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkDeleteProcess() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-Z", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-B", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-P", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			projectDetailPage.deleteProcess("Process-B");
			assertFalse(projectDetailPage.assertIsExistingProcess("Process-B"));
			projectDetailPage.checkSortedProcesses();
		} finally {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkProcessDeleteDialog() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			projectDetailPage.checkDeleteProcessDialog("Process-A");
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkCancelProcessDeleteDialog() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			projectDetailPage.checkCancelDeleteProcess("Process-A");
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-detail" })
	public void checkExecutableScheduleNumber() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic", DISCProcessTypes.GRAPH,
					"Process-A", Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
									"successfulGraph.grf"), true);
			createScheduleForProcess(projectTitle, testParams.getProjectId(), "Process-A", "/graph/successfulGraph.grf",
					null, null);
			projectDetailPage.checkExecutableScheduleNumber("Process-A", "errorGraph.grf", 0);
			projectDetailPage.checkExecutableScheduleNumber("Process-A",
					"longTimeRunningGraph.grf", 0);
			projectDetailPage.checkExecutableScheduleNumber("Process-A", "successfulGraph.grf", 1);
		} finally {
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnGroups = { "project-detail" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}
}
