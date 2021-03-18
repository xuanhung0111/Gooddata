package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class S3DataSourceProcessesTest extends AbstractDatasourceManagementTest {

    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    public DataloadScheduleDetail scheduleDetail;
    private DataSourceMenu dsMenu;
    private ContentDatasourceContainer container;
    private S3Configuration configuration;
    private FileUploadDialog uploadDialog;
    private PreviewCSVDialog dialog;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Modeler modeler;
    private ChartReport chartReport;
    private AnalysisPage analysisPage;
    public DatasourceHeading heading;
    private LogicalDataModelPage ldmPage;
    private static String URL_DATASOURCE;
    private static String defaultS3AccessKey;
    private static String defaultS3SecretKey;
    private final String DEPARTMENTS_DATASET = "departments.csv";
    private final String DATASOURCE_NAME = "S3_datasource_" + generateHashString();
    private final String DATASOURCE_ALIAS = "S3_alias_" + generateHashString();
    private final String BUCKET_VALUE = "msf-dev-grest/_QA_ATT";
    private final String VALIDATE_MSG = "Connection succeeded";
    private final String FULL_LOAD_PATH = "fullLoad";
    private final String INC_LOAD_PATH = "incrementalLoad";
    private final String GZIP_FILE_PATH = "gZIP";
    private final String ZIP_FILE_PATH = "zipFile";
    private final String S3_FULL_LOAD_PROCESS = "s3_full_process_" + generateHashString();
    private final String S3_LOAD_CSV_PROCESS = "s3_csv_process_" + generateHashString();
    private final String S3_LOAD_GZIP_PROCESS = "s3_gzip_process_" + generateHashString();
    private final String S3_LOAD_ZIP_PROCESS = "s3_zip_process_" + generateHashString();
    private final String CLIENT_IDENTIFIER = "Client01";
    private final String METRIC = "Departmentnumber";
    private final String ATTRIBUTE = "Departmentname";

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        defaultS3AccessKey = testParams.loadProperty("s3.accesskey");
        defaultS3SecretKey = testParams.loadProperty("s3.secretkey");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        initDatasourceManagementPage();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        createS3Datasource();
        createDataModel();
        uploadCsvFile();
    }

    @DataProvider(name = "s3TypeProcess")
    public Object[][] runS3Process() {
        return new Object[][] {
                { S3_LOAD_CSV_PROCESS, INC_LOAD_PATH, asList("Name02", "Name03")},
                { S3_LOAD_ZIP_PROCESS, ZIP_FILE_PATH, asList("Name02", "Name03", "Name05", "Name06")},
                { S3_LOAD_GZIP_PROCESS, GZIP_FILE_PATH, asList("Name02", "Name03", "Name05", "Name06", "Name08", "Name09")},
                { S3_FULL_LOAD_PROCESS, FULL_LOAD_PATH, asList("Name10")}
        };
    }

    @Test(dependsOnMethods = {"initialStageTest"}, dataProvider = "s3TypeProcess")
    public void executeS3IncrementalLoadTest(String processName, String path, List<String> axisLabelsList) {
        initDiscPageIgnoreAlert();
        sleepTightInSeconds(2);
        projectDetailPage.clickDeployButton();
        createDeployProcess(processName, DATASOURCE_NAME, path);
        executeSchedule(processName);
        createReportToCheckDataLoaded();
        assertEquals(chartReport.getAxisLabels(), axisLabelsList);
        analysisPage.clear();
    }

    @AfterClass(alwaysRun = true)
    public void deleteProcessAndDatasource() {
        ProjectDetailPage projectDetailPage = initDiscPageIgnoreAlert();
        Collection<String> listProcess = projectDetailPage.getProcessNames();
        if (listProcess.size() > 0) {
            listProcess.forEach(process -> projectDetailPage.deleteProcess(process));
        }
        initDatasourceManagementPage();
        waitForLoadingDatasourceManagementApp();
        dsMenu.selectDataSource(DATASOURCE_NAME);
        heading = container.getDatasourceHeading();
        heading.clickMoreButton();
        MoreContentDialog dialogOption = MoreContentDialog.getInstance(browser);
        dialogOption.clickDeleteButton().clickDelete();
    }

    private void createDeployProcess(String processName, String datasourceName, String sourcePath) {
        DeployProcessForm deployProcess = DeployProcessForm.getInstance(browser);
        deployProcess.selectADDProcess().selectDataSourceType(datasourceName).inputDatasourcePath(sourcePath)
                .inputClientIdentifier(CLIENT_IDENTIFIER);
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
        log.info("Datasource name is: " + DATASOURCE_NAME);
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addAliasTitle(DATASOURCE_ALIAS);
        configuration.addBucket(BUCKET_VALUE);
        configuration.addAccessKey(defaultS3AccessKey);
        configuration.addSecretKey(defaultS3SecretKey);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), VALIDATE_MSG);

        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        URL_DATASOURCE = browser.getCurrentUrl();
        log.info("URL for created datasource is: " + URL_DATASOURCE);
    }

    public void createReportToCheckDataLoaded() {
        analysisPage = initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addAttribute(ATTRIBUTE)
                .addMetric(METRIC, FieldType.FACT).waitForReportComputing();
        chartReport =  analysisPage.getChartReport();
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
        publishModelDialog.uncheckUploadDataCsvFile().publishSwitchToEditMode();
    }
}
