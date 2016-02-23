package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.entity.csvuploader.CsvFile.PAYROLL_COLUMN_NAMES;
import static com.gooddata.qa.graphene.entity.csvuploader.CsvFile.PAYROLL_COLUMN_TYPES;
import static com.gooddata.qa.graphene.entity.csvuploader.CsvFile.PAYROLL_DATA_ROW_COUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.google.common.collect.Lists;

public class DataPreviewAfterUploadTest extends AbstractCsvUploaderTest {

    private static final String NUMBERIC_COLUMN_NAME_ERROR = "Invalid column name. Names must begin with an alphabetic character.";
    private static final String EMPTY_COLUMN_NAME_ERROR = "Fill in the column name.";
    private static final int PAYROLL_FILE_DEFAULT_ROW_COUNT = 3876;
    private static final int PAYROLL_FILE_DEFAULT_COLUMN_COUNT = 9;

    private static final CsvFile NO_HEADER = new CsvFile("payroll.no.header",
            Collections.nCopies(9, ""), PAYROLL_COLUMN_TYPES, PAYROLL_DATA_ROW_COUNT);

    @FindBy(css = ".bubble-negative .content")
    private WebElement errorBubbleMessage;

    @Test(dependsOnMethods = {"createProject"})
    public void checkNoHeaderCsvFile() {
        initDataUploadPage();
        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        uploadFile(NO_HEADER);
        checkDataPreview(NO_HEADER);
        takeScreenshot(browser,
                toScreenshotName(DATA_PAGE_NAME, "empty-column-name-in", NO_HEADER.getFileName()), getClass());
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be disabled when column names are invalid");

        waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().setColumnsName(PAYROLL_COLUMN_NAMES);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "set-column-names", NO_HEADER.getFileName()),
                getClass());
        assertFalse(waitForFragmentVisible(dataPreviewPage).isIntegrationButtonDisabled(), "Add data button should be enabled");
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(NO_HEADER);
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
        uploadFile(PAYROLL);
        checkDataPreview(PAYROLL);
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(PAYROLL);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, DATA_PAGE_NAME + "-uploading-dataset-" + datasetName, getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, DATA_PAGE_NAME + "-dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());

        csvDatasetDetailPage.clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeColumnType() {
        initDataUploadPage();
        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();
        uploadFile(PAYROLL);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        String columnNameToChangeType = "Paydate";
        dataPreviewTable.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        List<String> changedTypes = PAYROLL.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        checkDataPreview(PAYROLL.getColumnNames(), changedTypes);
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "-changed-type-dataset-preview-", PAYROLL.getFileName()),
                getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(PAYROLL);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "-changed-type-dataset-uploaded-", datasetName),
                getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), changedTypes);

        waitForFragmentVisible(csvDatasetDetailPage).clickBackButton();
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvFileWithMultipleHeaderRows() {
        initDataUploadPage();
        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();
        CsvFile fileToUpload = new CsvFile("multiple.column.name.rows",
                asList("Id4", "Name4", "Lastname4", "Age4", "Amount4"),
                asList("Measure", "Attribute", "Attribute", "Measure", "Measure"),
                5);
        uploadFile(fileToUpload);
        waitForFragmentVisible(dataPreviewPage).selectHeader();

        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        List<WebElement> nonSelectablePreHeaderRows = dataPreviewTable.getNonSelectablePreHeaderRows();
        assertEquals(nonSelectablePreHeaderRows.size(), 3);
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(0),
                containsInAnyOrder("id1", "name1", "lastname1", "age1", "Amount1"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(1),
                containsInAnyOrder("id2", "name2", "lastname2", "age2", "Amount2"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(2),
                containsInAnyOrder("id3", "name3", "lastname3", "age3", "Amount3"));
        assertEquals(dataPreviewPage.getWarningMessage(),
                "First 3 rows cannot be set as a header. A header must be followed by rows containing at least one number.");
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
        uploadFile(PAYROLL);
        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertEquals(dataPreviewTable.getPreHeaderRows().size(), 3);

        List<String> customHeaderColumns =
                Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewTable.isColumnNameError("2006 03 01"), "Error is not shown!");
        assertTrue(dataPreviewTable.isColumnNameError("10230"), "Error is not shown!");

        dataPreviewTable.getColumnNameInput("2006 03 01").click();
        assertEquals(getErrorBubbleMessage(), NUMBERIC_COLUMN_NAME_ERROR);
        dataPreviewTable.getColumnNameInput("10230").click();
        assertEquals(getErrorBubbleMessage(), NUMBERIC_COLUMN_NAME_ERROR);
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");

        dataPreviewTable.changeColumnName("2006 03 01", "Paydate");
        dataPreviewTable.changeColumnName("10230", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("2006-03-01"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("10230"), "Amount");
        takeScreenshot(browser, toScreenshotName("custom-header", PAYROLL.getFileName()), getClass());

        checkDataPreview(customHeaderColumns, PAYROLL.getColumnTypes());
        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "saved-custom-header", PAYROLL.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(PAYROLL);
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
        uploadFile(PAYROLL);
        waitForFragmentVisible(dataPreviewPage);
        dataPreviewPage.selectHeader().getRowSelectionTable().getRow(3).click();
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertEquals(dataPreviewTable.getPreHeaderRows().size(), 3);
        assertThat(dataPreviewTable.getPreHeaderRowCells(0), contains(PAYROLL.getColumnNames().toArray()));
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
        takeScreenshot(browser, toScreenshotName("set-header", PAYROLL.getFileName()), getClass());

        dataPreviewPage.cancelTriggerIntegration();

        checkDataPreview(PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());
        takeScreenshot(browser,
                toScreenshotName(DATA_PREVIEW_PAGE, "cancel-custom-header", PAYROLL.getFileName()),
                getClass());
        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(PAYROLL);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void setHeaderForNoHeaderCsvFile() {
        initDataUploadPage();
        int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();
        uploadFile(NO_HEADER);
        checkDataPreview(NO_HEADER);
        DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewTable.isEmptyColumnNameError(0), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(3), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(6), "Error is not shown!");
        dataPreviewTable.getColumnNameInputs().get(3).click();
        assertEquals(getErrorBubbleMessage(), EMPTY_COLUMN_NAME_ERROR);

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
        takeScreenshot(browser, toScreenshotName("custom-header", NO_HEADER.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(NO_HEADER);
        waitForExpectedDatasetsCount(datasetCountBeforeUpload + 1);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL_COLUMN_TYPES);
    }

    @DataProvider(name = "csvDataProvider")
    public Object[][] csvDataProvider() {
        return new Object[][] {
            {PAYROLL, "Previewing first 50 rows out of total %s.", "3876"},
            {new CsvFile("payroll.more.column.names"), "Previewing first 50 rows out of total %s.", "3876"},
            {new CsvFile("data.less.than.50rows"), "Viewing all %s rows of the file.", "16"}
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "csvDataProvider")
    public void checkPreviewPageDisplayWithMaximun50Row(CsvFile csvFile, String rowCountMessage, String row) {
        initDataUploadPage();
        uploadFile(csvFile);
        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, csvFile.getFileName() + " dislays with correct rows: " + row, getClass());
        assertEquals(dataPreviewPage.getRowCountMessage(), format(rowCountMessage, row));
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
