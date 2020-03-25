package com.gooddata.qa.graphene.bigquery;

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
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.StandardSQLTypeName;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.cloudresources.BigQueryUtils.deleteDataset;
import static com.gooddata.qa.utils.cloudresources.BigQueryUtils.deleteTable;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.*;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.*;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BigQuerySegmentLoadTest extends AbstractADDProcessTest {
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
    private final String DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN + "_" + getCurrentDate();
    private final String TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN.replace("CUSTOMERSMAPPINGPROJECTID2", DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    private final String TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN.replace("CUSTOMERSMAPPINGPROJECTID2", DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    private IndigoRestRequest indigoRestRequest;
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DASHBOARD_NAME = "Dashboard Test";
    private final String INSIGHT_NAME = "Insight Test";
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String OTHER_CLIENT_ID = "att_other_client_" + generateHashString();
    private final String BIGQUERY_PROJECT = "gdc-us-dev";
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
    private BigQueryUtils bigqueryUtils;
    private DataSourceUtils dataSource;
    private ProcessUtils domainProcessUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private ConnectionInfo connectionInfo;

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @Override
    protected void customizeProject() {
        domainRestClient = new RestClient(getProfile(DOMAIN));
        dataSource = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        connectionInfo = dataSource.createBigQueryConnectionInfo(BIGQUERY_PROJECT,
                DatabaseType.BIGQUERY, DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    private static final List<Field> DATATYPES_SCHEMA = Arrays.asList(
            Field.newBuilder(PKCOLUMN_CUSKEY, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_NAME, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_BIRTHDAY, StandardSQLTypeName.DATE).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_AMOUNT, StandardSQLTypeName.NUMERIC).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_TIMESTAMP, StandardSQLTypeName.TIMESTAMP).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_DELETED, StandardSQLTypeName.BOOL).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_CLIENT_ID, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
    );

    private static final List<Field> DATATYPES_SCHEMA_DELETE = Arrays.asList(
            Field.newBuilder(PKCOLUMN_CUSKEY, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_TIMESTAMP, StandardSQLTypeName.TIMESTAMP).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_CLIENT_ID, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
    );

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initData() throws IOException {
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
    public Object[][] dataFirstFullLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID_1))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PKCOLUMN_CUSKEY, PK_CUSKEY, project1, clientProjectId1,
                CLIENT_ID_1}};
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dataFirstFullLoad")
    public void checkFirstFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                   Project project, String projectId, String clientId) throws IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        log.info("load data to tables in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        // Run ADD process
        log.info("Running ADD process .................... ");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results grey page
        log.info("Checking results on GreyPage .................... ");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", clientId);
        String conditionString2 = "= false";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        custkeyValues = bigqueryUtils.getRecordsByCondition(dataset, table, column, conditions, null, LIMIT_RECORDS);

        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, clientId, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        // Check results UI
        log.info("Checking results on UI .................... ");
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
    public Object[][] dataSecondIncrementalLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS5", "User5", "2019-09-30", "15", timeSecond, "0", CLIENT_ID_1))
                        .rows(asList("CUS6", "User6", "2019-09-30", "15", timeSecond, "false", CLIENT_ID_1))
                        .rows(asList("CUS1", "User1", "2019-09-30", "10", timeSecond, "true", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeSecond, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeSecond, CLIENT_ID_1)).rows(asList("CUS3", timeSecond, OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PK_CUSKEY, project1, clientProjectId1, CLIENT_ID_1,
                asList("CUS3", "CUS5", "CUS6")}};
    }

    @Test(dependsOnMethods = {"checkFirstFullLoad"}, dataProvider = "dataSecondIncrementalLoad")
    public void checkSecondIncrementalLoad(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
                                           String dataset, String attribute, Project project, String projectId, String clientId, List<String> expectedResult)
            throws IOException {
        // Create Delete Table in BigQuery DataBase
        BigQueryUtils.createTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, DATATYPES_SCHEMA_DELETE);
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        Path csvDeletePath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_DELETE_CUSTOMERS + ".csv");
        log.info("load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        BigQueryUtils.writeFileToTable(dataset, deleteTable, DATATYPES_SCHEMA_DELETE, csvDeletePath);
        // Run ADD process
        log.info("Running ADD process .................... ");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        log.info("Checking result on GreyPage .................... ");
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
        log.info("Checking result on UI .................... ");
        initIndigoDashboardsPageSpecificProject(projectId).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$40.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$40.00"), "Unconnected filter make impact to insight");
    }

    @Test(dependsOnMethods = {"checkSecondIncrementalLoad"})
    public void cleanUpDataWithNewLDM() {
        cleanUpTable();
    }

    /**
     * Customer want to clean everything with new LDM -> they will need to force full load but keep all KPI and AD design.
     */
    @DataProvider
    public Object[][] dataFourthForceFullLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID_1))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID_1))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID_1))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PKCOLUMN_CUSKEY, PK_CUSKEY, project1, clientProjectId1,
                CLIENT_ID_1}};
    }

    @Test(dependsOnMethods = {"cleanUpDataWithNewLDM"}, dataProvider = "dataFourthForceFullLoad")
    public void checkFourthForceFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                         Project project, String projectId, String clientId) throws IOException {
        bigqueryUtils.createTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        csvfile.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        log.info("load data to tables in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        // Run ADD process
        log.info("Running ADD process .................... ");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results grey page
        log.info("Checking results on GreyPage .................... ");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", clientId);
        String conditionString2 = "= false";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        custkeyValues = bigqueryUtils.getRecordsByCondition(dataset, table, column, conditions, null, LIMIT_RECORDS);
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, clientId, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        // Check results UI
        log.info("Checking results on UI .................... ");
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
    public Object[][] dataFifthLoadForceIncremental() {
        return new Object[][]{
                {TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                        datasetUpdateColumn()
                                .rows(asList("CUS5", "User5", "2019-09-30", "15", timeLoadFrom, "0", CLIENT_ID_1))
                                .rows(asList("CUS6", "User6", "2019-09-30", "15", timeLoadTo, "false", CLIENT_ID_1))
                                .rows(asList("CUS1", "User1", "2019-09-30", "10", timeLoadFrom, "true", CLIENT_ID_1))
                                .rows(asList("CUS3", "User3", "2019-09-30", "10", timeOverRange, "true", CLIENT_ID_1))
                                .rows(asList("CUS5", "User5", "2019-09-30", "15", timeLoadFrom, "false", CLIENT_ID_2)),
                        datasetDelete().rows(asList("CUS2", timeLoadFrom, CLIENT_ID_1))
                                .rows(asList("CUS3", timeLoadFrom, OTHER_CLIENT_ID)),
                        DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PK_CUSKEY, project1, clientProjectId1, project2,
                        clientProjectId2, CLIENT_ID_1, asList("CUS2", "CUS3", "CUS5", "CUS6")}};
    }

    @Test(dependsOnMethods = {"checkFourthForceFullLoad"}, dataProvider = "dataFifthLoadForceIncremental")
    public void checkFifthLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
                                           String dataset, String attribute, Project project, String projectId, Project projectNotLoad, String projectIdNotLoad,
                                           String clientId, List<String> expectedResult) throws IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        Path csvDeletePath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_DELETE_CUSTOMERS + ".csv");
        log.info("load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        BigQueryUtils.writeFileToTable(dataset, deleteTable, DATATYPES_SCHEMA_DELETE, csvDeletePath);

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
    public void cleanUp() throws ParseException, JSONException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcess);
        }
        lcmBrickFlowBuilder.destroy();
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        deleteDataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    private void setUpDatasource() throws IOException {
        // use the same Connection Info for 3 datasources.
        log.info("Setup Datasource .......................");
        ConnectionInfo connectionInfo = dataSource.createBigQueryConnectionInfo(BIGQUERY_PROJECT, DatabaseType.BIGQUERY,
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        bigqueryUtils = new BigQueryUtils(connectionInfo);
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
    }

    private void setUpModel() {
        log.info("Setup Model .......................");
        // setUp Model projects
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AMOUNT).withDates(DATE_BIRTHDAY);
        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).buildMaqlUsingPrimaryKey());

    }

    private void setUpKPIs() {
        log.info("Setup KPIs .......................");
        getMetricCreator().createSumAmountMetric();
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_BIRTHDAY)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AMOUNT));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME)
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
    }

    private void setUpDatabase() {
        log.info("Setup Database...............");
        // setUp Model projects
        bigqueryUtils = new BigQueryUtils(connectionInfo);
        log.info("Create Dataset  : " + bigqueryUtils.createDataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE).getDatasetId());
        // Create Table in BigQuery DataBase
        BigQueryUtils.createTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, DATATYPES_SCHEMA);
    }

    private void setUpProcess() {
        log.info("Setup Process .......................");
        try {
            dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, PROCESS_NAME,
                    dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
            domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
            updateLCM();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create process" + e.getMessage());
        }
    }

    private void updateLCM() {
        log.info("Update LCM .......................");
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        log.info("LCM updated successfully .......................");
    }

    private void cleanUpTable() {
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    private void prepareTimestamp() {
        log.info("Setup Timestamp .......................");
        lastSuccessful = LocalDateTime.now().withNano(0);
        lastSecondSuccessful = lastSuccessful.plusSeconds(5);
        lastThirdSuccessful = lastSuccessful.plusSeconds(5);
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

    private void createLCM() throws ParseException, IOException {
        log.info("Creating LCM .......................");
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

    private String getCurrentDate() {
        return DateTime.now().toString("YYYY_MM_dd_HH_mm_ss");
    }
}
