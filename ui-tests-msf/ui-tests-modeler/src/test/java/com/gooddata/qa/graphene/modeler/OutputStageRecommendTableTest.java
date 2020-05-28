package com.gooddata.qa.graphene.modeler;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.project.Project;
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
import static com.gooddata.qa.utils.cloudresources.DatasetUtils.datasetUserUpdate;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.TABLE_PRE_USER;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

public class OutputStageRecommendTableTest extends AbstractLDMPageTest {

    private DataSourceUtils dataSource;
    protected RestClient restClient;
    private SnowflakeUtils snowflakeUtils;
    private IndigoRestRequest indigoRestRequest;
    private Sidebar sidebar;
    private String time;
    private ToolBar toolbar;
    private LocalDateTime lastSuccessful;
    private Canvas canvas;
    private Project project;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private final String USER_DATASET = "user";
    private final String USERNAME_ATTRIBUTE = "username";
    private final String SCORE_ATTRIBUTE = "score";
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String INSIGHT_NAME = "Insight Test";
    private final String DATE_DATASET = "joindate";
    private final String DASHBOARD_NAME = "Dashboard Test";
    private final String PUBLISH_SUCCESS_MESSAGE = "Model successfully published";
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
    }

    // STEP 3: User try  to create OutputStage with Mode “Create Table” , check that suggest Output Stage correct
    @Test(dependsOnMethods = "initData")
    public void createTableMode(){
        LogicalDataModelPage ldmPage = initLogicalDataModelPage();
        Modeler modeler = initLogicalDataModelPage().getDataContent().getModeler();
        toolbar = modeler.getLayout().waitForLoading().getToolbar();
        sidebar = modeler.getSidebar();
        canvas = modeler.getLayout().getCanvas();
        OutputStage outputStage = toolbar.openOutputStagePopUp();
        outputStage.selectDatasource(DATA_SOURCE_NAME);
        OverlayWrapper.getInstance(browser).selectOption(OverlayWrapper.PROPERTIES_OPTION.CREATE_TABLE.getName());
        String resultOutputStage = outputStage.createOutputStage();
        outputStage.copyOutputStage();
        outputStage.cancelOutputStage();
        String outputStageString = getResourceAsString("/sql-file/outputstage_expected_2.txt");
        String recommendOutputStage = outputStageString.replaceFirst("DATABASE_NEED_TO_CHANGE", DATABASE_NAME.toUpperCase());
        log.info("Recommend String: " + recommendOutputStage);
        assertEquals(resultOutputStage, recommendOutputStage);
    }

    //STEP 4:  User want to change something on Model ,user publish updated model
    // and use mode Alter Table for update Table on Cloud Resources
    @Test(dependsOnMethods = "createTableMode")
    public void updateTableAndInsertData() throws SQLException, IOException {
        setUpDatabase();
        // user edit something on table
        MainModelContent mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        Model modelUser = mainModelContent.getModel(USER_DATASET);
        mainModelContent.focusOnDataset(USER_DATASET);
        mainModelContent.addAttributeToDataset(USERNAME_ATTRIBUTE, USER_DATASET);
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.editDatatypeOfMainLabel(SCORE_ATTRIBUTE, Model.DATA_TYPE.TEXT_128.getClassName());

        //publish with overwrite data mode
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        assertEquals(publishModelDialog.getTextSuccess(), PUBLISH_SUCCESS_MESSAGE);
        publishModelDialog.clickButtonCancelSuccessPopUp();

        //use ALTER_TABLE mode for recommend update table
        OutputStage outputStage = toolbar.openOutputStagePopUp();
        outputStage.selectDatasource(DATA_SOURCE_NAME);
        OverlayWrapper.getInstance(browser).selectOption(OverlayWrapper.PROPERTIES_OPTION.ALTER_TABLE.getName());
        String resultOutputStage = outputStage.createOutputStage();
        outputStage.copyOutputStage();
        outputStage.cancelOutputStage();
        String outputStageString = getResourceAsString("/sql-file/outputstage_expected_3.txt");
        String recommendOutputStage = outputStageString.replaceFirst("DATABASE_NEED_TO_CHANGE", DATABASE_NAME.toUpperCase());
        log.info("Recommend String: " + recommendOutputStage);
        assertEquals(resultOutputStage, recommendOutputStage);
        // user update database and insert data
        updateDatabase();
        CsvFile csvfile = datasetUserUpdate().rows(asList("ID1", "10", time, "Test", "User1"))
                .rows(asList("ID2", "20", time, "Test2", "User2"));
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" +  csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", TABLE_PRE_USER,  csvfile.getFilePath());
    }

    // User run ADDv2 makes sure he can upload data with valid Model publish above and verify on UI
    @Test(dependsOnMethods = "updateTableAndInsertData")
    public void runADD() throws IOException, SQLException {
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

    private void setUpDatabase() throws SQLException{
        String sql = getResourceAsString("/sql-file/sql_initial_2.txt");
        snowflakeUtils.executeSql(sql);
    }

    //because snowflake doesn't support run multisql , run individual sql statements
    private void updateDatabase() throws SQLException{
        snowflakeUtils.executeSql("ALTER TABLE PRE_USER ADD COLUMN A__USERNAME VARCHAR(128);");
        snowflakeUtils.executeSql("ALTER TABLE PRE_USER DROP COLUMN A__SCORE;");
        snowflakeUtils.executeSql("ALTER TABLE PRE_USER ADD COLUMN A__SCORE VARCHAR(128);");
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
