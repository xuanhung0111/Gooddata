package com.gooddata.qa.graphene.redshift;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetDelete;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetUpdateColumn;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetUseAmount;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.ATTR_ADDRESS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.ATTR_CITY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_ADDRESS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_CITY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATE_BIRTHDAY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATE_DATASET_BIRTHDAY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.LIMIT_RECORDS;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.VARCHAR_TYPE;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.md.Attribute;
import com.gooddata.project.Project;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.S3Utils;
import com.gooddata.qa.utils.cloudresources.ConnectionInfo;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.cloudresources.ProcessUtils;
import com.gooddata.qa.utils.cloudresources.RedshiftUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.schedule.ScheduleUtils;

public class RedShiftSegmentLoadTest extends AbstractADDProcessTest {
    private String dataSourceId;
    private String serviceProjectId;
    private String clientProjectId1;
    private String clientProjectId2;
    private Project serviceProject;
    private Project project1;
    private Project project2;
    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private IndigoRestRequest indigoRestRequest;
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DASHBOARD_NAME = "Dashboard Test";
    private final String INSIGHT_NAME = "Insight Test";
    private final String DATABASE_NAME = "dev";
    private final String SCHEMA_NAME = "autoschema" + generateHashString() + "__" + getCurrentDate();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String OTHER_CLIENT_ID = "att_other_client_" + generateHashString();
    private final String DEFAULT_S3_BUCKET_URI = "s3://msf-dev-grest/";
    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private LocalDateTime lastSecondSuccessful;
    private LocalDateTime lastThirdSuccessful;
    private String time;
    private String timeSecond;
    private String timeThird;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;
    private RedshiftUtils redshiftUtils;
    private DataSourceUtils dataSource;
    private ProcessUtils domainProcessUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private String defaultS3AccessKey;
    private String defaultS3SecretKey;

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @Override
    protected void customizeProject() throws Throwable {
        domainRestClient = new RestClient(getProfile(DOMAIN));
        dataSource = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        defaultS3AccessKey = testParams.loadProperty("s3.accesskey");
        defaultS3SecretKey = testParams.loadProperty("s3.secretkey");
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws IOException, SQLException {
        createLCM();
        setUpDatasource();
        setUpModel();
        setUpKPIs();
        setUpDatabase();
        setUpProcess();
        prepareTimestamp();
    }

    /**
     * In the Service project, run full load Snowflake ADD to distribute data to client projects for the first time.
     */
    @DataProvider
    public Object[][] dataFirstFullLoad() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetUseAmount().rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID_1))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, project1, clientProjectId1,
                CLIENT_ID_1 } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstFullLoad")
    public void checkFirstFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
            Project project, String projectId, String clientId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        File file = new File(csvfile.getFilePath());
        // Upload data to S3
        String fileName = "customers_" + generateHashString() + ".csv";
        S3Utils.uploadFile(file, "ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(table, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileName, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Run ADD process
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results grey page
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", clientId);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        redshiftUtils.executeCommandsForSpecificWarehouse();
        custkeyValues = redshiftUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);

        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, clientId, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        // Check results UI
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$30.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$30.00"), "Unconnected filter make impact to insight");
    }

    /**
     * Customer has more data so schedule/manual run incremental load
     * and distribute data to client projects.
     */
    @DataProvider
    public Object[][] dataSecondIncrementalLoad() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetUseAmount().rows(asList("CUS5", "User5", "2019-09-30", "15", timeSecond, "0", CLIENT_ID_1))
                        .rows(asList("CUS6", "User6", "2019-09-30", "15", timeSecond, "false", CLIENT_ID_1))
                        .rows(asList("CUS1", "User1", "2019-09-30", "10", timeSecond, "true", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeSecond, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeSecond, CLIENT_ID_1)).rows(asList("CUS3", timeSecond, OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, project1, clientProjectId1, CLIENT_ID_1,
                asList("CUS3", "CUS5", "CUS6") } };
    }

    @Test(dependsOnMethods = { "checkFirstFullLoad" }, dataProvider = "dataSecondIncrementalLoad")
    public void checkSecondIncrementalLoad(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, Project project, String projectId, String clientId, List<String> expectedResult)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        log.info("This is path of CSV File :" + csvFileDelete.getFilePath());
        File file = new File(csvfile.getFilePath());
        File fileDelete = new File(csvFileDelete.getFilePath());
        // Upload data to S3
        String fileName = "customers_" + generateHashString() + ".csv";
        String fileDeleteName = "customers_delete" + generateHashString() + ".csv";
        S3Utils.uploadFile(file, "ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(table, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileName, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.uploadFile(fileDelete, "ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey,
                DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(deleteTable, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileDeleteName,
                defaultS3AccessKey, defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        S3Utils.deleteFile("ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Run ADD process
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                projectId, clientId, dataset, lastSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount() - 1)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        // Check result UI
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$40.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$40.00"), "Unconnected filter make impact to insight");
    }

    /**
     * The business change after time, customer has more data with LDM and data structure which changed also.
     */
    @Test(dependsOnMethods = { "checkSecondIncrementalLoad" })
    public void changeBusiness() {
        updateModel();
        updateDatabase();
        updateLCM();
    }

    /**
     * In Service project, run incremental load, private KPI and AD on client project are kept.
     */
    @DataProvider
    public Object[][] dataThirdIncrementalLoad() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetUpdateColumn()
                        .rows(asList("CUS7", "User7", "2019-09-30", "10", timeThird, "0", CLIENT_ID_1, "address7", "city7", "10"))
                        .rows(asList("CUS8", "User8", "2019-09-30", "10", timeThird, "false", CLIENT_ID_1, "address8", "city8",
                                "10"))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeThird, "true", CLIENT_ID_1, "address3", "city3",
                                "10"))
                        .rows(asList("CUS6", "User6", "2019-09-30", "15", timeThird, "false", OTHER_CLIENT_ID, "address6",
                                "city6", "10")),
                datasetDelete().rows(asList("CUS5", timeThird, CLIENT_ID_1)).rows(asList("CUS6", timeThird, OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, project1, clientProjectId1, CLIENT_ID_1,
                asList("CUS6", "CUS7", "CUS8") } };
    }

    @Test(dependsOnMethods = { "checkSecondIncrementalLoad" }, dataProvider = "dataThirdIncrementalLoad")
    public void checkThirdLoadWithNewLDM(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete, String dataset,
            String attribute, Project project, String projectId, String clientId, List<String> expectedResult)
            throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        log.info("This is path of CSV File :" + csvFileDelete.getFilePath());
        File file = new File(csvfile.getFilePath());
        File fileDelete = new File(csvFileDelete.getFilePath());
        // Upload data to S3
        String fileName = "customers_" + generateHashString() + ".csv";
        String fileDeleteName = "customers_delete" + generateHashString() + ".csv";
        S3Utils.uploadFile(file, "ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(table, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileName, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.uploadFile(fileDelete, "ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey,
                DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(deleteTable, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileDeleteName,
                defaultS3AccessKey, defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        S3Utils.deleteFile("ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Run ADD process
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format(
                "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]",
                projectId, clientId, dataset, lastSecondSuccessful, lastSecondSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, delete_table, rows=%s", dataset, csvFileDelete.getDataRowCount() - 1)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 2)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        // Check result UI
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$35.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$35.00"), "Unconnected filter make impact to insight");
    }

    @Test(dependsOnMethods = { "checkThirdLoadWithNewLDM" })
    public void cleanUpDataWithNewLDM() {
        cleanUpTable();
    }

    /**
     * Customer want to clean everything with new LDM -> they will need to force full load but keep all KPI and AD design.
     */
    @DataProvider
    public Object[][] dataFourthForceFullLoad() throws IOException {
        return new Object[][] { { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                datasetUpdateColumn()
                        .rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID_1, "address1", "city1", "10"))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID_1, "address2", "city2", "10"))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID_1, "address3", "city3", "10"))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID, "address4", "city4",
                                "10")),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PKCOLUMN_CUSKEY, PK_CUSKEY, project1, clientProjectId1,
                CLIENT_ID_1 } };
    }

    @Test(dependsOnMethods = { "cleanUpDataWithNewLDM" }, dataProvider = "dataFourthForceFullLoad")
    public void checkFourthForceFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
            Project project, String projectId, String clientId) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        File file = new File(csvfile.getFilePath());
        // Upload data to S3
        String fileName = "customers_" + generateHashString() + ".csv";
        S3Utils.uploadFile(file, "ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(table, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileName, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Run ADD process
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results grey page
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", clientId);
        String conditionString2 = "= 0";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        custkeyValues = redshiftUtils.getRecordsByCondition(table, column, conditions, null, LIMIT_RECORDS);
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, clientId, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        // Check results UI
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$30.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$30.00"), "Unconnected filter make impact to insight");
    }

    /**
     * Customer had promotional events in some specific agents. So they run force incremental load
     * for defined period with GDC_TARGET_PROJECTS
     */
    @DataProvider
    public Object[][] dataFifthLoadForceIncremental() throws IOException {
        return new Object[][] {
                { TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                        datasetUpdateColumn()
                                .rows(asList("CUS5", "User5", "2019-09-30", "15", timeLoadFrom, "0", CLIENT_ID_1, "address5",
                                        "city5", "10"))
                                .rows(asList("CUS6", "User6", "2019-09-30", "15", timeLoadTo, "false", CLIENT_ID_1, "address6",
                                        "city6", "10"))
                                .rows(asList("CUS1", "User1", "2019-09-30", "10", timeLoadFrom, "true", CLIENT_ID_1, "address1",
                                        "city1", "10"))
                                .rows(asList("CUS3", "User3", "2019-09-30", "10", timeOverRange, "true", CLIENT_ID_1, "address3",
                                        "city3", "10"))
                                .rows(asList("CUS5", "User5", "2019-09-30", "15", timeLoadFrom, "false", CLIENT_ID_2, "address5",
                                        "city5", "10")),
                        datasetDelete().rows(asList("CUS2", timeLoadFrom, CLIENT_ID_1))
                                .rows(asList("CUS3", timeLoadFrom, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, PK_CUSKEY, project1, clientProjectId1, project2,
                        clientProjectId2, CLIENT_ID_1, asList("CUS2", "CUS3", "CUS5", "CUS6") } };
    }

    @Test(dependsOnMethods = { "checkFourthForceFullLoad" }, dataProvider = "dataFifthLoadForceIncremental")
    public void checkFifthLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
            String dataset, String attribute, Project project, String projectId, Project projectNotLoad, String projectIdNotLoad,
            String clientId, List<String> expectedResult) throws SQLException, IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        log.info("This is path of CSV File :" + csvFileDelete.getFilePath());
        File file = new File(csvfile.getFilePath());
        File fileDelete = new File(csvFileDelete.getFilePath());
        // Upload data to S3
        String fileName = "customers_" + generateHashString() + ".csv";
        String fileDeleteName = "customers_delete" + generateHashString() + ".csv";
        S3Utils.uploadFile(file, "ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(table, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileName, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.uploadFile(fileDelete, "ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey,
                DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(deleteTable, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileDeleteName,
                defaultS3AccessKey, defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        S3Utils.deleteFile("ATT_Redshift/" + fileDeleteName, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Run ADD process
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo)
                .addParameter("GDC_TARGET_PROJECTS", clientProjectId1);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format(
                "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]",
                projectId, clientId, dataset, lastSuccessful, lastThirdSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 3)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        Attribute attributeCustkeyNotLoad = getMdService().getObj(projectNotLoad, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        assertTrue(getAttributeValues(attributeCustkeyNotLoad).isEmpty());
        // Check result UI
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$50.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$50.00"), "Unconnected filter make impact to insight");

        initIndigoDashboardsPageSpecificProject(projectIdNotLoad).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(), "–",
                "Unconnected filter make impact to kpi");
        assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME).isEmptyValue(),
                "The empty state of insight is not correct");
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
        lcmBrickFlowBuilder.destroy();
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        redshiftUtils.dropTables(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN);
        redshiftUtils.dropSchemaIfExists();
        redshiftUtils.closeRedshiftConnection();
    }

    private void setUpDatasource() throws SQLException, IOException {
        // use the same Connection Info for 3 datasources.
        ConnectionInfo connectionInfo = dataSource.createRedshiftConnectionInfo(DATABASE_NAME, DatabaseType.REDSHIFT,
                SCHEMA_NAME);
        redshiftUtils = new RedshiftUtils(connectionInfo);
        redshiftUtils.createSchema();
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
    }

    private void setUpModel() {
        // setUp Model projects
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AMOUNT).withDates(DATE_BIRTHDAY);
        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).buildMaqlUsingPrimaryKey());

    }

    private void setUpKPIs() {
        getMetricCreator().createSumAmountMetric();
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_BIRTHDAY)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AMOUNT));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME)
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
    }

    private void setUpDatabase() throws SQLException, IOException {
        String sql = getResourceAsString("/sql-file/customers_update.txt");
        String sql_delete = getResourceAsString("/sql-file/customers_delete.txt");
        redshiftUtils.executeCommandsForSpecificWarehouse();
        redshiftUtils.executeSql(sql);
        redshiftUtils.executeSql(sql_delete);
    }

    private void setUpProcess() {
        try {
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create process" + e.getMessage());
        }
        updateLCM();
    }

    private void updateModel() {
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN)
                .withAttributes(ATTR_ADDRESS, ATTR_CITY).withFacts(FACT_AGE);
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).buildMaqlUpdateModel());
    }

    private void updateLCM() {
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
    }

    private void updateDatabase() {
        redshiftUtils.addColumn(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, COLUMN_ADDRESS, VARCHAR_TYPE);
        redshiftUtils.addColumn(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, COLUMN_CITY, VARCHAR_TYPE);
        redshiftUtils.addColumn(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN, COLUMN_AGE, NUMERIC_TYPE);
    }

    private void cleanUpTable() {
        redshiftUtils.truncateTable(TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN);
    }

    private void prepareTimestamp() {
        lastSuccessful = LocalDateTime.now().withNano(0);
        lastSecondSuccessful = lastSuccessful.plusSeconds(5);
        lastThirdSuccessful = lastSuccessful.plusSeconds(10);
        time = parseToTimeStampFormat(lastSuccessful);
        timeSecond = parseToTimeStampFormat(lastSecondSuccessful);
        timeThird = parseToTimeStampFormat(lastThirdSuccessful);
        timeLoadFrom = parseToTimeStampFormat(lastSuccessful.plusSeconds(20));
        timeLoadTo = parseToTimeStampFormat(lastSuccessful.plusSeconds(25));
        timeOverRange = parseToTimeStampFormat(lastSuccessful.plusSeconds(30));
    }

    protected Metrics getMetricCreator() {
        return new Metrics(domainRestClient, testParams.getProjectId());
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private String getCurrentDate() {
        return DateTime.now().toString("YYYY_MM_dd_HH_mm_ss");
    }

    private void createLCM() throws ParseException, IOException {
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        serviceProjectId = lcmBrickFlowBuilder.getLCMServiceProject().getServiceProjectId();
        serviceProject = domainRestClient.getProjectService().getProjectById(serviceProjectId);

        String devProjectId = testParams.getProjectId();
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
