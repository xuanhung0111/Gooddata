package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessType;
import com.gooddata.sdk.model.dataload.processes.Schedule;
import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.utils.ads.AdsHelper.ADS_DB_CONNECTION_URL;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class MsfProcessTest extends AbstractDataloadProcessTest {

    protected boolean useK8sExecutor;
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "ATT_LCM Client project " + generateHashString();

    private String devProjectId;
    private String clientProjectId;

    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private RestClient domainRestClient;
    private String dataloadScheduleName = "";

    @Override
    protected void customizeProject() {
        domainRestClient = new RestClient(new RestProfile(testParams.getHost(), testParams.getDomainUser(), testParams.getPassword(), true));
    }

    @Override
    public void setup() throws ParseException, JSONException, IOException {
        // need to create ads instance by domain user then we can remove the reference from master, develop, client project
        adsHelper = new AdsHelper(domainRestClient, testParams.getProjectId());
        ads = adsHelper.createAds("att-ads-" + generateHashString(), getAdsToken());

        log.info(String.format("Created ads instance:'%s'", ads.getUri()));
        // need add admin user to the ads instance's users group
        adsHelper.addUserToWarehouse(ads, testParams.getUser());

        adsHelper.associateAdsWithProject(ads, testParams.getProjectId(), CLIENT_ID, "");

        updateAdsTableProcess = getProcessService().createProcess(getProject(),
                new DataloadProcess(generateProcessName(), ProcessType.GRAPH), PackageFile.ADS_TABLE.loadFile());

        defaultParameters = () -> new Parameters()
                .addParameter(Parameter.ADS_URL, format(ADS_DB_CONNECTION_URL, testParams.getHost(), ads.getId()))
                .addParameter(Parameter.ADS_USER, testParams.getUser())
                .addSecureParameter(Parameter.ADS_PASSWORD, testParams.getPassword());
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void prepareAdsData() {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .buildMaql());
        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_CLIENT_ID_COLUMN))
                .rows("OOP1", "100", CLIENT_ID)
                .rows("OOP2", "200", CLIENT_ID)
                .rows("OPP3", "300", "");

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity)));

        Schedule schedule = createManualTriggeredSchedule(generateScheduleName(), SyncDatasets.custom(DATASET_OPPORTUNITY));
        dataloadScheduleName = schedule.getName();
    }

    @Test(dependsOnMethods = {"prepareAdsData"}, groups = {"precondition"})
    public void deployLcmFlow() throws ParseException, JSONException, IOException {
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        devProjectId = testParams.getProjectId();
        clientProjectId = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE);
        lcmBrickFlowBuilder.setSegmentId(SEGMENT_ID).setClientId(CLIENT_ID)
                .setDevelopProject(devProjectId).setClientProjects(clientProjectId).buildLcmProjectParameters();

        log.info("------dev project id:" + devProjectId);
        log.info("------client project id:" + clientProjectId);
        testParams.setProjectId(clientProjectId);
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        lcmBrickFlowBuilder.runLcmFlow();
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testPullingDataFromAds() throws ParseException, JSONException, IOException {
        AbstractScheduleDetail scheduleDetail = initDataloadScheduleDetail().executeSchedule().waitForExecutionFinish();
        assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                ScheduleStatus.OK.toString());
        String executionLog = new CommonRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .getResource(scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
        assertThat(executionLog,
                containsString("[INFO]: Output stage to LDM replication finished successfully"));
        Attribute opportunityAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
        log.info("Uri of opportunity attribute:" + opportunityAttr.getUri());
        assertEquals(getAttributeValues(opportunityAttr), asList("OOP1", "OOP2"));
    }

    @AfterClass(alwaysRun = true, dependsOnMethods = {"removeAdsInstance"})
    public void tearDown() {
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void removeAdsInstance() throws ParseException, JSONException, IOException {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        adsHelper.removeAds(ads);
    }

    private DataloadScheduleDetail initDataloadScheduleDetail() {
        return initDiscProjectDetailPage().getDataloadProcess().openSchedule(dataloadScheduleName);
    }
}
