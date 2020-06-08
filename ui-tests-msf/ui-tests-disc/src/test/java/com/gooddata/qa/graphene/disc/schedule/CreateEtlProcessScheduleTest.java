package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.DayOfWeek;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class CreateEtlProcessScheduleTest extends AbstractEtlProcessTest {

    @DataProvider(name = "processTypeProvider")
    public Object[][] getProcessTypeProvider() {
        return new Object[][] {
                {ProcessType.CSV_DOWNLOADER},
                {ProcessType.SQL_DOWNLOADER},
                {ProcessType.GOOGLE_ANALYTICS_DOWNLOADER},
                {ProcessType.SALESFORCE_DOWNLOADER},
                {ProcessType.ADS_INTEGRATOR},
                {ProcessType.SQL_EXECUTOR}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void createEtlProcessSchedule(ProcessType processType) {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(processName, processType);
        DataloadProcess process = getProcessByName(processName);

        assertNotNull(process);

        String scheduleName = generateScheduleName();
        try {
            CreateScheduleForm scheduleForm = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByCronExpression(ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            configScheduleType(scheduleForm, processType);
            scheduleForm.schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(processName).hasSchedule(scheduleName));
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    /**
     * Expected testing Etl process schedules with many schedule types
     */
    private void configScheduleType(CreateScheduleForm scheduleForm, ProcessType processType) {
        if (ProcessType.CSV_DOWNLOADER.equals(processType)) {
            scheduleForm.selectRunTimeByEveryHour(1);
        } else if (ProcessType.SQL_DOWNLOADER.equals(processType)) {
            scheduleForm.selectRunTimeByEveryDay(1,1);
        } else if (ProcessType.GOOGLE_ANALYTICS_DOWNLOADER.equals(processType)) {
            scheduleForm.selectRunTimeByEveryWeek(DayOfWeek.MONDAY, 1, 1);
        } else if (ProcessType.SALESFORCE_DOWNLOADER.equals(processType)) {
            scheduleForm.selectRunTimeByCronExpression(ScheduleCronTime.EVERY_30_MINUTES.getExpression());
        } else {
            scheduleForm.selectRunTime(ScheduleCronTime.EVERY_15_MINUTES);
        }
    }
}
