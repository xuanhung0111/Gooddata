package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.Sidebar;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.DateModel;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.EditDateDimensionDialog;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;

import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.SnowflakeUtils;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import com.gooddata.qa.utils.cloudresources.ProcessUtils;
import com.gooddata.qa.utils.cloudresources.ConnectionInfo;
import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static java.lang.String.format;

public class LogicalDataModelPageTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private JSONObject modelViewUpdate;
    private RestClient restClient;
    private DataSourceRestRequest dataSourceRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private Project project;
    private String dataSourceId = null;
    private SnowflakeUtils snowflakeUtils;
    private DataSourceUtils dataSource;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;

    private final String PRESERVE_DATA = "Preserve data";
    private final String DROP_DATA = "Drop data";
    private final String USER_DATASET = "user";
    private final String DATABASE_NAME = "ATT_MODELER_DATABASE";
    private final String INSIGHT_NAME = "Insight Test";
    private final String DASHBOARD_NAME = "Dashboard Test";
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();
    private final String CLASS_DATASET = "class";
    private final String DATE_DATASET = "date";
    private final String USERNAME_ATTRIBUTE = "username";
    private final String AGE_FACT = "age";
    private final String DEFAULT_STRING = " (default)";
    private final String USERID_PRIMARY_KEY = "userid";
    private final String USERCODE_FIRST_ATTRIBUTE = "usercodefirst";
    private final String USERCODE_FIRST_LABEL = "usercodefirst";
    private final String USERCODE_ATRIBUTE = "usercode";
    private final String USERCODE_LABEL = "usercode";
    private final String USERNUMBER_ATTRIBUTE = "usernumber";
    private final String USERNOTSAVE_ATTRIBUTE = "usernotsave";
    private final String USERNOTSAVE_ATTRIBUTE_2 = "usernotsave2";
    private final String USERNOTSAVE_FACT = "userfactnotsave";
    private final String CLASSID_GRAIN = "classid";
    private final String CLASSCODE_GRAIN = "classcode";
    private final String CLASSNAME_ATTRIBUTE = "classname";
    private final String CLASSNAME2_ATTRIBUTE = "classname2";
    private final String CLASSNAME2_LABEL = "classname2label";
    private final String DATE = "Date";
    private final String SCHOOL_DATASET = "school";
    private final String SCHOOL_NAME_ATTRIBUTE = "schoolname";
    private final String CITY_DATASET = "city";
    private final String DELETE_SUCCESS_MESSAGE = "Success! The \"%s\" was deleted.";
    private final String PUBLISH_SUCCESS_MESSAGE = "Model published! Put your data to work. Open data load";
    private final String UP_TO_DATE_MESSAGE = "Model is already up-to-date! Load data now";
    private final String PUBLISH_ERROR_MESSAGE = "\"Cannot publish the data model with the “Preserve data” option." +
            " If you really want to publish this model, then select “Overwrite” option (set preserveData=false) to force" +
            " publish the data model (may cause deletion of data).\"";

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
        dataSource = new DataSourceUtils(testParams.getUser());
        project = getAdminRestClient().getProjectService().getProjectById(testParams.getProjectId());
        dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"initTest"})
    public void initialPageTest() {
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_model.txt"));
        //MSF-17161: Update links in the modeler page header
        assertEquals(ldmPage.getLinkDISC(), format("https://%s/admin/disc/#/projects", testParams.getHost()));
        assertTrue(ldmPage.getMenuItems().equals(Arrays.asList("Model data", "Load data")));
        initLogicalDataModelPage();
    }

    // User try to Change default name with some characters, new name should be changed and identifier was updated successfully
    // After have dataset , user add new attribute , fact by  Click on “+ABC”, “+123” button on Dataset.
    // Check that: Attribute , Fact are created successfully
    @Test(dependsOnMethods = {"initialPageTest"})
    public void addNewModel() {
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);

        //MSF-17107 : Disable URN changing for already existing date dimension
        DateModel modelDate = mainModelContent.getDateModel(DATE_DATASET);
        mainModelContent.focusOnDateDataset(DATE_DATASET);
        EditDateDimensionDialog dateModelDialog = modelDate.openEditDateDimensionDialog();
        assertTrue(dateModelDialog.isInputFieldDisable());
        dateModelDialog.clickCancel();

        mainModelContent.focusOnDataset(CLASS_DATASET);
        mainModelContent.addAttributeToDataset(CLASSNAME_ATTRIBUTE, CLASS_DATASET);
        mainModelContent.addAttributeToDataset(CLASSNAME2_ATTRIBUTE, CLASS_DATASET);
        mainModelContent.addAttributeToDataset(CLASSID_GRAIN, CLASS_DATASET);
        mainModelContent.addAttributeToDataset(CLASSCODE_GRAIN, CLASS_DATASET);
        assertEquals(USERID_PRIMARY_KEY, modelClass.getReferenceText(USER_DATASET));

        mainModelContent.focusOnDataset(USER_DATASET);
        mainModelContent.addAttributeToDataset(USERNAME_ATTRIBUTE, USER_DATASET);
        mainModelContent.addAttributeToDataset(USERCODE_FIRST_ATTRIBUTE, USER_DATASET);
        mainModelContent.addAttributeToDataset(USERNUMBER_ATTRIBUTE, USER_DATASET);
        mainModelContent.addFactToDataset(AGE_FACT, USER_DATASET);

        Model modelUser = mainModelContent.getModel(USER_DATASET);
        assertEquals(USERNAME_ATTRIBUTE, modelUser.getAttributeText(USERNAME_ATTRIBUTE));
        assertEquals(USERNUMBER_ATTRIBUTE, modelUser.getAttributeText(USERNUMBER_ATTRIBUTE));
        assertEquals(AGE_FACT, modelUser.getFactText(AGE_FACT));
        assertEquals(USERCODE_FIRST_ATTRIBUTE, modelUser.getAttributeText(USERCODE_FIRST_ATTRIBUTE));
        assertEquals(USERID_PRIMARY_KEY, modelUser.getAttributeText(USERID_PRIMARY_KEY));
        assertEquals(DATE, modelUser.getDateText());
    }

    // When user have something need to update , he can edit objects in dataset :
    // Add primary key
    // Add label, Edit name attribute/fact/
    // Edit data type between types of number (int, decimal))
    // change urn date dimension
    // delete unnecessary dataset, attribute, label
    @Test(dependsOnMethods = {"addNewModel"})
    public void editModel(){
        Model modelUser = mainModelContent.getModel(USER_DATASET);
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);

        mainModelContent.focusOnDataset(CITY_DATASET);
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        modelCity.deleteDataset();

        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.editAttributeName(USERCODE_FIRST_ATTRIBUTE, USERCODE_ATRIBUTE);
        assertEquals(USERCODE_ATRIBUTE, modelUser.getAttributeText(USERCODE_ATRIBUTE));
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.editLabelName(USERCODE_FIRST_LABEL, USERCODE_LABEL);
        mainModelContent.focusOnDataset(USER_DATASET);
        //MSF-17077 : Attr/fact with empty name should not be added into the dataset
        mainModelContent.addAttributeToDataset("", USER_DATASET);
        modelUser.editDatatypeOfMainLabel(USERNUMBER_ATTRIBUTE, Model.DATA_TYPE.INTEGER.getClassName());
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.editDatatypeOfMainLabel(USERCODE_ATRIBUTE, Model.DATA_TYPE.BIG_INTEGER.getClassName());
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.openEditDialog();
        assertEquals(USERCODE_LABEL + DEFAULT_STRING, modelUser.getTextLabel(USERCODE_LABEL));
        assertEquals(Model.DATA_TYPE.INTEGER.getName(), modelUser.getTextDatatype(USERNUMBER_ATTRIBUTE + DEFAULT_STRING));
        assertEquals(Model.DATA_TYPE.BIG_INTEGER.getName(), modelUser.getTextDatatype(USERCODE_ATRIBUTE + DEFAULT_STRING));
        //MSF-17631 : [Web Modeler] Should not displayed data type value on attribute row
        assertEquals(modelUser.getSizeOfDataType(), 5);
        //MSF-17077 : Attr/fact with empty name should not be added into the dataset
        //make sure that just show 3 attribute, not add empty attribute above
        assertEquals(modelUser.getNumberOfAttrtibutes(), 3);
        modelUser.clickCancelEditPopUp();

        //MSF-17514: Update dataset detail - add fact/attr
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.openMorePopUpOnDataset().editDatasetDialog();
        modelUser.addAttribute(USERNOTSAVE_ATTRIBUTE);
        assertTrue(modelUser.isAttributeExist(USERNOTSAVE_ATTRIBUTE));
        modelUser.editAttributeNameAndNotSave(USERNOTSAVE_ATTRIBUTE, USERNOTSAVE_ATTRIBUTE_2 );
        assertTrue(modelUser.isAttributeExist(USERNOTSAVE_ATTRIBUTE_2));
        modelUser.addFact(USERNOTSAVE_FACT);
        assertTrue(modelUser.isFactExist(USERNOTSAVE_FACT));
        modelUser.clickCancelEditPopUp();

        mainModelContent.focusOnDataset(CLASS_DATASET);
        modelClass.setPrimaryKey(CLASSID_GRAIN);
        mainModelContent.focusOnDataset(CLASS_DATASET);
        modelClass.addNewLabel(CLASSNAME2_ATTRIBUTE, CLASSNAME2_LABEL);
        mainModelContent.focusOnDataset(CLASS_DATASET);
        modelClass.deleteAttributeOnDataset(CLASSNAME2_ATTRIBUTE);
        assertEquals(OverlayWrapper.getInstanceByIndex(browser, 1).getTextDeleteSuccess(),format(DELETE_SUCCESS_MESSAGE, CLASSNAME2_ATTRIBUTE) );
        assertFalse(modelClass.isAttributeExistOnModeler(CLASSNAME2_ATTRIBUTE));
    }

    //After all steps above, the user has the model that he wants, he clicks the Publish button publish Model to project,
    // check that publish to Project successfully with 2 modes : Preserve data, and Drop cascade.
    // User can manage connection by create reference from dataset has single primary key to another dataset
    // (advance case : M:N bridge) and republish
    @Test(dependsOnMethods = "editModel")
    public void publishModel() throws IOException {
        //first try publish with preserve data mode
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        assertTrue(publishModelDialog.isPreserveDataDisable());

        //second try publish with overwrite data mode
        publishModelDialog.checkOnPreserveData();
        publishModelDialog.publishSwitchToEditMode();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), PUBLISH_SUCCESS_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/admin/disc/#/projects/%s", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();
        
        String sql = getResourceAsString("/model_view.txt");
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, testParams.getProjectId());
        modelView = modelRestRequest.getProductionProjectModelView(false);
        assertEquals(modelView.toString(), sql);

        //edit model, set grain, add new dataset, try publish again
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "update_new_dataset.txt"));
        initDashboardIgnoreAlert();
        modeler.getLayout().waitForLoading();
        mainModelContent.focusOnDataset(CLASS_DATASET);
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);
        modelClass.setPrimaryKey(CLASSCODE_GRAIN);
        mainModelContent.focusOnDataset(SCHOOL_DATASET);
        mainModelContent.addAttributeToDataset(SCHOOL_NAME_ATTRIBUTE, SCHOOL_DATASET);

        // publish with preserve mode after update model
        toolbar.clickPublish();
        assertTrue(publishModelDialog.isPreserveDataDisable());
        publishModelDialog.publishSwitchToEditMode();
        assertEquals(wrapper.getTextPublishSuccess(), PUBLISH_SUCCESS_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/admin/disc/#/projects/%s", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();

        String sqlUpdate = getResourceAsString("/model_view_update.txt");
        modelViewUpdate = modelRestRequest.getProductionProjectModelView(false);
        assertEquals(modelViewUpdate.toString(), sqlUpdate);
    }

    // User run ADDv2 makes sure he can upload data with valid Model publish above and verify on UI
    @Test(dependsOnMethods = "publishModel", groups = "not-sanity-aws")
    public void runADDAndVerifyUI() throws IOException, SQLException {
        setUpDatasource();
        setUpProcess();
        setUpKPIs();

        JSONObject jsonDataset = processUtils.setModeDefaultDataset(USER_DATASET);
        String valueParam = processUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = processUtils.execute(parameters);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageSpecificProject(testParams.getProjectId()).selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AGE).getValue(),
                "$90.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$90.00"), "Unconnected filter make impact to insight");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException {
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        if (dataloadProcess != null) {
            restClient.getProcessService().removeProcess(dataloadProcess);
        }
        if (dataSourceId != null) {
            dataSourceRestRequest.deleteDataSource(dataSourceId);
        }
    }

    private void setUpDatasource() throws SQLException, IOException {
        ConnectionInfo connectionInfo = dataSource.createSnowflakeConnectionInfo(DATABASE_NAME, DatabaseType.SNOWFLAKE);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        dataSourceId = dataSource.createDataSource(DATA_SOURCE_NAME, connectionInfo);
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

    private void setUpKPIs() {
        getMetricCreator().createSumAgeMetric();
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(METRIC_AGE).dataSet(DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AGE));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME)
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
    }

    private Metrics getMetricCreator() {
        return new Metrics(restClient, testParams.getProjectId());
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }
}
