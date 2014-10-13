package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class SchedulesTests extends AbstractSchedulesTests {

	private final static String EXECUTION_HISTORY_EMPTY_STATE_MESSAGE = "No history available. This schedule has not been run yet.";

	@BeforeClass
	public void initProperties() {
		zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
		projectTitle = "Disc-test-schedule";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithCustomInput() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Create Schedule with Custom Input",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param", Arrays.asList("", "value"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Create Schedule with Custom Input", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Create Schedule with Custom Input", "DWHS2.grf", cronTime,
					parameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleForSpecificExecutable() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Create Schedule for Specific Executable",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			projectDetailPage.getExecutableTabByProcessName(
					"Create Schedule for Specific Executable").click();
			waitForElementVisible(projectDetailPage.getExecutableScheduleLink("DWHS2.grf")).click();
			waitForElementVisible(scheduleForm.getRoot());
			scheduleForm.createNewSchedule(null, null, null, null, true);
			waitForElementPresent(scheduleDetail.getRoot());
			scheduleDetail.clickOnCloseScheduleButton();
			waitForElementVisible(schedulesTable.getRoot());
			assertNewSchedule("Create Schedule for Specific Executable", "DWHS2.grf", cronTime,
					null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleFromSchedulesList() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Create Schedule from Schedule List",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			projectDetailPage.getNewScheduleLinkInSchedulesList(
					"Create Schedule from Schedule List").click();
			waitForElementVisible(scheduleForm.getRoot());
			scheduleForm.createNewSchedule(null, null, null, null, true);
			waitForElementPresent(scheduleDetail.getRoot());
			scheduleDetail.clickOnCloseScheduleButton();
			waitForElementNotPresent(scheduleDetail.getRoot());
			assertNewSchedule("Create Schedule from Schedule List", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithEveryWeekCronTime() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Edit Cron Time of Schedule",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYWEEK.getCronTime(),
					Arrays.asList("15", "10",
							ScheduleCronTimes.CRON_EVERYWEEK.getDaysInWeek().get(1)));
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Edit Cron Time of Schedule", null, cronTime, null);
			assertNewSchedule("Edit Cron Time of Schedule", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithEveryDayCronTime() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Schedule every day",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("30", "10"));
			createScheduleForProcess(projectTitle, testParams.getProjectId(), "Schedule every day",
					null, cronTime, null);
			assertNewSchedule("Schedule every day", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithCronExpression() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Schedule with cron expression",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EXPRESSION.getCronTime(), Arrays.asList("*/20 * * * *"));
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Schedule with cron expression", null, cronTime, null);
			assertNewSchedule("Schedule with cron expression", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkManualExecution() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Manual Execution", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Manual Execution", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Check Manual Execution", "successfulGraph.grf", cronTime, null);
			scheduleDetail.manualRun();
			scheduleDetail.assertLastExecutionDetails(true, true, false,
					"Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkStopManualExecution() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Stop Manual Execution", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Stop Manual Execution", "/graph/longTimeRunningGraph.grf", cronTime,
					null);
			assertNewSchedule("Check Stop Manual Execution", "longTimeRunningGraph.grf", cronTime,
					null);
			scheduleDetail.manualRun();
			Thread.sleep(5000);
			scheduleDetail.manualStop();
			scheduleDetail.assertLastExecutionDetails(false, true, true,
					"Basic/graph/longTimeRunningGraph.grf", DISCProcessTypes.GRAPH, 5);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void changeExecutableOfSchedule() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Change Executable of Schedule", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Change Executable of Schedule", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Change Executable of Schedule", "successfulGraph.grf", cronTime,
					null);
			scheduleDetail.changeExecutable("/graph/errorGraph.grf", true);
			assertNewSchedule("Change Executable of Schedule", "errorGraph.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void deleteSchedule() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
				DISCProcessTypes.GRAPH, "Delete Schedule",
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
		createScheduleForProcess(projectTitle, testParams.getProjectId(), "Delete Schedule",
				"/graph/successfulGraph.grf", cronTime, null);
		assertNewSchedule("Delete Schedule", "successfulGraph.grf", cronTime, null);
		scheduleDetail.deleteSchedule(true);
		waitForElementVisible(projectDetailPage.getRoot());
		waitForElementVisible(projectDetailPage.getScheduleTabByProcessName("Delete Schedule"))
				.click();
		waitForElementVisible(projectDetailPage.checkEmptySchedulesList("Delete Schedule"));
		Assert.assertTrue(projectDetailPage.checkEmptySchedulesList("Delete Schedule")
				.isDisplayed());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void changeScheduleCronTime() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Change Cron Time of Schedule", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Change Cron Time of Schedule", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf", cronTime, null);
			Pair<String, List<String>> newCronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			scheduleDetail.changeCronTime(newCronTime, true);
			assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf", newCronTime,
					null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void editScheduleParameters() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Edit schedule parameters",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("param 2", Arrays.asList("", "value 2"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Edit schedule parameters", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Edit schedule parameters", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1 new name", Arrays.asList("", "value 1 new"));
			changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
			changedParameters.put("secure param new name",
					Arrays.asList("secure", "secure value new"));
			scheduleDetail.editScheduleParameters(changedParameters, false, true);
			assertNewSchedule("Edit schedule parameters", "DWHS2.grf", cronTime, changedParameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void addNewParametersForSchedule() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Add New Parameters for Schedule",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Add New Parameters for Schedule", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> newParameters = new LinkedHashMap<String, List<String>>();
			newParameters.put("param 2", Arrays.asList("", "value 2"));
			newParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1", Arrays.asList("", "value 1"));
			changedParameters.put("param 2", Arrays.asList("", "value 2"));
			changedParameters.put("secure param", Arrays.asList("secure", "secure value"));
			changedParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
			scheduleDetail.editScheduleParameters(newParameters, true, true);
			assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", cronTime,
					changedParameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithIncorrectCron() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "Create Schedule With Error Cron",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		Pair<String, List<String>> incorrectCronTime = Pair.of(
				ScheduleCronTimes.CRON_EXPRESSION.getCronTime(), Arrays.asList("* * *"));
		projectDetailPage.clickOnNewScheduleButton();
		waitForElementVisible(scheduleForm.getRoot());
		scheduleForm.checkScheduleWithIncorrectCron(incorrectCronTime,
				scheduleForm.getConfirmScheduleButton());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void editScheduleWithIncorrectCron() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Edit Schedule With Error Cron",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Edit Schedule With Error Cron", "/graph/DWHS2.grf", cronTime, null);
			assertNewSchedule("Edit Schedule With Error Cron", "DWHS2.grf", cronTime, null);
			Pair<String, List<String>> incorrectCronTime = Pair.of("cron expression",
					Arrays.asList("* * *"));
			scheduleDetail.checkScheduleWithIncorrectCron(incorrectCronTime,
					scheduleDetail.getSaveChangedCronTimeButton());
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkBrokenSchedule() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Check Broken Schedule",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Broken Schedule", null, null, null);
			assertNewSchedule("Check Broken Schedule", "DWHS1.grf", cronTime, null);
			redeployProcess(projectTitle, testParams.getProjectId(), "Check Broken Schedule",
					"Basic", "Redeployed Process", DISCProcessTypes.GRAPH, Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			projectDetailPage.checkBrokenScheduleSection("Redeployed Process");
			assertBrokenSchedule("Check Broken Schedule", "DWHS1.grf", cronTime, null);
			scheduleDetail.checkBrokenSchedule("DWHS1.grf", "/graph/errorGraph.grf");
			assertNewSchedule("Check Broken Schedule", "errorGraph.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkDeleteScheduleParams() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Delete Schedule Parameter",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("param 2", Arrays.asList("", "value 2"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Delete Schedule Parameter", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Delete Schedule Parameter", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1 new name", null);
			changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
			changedParameters.put("secure param new name",
					Arrays.asList("secure", "secure value new"));
			scheduleDetail.editScheduleParameters(changedParameters, false, true);
			changedParameters.remove("param 1 new name");
			assertNewSchedule("Delete Schedule Parameter", "DWHS2.grf", cronTime, changedParameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelDeleteScheduleParams() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Cancel Delete Schedule Parameter",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("param 2", Arrays.asList("", "value 2"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Cancel Delete Schedule Parameter", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Cancel Delete Schedule Parameter", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1", null);
			changedParameters.put("param 2", Arrays.asList("", "value 2"));
			changedParameters.put("secure param", Arrays.asList("secure", "secure value"));
			scheduleDetail.editScheduleParameters(changedParameters, false, false);
			assertNewSchedule("Cancel Delete Schedule Parameter", "DWHS2.grf", cronTime, parameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkIncorrectRetryDelay() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Incorrect Retry Schedule", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Incorrect Retry Schedule", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Incorrect Retry Schedule", "errorGraph.grf", cronTime, null);
			scheduleDetail.addRetryDelay("5", true, false);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelCreateSchedule() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "Cancel Create Schedule from Schedule List",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		projectDetailPage.clickOnNewScheduleButton();
		waitForElementVisible(scheduleForm.getRoot());
		scheduleForm.createNewSchedule(null, null, null, null, false);
		waitForElementNotPresent(scheduleForm.getRoot());
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.assertProcessInList("Cancel Create Schedule from Schedule List",
				DISCProcessTypes.GRAPH, Arrays.asList("DWHS1.grf", "DWHS2.grf"));
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelChangeScheduleExecutable() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Cancel Change Executable", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Cancel Change Executable", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Cancel Change Executable", "successfulGraph.grf", cronTime, null);
			scheduleDetail.changeExecutable("/graph/errorGraph.grf", false);
			assertNewSchedule("Cancel Change Executable", "successfulGraph.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelChangeScheduleCronTime() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Cancel Change Cron Time of Schedule", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Cancel Change Cron Time of Schedule", "/graph/successfulGraph.grf", cronTime,
					null);
			assertNewSchedule("Cancel Change Cron Time of Schedule", "successfulGraph.grf",
					cronTime, null);
			Pair<String, List<String>> newCronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			scheduleDetail.changeCronTime(newCronTime, false);
			assertNewSchedule("Cancel Change Cron Time of Schedule", "successfulGraph.grf",
					cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelAddRetryDelay() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Retry Schedule", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Retry Schedule", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Retry Schedule", "errorGraph.grf", cronTime, null);
			scheduleDetail.addRetryDelay("15", false, true);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelEditScheduleParams() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
					DISCProcessTypes.GRAPH, "Cancel Edit schedule parameters",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("param 2", Arrays.asList("", "value 2"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Cancel Edit schedule parameters", "/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Cancel Edit schedule parameters", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1 new name", Arrays.asList("", "value 1 new"));
			changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
			changedParameters.put("secure param new name",
					Arrays.asList("secure", "secure value new"));
			scheduleDetail.editScheduleParameters(changedParameters, false, false);
			assertNewSchedule("Cancel Edit schedule parameters", "DWHS2.grf", cronTime, parameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkCancelDeleteSchedule() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Cancel Delete Schedule", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Cancel Delete Schedule", "/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Cancel Delete Schedule", "successfulGraph.grf", cronTime, null);
			scheduleDetail.deleteSchedule(false);
			waitForElementVisible(scheduleDetail.getRoot());
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkRemoveRetry() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Retry Schedule", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Retry Schedule", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Retry Schedule", "errorGraph.grf", cronTime, null);
			scheduleDetail.addRetryDelay("15", true, true);
			scheduleDetail.removeRetryDelay(false);
			scheduleDetail.removeRetryDelay(true);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkExecutionHistoryEmptyState() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, "Check Execution History Empty State", Arrays.asList(
							"errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Execution History Empty State", "/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule("Check Execution History Empty State", "errorGraph.grf", cronTime,
					null);
			assertNotNull(scheduleDetail.getExecutionHistoryEmptyState());
			assertEquals(EXECUTION_HISTORY_EMPTY_STATE_MESSAGE, scheduleDetail
					.getExecutionHistoryEmptyState().getText());
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkScheduleExecutionState() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			String processName = "Check Schedule Execution State";
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
					"/graph/errorGraph.grf", cronTime, null);
			assertNewSchedule(processName, "errorGraph.grf", cronTime, null);
			scheduleDetail.manualRun();
			assertTrue(scheduleDetail.isInScheduledState());
			assertTrue(scheduleDetail.isInRunningState());
			scheduleDetail.assertLastExecutionDetails(false, true, false,
					"Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH, 5);
			createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
					"/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule(processName, "successfulGraph.grf", cronTime, null);
			scheduleDetail.manualRun();
			assertTrue(scheduleDetail.isInScheduledState());
			assertTrue(scheduleDetail.isInRunningState());
			scheduleDetail.assertLastExecutionDetails(true, true, false,
					"Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkSuccessfulExecutionGroup() throws JSONException, InterruptedException {
		try {
			openProjectDetailPage(projectTitle, testParams.getProjectId());
			String processName = "Check Schedule Execution State";
			deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
					DISCProcessTypes.GRAPH, processName, Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(), processName,
					"/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule(processName, "successfulGraph.grf", cronTime, null);
			scheduleDetail.repeatManualRun(3, "/graph/successfulGraph.grf", DISCProcessTypes.GRAPH,
					true);
			scheduleDetail.checkOkExecutionGroup(3, 0);

		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnGroups = { "schedule" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}
}
