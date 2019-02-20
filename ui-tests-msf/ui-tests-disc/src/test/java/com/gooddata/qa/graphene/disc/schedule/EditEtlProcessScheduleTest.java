package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static com.gooddata.qa.graphene.entity.disc.Parameters.createRandomParam;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class EditEtlProcessScheduleTest extends AbstractEtlProcessTest {

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
    public void editEtlProcessScheduleWithCustomName(ProcessType processType) {
        testEditScheduleWithCustomName(processType);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void editEtlProcessScheduleParameters(ProcessType processType) {
        testEditScheduleParameters(processType);
    }

    private void testEditScheduleWithCustomName(ProcessType processType) {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(processName, processType);
        DataloadProcess process = getProcessByName(processName);

        assertNotNull(process);
        String scheduleName = generateScheduleName();
        etlProcessRequest.createEtlProcessSchedule(process.getId(), scheduleName,
                ScheduleCronTime.EVERY_30_MINUTES.getExpression());
        Schedule schedule = getEtlProcessScheduleByName(process, scheduleName);
        assertNotNull(schedule);
        try {
            String customName = generateScheduleName();
            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.editNameByClickOnTitle(customName).saveChanges();

            assertEquals(scheduleDetail.getName(), customName);
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(customName));
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    private void testEditScheduleParameters(ProcessType processType) {
        String processName = generateProcessName();
        createEtlProcessWithDefaultConfig(processName, processType);
        DataloadProcess process = getProcessByName(processName);

        assertNotNull(process);

        try {
            // Create schedule
            Pair<String, String> param = createRandomParam();
            Pair<String, String> secureParam = createRandomParam();
            Pair<String, String> randomParam = createRandomParam();
            Pair<String, String> randomSecureParam = createRandomParam();

            ((CreateScheduleForm) initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameter(param.getKey(), param.getValue())
                    .addParameter(randomParam.getKey(), randomParam.getValue())
                    .addSecureParameter(secureParam.getKey(), secureParam.getValue())
                    .addSecureParameter(randomSecureParam.getKey(), randomSecureParam.getValue()))
                    .schedule();

            // Verify create schedule success
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser);
            Map<String, String> actualParam = scheduleDetail.getAllParametersInfo();
            Map<String, String> actualSecureParam = scheduleDetail.getAllSecureParametersInfo();

            assertEquals(actualParam.size(), 2);
            assertThat(scheduleDetail.getAllParametersInfo().entrySet(), hasItem(param));
            assertEquals(actualSecureParam.size(), 2);
            assertTrue(scheduleDetail.getAllSecureParametersInfo().keySet().contains(secureParam.getKey()));

            // Edit schedule parameters
            Pair<String, String> editedParam = createRandomParam();
            Pair<String, String> editedSecureParam = createRandomParam();
            scheduleDetail.getParameter(param.getKey()).editNameValuePair(editedParam.getKey(), editedParam.getValue());
            scheduleDetail.getParameter(secureParam.getKey())
                    .editNameValuePair(editedSecureParam.getKey(), editedSecureParam.getValue());
            scheduleDetail.saveChanges();

            // Verify edit schedule parameters success
            scheduleDetail = ScheduleDetail.getInstance(browser);
            actualParam = scheduleDetail.getAllParametersInfo();
            actualSecureParam = scheduleDetail.getAllSecureParametersInfo();

            assertEquals(actualParam.size(), 2);
            assertThat(scheduleDetail.getAllParametersInfo().entrySet(), hasItem(editedParam));
            assertEquals(actualSecureParam.size(), 2);
            assertTrue(scheduleDetail.getAllSecureParametersInfo().keySet().contains(editedSecureParam.getKey()));
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    private Schedule getEtlProcessScheduleByName(DataloadProcess process, String scheduleName) {
        return getProcessService().listSchedules(getProject()).stream()
                .filter(s -> s.getProcessId().equals(process.getId()))
                .filter(s -> scheduleName.equals(s.getName()))
                .findFirst()
                .get();
    }

}
