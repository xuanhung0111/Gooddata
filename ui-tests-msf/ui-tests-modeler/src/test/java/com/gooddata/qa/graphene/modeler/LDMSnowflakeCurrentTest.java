package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.datasource.*;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.cloudresources.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_AGE;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.METRIC_PRICE;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.*;
import static org.testng.Assert.assertFalse;

public class LDMSnowflakeCurrentTest extends AbstractLDMPageTest{
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private final String SNOWFLAKE = "Snowflake";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_OTHER = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_WAREHOUSE = "ATT_WAREHOUSE";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "PUBLIC";
    private final String DATASOURCE_DATABASE = "ATT_DATABASE" + generateHashString();
    private final String DATASOURCE_DATABASE_OTHER = "ATT_OTHER" + generateHashString();
    private final String PERSON_TABLE = "PERSON";
    private final String PRE_OTHER_TABLE = "PRE_OTHER";
    private final String PERSON_DATASET = "person";
    private final String CAR_TABLE = "CAR";
    private final String CAR_DATASET = "car";
    private final String TIMESTAMP_DATASET = "timestamp";
    private final String PRE_CAR_TABLE = "PRE_CAR";
    private final String PUBLIC_SCHEMA = "PUBLIC";
    private final String ID_ATTRIBUTE = "Id";
    private final String NAME_ATTRIBUTE = "Name";
    private final String AGE_FACT = "Age";
    private final String CITY_ATTRIBUTE = "City";
    private final String BIRTHDAY_DATE = "Birthday";
    private final String CLIENT_ID_ATTRIBUTE = "Clientid";
    private final String DELETED_ATTRIBUTE = "Deleted";
    private final String COLOR_ATTRIBUTE = "Color";
    private final String YEAR_DATE = "Year";
    private final String PRICE_FACT = "Price";
    private final String PERSON_REFERENCE = "Person";
    private final String DISTRIBUTED_LOAD = "Distributed Load";
    private final String INCREMENTAL_LOAD = "Incremental Load";
    private final String DELETED_ROWS = "Deleted rows";
    private final String INSIGHT_NAME = "AGE CHART";
    private final String INSIGHT_NAME_2 = "PRICE CHART";
    private final String DASHBOARD_NAME = "DASHBOARD TEST";
    private final String CLIENT_ID = "clientcustom1";

    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private DataSourceUtils dataSourceUtils;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private DataSourceRestRequest dataSourceRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private Project project;
    private String dataSourceId;
    private String dataSourceOtherId;
    private SnowflakeUtils snowflakeUtils;
    private SnowflakeUtils snowflakeOther;
    private DataSourceUtils dataSource;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private OverlayWrapper wrapper;
    private DataSourcePanelContent datasourcePanel;
    private DataSourceDropDownBar dropDownBar;
    private DataSourceContent dataSourceContent;
    private DataSourceContentConnected dataSourceContentConnected;
    private DataSourceSchema datasourceSchema;
    private DataMappingUtils dataMappingProjectIdUtils;
    private String jsFile;
    private String projectId;

    private static final List<String> DROP_DOWN_PERSON_ALL_FIELDS = asList("Id", "Name", "Age", "City", "Birthday", "Clientid",
            "Timestamp", "Deleted");
    //work around issue
    private static final List<String> DROP_DOWN_CAR_ALL_FIELDS =  asList("Id", "Color", "Year", "Price", "Person");

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() throws Throwable {
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        projectId = testParams.getProjectId();
        restClient = new RestClient(getProfile(ADMIN));
        ConnectionInfo connectionInfo = dataSourceUtils.createSnowflakeConnectionInfo(DATASOURCE_DATABASE, DatabaseType.SNOWFLAKE);
        ConnectionInfo connectionOther = dataSourceUtils.createSnowflakeUseCustomSchema(DATASOURCE_DATABASE, DatabaseType.SNOWFLAKE
        ,"OTHER");
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATASOURCE_DATABASE);
        snowflakeOther =  new SnowflakeUtils(connectionOther);
        snowflakeOther.createDatabase(DATASOURCE_DATABASE_OTHER);
        DATASOURCE_URL = testParams.getSnowflakeJdbcUrl();
        DATASOURCE_USERNAME = testParams.getSnowflakeUserName();
        DATASOURCE_PASSWORD = testParams.getSnowflakePassword();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), projectId);

    }

    @Test(dependsOnMethods = "initTest")
    public void connectToWorkspaceTest() {
        createNewDatasource();
        ConnectWorkSpaceDialog connectWorkSpaceDialog = DatasourceHeading.getInstance(browser).clickConnectButton();
        connectWorkSpaceDialog.searchWorkspace(projectId);
        connectWorkSpaceDialog.selectedWorkspaceOnSearchList(projectId);
        ldmPage = connectWorkSpaceDialog.clickSelect();

        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();

        wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getMessageConnectDatasource(), "Data Source connected. You can now use it in the model.");
        datasourcePanel = DataSourcePanelContent.getInstance(browser);
        dropDownBar = datasourcePanel.getDropdownDatasource();
        assertEquals(dropDownBar.getTextButton(), DATASOURCE_NAME);
        dataSourceContent = DataSourceContent.getInstance(browser);
        assertTrue(dataSourceContent.verifyConnectingMessage());
        DataSourceContentConnected connected = dataSourceContent.getDatasourceConnected();
        DataSourceSchema schema =  connected.getDatasourceSchema();
        assertEquals(schema.getTextNoTableInSchema(), "There are no tables in the schema");
        log.info("----Datasource verify----" + DATASOURCE_NAME);
    }

    @Test(dependsOnMethods = "connectToWorkspaceTest")
    public void refeshSchemaTest() throws SQLException, FileNotFoundException{
        prepareTables();
        updateData();
        dataSourceContent = DataSourceContent.getInstance(browser);
        DataSourceContentConnected connected = dataSourceContent.getDatasourceConnected();
        DataSourceSchema schema =  connected.getDatasourceSchema();
        schema.clickRefeshSchema();
        assertTrue(dataSourceContent.verifyConnectingMessage());
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        assertTrue(schemaContent.isTableExisting(PERSON_TABLE));
        assertTrue(schemaContent.isTableExisting(CAR_TABLE));
        assertTrue(schemaContent.isTableExisting(PRE_CAR_TABLE));
        assertEquals(schema.getTextSchemaName(), PUBLIC_SCHEMA);

        BubleContent popUpContentPerson = schemaContent.openPopUpTable(PERSON_TABLE);
        assertEquals(popUpContentPerson.getItemName(), PERSON_TABLE + " (table)");
        List<String> listColumnOfPerSon = asList("ID", "NAME", "AGE", "CITY", "BIRTHDAY", "CLIENTID", "TIMESTAMP", "DELETED");
        assertTrue(popUpContentPerson.isPopUpContainsList(listColumnOfPerSon));
    }

    @Test(dependsOnMethods = "refeshSchemaTest" )
    public void searchAndReviewDataTest() {
        DataSourceContentConnected connected = dataSourceContent.getDatasourceConnected();
        connected.searchTable(PERSON_TABLE);
        DataSourceSchema schema =  connected.getDatasourceSchema();
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        WebElement modelContent = modeler.getLayout().getRoot();
        jsFile = getResourceAsString("/dragdrop.js");

        //Add table Person and verify preview Dialog
        schemaContent.dragdropTableToCanvas(PERSON_TABLE, jsFile);
        PreviewCSVDialog dialog = PreviewCSVDialog.getInstance(browser);
        GenericList dropdownRecommend = GenericList.getInstance(browser);
        dropdownRecommend.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        assertEquals(dialog.getListHeaders(), asList("ID", "NAME", "AGE", "CITY", "BIRTHDAY", "CLIENTID", "TIMESTAMP", "DELETED"));
        assertTrue(dialog.isShowCorrectRow("6"));
        assertEquals(dialog.getEditDatasetZone().getListColumns(), DROP_DOWN_PERSON_ALL_FIELDS);
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(ID_ATTRIBUTE), "Primary key");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(NAME_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(AGE_FACT), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(CITY_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(BIRTHDAY_DATE), "Date");
        dialog.clickImportButton();

        //Add table PRE_CAR and verify preview Dialog
        connected.clearSearchText();
        connected.searchTable(PRE_OTHER_TABLE);
        assertEquals(dataSourceContent.getDatasourceNoResultText(), "No results for\n" + "\""
                + PRE_OTHER_TABLE + "\"");
        connected.clearSearchText();
        schemaContent.dragdropTableToCanvas(PRE_CAR_TABLE, jsFile);
        PreviewCSVDialog dialogCar = PreviewCSVDialog.getInstance(browser);
        GenericList dropdownRecommendCar = GenericList.getInstance(browser);
        dropdownRecommendCar.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        assertEquals(dialogCar.getListHeaders(), asList("CP__ID", "A__COLOR", "D__YEAR", "F__PRICE", "R__PERSON"));
        assertTrue(dialogCar.isShowCorrectRow("6"));
        assertEquals(dialogCar.getEditDatasetZone().getListColumns(), DROP_DOWN_CAR_ALL_FIELDS);
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(ID_ATTRIBUTE), "Primary key");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(COLOR_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(YEAR_DATE), "Date");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PRICE_FACT), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PERSON_REFERENCE), "Reference");
        dialog.clickImportButton();

        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        assertTrue(isElementVisible(mainModelContent.getModel(PERSON_DATASET).getRoot()));
        assertTrue(isElementVisible(mainModelContent.getModel(CAR_DATASET).getRoot()));
    }

    @Test(dependsOnMethods = "searchAndReviewDataTest" )
    public void editDatasetDetail() {
        //delete Attribute, delete Date
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent.focusOnDataset(PERSON_DATASET);

        Model modelPerson = mainModelContent.getModel(PERSON_DATASET);
        modelPerson.deleteAttributeOnDataset("clientid").deleteAttributeOnDataset("deleted");

        DateModel modelTimestamp = mainModelContent.getDateModel(TIMESTAMP_DATASET);
        mainModelContent.focusOnDateDataset(TIMESTAMP_DATASET);
        modelTimestamp.deleteDateModel();
        // there are 2 overlay wrapper on UI now, need provide index
        OverlayWrapper.getInstanceByIndex(browser,1).getConfirmDeleteDatasetDialog().clickDeleteDataset();

        //Go to detail of Dataset person, add system fields
        mainModelContent.focusOnDataset(PERSON_DATASET);
        // there are 2 overlay wrapper on UI now, need provide index
        modelPerson.openEditDialog(1);
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        LdmControlLoad controlLoad = LdmControlLoad.getInstance(browser);
        // ON system fields
        controlLoad.toogleDistributedLoad().toogleIncrementalLoad().toogleDeletedRowsLoad();

        mappingTab.editDistributedLoadMapping("CLIENTID", false);
        mappingTab.editIncrementalLoadMapping("TIMESTAMP", false);
        mappingTab.editDeletedRowsMapping("DELETED", false);
        dialog.saveChanges();

        //Verify Detail Dataset Person : Mapped to,Mapping Fields, Datatype,
        mainModelContent.focusOnDataset(PERSON_DATASET);
        // there are 2 overlay wrapper on UI now, need provide index
        modelPerson.openEditDialog(1);
        dialog.clickOnDataMappingTab();
        MappedTo mappedTo = MappedTo.getInstance(browser);
        assertEquals(mappedTo.getSourceName(), PERSON_TABLE);
        assertEquals(mappingTab.getSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "ID");
        assertEquals(mappingTab.getSourceColumnByName(NAME_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "NAME");
        assertEquals(mappingTab.getSourceColumnByName(AGE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "AGE");
        assertEquals(mappingTab.getSourceColumnByName(CITY_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "CITY");
        assertEquals(mappingTab.getSourceColumnByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "BIRTHDAY");
        assertEquals(mappingTab.getSourceColumnByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "CLIENTID");
        assertEquals(mappingTab.getSourceColumnByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "TIMESTAMP");
        assertEquals(mappingTab.getSourceColumnByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "DELETED");

        assertEquals(mappingTab.getSourceTypeByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "VARCHAR");
        assertEquals(mappingTab.getSourceTypeByName(AGE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "NUMBER");
        assertEquals(mappingTab.getSourceTypeByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "DATE");
        assertEquals(mappingTab.getSourceTypeByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "String");
        assertEquals(mappingTab.getSourceTypeByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "Timestamp");
        assertEquals(mappingTab.getSourceTypeByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "Boolean");
        dialog.clickCancel();

        //Verify Detail Dataset Car :  Mapped to, Mapping Fields
        mainModelContent.focusOnDataset(CAR_DATASET);
        Model modelCar = mainModelContent.getModel(CAR_DATASET);
        modelCar.openEditDialog(1);
        dialog.clickOnDataMappingTab();
        MappedTo mappedToCar = MappedTo.getInstance(browser);
        assertEquals(mappedToCar.getSourceName(), PRE_CAR_TABLE);
        assertEquals(mappingTab.getSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "CP__ID");
        assertEquals(mappingTab.getSourceColumnByName(COLOR_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "A__COLOR");
        assertEquals(mappingTab.getSourceColumnByName(PRICE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "F__PRICE");
        assertEquals(mappingTab.getSourceColumnByName(YEAR_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "D__YEAR");
        assertEquals(mappingTab.getSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "R__PERSON");
        assertEquals(mappingTab.getSourceColumnByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "X__CLIENT_ID");
        assertEquals(mappingTab.getSourceColumnByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "X__TIMESTAMP");
        assertEquals(mappingTab.getSourceColumnByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "X__DELETED");
    }

    @Test(dependsOnMethods = "editDatasetDetail" )
    public void publishModelTest() throws IOException {
        // Cancel Dialog on previous test
        EditDatasetDialog dialogCar = EditDatasetDialog.getInstance(browser);
        dialogCar.clickCancel();

        //add table from OTHER schema
        dropDownBar.selectDatasource(DATASOURCE_NAME_OTHER);
        DataSourceContentConnected connected = dataSourceContent.clickButtonConnect();
        DataSourceSchema schema =  connected.getDatasourceSchema();
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        jsFile = getResourceAsString("/dragdrop.js");
        schemaContent.dragdropTableToCanvas(PRE_OTHER_TABLE, jsFile);
        PreviewCSVDialog dialog = PreviewCSVDialog.getInstance(browser);
        dialog.clickImportButton();

        //publish model
        toolbar = modeler.getToolbar();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        sleepTightInSeconds(200);

        String sql = getResourceAsString("/model_snowflake_current.txt");
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, projectId);
        modelView = modelRestRequest.getProductionProjectModelView(false);
        assertEquals(modelView .toString(), sql);
    }

    @Test(dependsOnMethods = "publishModelTest" )
    public void runADDAndVerifyKPITest() throws IOException {
        setUpDataMapping();
        setUpProcess();
        setUpKPIs();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException, SQLException {
        log.info("Clean up...............");
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        dataMappingProjectIdUtils.deleteProjectIdDataMapping(projectId);
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME), "Datasource " + DATASOURCE_NAME + " should be deleted");
        }

        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_OTHER)) {
            deleteDatasource(DATASOURCE_NAME_OTHER);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME_OTHER), "Datasource " + DATASOURCE_NAME_OTHER + " should be deleted");
        }
        snowflakeUtils.dropDatabaseIfExists(DATASOURCE_DATABASE_OTHER);
        snowflakeUtils.dropDatabaseIfExists(DATASOURCE_DATABASE);
        snowflakeUtils.closeSnowflakeConnection();
    }

    private void createNewDatasource() {
        initDatasourceManagementPage();
        //create new other datasource for verifying add datasource from many sources
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        ContentDatasourceContainer containerOther = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configurationOther = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME_OTHER);
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE_OTHER, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        dataSourceOtherId = container.getDataSourceId();
        log.info("---ID Datasource Other:" + dataSourceOtherId);
        // create main datasource
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail snowflakeDetail = container.getConnectionDetail();
        dataSourceId = container.getDataSourceId();
        log.info("---ID Datasource Main:" + dataSourceId);
        checkSnowflakeDetail(container.getDatasourceHeading().getName(), snowflakeDetail.getTextUrl(), snowflakeDetail.getTextUsername(),
                snowflakeDetail.getTextDatabase(), snowflakeDetail.getTextWarehouse(), snowflakeDetail.getTextPrefix(),
                snowflakeDetail.getTextSchema());
    }

    private void checkSnowflakeDetail(String name, String url, String username,
                                      String database, String warehouse, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME));
        assertEquals(url, DATASOURCE_URL);
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(warehouse, DATASOURCE_WAREHOUSE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }

    private void prepareTables() throws SQLException {
        snowflakeUtils.executeSql("CREATE TABLE person (id varchar(128) primary key, name varchar(255), age integer," +
                "city varchar(255), birthday date, clientid varchar(255), timestamp TIMESTAMP, deleted boolean)");
        snowflakeUtils.executeSql("CREATE TABLE car(id varchar(128), color varchar(255), year date, price integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean, owner varchar(128) " +
                "FOREIGN KEY REFERENCES person(id))");
        snowflakeUtils.executeSql("CREATE TABLE pre_car(cp__id varchar(128), a__color varchar(255), d__year date, f__price integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean, r__person varchar(128) " +
                "FOREIGN KEY REFERENCES person(id))");
        snowflakeOther.executeSql("CREATE TABLE pre_other(cp__id varchar(128), a__color varchar(255), d__year date, f__priceother integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean)");
    }

    private void updateData() throws FileNotFoundException, SQLException {
        CsvFile personCsv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/person.csv"));
        CsvFile carCsv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/car.csv"));
        CsvFile precarCsv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/pre_car.csv"));
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", PERSON_TABLE, personCsv.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", CAR_TABLE, carCsv.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", PRE_CAR_TABLE, precarCsv.getFilePath());
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
        JSONObject jsonDatasetPerson = processUtils.setModeDefaultDataset(PERSON_DATASET);
        JSONObject jsonDatasetCar = processUtils.setModeDefaultDataset(CAR_DATASET);
        log.info("jsonDataset Person : " + jsonDatasetPerson);
        log.info("jsonDataset Car : " + jsonDatasetCar);
        List<JSONObject> listJson = new ArrayList<JSONObject>();
        listJson.add(jsonDatasetPerson);
        listJson.add(jsonDatasetCar);
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
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}, {dataset.%s, full}]",
                        testParams.getProjectId(), CLIENT_ID, CAR_DATASET, PERSON_DATASET)));
    }

    private void setUpKPIs() {
        log.info("Setup KPIs...............");
        getMetricCreator().createSumAgeMetricAdvance();
        getMetricCreator().createSumPriceMetricAdvance();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AGE));
        createInsightHasOnlyMetric(INSIGHT_NAME_2, ReportType.COLUMN_CHART, asList(METRIC_PRICE));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(10).waitForWidgetsLoading();
        log.info("Delete cookies..............");
        browser.manage().deleteAllCookies();
        sleepTightInSeconds(3);
        indigoDashboardsPage.addDashboard().addInsight(INSIGHT_NAME).addInsight(INSIGHT_NAME_2).selectDateFilterByName("All time")
                .waitForWidgetsLoading().changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
        List<String> listValue = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        log.info("listValue : " + listValue);
        List<String> listValuePrice = indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME_2)
                .getChartReport().getDataLabels();
        log.info("listValuePrice : " + listValuePrice);
        assertEquals(listValue, singletonList("$80.00"), "Unconnected filter make impact to insight");
        assertEquals(listValuePrice, singletonList("$12,000.00"), "Unconnected filter make impact to insight");
    }

    protected Metrics getMetricCreator() {
        return new Metrics(restClient, projectId);
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private void setUpDataMapping() {
        List<Pair<String, String>> listProjectIdMapping = new ArrayList<>();
        listProjectIdMapping.add(Pair.of(projectId, CLIENT_ID));

        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), listProjectIdMapping, asList(), dataSourceId,
                projectId);
        dataMappingProjectIdUtils.createDataMapping();
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
