package com.gooddata.qa.graphene.datasource;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.project.Project;
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
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

public class RedShiftDataSourceAdvancedE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private final String DATASOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATASOURCE_NAME_UPDATE = "Auto_datasource_update" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String DATASOURCE_DATABASE = "dev";
    private final String DATASOURCE_DATASET = "orders";
    private final String DATASOURCE_DATASET_UPDATE = "shippers";
    private final String DATASOURCE_PREFIX = "pre_";
    private final String DATASOURCE_SCHEMA = "att_database";
    private final String DATASOURCE_SCHEMA_UPDATE = "att_database_update";
    private static final String blankProject = "Blank-Project-For-Publish-Workspace-Test";
    private final String INSIGHT_NAME = "RedShift Insight Test";
    private final String DASHBOARD_NAME = "RedShift Dashboard Test";
    private String oldProjectId;
    private String blankProjectId;
    private String dataSourceId;
    private String dataSource_update_Id;
    private String dataSourceTitle;
    private ContentDatasourceContainer container;
    private ConnectionConfiguration configuration;
    private ConnectionDetail redShiftDetail;
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

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        DATASOURCE_URL = testParams.getRedshiftJdbcUrl();
        DATASOURCE_USERNAME = testParams.getRedshiftUserName();
        DATASOURCE_PASSWORD = testParams.getRedshiftPassword();
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
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        log.info("Info Create DataSource for PreserveData : ");
        log.info("DATASOURCE_NAME : " + DATASOURCE_NAME);
        log.info("DATASOURCE_USERNAME : " + DATASOURCE_USERNAME);
        log.info("DATASOURCE_DATABASE : " + DATASOURCE_DATABASE);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_SCHEMA : " + DATASOURCE_SCHEMA);
        createRedShiftDataSource(DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        Screenshots.takeScreenshot(browser,"testPublishIntWorkspaceWithPreserveData",getClass());
        dataSourceId = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("Current ProjectID : " + testParams.getProjectId());
        log.info("dataSourceId : " + dataSourceId);
        log.info("dataSourceTitle : " + dataSourceTitle);
        redShiftDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.PRESERVE_DATA.toString());
        publishModeDialog.clickPublish();
        redShiftDetail.waitLoadingModelPage();
        PublishResult publishResult = redShiftDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        String sql = getResourceAsString("/sql_redshift_modelview.txt");
        log.info("sql for PreserveData : " + sql);
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, blankProjectId);
        modelView = modelRestRequest.getDatasetModelView(DATASOURCE_DATASET);
        log.info("modelView for PreserveData : " + modelView.toString());
        assertEquals(modelView.toString(), sql);
    }

    @Test(dependsOnMethods = {"testPublishIntWorkspaceWithPreserveData"})
    public void testPublishIntWorkspaceWithOverwrite() throws IOException {
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        log.info("Info Create DataSource for Overwrite : ");
        log.info("DATASOURCE_NAME_UPDATE : " + DATASOURCE_NAME_UPDATE);
        log.info("DATASOURCE_USERNAME : " + DATASOURCE_USERNAME);
        log.info("DATASOURCE_DATABASE : " + DATASOURCE_DATABASE);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_SCHEMA_UPDATE : " + DATASOURCE_SCHEMA_UPDATE);
        createRedShiftDataSource(DATASOURCE_NAME_UPDATE, DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA_UPDATE);
        Screenshots.takeScreenshot(browser,"testPublishIntWorkspaceWithOverwrite",getClass());
        dataSource_update_Id = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("Current ProjectID : " + testParams.getProjectId());
        log.info("dataSource_update_Id : " + dataSource_update_Id);
        log.info("dataSourceTitle : " + dataSourceTitle);
        redShiftDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.OVERWRITE.toString());
        assertEquals(publishModeDialog.getWarningMessage(), "Overwrite might break your saved insights.");
        publishModeDialog.clickPublish();
        redShiftDetail.waitLoadingModelPage();
        publishResult = redShiftDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        sql = getResourceAsString("/sql_redshift_modelview_update.txt");
        log.info("modelView for Overwrite: " + sql);
        modelView = new ModelRestRequest(restClient, blankProjectId).getDatasetModelView(DATASOURCE_DATASET_UPDATE);
        log.info("modelView for Overwrite: " + modelView.toString());
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

    @Test(dependsOnMethods = "testDeleteDatasourceExistingOnProcess")
    public void showDataIntegrationConsole() {
        initDatasourceManagementPage();
        dataSourceManagementPage.getContentWrapper().getContentDatasourceContainer().getConnectionDetail()
                .openDataIntegrationConsole();
        assertThat(browser.getCurrentUrl(), containsString("disc"));
    }

    private void createRedShiftDataSource(String databaseName, String url, String username, String password,
                                          String database, String prefix, String schema) {
        container.addConnectionTitle(databaseName);
        configuration.addRedshiftBasicInfo(url, username, password, database, prefix, schema);
        container.clickSavebutton();
    }

    private void OpenPublishIntoWorkSpace(String mode) {
        PublishWorkspaceDialog publishWorkspaceDialog = redShiftDetail.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickSelect();
        publishModeDialog = redShiftDetail.getPublishModeDialog();
        publishModeDialog.selectMode(mode);
    }

    private void setUpProcess() {
        // Create New Process Schedule
        log.info("SetUp Process...............");
        log.info("Current ProjectID : " + testParams.getProjectId());
        log.info("blankProjectId : " + blankProjectId);
        project = restClient.getProjectService().getProjectById(blankProjectId);
        log.info("SetUp Dataload Process...............");
        log.info("getUser :" + testParams.getUser());
        log.info("UserADomain :" + testParams.getUserDomain());
        log.info("DomainAUser :" + testParams.getDomainUser());
        log.info("SetUp Dataload Process...............");
        dataloadProcess = new ScheduleUtils(restClient).createDataDistributionProcess(project, PROCESS_NAME,
                dataSource_update_Id, "1");
        log.info("PROCESS_NAME :" + PROCESS_NAME);
        log.info("dataSource_update_Id : " + dataSource_update_Id);
        log.info("dataloadProcess : " + dataloadProcess);
        log.info("project.getDataLoadUri() :" + project.getDataLoadUri());
        processUtils = new ProcessUtils(restClient, dataloadProcess);
        log.info("processUtils : " + processUtils);
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(DATASOURCE_DATASET_UPDATE);
        log.info("jsonDataset : " + jsonDataset);
        String valueParam = processUtils.getDataset(jsonDataset);
        log.info("valueParam : " + valueParam);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail;
        log.info("parameters : " + parameters.getParameters().toString());
        log.info("Execute Process...............");
        try {
            detail = processUtils.execute(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Cannot execute process" + e.getMessage());
        }
        log.info("detail : " + detail);
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), blankProjectId);
        log.info("executionLog : " + executionLog);
        log.info("project.getSchedulesUri() :" + project.getSchedulesUri());
        log.info("project.getExecuteUri() :" + project.getExecuteUri());
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
        log.info("Current ProjectID : " + testParams.getProjectId());
        oldProjectId = testParams.getProjectId();
        log.info("oldProjectId : " + oldProjectId);
        testParams.setProjectId(blankProjectId);
        log.info("Current ProjectID : " + testParams.getProjectId());
        getMetricCreator().createSumAmountMetric();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AMOUNT));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().waitForWidgetsLoading();
        indigoDashboardsPage.addDashboard().addInsight(INSIGHT_NAME).selectDateFilterByName("All time").waitForWidgetsLoading()
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
        List<String> listValue = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        assertEquals(listValue, singletonList("$2,000.00"), "Unconnected filter make impact to insight");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException {
        log.info("Clean up...............");
        log.info("Current ProjectID : " + testParams.getProjectId());
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        dataSourceRestRequest.deleteDataSource(dataSource_update_Id);
        log.info("dataSource_update_Id : " + dataSource_update_Id);
        new DataSourceRestRequest(restClient, oldProjectId).deleteDataSource(dataSourceId);
        log.info("dataSourceId : " + dataSourceId);
    }
}
