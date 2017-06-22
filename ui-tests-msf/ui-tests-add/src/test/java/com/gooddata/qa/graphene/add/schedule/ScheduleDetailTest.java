package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalTime;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage.OverviewState;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewProjects.OverviewProjectItem;
import com.gooddata.qa.graphene.fragments.disc.process.DataloadProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail.ExecutionHistoryItem;

public class ScheduleDetailTest extends AbstractDataloadProcessTest {

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName()));

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void executeDataloadSchedule() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void autoExecuteDataloadSchedule() {
        LocalTime autoStartTime = LocalTime.now().plusMinutes(2);
        Schedule schedule = createSchedule(generateScheduleName(), SyncDatasets.ALL,
                parseTimeToCronExpression(autoStartTime));

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.waitForAutoExecute(autoStartTime).waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkConcurrentDataLoadSchedule() {
        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.LARGE_ADS_TABLE.getName()));

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        Schedule schedule1 = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);
        Schedule schedule2 = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DataloadProcessDetail processDetail = initDiscProjectDetailPage().getDataloadProcess();
            processDetail.openSchedule(schedule1.getName()).executeSchedule()
                    .waitForStatus(ScheduleStatus.RUNNING).close();
            processDetail.openSchedule(schedule2.getName()).executeSchedule().waitForExecutionFinish();

            ExecutionHistoryItem executionItem = DataloadScheduleDetail.getInstance(browser)
                    .getLastExecutionHistoryItem();
            assertEquals(executionItem.getStatusDescription(), "SCHEDULER_ERROR");
            assertEquals(executionItem.getErrorMessage(), "The schedule did not run because one or more of the "
                    + "datasets in this schedule is already synchronizing.");

        } finally {
            getProcessService().removeSchedule(schedule1);
            getProcessService().removeSchedule(schedule2);

            parameters.addParameter("SQL_QUERY",
                    SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName()));
            executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    parameters.getParameters(), parameters.getSecureParameters());
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void disableDataloadSchedule() {
        LocalTime autoStartTime = LocalTime.now().plusMinutes(2);
        Schedule schedule = createSchedule(generateScheduleName(), SyncDatasets.ALL,
                parseTimeToCronExpression(autoStartTime));

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.disableSchedule();
            assertFalse(scheduleDetail.canAutoTriggered(autoStartTime),
                    "Schedule executed automatically although disabled");

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void dependentDataloadSchedule() {
        Schedule schedule1 = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));
        Schedule schedule2 = createSchedule(generateScheduleName(), SyncDatasets.ALL, schedule1);

        try {
            DataloadProcessDetail processDetail = initDiscProjectDetailPage().getDataloadProcess();
            processDetail.openSchedule(schedule1.getName()).executeSchedule().close();

            DataloadScheduleDetail scheduleDetail = processDetail.openSchedule(schedule2.getName());

            scheduleDetail.waitForAutoExecute(LocalTime.now()).waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            getProcessService().removeSchedule(schedule1);
            getProcessService().removeSchedule(schedule2);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDataloadScheduleAtOverviewPage() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

            OverviewProjectItem project = initDiscOverviewPage()
                    .selectState(OverviewState.SUCCESSFUL).getOverviewProject(projectTitle);
            assertTrue(project.expand().hasSchedule(schedule.getName()), "Schedule " + schedule + " not show");
            assertEquals(project.getScheduleExecutable(schedule.getName()), "");

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }
}
