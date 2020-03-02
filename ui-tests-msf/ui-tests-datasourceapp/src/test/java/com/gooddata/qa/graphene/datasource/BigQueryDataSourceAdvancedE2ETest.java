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
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

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
    private final String DATASOURCE_TABLE = "orders";
    private final String DATASOURCE_TABLE_UPDATE = "shippers";
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
        dataSourceRestRequest = new DataSourceRestRequest(restClient, blankProjectId);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), blankProjectId);
        browser.manage().window().maximize();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPublishIntWorkspaceWithPreserveData() throws IOException {
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        createBigQueryDataSource(DATASOURCE_NAME, DATASOURCE_CLIENTEMAIL, privateKeyString,
                DATASOURCE_PROJECT, DATASOURCE_PREFIX, DATASOURCE_DATASET);
        dataSourceId = container.getDataSourceId();
        bigQueryDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.PRESERVE_DATA.toString());
        publishModeDialog.clickPublish();
        bigQueryDetail.waitLoadingModelPage();
        PublishResult publishResult = bigQueryDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        String sql = getResourceAsString("/sql_bigquery_modelview.txt");
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, blankProjectId);
        modelView = modelRestRequest.getDatasetModelView(DATASOURCE_TABLE);
        assertEquals(modelView.toString(), sql);
    }

    @Test(dependsOnMethods = {"testPublishIntWorkspaceWithPreserveData"})
    public void testPublishIntWorkspaceWithOverwrite() throws IOException {
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getConnectionConfiguration();
        createBigQueryDataSource(DATASOURCE_NAME_UPDATE, DATASOURCE_CLIENTEMAIL, privateKeyString,
                DATASOURCE_PROJECT, DATASOURCE_PREFIX, DATASOURCE_DATASET_UPDATE);
        dataSource_update_Id = container.getDataSourceId();
        dataSourceTitle = container.getDataSourceName();
        bigQueryDetail = container.getConnectionDetail();
        OpenPublishIntoWorkSpace(PublishModeDialog.PublishMode.OVERWRITE.toString());
        assertEquals(publishModeDialog.getWarningMessage(), "Overwrite might break your saved insights.");
        publishModeDialog.clickPublish();
        bigQueryDetail.waitLoadingModelPage();
        publishResult = bigQueryDetail.getPublishResultDialog();
        assertEquals(publishResult.getResultMessage(), "Model successfully published");
        publishResult.closeResultDialog();
        sql = getResourceAsString("/sql_bigquery_modelview_update.txt");
        modelView = new ModelRestRequest(new RestClient(getProfile(ADMIN)), blankProjectId).getDatasetModelView(DATASOURCE_TABLE_UPDATE);
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

    private void createBigQueryDataSource(String databaseName, String username, String password,
                                          String database, String prefix, String dataset) {
        container.addConnectionTitle(databaseName);
        configuration.addBigqueryInfo(username, password, database, dataset, prefix);
        container.clickSavebutton();
    }

    private void OpenPublishIntoWorkSpace(String mode) {
        PublishWorkspaceDialog publishWorkspaceDialog = bigQueryDetail.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickCancel();
        bigQueryDetail.openPublishIntoWorkSpaceDialog();
        publishWorkspaceDialog.searchWorkspace(blankProjectId).selectedWorkspaceOnSearchList(blankProjectId).clickSelect();
        publishModeDialog = bigQueryDetail.getPublishModeDialog();
        publishModeDialog.selectMode(mode);
    }

    private void setUpProcess() {
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
        projectDetailPage = initDiscProjectDetailPage();
        if (projectDetailPage.hasProcess(processName)) {
            removeProcess(processName);
        }
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
        }
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_UPDATE)) {
            deleteDatasource(DATASOURCE_NAME_UPDATE);
        }
    }

    private void deployProcess() {
        testParams.setProjectId(blankProjectId);
        processName = generateProcessName();
        projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
        deploySDDProcessDialog.selectDataSource(dataSourceTitle);
        deploySDDProcessDialog.selectScope(DeploySDDProcessDialog.Scope.CURRENT_PROJECT);
        deployForm.enterProcessName(processName);
        deployForm.clickSubmitButton();
    }

    private void setUpSchedule() {
        String schedule = "Schedule-" + generateHashString();
        CreateScheduleForm createScheduleForm = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(processName)
                .enterScheduleName(schedule);
        createScheduleForm.addParameter("GDC_DATALOAD_DATASETS", "[{\"uploadMode\":\"DEFAULT\",\"dataset\":\"dataset.shippers\"}]");
        createScheduleForm.addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        createScheduleForm.schedule();
        ScheduleDetail.getInstance(browser).executeSchedule();
    }

    private void removeProcess(String name) {
        projectDetailPage.deleteProcess(name);
    }

    private void deleteDatasource(String datasourceName) {
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
