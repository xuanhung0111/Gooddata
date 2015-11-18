package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.jboss.arquillian.graphene.Graphene;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExtendedCsvUploaderTest extends AbstractCsvUploaderTest {

    private static final String EMPTY_COLUMN_NAME_ERROR = "Fill in the column name.";
    private static final String NUMBERIC_COLUMN_NAME_ERROR = "Invalid column name. Names must begin with an alphabetic character.";
    public static final String TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR = "Row %d contains a value that exceeds the limit of 255 characters, or has more than the limit of 250 columns.";

    private static final long PAYROLL_FILE_SIZE_MINIMUM = 476L;
    private static final int PAYROLL_FILE_DEFAULT_ROW_COUNT = 3876;
    private static final int PAYROLL_FILE_DEFAULT_COLUMN_COUNT = 9;
    private String downloadFolder;

    @FindBy(css = ".bubble-negative .content")
    private WebElement errorBubbleMessage;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
        downloadFolder = testParams.loadProperty("browserDownloadFolder") + testParams.getFolderSeparator();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkNoHeaderCsvFile() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);
        takeScreenshot(browser,
                toScreenshotName(DATA_PAGE_NAME, "empty-column-name-in", fileToUpload.getFileName()), getClass());
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fix the errors in column names"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be disabled when column names are invalid");

        waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().setColumnsName(PAYROLL_COLUMN_NAMES);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "set-column-names", fileToUpload.getFileName()),
                getClass());
        assertFalse(waitForFragmentVisible(dataPreviewPage).isIntegrationButtonDisabled(), "Add data button should be enabled");
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES);

        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkFieldTypeDetection() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, DATA_PAGE_NAME + "-uploading-dataset-" + datasetName, getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, DATA_PAGE_NAME + "-dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(fileToUpload, datasetName);

        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"checkFieldTypeDetection"}, groups = {"myData"})
    public void checkUploadedDatasetAtManagePage() {
        CsvFile fileToUpload = CsvFile.PAYROLL;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        initManagePage();
        waitForFragmentVisible(datasetsTable).selectObject(datasetName);
        waitForFragmentVisible(datasetDetailPage);
        assertThat(
                datasetDetailPage.getAttributes(),
                containsInAnyOrder("Lastname", "Firstname", "Education", "Position", "Department", "State",
                        "County", String.format("Records of %s", datasetName)));
        assertThat(datasetDetailPage.getFacts(), containsInAnyOrder("Amount"));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void changeColumnType() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL_CHANGE_TYPE;
        uploadFile(fileToUpload);

        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        String columnNameToChangeType = "Paydate";
        dataPreviewTable.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        List<String> changedTypes = fileToUpload.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        checkDataPreview(fileToUpload.getColumnNames(), changedTypes);
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "-changed-type-dataset-preview-", fileToUpload.getFileName()),
                getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "-changed-type-dataset-uploaded-", datasetName),
                getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, fileToUpload.getColumnNames(), changedTypes);

        waitForFragmentVisible(csvDatasetDetailPage).clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkOnlyUploadedDatasetSync() throws JSONException {
        CsvFile fileToUploadFirst = CsvFile.PAYROLL_CHECK_NOT_SYNC_DATA;
        checkCsvUpload(fileToUploadFirst, this::uploadCsv, true);
        String firstDatasetUploadName = getNewDataset(fileToUploadFirst);
        waitForDatasetName(firstDatasetUploadName);
        waitForDatasetStatus(firstDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        CsvFile secondFileUpload = CsvFile.PAYROLL_CHECK_SYNC_DATA;
        checkCsvUpload(secondFileUpload, this::uploadCsv, true);
        String secondDatasetUploadName = getNewDataset(secondFileUpload);
        waitForDatasetName(secondDatasetUploadName);
        waitForDatasetStatus(secondDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        assertThat(waitForFragmentVisible(datasetDetailPage).getLatestUploadDate(), is(latestUploadDate));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadTooLargeCsvFile() throws IOException {
        CsvFile fileToUpload = CsvFile.TOO_LARGE_FILE;
        String csvFileName = fileToUpload.getFileName();
        File tooLargeFile = new File(fileToUpload.getCsvFileToUpload());
        RandomAccessFile file = new RandomAccessFile(tooLargeFile, "rw");
        try {
            file.setLength(1100 * 1024 * 1024);

            initDataUploadPage();
            waitForFragmentVisible(datasetsListPage).clickAddDataButton();
            waitForFragmentVisible(fileUploadDialog).pickCsvFile(fileToUpload.getCsvFileToUpload());

            String validationErrorMessage = waitForFragmentVisible(fileUploadDialog).getValidationErrorMessage();
            assertThat(validationErrorMessage,
                    is("The selected file is larger than 1 GB. Try uploading a subset of the data from the file."));
            takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", csvFileName),
                    getClass());

            checkButtonOnErrorUploadDialog();
        } finally {
            file.setLength(0);
            file.close();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadOneCsvFileMultipleTime() {
        CsvFile fileToUpload = CsvFile.PAYROLL_UPLOAD_MULTIPLE_TIMES;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String firstDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(firstDatasetName);
        waitForDatasetStatus(firstDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", firstDatasetName), getClass());

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(secondDatasetName);
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadAfterDeleteDataset() {
        CsvFile fileToUpload = CsvFile.PAYROLL_UPLOAD_AFTER_DELETE_DATASET;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String firstDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(firstDatasetName);
        waitForDatasetStatus(firstDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", firstDatasetName), getClass());

        // Delete dataset
        initDataUploadPage();
        removeDataset(fileToUpload, firstDatasetName);

        checkForDatasetRemoved(firstDatasetName);
        removeDatasetFromUploadHistory(fileToUpload, firstDatasetName);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, firstDatasetName, "dataset-deleted"), getClass());

        // Upload the same csv file again
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(secondDatasetName);
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadCsvFileWithoutFact() {
        CsvFile fileToUpload = CsvFile.WITHOUT_FACT;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems(
                        createErrorFailedToUploadFile(fileToUpload),
                        "The file seems to contain no numerical data for analysis on first 20 rows. " +
                                "Try removing comments and multi-row headers from the beginning of the file. " +
                                "Only files with at least one numerical column are supported."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadCsvFileWithIncorrectHeader() {
        CsvFile fileToUpload = CsvFile.BAD_STRUCTURE;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems(createErrorFailedToUploadFile(fileToUpload),
                        "Row 2 contains more columns than the header row."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadCsvFileWithMultipleError() {
        CsvFile fileToUpload = CsvFile.CRAZY_DATA;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems(createErrorFailedToUploadFile(fileToUpload),
                        "There are 5 rows containing less columns than the header row: 44-48.",
                        "There are 5 rows without at least one numerical value: 44-48. "
                                + "Each row must contain numerical data for analysis."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadCsvFileWithTooManyColumns() {
        CsvFile fileToUpload = CsvFile.TOO_MANY_COLUMNS;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(createErrorFailedToUploadFile(fileToUpload), String.format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 1)));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void uploadCsvFileWithTooLongField() {
        CsvFile fileToUpload = CsvFile.TOO_LONG_FIELD;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(createErrorFailedToUploadFile(fileToUpload), String.format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 2)));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkDatasetDetailPage() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, toScreenshotName(DATASET_DETAIL_PAGE_NAME, datasetName), getClass());

        checkCsvDatasetDetail(fileToUpload, datasetName);

        waitForFragmentVisible(csvDatasetDetailPage).downloadTheLatestCsvFileUpload();
        final File downloadedCsvFile = new File(downloadFolder + fileToUpload.getFileName());
        Predicate<WebDriver> fileDownloadComplete =
                browser -> downloadedCsvFile.length() > PAYROLL_FILE_SIZE_MINIMUM;
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(10, TimeUnit.SECONDS)
                .until(fileDownloadComplete);
        System.out.println("Download file size: " + downloadedCsvFile.length());
        System.out.println("Download file path: " + downloadedCsvFile.getPath());
        System.out.println("Download file name: " + downloadedCsvFile.getName());

        String createdDateTime =
                csvDatasetDetailPage.getCreatedDateTime().replaceAll("Created by.*on\\s+", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
        try {
            formatter.parse(createdDateTime);
        } catch (DateTimeParseException e) {
            Assert.fail("Incorrect format of created date time: " + createdDateTime);
        }
    }

    @Test(dependsOnMethods = {"createProject","enableAnalyticalDesigner"}, groups = {"myData"})
    public void checkDatasetAnalyzeLink() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        String adReportLink =
                String.format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                        getDatasetId(datasetName));
        assertThat(datasetsListPage.getDatasetAnalyzeLink(datasetName), is(adReportLink));
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        assertThat(waitForFragmentVisible(csvDatasetDetailPage).getDatasetAnalyzeLink(), is(adReportLink));
        takeScreenshot(browser, toScreenshotName(DATASET_DETAIL_PAGE_NAME, datasetName), getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkBasicUploadProgress() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", fileToUpload.getFileName()), getClass());

        assertThat(datasetsListPage.waitForSuccessMessageBar().getText(),
                is(String.format("Data has been loaded successfully to \"%s\". Start analyzing!", datasetName)));
        takeScreenshot(browser, toScreenshotName("Successful-upload-data-to-dataset", datasetName), getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkCsvFileWithMultipleHeaderRows() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.MULTIPLE_COLUMN_NAME_ROWS;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage).selectHeader();

        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        List<WebElement> nonSelectablePreHeaderRows = dataPreviewTable.getNonSelectablePreHeaderRows();
        assertThat(nonSelectablePreHeaderRows.size(), is(3));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(0),
                containsInAnyOrder("id1", "name1", "lastname1", "age1", "Amount1"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(1),
                containsInAnyOrder("id2", "name2", "lastname2", "age2", "Amount2"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(2),
                containsInAnyOrder("id3", "name3", "lastname3", "age3", "Amount3"));
        assertThat(
                dataPreviewPage.getWarningMessage(),
                is("First 3 rows cannot be set as a header. A header must be followed by rows containing at least one number."));
        takeScreenshot(browser, toScreenshotName("set-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        checkDataPreview(fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "dataset-preview", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "changed-type-dataset-uploaded", datasetName),
                getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void setCustomHeader() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewTable.getPreHeaderRows().size(), is(3));

        List<String> customHeaderColumns =
                Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fix the errors in column names"));
        assertTrue(dataPreviewTable.isColumnNameError("2006-03-01"), "Error is not shown!");
        assertTrue(dataPreviewTable.isColumnNameError("10230"), "Error is not shown!");

        dataPreviewTable.getColumnNameInput("2006-03-01").click();
        assertThat(getErrorBubbleMessage(), is(NUMBERIC_COLUMN_NAME_ERROR));
        dataPreviewTable.getColumnNameInput("10230").click();
        assertThat(getErrorBubbleMessage(), is(NUMBERIC_COLUMN_NAME_ERROR));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");

        dataPreviewTable.changeColumnName("2006-03-01", "Paydate");
        dataPreviewTable.changeColumnName("10230", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("2006-03-01"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("10230"), "Amount");
        takeScreenshot(browser, toScreenshotName("custom-header", fileToUpload.getFileName()), getClass());

        checkDataPreview(customHeaderColumns, fileToUpload.getColumnTypes());
        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "saved-custom-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        int numberOfRows = PAYROLL_FILE_DEFAULT_ROW_COUNT - 3; // All rows above header shouldn't be
                                                               // added
        String expectedDatasetStatus =
                String.format(".*%d.*,.*%d.*", numberOfRows, PAYROLL_FILE_DEFAULT_COLUMN_COUNT);
        assertTrue(waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetStatus(datasetName)
                .matches(expectedDatasetStatus), "Incorrect row/colum number!");

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL_COLUMN_TYPES);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void cancelChangeColumnNames() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewTable.getPreHeaderRows().size(), is(3));
        assertThat(dataPreviewTable.getPreHeaderRowCells(0), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(
                dataPreviewTable.getPreHeaderRowCells(1),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-01-01", "10230"));
        assertThat(
                dataPreviewTable.getPreHeaderRowCells(2),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-02-01", "10230"));

        assertThat(
                dataPreviewTable.getHeaderColumns(),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230"));
        takeScreenshot(browser, toScreenshotName("set-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.cancelTriggerIntegration();

        checkDataPreview(fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "cancel-custom-header", fileToUpload.getFileName()),
                getClass());
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(fileToUpload, datasetName);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void setHeaderForNoHeaderCsvFile() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fix the errors in column names"));
        assertTrue(dataPreviewTable.isEmptyColumnNameError(0), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(3), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(6), "Error is not shown!");
        dataPreviewTable.getColumnNameInputs().get(3).click();
        assertThat(getErrorBubbleMessage(), is(EMPTY_COLUMN_NAME_ERROR));

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(2).click();

        List<String> customHeaderColumns =
                Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        dataPreviewTable.changeColumnName("2006-03-01", "Paydate");
        dataPreviewTable.changeColumnName("10230", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("2006-03-01"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("10230"), "Amount");

        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser, toScreenshotName("custom-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL_COLUMN_TYPES);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void setHeaderWhenUpdate() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        doUploadFromDialog(CsvFile.PAYROLL_REFRESH);
        waitForFragmentVisible(dataPreviewPage);
        assertThat(dataPreviewPage.isSetHeaderButtonHidden(), is(true));
        takeScreenshot(browser, toScreenshotName("set-header-button-hidden", fileToUpload.getFileName()),
                getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"myData"})
    public void checkMyDataAndNoDataOfOthers() {
        initDataUploadPage();

        CsvFile fileToUpload = CsvFile.PAYROLL_BY_PROJECT_OWNER;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String myDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(myDatasetName);
        List<String> myDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();
        assertThat(myDatasetNames, hasItem(myDatasetName));
        assertThat(myDatasetNames, contains(myDatasetNames.stream().sorted().toArray()));
        waitForDatasetStatus(myDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", myDatasetName), getClass());
    }

    @Test(dependsOnGroups = {"myData"}, alwaysRun = true)
    public void addOtherAdminToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getAdminUser(), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"addOtherAdminToProject"})
    public void checkNoMyDataButDataOfOthers() throws JSONException {
        try {
            initDataUploadPage();
            List<String> projectOwnerDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();
            datasetsListPage.waitForMyDatasetsEmptyStateLoaded();

            assertThat(datasetsListPage.getOthersDatasetsTable().getNumberOfDatasets(),
                    is(projectOwnerDatasetNames.size()));
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"checkNoMyDataButDataOfOthers"})
    public void checkMyDataAndDataOfOthers() throws JSONException {
        try {
            initDataUploadPage();
            List<String> projectOwnerDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();

            datasetsListPage.waitForMyDatasetsEmptyStateLoaded();

            CsvFile fileToUpload = CsvFile.PAYROLL_BY_OTHER_ADMIN;

            checkCsvUpload(fileToUpload, this::uploadCsv, true);
            String myDatasetName = getNewDataset(fileToUpload);

            waitForDatasetName(myDatasetName);
            assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(myDatasetName));
            waitForDatasetStatus(myDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
            takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", myDatasetName),
                    getClass());

            waitForCollectionIsNotEmpty(datasetsListPage.getOthersDatasetsTable().getRows());
            assertThat(datasetsListPage.getOthersDatasetsTable().getNumberOfDatasets(),
                    is(projectOwnerDatasetNames.size()));
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }
    
    private String getErrorBubbleMessage() {
        return waitForElementVisible(errorBubbleMessage).getText();
    }

    private void checkButtonOnErrorUploadDialog() {
        assertThat(waitForFragmentVisible(fileUploadDialog).isUploadButtonDisabled(), is(true));
        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
        waitForFragmentNotVisible(fileUploadDialog);
    }
}
