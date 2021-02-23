package com.gooddata.qa.graphene.modeler;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static com.gooddata.qa.utils.cloudresources.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetUser;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_USER;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

public class OutputStageRecommendViewTest extends AbstractLDMPageTest {

    private DataSourceUtils dataSource;
    protected RestClient restClient;
    private SnowflakeUtils snowflakeUtils;
    private IndigoRestRequest indigoRestRequest;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private String time;
    private LocalDateTime lastSuccessful;
    private Project project;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private final String USER_DATASET = "user";
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String INSIGHT_NAME = "Insight Test";
    private final String DATE_DATASET = "joindate";
    private final String DASHBOARD_NAME = "Dashboard Test";
    private String dataSourceId;
    private DataSourceRestRequest dataSourceRestRequest;
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();

    // STEP 1 : User create Table on Cloud Resource
    // STEP 2 : User create Model on Workspace
    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws IOException, SQLException {
        restClient = new RestClient(getProfile(ADMIN));
        project = getAdminRestClient().getProjectService().getProjectById(testParams.getProjectId());
        lastSuccessful = LocalDateTime.now().withNano(0);
        time = parseToTimeStampFormat(lastSuccessful);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());

        // use the same Connection Info for 3 datasources.
        dataSource = new DataSourceUtils(testParams.getUser());
        dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        ConnectionInfo connectionInfo = dataSource.createSnowflakeConnectionInfo(DATABASE_NAME, DatabaseType.SNOWFLAKE);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        log.info("ID Datasource:" + dataSourceId);

        String maql = getResourceAsString("/maql-file/initial_model_outputstageTest.txt");
        setupMaql(maql);

        String sql = getResourceAsString("/sql-file/sql_initial.txt");
        snowflakeUtils.executeSql(sql);
    }

    // STEP 3: User try  to create OutputStage with Mode “Create View” , check that suggest Output Stage correct
    @Test(dependsOnMethods = "initData")
    public void createViewMode() {
        LogicalDataModelPage ldmPage = openViewModeLDMPage();
        Modeler modeler = ldmPage.getDataContent().getModeler();
        toolbar = modeler.getToolbar();
        OutputStage outputStage = toolbar.openOutputStagePopUp();
        outputStage.selectDatasource(DATA_SOURCE_NAME);
        OverlayWrapper.getInstance(browser).selectOption(OverlayWrapper.PROPERTIES_OPTION.CREATE_VIEW.getName());
        String resultOutputStage = outputStage.createOutputStage();
        outputStage.copyOutputStage();
        outputStage.cancelOutputStage();
        String outputStageString = getResourceAsString("/sql-file/outputstage_expected_1.txt");
        String recommendOutputStage = outputStageString.replaceFirst("DATABASE_NEED_TO_CHANGE", DATABASE_NAME.toUpperCase());
        log.info("Recommend String: " + recommendOutputStage);
        assertEquals(resultOutputStage, recommendOutputStage);
    }

    //STEP 4:  Now view doesn’t exist, user run suggest SQL on step 3 update View for that Table on Cloud Resources
    // User run ADDv2 makes sure he can upload data with valid Model publish above and verify on UI
    @Test(dependsOnMethods = "createViewMode")
    public void runADD() throws IOException, SQLException {
        CsvFile csvfile = datasetUser().rows(asList("ID1", "10", "10", time)).rows(asList("ID2", "20", "20", time));
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" +  csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", TABLE_USER,  csvfile.getFilePath());
        String sql = getResourceAsString("/sql-file/sql_update_recommend.txt");
        snowflakeUtils.executeSql(sql);

        setUpProcess();
        Metrics metricCreator = new Metrics(restClient, testParams.getProjectId());
        metricCreator.createSumAgeMetric();
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(METRIC_AGE).dataSet(DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AGE));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME)
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();

        JSONObject jsonDataset = processUtils.setModeDefaultDataset(USER_DATASET);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = processUtils.execute(parameters);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageSpecificProject(testParams.getProjectId()).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AGE).getValue(),
                "$30.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$30.00"), "Unconnected filter make impact to insight");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException, ParseException, JSONException, IOException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
        snowflakeUtils.closeSnowflakeConnection();
    }

    private void setUpDatasource() throws SQLException, IOException {
        // use the same Connection Info for 3 datasources.
        dataSource = new DataSourceUtils(testParams.getUser());
        dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        ConnectionInfo connectionInfo = dataSource.createSnowflakeConnectionInfo(DATABASE_NAME, DatabaseType.SNOWFLAKE);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        log.info("ID Datasource:" + dataSourceId);
    }

    private void setUpProcess() {
        try {
            dataloadProcess = new ScheduleUtils(restClient).createDataDistributionProcess(project, PROCESS_NAME,
                    dataSourceId, "1");
            processUtils = new ProcessUtils(restClient, dataloadProcess);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create process" + e.getMessage());
        }
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }
}
