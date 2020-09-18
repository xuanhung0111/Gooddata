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
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.Sidebar;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.TableView;
import com.gooddata.qa.graphene.fragments.modeler.TableViewDataset;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.modeler.PreviewCSVDialog;
import com.gooddata.qa.graphene.fragments.modeler.GenericList;
import com.gooddata.qa.graphene.fragments.modeler.DatasetEdit;
import com.gooddata.qa.graphene.fragments.modeler.ChooseReferencePopUp;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.EditDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.DataMapping;
import com.gooddata.qa.utils.CSVUtils;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.project.Project;
import org.json.JSONObject;
import org.openqa.selenium.support.FindBy;
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
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class E2EAutoMappingTest extends AbstractLDMPageTest {
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
    private final String MODEL_UP_TO_DATE_MESSAGE_DATALOAD = "Model is already up-to-date. Visit data load page";
    private final String PUBLISH_SUCCESS_MESSAGE = "Model successfully published. Explore data";
    private final String MODEL_UP_TO_DATE_MESSAGE = "Model is already up-to-date. Explore data";
    private final String INSIGHT_NAME = "Insight Test" + generateHashString();
    private final String DASHBOARD_NAME = "Dashboard Test" + generateHashString();
    private final String CLASS_DATASET = "class";
    private final String CLASS_TITLE = "Class";
    private static final String SCHOOL_ID = "Schoolid";
    private static final String CLASS_ID = "Classid";
    private static final String PRICE = "Price";
    private static final String TOTAL = "total";
    private static final String CREATED_DATE_DATASET = "Createdate";
    private static final String SCHOOL_DATASET = "school";
    private static final List<String> CSV_CLASS_CONTENT = asList("a__classid","a__classname","r__school","f__discount","f__price","d__createdate");
    private static final List<String> CSV_SCHOOL_CONTENT = asList("cp__schoolid","a__schoolname","f__total");
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
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), MODEL_UP_TO_DATE_MESSAGE_DATALOAD);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/admin/disc/#/projects/%s", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();
        final CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + fileName));
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        //set dataset reference
        GenericList dropdownRecommend = dialog.getEditDatasetZone().clickOnDatatypeByName(SCHOOL_ID);
        DatasetEdit edit = dropdownRecommend.selectReferenceItem();
        ChooseReferencePopUp referencePopup = edit.getChooseReferencePopUp(SCHOOL_ID);
        referencePopup.selectReferenceByName(SCHOOL_DATASET);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(SCHOOL_DATASET));

        dialog.clickImportButton();
        modeler.getLayout().waitForLoading();
        assertTrue(isElementVisible(mainModelContent.getModel(CLASS_DATASET).getRoot()));

        toolbar.clickPublish();
        publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), PUBLISH_SUCCESS_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/analyze/#/%s/reportId/edit", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();
    }

    // STEP 1: User drag/drop new Dataset from Sidebar , check that on Dataset Detail >> Data mapping,
    // it shows “Mapping not set” on Source column, from file Unknown
    // STEP 2: User Publish Model , check that publish model successfully
    // STEP 3: User  goes to Table View Download template, check that template CSV follow naming convention
    @Test(dependsOnMethods = "initTest")
    public void verifyDownloadTemplate() throws IOException {
        toolbar.switchToTableView();
        tableView = TableView.getInstance(browser);
        tableViewDataset = tableView.getTableViewDataset();
        tableViewDataset.clickButtonDownloadTemplate(SCHOOL_DATASET);
        File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + SCHOOL_DATASET + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        List<String> downloadContent = CSVUtils.readCsvFile(exportFile).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(downloadContent, containsInAnyOrder(CSV_SCHOOL_CONTENT.toArray()));
        try {
            Files.deleteIfExists(exportFile.toPath());
        } catch(Exception e) {
            log.info("File " + SCHOOL_DATASET + ".csv doesn't exist");
        }
        assertEquals(tableViewDataset.getStatus(SCHOOL_DATASET), "Published");
        assertFalse(tableViewDataset.getLastLoad(SCHOOL_DATASET).isEmpty());
    }

    //STEP 4: User goes to Table View load data by CSV file download at step 3 , check that Data Load successfully
    @Test(dependsOnMethods = "verifyDownloadTemplate")
    public void loadDataFollowNamingConvention() {
        //Update new file
        CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/school_naming_convention.csv"));
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(SCHOOL_DATASET);
        uploadDialog.pickCsvFile(csv.getFilePath());
        uploadDialog.importValidData();

        setUpKPIs();
        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, TOTAL).getValue(),
                "$20,000.00", "Unconnected filter make impact to kpi");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels(), singletonList("$20,000.00"), "Unconnected filter make impact to insight");
    }

    //STEP 5: User drag & drop CSV file ,web modeler create dataset
    //STEP 6: User can use custom mapping , but some reason missing some source columns, he want to load by Auto mapping
    //STEP 7: User  goes to Table View Download template, check that template CSV follow auto mapping
    @Test(dependsOnMethods = "loadDataFollowNamingConvention")
    public void useAutoMappingforImportCSVfile() throws IOException {
        initModelerPageContent();
        Model modelClass = mainModelContent.getModel(CLASS_DATASET);
        mainModelContent.focusOnDataset(CLASS_DATASET);
        scrollElementIntoView(modelClass.getRoot(), browser);
        modelClass.openEditDialog();
        EditDatasetDialog dialogEdit = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialogEdit.clickOnDataMappingTab();
        mappingTab.editSourceColumnByName(CLASS_ID, DataMapping.SOURCE_TYPE.LABEL.getName(), " ", true);
        dialogEdit.saveChanges();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteData();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), MODEL_UP_TO_DATE_MESSAGE);
        assertEquals(wrapper.getLinkPublishSuccess(),format("https://%s/analyze/#/%s/reportId/edit", testParams.getHost(),
                testParams.getProjectId()));
        wrapper.closePublishSuccess();

        toolbar.switchToTableView();
        tableViewDataset.clickButtonDownloadTemplate(CLASS_TITLE);
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
            log.info("File " + CLASS_DATASET + ".csv doesn't exist");
        }
        assertEquals(tableViewDataset.getStatus(CLASS_TITLE), "Published");
        assertFalse(tableViewDataset.getLastLoad(CLASS_TITLE).isEmpty());
    }

    //STEP 8: User  load data by CSV file downloaded above, check that Data Load successfully
    //STEP 9: He goes to Analyze, KPI page ,verify data upload for 2 dataset above
    @Test(dependsOnMethods = "useAutoMappingforImportCSVfile")
    public void loadDataDrapDropCSVFollowNamingConvention() {
        //Update new file
        CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/class_naming_convention.csv"));
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(CLASS_TITLE);
        uploadDialog.pickCsvFile(csv.getFilePath());
        uploadDialog.importValidData();

        initIndigoDashboardsPage().selectDateFilterByName("All time");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, PRICE).getValue(),
                "$20,000.00", "Unconnected filter make impact to kpi");
    }

    private void setUpKPIs() {
        getMetricCreator().createSumCustomMetric(PRICE, PRICE);
        getMetricCreator().createSumCustomMetric(TOTAL, TOTAL);
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder().metric(PRICE).dataSet(CREATED_DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        KpiConfiguration kpiTotal = new KpiConfiguration.Builder().metric(TOTAL).dataSet(CREATED_DATE_DATASET)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(TOTAL));
        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addInsightNext(INSIGHT_NAME).addKpi(kpiTotal)
                .changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
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
