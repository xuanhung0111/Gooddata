package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;

import org.testng.annotations.Test;

public class HappyUploadTest extends AbstractCsvUploaderTest {

    protected static final CsvFile PAYROLL_FILE = CsvFile.PAYROLL;
    protected static String PAYROLL_DATASET_NAME = PAYROLL_FILE.getDatasetNameOfFirstUpload();

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvUploadHappyPath() throws Exception {
        checkCsvUpload(PAYROLL_FILE, this::uploadCsv, true);
        PAYROLL_DATASET_NAME = getNewDataset(PAYROLL_FILE);

        waitForDatasetName(PAYROLL_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", PAYROLL_DATASET_NAME), getClass());

        waitForDatasetStatus(PAYROLL_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", PAYROLL_DATASET_NAME), getClass());
    }
}
