package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.cloudresources.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.VARCHAR_TYPE;
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
import com.gooddata.qa.utils.cloudresources.ConnectionInfo;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import com.gooddata.qa.utils.cloudresources.DatabaseColumn;
import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.cloudresources.ProcessUtils;
import com.gooddata.qa.utils.cloudresources.SnowflakeUtils;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.schedule.ScheduleUtils;

public class SegmentDeleteColumnForceLoadTest extends AbstractADDProcessTest {
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
    private final String PROCESS_NAME = "Process Test" + generateHashString();
    private SnowflakeUtils snowflakeUtils;
    private ProcessUtils domainProcessUtils;
    private DataSourceUtils dataSourceUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String timeForceFullLoad;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;

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
        Dataset datasetCustomerTimestampDeleted = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        ldmmodel = new LdmModel();
        // create MAQL
        setupMaql(ldmmodel.withDataset(datasetcustomer).withDataset(datasetCustomerTimestampDeleted).buildMaqlUsingPrimaryKey());
        // create Tables Snowflake
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumn1 = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnTimestampDeleted = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                deletedColumn);
        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumn1);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        timeForceFullLoad = parseToTimeStampFormat(lastSuccessful);
        timeLoadFrom = parseToTimeStampFormat(lastSuccessful.plusSeconds(5));
        timeLoadTo = parseToTimeStampFormat(lastSuccessful.plusSeconds(7));
        timeOverRange = parseToTimeStampFormat(lastSuccessful.plusSeconds(10));
    }

    @DataProvider
    public Object[][] dataForceFullLoadHasClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS,
                datasetNormal().rows(asList("CUS1", "User", "28", timeForceFullLoad, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS2", "User", "28", timeForceFullLoad, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS2", "User", "28", timeForceFullLoad, "TRUE", CLIENT_ID_1))
                        .rows(asList("CUS3", "User", "28", timeForceFullLoad, "FALSE", CLIENT_ID_1)),
                DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY, } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataForceFullLoadHasClientId")
    public void checkForceFullLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
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
    }

    @DataProvider
    public Object[][] dataForceFullLoadNoClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS_TIMESTAMP_DELETED,
                datasetTimeStampDeleted().rows(asList("CUS1B", "Phong", "28", timeForceFullLoad, "FALSE"))
                        .rows(asList("CUS2B", "Phong", "28", timeForceFullLoad, "false"))
                        .rows(asList("CUS2B", "Phong", "28", timeForceFullLoad, "true"))
                        .rows(asList("CUS3B", "Phong", "28", timeForceFullLoad, "f")),
                DATASET_CUSTOMERS_TIMESTAMP_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY, } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoadHasClientId" }, dataProvider = "dataForceFullLoadNoClientId")
    public void checkForceFullLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
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
    public Object[][] dataForceIncrementalHasClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS,
                datasetNormal().rows(asList("CUS4", "User", "28", timeLoadFrom, "0", CLIENT_ID_1))
                        .rows(asList("CUS5", "User", "28", timeLoadTo, "0", CLIENT_ID_1))
                        .rows(asList("CUS6", "User", "28", timeOverRange, "0", CLIENT_ID_1))
                        .rows(asList("CUS3", "User", "28", timeLoadFrom, "1", CLIENT_ID_1)),
                DATASET_CUSTOMERS, PK_CUSKEY, asList("CUS1", "CUS2", "CUS4", "CUS5") } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoadNoClientId" }, dataProvider = "dataForceIncrementalHasClientId")
    public void checkForceIncrementalLoadHasClientId(String table, CsvFile csvfile, String dataset, String attribute,
            List<String> expectedResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataForceIncrementalNoClientId() throws IOException {
        return new Object[][] { { TABLE_CUSTOMERS_TIMESTAMP_DELETED,
                datasetTimeStampDeleted().rows(asList("CUS4B", "Phong", "28", timeLoadFrom, "false"))
                        .rows(asList("CUS5B", "Phong", "28", timeLoadTo, "false"))
                        .rows(asList("CUS6B", "Phong", "28", timeOverRange, "false"))
                        .rows(asList("CUS3B", "Phong", "28", timeLoadFrom, "true")),
                DATASET_CUSTOMERS_TIMESTAMP_DELETED, PK_CUSKEY, asList("CUS1B", "CUS2B", "CUS4B", "CUS5B") } };
    }

    @Test(dependsOnMethods = { "checkForceIncrementalLoadHasClientId" }, dataProvider = "dataForceIncrementalNoClientId")
    public void checkForceIncrementalLoadNoClientId(String table, CsvFile csvfile, String dataset, String attribute,
            List<String> expectedResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId2, CLIENT_ID_2, dataset, lastSuccessful)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(expectedResult.toArray()));
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
