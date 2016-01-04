package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.google.common.base.Predicate;

public class DataOfOtherUsersTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkMyDataAndNoDataOfOthers() {
        initDataUploadPage();

        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String myDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(myDatasetName);
        List<String> myDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();
        assertThat(myDatasetNames, hasItem(myDatasetName));
        assertThat(myDatasetNames, contains(myDatasetNames.stream().sorted().toArray()));
        waitForDatasetStatus(myDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", myDatasetName), getClass());
    }

    @Test(dependsOnMethods = {"checkMyDataAndNoDataOfOthers"})
    public void addOtherAdminToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getAdminUser(), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"addOtherAdminToProject"})
    public void checkNoMyDataButDataOfOthers() throws JSONException {
        try {
            initDataUploadPage();
            List<String> projectOwnerDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();
            datasetsListPage.waitForMyDatasetsEmptyStateLoaded();

            assertThat(datasetsListPage.getOthersDatasetsTable().getNumberOfDatasets(),
                    is(projectOwnerDatasetNames.size()));
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"checkNoMyDataButDataOfOthers"})
    public void checkMyDataAndDataOfOthers() throws JSONException {
        try {
            initDataUploadPage();
            List<String> projectOwnerDatasetNames = datasetsListPage.getMyDatasetsTable().getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();

            datasetsListPage.waitForMyDatasetsEmptyStateLoaded();

            CsvFile fileToUpload = CsvFile.PAYROLL;

            checkCsvUpload(fileToUpload, this::uploadCsv, true);
            String myDatasetName = getNewDataset(fileToUpload);

            waitForDatasetName(myDatasetName);
            assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(myDatasetName));
            waitForDatasetStatus(myDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
            takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", myDatasetName),
                    getClass());

            waitForCollectionIsNotEmpty(datasetsListPage.getOthersDatasetsTable().getRows());
            assertThat(datasetsListPage.getOtherDatasetsCount(),
                    is(projectOwnerDatasetNames.size()));
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"checkMyDataAndDataOfOthers"})
    public void checkAdminCanDeleteDatasetOfOthers() throws JSONException {
        try {
            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage();
            String datasetName = CsvFile.PAYROLL.getDatasetNameOfFirstUpload();

            final int datasetCountBeforeDelete = datasetsListPage.getOtherDatasetsCount();

            datasetsListPage.getOthersDatasetsTable().getDatasetDeleteButton(datasetName).click();
            waitForFragmentVisible(datasetDeleteDialog).clickDelete();
            waitForFragmentNotVisible(datasetDeleteDialog);

            assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    is(String.format("\"%s\" was successfully deleted!", datasetName)));
            final int datasetCountAfterDelete = datasetCountBeforeDelete - 1;
            Predicate<WebDriver> datasetsCountEqualsExpected = input ->
                    waitForFragmentVisible(datasetsListPage).getOtherDatasetsCount() == datasetCountAfterDelete;

            Graphene.waitGui(browser)
                    .withMessage("Dataset count <" +
                            waitForFragmentVisible(datasetsListPage).getOtherDatasetsCount()
                            + "> in the dataset list doesn't match expected value <" + datasetCountAfterDelete + ">.")
                    .until(datasetsCountEqualsExpected);
            removeDatasetFromUploadHistory(CsvFile.PAYROLL, datasetName);
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"checkAdminCanDeleteDatasetOfOthers"})
    public void addViewerAndEditorToProject() throws ParseException, IOException, JSONException {
        addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"addViewerAndEditorToProject"})
    public void checkEditorCanManageHisOwnData() throws JSONException {
        try {
            logout();
            signInAtGreyPages(testParams.getEditorUser(), testParams.getEditorPassword());

            initDataUploadPage();
            final int datasetCount = datasetsListPage.getMyDatasetsCount();
            CsvFile fileToUpload = CsvFile.PAYROLL;
            checkCsvUpload(fileToUpload, this::uploadCsv, true);
            String datasetName = getNewDataset(fileToUpload);
            waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
            assertTrue(datasetsListPage.getMyDatasetsTable().isDeleteButtonVisible(datasetName),
                    "Delete button is not shown in editor's dataset");
            assertTrue(datasetsListPage.getMyDatasetsTable().isRefreshButtonVisible(datasetName),
                    "Update button is still shown in editor's dataset");
            assertTrue(datasetsListPage.getMyDatasetsTable().isAnalyzeButtonVisble(datasetName),
                    "Analyze button is not shown in editor's dataset");
            assertTrue(datasetsListPage.getMyDatasetsTable().isDetailButtonVisble(datasetName),
                    "Detail button is not shown in editor's dataset");
            datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
            waitForFragmentVisible(csvDatasetDetailPage);
            assertTrue(csvDatasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is not shown in editor's dataset");
            assertTrue(csvDatasetDetailPage.isRefreshButtonVisible(),
                    "Update button is not shown in editor's dataset");
            assertTrue(csvDatasetDetailPage.isAnalyzeButtonVisible(),
                    "Analyze button is not shown in editor's dataset");

            csvDatasetDetailPage.clickRefreshButton();
            refreshCsv(CsvFile.PAYROLL_REFRESH, datasetName, true);
            waitForFragmentVisible(csvDatasetDetailPage);

            csvDatasetDetailPage.clickBackButton();
            waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetDeleteButton(datasetName).click();
            waitForFragmentVisible(datasetDeleteDialog).clickDelete();
            waitForFragmentNotVisible(datasetDeleteDialog);
            removeDatasetFromUploadHistory(fileToUpload, datasetName);

            assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    is(String.format("\"%s\" was successfully deleted!", datasetName)));
            Predicate<WebDriver> datasetsCountEqualsExpected = input ->
                    waitForFragmentVisible(datasetsListPage).getMyDatasetsCount() == datasetCount;

            Graphene.waitGui(browser)
                    .withMessage("Dataset count <" +
                            waitForFragmentVisible(datasetsListPage).getMyDatasetsCount()
                            + "> in the dataset list doesn't match expected value <" + datasetCount + ">.")
                    .until(datasetsCountEqualsExpected);
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"addViewerAndEditorToProject"})
    public void checEditorCannotEditDataOfOthers() throws JSONException {
        try {
            initDataUploadPage();

            CsvFile fileToUpload = CsvFile.PAYROLL;

            checkCsvUpload(fileToUpload, this::uploadCsv, true);
            String datasetName = getNewDataset(fileToUpload);
            waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

            logout();
            signInAtGreyPages(testParams.getEditorUser(), testParams.getEditorPassword());

            initDataUploadPage();

            assertFalse(datasetsListPage.getOthersDatasetsTable().isDeleteButtonVisible(datasetName),
                    "Delete button is still shown in other dataset");
            assertFalse(datasetsListPage.getOthersDatasetsTable().isRefreshButtonVisible(datasetName),
                    "Update button is still shown in other dataset");
            assertTrue(datasetsListPage.getOthersDatasetsTable().isAnalyzeButtonVisble(datasetName),
                    "Analyze button is not shown in other dataset");
            assertTrue(datasetsListPage.getOthersDatasetsTable().isDetailButtonVisble(datasetName),
                    "Detail button is not shown in other dataset");

            datasetsListPage.getOthersDatasetsTable().getDatasetDetailButton(datasetName).click();
            waitForFragmentVisible(csvDatasetDetailPage);
            assertFalse(csvDatasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is still shown in other dataset");
            assertFalse(csvDatasetDetailPage.isRefreshButtonVisible(),
                    "Update button is still shown in other dataset");
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }
}
