package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.project.Project;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

public class ValidateImportCSVTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private TableView tableView;
    private TableViewDataset tableViewDataset;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private Project project;

    private static final String PAYROLL_DATASET = "Payroll";
    private static final String LESS_COLUMN_ERROR_MESSAGE = "Invalid rows=[97] (1 rows) detected with less columns than header.";
    private static final String MORE_COLUMN_ERROR_MESSAGE = "Invalid rows=[2, 5, 6] (3 rows) detected with more columns " +
            "than the header.";
    private static final String ONLY_HEADER_ERROR_MESSAGE = "CSV file contains no data.";
    private static final String TOO_MANY_CHARACTERS_ERROR_MESSAGE = "Error parsing row=2. The cell at column '6' is too long. " +
            "Maximum values are '10000' for the cell length.";
    private static final String TOO_MANY_COLUMNS_ERROR_MESSAGE = "Error parsing row=1. The number of columns is too high. " +
            "Maximum values are '250' for the column count.";
    private static final String DUPLICATE_NAME_ERROR_MESSAGE = "The generated dataset identifier is duplicated, rename CSV " +
            "file and upload again if you want to create new dataset.";
    private static final String LESS_COLUMN_TABLE_VIEW_ERROR_MESSAGE = "The number of columns in row [97] does not match " +
            "the number of columns defined in the file header.";
    private static final String MORE_COLUMN_TABLE_VIEW_ERROR_MESSAGE = "The number of columns in row [2, 5, 6] does not " +
            "match the number of columns defined in the file header.";
    private static final String ONLY_HEADER_TABLE_VIEW_ERROR_MESSAGE = "The file does not contain any data to load. Add " +
            "some data to the file, and try again.";
    private static final String TOO_MANY_COLUMNS_TABLE_VIEW_ERROR_MESSAGE = "Error parsing row=1. The number of columns is " +
            "too high. Maximum values are '250' for the column count.";
    private static final String TOO_MANY_CHARACTERS_TABLE_VIEW_ERROR_MESSAGE = "Error parsing row=2. The cell at column '6' " +
            "is too long. Maximum values are '10000' for the cell length.";
    private static final String EXCEEDED_PRECISION_TABLE_VIEW_ERROR_MESSAGE = "The values in the 'f__amount' column exceed " +
            "the precision defined by the 'NUMERIC(15,3)' data type.";
    private static final String WRONG_FORMAT_TABLE_VIEW_ERROR_MESSAGE = "The data in the 'f__amount' column cannot be converted " +
            "to 'NUMERIC(15,3)' format.";
    private static final String MISSING_COLUMN_TABLE_VIEW_ERROR_MESSAGE = "The CSV file is missing column 'd__date'.";
    private static final String ERROR_MESSAGE_PREVIEW_DIALOG = "The header row is missing or contains characters that are not allowed." +
            " Check the header row, and try again. The column names containing only numbers (for example, 111111) are not allowed.";
    private static final String WARNING_MESSAGE_PREVIEW_DIALOG = "The header of some columns incorrect. Please check your file.";

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getLayout().getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
        project = getAdminRestClient().getProjectService().getProjectById(testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "payroll_basic.txt"));
        initLogicalDataModelPage();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        modeler.getLayout().waitForLoading();
    }

    @DataProvider(name = "errorCsvFileProvider")
    public Object[][] errorCsvFileProvider() {
        final CsvFile lessColumn = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.less.columns.csv"));

        final CsvFile moreColumn = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.more.columns.csv"));

        final CsvFile onlyHeader = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.only.header.csv"));

        final CsvFile tooManyCharacters = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/too.many.characters.csv"));

        final CsvFile tooManyColumns = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/too.many.columns.csv"));

        final CsvFile duplicateName = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));

        return new Object[][]{
                {lessColumn, LESS_COLUMN_ERROR_MESSAGE},
                {moreColumn, MORE_COLUMN_ERROR_MESSAGE},
                {onlyHeader, ONLY_HEADER_ERROR_MESSAGE},
                {tooManyCharacters, TOO_MANY_CHARACTERS_ERROR_MESSAGE},
                {tooManyColumns, TOO_MANY_COLUMNS_ERROR_MESSAGE},
                {duplicateName, DUPLICATE_NAME_ERROR_MESSAGE}
        };
    }

    @Test(dependsOnMethods = "initTest", dataProvider = "errorCsvFileProvider")
    public void validateDrapDropCSV(CsvFile csv, String errorMessage) {
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        ErrorContent error = uploadDialog.importInvalidCSV();
        assertEquals(error.getErrorMessage(), errorMessage);
        assertEquals(error.getErrorTitle(), "Model generation failed");
        OverlayWrapper.getInstance(browser).closeWaitingDialog();
    }

    @DataProvider(name = "errorCsvFilePreviewProvider")
    public Object[][] errorCsvFilePreviewProvider() {
        final CsvFile noHeader = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.no.header.csv"));

        final CsvFile sameName = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.column.same.name.csv"));

        final CsvFile missingColumn = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.missing.column.name.csv"));

        return new Object[][]{
                {noHeader, true, ERROR_MESSAGE_PREVIEW_DIALOG, 0},
                {sameName, false, WARNING_MESSAGE_PREVIEW_DIALOG, 2},
                {missingColumn, false, WARNING_MESSAGE_PREVIEW_DIALOG, 1}
        };
    }

    @Test(dependsOnMethods = "validateDrapDropCSV", dataProvider = "errorCsvFilePreviewProvider")
    public void validateDrapDropCSVCasesShowPopUp(CsvFile csv, boolean isError, String expectedMessage,
                                                  int numberOfDisabledColumn) {
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        if(isError == true) {
            assertEquals(dialog.getErrorMessage(), expectedMessage);
        } else {
            assertEquals(dialog.getWarningMessage(), expectedMessage);
            assertEquals(dialog.getEditDatasetZone().getNumberOfDisabledColumns(), numberOfDisabledColumn);
        }
        dialog.clickCancelButton();
    }

    @DataProvider(name = "errorTableViewProvider")
    public Object[][] errorTableViewProvider() {
        toolbar.switchToTableView();
        tableView = TableView.getInstance(browser);
        tableViewDataset = tableView.getTableViewDataset();
        return new Object[][]{
                {"/payroll.precision.csv", EXCEEDED_PRECISION_TABLE_VIEW_ERROR_MESSAGE},
                {"/payroll.less.columns.csv", LESS_COLUMN_TABLE_VIEW_ERROR_MESSAGE},
                {"/payroll.more.columns.csv", MORE_COLUMN_TABLE_VIEW_ERROR_MESSAGE},
                {"/payroll.only.header.csv", ONLY_HEADER_TABLE_VIEW_ERROR_MESSAGE},
                {"/payroll.wrong.format.csv", WRONG_FORMAT_TABLE_VIEW_ERROR_MESSAGE},
                {"/payroll.missing.column.csv", MISSING_COLUMN_TABLE_VIEW_ERROR_MESSAGE},
                {"/too.many.characters.csv", TOO_MANY_CHARACTERS_TABLE_VIEW_ERROR_MESSAGE},
                {"/too.many.columns.csv", TOO_MANY_COLUMNS_TABLE_VIEW_ERROR_MESSAGE}
        };
    }

    @Test(dependsOnMethods = "validateDrapDropCSVCasesShowPopUp", dataProvider = "errorTableViewProvider")
    public void validateUploadCSVInTableView(String csvLink, String errorMessage) {
        CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + csvLink));
        FileUploadDialog uploadDialog = tableViewDataset.clickButtonUpdateFromFile(PAYROLL_DATASET);
        uploadDialog.pickCsvFile(csv.getFilePath());
        ErrorContent error = uploadDialog.importInvalidCSV();
        assertThat(error.getErrorMessage(), containsString(errorMessage));
        assertEquals(error.getErrorTitle(), "Load data from file failed.");
        OverlayWrapper.getInstance(browser).closeWaitingDialog();
    }
}
