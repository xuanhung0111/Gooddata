package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.google.common.base.Predicate;

public class DeleteDatasetTest extends AbstractCsvUploaderTest {

    private static final String AMOUNT_FACT = "Amount";
    private static final String REPORT1 = "Report1";
    private static final String REPORT2 = "Report2";
    private static final String SUM_OF_AMOUNT_METRIC = "Sum of Amount";
    private static final String DASHBOARD1 = "Dashboard1";
    private static final String FIRSTNAME_ATTRIBUTE = "Firstname";
    private static final String LASTNAME_ATTRIBUTE = "Lastname";
    private static final String EDUCATION_ATTRIBUTE = "Education";
    private static final String DELETE_DATASET_DIALOG_NAME = "delete-dataset-dialog";
    private static final String SUCCESSFUL_REMOVE_DATASET = "\"%s\" was successfully deleted!";
    private static final String CONFIRM_DELETE_MESSAGE = "All attributes and measures of the dataset will be "
            + "deleted along with the computed measures and visualization where they are used. "
            + "This action cannot be undone.";

    @Test(dependsOnMethods = {"createProject"})
    public void deleteCsvDatasetFromList() throws Exception {
        CsvFile fileToUpload = CsvFile.PAYROLL;
        String datasetName = uploadData(fileToUpload);
        
        createObjectsUsingUploadedData();
        initDataUploadPage();
        final int datasetCountBeforeDelete = datasetsListPage.getMyDatasetsCount();

        datasetsListPage.getMyDatasetsTable().getDatasetDeleteButton(datasetName).click();
        takeScreenshot(browser, DELETE_DATASET_DIALOG_NAME, getClass());
        waitForFragmentVisible(datasetDeleteDialog);
        assertThat(datasetDeleteDialog.getMessage(), is(CONFIRM_DELETE_MESSAGE));
        datasetDeleteDialog.clickDelete();

        assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                is(String.format(SUCCESSFUL_REMOVE_DATASET, datasetName)));

        waitForExpectedDatasetsCount(datasetCountBeforeDelete - 1);

        checkForDatasetRemoved(datasetName);
        removeDatasetFromUploadHistory(fileToUpload, datasetName);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, datasetName, "dataset-deleted"), getClass());
        
        checkObjectsCreatedAfterDatasetRemoved();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deleteCsvDatasetFromDetail() throws Exception {
        CsvFile fileToUpload = CsvFile.PAYROLL;
        String datasetName = uploadData(fileToUpload);
        
        final int datasetCountBeforeDelete = datasetsListPage.getMyDatasetsCount();

        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage).clickDeleteButton();
        waitForFragmentVisible(datasetDeleteDialog).clickDelete();
        
        assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                is(String.format(SUCCESSFUL_REMOVE_DATASET, datasetName)));

        waitForExpectedDatasetsCount(datasetCountBeforeDelete - 1);

        checkForDatasetRemoved(datasetName);
        removeDatasetFromUploadHistory(fileToUpload, datasetName);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, datasetName, "dataset-deleted"), getClass());
    }

    @Test(dependsOnMethods = {"deleteCsvDatasetFromList"})
    public void cancelDeleteDataset() {
        String datasetName = uploadData(CsvFile.PAYROLL);
        final int datasetCountBeforeDelete = datasetsListPage.getMyDatasetsCount();

        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage).clickDeleteButton();
        waitForFragmentVisible(datasetDeleteDialog).clickCancel();
        
        waitForFragmentNotVisible(datasetDeleteDialog);
        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
        waitForExpectedDatasetsCount(datasetCountBeforeDelete);
        
        datasetsListPage.getMyDatasetsTable().getDatasetDeleteButton(datasetName).click();
        waitForFragmentVisible(datasetDeleteDialog).clickCancel();
        waitForFragmentNotVisible(datasetDeleteDialog);
        waitForExpectedDatasetsCount(datasetCountBeforeDelete);
    }

    @Test(dependsOnMethods = {"deleteCsvDatasetFromList"})
    public void uploadAfterDeleteDataset() {
        uploadData(CsvFile.PAYROLL);
    }

    private String uploadData(CsvFile fileToUpload) {
        initDataUploadPage();
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        return datasetName;
    }

    private void removeDatasetFromUploadHistory(CsvFile csvFile, String datasetName) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        assertThat(fileUpload.isPresent(), is(true));
        fileUpload.get().removeDatasetName(datasetName);
    }

    private void checkForDatasetRemoved(final String csvDatasetName) {
        Predicate<WebDriver> datasetSuccessfullyRemoved = input ->
                waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRow(csvDatasetName) == null;

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + csvDatasetName + "' has not been removed from the dataset list.")
                .until(datasetSuccessfullyRemoved);
    }

    private void checkObjectsCreatedAfterDatasetRemoved() {
        initReportsPage();
        assertFalse(reportsPage.isReportVisible(REPORT1), "Report1 is not deleted");
        assertFalse(reportsPage.isReportVisible(REPORT2), "Report2 is not deleted");
        
        initDashboardsPage();
        dashboardsPage.selectDashboard(DASHBOARD1);
        waitForDashboardPageLoaded(browser);
        assertTrue(dashboardsPage.isEmptyDashboard(), "Widgets are not removed from Dashboard");
        
        initMetricPage();
        assertFalse(metricPage.isMetricVisible(SUM_OF_AMOUNT_METRIC), "Sum of Amount metric is not deleted");
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
