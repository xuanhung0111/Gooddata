package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.utils.ads.AdsHelper.ADS_DB_CONNECTION_URL;
import static java.lang.String.format;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

import com.gooddata.qa.utils.schedule.ManualTriggeredSchedule;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.sdk.common.GoodDataException;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessType;
import com.gooddata.sdk.model.dataload.processes.Schedule;
import com.gooddata.sdk.model.dataload.processes.ScheduleExecution;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;
import com.gooddata.sdk.model.warehouse.Warehouse;

public class AbstractDataloadProcessTest extends AbstractDataIntegrationTest {

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "Automated Data Distribution";
    protected static final String UPDATE_ADS_TABLE_EXECUTABLE = "DLUI/graph/CreateAndCopyDataToADS.grf";

    protected static final String DATASET_OPPORTUNITY = "opportunity";
    protected static final String DATASET_PERSON = "person";

    protected static final String X_TIMESTAMP_COLUMN = "timestamp";
    protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected static final String X_CLIENT_ID_COLUMN = "clientId";

    protected static final String ATTR_OPPORTUNITY = "opportunity";
    protected static final String ATTR_PERSON = "person";
    protected static final String ATTR_NAME = "name";
    protected static final String ATTR_TITLE = "title";

    protected static final String FACT_AGE = "age";
    protected static final String FACT_PRICE = "price";
    protected static final String FACT_TOTAL_PRICE = "totalprice";

    private static final String DATALOAD_PROCESS_TYPE = "DATALOAD";

    protected AdsHelper adsHelper;
    protected Warehouse ads;
    protected DataloadProcess updateAdsTableProcess;
    protected Supplier<Parameters> defaultParameters;

    @Test(dependsOnGroups = {"createProject"}, groups = {"initDataload"})
    public void setup() throws ParseException, JSONException, IOException {
        adsHelper = new AdsHelper(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        ads = adsHelper.createAds("att-ads-" + generateHashString(), getAdsToken());

        log.info("---created ads uri:" + ads.getUri());

        adsHelper.associateAdsWithProject(ads);

        updateAdsTableProcess = getProcessService().createProcess(getProject(),
                new DataloadProcess(generateProcessName(), ProcessType.GRAPH), PackageFile.ADS_TABLE.loadFile());

        defaultParameters = () -> new Parameters()
                .addParameter(Parameter.ADS_URL, format(ADS_DB_CONNECTION_URL, testParams.getHost(), ads.getId()))
                .addParameter(Parameter.ADS_USER, testParams.getUser())
                .addSecureParameter(Parameter.ADS_PASSWORD, testParams.getPassword());
    }

    @AfterClass(alwaysRun = true)
    public void removeAdsInstance() throws ParseException, JSONException, IOException {
        adsHelper.removeAds(ads);
    }

    protected DataloadProcess createDataloadProcess() {
        return getProcessService().createProcess(getProject(),
                new DataloadProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD_PROCESS_TYPE));
    }

    protected boolean canCreateDataloadProcess() {
        if (!hasDataloadProcess()) return true;

        try {
            createDataloadProcess();
            throw new RuntimeException("Dataload process can be created more than one!");

        } catch (GoodDataException e) {
            return false;
        }
    }

    protected DataloadProcess getDataloadProcess() {
        return findDataloadProcess().get();
    }

    @Deprecated
    protected boolean hasDataloadProcess() {
        return findDataloadProcess().isPresent();
    }

    protected Schedule createSchedule(String name, SyncDatasets datasetToSynchronize, String crontimeExpression) {
        return createScheduleWithTriggerType(name, datasetToSynchronize, crontimeExpression);
    }

    /**
     * @deprecated If need to create a real 'manual' schedule use createManualTriggeredSchedule instead
     * This method is used to create a schedule which never been automatically triggered.
     */
    protected Schedule createScheduleForManualTrigger(String name, SyncDatasets datasetToSynchronize) {
        String cronExpression = parseTimeToCronExpression(LocalTime.now().minusMinutes(2));
        return createSchedule(name, datasetToSynchronize, cronExpression);
    }

    //This method is used to create a 'real' manual schedule
    @Deprecated
    protected Schedule createManualTriggeredSchedule(String name, SyncDatasets datasetToSynchronize) {
        return createSchedule(name, datasetToSynchronize, "");
    }

    @Deprecated
    protected Schedule createSchedule(String name, SyncDatasets datasetToSynchronize, Schedule triggeringSchedule) {
        return createScheduleWithTriggerType(name, datasetToSynchronize, triggeringSchedule);
    }

    @Deprecated
    protected ScheduleExecution executeSchedule(Schedule schedule) {
        return executeSchedule(schedule, LoadMode.DEFAULT);
    }

    @Deprecated
    protected ScheduleExecution executeSchedule(Schedule schedule, LoadMode loadMode) {
        schedule.addParam("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", loadMode.toString());
        getProcessService().updateSchedule(schedule);
        return getProcessService().executeSchedule(schedule).get();
    }

    protected DataloadScheduleDetail initScheduleDetail(Schedule schedule) {
        return initDiscProjectDetailPage().getDataloadProcess().openSchedule(schedule.getName());
    }

    protected String getAdsToken() {
        return testParams.loadProperty("dss.authorizationToken");
    }

    protected String parseDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    protected LocalDateTime parseDateTime(String dateTime, String pattern) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    @Deprecated
    private Optional<DataloadProcess> findDataloadProcess() {
        return getProcessService().listProcesses(getProject())
                .stream().filter(p -> p.getType().equals(DATALOAD_PROCESS_TYPE)).findFirst();
    }

    @Deprecated
    private Schedule createScheduleWithTriggerType(String name, SyncDatasets datasetToSynchronize,
            Object triggerType) {
        Schedule schedule = null;

        if (triggerType instanceof String) {
            // if schedule has triggerType is an empty string, consider as 'manual' triggered schedule
            if ("".equals(triggerType)) {
                schedule = new ManualTriggeredSchedule(getDataloadProcess(), null);
            } else {
                schedule = new Schedule(getDataloadProcess(), null, (String) triggerType);
            }
        } else {
            schedule = new Schedule(getDataloadProcess(), null, (Schedule) triggerType);
        }

        Pair<String, String> datasetParameter = datasetToSynchronize.getParameter();
        schedule.addParam(datasetParameter.getKey(), datasetParameter.getValue());
        schedule.setName(name);

        return getProcessService().createSchedule(getProject(), schedule);
    }

    protected enum TxtFile {
        CREATE_LDM("createLdm.txt"),
        ADS_TABLE("adsTable.txt"),
        LARGE_ADS_TABLE("largeAdsTable.txt"),
        GD_ENDCODE_PARAM_RELEASE("gdEndcodeParam.txt"),
        GD_ENDCODE_PARAM_PROVISIONING("gdEndcodeParamProvision.txt"),
        GD_ENDCODE_PARAM_ROLLOUT("gdEndcodeParamRollout.txt");

        private String name;

        TxtFile(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
