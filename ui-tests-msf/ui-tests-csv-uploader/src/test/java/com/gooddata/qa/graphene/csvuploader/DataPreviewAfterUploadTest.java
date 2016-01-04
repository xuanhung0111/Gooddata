package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.google.common.collect.Lists;

public class DataPreviewAfterUploadTest extends AbstractCsvUploaderTest {

    private static final String NUMBERIC_COLUMN_NAME_ERROR = "Invalid column name. Names must begin with an alphabetic character.";
    private static final String EMPTY_COLUMN_NAME_ERROR = "Fill in the column name.";
    private static final int PAYROLL_FILE_DEFAULT_ROW_COUNT = 3876;
    private static final int PAYROLL_FILE_DEFAULT_COLUMN_COUNT = 9;

    @FindBy(css = ".bubble-negative .content")
    private WebElement errorBubbleMessage;

    @Test(dependsOnMethods = {"createProject"})
    public void checkNoHeaderCsvFile() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);
        takeScreenshot(browser,
                toScreenshotName(DATA_PAGE_NAME, "empty-column-name-in", fileToUpload.getFileName()), getClass());
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be disabled when column names are invalid");

        waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().setColumnsName(PAYROLL_COLUMN_NAMES);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "set-column-names", fileToUpload.getFileName()),
                getClass());
        assertFalse(waitForFragmentVisible(dataPreviewPage).isIntegrationButtonDisabled(), "Add data button should be enabled");
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES);

        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFieldTypeDetection() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, DATA_PAGE_NAME + "-uploading-dataset-" + datasetName, getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, DATA_PAGE_NAME + "-dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(fileToUpload, datasetName);

        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeColumnType() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        String columnNameToChangeType = "Paydate";
        dataPreviewTable.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        List<String> changedTypes = fileToUpload.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        checkDataPreview(fileToUpload.getColumnNames(), changedTypes);
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "-changed-type-dataset-preview-", fileToUpload.getFileName()),
                getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "-changed-type-dataset-uploaded-", datasetName),
                getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, fileToUpload.getColumnNames(), changedTypes);

        waitForFragmentVisible(csvDatasetDetailPage).clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvFileWithMultipleHeaderRows() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.MULTIPLE_COLUMN_NAME_ROWS;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage).selectHeader();

        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        List<WebElement> nonSelectablePreHeaderRows = dataPreviewTable.getNonSelectablePreHeaderRows();
        assertThat(nonSelectablePreHeaderRows.size(), is(3));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(0),
                containsInAnyOrder("id1", "name1", "lastname1", "age1", "Amount1"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(1),
                containsInAnyOrder("id2", "name2", "lastname2", "age2", "Amount2"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(2),
                containsInAnyOrder("id3", "name3", "lastname3", "age3", "Amount3"));
        assertThat(
                dataPreviewPage.getWarningMessage(),
                is("First 3 rows cannot be set as a header. A header must be followed by rows containing at least one number."));
        takeScreenshot(browser, toScreenshotName("set-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        checkDataPreview(fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "dataset-preview", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "changed-type-dataset-uploaded", datasetName),
                getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void setCustomHeader() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewTable.getPreHeaderRows().size(), is(3));

        List<String> customHeaderColumns =
                Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewTable.isColumnNameError("2006 03 01"), "Error is not shown!");
        assertTrue(dataPreviewTable.isColumnNameError("10230"), "Error is not shown!");

        dataPreviewTable.getColumnNameInput("2006 03 01").click();
        assertThat(getErrorBubbleMessage(), is(NUMBERIC_COLUMN_NAME_ERROR));
        dataPreviewTable.getColumnNameInput("10230").click();
        assertThat(getErrorBubbleMessage(), is(NUMBERIC_COLUMN_NAME_ERROR));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");

        dataPreviewTable.changeColumnName("2006 03 01", "Paydate");
        dataPreviewTable.changeColumnName("10230", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("2006-03-01"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("10230"), "Amount");
        takeScreenshot(browser, toScreenshotName("custom-header", fileToUpload.getFileName()), getClass());

        checkDataPreview(customHeaderColumns, fileToUpload.getColumnTypes());
        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "saved-custom-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        int numberOfRows = PAYROLL_FILE_DEFAULT_ROW_COUNT - 3; // All rows above header shouldn't be added
        String expectedDatasetStatus =
                String.format("%s rows, %s data fields", numberOfRows, String.valueOf(PAYROLL_FILE_DEFAULT_COLUMN_COUNT));
        assertTrue(waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetStatus(datasetName)
                .equals(expectedDatasetStatus), "Incorrect row/colum number!");

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL_COLUMN_TYPES);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelChangeColumnNames() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewTable.getPreHeaderRows().size(), is(3));
        assertThat(dataPreviewTable.getPreHeaderRowCells(0), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(
                dataPreviewTable.getPreHeaderRowCells(1),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-01-01", "10230"));
        assertThat(
                dataPreviewTable.getPreHeaderRowCells(2),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-02-01", "10230"));

        assertThat(
                dataPreviewTable.getHeaderColumns(),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230"));
        takeScreenshot(browser, toScreenshotName("set-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.cancelTriggerIntegration();

        checkDataPreview(fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "cancel-custom-header", fileToUpload.getFileName()),
                getClass());
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(fileToUpload, datasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void setHeaderForNoHeaderCsvFile() {
        initDataUploadPage();

        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        CsvFile fileToUpload = CsvFile.NO_HEADER;
        uploadFile(fileToUpload);

        checkDataPreview(fileToUpload);
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewTable.isEmptyColumnNameError(0), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(3), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(6), "Error is not shown!");
        dataPreviewTable.getColumnNameInputs().get(3).click();
        assertThat(getErrorBubbleMessage(), is(EMPTY_COLUMN_NAME_ERROR));

        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(2).click();

        List<String> customHeaderColumns =
                Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        dataPreviewTable.changeColumnName("2006 03 01", "Paydate");
        dataPreviewTable.changeColumnName("10230", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("2006-03-01"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("10230"), "Amount");

        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser, toScreenshotName("custom-header", fileToUpload.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL_COLUMN_TYPES);
    }

    private void checkDataPreview(CsvFile csvFile) {
        checkDataPreview(csvFile.getColumnNames(), csvFile.getColumnTypes());
    }

    private void checkDataPreview(List<String> expectedColumnNames, List<String> expectedColumnTypes) {
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        assertThat(dataPreviewTable.getColumnNames(), containsInAnyOrder(simplifyHeaderNames(expectedColumnNames).toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
    }

    private String getErrorBubbleMessage() {
        return waitForElementVisible(errorBubbleMessage).getText();
    }
}
