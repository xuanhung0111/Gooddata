package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormalHasDefaultLabel;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormalHasMainLabel;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormalNoDefaultLabel;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormalChangeDefaultLabel;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_NORMAL;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY_LABEL;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY_LINK;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY_LABEL;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY_LINK;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_MUlTILABELS_NODEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_MUlTILABELS_NORMAL;
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

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.apache.commons.lang3.tuple.Pair;

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
import com.gooddata.qa.utils.MaqlRestRequest;

public class SegmentDeleteByLabelTest extends AbstractADDProcessTest {
    private Project serviceProject;
    private String clientProjectId1;
    private String clientProjectId2;
    private Project project1;
    private String dataSourceId;
    private String devProjectId;
    private String serviceProjectId;
    private LdmModel ldmmodel;
    private LdmModel ldmmodelNoMainLabel;
    private LdmModel ldmmodelChangeDefaultLabel;
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
    private LocalDateTime lastSuccessfulSecondLoad;
    private LocalDateTime lastSuccessfulChangeLabel;
    private LocalDateTime lastSuccessfulDeleteChangeLabel;
    private String time;
    private String timeSecond;
    private String timeChangeLabel;
    private String timeDeleteChangeLabel;
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
        Dataset datasetSingleLabel = new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME)
                .withFacts(FACT_AGE);
        Dataset datasetMultiHasMainAttribute = new Dataset(DATASET_CUSTOMERS_MUlTILABELS_NORMAL).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE).withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LABEL))
                .withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LINK))
                .withDefaultLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LABEL));
        Dataset datasetMultiNoMainHasDefault = new Dataset(DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE).withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LABEL))
                .withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LINK))
                .withDefaultLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LABEL));
        Dataset datasetMultiNoMainNoDefault = new Dataset(DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE).withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LABEL))
                .withLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LINK));
        ldmmodel = new LdmModel();
        ldmmodelNoMainLabel = new LdmModel();
        // create MAQL
        setupMaql(ldmmodel.withDataset(datasetSingleLabel).withDataset(datasetMultiHasMainAttribute).buildMaqlUsingPrimaryKey());
        setupMaql(ldmmodelNoMainLabel.withDataset(datasetMultiNoMainHasDefault).withDataset(datasetMultiNoMainNoDefault)
                .buildMaqlUsingPrimaryKeyNoMainLabel());
        // create Tables Snowflake
        DatabaseColumn custkeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY);
        DatabaseColumn custkeyLabelColumn = new DatabaseColumn(PKCOLUMN_CUSKEY_LABEL, VARCHAR_TYPE);
        DatabaseColumn custkeyLinkColumn = new DatabaseColumn(PKCOLUMN_CUSKEY_LINK, VARCHAR_TYPE);
        DatabaseColumn nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE);
        DatabaseColumn ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE);
        DatabaseColumn timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE);
        DatabaseColumn deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE);
        DatabaseColumn clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumnSingle = asList(custkeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn,
                clientIdColumn);
        List<DatabaseColumn> listColumnMultiHasMainAttribute = asList(custkeyColumn, custkeyLabelColumn, custkeyLinkColumn,
                nameColumn, ageColumn, timestampColumn, deletedColumn, clientIdColumn);
        // default label will be replace Connection Point Column in SQL , so we don't
        // need create column for that label
        List<DatabaseColumn> listColumnMultiNoMainHasDefault = asList(custkeyColumn, custkeyLinkColumn, nameColumn, ageColumn,
                timestampColumn, deletedColumn, clientIdColumn);
        List<DatabaseColumn> listColumnMultiNoMainNoDefault = asList(custkeyColumn, custkeyLinkColumn, nameColumn, ageColumn,
                timestampColumn, deletedColumn, clientIdColumn);

        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumnSingle);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_MUlTILABELS_NORMAL, listColumnMultiHasMainAttribute);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT, listColumnMultiNoMainHasDefault);
        snowflakeUtils.createTable(TABLE_CUSTOMERS_MUlTILABELS_NODEFAULT, listColumnMultiNoMainNoDefault);
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        // On platform , Default Label will auto-asign to attribute which doesn't have,
        // so we need write test remove default label on Clients
        MaqlRestRequest maqlRestRequestProject1 = new MaqlRestRequest(project1, getProfile(Profile.ADMIN));
        maqlRestRequestProject1.setupMaqlRemoveDefaultLabel(DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT, PK_CUSKEY_LABEL, PK_CUSKEY);
        lastSuccessful = LocalDateTime.now().withNano(0);
        lastSuccessfulSecondLoad = LocalDateTime.now().plusSeconds(5).withNano(0);
        lastSuccessfulChangeLabel = LocalDateTime.now().plusSeconds(10).withNano(0);
        lastSuccessfulDeleteChangeLabel = LocalDateTime.now().plusSeconds(15).withNano(0);
        time = parseToTimeStampFormat(lastSuccessful);
        timeSecond = parseToTimeStampFormat(lastSuccessfulSecondLoad);
        timeChangeLabel = parseToTimeStampFormat(lastSuccessfulChangeLabel);
        timeDeleteChangeLabel = parseToTimeStampFormat(lastSuccessfulDeleteChangeLabel);
    }

    @DataProvider
    public Object[][] dataFirstLoad() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS,
                        datasetNormal().rows(asList("CUS1", "User", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS2", "User", "28", time, "false", CLIENT_ID_1))
                                .rows(asList("CUS3", "User", "28", time, "FALSE", CLIENT_ID_1)),
                        DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_MUlTILABELS_NORMAL,
                        datasetNormalHasMainLabel()
                                .rows(asList("CUS1BLABEL", "CUS1BLABEL", "CUS1BLINK", "User2", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS2BLABEL", "CUS2BLABEL", "CUS2BLINK", "User2", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS3BLABEL", "CUS3BLABEL", "CUS3BLINK", "User2", "28", time, "0", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_NORMAL, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT,
                        datasetNormalHasDefaultLabel()
                                .rows(asList("CUS1CLABEL", "CUS1CLINK", "User3", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS2CLABEL", "CUS2CLINK", "User3", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS3CLABEL", "CUS3CLINK", "User3", "28", time, "0", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT, PKCOLUMN_CUSKEY, PK_CUSKEY },
                { TABLE_CUSTOMERS_MUlTILABELS_NODEFAULT,
                        datasetNormalNoDefaultLabel()
                                .rows(asList("CUS1DLABEL", "CUS1DLINK", "User3", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS2DLABEL", "CUS2DLINK", "User3", "28", time, "0", CLIENT_ID_1))
                                .rows(asList("CUS3DLABEL", "CUS3DLINK", "User3", "28", time, "0", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT, PKCOLUMN_CUSKEY, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoad")
    public void checkFirstLoad(String table, CsvFile csvfile, String dataset, String column, String attribute)
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
    public Object[][] dataSecondLoad() throws IOException {
        return new Object[][] {
                { TABLE_CUSTOMERS,
                        datasetNormal().rows(asList("CUS1", "UserAA", "29", timeSecond, "1", CLIENT_ID_1))
                                .rows(asList("CUS2_Notcorrect", "User", "28", timeSecond, "1", CLIENT_ID_1)),
                        DATASET_CUSTOMERS, PK_CUSKEY, asList("CUS2", "CUS3") },
                { TABLE_CUSTOMERS_MUlTILABELS_NORMAL, datasetNormalHasMainLabel()
                        .rows(asList("CUS1BLABEL", "CUS1BLABEL", "CUS1BLINK", "UserAA", "29", timeSecond, "1", CLIENT_ID_1))
                        .rows(asList("CUS2BLABEL_Notcorrect", "CUS2BLABEL", "CUS2BLINK", "User2", "28", timeSecond, "1",
                                CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_NORMAL, PK_CUSKEY, asList("CUS2BLABEL", "CUS3BLABEL") },
                { TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT,
                        datasetNormalHasDefaultLabel()
                                .rows(asList("CUS1CLABEL", "CUS1CLINKK", "User3AA", "29", timeSecond, "1", CLIENT_ID_1))
                                .rows(asList("CUS2CLABEL_Notcorrect", "CUS2CLINK", "User3", "28", timeSecond, "1", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT, PK_CUSKEY, asList("CUS2CLABEL", "CUS3CLABEL") },
                { TABLE_CUSTOMERS_MUlTILABELS_NODEFAULT,
                        datasetNormalNoDefaultLabel()
                                .rows(asList("CUS1DLABEL", "CUS1CLINKAA", "User3AA", "29", timeSecond, "1", CLIENT_ID_1))
                                .rows(asList("CUS2DLABEL_Notcorrect", "CUS2DLINK", "User3", "28", timeSecond, "1", CLIENT_ID_1)),
                        DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT, PK_CUSKEY, asList("CUS2DLABEL", "CUS3DLABEL") } };
    }

    @Test(dependsOnMethods = { "checkFirstLoad" }, dataProvider = "dataSecondLoad")
    public void checkSecondLoad(String table, CsvFile csvfile, String dataset, String attribute, List<String> expectedResult)
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
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @Test(dependsOnMethods = { "checkSecondLoad" })
    public void checkUploadAfterChangeDefaultLabel() throws SQLException, IOException {
        Dataset datasetMultiNoMainChangeDefault = new Dataset(DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT)
                .withDefaultLabelOfAtrribute(Pair.of(PK_CUSKEY, PK_CUSKEY_LINK));
        ldmmodelChangeDefaultLabel = new LdmModel();
        setupMaql(ldmmodelChangeDefaultLabel.withDataset(datasetMultiNoMainChangeDefault).buildMaqlChangeDefaultLabel());
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        CsvFile csvfile = datasetNormalChangeDefaultLabel();
        snowflakeUtils.dropColumn(TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT, PKCOLUMN_CUSKEY_LINK);
        snowflakeUtils.addColumn(TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT, PKCOLUMN_CUSKEY_LABEL, VARCHAR_TYPE);
        csvfile.rows(asList("CUS4CLINK", "User4", "28", timeChangeLabel, "0", CLIENT_ID_1, "CUS4CLABEL"))
                .rows(asList("CUS5CLINK", "User5", "28", timeChangeLabel, "0", CLIENT_ID_1, "CUS5CLABEL"));
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT, csvfile.getFilePath());
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        assertThat(executionLog,
                containsString(String.format(
                        "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                        clientProjectId1, CLIENT_ID_1, DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT, lastSuccessfulSecondLoad)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT + "." + PK_CUSKEY));
        assertThat(getAttributeValues(attributeCustkey),
                containsInAnyOrder(asList("CUS2CLINK", "CUS3CLINK", "CUS4CLINK", "CUS5CLINK").toArray()));
    }

    @Test(dependsOnMethods = { "checkUploadAfterChangeDefaultLabel" })
    public void checkDeleteAfterChangeDefaultLabel() throws SQLException, IOException {
        CsvFile csvfile = datasetNormalChangeDefaultLabel();
        csvfile.rows(asList("CUS4CLINK", "User4", "28", timeDeleteChangeLabel, "1", CLIENT_ID_1, "CUS4CLABEL"))
                .rows(asList("CUS5CLINK_NotCorrect", "User5", "28", timeDeleteChangeLabel, "1", CLIENT_ID_1, "CUS5CLABEL"));
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT, csvfile.getFilePath());
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        assertThat(executionLog,
                containsString(String.format(
                        "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                        clientProjectId1, CLIENT_ID_1, DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT, lastSuccessfulChangeLabel)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT + "." + PK_CUSKEY));
        assertThat(getAttributeValues(attributeCustkey),
                containsInAnyOrder(asList("CUS2CLINK", "CUS3CLINK", "CUS5CLINK").toArray()));
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
        log.info("client 2 : " + clientProjectId2);
        lcmBrickFlowBuilder.setDevelopProject(devProjectId).setSegmentId(SEGMENT_ID).setClient(CLIENT_ID_1, clientProjectId1)
                .setClient(CLIENT_ID_2, clientProjectId2).buildLcmProjectParameters();
        lcmBrickFlowBuilder.runLcmFlow();
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId1);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId2);
    }
}
