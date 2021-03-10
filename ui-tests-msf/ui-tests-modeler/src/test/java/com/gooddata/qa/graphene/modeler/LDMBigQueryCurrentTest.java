package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.disc.Parameters;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;

import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.graphene.fragments.modeler.datasource.*;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.StandardSQLTypeName;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.cloudresources.BigQueryUtils.deleteDataset;
import static com.gooddata.qa.utils.cloudresources.BigQueryUtils.deleteTable;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.*;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class LDMBigQueryCurrentTest extends AbstractLDMPageTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private DataSourceContentConnected connected;
    private BigQueryUtils bigqueryUtils;
    private String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private String DATASOURCE_PREFIX = "PRE";
    private String DATASOURCE_CLIENT_EMAIL ;

    private final String ID_ATTRIBUTE = "Id";
    private final String COLOR_ATTRIBUTE = "Color";
    private final String YEAR_DATE = "Year";
    private final String PRICE_FACT = "Price";
    private final String DISTRIBUTED_LOAD = "Distributed Load";
    private final String INCREMENTAL_LOAD = "Incremental Load";
    private final String DELETED_ROWS = "Deleted rows";
    private final String CLIENT_ID = "clientcustom1";
    private final String BIGQUERY_PROJECT = "gdc-us-dev";
    private final String DATASET_MAPPING_PROJECT_ID = DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN + "_" + getCurrentDate();
    private final String TABLE_MAPPING_PROJECT_ID = TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN.replace("CUSTOMERSMAPPINGPROJECTID2", DATASET_MAPPING_PROJECT_ID);
    private final String DATASOURCE_PROJECT = "gdc-us-dev";

    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private DataSourceUtils dataSourceUtils;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private Project project;
    private String dataSourceId;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private ToastMessage toastMessage;
    private DataSourcePanelContent datasourcePanel;
    private DataSourceDropDownBar dropDownBar;
    private DataSourceContent dataSourceContent;
    private DataMappingUtils dataMappingProjectIdUtils;
    private DataSourceRestRequest dataSourceRestRequest;
    private String jsFile;
    private String projectId;
    private ConnectionInfo connectionInfo;
    private String DATASOURCE_PRIVATE_KEY;
    private String privateKeyString;

    //work around issue
    private static final List<String> DROP_DOWN_ALL_FIELDS =  asList("Id", "Color", "Year", "Price");

    private final List<Field> DATATYPES_SCHEMA = Arrays.asList(
            Field.newBuilder(ID_ATTRIBUTE, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLOR_ATTRIBUTE, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(YEAR_DATE, StandardSQLTypeName.DATE).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(PRICE_FACT, StandardSQLTypeName.NUMERIC).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_CLIENT_ID, StandardSQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_TIMESTAMP, StandardSQLTypeName.TIMESTAMP).setMode(Field.Mode.NULLABLE).build(),
            Field.newBuilder(COLUMN_X_DELETED, StandardSQLTypeName.BOOL).setMode(Field.Mode.NULLABLE).build()
    );

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() throws Throwable {
        DATASOURCE_CLIENT_EMAIL = testParams.getBigqueryClientEmail();
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        projectId = testParams.getProjectId();
        restClient = new RestClient(getProfile(ADMIN));
        dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        connectionInfo = dataSourceUtils.createBigQueryConnectionInfo(BIGQUERY_PROJECT,
                DatabaseType.BIGQUERY,  DATASET_MAPPING_PROJECT_ID);
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), projectId);
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        privateKeyString = DATASOURCE_PRIVATE_KEY.replace("\n", "\\n");
        setUpDatabase();
        updateData();
    }

    @Test(dependsOnMethods = "initTest")
    public void connectToWorkspaceTest() {
        createNewDatasource();
        ConnectWorkSpaceDialog connectWorkSpaceDialog = DatasourceHeading.getInstance(browser).clickConnectButton();
        connectWorkSpaceDialog.searchWorkspace(projectId);
        connectWorkSpaceDialog.selectedWorkspaceOnSearchList(projectId);
        ldmPage = connectWorkSpaceDialog.clickSelect();
        toastMessage = ToastMessage.getInstance(browser);
        assertEquals(toastMessage.getToastMessage(), "Data Source connected. You can now use it in the model.");

        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();

        datasourcePanel = DataSourcePanelContent.getInstance(browser);
        dropDownBar = datasourcePanel.getDropdownDatasource();
        assertEquals(dropDownBar.getTextButton(), DATASOURCE_NAME);
        dataSourceContent = DataSourceContent.getInstance(browser);
        assertTrue(dataSourceContent.verifyConnectingMessage());
        connected = dataSourceContent.getDatasourceConnected();
        DataSourceSchema schema =  connected.getDatasourceSchema();
        log.info("----Datasource verify----" + DATASOURCE_NAME);
    }

    @Test(dependsOnMethods = "connectToWorkspaceTest")
    public void refeshSchemaTest() throws SQLException, FileNotFoundException {
        dataSourceContent = DataSourceContent.getInstance(browser);
        DataSourceSchema schema =  connected.getDatasourceSchema();
        schema.clickRefeshSchema();
        assertTrue(dataSourceContent.verifyConnectingMessage(), "Verify message is wrong !!");
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        assertTrue(schemaContent.isTableExisting(TABLE_MAPPING_PROJECT_ID), "Table is not present!!");
        assertEquals(schema.getTextSchemaName(), DATASET_MAPPING_PROJECT_ID);
    }

    @Test(dependsOnMethods = "refeshSchemaTest" )
    public void searchAndReviewDataTest() {
        DataSourceSchema schema =  connected.getDatasourceSchema();
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        jsFile = getResourceAsString("/dragdrop.js");

        //Add table PRE_CAR and verify preview Dialog
        schemaContent.dragdropTableToCanvas(TABLE_MAPPING_PROJECT_ID, jsFile);
        PreviewCSVDialog dialog = PreviewCSVDialog.getInstance(browser);
        GenericList dropdownRecommendCar = GenericList.getInstance(browser);
        dropdownRecommendCar.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        assertEquals(dialog.getListHeaders(), asList("Id", "Color", "Year", "Price"));
        assertEquals(dialog.getEditDatasetZone().getListColumns(), DROP_DOWN_ALL_FIELDS);
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(ID_ATTRIBUTE), "Primary key");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(COLOR_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(YEAR_DATE), "Date");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PRICE_FACT), "Measure");
        dialog.clickImportButton();

        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        assertTrue(isElementVisible(mainModelContent.getModel(DATASET_MAPPING_PROJECT_ID).getRoot()));
    }

    @Test(dependsOnMethods = "searchAndReviewDataTest" )
    public void editDatasetDetail() {
        //delete Attribute, delete Date
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        //Verify Detail Dataset :  Mapped to, Mapping Fields
        mainModelContent.focusOnDataset(DATASET_MAPPING_PROJECT_ID);
        Model model = mainModelContent.getModel(DATASET_MAPPING_PROJECT_ID);
        model.openEditDialog(2);
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        dialog.clickOnDataMappingTab();
        MappedTo mappedToCar = MappedTo.getInstance(browser);
        assertEquals(mappedToCar.getSourceName(), TABLE_MAPPING_PROJECT_ID);
        assertEquals(mappingTab.getSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "Id");
        assertEquals(mappingTab.getSourceColumnByName(COLOR_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "Color");
        assertEquals(mappingTab.getSourceColumnByName(PRICE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "Price");
        assertEquals(mappingTab.getSourceColumnByName(YEAR_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "Year");
        assertEquals(mappingTab.getSourceColumnByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "X__CLIENT_ID");
        assertEquals(mappingTab.getSourceColumnByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "X__TIMESTAMP");
        assertEquals(mappingTab.getSourceColumnByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "X__DELETED");

        // Cancel Dialog on previous test
        dialog.clickCancel();

        //publish model
        toolbar = modeler.getToolbar();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.publishModel();
        sleepTightInSeconds(200);
    }

    @Test(dependsOnMethods = "editDatasetDetail" )
    public void runADDAndVerifyKPITest() throws IOException, SQLException {
        setUpDataMapping();
        setUpProcess();
    }

    private void createNewDatasource() {
        initDatasourceManagementPage();
        // create main datasource
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addBigqueryInfo(DATASOURCE_CLIENT_EMAIL, privateKeyString, DATASOURCE_PROJECT,
                DATASET_MAPPING_PROJECT_ID, DATASOURCE_PREFIX);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail bigqueryDetail = container.getConnectionDetail();
        dataSourceId = container.getDataSourceId();
        log.info("---ID Datasource Main:" + dataSourceId);
        checkBigqueryDetail(container.getDatasourceHeading().getName(), bigqueryDetail.getTextClientEmail(),
                bigqueryDetail.getTextProject(),  bigqueryDetail.getTextDataset(), bigqueryDetail.getTextPrefix());
    }

    private void setUpDatabase() {
        log.info("Setup Database...............");
        // setUp Model projects
        bigqueryUtils = new BigQueryUtils(connectionInfo);
        log.info("Create Dataset  : " + bigqueryUtils.createDataset(DATASET_MAPPING_PROJECT_ID).getDatasetId());
        // Create Table in BigQuery DataBase
        BigQueryUtils.createTable(DATASET_MAPPING_PROJECT_ID, TABLE_MAPPING_PROJECT_ID, DATATYPES_SCHEMA);

    }

    private void updateData() throws IOException, SQLException {
        // create csv file path to get get data from csv file
        Path csvPath = Paths.get(getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/carbigquery.csv"));
        log.info("Load data to table in BigQuery .................... ");
        BigQueryUtils.writeFileToTable(DATASET_MAPPING_PROJECT_ID, TABLE_MAPPING_PROJECT_ID, DATATYPES_SCHEMA, csvPath);
    }

    private void setUpProcess() {
        // Create New Process Schedule
        log.info("Setup Process...............");
        project = restClient.getProjectService().getProjectById(projectId);
        dataloadProcess = new ScheduleUtils(restClient).createDataDistributionProcess(project, "PROCESS_NAME",
                dataSourceId, "1");
        log.info("dataloadProcess : " + dataloadProcess);
        processUtils = new ProcessUtils(restClient, dataloadProcess);
        log.info("processUtils : " + processUtils);
        JSONObject jsonDataset = processUtils.setModeDefaultDataset(DATASET_MAPPING_PROJECT_ID);
        log.info("jsonDataset : " + jsonDataset);
        List<JSONObject> listJson = new ArrayList<JSONObject>();
        listJson.add(jsonDataset);
        String valueParam = processUtils.getDatasets(listJson);
        log.info("valueParam : " + valueParam);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail;
        log.info("Execute Process...............");
        try {
            detail = processUtils.execute(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Cannot execute process" + e.getMessage());
        }
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        log.info("executionLog : " + executionLog);
        assertThat(executionLog, containsString(String.format("Project=\"%s\"", projectId)));
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}",
                        testParams.getProjectId(), CLIENT_ID, DATASET_MAPPING_PROJECT_ID)));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        log.info("Clean up...............");
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(testParams.getProjectId());
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        deleteTable(DATASET_MAPPING_PROJECT_ID, TABLE_MAPPING_PROJECT_ID);
        deleteDataset(DATASET_MAPPING_PROJECT_ID);
    }

    private void setUpDataMapping() {
        List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
        listProjectIdMapping.add(Pair.of(projectId, CLIENT_ID));

        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, asList(), dataSourceId,
                projectId);
        dataMappingProjectIdUtils.createDataMapping();
    }

    private String getCurrentDate() {
        return DateTime.now().toString("YYYY_MM_dd_HH_mm_ss");
    }

    private void checkBigqueryDetail(String name, String clientEmail, String project, String dataset, String prefix) {
        assertTrue(name.contains(DATASOURCE_NAME), "Datasource name is not correct");
        assertEquals(clientEmail, DATASOURCE_CLIENT_EMAIL);
        assertEquals(project, DATASOURCE_PROJECT);
        assertEquals(dataset, DATASET_MAPPING_PROJECT_ID);
        assertEquals(prefix, DATASOURCE_PREFIX);
    }
}
