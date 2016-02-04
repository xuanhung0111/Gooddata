package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;

import java.io.IOException;

import org.testng.annotations.Test;

public class HappyUploadTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvUploadHappyPath() throws IOException {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);

        final String dataset = getNewDataset(PAYROLL);
        waitForDatasetName(dataset);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", dataset), getClass());
        waitForDatasetStatus(dataset, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", dataset), getClass());
    }
}
