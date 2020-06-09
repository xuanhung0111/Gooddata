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

public class CustomCurrentLoadTest extends AbstractADDProcessTest {
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
    private LocalDateTime lastSecondSuccessful;
    private LocalDateTime lastThirdSuccessful;
    private LocalDateTime lastFourthSuccessful;
    private String time;
    private String timeSecond;
    private String timeThird;
    private String timeFourth;
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
                        datasetNormal().rows(asList("CUS1", "User", "28", time, "0", CLIENT_ID))
                                .rows(asList("CUS2", "User2", "28", time, "false", CLIENT_ID))
                                .rows(asList("CUS3", "User3", "28", time, "FALSE", CLIENT_ID))
                                .rows(asList("CUS4", "User4", "28", time, "FALSE", OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtils,
                        projectFirst, projectMappingPID },
                { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS1B", "UserB", "28", time, "0", CLIENT_ID))
                                .rows(asList("CUS2B", "User2B", "28", time, "false", CLIENT_ID))
                                .rows(asList("CUS3B", "User3B", "28", time, "FALSE", CLIENT_ID))
                                .rows(asList("CUS4B", "User4B", "28", time, "FALSE", OTHER_CLIENT_ID)),
                        DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsThird,
                        projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkFirstLoadWithClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            ProcessUtils processUtils, Project project, String projectId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
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
                        datasetTimeStampDeleted().rows(asList("CUS1C", "User1C", "28", time, "0"))
                                .rows(asList("CUS2C", "User2C", "28", time, "false"))
                                .rows(asList("CUS3C", "User3C", "28", time, "0")),
                        DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtils,
                        projectFirst, projectMappingPID },
                { TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS1D", "User1D", "28", time, "0"))
                                .rows(asList("CUS2D", "User2D", "28", time, "false"))
                                .rows(asList("CUS3D", "User3D", "28", time, "0")),
                        DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsSecond,
                        projectSecond, projectMappingClientID },
                { TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS1E", "User1E", "28", time, "0"))
                                .rows(asList("CUS2E", "User2E", "28", time, "false"))
                                .rows(asList("CUS3E", "User3E", "28", time, "0")),
                        DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, domainProcessUtilsThird,
                        projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadWithClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkFirstLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
            ProcessUtils processUtils, Project project, String projectId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
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
                        datasetNormal().rows(asList("CUS5", "User5", "28", timeSecond, "0", CLIENT_ID))
                                .rows(asList("CUS6", "User6", "28", timeSecond, "false", CLIENT_ID))
                                .rows(asList("CUS1", "User1", "28", timeSecond, "true", CLIENT_ID))
                                .rows(asList("CUS3", "User3", "28", timeSecond, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS2", timeSecond, CLIENT_ID)).rows(
                                asList("CUS3", timeSecond, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst,
                        projectMappingPID, asList("CUS3", "CUS5", "CUS6") },
                { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS5B", "User5B", "28", timeSecond, "false", CLIENT_ID))
                                .rows(asList("CUS6B", "User6B", "28", timeSecond, "false", CLIENT_ID))
                                .rows(asList("CUS1B", "User1B", "28", timeSecond, "true", CLIENT_ID))
                                .rows(asList("CUS3B", "User3B", "28", timeSecond, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS2B", timeSecond, CLIENT_ID))
                                .rows(asList("CUS3B", timeSecond, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtilsThird, projectThird,
                        projectMappingBoth, asList("CUS3B", "CUS5B", "CUS6B") } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadNoClientId" }, dataProvider = "dataSecondLoadHasClientId")
    public void checkSecondLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
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
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                projectId, CLIENT_ID, dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount() - 1)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataThirdLoadHasClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS7", "User7", "28", timeThird, "0", CLIENT_ID))
                                .rows(asList("CUS8", "User8", "28", timeThird, "false", CLIENT_ID))
                                .rows(asList("CUS6", "User6", "28", timeThird, "true", CLIENT_ID))
                                .rows(asList("CUS9", "User9", "28", timeThird, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS3", timeThird, CLIENT_ID)).rows(
                                asList("CUS5", timeThird, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst,
                        projectMappingPID, asList("CUS5", "CUS7", "CUS8") },
                { TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN,
                        datasetNormal().rows(asList("CUS7B", "User7B", "28", timeThird, "false", CLIENT_ID))
                                .rows(asList("CUS8B", "User8B", "28", timeThird, "false", CLIENT_ID))
                                .rows(asList("CUS6B", "User6B", "28", timeThird, "true", CLIENT_ID))
                                .rows(asList("CUS9B", "User9B", "28", timeThird, "false", OTHER_CLIENT_ID)),
                        datasetDelete().rows(asList("CUS3B", timeThird, CLIENT_ID))
                                .rows(asList("CUS5B", timeThird, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtilsThird, projectThird,
                        projectMappingBoth, asList("CUS5B", "CUS7B", "CUS8B") } };
    }

    @Test(dependsOnMethods = { "checkSecondLoadWithClientId" }, dataProvider = "dataThirdLoadHasClientId")
    public void checkThirdLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
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
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format(
                "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]",
                projectId, CLIENT_ID, dataset, lastSecondSuccessful, lastSecondSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount() - 1)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataSecondLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4C", "User4C", "28", timeSecond, "0"))
                                .rows(asList("CUS5C", "User5C", "28", timeSecond, "false"))
                                .rows(asList("CUS1C", "User1C", "28", timeSecond, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2C", timeSecond)),
                        DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS3C", "CUS4C", "CUS5C"),
                        domainProcessUtils, projectFirst, projectMappingPID },
                { TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4D", "User4D", "28", timeSecond, "0"))
                                .rows(asList("CUS5D", "User5D", "28", timeSecond, "false"))
                                .rows(asList("CUS1D", "User1D", "28", timeSecond, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2D", timeSecond)),
                        DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS3D", "CUS4D", "CUS5D"),
                        domainProcessUtilsSecond, projectSecond, projectMappingClientID },
                { TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS4E", "User4E", "28", timeSecond, "0"))
                                .rows(asList("CUS5E", "User5E", "28", timeSecond, "false"))
                                .rows(asList("CUS1E", "User1E", "28", timeSecond, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS2E", timeSecond)),
                        DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, PK_CUSKEY, asList("CUS3E", "CUS4E", "CUS5E"),
                        domainProcessUtilsThird, projectThird, projectMappingBoth } };
    }

    @Test(dependsOnMethods = { "checkThirdLoadWithClientId" }, dataProvider = "dataSecondLoadNoClientId")
    public void checkSecondLoadNoClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
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
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format("Project=\"%s\"", projectId)));
        assertThat(executionLog,
                containsString(String.format("datasets=[{dataset.%s, incremental, loadDataFrom=%s}]", dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount())));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 1)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataThirdLoadNoClientId() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS6C", "User6C", "28", timeThird, "0"))
                                .rows(asList("CUS7C", "User7C", "28", timeThird, "false"))
                                .rows(asList("CUS3C", "User3C", "28", timeThird, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS4C", timeThird)),
                        DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst,
                        projectMappingPID, asList("CUS5C", "CUS6C", "CUS7C") },
                { TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS6D", "User6", "28", timeThird, "0"))
                                .rows(asList("CUS7D", "User7D", "28", timeThird, "false"))
                                .rows(asList("CUS3D", "User3D", "28", timeThird, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS4D", timeThird)),
                        DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtilsSecond, projectSecond,
                        projectMappingClientID, asList("CUS5D", "CUS6D", "CUS7D") },
                { TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN,
                        datasetTimeStampDeleted().rows(asList("CUS6E", "User6E", "28", timeThird, "0"))
                                .rows(asList("CUS7E", "User7E", "28", timeThird, "false"))
                                .rows(asList("CUS3E", "User3E", "28", timeThird, "true")),
                        datasetDeleteTimeStampDeleted().rows(asList("CUS4E", timeThird)),
                        DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtilsThird, projectThird,
                        projectMappingBoth, asList("CUS5E", "CUS6E", "CUS7E") } };
    }

    @Test(dependsOnMethods = { "checkSecondLoadNoClientId" }, dataProvider = "dataThirdLoadNoClientId")
    public void checkThirdLoadNoClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete, String dataset,
            String attribute, ProcessUtils processUtils, Project project, String projectId, List<String> expectedResult)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", deleteTable, csvFileDelete.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(dataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format("Project=\"%s\"", projectId)));
        assertThat(executionLog,
                containsString(String.format("datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]", dataset,
                        lastSecondSuccessful, lastSecondSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount())));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 1)));
        Attribute attributeCustkey = getMdService().getObj(getAdminRestClient().getProjectService().getProjectById(projectId),
                Attribute.class, identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
    }

    @DataProvider
    public Object[][] dataLoadMappingClientId() throws IOException {
        return new Object[][] { { TABLE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS6", "User6", "28", timeThird, "0", CLIENT_ID))
                        .rows(asList("CUS7", "User7", "28", timeThird, "false", CLIENT_ID)),
                DATASET_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY } };
    }

    @Test(dependsOnMethods = { "checkThirdLoadNoClientId" }, dataProvider = "dataLoadMappingClientId")
    public void checkLoadMappingOnlyClientId(String table, CsvFile csvfile, String dataset, String attribute)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtilsSecond.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtilsSecond.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        String errorMessage = "The output stage has column x__client_id, but data mapping is not specified for the current project";
        String errorReturn = domainProcessUtilsSecond.executeError(parameters);
        assertThat(errorReturn, containsString(errorMessage));
    }

    @DataProvider
    public Object[][] dataLoadUpdateDataMapping() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetNormal().rows(asList("CUS9", "User9", "28", timeFourth, "0", UPDATE_CLIENT_ID))
                        .rows(asList("CUS10", "User10", "28", timeFourth, "false", UPDATE_CLIENT_ID))
                        .rows(asList("CUS5", "User5", "28", timeFourth, "true", UPDATE_CLIENT_ID))
                        .rows(asList("CUS11", "User11", "28", timeFourth, "false", CLIENT_ID)),
                datasetDelete().rows(asList("CUS7", timeFourth, UPDATE_CLIENT_ID)).rows(asList("CUS8", timeFourth, CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, domainProcessUtils, projectFirst, projectMappingPID,
                asList("CUS8", "CUS9", "CUS10"), dataMappingProjectIdUtils, projectMappingPID, UPDATE_CLIENT_ID } };
    }

    @Test(dependsOnMethods = { "checkLoadMappingOnlyClientId" }, dataProvider = "dataLoadUpdateDataMapping")
    public void checkUpdateDataMapping(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete, String dataset,
            String attribute, ProcessUtils processUtils, Project project, String projectId, List<String> expectedResult,
            DataMappingUtils dataMappingUtils, String mappingKey, String mappingValue) throws IOException, SQLException {
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
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format(
                "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]",
                projectId, mappingValue, dataset, lastThirdSuccessful, lastThirdSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount() - 1)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
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

    protected String createNewEmptyProject(final String projectTitle) {
        TestParameters testParameters = TestParameters.getInstance();
        final Project project = new Project(projectTitle, testParameters.getAuthorizationToken());
        project.setDriver(testParameters.getProjectDriver());
        project.setEnvironment(testParameters.getProjectEnvironment());

        return domainRestClient.getProjectService().createProject(project)
                .get(testParameters.getCreateProjectTimeout(), TimeUnit.MINUTES).getId();
    }

    private void setUpProject() throws IOException {
        projectMappingPID = testParams.getProjectId();
        projectMappingClientID = createNewEmptyProject("Project Using Mapping ProjectID");
        projectMappingBoth = createNewEmptyProject("Project Using Mapping Both ID");
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
        lastSecondSuccessful = lastSuccessful.plusSeconds(5);
        lastThirdSuccessful = lastSuccessful.plusSeconds(10);
        lastFourthSuccessful = lastSuccessful.plusSeconds(15);
        time = parseToTimeStampFormat(lastSuccessful);
        timeSecond = parseToTimeStampFormat(lastSecondSuccessful);
        timeThird = parseToTimeStampFormat(lastThirdSuccessful);
        timeFourth = parseToTimeStampFormat(lastFourthSuccessful);
    }
}
