package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

public class DeleteDatasetTest extends AbstractCsvUploaderTest {

    private static final String AMOUNT_FACT = "Amount";
    private static final String REPORT1 = "Report1";
    private static final String REPORT2 = "Report2";
    private static final String SUM_OF_AMOUNT_METRIC = "Sum of Amount";
    private static final String DASHBOARD1 = "Dashboard1";
    private static final String FIRSTNAME_ATTRIBUTE = "Firstname";
    private static final String LASTNAME_ATTRIBUTE = "Lastname";
    private static final String EDUCATION_ATTRIBUTE = "Education";
    private static final String SUCCESSFUL_REMOVE_DATASET = "\"%s\" was successfully deleted!";
    private static final String CONFIRM_DELETE_MESSAGE = "All attributes and measures of the dataset will be "
            + "deleted along with the computed measures and visualization where they are used. "
            + "This action cannot be undone.";

    @Test(dependsOnGroups = {"createProject"})
    public void deleteCsvDatasetFromList() {
        final String datasetName = uploadCsv(PAYROLL).getName();

        createObjectsUsingUploadedData();

        final int datasetCountBeforeDelete = initDataUploadPage().getMyDatasetsCount();

        final DatasetDeleteDialog deleteDialog = DatasetsListPage.getInstance(browser).getMyDatasetsTable()
            .getDataset(datasetName)
            .clickDeleteButton();

        takeScreenshot(browser, "delete-dataset-dialog-visible", getClass());
        assertEquals(deleteDialog.getMessage(), CONFIRM_DELETE_MESSAGE);

        deleteDialog.clickDelete();
        Dataset.waitForDatasetLoaded(browser);

        assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                format(SUCCESSFUL_REMOVE_DATASET, datasetName));

        final int datasetCountAfterDelete = DatasetsListPage.getInstance(browser).getMyDatasetsCount();
        assertEquals(datasetCountAfterDelete, datasetCountBeforeDelete - 1,
                "Dataset count <" + datasetCountAfterDelete + "> in the dataset list"
                        + " doesn't match expected value <" + (datasetCountBeforeDelete - 1) + ">.");

        checkForDatasetRemoved(datasetName);
        removeDatasetFromUploadHistory(PAYROLL, datasetName);
        takeScreenshot(browser, "dataset-" + datasetName + "-deleted", getClass());

        checkObjectsCreatedAfterDatasetRemoved();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteCsvDatasetFromDetail() {
        final String datasetName = uploadCsv(PAYROLL).getName();

        final int datasetCountBeforeDelete = DatasetsListPage.getInstance(browser).getMyDatasetsCount();

        DatasetsListPage.getInstance(browser).getMyDatasetsTable()
            .getDataset(datasetName)
            .openDetailPage()
            .clickDeleteButton()
            .clickDelete();
        Dataset.waitForDatasetLoaded(browser);

        assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                format(SUCCESSFUL_REMOVE_DATASET, datasetName));

        final int datasetCountAfterDelete = DatasetsListPage.getInstance(browser).getMyDatasetsCount();
        assertEquals(datasetCountAfterDelete, datasetCountBeforeDelete - 1,
                "Dataset count <" + datasetCountAfterDelete + "> in the dataset list"
                        + " doesn't match expected value <" + (datasetCountBeforeDelete - 1) + ">.");

        checkForDatasetRemoved(datasetName);
        removeDatasetFromUploadHistory(PAYROLL, datasetName);
        takeScreenshot(browser, "dataset-" + datasetName + "-deleted", getClass());
    }

    @Test(dependsOnMethods = {"deleteCsvDatasetFromList"})
    public void cancelDeleteDataset() {
        final String datasetName = uploadCsv(PAYROLL).getName();

        final int datasetCountBeforeDelete = DatasetsListPage.getInstance(browser).getMyDatasetsCount();

        final DatasetDetailPage datasetDetailPage = DatasetsListPage.getInstance(browser)
                .getMyDatasetsTable()
                .getDataset(datasetName)
                .openDetailPage();

        datasetDetailPage.clickDeleteButton()
            .clickCancel();

        int datasetCountAfterDelete = datasetDetailPage.clickBackButton()
                .getMyDatasetsCount();

        assertEquals(datasetCountAfterDelete, datasetCountBeforeDelete,
                "Dataset count <" + datasetCountAfterDelete + "> in the dataset list"
                        + " doesn't match expected value <" + datasetCountBeforeDelete + ">.");

        DatasetsListPage.getInstance(browser)
            .getMyDatasetsTable()
            .getDataset(datasetName)
            .clickDeleteButton()
            .clickCancel();

        datasetCountAfterDelete = DatasetsListPage.getInstance(browser).getMyDatasetsCount();
        assertEquals(datasetCountAfterDelete, datasetCountBeforeDelete,
                "Dataset count <" + datasetCountAfterDelete + "> in the dataset list"
                        + " doesn't match expected value <" + datasetCountBeforeDelete + ">.");
//        waitForExpectedDatasetsCount(datasetCountBeforeDelete);
    }

    @Test(dependsOnMethods = {"deleteCsvDatasetFromList"})
    public void uploadAfterDeleteDataset() {
        uploadCsv(PAYROLL);
    }

    private void checkForDatasetRemoved(final String csvDatasetName) {
        final Function<WebDriver, Boolean> datasetSuccessfullyRemoved = input -> {
            try {
                DatasetsListPage.getInstance(browser).getMyDatasetsTable().getDataset(csvDatasetName);
                return false;
            } catch (NoSuchElementException e) {
                return true;
            }
        };

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + csvDatasetName + "' has not been removed from the dataset list.")
                .until(datasetSuccessfullyRemoved);
    }

    private void checkObjectsCreatedAfterDatasetRemoved() {
        ReportsPage reportsPage = initReportsPage();
        assertFalse(reportsPage.isReportVisible(REPORT1), "Report1 is not deleted");
        assertFalse(reportsPage.isReportVisible(REPORT2), "Report2 is not deleted");

        initDashboardsPage();
        dashboardsPage.selectDashboard(DASHBOARD1);
        assertTrue(dashboardsPage.isEmptyDashboard(), "Widgets are not removed from Dashboard");

        assertFalse(initMetricPage().isMetricVisible(SUM_OF_AMOUNT_METRIC), "Sum of Amount metric is not deleted");
    }

    private void createObjectsUsingUploadedData() {
        getMdService().createObj(getProject(),
                new Metric(SUM_OF_AMOUNT_METRIC, format("SELECT SUM([%s])",
                        getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT_FACT))), "#,##0"));

        createReport(new UiReportDefinition().withHows(EDUCATION_ATTRIBUTE)
                        .withWhats(SUM_OF_AMOUNT_METRIC)
                        .withName(REPORT1),
                REPORT1);

        createReport(new UiReportDefinition().withHows(LASTNAME_ATTRIBUTE)
                        .withWhats(SUM_OF_AMOUNT_METRIC)
                        .withName(REPORT2)
                        .withFilters(FilterItem.Factory.createAttributeFilter(FIRSTNAME_ATTRIBUTE, "Sheri", "Derrick")),
                REPORT2);

        createDashboard(DASHBOARD1);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(REPORT1);
        dashboardEditBar.addReportToDashboard(REPORT2);
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, SUM_OF_AMOUNT_METRIC);
        dashboardEditBar.saveDashboard();
    }
}
