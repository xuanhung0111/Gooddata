package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadProgressDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.SourceDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.SourcesListPage;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.gooddata.warehouse.Warehouse;
import com.google.common.base.Predicate;

import java.util.function.Consumer;

public class CsvUploaderTest extends AbstractMSFTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/project/%s/sources";
    private static final String CSV_DATASET_NAME = "payroll";
    private static final String CSV_FILE_NAME = CSV_DATASET_NAME + ".csv";
    /** This csv file has incorrect column count (one more than expected) on the line number 2. */
    private static final String BAD_CSV_FILE_NAME = CSV_DATASET_NAME + ".bad.csv";

    private static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    private static final String DATA_PAGE_NAME = "data-page";
    private static final String SOURCE_DETAIL_PAGE_NAME = "source-detail";
    private static final String DATA_PREVIEW_PAGE = "data-preview";

    private static final String SUCCESSFUL_STATUS_MESSAGE = "Data uploaded successfully";

    private AdsHelper adsHelper;

    private Warehouse ads;

    @FindBy(className = "s-sources-list")
    private SourcesListPage sourcesListPage;

    @FindBy(className = "s-upload-dialog")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    private DataPreviewPage dataPreviewPage;

    @FindBy(className = "s-source-detail")
    private SourceDetailPage sourceDetailPage;

    @FindBy(className = "s-progress-dialog")
    private FileUploadProgressDialog fileUploadProgressDialog;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
        adsHelper = new AdsHelper(getGoodDataClient(), getRestApiClient());
        ads = adsHelper.createAds("CSV Uploader Test ADS", dssAuthorizationToken);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownOutputStage() {
        adsHelper.removeAds(ads);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        sourcesListPage.waitForHeaderVisible();
        sourcesListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        sourcesListPage.waitForEmptyStateLoaded();

        takeScreenshot(browser, DATA_PAGE_NAME + "-empty", getClass());

        System.out.println("Empty state message: " + sourcesListPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"checkEmptyState"})
    public void checkCsvUploadHappyPath() throws Exception {
        adsHelper.associateAdsWithProject(ads, testParams.getProjectId());

        checkCsvUpload(CSV_FILE_NAME, this::uploadCsv, true);

        final String sourceName = removeExtension(CSV_FILE_NAME);

        assertThat("Source with name '" + sourceName + "' wasn't found in sources list.",
                sourcesListPage.getMySourcesTable().getSourceNames(),
                hasItem(sourceName));

        takeScreenshot(browser, DATA_PAGE_NAME + "-uploading-dataset-" + sourceName, getClass());

        waitForSourceUploaded(sourceName);

        takeScreenshot(browser, DATA_PAGE_NAME + "-dataset-uploaded-" + sourceName, getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        initDataUploadPage();
        sourcesListPage.clickSourceDetailButton();

        waitForFragmentVisible(sourceDetailPage);

        takeScreenshot(browser, SOURCE_DETAIL_PAGE_NAME, getClass());

        assertThat(sourceDetailPage.getSourceName(), is(CSV_DATASET_NAME));
        assertThat(sourceDetailPage.getColumnNames(), containsInAnyOrder("Lastname","Firstname","Education","Position",
                "Department","State","County","Paydate","Amount"));

        sourceDetailPage.clickBackButton();

        waitForFragmentVisible(sourcesListPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvBadFormat() throws Exception {
        checkCsvUpload(BAD_CSV_FILE_NAME, this::uploadBadCsv, false);

        final String sourceName = removeExtension(BAD_CSV_FILE_NAME);
        assertThat("Source with name '" + sourceName + "' should not be in sources list.",
                sourcesListPage.getMySourcesTable().getSourceNames(),
                not(hasItem(sourceName)));
    }

    private void checkCsvUpload(String csvFileName,
                                Consumer<String> uploadCsvFunction,
                                boolean newDatasetExpected) throws JSONException {
        initDataUploadPage();

        final int datasetCountBeforeUpload = sourcesListPage.getMySourcesCount();

        uploadCsvFunction.accept(csvFileName);

        waitForExpectedSourcesCount(newDatasetExpected ? datasetCountBeforeUpload + 1 : datasetCountBeforeUpload);
    }

    private void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(sourcesListPage);
    }

    private void uploadCsv(String csvFileName) {

        uploadFile(csvFileName);

        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, DATA_PREVIEW_PAGE + "-" + csvFileName, getClass());

        dataPreviewPage.selectFact().triggerIntegration();
    }

    private void uploadBadCsv(String csvFileName) {

        uploadFile(csvFileName);

        // the processing should not go any further but display validation error directly in File Upload Dialog
        assertThat(waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors(),
                contains("csv.validations.structural.incorrect-column-count"));

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-validation-errors-" + csvFileName, getClass());

        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
    }

    private void uploadFile(String csvFileName) {
        waitForFragmentVisible(sourcesListPage).clickAddDataButton();

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-initial-state-" + csvFileName, getClass());

        waitForFragmentVisible(fileUploadDialog).pickCsvFile(getCsvFileToUpload(csvFileName));

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-csv-file-picked-" + csvFileName, getClass());

        fileUploadDialog.clickUploadButton();

        waitForFragmentVisible(fileUploadProgressDialog);

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-upload-in-progress-" + csvFileName, getClass());
    }

    private void waitForExpectedSourcesCount(int expectedSourcesCount) {
        Predicate<WebDriver> sourcesCountEqualsExpected = input ->
            waitForFragmentVisible(sourcesListPage).getMySourcesCount() == expectedSourcesCount;

        Graphene.waitGui(browser).until(sourcesCountEqualsExpected);
    }

    private void waitForSourceUploaded(final String sourceName) {
        Predicate<WebDriver> sourceHasSuccessfulStatus = input -> {
            final String sourceStatus = sourcesListPage.getMySourcesTable().getSourceStatus(sourceName);
            return isNotEmpty(sourceStatus) && sourceStatus.contains(SUCCESSFUL_STATUS_MESSAGE);
        };

        Graphene.waitGui(browser).until(sourceHasSuccessfulStatus);
    }

    private String getCsvFileToUpload(String csvFileName) {
        return ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + csvFileName);
    }
}
