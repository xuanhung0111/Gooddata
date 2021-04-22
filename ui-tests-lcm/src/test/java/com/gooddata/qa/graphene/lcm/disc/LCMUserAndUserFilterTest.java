package com.gooddata.qa.graphene.lcm.disc;

import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;

import com.gooddata.qa.graphene.enums.LcmDirectoryConfiguration;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;

import com.gooddata.qa.graphene.enums.process.Parameter;

import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.AttributeElement;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LCMUserAndUserFilterTest extends AbstractDataloadProcessTest {
    public DataloadScheduleDetail scheduleDetail;
    public CreateScheduleForm scheduleForm;
    public DataSourceUtils dataSourceUtils;
    public PostgreUtils postgreUtils;
    public SnowflakeUtils snowflakeUtils;
    public RedshiftUtils redshiftUtils;
    public BigQueryUtils bigqueryUtils;
    private String DATASOURCE_PRIVATE_KEY;
    private String DEVELOPMENT_PID;
    private String privatekey;
    private String snowflakeUser;
    private String redshiftUser;
    private String bigqueryUser;
    private String postgresUser;

    private final String LCM_USER_SNOWFLAKE = "LCM_SNOWFLAKE_USER_" + generateHashString();
    private final String LCM_USER_REDSHIFT = "LCM_REDSHIFT_USER_" + generateHashString();
    private final String LCM_USER_BIGQUERY = "LCM_BIGQUERY_USER_" + generateHashString();
    private final String LCM_USER_POSTGRES = "LCM_POSTGRE_USER_" + generateHashString();
    private final String LCM_USERFILTER_SNOWFLAKE = "LCM_USERFILTER_SNOWFLAKE_" + generateHashString();
    private final String LCM_USERFILTER_REDSHIFT = "LCM_USERFILTER_REDSHIFT_" + generateHashString();
    private final String LCM_USERFILTER_BIGQUERY = "LCM_USERFILTER_BIGQUERY_" + generateHashString();
    private final String LCM_USERFILTER_POSTGRES = "LCM_USERFILTER_POSTGRE_" + generateHashString();

    private static String END_CODE_HIDDEN_PARAM;

    private static final String USER_SCHEDULE_NAME_SNOWFLAKE = "User_Schedule_Snowflake";
    private static final String USER_SCHEDULE_NAME_REDSHIFT = "User_Schedule_Redshift";
    private static final String USER_SCHEDULE_NAME_BIGQUERY = "User_Schedule_Bigquery";
    private static final String USER_SCHEDULE_NAME_POSTGRES = "User_Schedule_Postgres";
    private static final String USER_FILTER_SCHEDULE_NAME_SNOWFLAKE = "UserFilter_Schedule_Snowflake";
    private static final String USER_FILTER_SCHEDULE_NAME_REDSHIFT = "UserFilter_Schedule_Redshift";
    private static final String USER_FILTER_SCHEDULE_NAME_BIGQUERY = "UserFilter_Schedule_Bigquery";
    private static final String USER_FILTER_SCHEDULE_NAME_POSTGRES = "UserFilter_Schedule_Postgres";
    private static final String USER_BRICK_QUERY = "insert into userbrick values('%s','%s','%s');";
    private static final String USER_FILTER_BRICK_QUERY = "insert into usersfilter values('%s','%s','%s');";
    private Attribute cityAttribute;
    private AttributeElement cityValue;
    private static final String EXPRESSION = "[%s] IN ([%s])";
    private static final String ADMIN_ROLE = "AdminRole";
    private static final String USER_FILTER_DATA = "Aberdeen";
    private GeoPushpinChartPicker geoChart;
    private String serviceProject;
    private UserManagementRestRequest userManagementRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_LCM_TEST_CloudResource";
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws Throwable {
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        privatekey = DATASOURCE_PRIVATE_KEY.replace("\n", "\\n");
        userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        snowflakeUser = createUser();
        redshiftUser = createUser();
        bigqueryUser = createUser();
        postgresUser = createUser();
        String adsTableText = SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_CLOUD_RESOURCE.getName());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));
        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY, adsTableText);
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);
        serviceProject = testParams.getProjectId();
        addDataToTable();
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"precondition"})
    public void runAddLoadingData() throws Throwable {
        initDiscProjectDetailPage();
        createAddLoadScheduleProcess(DeployProcessForm.ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());
        scheduleDetail.close();
    }

    @DataProvider(name = "userProvider")
    public Object[][] userProvider() {
        return new Object[][] {
                {TxtFile.GD_ENDCODE_USER_SNOWFLAKE.getName(), LCM_USER_SNOWFLAKE, USER_SCHEDULE_NAME_SNOWFLAKE, snowflakeUser},
                {TxtFile.GD_ENDCODE_USER_REDSHIFT.getName(), LCM_USER_REDSHIFT, USER_SCHEDULE_NAME_REDSHIFT, redshiftUser},
                {TxtFile.GD_ENDCODE_USER_BIGQUERY.getName(), LCM_USER_BIGQUERY, USER_SCHEDULE_NAME_BIGQUERY, bigqueryUser},
                {TxtFile.GD_ENDCODE_USER_POSTGRES.getName(), LCM_USER_POSTGRES, USER_SCHEDULE_NAME_POSTGRES, postgresUser}
        };
    }

    @Test(dependsOnMethods = {"runAddLoadingData"}, dataProvider = "userProvider")
    public void runLcmUserBrickProcess(String gdEncodedParam, String lcmProcess, String userScheduleName, String user){
        END_CODE_HIDDEN_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() +
                gdEncodedParam);
        initDiscProjectDetailPage();
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(DeployProcessForm.ProcessType.LCM_USER, lcmProcess);
        initDiscProjectDetailPage();
        createAndRunProcess(lcmProcess, END_CODE_HIDDEN_PARAM, userScheduleName);
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());
        scheduleDetail.close();
        assertTrue(initProjectsAndUsersPage().isUserDisplayedInList(user), user + " has not been active");
    }

    @DataProvider(name = "userFilterProvider")
    public Object[][] userFilterProvider() {
        return new Object[][] {
                {TxtFile.GD_ENDCODE_USERFILTER_SNOWFLAKE.getName(), LCM_USERFILTER_SNOWFLAKE, USER_FILTER_SCHEDULE_NAME_SNOWFLAKE, snowflakeUser},
                {TxtFile.GD_ENDCODE_USERFILTER_REDSHIFT.getName(), LCM_USERFILTER_REDSHIFT, USER_FILTER_SCHEDULE_NAME_REDSHIFT, redshiftUser},
                {TxtFile.GD_ENDCODE_USERFILTER_BIGQUERY.getName(), LCM_USERFILTER_BIGQUERY, USER_FILTER_SCHEDULE_NAME_BIGQUERY, bigqueryUser},
                {TxtFile.GD_ENDCODE_USERFILTER_POSTGRES.getName(), LCM_USERFILTER_POSTGRES, USER_FILTER_SCHEDULE_NAME_POSTGRES, postgresUser}
        };
    }
    @Test(dependsOnMethods = {"runLcmUserBrickProcess"}, dataProvider = "userFilterProvider")
    public void runLcmUserFilterBrickProcess(String gdEncodedParam, String lcmProcess, String userFilterScheduleName, String user){
        END_CODE_HIDDEN_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() + gdEncodedParam);
        initDiscProjectDetailPage();
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(DeployProcessForm.ProcessType.LCM_USER_FILTER, lcmProcess);
        initDiscProjectDetailPage();
        createProcess(lcmProcess, END_CODE_HIDDEN_PARAM, userFilterScheduleName);
        initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        projectDetailPage.getDataProcessName(lcmProcess).openSchedule(userFilterScheduleName);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());
        scheduleDetail.close();
        UserProfilePage userProfilePage = initProjectsAndUsersPage().openUserProfile(user);
        sleepTightInSeconds(2);
        assertEquals(userProfilePage.getAvailableMufExpressions(), singletonList(format(EXPRESSION, "city",  USER_FILTER_DATA)));
    }

    private void createDeployProcess(DeployProcessForm.ProcessType processType, String processName) {
        DeployProcessForm deployProcess = DeployProcessForm.getInstance(browser);
        deployProcess.scrollToSelectProcessType(processType, 10000).enterProcessName(processName).submit();
    }

    private void createAndRunProcess(String processTypeName, String endcodeParam, String scheduleName) {
        addParamScheduleProcess(processTypeName);
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_HIDEN_PARAMS.getParamName(), endcodeParam);
        scheduleForm.enterScheduleName(scheduleName);
        addCloudResourceParams(scheduleName);
        scheduleForm.schedule();
        runProcess();
    }

    private void createProcess(String processTypeName, String endcodeParam, String scheduleName) {
        addParamScheduleProcess(processTypeName);
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_HIDEN_PARAMS.getParamName(), endcodeParam);
        scheduleForm.enterScheduleName(scheduleName);
        addCloudResourceParams(scheduleName);
        scheduleForm.schedule();
    }

    private void runProcess() {
        scheduleDetail = DataloadScheduleDetail.getInstance(browser);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
    }

    private void createAddLoadScheduleProcess(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName).selectAllDatasetsOption().schedule();
        runProcess();
    }

    private String createUser() throws IOException {
        String dynamicUser = createDynamicUserFrom(testParams.getUser().replace("@", "+dynamic@"));
        userManagementRestRequest.createUser(testParams.getUserDomain(), dynamicUser, testParams.getPassword());
        return dynamicUser;
    }

    private void addDataToTable() throws SQLException {
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        ConnectionInfo connectionInfoSnowflake = dataSourceUtils.createSnowflakeConnectionInfo("ATT_LCM_TEST", DatabaseType.SNOWFLAKE);
        ConnectionInfo connectionInfoRedshift = dataSourceUtils.createRedshiftConnectionInfo("dev", DatabaseType.REDSHIFT, "att_lcm_test");
        ConnectionInfo connectionInfoBigquery = dataSourceUtils.createBigQueryConnectionInfo("gdc-us-dev", DatabaseType.BIGQUERY,"att_lcm_test");
        ConnectionInfo connectionInfoPostgres = dataSourceUtils.createPostGreConnectionInfo("qa", DatabaseType.POSTGRE,"public");

        postgreUtils = new PostgreUtils(connectionInfoPostgres);
        snowflakeUtils = new SnowflakeUtils(connectionInfoSnowflake);
        redshiftUtils = new RedshiftUtils(connectionInfoRedshift);
        bigqueryUtils = new BigQueryUtils(connectionInfoBigquery);

        snowflakeUtils.executeSql(format(USER_BRICK_QUERY, snowflakeUser, serviceProject, ADMIN_ROLE));

        redshiftUtils.executeCommandsForSpecificWarehouse();
        redshiftUtils.executeSql(format(USER_BRICK_QUERY, redshiftUser, serviceProject, ADMIN_ROLE));

        bigqueryUtils.executeSql(format(USER_BRICK_QUERY, bigqueryUser, serviceProject, ADMIN_ROLE));

        postgreUtils.executeCommandsForSpecificWarehouse();
        postgreUtils.executeSql(format(USER_BRICK_QUERY, postgresUser, serviceProject, ADMIN_ROLE));

        snowflakeUtils.executeSql(format(USER_FILTER_BRICK_QUERY, snowflakeUser, USER_FILTER_DATA, serviceProject));

        redshiftUtils.executeCommandsForSpecificWarehouse();
        redshiftUtils.executeSql(format(USER_FILTER_BRICK_QUERY, redshiftUser, USER_FILTER_DATA, serviceProject));

        bigqueryUtils.executeSql(format(USER_FILTER_BRICK_QUERY, bigqueryUser, USER_FILTER_DATA, serviceProject));

        postgreUtils.executeCommandsForSpecificWarehouse();
        postgreUtils.executeSql(format(USER_FILTER_BRICK_QUERY, postgresUser, USER_FILTER_DATA, serviceProject));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpLCMTest() throws ParseException, JSONException, IOException {
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), snowflakeUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), redshiftUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), bigqueryUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), postgresUser);
    }

    private void addCloudResourceParams(String scheduleName) {
        if (scheduleName == USER_SCHEDULE_NAME_SNOWFLAKE || scheduleName == USER_FILTER_SCHEDULE_NAME_SNOWFLAKE) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.SNOWFLAKE_USER_NAME.getParamName(), testParams.getSnowflakeUserName())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_PASSWORD.getParamName(), testParams.getSnowflakePassword())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_URL.getParamName(), testParams.getSnowflakeJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_DATABASE.getParamName(), "ATT_LCM_TEST")
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_WAREHOUSE.getParamName(), "ATT_WAREHOUSE")
                    .addParameter(LcmDirectoryConfiguration.SNOWFLAKE_SCHEMA.getParamName(), "PUBLIC")
                    .addParameter(LcmDirectoryConfiguration.LOGIN_EMAIL.getParamName(), snowflakeUser);
        }
        if (scheduleName == USER_SCHEDULE_NAME_REDSHIFT || scheduleName == USER_FILTER_SCHEDULE_NAME_REDSHIFT) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.REDSHIFT_USER_NAME.getParamName(), testParams.getRedshiftUserName())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_PASSWORD.getParamName(), testParams.getRedshiftPassword())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_DATABASE.getParamName(), "dev")
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_URL.getParamName(), testParams.getRedshiftJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.REDSHIFT_SCHEMA.getParamName(), "att_lcm_test")
                    .addParameter(LcmDirectoryConfiguration.LOGIN_EMAIL.getParamName(), redshiftUser);
        }
        if (scheduleName == USER_SCHEDULE_NAME_BIGQUERY || scheduleName == USER_FILTER_SCHEDULE_NAME_BIGQUERY) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.BIGQUERY_CLIENT_EMAIL.getParamName(), testParams.getBigqueryClientEmail())
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_PRIVATE_KEY.getParamName(), privatekey)
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_URL.getParamName(), testParams.getPostgreJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_SCHEMA.getParamName(), "att_lcm_test")
                    .addParameter(LcmDirectoryConfiguration.BIGQUERY_PROJECT.getParamName(), "gdc-us-dev")
                    .addParameter(LcmDirectoryConfiguration.LOGIN_EMAIL.getParamName(), bigqueryUser);
        }
        if (scheduleName == USER_SCHEDULE_NAME_POSTGRES || scheduleName == USER_FILTER_SCHEDULE_NAME_POSTGRES) {
            scheduleForm.addParameter(LcmDirectoryConfiguration.POSTGRE_URL.getParamName(), testParams.getPostgreJdbcUrl())
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_SSL_MODE.getParamName(), "prefer")
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_DATABASE.getParamName(), "qa")
                    .addParameter(LcmDirectoryConfiguration.POSTGRES_USER_NAME.getParamName(), testParams.getPostgreUserName())
                    .addParameter(LcmDirectoryConfiguration.POSTGRES_PASSWORD.getParamName(), testParams.getPostgrePassword())
                    .addParameter(LcmDirectoryConfiguration.POSTGRE_SCHEMA.getParamName(), "public")
                    .addParameter(LcmDirectoryConfiguration.LOGIN_EMAIL.getParamName(), postgresUser);
        }
    }

    private void addParamScheduleProcess(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName);
        scheduleForm = CreateScheduleForm.getInstance(browser);
        scheduleForm.addParameter(LcmDirectoryConfiguration.CLIENT_GDC_HOSTNAME.getParamName(), testParams.getHost())
                .addParameter(LcmDirectoryConfiguration.ORGANIZATION.getParamName(), testParams.getUserDomain())
                .addParameter(LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamName(),
                        LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamName(),
                        LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamValue());
    }
}
