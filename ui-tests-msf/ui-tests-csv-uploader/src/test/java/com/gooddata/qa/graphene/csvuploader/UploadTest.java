package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.json.JSONException;
import org.testng.annotations.Test;

public class UploadTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
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
    public void checkOnlyUploadedDatasetSync() throws JSONException {
        CsvFile fileToUploadFirst = CsvFile.PAYROLL;
        checkCsvUpload(fileToUploadFirst, this::uploadCsv, true);
        String firstDatasetUploadName = getNewDataset(fileToUploadFirst);
        waitForDatasetName(firstDatasetUploadName);
        waitForDatasetStatus(firstDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        CsvFile secondFileUpload = CsvFile.PAYROLL;
        checkCsvUpload(secondFileUpload, this::uploadCsv, true);
        String secondDatasetUploadName = getNewDataset(secondFileUpload);
        waitForDatasetName(secondDatasetUploadName);
        waitForDatasetStatus(secondDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        assertThat(waitForFragmentVisible(datasetDetailPage).getLatestUploadDate(), is(latestUploadDate));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadOneCsvFileMultipleTime() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

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

    @Test(dependsOnMethods = {"createProject"})
    public void checkBasicUploadProgress() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", fileToUpload.getFileName()), getClass());

        assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                is(String.format(SUCCESSFUL_DATA_MESSAGE, datasetName)));
        takeScreenshot(browser, toScreenshotName("Successful-upload-data-to-dataset", datasetName), getClass());
    }
}
