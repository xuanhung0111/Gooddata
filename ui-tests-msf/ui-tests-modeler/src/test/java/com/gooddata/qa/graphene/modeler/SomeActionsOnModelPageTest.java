package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.freegrowth.WorkspaceHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.TableView;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.EditDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.TableViewDataset;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.SaveAsDraftDialog;
import com.gooddata.qa.graphene.fragments.modeler.ViewDetailDialog;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.LeaveConfirmDialog;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.FileUploadDialog;

import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.Test;
import java.util.List;
import java.util.NoSuchElementException;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static java.lang.String.format;


public class SomeActionsOnModelPageTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private ToolBar toolbar;
    private Canvas canvas;
    private TableView tableView;
    private TableViewDataset tableViewDataset;
    private MainModelContent mainModelContent;
    private RestClient restClient;
    private EditDatasetDialog editDialog;

    private final String COMPANY_DATASET = "company";
    private final String CITY_DATASET = "city";
    private final String DISTRICT_ATTR = "district";
    private final String STREET_ATTR = "street";
    private final String HOUSE_ATTR = "house";
    private final String OFFICE_LINK_ATTR = "officelink";
    private final String OFFICE_TEXT_ATTR = "officetext";
    private final String PEOPLE_ATTR = "people";
    private final String EMPLOYEE_ATTR = "employee";
    private final String HARD_WORKER_LABEL = "hardworker";
    private final String ACCOUNT_ATTR = "accounter";
    private final String SALARY_FACT = "salary";
    private final String POPULATION_FACT = "population";
    private final String ASCENDING_REPORT = "Ascending Report";
    private final String DESCENDING_REPORT = "Descending Report";
    private final String NEW_SORT_REPORT = "New Sort Report";
    private final String DEFAULT_REPORT = "Default Report";
    private final String SECOND_LABEL_REPORT = "Second Label Report";

    private final String SAVE_AS_DRAFT_CONTENT = "These changes will not be reflected in the workspace until you publish them.\n\n"
            + "All your changes are stored as a draft.";
    private final String DIALOG_CONTENT = "Don’t worry, your unpublished changes are saved as a local draft.";
    private final String DIALOG_HEADER = "The changes you made have not been published yet";
    private final String NO_KEY_MESSAGE = "To create a connection the dataset must have primary key consisting of one attribute.\n"
            + "Click More… to set a primary key.";
    private final String COMPOUND_KEY_MESSAGE = "To create a connection the dataset must have primary key consisting of one attribute. "
            + "Compound primary key cannot be used.\n" + "Click More… to change primary key.";
    private final String VALID_CONNECTION = "Drag and drop to connect this dataset to another dataset. "
            + "A reference to this dataset will be added to the connected dataset.";


    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
    }

    @Test(dependsOnMethods = {"initTest"})
    public void initialPageTest() {
        canvas = modeler.getLayout().getCanvas();
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_default_model.txt"));
        initLogicalDataModelPage();
        waitForFragmentVisible( modeler.getLayout());
        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        uploadCsvFile();
        initLogicalDataModelPage();
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void saveAsDraft() {
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(DISTRICT_ATTR, CITY_DATASET);
        assertEquals(modelCity.getAttributeText(DISTRICT_ATTR), DISTRICT_ATTR);
        assertTrue(toolbar.isSaveAsDraftVisible(), "Save as draft button should be displayed");
        assertEquals(toolbar.getDescriptionSaveButton(), "Last edit a few seconds ago");

        toolbar.clickSaveAsDraftBtn();
        SaveAsDraftDialog dialog = SaveAsDraftDialog.getInstance(browser);
        assertTrue(dialog.isDialogVisible(), "Save As Draft dialog should be shown");
        assertEquals(dialog.getContentDialog(), SAVE_AS_DRAFT_CONTENT);
        assertTrue( dialog.getDraftHistory().contains("Today at"));
        dialog.discardDraft();
        assertFalse(modelCity.isAttributeExistOnModeler(DISTRICT_ATTR));

        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(DISTRICT_ATTR, CITY_DATASET);
        toolbar.waitForSaveAsDraftButtonDisplay();
        publishOverrideModel();
        toolbar.waitForSaveAsDraftButtonHidden();
        assertFalse(toolbar.isSaveAsDraftVisible(), "Save as draft button should be hidden");
        assertTrue(modelCity.isAttributeExistOnModeler(DISTRICT_ATTR), "Attribute must be in dataset");
    }

    @Test(dependsOnMethods = {"saveAsDraft"})
    public void changeIdentifier() throws NoSuchElementException {
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(STREET_ATTR, CITY_DATASET);
        mainModelContent.addFactToDataset(POPULATION_FACT, CITY_DATASET);
        assertTrue(modelCity.isAttributeExistOnDataset(CITY_DATASET, STREET_ATTR));
        assertTrue(modelCity.isFactExistOnDataset(CITY_DATASET, POPULATION_FACT));

        modelCity.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        dialog.moveLabelToDataset("attr." + CITY_DATASET + "." + STREET_ATTR).selectDataset(COMPANY_DATASET);
        dialog.moveLabelToDataset("attr." + CITY_DATASET + "." + DISTRICT_ATTR).selectDataset(COMPANY_DATASET);
        dialog.saveChanges();
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.openEditDialog();
        dialog.moveLabelToDataset("fact." + CITY_DATASET + "." + POPULATION_FACT).selectDataset(COMPANY_DATASET);
        dialog.saveChanges();

        Model modelCompany = mainModelContent.getModel(COMPANY_DATASET);
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        assertTrue(modelCompany.isAttributeExistOnDataset(COMPANY_DATASET, STREET_ATTR));
        assertTrue(modelCompany.isFactExistOnDataset(COMPANY_DATASET, POPULATION_FACT));
        modelCompany.openEditDialog();
        ViewDetailDialog viewDialog = ViewDetailDialog.getInstance(browser);
        assertEquals(viewDialog.getIdentifierOfAttribute(DISTRICT_ATTR), "attr.city.district");
        assertEquals(viewDialog.getIdentifierOfAttribute(STREET_ATTR), "attr.company.street");
        assertEquals(viewDialog.getIdentifierOfFact(POPULATION_FACT), "fact.company.population");
        modelCompany.clickCancelEditPopUp();

        modelCompany.moveAttributeOnDataset(COMPANY_DATASET, STREET_ATTR, CITY_DATASET);
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getMoveLabelSuccess(),format("Success! The \"%s\" was moved to dataset %s",
            STREET_ATTR, CITY_DATASET));

        modelCompany.moveFactOnDataset(COMPANY_DATASET, POPULATION_FACT, CITY_DATASET);
        assertEquals(wrapper.getMoveLabelSuccess(),format("Success! The \"%s\" was moved to dataset %s", POPULATION_FACT,
            CITY_DATASET));

        mainModelContent.focusOnDataset(CITY_DATASET);
        assertTrue(modelCity.isAttributeExistOnDataset(CITY_DATASET, STREET_ATTR));
        assertTrue(modelCity.isFactExistOnDataset(CITY_DATASET, POPULATION_FACT));
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        assertFalse(modelCompany.isAttributeExistOnDataset(COMPANY_DATASET, STREET_ATTR));
        assertFalse(modelCompany.isFactExistOnDataset(COMPANY_DATASET, POPULATION_FACT));

        toolbar.waitForSaveAsDraftButtonDisplay();
        toolbar.clickSaveAsDraftBtn();
        SaveAsDraftDialog dialogSaveAsDraft = SaveAsDraftDialog.getInstance(browser);
        dialogSaveAsDraft.discardDraft();
        mainModelContent.focusOnDataset(CITY_DATASET);
        assertFalse(modelCity.isAttributeExistOnDataset(CITY_DATASET, STREET_ATTR));
    }

    @Test(dependsOnMethods = {"saveAsDraft"})
    public void changeLabelTypeToHyperlink() throws NoSuchElementException {
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        dialog.addNewLabelTypeHyperlink(PEOPLE_ATTR, OFFICE_LINK_ATTR);
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.openEditDialog();
        ViewDetailDialog viewDialog = ViewDetailDialog.getInstance(browser);
        assertEquals(viewDialog.getTypeOfLabelLink(OFFICE_LINK_ATTR), "Hyperlink");
        modelCity.clickCancelEditPopUp();

        publishOverrideModel();
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.openEditDialog();
        assertEquals(viewDialog.getTypeOfLabelLink(OFFICE_LINK_ATTR), "Hyperlink");

        viewDialog.getTypeOfLabel(PEOPLE_ATTR);
        assertFalse(viewDialog.isChangeLabelTypeIconVisible(), "Must be hidden icon navigatedown");
        viewDialog.getTypeOfLabelLink(OFFICE_LINK_ATTR);
        assertTrue(viewDialog.isChangeLabelTypeIconVisible(), "Must be shown icon navigatedown");

        viewDialog.addNewLabel(PEOPLE_ATTR, OFFICE_TEXT_ATTR);
        viewDialog.getTypeOfLabel(OFFICE_TEXT_ATTR);
        assertFalse(viewDialog.isChangeLabelTypeIconVisible(), "Must be hidden icon navigatedown");

        viewDialog.moveToLabelLink(OFFICE_LINK_ATTR).clickChangeLabelType();
        OverlayWrapper.getInstanceByIndex(browser,1).getTextEditorWrapper().clickAddTextLabel();
        assertEquals(viewDialog.getTypeOfLabel(OFFICE_LINK_ATTR), "Text label");

        viewDialog.getTypeOfLabel(OFFICE_TEXT_ATTR);
        assertTrue(viewDialog.isChangeLabelTypeIconVisible(), "Must be shown icon navigatedown");
        viewDialog.clickChangeLabelType();
        OverlayWrapper.getInstanceByIndex(browser,1).getTextEditorWrapper().clickAddLinkLabel();
        assertEquals(viewDialog.getTypeOfLabelLink(OFFICE_TEXT_ATTR), "Hyperlink");
        dialog.saveChanges();
        publishOverrideModel();
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.openEditDialog();

        assertEquals(viewDialog.getTypeOfLabel(OFFICE_LINK_ATTR), "Text label");
        assertEquals(viewDialog.getTypeOfLabelLink(OFFICE_TEXT_ATTR), "Hyperlink");
        dialog.clickCancel();
    }

    @Test(dependsOnMethods = {"saveAsDraft"})
    public void warningUserWhenLeavingModelerPage() {
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(HOUSE_ATTR, CITY_DATASET);
        WorkspaceHeader.getInstance(browser).goToLoadDataPage();

        LeaveConfirmDialog confirmDialog = LeaveConfirmDialog.getInstance(browser);
        assertEquals(confirmDialog.getDialogHeader(), DIALOG_HEADER);
        assertEquals(confirmDialog.getDialogContent(), DIALOG_CONTENT);
        assertTrue(confirmDialog.isLeaveConfirmDialogDisplay(), "Leave Confirm Dialog should be shown");

        confirmDialog.clickCloseDialog();
        assertFalse(confirmDialog.isLeaveConfirmDialogDisplay(),
        "Leave Confirm Dialog should be hidden when user press close button");

        WorkspaceHeader.getInstance(browser).goToLoadDataPage();
        confirmDialog.clickPublishButton();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        assertFalse(publishModelDialog.isPublishModelDialogDisplay(), "Publish dialog should be displayed");
        publishModelDialog.clickCancel();
        confirmDialog.waitForLeaveConfirmDialogHidden();

        assertFalse(confirmDialog.isLeaveConfirmDialogDisplay(),
        "Leave Confirm Dialog should be hidden when user click cancel press publish button");

        WorkspaceHeader.getInstance(browser).goToLoadDataPage();
        confirmDialog.clickLeaveAnywayButton();
        assertFalse(confirmDialog.isLeaveConfirmDialogDisplay(),
        "Leave Confirm Dialog should be hidden when user press leave anyway button");
        assertTrue(browser.getCurrentUrl().contains("admin/disc/#/projects/"),  "Must be load to disc page");

        initLogicalDataModelPage();
        mainModelContent.focusOnDataset(CITY_DATASET);
        assertFalse(modelCity.isAttributeExistOnModeler(HOUSE_ATTR), "Attribute mustn't be in dataset");
    }

    @Test(dependsOnMethods = {"saveAsDraft"})
    public void verifyTooltipMessageWhenRelationCannotCreated() {
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        assertFalse(modelCity.isDatasetTooltipDisplayed(), "Tooltip should be hidden");

        modelCity.hoverOnTooltipDataset();
        assertTrue(modelCity.isDatasetTooltipDisplayed(), "Tooltip should be shown");
        assertEquals(modelCity.getConnectionStatusOnDataset(), VALID_CONNECTION);

        modelCity.setPrimaryKey(ACCOUNT_ATTR);
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.hoverOnTooltipDataset();
        assertTrue(modelCity.isDatasetTooltipDisplayed(), "Tooltip should be shown");
        assertEquals(modelCity.getConnectionStatusOnDataset(), NO_KEY_MESSAGE);

        modelCity.setPrimaryKey(ACCOUNT_ATTR);
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.setPrimaryKey(PEOPLE_ATTR);
        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.hoverOnTooltipDataset();
        assertTrue(modelCity.isDatasetTooltipDisplayed(), "Tooltip should be shown");
        assertEquals(modelCity.getConnectionStatusOnDataset(), COMPOUND_KEY_MESSAGE);
        clickDiscardDraft();
    }

    @Test(dependsOnMethods = {"verifySortFunction"})
    public void verifyDefaultLabel() {
        Model modelCompany = mainModelContent.getModel(COMPANY_DATASET);
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        dialog.clickOnLabelInDataset(EMPLOYEE_ATTR, COMPANY_DATASET);
        assertFalse(dialog.isDefaultLabelPresent(EMPLOYEE_ATTR, COMPANY_DATASET), "Currently, it was default label");

        dialog.clickOnLabelInDataset(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        assertTrue(dialog.isDefaultLabelPresent(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET),
            "Currently, it should have option to set default label");
        dialog.clickOnLabelInDataset(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        dialog.clickCancel();

        assertEquals(createReportAndGetXaxisValue(DEFAULT_REPORT), asList("Lucky", "Job", "William", "Join", "Mie",
                "Bon", "David"));

        initLogicalDataModelPage();
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        dialog.setDefaultLabel(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL , COMPANY_DATASET);
        publishOverrideModel();

        assertEquals(createReportAndGetXaxisValue(SECOND_LABEL_REPORT), asList("Aguro", "Bruno", "Cris", "Harry", "Leo",
                "Mourinho", "Smith"));

        initLogicalDataModelPage();
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        dialog.deleteLabelInDatasetDialog(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        dialog.saveChanges();
        publishOverrideModel();
        AnalysisPage analysisPage  = initAnalysePage();
        assertFalse(analysisPage.searchInsight(SECOND_LABEL_REPORT), "Insight should be disappeared");
        analysisPage.openInsight(DEFAULT_REPORT).waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "SORRY, WE CAN'T DISPLAY THIS INSIGHT\n" +
                "Try applying different filters, or using different measures or attributes.\n" +
                "If this did not help, contact your administrator.");
    }

    @Test(dependsOnMethods = {"saveAsDraft"})
    public void verifySortFunction() {
        Model modelCompany = mainModelContent.getModel(COMPANY_DATASET);
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        editDialog = EditDatasetDialog.getInstance(browser);
        editDialog.clickOnLabelInDataset(EMPLOYEE_ATTR, COMPANY_DATASET);
        assertTrue(editDialog.isSortLabelPresent(EMPLOYEE_ATTR, COMPANY_DATASET),
                "Should be have option sort attribute for this label");

        editDialog.clickOnLabelInDataset(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        assertTrue(editDialog.isSortLabelPresent(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET),
                "Should be have option sort attribute for this label");
        editDialog.clickOnLabelInDataset(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);

        log.info("========Set Sort Ascending ========");
        editDialog.clickSortAttributeLabel(EMPLOYEE_ATTR, COMPANY_DATASET);
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        setSortAscendingForLabel(EMPLOYEE_ATTR, COMPANY_DATASET);
        assertEquals(createReportAndGetXaxisValue(ASCENDING_REPORT), asList("Bon", "David", "Job", "Join", "Lucky",
                "Mie", "William"));

        log.info("=====Set Sort Descending====");
        initLogicalDataModelPage();
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        setSortDescendingForLabel(EMPLOYEE_ATTR, COMPANY_DATASET);
        assertEquals(createReportAndGetXaxisValue(DESCENDING_REPORT), asList("William", "Mie", "Lucky", "Join", "Job",
                "David", "Bon"));

        log.info("=====Set Sort Ascending for second label====");
        initLogicalDataModelPage();
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        editDialog.clickSortAttributeLabel(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        mainModelContent.focusOnDataset(COMPANY_DATASET);
        modelCompany.openEditDialog();
        setSortAscendingForLabel(EMPLOYEE_ATTR + "." + HARD_WORKER_LABEL, COMPANY_DATASET);
        assertEquals(createReportAndGetXaxisValue(NEW_SORT_REPORT), asList("Lucky", "Job", "William", "Join", "Mie",
                "Bon", "David"));

        initLogicalDataModelPage();
    }

    public void setSortDescendingForLabel(String attribute, String dataset) {
        editDialog.setChangeSortOrder(attribute, dataset).sortDescending();
        editDialog.saveChanges();
        publishOverrideModel();
    }

    public void setSortAscendingForLabel(String attribute, String dataset) {
        editDialog.setChangeSortOrder(attribute, dataset).sortAscending();
        editDialog.saveChanges();
        publishOverrideModel();
    }

    public List<String> createReportAndGetXaxisValue(String reportName) {
        AnalysisPage analysisPage = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(SALARY_FACT, FieldType.FACT).addAttribute(EMPLOYEE_ATTR).waitForReportComputing()
            .saveInsight(reportName).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        return chartReport.getXaxisLabels();
    }

    public void publishOverrideModel() {
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteDataSwitchToEditMode();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        wrapper.closePublishSuccess();
    }

    public void uploadCsvFile() {
        CsvFile csv = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/company.csv"));
        toolbar.switchToTableView();
        tableView = TableView.getInstance(browser);
        tableViewDataset = tableView.getTableViewDataset();
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(COMPANY_DATASET);
        uploadDialog.pickCsvFile(csv.getFilePath());
        uploadDialog.importValidData();
    }

    public void clickDiscardDraft() {
        toolbar.waitForSaveAsDraftButtonDisplay();
        toolbar.clickSaveAsDraftBtn();
        SaveAsDraftDialog dialogSaveAsDraft = SaveAsDraftDialog.getInstance(browser);
        dialogSaveAsDraft.discardDraft();
    }
}
