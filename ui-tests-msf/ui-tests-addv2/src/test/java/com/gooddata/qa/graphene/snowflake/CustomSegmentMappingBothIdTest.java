package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDelete;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
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
import com.gooddata.qa.utils.snowflake.DataMappingUtils;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import com.gooddata.qa.utils.snowflake.DataSourceUtils;
import com.gooddata.qa.utils.snowflake.DatabaseColumn;
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class CustomSegmentMappingBothIdTest extends AbstractADDProcessTest {
    private Project serviceProject;
    private String clientProjectId1;
    private String devProjectId;
    private String clientProjectId2;
    private Project project1;
    private Project project2;
    private String dataSourceId;
    private String serviceProjectId;

    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String OTHER_CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();

    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String time;
    private LocalDateTime lastSecondSuccessful;
    private String timeSecond;
    private LocalDateTime lastForceFullSuccessful;
    private String timeForceFullLoad;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;

    private SnowflakeUtils snowflakeUtils;
    private DataSourceUtils dataSourceUtils;
    private ProcessUtils domainProcessUtils;
    private DataMappingUtils dataMappingProjectIdUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = { "createProject" })
    public void initData() throws JSONException, IOException, SQLException {
        createLCM();
        setUpDataSource();
        setUpModel();
        setUpDatabase();
        setUpDataMapping();
        setUpProcess();
        prepareTimestamp();
        // re-run LCM flow apply updated Model
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() throws IOException {
        return new Object[][] { { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS1", "User", "28", time, "0", CLIENT_ID_1))
                        .rows(asList("CUS2", "User2", "28", time, "false", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "28", time, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS4", "User4", "28", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkFirstLoadWithClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
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
        String conditionString = String.format("= '%s'", CLIENT_ID_1);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
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
                clientProjectId2, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadHasClientId() throws IOException {
        return new Object[][] { { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS5", "User5", "28", timeSecond, "0", CLIENT_ID_1))
                        .rows(asList("CUS6", "User6", "28", timeSecond, "false", CLIENT_ID_1))
                        .rows(asList("CUS1", "User1", "28", timeSecond, "true", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "28", timeSecond, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeSecond, CLIENT_ID_1)).rows(asList("CUS3", timeSecond, OTHER_CLIENT_ID)),
                DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS3", "CUS5", "CUS6"),
                asList("CUS1", "CUS2", "CUS3") } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadWithClientId" }, dataProvider = "dataSecondLoadHasClientId")
    public void checkSecondLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, List<String> expectedResult, List<String> keepResult)
            throws SQLException, IOException {
        dataMappingProjectIdUtils.updateClientIdDataMapping(Pair.of(CLIENT_ID_1, OTHER_CLIENT_ID));
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        assertThat(domainProcessUtils.executeWarning(parameters), containsString("Execution was not successful"));
        Attribute attributeCustkey = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        Attribute attributeCustkey1 = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey1), containsInAnyOrder(keepResult.toArray()));
    }

    @DataProvider
    public Object[][] dataForceFullLoad() throws IOException {
        return new Object[][] { { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS7", "User7", "28", timeForceFullLoad, "0", CLIENT_ID_1))
                        .rows(asList("CUS8", "User8", "28", timeForceFullLoad, "false", CLIENT_ID_1))
                        .rows(asList("CUS9", "User9", "28", timeForceFullLoad, "true", CLIENT_ID_1))
                        .rows(asList("CUS10", "User10", "28", timeForceFullLoad, "false", CLIENT_ID_2)),
                datasetDelete().rows(asList("CUS5", timeForceFullLoad, CLIENT_ID_1))
                        .rows(asList("CUS6", timeForceFullLoad, CLIENT_ID_1)),
                DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, PKCOLUMN_CUSKEY, asList("CUS1", "CUS2", "CUS3") } };
    }

    @Test(dependsOnMethods = { "checkSecondLoadWithClientId" }, dataProvider = "dataForceFullLoad")
    public void checkForceFullLoad(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete, String dataset,
            String attribute, String column, List<String> keepResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");

        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", CLIENT_ID_1);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(domainProcessUtils.executeWarning(parameters), containsString("Execution was not successful"));
        Attribute attributeCustkey = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
        Attribute attributeCustkey1 = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey1), containsInAnyOrder(keepResult.toArray()));
    }

    @DataProvider
    public Object[][] dataForceIncreMisMatchDataMapping() throws IOException {
        return new Object[][] { { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS11", "User11", "28", timeLoadFrom, "0", CLIENT_ID_1))
                        .rows(asList("CUS12", "User12", "28", timeLoadTo, "false", CLIENT_ID_1))
                        .rows(asList("CUS1", "User1", "28", timeLoadFrom, "true", CLIENT_ID_1))
                        .rows(asList("CUS13", "User13", "28", timeOverRange, "false", CLIENT_ID_1))
                        .rows(asList("CUS14", "User14", "28", timeLoadFrom, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeLoadFrom, CLIENT_ID_1)), DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                PK_CUSKEY, asList("CUS2", "CUS3", "CUS5", "CUS6", "CUS7", "CUS8", "CUS11", "CUS12"), asList("CUS1", "CUS2", "CUS3") } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoad" }, dataProvider = "dataForceIncreMisMatchDataMapping")
    public void checkForceIncrementalMisMatchDataMapping(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, List<String> expectedResult, List<String> keepResult)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        assertThat(domainProcessUtils.executeWarning(parameters), containsString("Execution was not successful"));
        Attribute attributeCustkey = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        Attribute attributeCustkey1 = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey1), containsInAnyOrder(keepResult.toArray()));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException, ParseException, JSONException, IOException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        domainRestClient.getProcessService().removeProcess(dataloadProcess);
        lcmBrickFlowBuilder.destroy();
        dataMappingProjectIdUtils.deleteClientIdDataMapping(CLIENT_ID_1);
        dataMappingProjectIdUtils.deleteClientIdDataMapping(CLIENT_ID_2);
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(clientProjectId1);
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(clientProjectId2);
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
        snowflakeUtils.closeSnowflakeConnection();
    }

    private void setUpDataSource() throws SQLException, IOException {
        ConnectionInfo connectionInfo = dataSourceUtils.createDefaultConnectionInfo(DATABASE_NAME);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceUtils.createDataSource(DATA_SOURCE_NAME, connectionInfo);
    }

    private void setUpModel() {
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset secondDatasetMappingProjectId = new Dataset(DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).withDataset(secondDatasetMappingProjectId)
                .buildMaqlUsingPrimaryKey());
    }

    private void setUpDatabase() throws SQLException {
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumnNormal = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnNoClientId = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn);
        List<DatabaseColumn> listDeleteColumnNormal = asList(custkeyColumn, timestampColumn, clientIdColumn);
        List<DatabaseColumn> listDeleteColumnNoClientId = asList(custkeyColumn, timestampColumn);

        snowflakeUtils.createTable(TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, listColumnNoClientId);
        snowflakeUtils.createTable(TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, listColumnNormal);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, listDeleteColumnNoClientId);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, listDeleteColumnNormal);
    }

    private void setUpDataMapping() {
        List<Pair<String, String>> listClientIdMapping = new ArrayList<>();
        listClientIdMapping.add(Pair.of(CLIENT_ID_1, CLIENT_ID_1));
        listClientIdMapping.add(Pair.of(CLIENT_ID_2, CLIENT_ID_1));
        List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
        listProjectIdMapping.add(Pair.of(clientProjectId1, CLIENT_ID_1));
        listProjectIdMapping.add(Pair.of(clientProjectId2, CLIENT_ID_1));
        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, listClientIdMapping,
                dataSourceId, testParams.getProjectId());
        dataMappingProjectIdUtils.createDataMapping();
    }

    private void setUpProcess() {
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);

    }

    private void prepareTimestamp() {
        lastSuccessful = LocalDateTime.now().withNano(0);
        lastSecondSuccessful = lastSuccessful.plusSeconds(5);
        time = parseToTimeStampFormat(lastSuccessful);
        timeSecond = parseToTimeStampFormat(lastSecondSuccessful);
        lastSuccessful = LocalDateTime.now().withNano(0);
        time = parseToTimeStampFormat(lastSuccessful);
        lastForceFullSuccessful = lastSuccessful.plusSeconds(10);
        timeForceFullLoad = parseToTimeStampFormat(lastForceFullSuccessful);
        timeLoadFrom = parseToTimeStampFormat(lastSuccessful.plusSeconds(15));
        timeLoadTo = parseToTimeStampFormat(lastSuccessful.plusSeconds(20));
        timeOverRange = parseToTimeStampFormat(lastSuccessful.plusSeconds(25));
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
