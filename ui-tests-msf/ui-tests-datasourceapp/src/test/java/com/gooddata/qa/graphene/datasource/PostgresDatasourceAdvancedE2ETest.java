package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.ProcessUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class PostgresDatasourceAdvancedE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private String oldProjectId;
    private String blankProjectId;
    private String dataSourceId;
    private String dataSource_update_Id;
    private ContentDatasourceContainer container;
    private ConnectionDetail postgresDetail;
    private PublishModeDialog publishModeDialog;
    private PublishResult publishResult;
    private JSONObject modelView;
    private String sql;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private Project project;
    private RestClient restClient;
    private DataSourceRestRequest dataSourceRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private String dataSourceTitle;
    private final String DATASOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATASOURCE_NAME_UPDATE = "Auto_datasource_update" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String DATASOURCE_DATABASE = "qa";
    private final String DATASOURCE_DATASET = "Orders";
    private final String DATASOURCE_DATASET_UPDATE = "Shippers";
    private final String DATASET_UPDATE = "shippers";
    private final String DATASOURCE_PREFIX = "pre_";
    private final String DATASOURCE_SCHEMA = "att_database";
    private final String DATASOURCE_SCHEMA_UPDATE = "att_database_update";
    private final String INSIGHT_NAME = "Postgre Insight Test";
    private final String DASHBOARD_NAME = "Postgre Dashboard Test";
    private static final String blankProject = "Blank-Project-For-Publish-Workspace-Test";

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        DATASOURCE_URL = testParams.getPostgreJdbcUrl().replace("jdbc:postgresql://", "")
                .replace(":5432/", "");
        DATASOURCE_USERNAME = testParams.getPostgreUserName();
        DATASOURCE_PASSWORD = testParams.getPostgrePassword();
        restClient = new RestClient(getProfile(ADMIN));
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        blankProjectId = createNewEmptyProject(blankProject);
        log.info("blankProjectId : " + blankProjectId);
        dataSourceRestRequest = new DataSourceRestRequest(restClient, blankProjectId);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), blankProjectId);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPublishIntWorkspaceWithPreserveData() throws IOException {
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        log.info("Info Create DataSource for PreserveData : ");
        log.info("DATASOURCE_NAME : " + DATASOURCE_NAME);
        log.info("DATASOURCE_URL : " + DATASOURCE_URL);
        log.info("DATASOURCE_USERNAME : " + DATASOURCE_USERNAME);
        log.info("DATASOURCE_DATABASE : " + DATASOURCE_DATABASE);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_SCHEMA : " + DATASOURCE_SCHEMA);
        createPostgresDataSource(DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        dataSourceId = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("dataSourceId : " + dataSourceId);
        log.info("dataSourceTitle : " + dataSourceTitle);
        postgresDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.PRESERVE_DATA.toString());
        publishModeDialog.clickPublish();
        postgresDetail.waitLoadingModelPage();
        PublishResult publishResult = postgresDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        String sql = getResourceAsString("/sql_postgres_modelview.txt");
        log.info("sql for PreserveData: " + sql);
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, blankProjectId);
        modelView = modelRestRequest.getDatasetModelView(DATASOURCE_DATASET);
        log.info("modelView for PreserveData: " + modelView.toString());
        assertEquals(modelView.toString(), sql);
    }

    @Test(dependsOnMethods = {"testPublishIntWorkspaceWithPreserveData"})
    public void testPublishIntWorkspaceWithOverwrite() throws IOException {
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        log.info("Info Create DataSource for Overwrite : ");
        log.info("DATASOURCE_NAME_UPDATE : " + DATASOURCE_NAME_UPDATE);
        log.info("DATASOURCE_URL : " + DATASOURCE_URL);
        log.info("DATASOURCE_USERNAME : " + DATASOURCE_USERNAME);
        log.info("DATASOURCE_SCHEMA_UPDATE : " + DATASOURCE_SCHEMA_UPDATE);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_DATABASE : " + DATASOURCE_DATABASE);
        createPostgresDataSource(DATASOURCE_NAME_UPDATE, DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA_UPDATE);
        dataSource_update_Id = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("dataSourceId : " + dataSource_update_Id);
        log.info("dataSourceTitle : " + dataSourceTitle);
        postgresDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.OVERWRITE.toString());
        assertEquals(publishModeDialog.getWarningMessage(), "Overwrite might break your saved insights.");
        publishModeDialog.clickPublish();
        postgresDetail.waitLoadingModelPage();
        publishResult = postgresDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        sql = getResourceAsString("/sql_postgres_modelview_update.txt");
        log.info("sql for Overwrite : " + sql);
        modelView = new ModelRestRequest(restClient, blankProjectId).getDatasetModelView(DATASOURCE_DATASET_UPDATE);
        log.info("modelView for Overwrite : " + modelView.toString());
        assertEquals(modelView.toString(), sql);
    }

    @Test(dependsOnMethods = {"testPublishIntWorkspaceWithOverwrite"})
    public void testDeleteDatasourceExistingOnProcess() {
        setUpProcess();
        initDatasourceManagementPage();
        dsMenu.selectDataSource(DATASOURCE_NAME_UPDATE);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDeleteToCheckMessage();
        assertEquals(deleteDialog.getWarningMessage(), "Cannot delete data source, because it is being used by process or processes");
        Screenshots.takeScreenshot(browser, "Delete Datasource " + DATASOURCE_NAME_UPDATE, getClass());
    }

    private void createPostgresDataSource(String databaseName, String url, String username, String password,
                                           String database, String prefix, String schema) {
        container.addConnectionTitle(databaseName);
        container.addPostgreBasicInfo(url, username, password, database, prefix, schema);
        container.clickSavebutton();
    }

    private void OpenPublishIntoWorkSpace(String mode) {
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        PublishWorkspaceDialog publishWorkspaceDialog = heading.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickSelect();
        publishModeDialog = postgresDetail.getPublishModeDialog();
        publishModeDialog.selectMode(mode);
    }

    private void setUpProcess() {
        // Create New Process Schedule
        log.info("Setup Process...............");
        project = restClient.getProjectService().getProjectById(blankProjectId);
        log.info("dataSource_update_Id : " + dataSource_update_Id);
        dataloadProcess = new ScheduleUtils(restClient).createDataDistributionProcess(project, PROCESS_NAME,
                dataSource_update_Id, "1");
        log.info("dataloadProcess : " + dataloadProcess);
        processUtils = new ProcessUtils(restClient, dataloadProcess);
        log.info("processUtils : " + processUtils);
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(DATASET_UPDATE);
        log.info("jsonDataset : " + jsonDataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        log.info("valueParam : " + valueParam);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail;
        log.info("Execute Process...............");
        detail = processUtils.execute(parameters);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), blankProjectId);
        log.info("executionLog : " + executionLog);
        setUpKPIs();
    }

    protected Metrics getMetricCreator() {
        return new Metrics(restClient, blankProjectId);
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private void setUpKPIs() {
        log.info("Setup KPIs...............");
        oldProjectId = testParams.getProjectId();
        testParams.setProjectId(blankProjectId);
        log.info("blankProjectId : " + blankProjectId);
        getMetricCreator().createSumAmountMetricAdvance();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AMOUNT));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(10).waitForWidgetsLoading();
        log.info("Delete cookies..............");
        browser.manage().deleteAllCookies();
        sleepTightInSeconds(3);
        indigoDashboardsPage.addDashboard().addInsight(INSIGHT_NAME).selectDateFilterByName("All time").waitForWidgetsLoading()
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
        List<String> listValue = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        log.info("listValue : " + listValue);
        assertEquals(listValue, singletonList("$2,000.00"), "Unconnected filter make impact to insight");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException {
        log.info("Clean up...............");
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME), "Datasource " + DATASOURCE_NAME + " should be deleted");
        }
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_UPDATE)) {
            deleteDatasource(DATASOURCE_NAME_UPDATE);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME_UPDATE), "Datasource " + DATASOURCE_NAME_UPDATE + " should be deleted");
        }
    }

    private void deleteDatasource(String datasourceName) {
        log.info("Delete Datasource...............");
        dsMenu.selectDataSource(datasourceName);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(datasourceName);
    }
}
