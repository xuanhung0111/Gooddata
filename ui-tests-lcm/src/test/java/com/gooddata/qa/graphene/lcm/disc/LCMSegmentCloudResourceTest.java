package com.gooddata.qa.graphene.lcm.disc;

import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.LcmDirectoryConfiguration;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.deleteSegment;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.getMasterProjectId;
import static java.lang.String.format;
import static org.testng.Assert.*;

public class LCMSegmentCloudResourceTest extends AbstractDataloadProcessTest {

    public DataloadScheduleDetail scheduleDetail;
    public CreateScheduleForm scheduleForm;
    public DataSourceUtils dataSourceUtils;
    public PostgreUtils postgreUtils;
    public SnowflakeUtils snowflakeUtils;
    public RedshiftUtils redshiftUtils;
    public BigQueryUtils bigqueryUtils;
    private String DATASOURCE_PRIVATE_KEY;
    private String privatekey;

    private final String LCM_RELEASE_SNOWFLAKE = "LCM_SNOWFLAKE_RELEASE_" + generateHashString();
    private final String LCM_RELEASE_REDSHIFT = "LCM_REDSHIFT_RELEASE_" + generateHashString();
    private final String LCM_RELEASE_BIGQUERY = "LCM_BIGQUERY_RELEASE_" + generateHashString();
    private final String LCM_RELEASE_POSTGRES = "LCM_POSTGRE_RELEASE_" + generateHashString();
    private final String LCM_ROLLOUT_SNOWFLAKE = "LCM_ROLLOUT_SNOWFLAKE_" + generateHashString();
    private final String LCM_ROLLOUT_REDSHIFT = "LCM_ROLLOUT_REDSHIFT_" + generateHashString();
    private final String LCM_ROLLOUT_BIGQUERY = "LCM_ROLLOUT_BIGQUERY_" + generateHashString();
    private final String LCM_ROLLOUT_POSTGRES = "LCM_ROLLOUT_POSTGRE_" + generateHashString();
    private final String LCM_PROVISIONING_SNOWFLAKE = "LCM_PROVISIONING_SNOWFLAKE_" + generateHashString();
    private final String LCM_PROVISIONING_REDSHIFT = "LCM_PROVISIONING_REDSHIFT_" + generateHashString();
    private final String LCM_PROVISIONING_BIGQUERY = "LCM_PROVISIONING_BIGQUERY_" + generateHashString();
    private final String LCM_PROVISIONING_POSTGRES = "LCM_PROVISIONING_POSTGRE_" + generateHashString();

    private final String MASTER_NAME_SNOWFLAKE = ("ATT_Master_Snowflake_" + generateHashString()).toUpperCase();
    private final String MASTER_NAME_REDSHIFT = "ATT_Master_Redshift_" + generateHashString();
    private final String MASTER_NAME_BIGQUERY = "ATT_Master_Bigquery_" + generateHashString();
    private final String MASTER_NAME_POSTGRES = "ATT_Master_Postgres_" + generateHashString();
    private final String SEGMENT_ID_SNOWFLAKE = ("ATTSegmentSnowflake_" + generateHashString()).toUpperCase();
    private final String SEGMENT_ID_REDSHIFT = "ATTSegmentRedshift_" + generateHashString();
    private final String SEGMENT_ID_BIGQUERY = "ATTSegmentBigquery_" + generateHashString();
    private final String SEGMENT_ID_POSTGRES = "ATTSegmentPostgres_" + generateHashString();
    private static String END_CODE_PARAM;
    private static String END_CODE_HIDDEN_PARAM;
    private static String DEVELOPMENT_PID;
    private static String DEVELOPMENT_TITLE;
    private static String ADS_WAREHOUSE;

    private static final String INSIGHT_LCM = "Insight-LCM";
    private static final String RELEASE_SCHEDULE_NAME_SNOWFLAKE = "Release_Schedule_Snowflake";
    private static final String RELEASE_SCHEDULE_NAME_REDSHIFT = "Release_Schedule_Redshift";
    private static final String RELEASE_SCHEDULE_NAME_BIGQUERY = "Release_Schedule_Bigquery";
    private static final String RELEASE_SCHEDULE_NAME_POSTGRES = "Release_Schedule_Postgres";
    private static final String PROVISION_SCHEDULE_NAME_SNOWFLAKE = "Provision_Schedule_Snowflake";
    private static final String PROVISION_SCHEDULE_NAME_REDSHIFT = "Provision_Schedule_Redshift";
    private static final String PROVISION_SCHEDULE_NAME_BIGQUERY = "Provision_Schedule_Bigquery";
    private static final String PROVISION_SCHEDULE_NAME_POSTGRES = "Provision_Schedule_Postgres";
    private static final String ROLLOUT_SCHEDULE_NAME_SNOWFLAKE = "Rollout_Schedule_Snowflake";
    private static final String ROLLOUT_SCHEDULE_NAME_REDSHIFT = "Rollout_Schedule_Redshift";
    private static final String ROLLOUT_SCHEDULE_NAME_BIGQUERY = "Rollout_Schedule_Bigquery";
    private static final String ROLLOUT_SCHEDULE_NAME_POSTGRES = "Rollout_Schedule_Postgres";
    private static final String INSERT_QUERY = "insert into provisioning values('%s','%s','%s');";
    private static int isDeleteInsight = 0;
    private final String CLIENT_NAME_SNOWFLAKE = ("LcmClientSnowflake_" + generateHashString()).toUpperCase();
    private final String CLIENT_NAME_REDSHIFT = "LcmClientRedshift_" + generateHashString();
    private final String CLIENT_NAME_BIGQUERY = "LcmClientBigquery_" + generateHashString();
    private final String CLIENT_NAME_POSTGRES = "LcmClientPostgres_" + generateHashString();
    private GeoPushpinChartPicker geoChart;
    private String serviceProject;
    private String previousMasterSnowflake;
    private String previousMasterRedshift;
    private String previousMasterBigquery;
    private String previousMasterPostgres;


    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_LCM_TEST_CloudResource";
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, SQLException {
        String adsTableText = SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_CLOUD_RESOURCE.getName());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));
        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY, adsTableText);
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);
        addDataToProvisionTable();
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        privatekey = DATASOURCE_PRIVATE_KEY.replace("\n", "\\n");
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"precondition"})
    public void configAttributeToGeoPushpin() {
        initAttributePage().initAttribute("city").selectGeoLableType("Geo pushpin");
    }

    @Test(dependsOnMethods = {"configAttributeToGeoPushpin"}, groups = {"precondition"})
    public void runAddLoadingData() throws Throwable {
        initDiscProjectDetailPage();
        ADS_WAREHOUSE = ads.getConnectionUrl();
        DEVELOPMENT_PID = testParams.getProjectId();
        DEVELOPMENT_TITLE = projectTitle;
        createAddLoadScheduleProcess(DeployProcessForm.ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initAnalysePage().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_PUSHPIN, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_AMOUNT, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_AMOUNT, FieldType.FACT)
                .addStack(ATTR_COUNTRY).waitForReportComputing().saveInsight(INSIGHT_LCM);
        createProject();
        serviceProject = testParams.getProjectId();
        log.info("Service project ne: " + serviceProject);
    }

    @DataProvider (name = "releaseProvider")
    public Object[][] releaseProvider() {
        return new Object[][] {
                {TxtFile.GD_ENDCODE_RELEASE_SNOWFLAKE.getName(), LCM_RELEASE_SNOWFLAKE, SEGMENT_ID_SNOWFLAKE,
                        RELEASE_SCHEDULE_NAME_SNOWFLAKE, MASTER_NAME_SNOWFLAKE},
                {TxtFile.GD_ENDCODE_RELEASE_REDSHIFT.getName(), LCM_RELEASE_REDSHIFT, SEGMENT_ID_REDSHIFT,
                        RELEASE_SCHEDULE_NAME_REDSHIFT,  MASTER_NAME_REDSHIFT},
                {TxtFile.GD_ENDCODE_RELEASE_BIGQUERY.getName(), LCM_RELEASE_BIGQUERY, SEGMENT_ID_BIGQUERY,
                        RELEASE_SCHEDULE_NAME_BIGQUERY, MASTER_NAME_BIGQUERY},
                {TxtFile.GD_ENDCODE_RELEASE_POSTGRES.getName(), LCM_RELEASE_POSTGRES, SEGMENT_ID_POSTGRES,
                        RELEASE_SCHEDULE_NAME_POSTGRES, MASTER_NAME_POSTGRES}
        };
    }

    @Test(dependsOnMethods = {"runAddLoadingData"}, groups = {"precondition"}, dataProvider = "releaseProvider")
    public void runLcmReleaseProcess(String gdEncodedParam, String lcmProcess,String segmentId,
                                     String releaseScheduleName, String masterNameProject){
        END_CODE_HIDDEN_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() +
                gdEncodedParam);
        initDiscProjectDetailPage();
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(DeployProcessForm.ProcessType.LCM_RELEASE, lcmProcess);
        initDiscProjectDetailPage();
        createAndRunReleaseProcess(lcmProcess, END_CODE_HIDDEN_PARAM, segmentId, releaseScheduleName, masterNameProject);

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initDiscProjectsPage();
        projectsPage.searchProject(masterNameProject + " #1");

        assertTrue(projectsPage.hasProject(masterNameProject + " #1"), "There is no Master Project");

        projectsPage.clickOnProjectTitleLink(masterNameProject + " #1");
        createAddLoadScheduleProcess(DeployProcessForm.ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();

        assertTrue(projectDetailPage.goToAnalyze().getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "There is no insight in Master Project");
    }

    @Test(dependsOnMethods = {"runLcmReleaseProcess"})
    public void getMasterProject() {
        previousMasterSnowflake = getMasterProjectId(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_SNOWFLAKE);
        previousMasterRedshift = getMasterProjectId(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_REDSHIFT);
        previousMasterBigquery = getMasterProjectId(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_BIGQUERY);
        previousMasterPostgres = getMasterProjectId(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_POSTGRES);

    }

    @DataProvider (name = "provisionProvider")
    public Object[][] provisionProvider() {
        return new Object[][] {
                {TxtFile.GD_ENDCODE_PROVISIONING_SNOWFLAKE.getName(), LCM_PROVISIONING_SNOWFLAKE, SEGMENT_ID_SNOWFLAKE,
                        PROVISION_SCHEDULE_NAME_SNOWFLAKE, CLIENT_NAME_SNOWFLAKE},
                {TxtFile.GD_ENDCODE_PROVISIONING_REDSHIFT.getName(), LCM_PROVISIONING_REDSHIFT, SEGMENT_ID_REDSHIFT,
                        PROVISION_SCHEDULE_NAME_REDSHIFT,  CLIENT_NAME_REDSHIFT},
                {TxtFile.GD_ENDCODE_PROVISIONING_BIGQUERY.getName(), LCM_PROVISIONING_BIGQUERY, SEGMENT_ID_BIGQUERY,
                        PROVISION_SCHEDULE_NAME_BIGQUERY, CLIENT_NAME_BIGQUERY},
                {TxtFile.GD_ENDCODE_PROVISIONING_POSTGRES.getName(), LCM_PROVISIONING_POSTGRES, SEGMENT_ID_POSTGRES,
                        PROVISION_SCHEDULE_NAME_POSTGRES, CLIENT_NAME_POSTGRES}
        };
    }

    @Test(dependsOnMethods = {"getMasterProject"}, groups = {"precondition"}, dataProvider = "provisionProvider")
    public void runLcmProvisioningProcess(String gdEncodedParam, String lcmProcess,String segmentId,
                                          String provisionScheduleName, String clientProject) {
        initDiscProjectDetailPage();
        END_CODE_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() + gdEncodedParam);
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(DeployProcessForm.ProcessType.LCM_RPOVISIONING, lcmProcess);
        initDiscProjectDetailPage();
        createProcess(lcmProcess, END_CODE_PARAM, segmentId, provisionScheduleName);

        initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        projectDetailPage.getDataProcessName(lcmProcess).openSchedule(provisionScheduleName);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());
        scheduleDetail.close();

        initDiscProjectsPage();
        projectsPage.searchProject(clientProject);

        assertTrue(projectsPage.hasProject(clientProject), "There is no Client Project");

        projectsPage.clickOnProjectTitleLink(clientProject);
        createAddLoadScheduleProcess(DeployProcessForm.ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        AnalysisPage analysisPage= projectDetailPage.goToAnalyze();
        assertTrue(analysisPage.getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "There is no insight in Client Project");
    }

    @DataProvider (name = "rolloutProvider")
    public Object[][] rolloutProvider() {
        return new Object[][] {
                {TxtFile.GD_ENDCODE_ROLLOUT_SNOWFLAKE.getName(), LCM_RELEASE_SNOWFLAKE, LCM_ROLLOUT_SNOWFLAKE,
                        SEGMENT_ID_SNOWFLAKE, ROLLOUT_SCHEDULE_NAME_SNOWFLAKE, RELEASE_SCHEDULE_NAME_SNOWFLAKE, CLIENT_NAME_SNOWFLAKE,
                        MASTER_NAME_SNOWFLAKE},
                {TxtFile.GD_ENDCODE_ROLLOUT_REDSHIFT.getName(), LCM_RELEASE_REDSHIFT, LCM_ROLLOUT_REDSHIFT, SEGMENT_ID_REDSHIFT,
                        ROLLOUT_SCHEDULE_NAME_REDSHIFT, RELEASE_SCHEDULE_NAME_REDSHIFT,  CLIENT_NAME_REDSHIFT, MASTER_NAME_REDSHIFT},
                {TxtFile.GD_ENDCODE_ROLLOUT_BIGQUERY.getName(), LCM_RELEASE_BIGQUERY, LCM_ROLLOUT_BIGQUERY, SEGMENT_ID_BIGQUERY,
                        ROLLOUT_SCHEDULE_NAME_BIGQUERY, RELEASE_SCHEDULE_NAME_BIGQUERY, CLIENT_NAME_BIGQUERY, MASTER_NAME_BIGQUERY},
                {TxtFile.GD_ENDCODE_ROLLOUT_POSTGRES.getName(), LCM_RELEASE_POSTGRES, LCM_ROLLOUT_POSTGRES,SEGMENT_ID_POSTGRES,
                        ROLLOUT_SCHEDULE_NAME_POSTGRES, RELEASE_SCHEDULE_NAME_POSTGRES, CLIENT_NAME_POSTGRES, MASTER_NAME_POSTGRES}
        };
    }

    @Test(dependsOnMethods = {"runLcmProvisioningProcess"}, groups = {"precondition"}, dataProvider = "rolloutProvider")
    public void runLcmRolloutProcess(String gdEncodedParam,String lcmReleaseProcess, String lcmRolloutProcess,String segmentId,
                                     String rollOutScheduleName, String releaseScheduleName, String clientProject,
                                     String masterName) {
        isDeleteInsight += 1;
        deleteInsightOnDev();
        initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        projectDetailPage.getDataProcessName(lcmReleaseProcess).openSchedule(releaseScheduleName);
        scheduleDetail.executeSchedule();
        ((JavascriptExecutor) browser).executeScript("window.scrollBy(0,100)");
        scheduleDetail.waitForExecutionFinish().close();
        initDiscProjectsPage();
        projectsPage.searchProject(masterName + " #2");

        assertTrue(projectsPage.hasProject(masterName + " #2"), "There is no New Master Project");

        initDiscProjectDetailPage();
        END_CODE_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() + gdEncodedParam);
        sleepTightInSeconds(2);
        projectDetailPage.clickDeployButton();
        createDeployProcess(ProcessType.LCM_ROLLOUT, lcmRolloutProcess);
        initDiscProjectDetailPage();
        createProcess(lcmRolloutProcess, END_CODE_PARAM, segmentId, rollOutScheduleName);
        initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        projectDetailPage.getDataProcessName(lcmRolloutProcess).openSchedule(rollOutScheduleName);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initDiscProjectsPage().searchProject(clientProject).clickOnProjectTitleLink(clientProject);

        assertFalse(projectDetailPage.goToAnalyze().getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                    "Insight still existed");
    }

    private void deleteInsightOnDev() {
        initDiscProjectsPage();
        sleepTightInSeconds(2);
        ProjectDetailPage detailPage = projectsPage.searchProject(DEVELOPMENT_PID).clickOnProjectTitleLink(DEVELOPMENT_TITLE);
        if (isDeleteInsight == 1) {
            sleepTightInSeconds(2);
            detailPage.goToAnalyze().getPageHeader().expandInsightSelection().deleteInsight(INSIGHT_LCM);
        }
    }

    private void createAddLoadScheduleProcess(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName).selectAllDatasetsOption().schedule();
        runProcess();
    }

    private void runProcess() {
        scheduleDetail = DataloadScheduleDetail.getInstance(browser);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
    }

    private void createDeployProcess(DeployProcessForm.ProcessType processType, String processName) {
        DeployProcessForm deployProcess = DeployProcessForm.getInstance(browser);
        deployProcess.scrollToSelectProcessType(processType, 10000).enterProcessName(processName).submit();
    }

    private void createAndRunReleaseProcess(String processTypeName, String endcodeParam, String segmentId,
                                            String releaseScheduleName, String masterName) {
        addParamScheduleProcess(processTypeName, segmentId, masterName);
        createReleaseProcess(endcodeParam, releaseScheduleName);
        runProcess();
    }

    private void createProcess(String processTypeName, String endcodeParam, String segmentId,
                                     String provisionScheduleName) {
        addParamScheduleProcess(processTypeName, segmentId, "");
        createProcess(endcodeParam, provisionScheduleName);
    }

    private void createReleaseProcess(String endcodeParam, String releaseScheduleName) {
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_HIDEN_PARAMS.getParamName(), endcodeParam);
        scheduleForm.addParameter(LcmDirectoryConfiguration.TOKEN_ID.getParamName(), testParams.getAuthorizationToken());
        scheduleForm.enterScheduleName(releaseScheduleName);
        scheduleForm.schedule();
    }
    
    private void createProcess(String endcodeParam, String provisionScheduleName) {
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_PARAM.getParamName(), endcodeParam);
        scheduleForm.enterScheduleName(provisionScheduleName);
        scheduleForm.schedule();
    }

    private void addDataToProvisionTable() throws SQLException {
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        ConnectionInfo connectionInfoSnowflake = dataSourceUtils.createSnowflakeConnectionInfo("ATT_LCM_TEST", DatabaseType.SNOWFLAKE);
        ConnectionInfo connectionInfoRedshift = dataSourceUtils.createRedshiftConnectionInfo("dev", DatabaseType.REDSHIFT, "att_lcm_test");
        ConnectionInfo connectionInfoBigquery = dataSourceUtils.createBigQueryConnectionInfo("gdc-us-dev", DatabaseType.BIGQUERY,"att_lcm_test");
        ConnectionInfo connectionInfoPostgres = dataSourceUtils.createPostGreConnectionInfo("qa", DatabaseType.POSTGRE,"public");

        postgreUtils = new PostgreUtils(connectionInfoPostgres);
        snowflakeUtils = new SnowflakeUtils(connectionInfoSnowflake);
        redshiftUtils = new RedshiftUtils(connectionInfoRedshift);
        bigqueryUtils = new BigQueryUtils(connectionInfoBigquery);

        snowflakeUtils.executeSql(format(INSERT_QUERY, CLIENT_NAME_SNOWFLAKE, SEGMENT_ID_SNOWFLAKE, CLIENT_NAME_SNOWFLAKE));

        redshiftUtils.executeCommandsForSpecificWarehouse();
        redshiftUtils.executeSql(format(INSERT_QUERY, CLIENT_NAME_REDSHIFT, SEGMENT_ID_REDSHIFT, CLIENT_NAME_REDSHIFT));

        bigqueryUtils.executeSql(format(INSERT_QUERY, CLIENT_NAME_BIGQUERY, SEGMENT_ID_BIGQUERY, CLIENT_NAME_BIGQUERY));

        postgreUtils.executeCommandsForSpecificWarehouse();
        postgreUtils.executeSql(format(INSERT_QUERY, CLIENT_NAME_POSTGRES, SEGMENT_ID_POSTGRES, CLIENT_NAME_POSTGRES));
    }

    private void addParamScheduleProcess(String processTypeName, String segmentId, String masterName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName);
        scheduleForm = CreateScheduleForm.getInstance(browser);
        scheduleForm.addParameter(LcmDirectoryConfiguration.CLIENT_GDC_HOSTNAME.getParamName(), testParams.getHost())
                .addSecureParameter(LcmDirectoryConfiguration.GDC_PASSWORD.getParamName(), testParams.getPassword())
                .addParameter(LcmDirectoryConfiguration.GDC_USERNAME.getParamName(), testParams.getUser())
                .addParameter(LcmDirectoryConfiguration.ORGANIZATION.getParamName(), testParams.getUserDomain())
                .addSecureParameter(LcmDirectoryConfiguration.ADS_PASSWORD.getParamName(), testParams.getPassword())
                .addParameter(LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamName(),
                        LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamName(),
                        LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.EXCLUDE_FACT_RULE.getParamName(),
                        LcmDirectoryConfiguration.EXCLUDE_FACT_RULE.getParamValue())
                .addParameter(LcmDirectoryConfiguration.DATA_PRODUCT.getParamName(),
                        LcmDirectoryConfiguration.DATA_PRODUCT.getParamValue())
                .addParameter(LcmDirectoryConfiguration.TRANSFER_ALL.getParamName(),
                        LcmDirectoryConfiguration.TRANSFER_ALL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.SEGMENT_ID.getParamName(), segmentId)
                .addParameter(LcmDirectoryConfiguration.JDBC_URL.getParamName(), ADS_WAREHOUSE)
                .addParameter(LcmDirectoryConfiguration.DEVELOPMENT_PID.getParamName(), DEVELOPMENT_PID)
                .addParameter(LcmDirectoryConfiguration.MASTER_PROJECT_NAME.getParamName(), masterName);
        if (segmentId == SEGMENT_ID_SNOWFLAKE) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.SNOWFLAKE_USER_NAME.getParamName(), testParams.getSnowflakeUserName())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_PASSWORD.getParamName(), testParams.getSnowflakePassword())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_URL.getParamName(), testParams.getSnowflakeJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_DATABASE.getParamName(), "ATT_LCM_TEST")
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_WAREHOUSE.getParamName(), "ATT_WAREHOUSE")
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_SCHEMA.getParamName(), "PUBLIC");
        }
        if (segmentId == SEGMENT_ID_REDSHIFT) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.REDSHIFT_USER_NAME.getParamName(), testParams.getRedshiftUserName())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_PASSWORD.getParamName(), testParams.getRedshiftPassword())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_DATABASE.getParamName(), "dev")
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_URL.getParamName(), testParams.getRedshiftJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_SCHEMA.getParamName(), "att_lcm_test");
        }
        if (segmentId == SEGMENT_ID_BIGQUERY) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.BIGQUERY_CLIENT_EMAIL.getParamName(), testParams.getBigqueryClientEmail())
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_PRIVATE_KEY.getParamName(), privatekey)
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_URL.getParamName(), testParams.getPostgreJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_SCHEMA.getParamName(), "att_lcm_test")
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_PROJECT.getParamName(), "gdc-us-dev");
        }
        if (segmentId == SEGMENT_ID_POSTGRES) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.POSTGRE_URL.getParamName(), testParams.getPostgreJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_SSL_MODE.getParamName(), "prefer")
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_DATABASE.getParamName(), "qa")
                    .addParameter(LcmDirectoryConfiguration.POSTGRES_USER_NAME.getParamName(), testParams.getPostgreUserName())
                    .addParameter(LcmDirectoryConfiguration.POSTGRES_PASSWORD.getParamName(), testParams.getPostgrePassword())
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_SCHEMA.getParamName(), "public");
        }
    }

    public String getMasterProjetIdBySegment(String segment) {
        return getMasterProjectId(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), segment);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void removeAdsInstance() throws ParseException, JSONException, IOException {
        adsHelper = new AdsHelper(new RestClient(getProfile(DOMAIN)), testParams.getProjectId());
        adsHelper.removeAds(ads);

        if(getMasterProjetIdBySegment(SEGMENT_ID_SNOWFLAKE) != previousMasterSnowflake) {
            deleteProject(getProfile(DOMAIN),previousMasterSnowflake);
        }

        if(getMasterProjetIdBySegment(SEGMENT_ID_REDSHIFT) != previousMasterRedshift) {
            deleteProject(getProfile(DOMAIN),previousMasterRedshift);
        }

        if(getMasterProjetIdBySegment(SEGMENT_ID_BIGQUERY) != previousMasterBigquery) {
            deleteProject(getProfile(DOMAIN),previousMasterBigquery);
        }

        if(getMasterProjetIdBySegment(SEGMENT_ID_POSTGRES) != previousMasterPostgres) {
            deleteProject(getProfile(DOMAIN),previousMasterPostgres);
        }

        deleteSegment(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_SNOWFLAKE);
        deleteSegment(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_REDSHIFT);
        deleteSegment(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_BIGQUERY);
        deleteSegment(new RestClient(getProfile(DOMAIN)), testParams.getUserDomain(), SEGMENT_ID_POSTGRES);
    }
}
