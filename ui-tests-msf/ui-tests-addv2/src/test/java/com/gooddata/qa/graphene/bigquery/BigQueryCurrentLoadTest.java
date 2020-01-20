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
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
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

public class BigQueryCurrentLoadTest extends AbstractADDProcessTest {

    private Project projectTest;
    private IndigoRestRequest indigoRestRequest;
    private String projectMappingPID;
    private final String DATA_SOURCE_NAME = "BigQuery_datasource" + generateHashString();
    private final String DASHBOARD_NAME = "BigQuery Dashboard Test";
    private final String INSIGHT_NAME = "BigQuery Insight Test";
    private final String PROCESS_NAME = "BigQuery AutoProcess Test" + generateHashString();
    private final String CLIENT_ID = "bigquery_client_att_" + generateHashString();
    private final String OTHER_CLIENT_ID = "bigquery_other_client_att_" + generateHashString();
    private final String BIGQUERY_PROJECT = "gdc-us-dev";
    private final String DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN + "__" + getCurrentDate();
    private final String TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN.replace("CUSTOMERSMAPPINGPROJECTID2", DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    private final String TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE = TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN.replace("CUSTOMERSMAPPINGPROJECTID2", DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
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
    private DataMappingUtils dataMappingProjectIdUtils;
    private BigQueryUtils bigqueryUtils;
    private DataSourceUtils dataSource;
    private ProcessUtils domainProcessUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private String dataSourceId;
    private ConnectionInfo connectionInfo;

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

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @Override
    protected void customizeProject() {
        domainRestClient = new RestClient(getProfile(DOMAIN));
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        dataSource = new DataSourceUtils(testParams.getDomainUser());
        connectionInfo = dataSource.createBigQueryConnectionInfo(BIGQUERY_PROJECT,
                DatabaseType.BIGQUERY, DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initData() throws IOException {
        setUpProject();
        setUpDatasource();
        setUpDataMapping();
        setUpModel();
        setUpKPIs();
        setUpDatabase();
        setUpProcess();
        prepareTimestamp();
    }

    private void setUpProject() {
        log.info("Setup Project...............");
        projectMappingPID = testParams.getProjectId();
        projectTest = getAdminRestClient().getProjectService().getProjectById(projectMappingPID);
    }

    private void setUpDatasource() throws IOException {
        log.info("Setup Datasource...............");
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
    }

    private void setUpDataMapping() {
        log.info("Setup DataMapping...............");
        List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
        listProjectIdMapping.add(Pair.of(projectMappingPID, CLIENT_ID));
        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, asList(), dataSourceId,
                testParams.getProjectId());
        dataMappingProjectIdUtils.createDataMapping();
    }

    private void setupMaql(String maql, Project project) {
        getAdminRestClient().getModelService().updateProjectModel(project, maql).get();
    }

    private void setUpModel() {
        // setUp Model projects
        log.info("Setup Model...............");
        Dataset datasetMappingProjectId = new Dataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE).withPrimaryKey(PK_CUSKEY)
                .withAttributes(ATTR_NAME).withFacts(FACT_AMOUNT).withDates(DATE_BIRTHDAY);
        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetMappingProjectId).buildMaqlUsingPrimaryKey(), projectTest);

    }

    private void setUpKPIs() {
        log.info("Setup KPIs...............");
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

    /**
     * Create schedules for the process to run full load and distribute data to
     * projects based on client_id.
     */
    @DataProvider
    public Object[][] dataFirstFullLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PKCOLUMN_CUSKEY, PK_CUSKEY, projectTest, projectMappingPID}};
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "dataFirstFullLoad")
    public void checkFirstFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                   Project project, String projectId) throws Exception {
        csvfile.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        log.info("Load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        // setup param to provide execute process
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        // Execute process
        log.info("Executing project .................... ");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results on grey page
        log.info("Checking results on GreyPage .................... ");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", CLIENT_ID);
        String conditionString2 = "= false";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        log.info("Query records By Condition .................... ");
        custkeyValues = bigqueryUtils.getRecordsByCondition(dataset, table, column, conditions, null, LIMIT_RECORDS);
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, CLIENT_ID, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
        // Check results UI
        log.info("Checking results on UI .................... ");
        initIndigoDashboardsPage().selectDateFilterByName("All time").waitForWidgetsLoading();
        String value = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue();
        assertEquals(value, "$30.00", "Unconnected filter make impact to kpi");
        List<String> listValue = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        assertEquals(listValue, singletonList("$30.00"), "Unconnected filter make impact to insight");
    }

    /**
     * Customer have more data which is updated on BigQuery manual scheduled run the
     * ADD process to incremental load data from BigQuery.
     */
    @DataProvider
    public Object[][] dataSecondIncrementalLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS5", "User5", "2019-09-30", "15", timeSecond, "0", CLIENT_ID))
                        .rows(asList("CUS6", "User6", "2019-09-30", "15", timeSecond, "false", CLIENT_ID))
                        .rows(asList("CUS1", "User1", "2019-09-30", "10", timeSecond, "true", CLIENT_ID))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeSecond, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeSecond, CLIENT_ID)).rows(asList("CUS3", timeSecond, OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PK_CUSKEY, projectTest, projectMappingPID,
                asList("CUS3", "CUS5", "CUS6")}};
    }

    @Test(dependsOnMethods = {"checkFirstFullLoad"}, dataProvider = "dataSecondIncrementalLoad")
    public void checkSecondIncrementalLoad(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
                                           String dataset, String attribute, Project project, String projectId, List<String> expectedResult)
            throws IOException {
        // Create Delete Table in BigQuery DataBase
        BigQueryUtils.createTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, DATATYPES_SCHEMA_DELETE);
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        Path csvDeletePath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_DELETE_CUSTOMERS + ".csv");
        log.info("Load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        BigQueryUtils.writeFileToTable(dataset, deleteTable, DATATYPES_SCHEMA_DELETE, csvDeletePath);
        // Run ADD process
        log.info("Running ADD process................");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        log.info("Checking result on GreyPage ...........");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
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
        // Check result UI
        log.info("Checking result on UI ...........");
        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$40.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$40.00"), "Unconnected filter make impact to insight");
    }

    /**
     * Customer need to force full load but keep all KPI and AD design.
     */
    @DataProvider
    public Object[][] dataFourthForceFullLoad() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS1", "User", "2019-09-30", "10", time, "0", CLIENT_ID))
                        .rows(asList("CUS2", "User2", "2019-09-30", "10", time, "false", CLIENT_ID))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", time, "FALSE", CLIENT_ID))
                        .rows(asList("CUS4", "User4", "2019-09-30", "10", time, "FALSE", OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PKCOLUMN_CUSKEY, PK_CUSKEY, projectTest, projectMappingPID}};
    }

    @Test(dependsOnMethods = {"checkSecondIncrementalLoad"})
    public void cleanUpDataWithNewLDM() {
        cleanUpTable();
    }

    private void cleanUpTable() {
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    @Test(dependsOnMethods = {"cleanUpDataWithNewLDM"}, dataProvider = "dataFourthForceFullLoad")
    public void checkFourthForceFullLoad(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                         Project project, String projectId) throws IOException {
        bigqueryUtils.createTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        csvfile.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        log.info("load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        // Run ADD process
        log.info("Running ADD process.......................");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        log.info("Execute Processing.......................");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check results grey page
        log.info("Checking results GreyPage.......................");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        List<String> custkeyValues = new ArrayList<String>();
        List<Pair<String, String>> conditions = new ArrayList<>();
        String conditionString = String.format("= '%s'", CLIENT_ID);
        String conditionString2 = "= false";
        conditions.add(Pair.of(COLUMN_X_CLIENT_ID, conditionString));
        conditions.add(Pair.of(COLUMN_X_DELETED, conditionString2));
        custkeyValues = bigqueryUtils.getRecordsByCondition(dataset, table, column, conditions, null, LIMIT_RECORDS);
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]", projectId, CLIENT_ID, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
        // Check results UI
        log.info("Checking results UI.......................");
        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$30.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$30.00"), "Unconnected filter make impact to insight");
    }

    /**
     * Customer did a promotional eventsthey would like to make reports in the time
     * which event happened Run force incremental load for defined period.
     */
    @DataProvider
    public Object[][] dataFifthLoadForceIncremental() {
        return new Object[][]{{TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE,
                datasetUseAmount().rows(asList("CUS5", "User5", "2019-09-30", "15", timeLoadFrom, "0", CLIENT_ID))
                        .rows(asList("CUS6", "User6", "2019-09-30", "15", timeLoadTo, "false", CLIENT_ID))
                        .rows(asList("CUS1", "User1", "2019-09-30", "10", timeLoadFrom, "true", CLIENT_ID))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeOverRange, "true", CLIENT_ID))
                        .rows(asList("CUS3", "User3", "2019-09-30", "10", timeLoadFrom, "false", OTHER_CLIENT_ID)),
                datasetDelete().rows(asList("CUS2", timeLoadFrom, CLIENT_ID)).rows(asList("CUS3", timeLoadFrom, OTHER_CLIENT_ID)),
                DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, PK_CUSKEY, projectTest, projectMappingPID,
                asList("CUS2", "CUS3", "CUS5", "CUS6")}};
    }

    @Test(dependsOnMethods = {"checkFourthForceFullLoad"}, dataProvider = "dataFifthLoadForceIncremental")
    public void checkFifthLoadWithClientId(String table, String deleteTable, CsvFile csvfile, CsvFile csvFileDelete,
                                           String dataset, String attribute, Project project, String projectId, List<String> expectedResult)
            throws IOException {
        csvfile.saveToDisc(testParams.getCsvFolder());
        csvFileDelete.saveToDisc(testParams.getCsvFolder());
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_CUSTOMERS + ".csv");
        Path csvUpdatePath = Paths.get(testParams.getCsvFolder() + "/" + DATASET_DELETE_CUSTOMERS + ".csv");
        BigQueryUtils.writeFileToTable(dataset, table, DATATYPES_SCHEMA, csvPath);
        BigQueryUtils.writeFileToTable(dataset, deleteTable, DATATYPES_SCHEMA_DELETE, csvUpdatePath);
        // Run ADD process
        log.info("Running ADD process .......................");
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        // Check result greypage
        log.info("Checking result on GreyPage .......................");
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), projectId);
        assertThat(executionLog, containsString(String.format(
                "Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s, deleteDataFrom=%s}]",
                projectId, CLIENT_ID, dataset, lastSuccessful, lastThirdSuccessful)));
        assertThat(executionLog,
                containsString(String.format("dataset.%s, upsert, rows=%s", dataset, csvfile.getDataRowCount() - 3)));
        Attribute attributeCustkey = getMdService().getObj(project, Attribute.class,
                identifier("attr." + dataset + "." + attribute));

        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(expectedResult.toArray()));
        // Check result UI
        log.info("Checking results on UI.......................");
        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$50.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$50.00"), "Unconnected filter make impact to insight");
    }

    protected Metrics getMetricCreator() {
        return new Metrics(domainRestClient, projectMappingPID);
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private void setUpProcess() {
        log.info("Setup Process...............");
        try {
            dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(projectTest, PROCESS_NAME,
                    dataSourceId, "1");
            domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create process" + e.getMessage());
        }
    }

    private void prepareTimestamp() {
        log.info("Prepare Timestamp...............");
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

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        log.info("Clean up...............");
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcess);
        }
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(projectMappingPID);
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        deleteTable(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE, TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
        deleteDataset(DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN_GENERATE);
    }

    private String getCurrentDate() {
        return DateTime.now().toString("YYYY_MM_dd_HH_mm_ss");
    }
}
