package com.gooddata.qa.utils.schedule;

import static java.lang.String.format;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import com.gooddata.qa.utils.datasource.DataDistributionProcess;
import org.apache.commons.lang3.tuple.Pair;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecution;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;

import com.gooddata.qa.utils.http.RestClient;

public class ScheduleUtils {

    protected RestClient restClient;
    protected TestParameters testParams;
    private static final String SEGMENT_URI = "/gdc/domains/%s/dataproducts/%s/segments/%s";

    public ScheduleUtils(RestClient restClient) {
        this.testParams = TestParameters.getInstance();
        this.restClient = restClient;
    }

    public ProcessService getProcessService() {
        return restClient.getProcessService();
    }

    public DataloadProcess createDataDistributionProcess(Project serviceProject, String processName, String datasourceId,
                                                         String segmentId, String dataproduct, String version) {
        return getProcessService().createProcess(serviceProject,
                new DataDistributionProcess(processName, datasourceId,
                        String.format(SEGMENT_URI, testParams.getUserDomain(), dataproduct, segmentId), version));
    }

    public DataloadProcess createDataDistributionProcess(Project serviceProject, String processName, String datasourceId, String version) {
        return getProcessService().createProcess(serviceProject, new DataDistributionProcess(processName, datasourceId, version));
    }

    public DataloadProcess getDataloadProcess(String processName, Project serviceProject) {
        return findDataloadProcess(processName, serviceProject).get();
    }

    public Schedule createSchedule(String name, SyncDatasets datasetToSynchronize, String crontimeExpression, String processName,
            Project serviceproject) {
        return createScheduleWithTriggerType(name, datasetToSynchronize, crontimeExpression, processName, serviceproject);
    }

    public Schedule createSchedule(String name, SyncDatasets datasetToSynchronize, Schedule triggeringSchedule,
            String processName, Project serviceproject) {
        return createScheduleWithTriggerType(name, datasetToSynchronize, triggeringSchedule, processName, serviceproject);
    }

    public ScheduleExecution executeSchedule(Schedule schedule) {
        return executeSchedule(schedule, LoadMode.DEFAULT);
    }

    public ScheduleExecution executeSchedule(Schedule schedule, LoadMode loadMode) {
        schedule.addParam("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", loadMode.toString());
        getProcessService().updateSchedule(schedule);
        return getProcessService().executeSchedule(schedule).get();
    }

    public Optional<DataloadProcess> findDataloadProcess(String processName, Project serviceproject) {
        return getProcessService().listProcesses(serviceproject).stream().filter(p -> p.getName().equals(processName))
                .findFirst();
    }

    public String generateScheduleName() {
        return "schedule-" + generateHashString();
    }

    public String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    public Schedule createScheduleWithTriggerType(String name, SyncDatasets datasetToSynchronize, Object triggerType,
            String processName, Project serviceproject) {
        Schedule schedule = null;

        if (triggerType instanceof String) {
            // if schedule has triggerType is an empty string, consider as 'manual'
            // triggered schedule
            if ("".equals(triggerType)) {
                schedule = new ManualTriggeredSchedule(getDataloadProcess(processName, serviceproject), null);
            } else {
                schedule = new Schedule(getDataloadProcess(processName, serviceproject), null, (String) triggerType);
            }
        } else {
            schedule = new Schedule(getDataloadProcess(processName, serviceproject), null, (Schedule) triggerType);
        }

        Pair<String, String> datasetParameter = datasetToSynchronize.getParameter();
        schedule.addParam(datasetParameter.getKey(), datasetParameter.getValue());
        schedule.setName(name);

        return getProcessService().createSchedule(serviceproject, schedule);
    }

    public String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
