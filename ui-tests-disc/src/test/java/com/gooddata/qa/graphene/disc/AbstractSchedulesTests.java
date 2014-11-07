package com.gooddata.qa.graphene.disc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractSchedulesTests extends AbstractDeployProcesses {

    protected void createScheduleForProcess(String projectName, String projectId,
            String processName, String scheduleName,
            String executable, Pair<String, List<String>> cronTime, Map<String, List<String>> parameters) throws InterruptedException {
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.createNewSchedule(processName, executable, cronTime, parameters, scheduleName, true);
        waitForElementPresent(scheduleDetail.getRoot());
        scheduleDetail.clickOnCloseScheduleButton();
        waitForElementNotPresent(scheduleDetail.getRoot());
    }

    protected void assertNewSchedule(String processName, String scheduleName,
            String executable, Pair<String, List<String>> cronTime, Map<String, List<String>> parameters)
            throws InterruptedException {
        projectDetailPage.checkFocusedProcess(processName);
        waitForElementVisible(schedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(schedulesTable, scheduleName, executable, cronTime);
        schedulesTable.getScheduleTitle(scheduleName).click();
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.assertScheduleDetail(scheduleName, executable, cronTime, parameters);
    }

    protected void assertBrokenSchedule(String scheduleName, String executable,
            Pair<String, List<String>> cronTime)
            throws InterruptedException {
        waitForElementVisible(brokenSchedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(brokenSchedulesTable, scheduleName, executable, cronTime);
    }
}
