package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;

public class DataOfOtherUsersTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkMyDataAndNoDataOfOthers() {
        final Dataset dataset = uploadCsv(PAYROLL);
        assertTrue(dataset.getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        takeScreenshot(browser, "dataset-uploaded-" + dataset.getName(), getClass());
    }

    @Test(dependsOnMethods = {"checkMyDataAndNoDataOfOthers"})
    public void addOtherAdminToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getAdminUser(), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"addOtherAdminToProject"})
    public void checkNoMyDataButDataOfOthers() throws JSONException {
        try {
            final List<String> projectOwnerDatasetNames = initDataUploadPage()
                    .getMyDatasetsTable()
                    .getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage().waitForMyDatasetsEmptyStateLoaded();

            assertEquals(datasetsListPage.getOthersDatasetsTable().getNumberOfDatasets(),
                    projectOwnerDatasetNames.size());
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"checkNoMyDataButDataOfOthers"})
    public void checkMyDataAndDataOfOthers() throws JSONException {
        try {
            final List<String> projectOwnerDatasetNames = initDataUploadPage()
                    .getMyDatasetsTable()
                    .getDatasetNames();

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            initDataUploadPage().waitForMyDatasetsEmptyStateLoaded();

            final Dataset dataset = uploadCsv(PAYROLL);
            assertTrue(dataset.getStatus()
                .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

            takeScreenshot(browser, "dataset-uploaded-" + dataset.getName(), getClass());

            waitForCollectionIsNotEmpty(datasetsListPage.getOthersDatasetsTable().getRows());
            assertEquals(datasetsListPage.getOtherDatasetsCount(), projectOwnerDatasetNames.size());
            assertThat(datasetsListPage.getOthersDatasetsTable().getDatasetNames(),
                    contains(projectOwnerDatasetNames.toArray()));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"checkMyDataAndDataOfOthers"})
    public void checkAdminCanDeleteDatasetOfOthers() throws JSONException {
        try {
            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            final String datasetName = PAYROLL.getDatasetNameOfFirstUpload();
            final int datasetCountBeforeDelete = initDataUploadPage().getOtherDatasetsCount();

            datasetsListPage.getOthersDatasetsTable()
                .getDataset(datasetName)
                .clickDeleteButton()
                .clickDelete();
            Dataset.waitForDatasetLoaded(browser);

            assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                    format("\"%s\" was successfully deleted!", datasetName));

            final int datasetCountAfterDelete = waitForFragmentVisible(datasetsListPage).getOtherDatasetsCount();
            assertEquals(datasetCountAfterDelete, datasetCountBeforeDelete - 1,
                    "Dataset count <" + datasetCountAfterDelete + "> in the dataset list doesn't"
                            + " match expected value <" + (datasetCountBeforeDelete - 1) + ">.");

            removeDatasetFromUploadHistory(PAYROLL, datasetName);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"checkAdminCanDeleteDatasetOfOthers"})
    public void addViewerAndEditorToProject() throws ParseException, IOException, JSONException {
        addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"addViewerAndEditorToProject"})
    public void checkEditorCanManageHisOwnData() throws JSONException {
        try {
            logoutAndLoginAs(true, UserRoles.EDITOR);

            final int datasetCount = initDataUploadPage().getMyDatasetsCount();

            final Dataset dataset = uploadCsv(PAYROLL);
            assertTrue(dataset.getStatus()
                .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

            final String datasetName = dataset.getName();

            assertTrue(dataset.isDeleteButtonVisible(), "Delete button is not shown in editor's dataset");
            assertTrue(dataset.isUpdateButtonVisible(), "Update button is still shown in editor's dataset");
            assertFalse(dataset.isAnalyzeLinkDisabled(), "Analyze button is not shown in editor's dataset");
            assertTrue(dataset.isDetailButtonVisible(), "Detail button is not shown in editor's dataset");

            final DatasetDetailPage datasetDetailPage = dataset.openDetailPage();

            assertTrue(datasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is not shown in editor's dataset");
            assertTrue(datasetDetailPage.isRefreshButtonVisible(),
                    "Update button is not shown in editor's dataset");
            assertTrue(datasetDetailPage.isAnalyzeButtonVisible(),
                    "Analyze button is not shown in editor's dataset");

            updateCsvInDetailPage(PAYROLL_REFRESH, dataset, true)
                .clickBackButton();

            waitForFragmentVisible(dataset)
                .clickDeleteButton()
                .clickDelete();
            Dataset.waitForDatasetLoaded(browser);

            removeDatasetFromUploadHistory(PAYROLL, datasetName);

            assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                    format("\"%s\" was successfully deleted!", datasetName));

            final int datasetCountAfterDelete = waitForFragmentVisible(datasetsListPage).getMyDatasetsCount();
            assertEquals(datasetCountAfterDelete, datasetCount,
                    "Dataset count <" + datasetCountAfterDelete + "> in the dataset list doesn't "
                            + "match expected value <" + datasetCount + ">.");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"addViewerAndEditorToProject"})
    public void checEditorCannotEditDataOfOthers() throws JSONException {
        try {
            Dataset dataset = uploadCsv(PAYROLL);
            assertTrue(dataset.getStatus()
                .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
            String datasetName = dataset.getName();

            logoutAndLoginAs(true, UserRoles.EDITOR);

            dataset = initDataUploadPage().getOthersDatasetsTable().getDataset(datasetName);

            assertFalse(dataset.isDeleteButtonVisible(), "Delete button is still shown in other dataset");
            assertFalse(dataset.isUpdateButtonVisible(), "Update button is still shown in other dataset");
            assertFalse(dataset.isAnalyzeLinkDisabled(), "Analyze button is not shown in other dataset");
            assertTrue(dataset.isDetailButtonVisible(), "Detail button is not shown in other dataset");

            final DatasetDetailPage datasetDetailPage = dataset.openDetailPage();

            assertFalse(datasetDetailPage.isDeleteButtonVisible(),
                    "Delete button is still shown in other dataset");
            assertFalse(datasetDetailPage.isRefreshButtonVisible(),
                    "Update button is still shown in other dataset");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
