package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

    // There is a known issue for now:
    // Missing input validation before upload csv file (e.g empty field name...)
    @Test(dependsOnMethods = {"createProject"})
    public void checkNoHeaderCsvFile() {
        initDataUploadPage();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);

        /* * Todo: - Try to click on confirmation button on preview page - Check that file is not
         * uploaded (No progress is shown) - An error message will be shown - Fill in field names
         * with correct values - Check that the file is uploaded well
         */
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

        assertThat("Dataset with name '" + datasetName + "' wasn't found in datasets list.", datasetsListPage
                .getMyDatasetsTable().getDatasetNames(), hasItem(datasetName));
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

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        CsvFile secondFileUpload = CsvFile.PAYROLL_CHECK_SYNC_DATA;
        checkCsvUpload(secondFileUpload, this::uploadCsv, true);
        getNewDataset(secondFileUpload);

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

            waitForFragmentVisible(fileUploadDialog).clickCancelButton();
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

        assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(firstDatasetName));
        waitForDatasetStatus(firstDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", firstDatasetName), getClass());

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(fileToUpload);

        assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(secondDatasetName));
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadAfterDeleteDataset() {
        CsvFile fileToUpload = CsvFile.PAYROLL_UPLOAD_AFTER_DELETE_DATASET;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String firstDatasetName = getNewDataset(fileToUpload);

        assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(firstDatasetName));
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

        assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(secondDatasetName));
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
    }
}
