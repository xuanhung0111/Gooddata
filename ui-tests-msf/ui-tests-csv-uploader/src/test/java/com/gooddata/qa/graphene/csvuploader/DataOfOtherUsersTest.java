package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertEquals;
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
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.google.common.base.Predicate;

public class DataOfOtherUsersTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkMyDataAndNoDataOfOthers() {
        initDataUploadPage();

        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String myDatasetName = getNewDataset(PAYROLL);

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

            assertEquals(datasetsListPage.getOthersDatasetsTable().getNumberOfDatasets(),
                    projectOwnerDatasetNames.size());
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

            checkCsvUpload(PAYROLL, this::uploadCsv, true);
            String myDatasetName = getNewDataset(PAYROLL);

            waitForDatasetName(myDatasetName);
            assertThat(datasetsListPage.getMyDatasetsTable().getDatasetNames(), hasItem(myDatasetName));
            waitForDatasetStatus(myDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
            takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", myDatasetName),
                    getClass());

            waitForCollectionIsNotEmpty(datasetsListPage.getOthersDatasetsTable().getRows());
            assertEquals(datasetsListPage.getOtherDatasetsCount(), projectOwnerDatasetNames.size());
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
            String datasetName = PAYROLL.getDatasetNameOfFirstUpload();

            final int datasetCountBeforeDelete = datasetsListPage.getOtherDatasetsCount();

            datasetsListPage.getOthersDatasetsTable().getDataset(datasetName).clickDeleteButton();
            waitForFragmentVisible(datasetDeleteDialog).clickDelete();
            waitForFragmentNotVisible(datasetDeleteDialog);

            assertEquals(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    format("\"%s\" was successfully deleted!", datasetName));
            final int datasetCountAfterDelete = datasetCountBeforeDelete - 1;
            Predicate<WebDriver> datasetsCountEqualsExpected = input ->
                    waitForFragmentVisible(datasetsListPage).getOtherDatasetsCount() == datasetCountAfterDelete;

            Graphene.waitGui(browser)
                    .withMessage("Dataset count <" +
                            waitForFragmentVisible(datasetsListPage).getOtherDatasetsCount()
                            + "> in the dataset list doesn't match expected value <" + datasetCountAfterDelete + ">.")
                    .until(datasetsCountEqualsExpected);
            removeDatasetFromUploadHistory(PAYROLL, datasetName);
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
            checkCsvUpload(PAYROLL, this::uploadCsv, true);
            String datasetName = getNewDataset(PAYROLL);
            waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

            Dataset dataset = datasetsListPage.getMyDatasetsTable().getDataset(datasetName);

            assertTrue(dataset.isDeleteButtonVisible(), "Delete button is not shown in editor's dataset");
            assertTrue(dataset.isUpdateButtonVisible(), "Update button is still shown in editor's dataset");
            assertFalse(dataset.isAnalyzeLinkDisabled(), "Analyze button is not shown in editor's dataset");
            assertTrue(dataset.isDetailButtonVisible(), "Detail button is not shown in editor's dataset");

            DatasetDetailPage datasetDetailPage = dataset.openDetailPage();

            assertTrue(datasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is not shown in editor's dataset");
            assertTrue(datasetDetailPage.isRefreshButtonVisible(),
                    "Update button is not shown in editor's dataset");
            assertTrue(datasetDetailPage.isAnalyzeButtonVisible(),
                    "Analyze button is not shown in editor's dataset");

            datasetDetailPage.clickRefreshButton();
            refreshCsv(PAYROLL_REFRESH, datasetName, true);

            waitForFragmentVisible(datasetDetailPage).clickBackButton();
            waitForFragmentVisible(dataset).clickDeleteButton();

            waitForFragmentVisible(datasetDeleteDialog).clickDelete();
            waitForFragmentNotVisible(datasetDeleteDialog);
            removeDatasetFromUploadHistory(PAYROLL, datasetName);

            assertEquals(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    format("\"%s\" was successfully deleted!", datasetName));
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

            checkCsvUpload(PAYROLL, this::uploadCsv, true);
            String datasetName = getNewDataset(PAYROLL);
            waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

            logout();
            signInAtGreyPages(testParams.getEditorUser(), testParams.getEditorPassword());

            initDataUploadPage();

            Dataset dataset = datasetsListPage.getOthersDatasetsTable().getDataset(datasetName);

            assertFalse(dataset.isDeleteButtonVisible(), "Delete button is still shown in other dataset");
            assertFalse(dataset.isUpdateButtonVisible(), "Update button is still shown in other dataset");
            assertFalse(dataset.isAnalyzeLinkDisabled(), "Analyze button is not shown in other dataset");
            assertTrue(dataset.isDetailButtonVisible(), "Detail button is not shown in other dataset");

            DatasetDetailPage datasetDetailPage = dataset.openDetailPage();

            assertFalse(datasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is still shown in other dataset");
            assertFalse(datasetDetailPage.isRefreshButtonVisible(),
                    "Update button is still shown in other dataset");
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }
}
