package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;

public class RefreshTest extends HappyUploadTest {

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromList() {
        initDataUploadPage();
        String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();
        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();

        refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, true);

        waitForDatasetStatus(CsvFile.PAYROLL.getDatasetNameOfFirstUpload(), SUCCESSFUL_STATUS_MESSAGE_REGEX);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromDetail() {
        initDataUploadPage();
        String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();

        waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();

        refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, true);

        waitForFragmentVisible(csvDatasetDetailPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkSetHeaderHiddenWhenUpdate() {
        initDataUploadPage();
        String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();

        waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        doUploadFromDialog(CsvFile.PAYROLL_REFRESH);
        waitForFragmentVisible(dataPreviewPage);
        assertThat(dataPreviewPage.isSetHeaderButtonHidden(), is(true));
        takeScreenshot(browser, toScreenshotName("set-header-button-hidden", CsvFile.PAYROLL_REFRESH.getFileName())
                , getClass());
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

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"}, alwaysRun = true)
    public void addOtherAdminToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getAdminUser(), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"addOtherAdminToProject"})
    public void checkAdminUpdateDataOfOthers() throws JSONException {
        try {
            String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();
            log.info("datasetName by owner: " + datasetName);
            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();
            datasetsListPage.getOthersDatasetsTable().getDatasetRefreshButton(datasetName).click();
            refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, false);

            assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    is(String.format(SUCCESSFUL_DATA_MESSAGE, datasetName)));
            datasetsListPage.getOthersDatasetsTable().getDatasetDetailButton(datasetName).click();
            waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();
            refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, false);
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }
}
