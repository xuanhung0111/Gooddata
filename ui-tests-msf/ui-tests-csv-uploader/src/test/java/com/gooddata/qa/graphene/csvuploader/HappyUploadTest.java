package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;

import org.testng.annotations.Test;

public class HappyUploadTest extends AbstractCsvUploaderTest {

    protected static String PAYROLL_DATASET_NAME = PAYROLL.getDatasetNameOfFirstUpload();

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvUploadHappyPath() throws Exception {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        PAYROLL_DATASET_NAME = getNewDataset(PAYROLL);
        waitForDatasetName(PAYROLL_DATASET_NAME);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", PAYROLL_DATASET_NAME), getClass());
        waitForDatasetStatus(PAYROLL_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", PAYROLL_DATASET_NAME), getClass());
    }
}
