package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.*;

import java.util.List;

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

        waitForDatasetName(PAYROLL_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", PAYROLL_DATASET_NAME), getClass());

        waitForDatasetStatus(PAYROLL_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE_REGEX);

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

    @Test(dependsOnMethods = {"checkCsvRefreshFromDetail"})
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

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkNoFactAndNumericColumnNameCsvConfig() {
        initDataUploadPage();
        uploadFile(PAYROLL_FILE);
        waitForFragmentVisible(dataPreviewPage);

        final String factColumnName = "Amount";

        dataPreviewPage.getDataPreviewTable().changeColumnType(factColumnName, DataPreviewTable.ColumnType.ATTRIBUTE);
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Mark at least one column as measure. Only files with at least one numerical column are supported."));
        dataPreviewPage.getDataPreviewTable().changeColumnType(factColumnName, DataPreviewTable.ColumnType.FACT);

        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click(); // select data row as header
        dataPreviewPage.triggerIntegration();                                    // confirm header row
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fix the errors in column names"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDuplicateColumnNames() {

        String columnName = "the same";

        initDataUploadPage();
        uploadFile(PAYROLL_FILE);
        waitForFragmentVisible(dataPreviewPage);

        // set up the same names
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        dataPreviewTable.changeColumnName(0, columnName);
        dataPreviewTable.changeColumnName(1, columnName);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "columnNameValidationErrors"), getClass());

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fix the errors in column names"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when columns have the same names");

        // fix it by editing the first column
        dataPreviewTable.changeColumnName(0, RandomStringUtils.randomAlphabetic(20));

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "columnNamesValid"), getClass());

        assertFalse(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be enabled");
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromList() {
        initDataUploadPage();
        String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();
        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();

        refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, true);

        waitForDatasetStatus(CsvFile.PAYROLL.getDatasetNameOfFirstUpload(), SUCCESSFUL_STATUS_MESSAGE_REGEX);
    }

    @Test(dependsOnMethods = {"checkCsvDatasetDetail"})
    public void checkCsvRefreshFromDetail() {
        initDataUploadPage();
        String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();

        waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();

        refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, true);

        waitForFragmentVisible(csvDatasetDetailPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshWithIncorrectMetadata() {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable()
            .getDatasetRefreshButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        doUploadFromDialog(CsvFile.PAYROLL_REFRESH_BAD);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors, 
                hasItems(String.format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", CsvFile.PAYROLL_REFRESH_BAD.getFileName())));
        assertEquals(fileUploadDialog.getLinkInBackendValidationError(),
                String.format(DATASET_LINK, testParams.getHost(), testParams.getProjectId(), 
                        getDatasetId(CsvFile.PAYROLL.getDatasetNameOfFirstUpload())));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCancelCsvRefresh() {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable()
            .getDatasetRefreshButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        doUploadFromDialog(CsvFile.PAYROLL_REFRESH);
        
        waitForFragmentVisible(dataPreviewPage).cancelTriggerIntegration();
        
        waitForFragmentVisible(datasetsListPage);
        
        datasetsListPage.getMyDatasetsTable()
            .getDatasetDetailButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();

        doUploadFromDialog(CsvFile.PAYROLL_REFRESH);
        
        waitForFragmentVisible(dataPreviewPage).cancelTriggerIntegration();
        
        waitForFragmentVisible(csvDatasetDetailPage);
    }

    @Test
    public void navigateToProjectsPageWhenInvalidProjectId() throws Exception {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI_TEMPLATE, "nonExistingProjectIdL123321"));

        waitForStringInUrl("/projects.html#status=notAuthorized");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void showErrorOnUploadsPageWhenInvalidDatasetId() throws Exception {
        openUrl(String.format(CSV_DATASET_DETAIL_PAGE_URI_TEMPLATE, testParams.getProjectId(), "nonExistingDataset"));

        final String errorMessage = waitForFragmentVisible(csvDatasetMessageBar).waitForErrorMessageBar().getText();

        takeScreenshot(browser, "invalid-dataset-id", getClass());

        assertThat(errorMessage, containsString("The dataset you are looking for no longer exists."));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void showUploadsPageWhenBadUrlAfterExistingProjectId() throws Exception {
        openUrl(String.format(CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/this/is/bad/url", testParams.getProjectId()));

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redirectToErrorPageWhenInsufficientAccessRights() throws Exception {
        addViewerUserToProject();

        try {
            logout();
            signIn(true, UserRoles.VIEWER);

            openUrl(String.format(DATA_UPLOAD_PAGE_URI_TEMPLATE, testParams.getProjectId()));

            final String insufficientAccessHeader = waitForFragmentVisible(insufficientAccessRightsPage).getHeader1();

            takeScreenshot(browser, "insufficient-access-rights", getClass());

            assertThat(insufficientAccessHeader, containsString("you do not have access to the Load section."));
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }
}
