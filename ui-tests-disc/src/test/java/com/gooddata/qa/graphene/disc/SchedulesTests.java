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

public class SchedulesTests extends AbstractSchedulesTests {

	@BeforeClass
	public void initProperties() {
		zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
		projectTitle = "disc-schedule-test";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithCustomInput() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Create Schedule with Custom Input", Arrays.asList("DWHS1.grf", "DWHS2.grf"),
					true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param", Arrays.asList("", "value"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Create Schedule with Custom Input",
					"/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Create Schedule with Custom Input", "DWHS2.grf", cronTime,
					parameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleForSpecificExecutable() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Create Schedule for Specific Executable",
					Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			openProjectDetailPage(projectTitle);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.getExecutableTabByProcessName(
					"Create Schedule for Specific Executable").click();
			waitForElementVisible(projectDetailPage.getExecutableScheduleLink("DWHS2.grf")).click();
			waitForElementVisible(scheduleForm.getRoot());
			scheduleForm.createNewSchedule(null, null, null, null);
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
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Create Schedule from Schedule List", Arrays.asList("DWHS1.grf", "DWHS2.grf"),
					true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			openProjectDetailPage(projectTitle);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.getNewScheduleLinkInSchedulesList(
					"Create Schedule from Schedule List").click();
			waitForElementVisible(scheduleForm.getRoot());
			scheduleForm.createNewSchedule(null, null, null, null);
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
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Edit Cron Time of Schedule", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYWEEK.getCronTime(),
					Arrays.asList("15", "10",
							ScheduleCronTimes.CRON_EVERYWEEK.getDaysInWeek().get(1)));
			createScheduleForProcess(projectTitle, "Edit Cron Time of Schedule", null, cronTime,
					null);
			assertNewSchedule("Edit Cron Time of Schedule", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithEveryDayCronTime() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Schedule every day", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("30", "10"));
			createScheduleForProcess(projectTitle, "Schedule every day", null, cronTime, null);
			assertNewSchedule("Schedule every day", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithCronExpression() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Schedule with cron expression", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EXPRESSION.getCronTime(), Arrays.asList("*/20 * * * *"));
			createScheduleForProcess(projectTitle, "Schedule with cron expression", null, cronTime,
					null);
			assertNewSchedule("Schedule with cron expression", "DWHS1.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkManualExecution() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH,
					"Check Manual Execution", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Check Manual Execution",
					"/graph/successfulGraph.grf", cronTime, null);
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
			deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH,
					"Check Stop Manual Execution", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Check Stop Manual Execution",
					"/graph/longTimeRunningGraph.grf", cronTime, null);
			assertNewSchedule("Check Stop Manual Execution", "longTimeRunningGraph.grf", cronTime,
					null);
			scheduleDetail.manualRun();
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
			deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH,
					"Change Executable of Schedule", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Change Executable of Schedule",
					"/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Change Executable of Schedule", "successfulGraph.grf", cronTime,
					null);
			scheduleDetail.changeExecutable("/graph/errorGraph.grf");
			assertNewSchedule("Change Executable of Schedule", "errorGraph.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void deleteSchedule() throws JSONException, InterruptedException {
		deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH, "Delete Schedule",
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
		createScheduleForProcess(projectTitle, "Delete Schedule", "/graph/successfulGraph.grf",
				cronTime, null);
		assertNewSchedule("Delete Schedule", "successfulGraph.grf", cronTime, null);
		scheduleDetail.deleteSchedule();
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
			deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH,
					"Change Cron Time of Schedule", Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_30_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Change Cron Time of Schedule",
					"/graph/successfulGraph.grf", cronTime, null);
			assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf", cronTime, null);
			Pair<String, List<String>> newCronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			scheduleDetail.changeCronTime(newCronTime);
			assertNewSchedule("Change Cron Time of Schedule", "successfulGraph.grf", newCronTime,
					null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void editScheduleParameters() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Edit schedule parameters", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("param 2", Arrays.asList("", "value 2"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Edit schedule parameters", "/graph/DWHS2.grf",
					cronTime, parameters);
			assertNewSchedule("Edit schedule parameters", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1 new name", Arrays.asList("", "value 1 new"));
			changedParameters.put("param 2 new name", Arrays.asList("", "value 2 new"));
			changedParameters.put("secure param new name",
					Arrays.asList("secure", "secure value new"));
			scheduleDetail.editScheduleParameters(changedParameters, false);
			assertNewSchedule("Edit schedule parameters", "DWHS2.grf", cronTime, changedParameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void addNewParametersForSchedule() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Add New Parameters for Schedule", Arrays.asList("DWHS1.grf", "DWHS2.grf"),
					true);
			Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
			parameters.put("param 1", Arrays.asList("", "value 1"));
			parameters.put("secure param", Arrays.asList("secure", "secure value"));
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Add New Parameters for Schedule",
					"/graph/DWHS2.grf", cronTime, parameters);
			assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", cronTime, parameters);
			Map<String, List<String>> newParameters = new LinkedHashMap<String, List<String>>();
			newParameters.put("param 2", Arrays.asList("", "value 2"));
			newParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
			Map<String, List<String>> changedParameters = new LinkedHashMap<String, List<String>>();
			changedParameters.put("param 1", Arrays.asList("", "value 1"));
			changedParameters.put("param 2", Arrays.asList("", "value 2"));
			changedParameters.put("secure param", Arrays.asList("secure", "secure value"));
			changedParameters.put("secure param 2", Arrays.asList("secure", "secure value 2"));
			scheduleDetail.editScheduleParameters(newParameters, true);
			assertNewSchedule("Add New Parameters for Schedule", "DWHS2.grf", cronTime,
					changedParameters);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void createScheduleWithIncorrectCron() throws JSONException, InterruptedException {
		deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
				"Create Schedule With Error Cron", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		Pair<String, List<String>> incorrectCronTime = Pair.of(
				ScheduleCronTimes.CRON_EXPRESSION.getCronTime(), Arrays.asList("* * *"));
		openProjectDetailPage(projectTitle);
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.clickOnNewScheduleButton();
		waitForElementVisible(scheduleForm.getRoot());
		scheduleForm.checkScheduleWithIncorrectCron(incorrectCronTime,
				scheduleForm.getConfirmScheduleButton());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void editScheduleWithIncorrectCron() throws JSONException, InterruptedException {
		try {
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Edit Schedule With Error Cron", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Edit Schedule With Error Cron",
					"/graph/DWHS2.grf", cronTime, null);
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
			deployInProjectDetailPage(projectTitle, "cloudconnect", DISCProcessTypes.GRAPH,
					"Check Broken Schedule", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectTitle, "Check Broken Schedule", null, null, null);
			assertNewSchedule("Check Broken Schedule", "DWHS1.grf", cronTime, null);
			redeployProcess(projectTitle, "Check Broken Schedule", "Basic", "Redeployed Process",
					DISCProcessTypes.GRAPH, Arrays.asList("errorGraph.grf",
							"longTimeRunningGraph.grf", "successfulGraph.grf"), true);
			projectDetailPage.checkBrokenScheduleSection("Redeployed Process");
			assertBrokenSchedule("Check Broken Schedule", "DWHS1.grf", cronTime, null);
			scheduleDetail.checkBrokenSchedule("DWHS1.grf", "/graph/errorGraph.grf");
			assertNewSchedule("Check Broken Schedule", "errorGraph.grf", cronTime, null);
		} finally {
			scheduleDetail.disableSchedule();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedule" })
	public void checkScheduleFailForManyTimes() throws JSONException, InterruptedException {
		deployInProjectDetailPage(projectTitle, "Basic", DISCProcessTypes.GRAPH,
				"Check Failed Schedule",
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
		createScheduleForProcess(projectTitle, "Check Failed Schedule", null, cronTime, null);
		assertNewSchedule("Check Failed Schedule", "errorGraph.grf", cronTime, null);
		scheduleDetail.checkFailedSchedule("Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH);
	}

	@Test(dependsOnGroups = { "schedule" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}
}
