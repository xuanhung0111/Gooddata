package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class ExtendedCsvUploaderTest extends AbstractCsvUploaderTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNoHeaderCsvFile() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);
        List<String> errors = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().getColumnErrors();
        takeScreenshot(browser,
                toScreenshotName(DATA_PAGE_NAME, "empty-column-name-in", fileToUpload.getFileName()), getClass());
        assertThat(errors.size(), is(PAYROLL_COLUMN_NAMES.size()));
        errors.stream().allMatch(error -> error.equals("Column name can't be empty"));

        waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().setColumnsName(PAYROLL_COLUMN_NAMES);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "set-column-names", fileToUpload.getFileName()),
                getClass());
        assertThat(waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().getColumnErrors(), empty());
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

    @Test(dependsOnMethods = {"createProject"})
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

    @Test(dependsOnMethods = {"checkFieldTypeDetection"})
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

    @Test(dependsOnMethods = {"createProject"})
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

    @Test(dependsOnMethods = {"createProject"})
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

    @Test(dependsOnMethods = {"createProject"})
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
                    is("The selected file is larger than 1 GB. Select a different file."));
            takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", csvFileName),
                    getClass());

            checkButtonOnErrorUploadDialog();
        } finally {
            file.setLength(0);
            file.close();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
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

    @Test(dependsOnMethods = {"createProject"})
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

    @Test(dependsOnMethods = {"createProject"})
    public void uploadCsvFileWithoutFact() {
        CsvFile fileToUpload = CsvFile.WITHOUT_FACT;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems(
                        "Failed to upload the " + fileToUpload.getFileName() + " file.",
                        "We haven’t found at least one numeric value (measurable data) on first 20 rows. "
                                + "We skipped further processing as it seems the file doesn’t contain numeric data for analysis. "
                                + "Upload a different CSV file."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadCsvFileWithIncorrectHeader() {
        CsvFile fileToUpload = CsvFile.BAD_STRUCTURE;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems("Failed to upload the " + fileToUpload.getFileName() + " file.",
                        "Row 2 contains more columns than the header row."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadCsvFileWithMultipleError() {
        CsvFile fileToUpload = CsvFile.CRAZY_DATA;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(
                backendValidationErrors,
                hasItems("Failed to upload the " + fileToUpload.getFileName() + " file.",
                        "There are 5 rows containing less columns than the header row: 44-48.",
                        "There are 5 rows without at least one numeric value (measurable data): 44-48. "
                                + "Each row must contain numeric data for analysis."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadCsvFileWithTooManyColumns() {
        CsvFile fileToUpload = CsvFile.TOO_MANY_COLUMNS;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems("Failed to upload the " + fileToUpload.getFileName() + " file.",
                        "Load failed and stopped on row 1. Some cell value at this row exceeds 255 characters, "
                                + "or the number of columns exceeds 250."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadCsvFileWithTooLongField() {
        CsvFile fileToUpload = CsvFile.TOO_LONG_FIELD;

        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems("Failed to upload the " + fileToUpload.getFileName() + " file.",
                        "Load failed and stopped on row 2. Some cell value at this row exceeds 255 characters, "
                                + "or the number of columns exceeds 250."));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    private void checkButtonOnErrorUploadDialog() {
        assertThat(waitForFragmentVisible(fileUploadDialog).isUploadButtonDisabled(), is(true));
        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
        waitForFragmentNotVisible(fileUploadDialog);
    }
}
