package com.gooddata.qa.graphene.flow;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyTimeStamp;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.OPTIONAL_PREFIX;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.VARCHAR_TYPE;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.gooddata.qa.graphene.AbstractAutomatedDataDistributionTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DatabaseColumn;
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class SegmentForceLoadTest extends AbstractAutomatedDataDistributionTest {
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
    private final String SEGMENT_URI = "/gdc/domains/%s/dataproducts/%s/segments/%s";
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "Process Test" + generateHashString();
    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private SnowflakeUtils snowflakeUtils;
    private ProcessUtils domainProcessUtils;
    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String timeForceFullLoad;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;
    private ConnectionInfo connectionInfo;

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
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws JSONException, IOException, SQLException {
        createLCM();
        connectionInfo = datasourceUtils.createConnectionInfo(DATABASE_NAME);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = datasourceUtils.createDataSource(DATABASE_NAME, OPTIONAL_PREFIX, DATA_SOURCE_NAME, connectionInfo);
        // setUp Model projects
        Dataset datasetcustomer = new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME)
                .withFacts(FACT_AGE);
        Dataset datasetCustomerOnlyTimestamp = new Dataset(DATASET_CUSTOMERS_ONLY_TIMESTAMP).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampClientId = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetCustomerTimestampDeleted = new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        ldmmodel = new LdmModel();
        // create MAQL
        setupMaql(ldmmodel.withDataset(datasetcustomer).withDataset(datasetCustomerOnlyTimestamp)
                .withDataset(datasetCustomerTimestampClientId).withDataset(datasetCustomerTimestampDeleted)
                .buildMaqlUsingPrimaryKey());
        // create Tables Snowflake
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumn1 = Arrays.asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                deletedColumn, clientIdColumn);
        List<DatabaseColumn> listColumnOnlyTimestamp = Arrays.asList(custkeyColumn, nameColumn, ageColumn, timestampColumn);
        List<DatabaseColumn> listColumnTimestampClientId = Arrays.asList(custkeyColumn, nameColumn, ageColumn,
                timestampColumn, clientIdColumn);
        List<DatabaseColumn> listColumnTimestampDeleted = Arrays.asList(custkeyColumn, nameColumn, ageColumn, timestampColumn,
                deletedColumn);
        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumn1);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_ONLY_TIMESTAMP, listColumnOnlyTimestamp);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listColumnTimestampClientId);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);
        dataloadProcess = domainScheduleUtils.createDataDistributionProcess(serviceProject, PROCESS_NAME, dataSourceId,
                SEGMENT_URI, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        timeForceFullLoad = parseToTimeStampFormat(LocalDateTime.now());
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkForceFullLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
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
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @Test(dependsOnMethods = { "checkForceFullLoadHasClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkForceFullLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            List<String> data) throws SQLException, IOException {
        try {
            csvfile.rows(data);
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
            ResultSet result = snowflakeUtils.getRecords(table, column);
            while (result.next()) {
                custkeyValues.add(result.getString(column));
            }
            assertThat(executionLog,
                    containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                            clientProjectId1, CLIENT_ID_1, dataset)));
            Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                    identifier("attr." + dataset + "." + attribute));
            assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
            assertThat(executionLog,
                    containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                            clientProjectId2, CLIENT_ID_2, dataset)));
            Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                    identifier("attr." + dataset + "." + attribute));
            assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
        } finally {
            timeLoadFrom = parseToTimeStampFormat(LocalDateTime.now());
            timeLoadTo = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(3));
            timeOverRange = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(5));
        }
    }

    @Test(dependsOnMethods = { "checkForceFullLoadNoClientId" }, dataProvider = "dataIncrementalLoadHasClientId")
    public void checkForceIncrementalLoadHasClientId(String table, CsvFile csvfile, String dataset, String column,
            String attribute, List<String> dataFrom, List<String> dataTo, List<String> dataOverRange) throws SQLException, IOException {
        csvfile.rows(dataFrom).rows(dataTo).rows(dataOverRange);
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
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecordsInRangeTimeStamp(table, column, parseToTimeStampFormat(lastSuccessful),
                timeLoadTo);
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

    @Test(dependsOnMethods = { "checkForceIncrementalLoadHasClientId" }, dataProvider = "dataIncrementalLoadNoClientId")
    public void checkForceIncrementalLoadNoClientId(String table, CsvFile csvfile, String dataset, String column,
            String attribute, List<String> dataFrom, List<String> dataTo, List<String> dataOverRange)
            throws SQLException, IOException {
        csvfile.rows(dataFrom).rows(dataTo).rows(dataOverRange);
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
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecordsInRangeTimeStamp(table, column, parseToTimeStampFormat(lastSuccessful),
                timeLoadTo);
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

    @DataProvider
    public Object[][] dataIncrementalLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS1B", "User", "30", timeLoadFrom, "0", CLIENT_ID_1),
                        asList("CUS1C", "User", "30", timeLoadTo, "0", CLIENT_ID_1),
                        asList("CUS1D", "User", "30", timeOverRange, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6B", "Phong", "30", timeLoadFrom, CLIENT_ID_1),
                        asList("CUS6C", "Phong", "30", timeLoadTo, CLIENT_ID_1),
                        asList("CUS6D", "Phong", "30", timeOverRange, CLIENT_ID_1) } };
    }

    @DataProvider
    public Object[][] dataIncrementalLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4B", "Phong", "30", timeLoadFrom), asList("CUS4C", "Phong", "30", timeLoadTo),
                        asList("CUS4D", "Phong", "30", timeOverRange) },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8B", "Phong", "30", timeLoadFrom, "0"),
                        asList("CUS8C", "Phong", "30", timeLoadTo, "0"), asList("CUS8D", "Phong", "30", timeOverRange, "0") } };
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS1", "User", "28", timeForceFullLoad, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6", "Phong", "28", timeForceFullLoad, CLIENT_ID_1) } };
    }

    @DataProvider
    public Object[][] dataFirstLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4", "Phong", "28", timeForceFullLoad) },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8", "Phong", "28", timeForceFullLoad, "0") } };
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException {
        domainScheduleUtils.getProcessService().removeProcess(dataloadProcess);
        lcmBrickFlowBuilder.destroy();
        datasourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
    }
}
