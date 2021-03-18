package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;

import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.Sidebar;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.PreviewCSVDialog;
import com.gooddata.qa.graphene.fragments.modeler.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.DuplicateDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.EditDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;


public class ImportCSVTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private Canvas canvas;
    private ToolBar toolbar;
    private MainModelContent mainModelContent;
    private PreviewCSVDialog dialog;
    private FileUploadDialog uploadDialog;
    private static String TXT_FILE = "outputstage_expected_1.txt";
    private static String COMPANY_DATASET = "company.csv";
    private static String DATASET_DIFF_VALUE = "company diff value.csv";
    private static String DATASET_MORE_VALUE = "company with more 2 rows.csv";
    private static String DATASET_LESS_VALUE = "company with less rows.csv";
    private static String DATASET_DUPLICATED_HEADER = "duplicatedheader.csv";
    private static String DATASET_EMPTY_HEADER = "emptyheader.csv";
    private static String DATASET_NAME = "company";
    private static String SECURITY_ATTR = "security";
    private static String OFFICE_ATTR = "office";
    private static String EMPLOYEE_ATTR = "employee";
    private static String HARD_WORKER_ATTR = "employeehardworker";
    private static String LAZY_WORKER_ATTR = "employeelazyworker";
    private static String WORKER_ATTR = "employeeworker";
    private static String AGE_FACT = "age";
    private static String SALARY_FACT = "salary";
    private static String RATE_FACT = "rate";
    private static String POPULATION_FACT = "population";
    private static String DUPLICATED_CONTENT = "The model already contains a dataset %s with the exactly same structure."
            + " There is nothing to update.";
    private static String WARNING_MESSAGE = "There is already a dataset with such a name.";
    private final String PUBLISH_SUCCESS_MESSAGE = "Model published and data uploaded! Put your data to work. Open dashboards";
    private final String INVALID_FILE = "The file %s could not be imported, because it is not a valid CSV file";
    private final String INCORRECT_MSG = "The header of some columns incorrect. Please check your file.";


    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        modeler.getLayout().waitForLoading();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
    }

    @Test(dependsOnMethods = "initTest")
    public void importNewCSVTest() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + COMPANY_DATASET);
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        assertFalse(dialog.isSelectedModifyDataset(), "Should NOT check on 'Modify structure dataset' option");
        dialog.clickImportButton();
        canvas = modeler.getLayout().getCanvas();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent.focusOnDataset(DATASET_NAME);

        Model companyModel = mainModelContent.getModel(DATASET_NAME);
        assertTrue(companyModel.isAttributeExistOnModeler(SECURITY_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(EMPLOYEE_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(HARD_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(LAZY_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(WORKER_ATTR));
        assertTrue(companyModel.isFactExistOnModeler(AGE_FACT));
        assertTrue(companyModel.isFactExistOnModeler(SALARY_FACT));
    }

    @Test(dependsOnMethods = "importNewCSVTest")
    public void importSameCsvTest() {
        final CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + COMPANY_DATASET));
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        DuplicateDatasetDialog duplicatedDialog =  DuplicateDatasetDialog.getInstance(browser);
        log.info("===Dialog content: " + duplicatedDialog.getDialogContent());
        assertThat(duplicatedDialog.getDialogContent(), containsString(String.format(DUPLICATED_CONTENT, "Company")));
        duplicatedDialog.clickCloseDialog();
    }

    @Test(dependsOnMethods = "importSameCsvTest")
    public void importDifferentValueCsvTest() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + DATASET_DIFF_VALUE);
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        assertFalse(dialog.isSelectedModifyDataset(), "Should NOT check on 'Modify structure dataset' option");
        dialog.checkOnModifyDatasetStructure();
        assertTrue(dialog.isSelectedModifyDataset(), "Should check on 'Modify structure dataset' option");
        assertEquals(dialog.getDatasetNameModify(), "Company");
        dialog.clickImportButton();

        Model companyModel = mainModelContent.getModel(DATASET_NAME);
        assertTrue(companyModel.isAttributeExistOnModeler(SECURITY_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(EMPLOYEE_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(HARD_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(LAZY_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(WORKER_ATTR));
        assertTrue(companyModel.isFactExistOnModeler(AGE_FACT));
        assertTrue(companyModel.isFactExistOnModeler(SALARY_FACT));

        mainModelContent.focusOnDataset(DATASET_NAME);
        companyModel.openEditDialog();
        EditDatasetDialog editDialog = EditDatasetDialog.getInstance(browser);
        assertFalse(editDialog.checkIdOfAttributeDatasetSameWithTitleDataset("company").isPresent(), "It's should be contain ID of dataset");
        editDialog.clickCancel();
    }

    @Test(dependsOnMethods = "importDifferentValueCsvTest")
    public void importSameCsvWithMore2RowsTest() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + DATASET_MORE_VALUE);
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        dialog.checkOnModifyDatasetStructure();
        dialog.checkOnCreateNewDataset();
        dialog.editCreateNewDataset("Company");

        assertTrue(dialog.isExistingDatasetWarningDisplay(), "Should display warning message");
        assertThat(dialog.getDatasetWarningDisplay(), containsString(WARNING_MESSAGE));

        dialog.clickModifyDatasetLink();
        assertTrue(dialog.isSelectedModifyDataset(), "Should check on 'Modify the existing dataset structure");
        dialog.clickImportButton();

        Model companyModel = mainModelContent.getModel(DATASET_NAME);
        assertTrue(companyModel.isAttributeExistOnModeler(SECURITY_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(EMPLOYEE_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(HARD_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(LAZY_WORKER_ATTR));
        assertTrue(companyModel.isAttributeExistOnModeler(WORKER_ATTR));
        assertTrue(companyModel.isFactExistOnModeler(SALARY_FACT));
        assertTrue(companyModel.isFactExistOnModeler(RATE_FACT));
        assertTrue(companyModel.isFactExistOnModeler(POPULATION_FACT));
        assertFalse(companyModel.isFactExistOnModeler(AGE_FACT));

        mainModelContent.focusOnDataset(DATASET_NAME);
        companyModel.openEditDialog();
        EditDatasetDialog editDialog = EditDatasetDialog.getInstance(browser);
        assertFalse(editDialog.checkIdOfAttributeDatasetSameWithTitleDataset(".company.").isPresent(), "It's should be contain ID of dataset");
        editDialog.clickCancel();
    }

    @Test(dependsOnMethods = "importSameCsvWithMore2RowsTest")
    public void importSameCsvWithLessRowsTest() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + DATASET_LESS_VALUE);
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        dialog.checkOnModifyDatasetStructure();
        dialog.clickImportButton();

        Model companyModel = mainModelContent.getModel(DATASET_NAME);
        mainModelContent.focusOnDataset(DATASET_NAME);
        assertTrue(companyModel.isAttributeExistOnModeler(OFFICE_ATTR));
        assertFalse(companyModel.isAttributeExistOnModeler(SECURITY_ATTR));
        assertFalse(companyModel.isAttributeExistOnModeler(EMPLOYEE_ATTR));
        assertFalse(companyModel.isAttributeExistOnModeler(HARD_WORKER_ATTR));
        assertFalse(companyModel.isAttributeExistOnModeler(LAZY_WORKER_ATTR));
        assertFalse(companyModel.isAttributeExistOnModeler(WORKER_ATTR));
        assertFalse(companyModel.isFactExistOnModeler(SALARY_FACT));
        assertFalse(companyModel.isFactExistOnModeler(RATE_FACT));
        assertFalse(companyModel.isFactExistOnModeler(POPULATION_FACT));
        companyModel.deleteDataset();
    }

    @Test(dependsOnMethods = "importSameCsvWithLessRowsTest")
    public void importSameNameCsvWithDifferentStructure() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + COMPANY_DATASET);
        dialog.clickImportButton();
        uploadCsvFile("/" + ResourceDirectory.OVERRIDE_CSV + "/" + COMPANY_DATASET);
        assertFalse(dialog.isSelectedCreateDataset(), "Should NOT check on 'Create a new dataset' option");
        assertTrue(dialog.isSelectedModifyDataset(), "Should check on 'Modify dataset structure' option");

        dialog.checkOnCreateNewDataset();
        assertThat(dialog.getDatasetWarningDisplay(), containsString(WARNING_MESSAGE));
        dialog.clickImportButton();
        Model companyModel = mainModelContent.getModel("company1");
        mainModelContent.focusOnDataset("company1");
        companyModel.openEditDialog();
        EditDatasetDialog editDialog = EditDatasetDialog.getInstance(browser);
        assertFalse(editDialog.checkIdOfAttributeDatasetSameWithTitleDataset(".company1.").isPresent(),
                "It's should be contain ID of dataset - Company1");
        editDialog.clickCancel();
        mainModelContent.focusOnDataset("company1");
        companyModel.deleteDataset();

        uploadCsvFile("/" + ResourceDirectory.OVERRIDE_CSV + "/" + COMPANY_DATASET);
        assertFalse(dialog.isSelectedCreateDataset(), "Should NOT check on 'Create a new dataset' option");
        assertTrue(dialog.isSelectedModifyDataset(), "Should check on 'Modify dataset structure' option");
        dialog.clickImportButton();

        Model currentModel = mainModelContent.getModel(DATASET_NAME);
        mainModelContent.focusOnDataset(DATASET_NAME);
        assertTrue(currentModel.isAttributeExistOnModeler(SECURITY_ATTR));
        assertTrue(currentModel.isAttributeExistOnModeler(EMPLOYEE_ATTR));
        assertTrue(currentModel.isAttributeExistOnModeler(HARD_WORKER_ATTR));
        assertTrue(currentModel.isAttributeExistOnModeler(LAZY_WORKER_ATTR));
        assertTrue(currentModel.isAttributeExistOnModeler(WORKER_ATTR));
        assertTrue(currentModel.isFactExistOnModeler(AGE_FACT));
        assertFalse(currentModel.isFactExistOnModeler(SALARY_FACT));
        currentModel.deleteDataset();
    }

    @DataProvider(name = "getDuplicatedAndEmptyHeader")
    public Object[][] getDuplicatedAndEmptyHeader() {
        return new Object[][] {
                {CsvFile.loadFile(getFilePathFromResource(
                        "/" + ResourceDirectory.UPLOAD_CSV + "/" + DATASET_DUPLICATED_HEADER)), "duplicatedheader"},
                {CsvFile.loadFile(getFilePathFromResource(
                        "/" + ResourceDirectory.UPLOAD_CSV + "/" + DATASET_EMPTY_HEADER)), "emptyheader"}
        };
    }

    @Test(dependsOnMethods = "importSameNameCsvWithDifferentStructure", dataProvider = "getDuplicatedAndEmptyHeader")
    public void importEmptyHeaderAndDuplicatedHeader(CsvFile csv, String datasetName) {
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        assertThat(dialog.getWarningMessage(), containsString(INCORRECT_MSG));

        dialog.clickImportButton();
        Model duplicatedModel = mainModelContent.getModel(datasetName);
        mainModelContent.focusOnDataset(datasetName);
        assertTrue(duplicatedModel.isAttributeExistOnModeler("name"));
        assertTrue(duplicatedModel.isAttributeExistOnModeler("classtable"));
        assertFalse(duplicatedModel.isAttributeExistOnModeler("classname"));
        duplicatedModel.deleteDataset();
    }

    @Test(dependsOnMethods = "importEmptyHeaderAndDuplicatedHeader")
    public void processImportCsvFileTest() {
        uploadCsvFile("/" + ResourceDirectory.UPLOAD_CSV + "/" + COMPANY_DATASET);
        assertTrue(dialog.isSelectedCreateDataset(), "Should check on 'Create a new dataset' option");
        assertFalse(dialog.isSelectedModifyDataset(), "Should NOT check on 'Modify dataset structure' option");
        dialog.clickImportButton();
        toolbar.clickPublish();
        PublishModelDialog  publishModelDialog = PublishModelDialog.getInstance(browser);
        assertTrue(publishModelDialog.isUploadCsvChecked(), "Must check on CSV upload");
        publishModelDialog.publishSwitchToEditMode();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getTextPublishSuccess(), PUBLISH_SUCCESS_MESSAGE);
    }

    @Test(dependsOnMethods = "initTest")
    public void uploadDifferentCSVFile() {
        final CsvFile csv = CsvFile.loadFile(getFilePathFromResource(
                "/" + ResourceDirectory.SQL_FILES + "/" + TXT_FILE));
        uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        assertThat(uploadDialog.getErrorMessage(), containsString(String.format(INVALID_FILE, TXT_FILE)));
        log.info("Header content is : " + uploadDialog.getErrorMessage());
        uploadDialog.cancelDialog();
    }

    public void uploadCsvFile(String csvPath) {
        final CsvFile csv = CsvFile.loadFile(getFilePathFromResource(csvPath));
        uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        dialog = PreviewCSVDialog.getInstance(browser);
    }
}
