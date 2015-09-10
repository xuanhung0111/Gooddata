package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadProgressDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.google.common.base.Predicate;

import java.util.function.Consumer;

public class CsvUploaderTest extends AbstractMSFTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/projects/%s/datasets";
    private static final String CSV_DATASET_NAME = "Csv Payroll";
    private static final String CSV_FILE_NAME = "payroll.csv";
    /** This csv file has incorrect column count (one more than expected) on the line number 2. */
    private static final String BAD_CSV_FILE_NAME = "payroll.bad.csv";
    private static final String BAD_CSV_DATASET_NAME = "Csv Payroll Bad";

    private static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    private static final String DATA_PAGE_NAME = "data-page";
    private static final String DATASET_DETAIL_PAGE_NAME = "dataset-detail";
    private static final String DATA_PREVIEW_PAGE = "data-preview";
    private static final String DELETE_DATASET_DIALOG_NAME = "delete-dataset-dialog";

    private static final String SUCCESSFUL_STATUS_MESSAGE = "Data uploaded successfully";

    @FindBy(className = "s-datasets-list")
    private DatasetsListPage datasetsListPage;

    @FindBy(className = "s-upload-dialog")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    private DataPreviewPage dataPreviewPage;

    @FindBy(className = "s-dataset-detail")
    private DatasetDetailPage csvDatasetDetailPage;

    @FindBy(className = "s-progress-dialog")
    private FileUploadProgressDialog fileUploadProgressDialog;

    @FindBy(className = "s-dataset-delete-dialog")
    private DatasetDeleteDialog datasetDeleteDialog;

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
        checkCsvUpload(CSV_FILE_NAME, this::uploadCsv, true);


        assertThat("Dataset with name '" + CSV_DATASET_NAME + "' wasn't found in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                hasItem(CSV_DATASET_NAME));

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", CSV_DATASET_NAME), getClass());

        waitForDatasetStatus(CSV_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", CSV_DATASET_NAME), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        initDataUploadPage();
        datasetsListPage.clickDatasetDetailButton();

        waitForFragmentVisible(csvDatasetDetailPage);

        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        assertThat(csvDatasetDetailPage.getDatasetName(), is(CSV_DATASET_NAME));
        assertThat(csvDatasetDetailPage.getColumnNames(), containsInAnyOrder("Lastname", "Firstname", "Education", "Position",
                "Department", "State", "County", "Paydate", "Amount"));

        csvDatasetDetailPage.clickBackButton();

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"checkCsvDatasetDetail"})
    public void checkDeleteCsvDataset() throws Exception {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable().getDatasetDeleteButton(CSV_DATASET_NAME).click();

        takeScreenshot(browser, DELETE_DATASET_DIALOG_NAME, getClass());

        waitForFragmentVisible(datasetDeleteDialog).clickDelete();

        checkForDatasetRemoved(CSV_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, CSV_DATASET_NAME, "dataset-deleted"), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvBadFormat() throws Exception {
        checkCsvUpload(BAD_CSV_FILE_NAME, this::uploadBadCsv, false);

        assertThat("Dataset with name '" + BAD_CSV_DATASET_NAME + "' should not be in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                not(hasItem(BAD_CSV_DATASET_NAME)));
    }

    private void checkCsvUpload(String csvFileName,
                                Consumer<String> uploadCsvFunction,
                                boolean newDatasetExpected) throws JSONException {
        initDataUploadPage();

        final int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        uploadCsvFunction.accept(csvFileName);

        waitForExpectedDatasetsCount(newDatasetExpected ? datasetCountBeforeUpload + 1 : datasetCountBeforeUpload);
    }

    private void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(datasetsListPage);
    }

    private void uploadCsv(String csvFileName) {

        uploadFile(csvFileName);

        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, csvFileName), getClass());

        dataPreviewPage.selectFact().triggerIntegration();
    }

    private void uploadBadCsv(String csvFileName) {

        uploadFile(csvFileName);

        // the processing should not go any further but display validation error directly in File Upload Dialog
        assertThat(waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors(),
                contains("csv.validations.structural.incorrect-column-count"));

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", csvFileName), getClass());

        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
    }

    private void uploadFile(String csvFileName) {
        waitForFragmentVisible(datasetsListPage).clickAddDataButton();

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "initial-state", csvFileName), getClass());

        waitForFragmentVisible(fileUploadDialog).pickCsvFile(getCsvFileToUpload(csvFileName));

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "csv-file-picked", csvFileName), getClass());

        fileUploadDialog.clickUploadButton();

        waitForFragmentVisible(fileUploadProgressDialog);

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "upload-in-progress", csvFileName), getClass());
    }

    private void waitForExpectedDatasetsCount(final int expectedDatasetsCount) {
        Predicate<WebDriver> datasetsCountEqualsExpected = input ->
            waitForFragmentVisible(datasetsListPage).getMyDatasetsCount() == expectedDatasetsCount;

        Graphene.waitGui(browser)
                .withMessage("Dataset count <" + waitForFragmentVisible(datasetsListPage).getMyDatasetsCount()
                        + "> in the dataset list doesn't match expected value <" + expectedDatasetsCount + ">.")
                .until(datasetsCountEqualsExpected);
    }

    private void waitForDatasetStatus(final String datasetName, final String expectedStatusMessage) {
        Predicate<WebDriver> datasetHasSuccessfulStatus = input -> {
            final String datasetStatus =
                    waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetStatus(datasetName);
            return isNotEmpty(datasetStatus) && datasetStatus.contains(expectedStatusMessage);
        };

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + datasetName + "' has incorrect status. "
                        + "Expected: '" + expectedStatusMessage
                        + "', but was: '" + datasetsListPage.getMyDatasetsTable().getDatasetStatus(datasetName)
                        + "'.")
                .until(datasetHasSuccessfulStatus);
    }

    private void checkForDatasetRemoved(final String csvDatasetName) {
        Predicate<WebDriver> datasetSuccessfullyRemoved = input ->
                waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRow(csvDatasetName) == null;

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + csvDatasetName + "' has not been removed from the dataset list.")
                .until(datasetSuccessfullyRemoved);
    }

    private String getCsvFileToUpload(String csvFileName) {
        return ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + csvFileName);
    }
}
