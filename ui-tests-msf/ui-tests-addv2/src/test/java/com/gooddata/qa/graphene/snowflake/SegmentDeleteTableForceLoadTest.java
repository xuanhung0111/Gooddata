package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDelete;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDeleteTimeStampDeleted;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDeleteTimeStampClientId;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DELETED_TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DELETED_TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DELETED_TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.VARCHAR_TYPE;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
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
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class SegmentDeleteTableForceLoadTest extends AbstractADDProcessTest {
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
    private String timeForceFullLoad;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;
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
        ConnectionInfo connectionInfo = dataSourceUtils.createDefaultConnectionInfo(DATABASE_NAME);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceUtils.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        log.info("dataSourceId:" + dataSourceId);
        // setUp Model projects
        Dataset datasetcustomer = new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME)
                .withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampClientId = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampDeleted = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        ldmmodel = new LdmModel();
        // create MAQL
        setupMaql(ldmmodel.withDataset(datasetcustomer).withDataset(datasetCustomerTimestampClientId)
                .withDataset(datasetCustomerTimestampDeleted).buildMaqlUsingPrimaryKey());

        // create Tables Snowflake
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumnNormal = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listDeleteColumnNormal = asList(custkeyColumn, timestampColumn, clientIdColumn);
        List<DatabaseColumn> listColumnTimestampClientId = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                clientIdColumn);
        List<DatabaseColumn> listDeleteColumnTimestampClientId = asList(custkeyColumn, timestampColumn, clientIdColumn);
        List<DatabaseColumn> listColumnTimestampDeleted = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                deletedColumn);
        List<DatabaseColumn> listDeleteColumnTimestampDeleted = asList(custkeyColumn, timestampColumn);
        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumnNormal);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listColumnTimestampClientId);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);
        snowflakeUtils.createTable(DELETED_TABLE_CUSTOMERS, listDeleteColumnNormal);
        snowflakeUtils.createTable(DELETED_TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listDeleteColumnTimestampClientId);
        snowflakeUtils.createTable(DELETED_TABLE_CUSTOMERS_TIMESTAMP_DELETED, listDeleteColumnTimestampDeleted);
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        timeForceFullLoad = parseToTimeStampFormat(lastSuccessful);
        timeLoadFrom = parseToTimeStampFormat(lastSuccessful.plusSeconds(3));
        timeLoadTo = parseToTimeStampFormat(lastSuccessful.plusSeconds(5));
        timeOverRange = parseToTimeStampFormat(lastSuccessful.plusSeconds(7));
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS,
                        datasetNormal().rows(asList("CUS1", "User", "28", timeForceFullLoad, "0", CLIENT_ID_1))
                                .rows(asList("CUS2", "User", "28", timeForceFullLoad, "false", CLIENT_ID_1))
                                .rows(asList("CUS3", "User", "28", timeForceFullLoad, "FALSE", CLIENT_ID_1)),
                        DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY, },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID,
                        datasetTimeStampClientId().rows(asList("CUS1B", "User2", "28", timeForceFullLoad, CLIENT_ID_1))
                                .rows(asList("CUS2B", "User2", "28", timeForceFullLoad, CLIENT_ID_1))
                                .rows(asList("CUS3B", "User2", "28", timeForceFullLoad, CLIENT_ID_1)),
                        DATASET_CUSTOMERS_TIMESTAMP_CLIENTID, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkFirstLoadWithClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecords(table, column, LIMIT_RECORDS);
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
        return new Object[][] { { TABLE_CUSTOMERS_TIMESTAMP_DELETED,
                datasetTimeStampDeleted().rows(asList("CUS1C", "User3", "28", timeForceFullLoad, "0"))
                        .rows(asList("CUS2C", "User3", "28", timeForceFullLoad, "false"))
                        .rows(asList("CUS3C", "User3", "28", timeForceFullLoad, "0")),
                DATASET_CUSTOMERS_TIMESTAMP_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadWithClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkFirstLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecords(table, column, LIMIT_RECORDS);
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
        return new Object[][] {
                { TABLE_CUSTOMERS, DELETED_TABLE_CUSTOMERS,
                        datasetNormal().rows(asList("CUS4", "User", "28", timeLoadFrom, "0", CLIENT_ID_1))
                                .rows(asList("CUS5", "User", "28", timeLoadTo, "false", CLIENT_ID_1))
                                .rows(asList("CUS6", "User", "28", timeOverRange, "false", CLIENT_ID_1)),
                        datasetDelete().rows(asList("CUS1", timeLoadTo, CLIENT_ID_1))
                                .rows(asList("CUS2", timeLoadTo, CLIENT_ID_1)),
                        DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, DELETED_TABLE_CUSTOMERS_TIMESTAMP_CLIENTID,
                        datasetTimeStampClientId().rows(asList("CUS4B", "User2", "28", timeLoadFrom, CLIENT_ID_1))
                                .rows(asList("CUS5B", "User2", "28", timeLoadTo, CLIENT_ID_1))
                                .rows(asList("CUS6B", "User", "28", timeOverRange, "false", CLIENT_ID_1)),
                        datasetDeleteTimeStampClientId().rows(asList("CUS1B", timeLoadFrom, CLIENT_ID_1))
                                .rows(asList("CUS2B", timeLoadTo, CLIENT_ID_1)),
                        DATASET_CUSTOMERS_TIMESTAMP_CLIENTID, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadNoClientId" }, dataProvider = "dataSecondLoadHasClientId")
    public void checkSecondLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String column, String attribute) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("BETWEEN '%s' AND '%s'", parseToTimeStampFormat(lastSuccessful), timeLoadTo);
        conditions.add(Pair.of(COLUMN_X_TIMESTAMP, conditionString));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadNoClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS_TIMESTAMP_DELETED, DELETED_TABLE_CUSTOMERS_TIMESTAMP_DELETED,
                datasetTimeStampDeleted().rows(asList("CUS4C", "User3", "28", timeLoadFrom, "0"))
                        .rows(asList("CUS5C", "User3", "28", timeLoadTo, "false"))
                        .rows(asList("CUS6C", "User3", "28", timeOverRange, "0")),
                datasetDeleteTimeStampDeleted().rows(asList("CUS1C", timeLoadFrom)).rows(asList("CUS2C", timeLoadTo)),
                DATASET_CUSTOMERS_TIMESTAMP_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkSecondLoadWithClientId" }, dataProvider = "dataSecondLoadNoClientId")
    public void checkSecondLoadNoClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String column, String attribute) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("BETWEEN '%s' AND '%s'", parseToTimeStampFormat(lastSuccessful), timeLoadTo);
        conditions.add(Pair.of(COLUMN_X_TIMESTAMP, conditionString));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
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
