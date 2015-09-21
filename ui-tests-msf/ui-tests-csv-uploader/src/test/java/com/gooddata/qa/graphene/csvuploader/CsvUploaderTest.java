package com.gooddata.qa.graphene.csvuploader;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsvUploaderTest extends AbstractCsvUploaderTest {

    private static final CsvFile PAYROLL_FILE = CsvFile.PAYROLL;
    private static String PAYROLL_DATASET_NAME = PAYROLL_FILE.getDatasetNameOfFirstUpload();

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        datasetsListPage.waitForHeaderVisible();
        datasetsListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        datasetsListPage.waitForEmptyStateLoaded();

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "empty"), getClass());

        System.out.println("Empty state message: " + datasetsListPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"checkEmptyState"})
    public void checkCsvUploadHappyPath() throws Exception {
        checkCsvUpload(PAYROLL_FILE, this::uploadCsv, true);
        PAYROLL_DATASET_NAME = getNewDataset(PAYROLL_FILE);

        assertThat("Dataset with name '" + PAYROLL_DATASET_NAME + "' wasn't found in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                hasItem(PAYROLL_DATASET_NAME));

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", PAYROLL_DATASET_NAME), getClass());

        waitForDatasetStatus(PAYROLL_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", PAYROLL_DATASET_NAME), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        initDataUploadPage();
        datasetsListPage.clickDatasetDetailButton(PAYROLL_DATASET_NAME);

        waitForFragmentVisible(csvDatasetDetailPage);

        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        csvDatasetDetailPage.clickBackButton();

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"checkCsvDatasetDetail"})
    public void checkDeleteCsvDataset() throws Exception {
        initDataUploadPage();

        removeDataset(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        checkForDatasetRemoved(PAYROLL_DATASET_NAME);
        removeDatasetFromUploadHistory(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, PAYROLL_DATASET_NAME, "dataset-deleted"), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvBadFormat() throws Exception {
        CsvFile fileToUpload = CsvFile.BAD_STRUCTURE;
        checkCsvUpload(fileToUpload, this::uploadBadCsv, false);

        String datasetName = fileToUpload.getDatasetNameOfFirstUpload();
        assertThat("Dataset with name '" + datasetName + "' should not be in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                not(hasItem(datasetName)));
    }
}
