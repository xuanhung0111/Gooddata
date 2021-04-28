package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.cloudresources.DataMappingUtils;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.lcm.LcmRestUtils;
import com.gooddata.sdk.model.project.Project;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.deleteSegment;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class S3DataSourceSegmentTest extends AbstractLDMPageTest {

    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_ALIAS = "S3_alias_" + generateHashString();
    private final String BUCKET_VALUE = "msf-dev-grest/_QA_ATT";
    private final String DATASET_NAME = "departments";
    private final String X_CLIENT_ID = "Client01";
    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String CLIENT_ID_3 = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_3 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String FULL_LOAD_PATH = "fullLoad";
    private final String DELETED_ROW_PATH = "fullLoad_Deleted";
    private final String INC_LOAD_PATH = "incrementalLoad";
    private final String GZIP_FILE_PATH = "gZIP";
    private final String ZIP_FILE_PATH = "zipFile";
    private final String S3_FULL_LOAD_PROCESS = "s3_full_process_" + generateHashString();
    private final String S3_LOAD_CSV_PROCESS = "s3_csv_process_" + generateHashString();
    private final String S3_LOAD_GZIP_PROCESS = "s3_gzip_process_" + generateHashString();
    private final String S3_LOAD_ZIP_PROCESS = "s3_zip_process_" + generateHashString();
    private final String S3_X_DELETE_PROCESS = "s3_x_delete_process_" + generateHashString();

    private final String VALIDATE_MSG = "Connection succeeded";
    private final String DEPARTMENT_NUMBER = "Departmentnumber";
    private final String DEPARTMENT_NAME = "Departmentname";
    private final String DEPARTMENT_ID = "Departmentid";
    private final String DEPARTMENTS_DATASET = "departments.csv";
    private static String defaultS3AccessKey;
    private static String defaultS3SecretKey;

    private String devProjectId;
    private String serviceProjectId;
    private String clientProjectId1;
    private String clientProjectId2;
    private String clientProjectId3;
    private Project serviceProject;
    private Project project1;
    private Project project2;
    private Project project3;
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private ToolBar toolbar;
    private ChartReport chartReport;
    private Canvas canvas;
    private Sidebar sidebar;
    private AnalysisPage analysisPage;
    private PreviewCSVDialog dialog;
    private ContentDatasourceContainer container;
    private S3Configuration configuration;
    private FileUploadDialog uploadDialog;
    private String projectId;
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private RestClient domainRestClient;
    public DataloadScheduleDetail scheduleDetail;
    private MainModelContent mainModelContent;
    private DataMappingUtils dataMappingProjectIdUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private String dataSourceId;
    private String masterProjectId;
    private boolean useK8sExecutor = true;

    @BeforeClass(alwaysRun = true)
    public void disableDynamicUser() {
        useDynamicUser = false;
    }

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        defaultS3AccessKey = testParams.loadProperty("s3.accesskey");
        defaultS3SecretKey = testParams.loadProperty("s3.secretkey");
        domainRestClient = new RestClient(getProfile(DOMAIN));
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() throws IOException {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        if (testParams.isTestingEnvironment()) {
            dataSourceManagementPage = initDatasourceManagementPage();
            contentWrapper = dataSourceManagementPage.getContentWrapper();
            dsMenu = dataSourceManagementPage.getMenuBar();
            container = contentWrapper.getContentDatasourceContainer();
            createS3Datasource();
            createDataModel();
            uploadCsvFile();
            createLCM();
            setUpDataMapping();
            createCustomMappingForLcmProject(masterProjectId);
            createCustomMappingForLcmProject(clientProjectId1);
            createCustomMappingForLcmProject(clientProjectId2);
            createCustomMappingForLcmProject(clientProjectId3);
        } else {
            log.warning("DSS token isn't configured on CI-Infa for client-demo");
            throw new SkipException("Skip test LCM on Client demo !!");
        }
    }

    @DataProvider(name = "s3SegmentTypeProcess")
    public Object[][] runS3SegmentProcess() {
        return new Object[][] {
                { S3_LOAD_CSV_PROCESS, INC_LOAD_PATH, asList("Name02", "Name03")},
                { S3_LOAD_ZIP_PROCESS, ZIP_FILE_PATH, asList("Name02", "Name03", "Name05", "Name06")},
                { S3_LOAD_GZIP_PROCESS, GZIP_FILE_PATH, asList("Name02", "Name03", "Name05", "Name06", "Name08", "Name09")},
                { S3_FULL_LOAD_PROCESS, FULL_LOAD_PATH, asList("Name10")}
        };
    }

    @Test(dependsOnMethods = {"initialStageTest"}, dataProvider = "s3SegmentTypeProcess")
    public void executeS3SegmentLoadTest(String processName, String path, List<String> axisLabelsList) {
        initDiscPageIgnoreAlert();
        sleepTightInSeconds(2);
        projectDetailPage.clickDeployButton();
        createDeployProcess(processName, DATASOURCE_NAME, path);
        executeSchedule(processName);

        // Check data on Master project and Client 3 (has different client_id) -> Will have no data
        createReportToCheckDataLoaded(masterProjectId);
        assertTrue(analysisPage.isEmptyInsightResult());
        analysisPage.clear();

        createReportToCheckDataLoaded(clientProjectId3);
        assertTrue(analysisPage.isEmptyInsightResult());
        analysisPage.clear();

        // Check data on Client project -> Will update data as expected
        createReportToCheckDataLoaded(clientProjectId1);
        chartReport =  analysisPage.getChartReport();
        assertEquals(chartReport.getAxisLabels(), axisLabelsList);
        analysisPage.clear();

        createReportToCheckDataLoaded(clientProjectId2);
        chartReport =  analysisPage.getChartReport();
        assertEquals(chartReport.getAxisLabels(), axisLabelsList);
        analysisPage.clear();
    }

    @Test(dependsOnMethods = "executeS3SegmentLoadTest" )
    public void loadDistributeDeletedRow() {
        createDeleteRowMapping(masterProjectId);
        createDeleteRowMapping(clientProjectId1);
        createDeleteRowMapping(clientProjectId2);
        createDeleteRowMapping(clientProjectId3);

        initDiscPageIgnoreAlert();
        sleepTightInSeconds(2);
        projectDetailPage.clickDeployButton();
        createDeployProcess(S3_X_DELETE_PROCESS, DATASOURCE_NAME, DELETED_ROW_PATH);
        executeSchedule(S3_X_DELETE_PROCESS);

        // Check data on Master project -> Will have no data
        createReportToCheckDataLoaded(masterProjectId);
        assertTrue(analysisPage.isEmptyInsightResult());
        analysisPage.clear();

        createReportToCheckDataLoaded(clientProjectId1);
        chartReport =  analysisPage.getChartReport();
        assertEquals(chartReport.getAxisLabels(), asList("Name11", "Name13"));
        analysisPage.clear();
    }

    @Test(dependsOnMethods = "loadDistributeDeletedRow" )
    public void checkWarningIcon() {
        initLogicalDataModelPageByPID(clientProjectId2);
        modeler.getLayout().waitForLoading();
        mainModelContent.focusOnDataset(DATASET_NAME);
        Model departmentModel = mainModelContent.getModel(DATASET_NAME.toLowerCase());
        departmentModel.deleteAttributeOnDataset(DEPARTMENT_NAME.toLowerCase());
        toolbar.clickPublish();
        PublishModelDialog.getInstance(browser).publishModel();

        initDiscPageIgnoreAlert();
        executeSchedule(S3_X_DELETE_PROCESS);

        assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                ScheduleStatus.WARNING.toString());
    }

    private void createLCM() throws IOException {
        log.info("Segment ID of all projects is: " + SEGMENT_ID);
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        serviceProjectId = lcmBrickFlowBuilder.getLCMServiceProject().getServiceProjectId();
        serviceProject = domainRestClient.getProjectService().getProjectById(serviceProjectId);

        devProjectId = testParams.getProjectId();
        log.info("dev project : " + devProjectId);

        clientProjectId1 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_1);
        project1 = domainRestClient.getProjectService().getProjectById(clientProjectId1);
        log.info("client 1 : " + clientProjectId1);

        clientProjectId2 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_2);
        project2 = domainRestClient.getProjectService().getProjectById(clientProjectId2);
        log.info("client 2 : " + clientProjectId2);

        clientProjectId3 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_3);
        project3 = domainRestClient.getProjectService().getProjectById(clientProjectId3);
        log.info("client 3 : " + clientProjectId3);

        lcmBrickFlowBuilder.setDevelopProject(devProjectId).setSegmentId(SEGMENT_ID).setClient(CLIENT_ID_1, clientProjectId1)
                .setClient(CLIENT_ID_2, clientProjectId2).setClient(CLIENT_ID_3, clientProjectId3).buildLcmProjectParameters();
        lcmBrickFlowBuilder.runLcmFlow();
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId1);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId2);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId3);

        masterProjectId = LcmRestUtils.getMasterProjectId(domainRestClient,"default" , SEGMENT_ID);
        log.info("master project id : " + masterProjectId);
    }

    private void createDeployProcess(String processName, String datasourceName, String sourcePath) {
        DeployProcessForm deployProcess = DeployProcessForm.getInstance(browser);
        deployProcess.selectADDProcess().selectDataSourceType(datasourceName).inputDatasourcePath(sourcePath)
                .selectScope(DeploySDDProcessDialog.Scope.SEGMENT).selectSegment(SEGMENT_ID);
        deployProcess.enterProcessName(processName).submit();
    }

    private void executeSchedule(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName).selectAllDatasetsOption().schedule();
        scheduleDetail = DataloadScheduleDetail.getInstance(browser);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
    }

    public void createS3Datasource() {
        dsMenu.selectS3DataSource();
        configuration = container.getS3ConnectionConfiguration();
        log.info("-----Datasource name is: " + DATASOURCE_NAME);
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addAliasTitle(DATASOURCE_ALIAS);
        configuration.addBucket(BUCKET_VALUE);
        configuration.addAccessKey(defaultS3AccessKey);
        configuration.addSecretKey(defaultS3SecretKey);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), VALIDATE_MSG);

        container.clickSavebutton();
        dataSourceId = getDatasourceId();
        log.info("Datasource ID is : "  + dataSourceId);
        contentWrapper.waitLoadingManagePage();
    }

    public void createReportToCheckDataLoaded(String projectId) {
        analysisPage = initAnalyseDetailPage(projectId).changeReportType(ReportType.COLUMN_CHART).addAttribute(DEPARTMENT_NAME)
                .addMetric(DEPARTMENT_NUMBER, FieldType.FACT).waitForReportComputing();
    }

    public void createDataModel() {
        openUrl(LogicalDataModelPage.getUri(testParams.getProjectId()));
        ldmPage = LogicalDataModelPage.getInstance(browser);
        modeler = ldmPage.getDataContent().getModeler();
        modeler.getLayout().waitForLoading();
        toolbar = modeler.getToolbar();
        sidebar = modeler.getSidebar();
        if (Layout.getInstance(browser).isInitialPagePresent()) {
            ViewMode.getInstance(browser).clickButtonChangeToEditMode();
        }
        if (!isElementPresent(By.className("gdc-ldm-sidebar"), browser)) {
            ToolBar.getInstance(browser).clickEditBtn();
        }
    }

    public void uploadCsvFile() {
        final CsvFile csv = CsvFile.loadFile(getFilePathFromResource(
                "/" + ResourceDirectory.UPLOAD_CSV + "/" + DEPARTMENTS_DATASET));
        uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        dialog = PreviewCSVDialog.getInstance(browser);
        dialog.clickImportButton();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.uncheckUploadDataCsvFile().publishModel();
    }

    private void addUserToSpecificProject(String email, UserRoles userRole, String projectId) throws IOException {
        final String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(new RestClient(
                new RestClient.RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true)),
                projectId);
        userManagementRestRequest.addUserToProject(email, userRole);
    }

    private void deleteProcessesAndDatasource() {
        log.info("Deleting Processes...............!!!");
        ProjectDetailPage projectDetailPage = initDiscPageIgnoreAlert();
        Collection<String> listProcess = projectDetailPage.getProcessNames();
        if (listProcess.size() > 0) {
            listProcess.forEach(process -> projectDetailPage.deleteProcess(process));
        }

        log.info("Deleting Datasource...............!!!");
        dataSourceRestRequest.deleteDataSource(dataSourceId);
    }

    private void setUpDataMapping() throws IOException {
        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), asList(), asList(), dataSourceId,
                testParams.getProjectId());
        dataMappingProjectIdUtils.createDataMapping();
        dataMappingProjectIdUtils.updateClientIdDataMapping(Pair.of(CLIENT_ID_1, X_CLIENT_ID));
        dataMappingProjectIdUtils.updateClientIdDataMapping(Pair.of(CLIENT_ID_2, X_CLIENT_ID));
    }

    public String getDatasourceId() {
        log.info("Current browser URL is: " + browser.getCurrentUrl());
        sleepTightInSeconds(2); // need to sleep for loading url
        return browser.getCurrentUrl().split("/datasource/")[1].replace("?navigation=disc", "");
    }

    public void createCustomMappingForLcmProject(String projectId) {
        openUrl(LogicalDataModelPage.getUri(projectId));
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        toolbar.clickEditBtn();
        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        Model departmentModel = mainModelContent.getModel(DATASET_NAME);
        mainModelContent.focusOnDataset(DATASET_NAME);
        departmentModel.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        LdmControlLoad.getInstance(browser).toogleDistributedLoad();
        mappingTab.editSourceColumnByName(DEPARTMENT_NAME, DataMapping.SOURCE_TYPE.LABEL.getName(), DEPARTMENT_NAME, false);
        mappingTab.editSourceColumnByName(DEPARTMENT_ID, DataMapping.SOURCE_TYPE.LABEL.getName(), DEPARTMENT_ID, false);
        mappingTab.editSourceColumnByName(DEPARTMENT_NUMBER, DataMapping.SOURCE_TYPE.FACT.getName(), DEPARTMENT_NUMBER, false);

        dialog.saveChanges();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.publishModel();
    }

    public void createDeleteRowMapping(String projectId) {
        openUrl(LogicalDataModelPage.getUri(projectId));
        modeler.getLayout().waitForLoading();
        toolbar.clickEditBtn();
        modeler.getLayout().waitForLoading();
        Model departmentModel = mainModelContent.getModel(DATASET_NAME);
        mainModelContent.focusOnDataset(DATASET_NAME);
        departmentModel.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        dialog.clickOnDataMappingTab();
        LdmControlLoad.getInstance(browser).toogleDeletedRowsLoad();
        dialog.saveChanges();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.publishModel();
    }

    @AfterClass(alwaysRun = true)
    private void deleteClientAndProject() {
        if (testParams.isTestingEnvironment()) {
            deleteProcessesAndDatasource();
            deleteSegment(new RestClient(getProfile(ADMIN)), testParams.getUserDomain(), SEGMENT_ID);
            deleteProject(serviceProjectId);
        } else {
            log.warning("DSS token isn't configured on CI-Infa for client-demo");
            throw new SkipException("Skip test LCM on Client demo !!");
        }
    }
}
