package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.common.Sleeper;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.SourcesListPage;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.gooddata.warehouse.Warehouse;

public class CsvUploaderTest extends AbstractMSFTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/project/%s/sources";
    private static final String PAYROLL_CSV_FILE_NAME = "payroll.csv";

    private AdsHelper adsHelper;

    private Warehouse ads;

    @FindBy(className = "s-sources-list")
    private SourcesListPage sourcesListPage;

    @FindBy(className = "s-upload-dialog")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    private DataPreviewPage dataPreviewPage;

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
        System.out.println("Empty state message: " + sourcesListPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"checkEmptyState"})
    public void checkCsvUploadHappyPath() throws Exception {
        setUpOutputStage();

        initDataUploadPage();

        final int datasetCountBeforeUpload = sourcesListPage.getMySourcesCount();

        uploadCsv(getCsvFileToUpload());

        assertEquals(sourcesListPage.getMySourcesCount(), datasetCountBeforeUpload + 1);
        assertNotNull(sourcesListPage.getMySourcesTable().getSource(PAYROLL_CSV_FILE_NAME));
    }

    private void setUpOutputStage() throws JSONException {
        adsHelper.associateAdsWithProject(ads, testParams.getProjectId());
    }

    private void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(sourcesListPage);
    }

    private void uploadCsv(String csvFileToUpload) {

        sourcesListPage.clickAddDataButton();

        waitForFragmentVisible(fileUploadDialog);

        fileUploadDialog.pickCsvFile(csvFileToUpload);
        fileUploadDialog.clickUploadButton();

        waitForFragmentVisible(dataPreviewPage);

        dataPreviewPage.selectFact();
        dataPreviewPage.triggerIntegration();

        //waiting for refresh data from backend
        Sleeper.sleepTightInSeconds(5);

        waitForFragmentVisible(sourcesListPage);
    }

    private String getCsvFileToUpload() {
        return ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + PAYROLL_CSV_FILE_NAME);
    }
}
