package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import org.testng.annotations.Test;

import java.time.DayOfWeek;

import static org.testng.Assert.assertTrue;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class CreateEtlProcessScheduleTest extends AbstractEtlProcessTest {

    @Test(dependsOnGroups = {"createProject"})
    public void createCSVDownloaderSchedule() {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(getProject().getId(), processName, ProcessType.CSV_DOWNLOADER);
        DataloadProcess process = getProcessByName(processName);

        assertTrue(process != null, "Failed to deploy CSV Downloader process");

        String scheduleName = generateScheduleName();
        try {
            CreateScheduleForm scheduleForm = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByEveryHour(1);
            scheduleForm.schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(processName).hasSchedule(scheduleName),
                    "CSV Downloader process schedule is not created");
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSQLDownloaderSchedule() {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(getProject().getId(), processName, ProcessType.SQL_DOWNLOADER);
        DataloadProcess process = getProcessByName(processName);

        assertTrue(process != null, "Failed to deploy SQL Downloader process");

        String scheduleName = generateScheduleName();
        try {
            CreateScheduleForm scheduleForm = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByEveryDay(1,1);
            scheduleForm.schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(processName).hasSchedule(scheduleName),
                    "SQL Downloader process schedule is not created");
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createADSIntegratorSchedule() {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(getProject().getId(), processName, ProcessType.ADS_INTEGRATOR);
        DataloadProcess process = getProcessByName(processName);

        assertTrue(process != null, "Failed to deploy ADS Integrator process");

        String scheduleName = generateScheduleName();
        try {
            CreateScheduleForm scheduleForm = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByEveryWeek(DayOfWeek.MONDAY, 1, 1);
            scheduleForm.schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(processName).hasSchedule(scheduleName),
                    "ADS Integrator process schedule is not created");
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSQLExecutorSchedule() {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(getProject().getId(), processName, ProcessType.SQL_DOWNLOADER);
        DataloadProcess process = getProcessByName(processName);

        assertTrue(process != null, "Failed to deploy SQL Executor process");

        String scheduleName = generateScheduleName();
        try {
            CreateScheduleForm scheduleForm = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByCronExpression(ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            scheduleForm.schedule();

            ScheduleDetail.getInstance(browser).close();
            assertTrue(projectDetailPage.getProcess(processName).hasSchedule(scheduleName),
                    "SQL Executor process schedule is not created");
        } finally {
            getProcessService().removeProcess(process);
        }
    }
}
