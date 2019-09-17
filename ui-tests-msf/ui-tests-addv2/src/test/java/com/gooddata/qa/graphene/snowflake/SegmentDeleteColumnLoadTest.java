package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_DELETED_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_NO_SYSTEM;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_DELETED_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_NO_SYSTEM;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.VARCHAR_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDeletedClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyDeleted;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.md.Attribute;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import com.gooddata.qa.utils.snowflake.DataSourceUtils;
import com.gooddata.qa.utils.snowflake.DatabaseColumn;
import com.gooddata.qa.utils.snowflake.DatabaseType;
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class SegmentDeleteColumnLoadTest extends AbstractADDProcessTest {
    private Project serviceProject;
    private String clientProjectId1;
    private String clientProjectId2;
    private Project project1;
    private Project project2;
    private String dataSourceId;
    private String devProjectId;
    private String serviceProjectId;
    private LdmModel ldmmodel;
    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private LcmBrickFlowBuilder lcmBrickFlowBuilder;

    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String time;
    private String timeSecond;
    private SnowflakeUtils snowflakeUtils;
    private DataSourceUtils dataSourceUtils;
    private ProcessUtils domainProcessUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws JSONException, IOException, SQLException {
        createLCM();
        ConnectionInfo connectionInfo = dataSourceUtils.createSnowflakeConnectionInfo(DATABASE_NAME, DatabaseType.SNOWFLAKE);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceUtils.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        log.info("dataSourceId:" + dataSourceId);
        // setUp Model projects
        Dataset datasetcustomer = new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME)
                .withFacts(FACT_AGE);
        Dataset datasetCustomerNosystem = new Dataset(DATASET_CUSTOMERS_NO_SYSTEM).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerOnlyClientId = new Dataset(DATASET_CUSTOMERS_ONLY_CLIENTID).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerOnlyTimestamp = new Dataset(DATASET_CUSTOMERS_ONLY_TIMESTAMP).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerOnlyDeleted = new Dataset(DATASET_CUSTOMERS_ONLY_DELETED).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampClientId = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerDeletedClientId = new Dataset(DATASET_CUSTOMERS_DELETED_CLIENTID).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampDeleted = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        ldmmodel = new LdmModel();
        // create MAQL
        setupMaql(ldmmodel.withDataset(datasetcustomer).withDataset(datasetCustomerNosystem)
                .withDataset(datasetCustomerOnlyClientId).withDataset(datasetCustomerOnlyTimestamp)
                .withDataset(datasetCustomerOnlyDeleted).withDataset(datasetCustomerTimestampClientId)
                .withDataset(datasetCustomerDeletedClientId).withDataset(datasetCustomerTimestampDeleted)
                .buildMaqlUsingPrimaryKey());

        // create Tables Snowflake
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumn1 = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnNoSystem = asList(custkeyColumn, nameColumn, ageColumn);
        List<DatabaseColumn> listColumnOnlyClientId = asList(custkeyColumn, nameColumn, ageColumn, clientIdColumn);
        List<DatabaseColumn> listColumnOnlyTimestamp = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn);
        List<DatabaseColumn> listColumnOnlyDeleted = asList(custkeyColumn, nameColumn, ageColumn, deletedColumn);
        List<DatabaseColumn> listColumnTimestampClientId = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnDeletedClientId = asList(custkeyColumn, nameColumn, ageColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnTimestampDeleted = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                deletedColumn);
        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumn1);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_NO_SYSTEM, listColumnNoSystem);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_ONLY_CLIENTID, listColumnOnlyClientId);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_ONLY_TIMESTAMP, listColumnOnlyTimestamp);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_ONLY_DELETED, listColumnOnlyDeleted);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listColumnTimestampClientId);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_DELETED_CLIENTID, listColumnDeletedClientId);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        time = parseToTimeStampFormat(lastSuccessful);
        timeSecond = parseToTimeStampFormat(lastSuccessful.plusSeconds(5));
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS,
                        datasetNormal().rows(asList("CUS1", "User", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS2", "User", "28", time, "false", CLIENT_ID_1))
                                .rows(asList("CUS2", "User", "28", timeSecond, "TRUE", CLIENT_ID_1))
                                .rows(asList("CUS3", "User", "28", time, "FALSE", CLIENT_ID_1)),
                        DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_DELETED_CLIENTID,
                        datasetDeletedClientId().rows(asList("CUS1B", "User2", "28", "0", CLIENT_ID_1))
                                .rows(asList("CUS2B", "User2", "28", "false", CLIENT_ID_1))
                                .rows(asList("CUS2B", "User2", "28", "1", CLIENT_ID_1))
                                .rows(asList("CUS3B", "User2", "28", "0", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_DELETED_CLIENTID, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkFirstLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custkeyValues = new ArrayList<String>();
        // In the first load, when loadDataFrom not exist, only query and upload records
        // have x__deleted = false
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= 0");
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, null, conditions, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataFirstLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_DELETED,
                        datasetOnlyDeleted().rows(asList("CUS1", "User", "28", "0")).rows(asList("CUS2", "User", "28", "false"))
                                .rows(asList("CUS2", "User", "28", "1")).rows(asList("CUS3", "User", "28", "f")),
                        DATASET_CUSTOMERS_ONLY_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted().rows(asList("CUS1B", "Phong", "28", time, "0"))
                        .rows(asList("CUS2B", "Phong", "28", time, "false"))
                        .rows(asList("CUS2B", "Phong", "28", timeSecond, "true")).rows(asList("CUS3B", "Phong", "28", time, "f")),
                        DATASET_CUSTOMERS_TIMESTAMP_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadHasClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkFirstLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {

        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= 0");
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId2, CLIENT_ID_2, dataset)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadHasClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS,
                datasetNormal().rows(asList("CUS3", "User", "28", timeSecond, "TRUE", CLIENT_ID_1))
                        .rows(asList("CUS4", "User", "28", timeSecond, "0", CLIENT_ID_1)),
                DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadNoClientId" }, dataProvider = "dataSecondLoadHasClientId")
    public void checkSecondLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custkeyValues = new ArrayList<String>();
        // In the second load, take all IDs that X_DELETED <> 1
        String sqlStr = String.format("select %s from %s where %s NOT IN (Select %s from %s where %s = %s) ", column, table,
                column, column, table, COLUMN_X_DELETED, "1");
        ResultSet result = snowflakeUtils.getSqlResult(sqlStr);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadNoClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS_TIMESTAMP_DELETED,
                datasetTimeStampDeleted().rows(asList("CUS3B", "Phong", "28", timeSecond, "t"))
                        .rows(asList("CUS4B", "Phong", "28", timeSecond, "false")),
                DATASET_CUSTOMERS_TIMESTAMP_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkSecondLoadHasClientId" }, dataProvider = "dataSecondLoadNoClientId")
    public void checkSecondLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());

        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custkeyValues = new ArrayList<String>();
        String sqlStr = String.format("select %s from %s where %s NOT IN (Select %s from %s where %s = %s) ", column, table,
                column, column, table, COLUMN_X_DELETED, "1");
        ResultSet result = snowflakeUtils.getSqlResult(sqlStr);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId2, CLIENT_ID_2, dataset, lastSuccessful)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        domainRestClient.getProcessService().removeProcess(dataloadProcess);
        lcmBrickFlowBuilder.destroy();
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
        snowflakeUtils.closeSnowflakeConnection();
    }

    private void createLCM() throws ParseException, IOException {
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        serviceProjectId = lcmBrickFlowBuilder.getLCMServiceProject().getServiceProjectId();
        serviceProject = domainRestClient.getProjectService().getProjectById(serviceProjectId);
        devProjectId = testParams.getProjectId();
        log.info("dev project : " + devProjectId);
        clientProjectId1 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_1);
        project1 = domainRestClient.getProjectService().getProjectById(clientProjectId1);
        log.info("client 1 : " + clientProjectId1);
        clientProjectId2 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_2);
        project2 = domainRestClient.getProjectService().getProjectById(clientProjectId2);
        log.info("client 2 : " + clientProjectId2);
        lcmBrickFlowBuilder.setDevelopProject(devProjectId).setSegmentId(SEGMENT_ID).setClient(CLIENT_ID_1, clientProjectId1)
                .setClient(CLIENT_ID_2, clientProjectId2).buildLcmProjectParameters();
        lcmBrickFlowBuilder.runLcmFlow();
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId1);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId2);
    }
}
