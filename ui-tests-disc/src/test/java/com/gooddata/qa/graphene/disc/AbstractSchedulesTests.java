package com.gooddata.qa.graphene.disc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.graphene.fragments.disc.SchedulesTable;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractSchedulesTests extends AbstractDeployProcesses {

	protected void createScheduleForProcess(String projectName, String projectId,
			String processName, String executable,
			Pair<String, List<String>> cronTime, Map<String, List<String>> parameters) throws InterruptedException {
		projectDetailPage.clickOnNewScheduleButton();
		waitForElementVisible(scheduleForm.getRoot());
		scheduleForm.createNewSchedule(processName, executable, cronTime, parameters, true);
		waitForElementPresent(scheduleDetail.getRoot());
		scheduleDetail.clickOnCloseScheduleButton();
		waitForElementVisible(projectDetailPage.getRoot());
	}

	protected void assertNewSchedule(String processName, String executableName,
			Pair<String, List<String>> cronTime, Map<String, List<String>> parameters)
			throws InterruptedException {
		assertSchedule(schedulesTable, processName, executableName, cronTime, parameters);
	}

	protected void assertBrokenSchedule(String processName, String executableName,
			Pair<String, List<String>> cronTime, Map<String, List<String>> parameters)
			throws InterruptedException {
		assertSchedule(brokenSchedulesTable, processName, executableName, cronTime, parameters);
	}

	protected void assertSchedule(SchedulesTable schedulesTable, String processName,
			String executableName, Pair<String, List<String>> cronTime,
			Map<String, List<String>> parameters) throws InterruptedException {
		waitForElementVisible(schedulesTable.getRoot());
		for(int i = 0; i < 10 && schedulesTable.getScheduleTitle(executableName) == null; i++)
			Thread.sleep(1000);
		Assert.assertEquals(executableName, schedulesTable.getScheduleTitle(executableName)
				.getText());
		String cronFormat = "";
		if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYWEEK.getCronTime()))
			cronFormat = ScheduleCronTimes.CRON_EVERYWEEK.getCronFormat()
					.replace("${day}", cronTime.getValue().get(2))
					.replace("${hour}", cronTime.getValue().get(1))
					.replace("${minute}", cronTime.getValue().get(0));
		if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYDAY.getCronTime()))
			cronFormat = ScheduleCronTimes.CRON_EVERYDAY.getCronFormat()
					.replace("${hour}", cronTime.getValue().get(1))
					.replace("${minute}", cronTime.getValue().get(0));
		if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime())
				|| cronTime.getKey().equals(ScheduleCronTimes.CRON_15_MINUTES.getCronTime())
				|| cronTime.getKey().equals(ScheduleCronTimes.CRON_30_MINUTES.getCronTime()))
			cronFormat = cronTime.getKey();
		if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EXPRESSION.getCronTime()))
			cronFormat = cronTime.getValue().get(0) + " UTC";
		Assert.assertEquals(cronFormat, schedulesTable.getScheduleCron(executableName).getText());
		schedulesTable.getScheduleTitle(executableName).click();
		scheduleDetail.assertScheduleParameters(parameters);
	}
}
