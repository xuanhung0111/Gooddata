package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.sdk.model.md.Restriction.identifier;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.cloudresources.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetDelete;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetDeleteTimeStampDeleted;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN;
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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.cloudresources.ConnectionInfo;
import com.gooddata.qa.utils.cloudresources.DataMappingUtils;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import com.gooddata.qa.utils.cloudresources.DatabaseColumn;
import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.cloudresources.ProcessUtils;
import com.gooddata.qa.utils.cloudresources.SnowflakeUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.schedule.ScheduleUtils;

public class CustomCurrentForceLoadTest extends AbstractADDProcessTest {
    private Project projectFirst;
    private Project projectSecond;
    private Project projectThird;
    private String dataSourceId;
    private String dataSourceSecondId;
    private String dataSourceThirdId;
    private String projectMappingPID;
    private String projectMappingClientID;
    private String projectMappingBoth;
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String OTHER_CLIENT_ID = "att_client_" + generateHashString();
    private final String UPDATE_CLIENT_ID = "att_client_" + generateHashString();
    private DataloadProcess dataloadProcess;
    private DataloadProcess dataloadProcessSecond;
    private DataloadProcess dataloadProcessThird;
    private LocalDateTime lastSuccessful;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;
    private String timeForceFullLoad;
    private SnowflakeUtils snowflakeUtils;
    private DataSourceUtils dataSourceFirst;
    private DataSourceUtils dataSourceSecond;
    private DataSourceUtils dataSourceThird;
    private ProcessUtils domainProcessUtils;
    private ProcessUtils domainProcessUtilsSecond;
    private ProcessUtils domainProcessUtilsThird;
    private DataMappingUtils dataMappingProjectIdUtils;
    private DataMappingUtils dataMappingClientIdUtils;
    private DataMappingUtils dataMappingBothIdUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        domainRestClient = new RestClient(getProfile(DOMAIN));
        dataSourceFirst = new DataSourceUtils(testParams.getDomainUser());
        dataSourceSecond = new DataSourceUtils(testParams.getDomainUser());
        dataSourceThird = new DataSourceUtils(testParams.getDomainUser());
        // use the same dataSourceRestRequest for 3 datasources.
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws IOException, SQLException {
        setUpProject();
        setUpDatasource();
        setUpModel();
        setUpDatabase();
        setUpDataMapping();
        setUpProcess();
        prepareTimestamp();
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS1", "User", "28", timeForceFullLoad, "0", CLIENT_ID))
                                .rows(asList("CUS2", "User2", "28", timeForceFullLoad, "false", CLIENT_ID))
                                .rows(asList("CUS3", "User3", "28", timeForceFullLoad, "FALSE", CLIENT_ID))
                                .rows(asList("CUS4", "User4", "28", timeForceFullLoad, "FALSE", OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtils,
                        projectFirst, projectMappingPID },
                { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS1B", "UserB", "28", timeForceFullLoad, "0", CLIENT_ID))
                                .rows(asList("CUS2B", "User2B", "28", timeForceFullLoad, "false", CLIENT_ID))
                                .rows(asList("CUS3B", "User3B", "28", timeForceFullLoad, "FALSE", CLIENT_ID))
                                .rows(asList("CUS4B", "User4B", "28", timeForceFullLoad, "FALSE", OTHER_CLIENT_ID)),
                        DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsThird,
                        projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkForceFullLoadWithClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            ProcessUtils processUtils, Project project, String projectId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);

        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", CLIENT_ID);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }

        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, CLIENT_ID, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataFirstLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS1C", "User1C", "28", timeForceFullLoad, "0"))
                                .rows(asList("CUS2C", "User2C", "28", timeForceFullLoad, "false"))
                                .rows(asList("CUS3C", "User3C", "28", timeForceFullLoad, "0")),
                        DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtils,
                        projectFirst, projectMappingPID },
                { TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS1D", "User1D", "28", timeForceFullLoad, "0"))
                                .rows(asList("CUS2D", "User2D", "28", timeForceFullLoad, "false"))
                                .rows(asList("CUS3D", "User3D", "28", timeForceFullLoad, "0")),
                        DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsSecond,
                        projectSecond, projectMappingClientID },
                { TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS1E", "User1E", "28", timeForceFullLoad, "0"))
                                .rows(asList("CUS2E", "User2E", "28", timeForceFullLoad, "false"))
                                .rows(asList("CUS3E", "User3E", "28", timeForceFullLoad, "0")),
                        DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsThird,
                        projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoadWithClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkForceFullLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            ProcessUtils processUtils, Project project, String projectId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);

        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= 0");
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }

        assertThat(executionLog, containsString(String.format("Project=\"%s\"", projectId)));
        assertThat(executionLog, containsString(String.format("datasets=[{dataset.%s, full}]", dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS5", "User5", "28", timeLoadFrom, "0", CLIENT_ID))
                                .rows(asList("CUS6", "User6", "28", timeLoadTo, "false", CLIENT_ID))
                                .rows(asList("CUS1", "User1", "28", timeLoadFrom, "true", CLIENT_ID))
                                .rows(asList("CUS7", "User7", "28", timeOverRange, "false", CLIENT_ID))
                                .rows(asList("CUS8", "User8", "28", timeLoadFrom, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS2", timeLoadFrom, CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst,
                        projectMappingPID, asList("CUS2", "CUS3", "CUS5", "CUS6") },
                { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS5B", "User5B", "28", timeLoadFrom, "false", CLIENT_ID))
                                .rows(asList("CUS6B", "User6B", "28", timeLoadTo, "false", CLIENT_ID))
                                .rows(asList("CUS1B", "User1B", "28", timeLoadFrom, "true", CLIENT_ID))
                                .rows(asList("CUS7B", "User7B", "28", timeOverRange, "false", CLIENT_ID))
                                .rows(asList("CUS8B", "User8B", "28", timeLoadFrom, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS2B", timeLoadTo, CLIENT_ID)), DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        PK_CUSKEY, domainProcessUtilsThird, projectThird, projectMappingBoth,
                        asList("CUS2B", "CUS3B", "CUS5B", "CUS6B") } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoadNoClientId" }, dataProvider = "dataSecondLoadHasClientId")
    public void checkForceIncrementalWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, ProcessUtils processUtils, Project project, String projectId,
            List<String> expectedResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                projectId, CLIENT_ID, dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 3)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4C", "User4C", "28", timeLoadFrom, "0"))
                                .rows(asList("CUS5C", "User5C", "28", timeLoadTo, "false"))
                                .rows(asList("CUS6C", "User6C", "28", timeOverRange, "false"))
                                .rows(asList("CUS1C", "User1C", "28", timeLoadFrom, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2C", timeLoadFrom)),
                        DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS2C", "CUS3C", "CUS4C", "CUS5C"),
                        domainProcessUtils, projectFirst, projectMappingPID },
                { TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4D", "User4D", "28", timeLoadFrom, "0"))
                                .rows(asList("CUS5D", "User5D", "28", timeLoadTo, "false"))
                                .rows(asList("CUS6D", "User6D", "28", timeOverRange, "false"))
                                .rows(asList("CUS1D", "User1D", "28", timeLoadFrom, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2D", timeLoadFrom)),
                        DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS2D", "CUS3D", "CUS4D", "CUS5D"),
                        domainProcessUtilsSecond, projectSecond, projectMappingClientID },
                { TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4E", "User4E", "28", timeLoadFrom, "0"))
                                .rows(asList("CUS5E", "User5E", "28", timeLoadTo, "false"))
                                .rows(asList("CUS6E", "User6E", "28", timeOverRange, "false"))
                                .rows(asList("CUS1E", "User1E", "28", timeLoadFrom, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2E", timeLoadFrom)),
                        DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS2E", "CUS3E", "CUS4E", "CUS5E"),
                        domainProcessUtilsThird, projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "checkForceIncrementalWithClientId" }, dataProvider = "dataSecondLoadNoClientId")
    public void checkForceIncrementalNoClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, List<String> expectedResult, ProcessUtils processUtils, Project project,
            String projectId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format("Project=\"%s\"", projectId)));
        assertThat(executionLog,
                containsString(String.format("datasets=[{dataset.%s, incremental, loadDataFrom=%s}]", dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataLoadUpdateDataMapping() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS9", "User9", "28", timeForceFullLoad, "0", UPDATE_CLIENT_ID))
                                .rows(asList("CUS10", "User10", "28", timeForceFullLoad, "false", UPDATE_CLIENT_ID))
                                .rows(asList("CUS11", "User11", "28", timeForceFullLoad, "true", UPDATE_CLIENT_ID))
                                .rows(asList("CUS12", "User11", "28", timeForceFullLoad, "false", CLIENT_ID)),
                        datasetDelete().rows(asList("CUS7", timeForceFullLoad, UPDATE_CLIENT_ID))
                                .rows(asList("CUS8", timeForceFullLoad, UPDATE_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, PKCOLUMN_CUSKEY, domainProcessUtils,
                        projectFirst, projectMappingPID, dataMappingProjectIdUtils, projectMappingPID, UPDATE_CLIENT_ID } };
    }

    @Test(dependsOnMethods = { "checkForceIncrementalNoClientId" }, dataProvider = "dataLoadUpdateDataMapping")
    public void checkForceFullUpdateDataMapping(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, String column, ProcessUtils processUtils, Project project, String projectId,
            DataMappingUtils dataMappingUtils, String mappingKey, String mappingValue) throws SQLException, IOException {
        dataMappingUtils.updateProjectIdDataMapping(Pair.of(mappingKey, mappingValue));
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);

        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", UPDATE_CLIENT_ID);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        ResultSet result = snowflakeUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }

        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full",
                projectId, UPDATE_CLIENT_ID, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadUpdateDataMapping() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS13", "User13", "28", timeLoadFrom, "0", UPDATE_CLIENT_ID))
                                .rows(asList("CUS14", "User14", "28", timeLoadTo, "false", UPDATE_CLIENT_ID))
                                .rows(asList("CUS9", "User9", "28", timeLoadFrom, "true", UPDATE_CLIENT_ID))
                                .rows(asList("CUS15", "User15", "28", timeOverRange, "false", UPDATE_CLIENT_ID))
                                .rows(asList("CUS16", "User16", "28", timeLoadFrom, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS10", timeLoadFrom, UPDATE_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst,
                        projectMappingPID, asList("CUS10", "CUS13", "CUS14") } };
    }

    @Test(dependsOnMethods = { "checkForceFullUpdateDataMapping" }, dataProvider = "dataSecondLoadUpdateDataMapping")
    public void checkForceIncrementalUpdateDataMapping(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, ProcessUtils processUtils, Project project, String projectId,
            List<String> expectedResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                projectId, UPDATE_CLIENT_ID, dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 3)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_column, rows=%s", dataset, csvfile.getDataRowCount() - 4)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataLoadDeleteDataMapping() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS9", "User9", "28", timeForceFullLoad, "0", UPDATE_CLIENT_ID))
                        .rows(asList("CUS10", "User10", "28", timeForceFullLoad, "false", UPDATE_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, PKCOLUMN_CUSKEY, domainProcessUtils, projectFirst,
                projectMappingPID, dataMappingProjectIdUtils, projectMappingPID, dataSourceId } };
    }

    @Test(dependsOnMethods = { "checkForceIncrementalUpdateDataMapping" }, dataProvider = "dataLoadDeleteDataMapping")
    public void checkDeleteMapping(String table, CsvFile csvfile, String dataset, String attribute, String column,
            ProcessUtils processUtils, Project project, String projectId, DataMappingUtils dataMappingUtils, String mappingKey,
            String dataSourceId) throws SQLException, IOException {
        try {
            dataMappingUtils.deleteProjectIdDataMapping(mappingKey);
            csvfile.saveToDisc(testParams.getCsvFolder());
            log.info("This is path of CSV File :" + csvfile.getFilePath());
            snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
            // CHECK RESULT
            JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
            String valueParam = processUtils.getDataset(jsonDataset);
            Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                    .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
            String errorMessage = "The Output Stage has the x__client_id column, but no Client Identifier was provided for the current workspace.";
            assertThat(processUtils.executeError(parameters), containsString(errorMessage));
        } finally {
            // re-create dataMapping for using delete all dataMapping AfterTest
            List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
            listProjectIdMapping.add(Pair.of(mappingKey, CLIENT_ID));
            dataMappingUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, asList(), dataSourceId,
                    testParams.getProjectId());
            dataMappingUtils.createDataMapping();
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException, ParseException, JSONException, IOException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcess);
        }
        if (dataloadProcessSecond != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcessSecond);
        }
        if (dataloadProcessThird != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcessThird);
        }
        domainRestClient.getProjectService().removeProject(projectSecond);
        domainRestClient.getProjectService().removeProject(projectThird);
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(projectMappingPID);
        dataMappingClientIdUtils.deleteClientIdDataMapping(CLIENT_ID);
        dataMappingBothIdUtils.deleteProjectIdDataMapping(projectMappingBoth);
        dataMappingBothIdUtils.deleteClientIdDataMapping(CLIENT_ID);
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        dataSourceRestRequest.deleteDataSource(dataSourceSecondId);
        dataSourceRestRequest.deleteDataSource(dataSourceThirdId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
        snowflakeUtils.closeSnowflakeConnection();
    }

    private String createNewEmptyProject(final TestParameters testParameters, final String projectTitle) {
        final Project project = new Project(projectTitle, testParameters.getAuthorizationToken());
        project.setDriver(testParameters.getProjectDriver());
        project.setEnvironment(testParameters.getProjectEnvironment());

        return domainRestClient.getProjectService().createProject(project)
                .get(testParameters.getCreateProjectTimeout(), TimeUnit.MINUTES).getId();
    }

    private void setUpProject() throws IOException {
        projectMappingPID = testParams.getProjectId();
        projectMappingClientID = createNewEmptyProject(testParams, "Project Using Mapping ProjectID");
        projectMappingBoth = createNewEmptyProject(testParams, "Project Using Mapping Both ID");
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, projectMappingClientID);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, projectMappingBoth);
        projectFirst = getAdminRestClient().getProjectService().getProjectById(projectMappingPID);
        log.info("Project 1 :" + projectMappingPID);
        projectSecond = getAdminRestClient().getProjectService().getProjectById(projectMappingClientID);
        log.info("Project 2 :" + projectMappingClientID);
        projectThird = getAdminRestClient().getProjectService().getProjectById(projectMappingBoth);
        log.info("Project 3 :" + projectMappingBoth);
    }

    private void setUpDatasource() throws SQLException, IOException {
        // use the same Connection Info for 3 datasources.
        ConnectionInfo connectionInfo = dataSourceFirst.createSnowflakeConnectionInfo(DATABASE_NAME, DatabaseType.SNOWFLAKE);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceFirst.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        dataSourceSecondId = dataSourceSecond.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        dataSourceThirdId = dataSourceThird.createDataSource(DATA_SOURCE_NAME, connectionInfo);
    }

    private void setupMaql(String maql, Project project) {
        getAdminRestClient().getModelService().updateProjectModel(project, maql).get();
    }

    private void setUpModel() {
        // setUp Model projects
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetMappingClientId = new Dataset(DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset datasetMappingBoth = new Dataset(DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset secondDatasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN)
                .withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset secondDatasetMappingClientId = new Dataset(DATASET_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN)
                .withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        Dataset secondDatasetMappingBoth = new Dataset(DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AGE);
        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).withDataset(secondDatasetMappingProjectId)
                .buildMaqlUsingPrimaryKey(), projectFirst);
        setupMaql(new LdmModel().withDataset(datasetMappingClientId).withDataset(secondDatasetMappingClientId)
                .buildMaqlUsingPrimaryKey(), projectSecond);
        setupMaql(new LdmModel().withDataset(datasetMappingBoth).withDataset(secondDatasetMappingBoth).buildMaqlUsingPrimaryKey(),
                projectThird);
    }

    private void setUpDatabase() throws SQLException {
        // create Tables Snowflake
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

        snowflakeUtils.createTable(TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, listColumnNoClientId);
        snowflakeUtils.createTable(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, listColumnNormal);
        snowflakeUtils.createTable(TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, listColumnNoClientId);
        snowflakeUtils.createTable(TABLE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN, listColumnNormal);
        snowflakeUtils.createTable(TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, listColumnNoClientId);
        snowflakeUtils.createTable(TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, listColumnNormal);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, listDeleteColumnNoClientId);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, listDeleteColumnNormal);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, listDeleteColumnNoClientId);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN, listDeleteColumnNormal);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, listDeleteColumnNoClientId);
        snowflakeUtils.createTable(TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, listDeleteColumnNormal);
    }

    private void setUpDataMapping() {
        List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
        listProjectIdMapping.add(Pair.of(projectMappingPID, CLIENT_ID));
        List<Pair<String, String>> listClientIdMapping = new ArrayList<>();
        listClientIdMapping.add(Pair.of(CLIENT_ID, CLIENT_ID));
        List<Pair<String, String>> listProjectIdMappingBoth = new ArrayList<>();
        listProjectIdMappingBoth.add(Pair.of(projectMappingBoth, CLIENT_ID));

        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, asList(), dataSourceId,
                testParams.getProjectId());
        dataMappingProjectIdUtils.createDataMapping();

        dataMappingClientIdUtils = new DataMappingUtils(testParams.getDomainUser(), asList(), listClientIdMapping,
                dataSourceSecondId, testParams.getProjectId());
        dataMappingClientIdUtils.createDataMapping();

        dataMappingBothIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMappingBoth, listClientIdMapping,
                dataSourceThirdId, testParams.getProjectId());
        dataMappingBothIdUtils.createDataMapping();
    }

    private void setUpProcess() {
        try {
            dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(projectFirst, PROCESS_NAME,
                    dataSourceId, "1");
            dataloadProcessSecond = new ScheduleUtils(domainRestClient).createDataDistributionProcess(projectSecond, PROCESS_NAME,
                    dataSourceSecondId, "1");
            dataloadProcessThird = new ScheduleUtils(domainRestClient).createDataDistributionProcess(projectThird, PROCESS_NAME,
                    dataSourceThirdId, "1");
            domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
            domainProcessUtilsSecond = new ProcessUtils(domainRestClient, dataloadProcessSecond);
            domainProcessUtilsThird = new ProcessUtils(domainRestClient, dataloadProcessThird);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create process" + e.getMessage());
        }
    }

    private void prepareTimestamp() {
        lastSuccessful = LocalDateTime.now().withNano(0);
        timeForceFullLoad = parseToTimeStampFormat(lastSuccessful);
        timeLoadFrom = parseToTimeStampFormat(lastSuccessful.plusSeconds(3));
        timeLoadTo = parseToTimeStampFormat(lastSuccessful.plusSeconds(5));
        timeOverRange = parseToTimeStampFormat(lastSuccessful.plusSeconds(7));
    }
}
