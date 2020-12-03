package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class BigQueryDataSourceAdvancedE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_CLIENTEMAIL;
    private String DATASOURCE_PRIVATEKEY;
    private final String DATASOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATASOURCE_NAME_UPDATE = "Auto_datasource_update" + generateHashString();
    private final String DATASOURCE_PROJECT = "gdc-us-dev";
    private final String DATASOURCE_DATASET = "att_database";
    private final String DATASOURCE_DATASET_UPDATE = "att_database_update";
    private final String DATASOURCE_TABLE = "Orders";
    private final String DATASOURCE_TABLE_UPDATE = "Shippers";
    private final String DATASOURCE_PREFIX = "PRE_";
    private static final String blankProject = "BigQuery-Blank-Project-Test";
    private final String INSIGHT_NAME = "BigQuery Insight Test";
    private final String DASHBOARD_NAME = "BigQuery Dashboard Test";
    private String oldProjectId;
    private String blankProjectId;
    private String dataSourceId;
    private String dataSource_update_Id;
    private String dataSourceTitle;
    private ContentDatasourceContainer container;
    private ConnectionConfiguration configuration;
    private ConnectionDetail bigQueryDetail;
    private PublishModeDialog publishModeDialog;
    private PublishResult publishResult;
    private JSONObject modelView;
    private String sql;
    private RestClient restClient;
    private DataSourceRestRequest dataSourceRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private String privateKeyString;
    private String processName;
    private ProjectDetailPage projectDetailPage;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        DATASOURCE_CLIENTEMAIL = testParams.getBigqueryClientEmail();
        DATASOURCE_PRIVATEKEY = testParams.getBigqueryPrivateKey();
        privateKeyString = DATASOURCE_PRIVATEKEY.replace("\n", "\\n");
        restClient = new RestClient(getProfile(ADMIN));
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        blankProjectId = createNewEmptyProject(blankProject);
        log.info("blankProjectId: " + blankProjectId);
        dataSourceRestRequest = new DataSourceRestRequest(restClient, blankProjectId);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), blankProjectId);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPublishIntWorkspaceWithPreserveData() throws IOException {
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        log.info("Info Create DataSource for PreserveData : ");
        log.info("DATASOURCE_NAME : " + DATASOURCE_NAME);
        log.info("DATASOURCE_CLIENTEMAIL : " + DATASOURCE_CLIENTEMAIL);
        log.info("DATASOURCE_PROJECT : " + DATASOURCE_PROJECT);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_DATASET_UPDATE : " + DATASOURCE_DATASET);
        createBigQueryDataSource(DATASOURCE_NAME, DATASOURCE_CLIENTEMAIL, privateKeyString,
                DATASOURCE_PROJECT, DATASOURCE_PREFIX, DATASOURCE_DATASET);
        dataSourceId = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("dataSourceId : " + dataSourceId);
        log.info("dataSourceTitle : " + dataSourceTitle);
        bigQueryDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.PRESERVE_DATA.toString());
        publishModeDialog.clickPublish();
        bigQueryDetail.waitLoadingModelPage();
        PublishResult publishResult = bigQueryDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        String sql = getResourceAsString("/sql_bigquery_modelview.txt");
        log.info("sql for PreserveData: " + sql);
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, blankProjectId);
        modelView = modelRestRequest.getDatasetModelView(DATASOURCE_TABLE);
        log.info("modelView for PreserveData: " + modelView.toString());
        assertEquals(modelView.toString(), sql);
    }

    @Test(dependsOnMethods = {"testPublishIntWorkspaceWithPreserveData"})
    public void testPublishIntWorkspaceWithOverwrite() throws IOException {
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        log.info("Info Create DataSource for Overwrite : ");
        log.info("DATASOURCE_NAME_UPDATE : " + DATASOURCE_NAME_UPDATE);
        log.info("DATASOURCE_CLIENTEMAIL : " + DATASOURCE_CLIENTEMAIL);
        log.info("DATASOURCE_PROJECT : " + DATASOURCE_PROJECT);
        log.info("DATASOURCE_PREFIX : " + DATASOURCE_PREFIX);
        log.info("DATASOURCE_DATASET_UPDATE : " + DATASOURCE_DATASET_UPDATE);
        createBigQueryDataSource(DATASOURCE_NAME_UPDATE, DATASOURCE_CLIENTEMAIL, privateKeyString,
                DATASOURCE_PROJECT, DATASOURCE_PREFIX, DATASOURCE_DATASET_UPDATE);
        dataSource_update_Id = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        log.info("dataSource_update_Id : " + dataSource_update_Id);
        log.info("dataSourceTitle : " + dataSourceTitle);
        bigQueryDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.OVERWRITE.toString());
        assertEquals(publishModeDialog.getWarningMessage(), "Overwrite might break your saved insights.");
        publishModeDialog.clickPublish();
        bigQueryDetail.waitLoadingModelPage();
        publishResult = bigQueryDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        sql = getResourceAsString("/sql_bigquery_modelview_update.txt");
        log.info("sql for PreserveData: " + sql);
        modelView = new ModelRestRequest(new RestClient(getProfile(ADMIN)), blankProjectId).getDatasetModelView(DATASOURCE_TABLE_UPDATE);
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

    private void createBigQueryDataSource(String databaseName, String username, String password,
                                          String database, String prefix, String dataset) {
        log.info("Create BigQuery DataSource........... ");
        container.addConnectionTitle(databaseName);
        configuration.addBigqueryInfo(username, password, database, dataset, prefix);
        container.clickSavebutton();
    }

    private void OpenPublishIntoWorkSpace(String mode) {
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        PublishWorkspaceDialog publishWorkspaceDialog = heading.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickCancel();
        heading.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickSelect();
        publishModeDialog = bigQueryDetail.getPublishModeDialog();
        publishModeDialog.selectMode(mode);
    }

    private void setUpProcess() {
        log.info("SetUp Process........... ");
        deployProcess();
        setUpSchedule();
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

        getMetricCreator().createSumAmountMetricAdvance();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AMOUNT));
        log.info("INSIGHT_NAME :" + INSIGHT_NAME);
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(10).waitForWidgetsLoading();
        log.info("Delete cookies..............");
        browser.manage().deleteAllCookies();
        sleepTightInSeconds(3);
        indigoDashboardsPage.addDashboard().addInsight(INSIGHT_NAME).selectDateFilterByName("All time").waitForWidgetsLoading()
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
        log.info("DASHBOARD_NAME :" + DASHBOARD_NAME);
        List<String> listValue = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        log.info("listValue : " + listValue);
        assertEquals(listValue, singletonList("$2,000.00"), "Unconnected filter make impact to insight");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException {
        log.info("Clean up...............");
        projectDetailPage = initDiscProjectDetailPage();
        if (projectDetailPage.hasProcess(processName)) {
            removeProcess(processName);
        }
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            log.info("DATASOURCE_NAME : " + DATASOURCE_NAME);
            deleteDatasource(DATASOURCE_NAME);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME), "Datasource " + DATASOURCE_NAME + " should be deleted");
        }
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_UPDATE)) {
            log.info("DATASOURCE_NAME_UPDATE : " + DATASOURCE_NAME_UPDATE);
            deleteDatasource(DATASOURCE_NAME_UPDATE);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME_UPDATE), "Datasource " + DATASOURCE_NAME_UPDATE + " should be deleted");
        }
    }

    private void deployProcess() {
        log.info("Deploy Process........... ");
        testParams.setProjectId(blankProjectId);
        log.info("blankProjectId : " + blankProjectId);
        processName = generateProcessName();
        log.info("processName : " + processName);
        projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
        deploySDDProcessDialog.selectDataSource(dataSourceTitle);
        deploySDDProcessDialog.selectScope(DeploySDDProcessDialog.Scope.CURRENT_PROJECT);
        deployForm.enterProcessName(processName);
        deployForm.clickSubmitButton();
    }

    private void setUpSchedule() {
        log.info("Set Up Schedule........... ");
        String schedule = "Schedule-" + generateHashString();
        log.info("Schedule Name : " + schedule);
        CreateScheduleForm createScheduleForm = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(processName)
                .enterScheduleName(schedule);
        createScheduleForm.addParameter("GDC_DATALOAD_DATASETS", "[{\"uploadMode\":\"DEFAULT\",\"dataset\":\"dataset.shippers\"}]");
        createScheduleForm.addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        createScheduleForm.schedule();
        ScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish();
    }

    private void removeProcess(String name) {
        projectDetailPage.deleteProcess(name);
    }

    private void deleteDatasource(String datasourceName) {
        log.info("Delete Datasource........... ");
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

