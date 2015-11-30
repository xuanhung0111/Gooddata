package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

public class DeleteDatasetTest extends HappyUploadTest {

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkDeleteCsvDataset() throws Exception {
        initDataUploadPage();

        removeDataset(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        checkForDatasetRemoved(PAYROLL_DATASET_NAME);
        removeDatasetFromUploadHistory(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, PAYROLL_DATASET_NAME, "dataset-deleted"), getClass());
    }

    @Test(dependsOnMethods = {"checkDeleteCsvDataset"})
    public void uploadAfterDeleteDataset() {
        checkCsvUpload(PAYROLL_FILE, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(PAYROLL_FILE);

        waitForDatasetName(secondDatasetName);
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
    }

    private void removeDataset(CsvFile csvFile, String datasetName) {
        final int datasetCountBeforeDelete = datasetsListPage.getMyDatasetsCount();

        datasetsListPage.getMyDatasetsTable().getDatasetDeleteButton(datasetName).click();
        takeScreenshot(browser, DELETE_DATASET_DIALOG_NAME, getClass());
        waitForFragmentVisible(datasetDeleteDialog).clickDelete();

        waitForExpectedDatasetsCount(datasetCountBeforeDelete - 1);
    }

    private void removeDatasetFromUploadHistory(CsvFile csvFile, String datasetName) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        assertThat(fileUpload.isPresent(), is(true));
        fileUpload.get().removeDatasetName(datasetName);
    }

    private void checkForDatasetRemoved(final String csvDatasetName) {
        Predicate<WebDriver> datasetSuccessfullyRemoved = input ->
                waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRow(csvDatasetName) == null;

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + csvDatasetName + "' has not been removed from the dataset list.")
                .until(datasetSuccessfullyRemoved);
    }
}
