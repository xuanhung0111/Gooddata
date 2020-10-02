package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.Sidebar;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.TableView;
import com.gooddata.qa.graphene.fragments.modeler.TableViewDataset;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.GenericList;
import com.gooddata.qa.graphene.fragments.modeler.DatasetEdit;
import com.gooddata.qa.graphene.fragments.modeler.ChooseReferencePopUp;
import com.gooddata.qa.graphene.fragments.modeler.DataMapping;
import com.gooddata.qa.graphene.fragments.modeler.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.modeler.PreviewCSVDialog;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.EditDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.project.Project;
import org.json.JSONObject;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class E2ECustomMappingTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private RestClient adminRestClient;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private ToolBar toolbarTableView;
    private Canvas canvas;
    private TableView tableView;
    private TableViewDataset tableViewDataset;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private Project project;

    private String fileName = "class.csv";
    private final String PUBLISH_SUCCESS_MESSAGE = "Model successfully published. Explore data";
    private final String MODEL_UP_TO_DATE_MESSAGE = "Model is already up-to-date. Explore data";
    private final String INSIGHT_NAME = "Insight Test" + generateHashString();
    private final String DASHBOARD_NAME = "Dashboard Test" + generateHashString();
    private final String CLASS_DATASET = "class";
    private final String CLASS_TITLE = "Class";
    private static final String CLASS_ID = "Classid";
    private static final String CLASS_NAME = "Classname";
    private static final String CLASS_NAME_CHANGED = "Classnamechanged";
    private static final String PRICE = "Price";
    private static final String PRICE_AFTER_CHANGE = "Price_After";
    private static final String DISCOUNT = "Discount";
    private static final String CREATE_DATE = "Createdate";
    private static final String BIRTHDAY_DATE = "birthday";
    private static final String END_DATE = "Enddate";
    private static final String SCHOOL_ID_KEY = "schoolid";
    private static final String SCHOOL_ID = "Schoolid";
    private static final String SCHOOL_NAME = "schoolname";
    private static final String TOTAL = "total";
    private static final String CREATED_DATE_DATASET = "createddate";
    private static final String END_DATE_DATASET = "enddate";
    private static final String SCHOOL_DATASET = "school";
    private static final String STUDENT_DATASET = "student";
    private static final String STUDENT_CODE = "studentcode";
    private static final String STUDENT_ID = "studentid";
    private static final String AGE = "age";
    private static final String SCHOOL_REFERENCE = "schoolid";
    private static final List<String> CSV_CLASS_CONTENT = asList("Classid", "Classname", "Createdate", "Discount", "Price", "Schoolid");
    private static final List<String> CSV_CLASS_CONTENT_EDITED = asList("Classid", "Classnamechanged", "Createdate", "Discount", "Price_After", "Schoolid");
    private static final List<String> CSV_SCHOOL_CONTENT = asList("Schoolid", "schoolname", "total");
    private static final List<String> CSV_STUDENT_CONTENT = asList("studentcode", "studentid", "age", "birthday");

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        adminRestClient = new RestClient(getProfile(ADMIN));
        initModelerPageContent();
        restClient = new RestClient(getProfile(ADMIN));
        project = getAdminRestClient().getProjectService().getProjectById(testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_model_mapping.txt"));
        initLogicalDataModelPage();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        modeler.getLayout().waitForLoading();
    }

    //STEP 1: User drag/drop CSV to Canvas, Modeler analyze the file and suggest structure of the dataset base on structure
    //STEP 2: User review structure and edit:
    // + type column (fact -> attribute)
    // + Column name
    // + date reference
    // + Dataset preference
    // + Set primary key
    // Confirm it , web modeler create dataset
    @Test(dependsOnMethods = "initTest")
    public void drapDropCSVAndReviewStructureTest() {
        final CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + fileName));
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());

        //User preview structure
        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        assertTrue(dialog.isShowCorrectRow("11"));
        assertEquals(dialog.getEditDatasetZone().getListColumns(), asList(CLASS_ID, CLASS_NAME, PRICE, DISCOUNT, CREATE_DATE, SCHOOL_ID));
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(CLASS_ID), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(CLASS_NAME), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(SCHOOL_ID), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PRICE), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextImbigousDateByName(CREATE_DATE), "Date");

        //User edit structure
        //edit type column
        GenericList dropdownRecommend = dialog.getEditDatasetZone().clickOnDatatypeByName(DISCOUNT);
        dropdownRecommend.selectBasicItem(GenericList.DATA_TYPE_PICKER.ATTRIBUTE.getClassName());
        //edit column name
        dialog.getEditDatasetZone().editColumnByName(CLASS_NAME, CLASS_NAME_CHANGED);
        //set primary key
        dialog.getEditDatasetZone().clickOnDatatypeByName(CLASS_ID);
        dropdownRecommend.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        //set date reference
        GenericList dropdownRecommendDate = dialog.getEditDatasetZone().clickOnImbigousDateByName(CREATE_DATE);
        DatasetEdit edit_date_reference = dropdownRecommendDate.selectReferenceItem();
        ChooseReferencePopUp referencePopupDate = edit_date_reference.getChooseReferencePopUp(CREATE_DATE);
        referencePopupDate.selectReferenceByName(CREATED_DATE_DATASET);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(CREATED_DATE_DATASET));
        //set dataset reference
        GenericList dropdownRecommend2 = dialog.getEditDatasetZone().clickOnDatatypeByName(SCHOOL_ID);
        DatasetEdit edit = dropdownRecommend2.selectReferenceItem();
        ChooseReferencePopUp referencePopup = edit.getChooseReferencePopUp(SCHOOL_ID);
        referencePopup.selectReferenceByName(SCHOOL_DATASET);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(SCHOOL_DATASET));

        //import to project
        dialog.clickImportButton();
        modeler.getLayout().waitForLoading();
        assertTrue(isElementVisible(mainModelContent.getModel(CLASS_DATASET).getRoot()));
    }

    //STEP 3: User goes to Dataset Detail > Data mapping, it shows correctly Source mapping , source type
    // And In table view Dataset A show Unpublished, 2 button download template, upload file disabled
    @Test(dependsOnMethods = "drapDropCSVAndReviewStructureTest")
    public void verifyOnTableViewAndDataMapping() {
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);
        mainModelContent.focusOnDataset(CLASS_DATASET);
        scrollElementIntoView(modelClass.getRoot(), browser);
        modelClass.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        assertEquals(dialog.getTextSourceName(),"class");

        assertEquals(mappingTab.getSourceColumnByName(SCHOOL_REFERENCE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), SCHOOL_ID);
        assertEquals(mappingTab.getSourceColumnByName(CLASS_ID, DataMapping.SOURCE_TYPE.LABEL.getName()), CLASS_ID);
        assertEquals(mappingTab.getSourceColumnByName(CLASS_NAME_CHANGED, DataMapping.SOURCE_TYPE.LABEL.getName()), CLASS_NAME);
        assertEquals(mappingTab.getSourceColumnByName(DISCOUNT, DataMapping.SOURCE_TYPE.LABEL.getName()), DISCOUNT);
        assertEquals(mappingTab.getSourceColumnByName(PRICE, DataMapping.SOURCE_TYPE.FACT.getName()), PRICE);
        assertEquals(mappingTab.getSourceColumnByName(CREATED_DATE_DATASET, DataMapping.SOURCE_TYPE.REFERENCE.getName()), CREATE_DATE);

        assertEquals(mappingTab.getSourceTypeByName(SCHOOL_REFERENCE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(CLASS_ID, DataMapping.SOURCE_TYPE.LABEL.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(CLASS_NAME_CHANGED, DataMapping.SOURCE_TYPE.LABEL.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(DISCOUNT, DataMapping.SOURCE_TYPE.LABEL.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(PRICE, DataMapping.SOURCE_TYPE.FACT.getName()), "number");
        assertTrue(mappingTab.getSourceTypeByName(CREATED_DATE_DATASET, DataMapping.SOURCE_TYPE.REFERENCE.getName()).contains("MM-dd-yyyy"));

        dialog.clickCancel();

        toolbar.switchToTableView();
        tableView = TableView.getInstance(browser);
        tableViewDataset = tableView.getTableViewDataset();
        assertEquals(tableViewDataset.getStatus(CLASS_TITLE), "Not published yet");
        assertTrue(tableViewDataset.getLastLoad(CLASS_TITLE).isEmpty());
        assertTrue(tableViewDataset.isButtonUpdateDisable(CLASS_TITLE));
        assertTrue(tableViewDataset.isButtonDownloadTemplateDisable(CLASS_TITLE));
        assertEquals(tableViewDataset.getStatus(SCHOOL_DATASET), "Published");
        assertEquals(tableViewDataset.getStatus(STUDENT_DATASET), "Published");
    }

    //STEP 4 : User publish Model and on Publish dialog, user check on “Upload data from Import CSV file” checkbox .show message successfully
    //STEP 5 : User goes to Analyze, KPI  page , verify data upload
    @Test(dependsOnMethods = "verifyOnTableViewAndDataMapping")
    public void publishModelAndImportData () {
        toolbarTableView = ToolBar.getInstanceInTableView(browser, 1);
        toolbarTableView.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), PUBLISH_SUCCESS_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/analyze/#/%s/reportId/edit", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();

        setUpKPIs();
        // Check results UI
        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, PRICE).getValue(),
                "$10,000.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$10,000.00"), "Unconnected filter make impact to insight");
    }

    //STEP 6: User goes to Table View and Download template , check that template CSV follow custom mapping,
    // check 2 columns: last load, publish
    @Test(dependsOnMethods = "publishModelAndImportData")
    public void verifyDownloadTemplate() throws IOException {
        initModelerPageContent();
        modeler.getLayout().waitForLoading();
        toolbar.switchToTableView();
        tableViewDataset.clickButtonDownloadTemplate(CLASS_TITLE);
        initModelerPageContent();
        modeler.getLayout().waitForLoading();
        toolbar.switchToTableView();
        File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + CLASS_DATASET + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        List<String> downloadContent = CSVUtils.readCsvFile(exportFile).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(downloadContent, containsInAnyOrder(CSV_CLASS_CONTENT.toArray()));
        try {
            Files.deleteIfExists(exportFile.toPath());
        } catch(Exception e) {
            log.info("File " + CLASS_TITLE + ".csv doesn't exist");
        }
        assertEquals(tableViewDataset.getStatus(CLASS_TITLE), "Published");
        assertFalse(tableViewDataset.getLastLoad(CLASS_TITLE).isEmpty());
    }

    // STEP 7 : By some reason , user goes to Data mapping to change :
    // Mapping source column
    // Date type
    // STEP 8 : User publish Model and on Publish dialog,user check on “Upload data from Import CSV file” checkbox same as step 4
    // Check publish sucessfully
    @Test(dependsOnMethods = "verifyDownloadTemplate")
    public void editDaMappingAndPublish()  {
        toolbarTableView.switchToModel();
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);
        mainModelContent.focusOnDataset(CLASS_DATASET);
        scrollElementIntoView(modelClass.getRoot(), browser);
        modelClass.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        mappingTab.editSourceColumnByName(PRICE, DataMapping.SOURCE_TYPE.FACT.getName(), PRICE_AFTER_CHANGE, true);
        mappingTab.editSourceColumnByName(CLASS_NAME_CHANGED, DataMapping.SOURCE_TYPE.LABEL.getName(), CLASS_NAME_CHANGED, true);
        mappingTab.editDateFormatByName(CREATED_DATE_DATASET, "yyyy-MM-dd");
        dialog.saveChanges();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), MODEL_UP_TO_DATE_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/analyze/#/%s/reportId/edit", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();
    }

    // STEP 9 : User goes to Table View and Download template , check that template CSV follow custom mapping as he edit at step 7
    // STEP 10 : Now he can goes to Table View and Upload Data again by click on “Update from file” with valid CSV file from step 9,
    // Check that Last upload in Table is updated
    @Test(dependsOnMethods = "editDaMappingAndPublish")
    public void downloadTemplateAfterEditAndLoadData() throws IOException {
        toolbar.switchToTableView();
        tableViewDataset.clickButtonDownloadTemplate(CLASS_TITLE);
        initLogicalDataModelPage();
        modeler.getLayout().waitForLoading();
        toolbar.switchToTableView();
        File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + CLASS_DATASET + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        List<String> downloadContent = CSVUtils.readCsvFile(exportFile).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(downloadContent, containsInAnyOrder(CSV_CLASS_CONTENT_EDITED.toArray()));
        try {
            Files.deleteIfExists(exportFile.toPath());
        } catch(Exception e) {
            log.info("File " + CLASS_TITLE + ".csv doesn't exist");
        }

        //Update new file
        CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/class_update.csv"));
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(CLASS_TITLE);
        uploadDialog.pickCsvFile(csv.getFilePath());
        uploadDialog.importValidData();

        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, PRICE).getValue(),
                "$20,000.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$20,000.00"), "Unconnected filter make impact to insight");
    }

    //STEP 11 : User want to load data for Dataset which already have on Project, he goes to Data mapping and add custom source column as he want
    //STEP 12 : User publish model again ,and goes to Table View load data, check that Data Load successfully
    //STEP 13 : He goes to Analyze, KPI page ,verify data upload at step 13, step 11
    @DataProvider(name = "importDataProvider")
    public Object[][] importDataProvider() {
        return new Object[][]{
                {SCHOOL_DATASET, SCHOOL_ID_KEY, SCHOOL_NAME, TOTAL, SCHOOL_ID, SCHOOL_NAME, TOTAL, CSV_SCHOOL_CONTENT, "/school.csv", "$60.00", false, null},
                {STUDENT_DATASET, STUDENT_CODE, STUDENT_ID,AGE, STUDENT_CODE, STUDENT_ID, AGE, CSV_STUDENT_CONTENT, "/student.csv", "$60.00", true, BIRTHDAY_DATE}
        };
    }
    @Test(dependsOnMethods = "downloadTemplateAfterEditAndLoadData", dataProvider = "importDataProvider")
    public void loadDataForDatasetWhichNotImported (String datasetName, String firstAttribute, String secondAttribute, String fact,
                                                    String changeFirstName, String changeSecondName, String changeName3, List<String> expectContent,
                                                    String file, String expectResult, boolean hasDate, String date) throws IOException {
        initModelerPageContent();
        Model model = mainModelContent.getModel(datasetName);
        mainModelContent.focusOnDataset(datasetName);
        scrollElementIntoView(model.getRoot(), browser);
        model.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        mappingTab.editSourceColumnByName(firstAttribute, DataMapping.SOURCE_TYPE.LABEL.getName(), changeFirstName, false);
        mappingTab.editSourceColumnByName(secondAttribute, DataMapping.SOURCE_TYPE.LABEL.getName(), changeSecondName, false);
        mappingTab.editSourceColumnByName(fact, DataMapping.SOURCE_TYPE.FACT.getName(), changeName3, false);
        if (hasDate == true) {
            mappingTab.editSourceColumnByName(date, DataMapping.SOURCE_TYPE.REFERENCE.getName(), date, false);
            dialog.saveChanges();
            mainModelContent.focusOnDataset(datasetName);
            scrollElementIntoView(model.getRoot(), browser);
            model.openEditDialog();
            dialog.clickOnDataMappingTab();
            mappingTab.editDateFormatByName(date, "yyyy-MM-dd");
            dialog.saveChanges();
        } else {
            dialog.saveChanges();
        }
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), MODEL_UP_TO_DATE_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/analyze/#/%s/reportId/edit", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();

        toolbar.switchToTableView();
        tableViewDataset.clickButtonDownloadTemplate(datasetName);
        initModelerPageContent();
        modeler.getLayout().waitForLoading();
        toolbar.switchToTableView();
        File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + datasetName + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        List<String> downloadContent = CSVUtils.readCsvFile(exportFile).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(downloadContent, containsInAnyOrder(expectContent.toArray()));
        try {
            Files.deleteIfExists(exportFile.toPath());
        } catch(Exception e) {
            log.info("File csv doesn't exist");
        }

        CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + file));
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(datasetName);
        uploadDialog.pickCsvFile(csv.getFilePath());
        uploadDialog.importValidData();

        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, fact).getValue(),
                expectResult, "Unconnected filter make impact to kpi");
    }

    private void setUpKPIs() {
        getMetricCreator().createSumCustomMetric(PRICE, PRICE);
        getMetricCreator().createSumCustomMetric(TOTAL, TOTAL);
        getMetricCreator().createSumCustomMetric(AGE, AGE);
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(PRICE).dataSet(CREATED_DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        KpiConfiguration kpiTotal = new KpiConfiguration.Builder().metric(TOTAL).dataSet(CREATED_DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        KpiConfiguration kpiAge = new KpiConfiguration.Builder().metric(AGE).dataSet(BIRTHDAY_DATE)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(PRICE));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME).addKpi(kpiTotal)
                .addKpi(kpiAge).changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
    }

    private Metrics getMetricCreator() {
        return new Metrics(adminRestClient, testParams.getProjectId());
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private void initModelerPageContent() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getLayout().getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
    }
}
