package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.google.common.collect.Lists;

public class DataPreviewAfterUploadTest extends AbstractCsvUploaderTest {

    private static final String NUMBERIC_COLUMN_NAME_ERROR = "Invalid column name. Names must begin with an alphabetic character.";
    private static final String EMPTY_COLUMN_NAME_ERROR = "Fill in the column name.";

    private static final CsvFile NO_HEADER = new CsvFile("no.header")
        .columns(new CsvFile.Column("", "Attribute"), new CsvFile.Column("", "Date (Day/Month/Year)"),
                new CsvFile.Column("", "Measure"))
        .rows("Xen", "01/01/2006", "29")
        .rows("Gooddata", "31/01/2015", "100");

    private static final String ATTRIBUTE = "Attribute";
    private static final String FACT = "Measure";
    private static final String DATE = "Date";

    private static final String DISABLED_DATE_DESCRIPTION =
            "Date must be in one of the supported date formats. Learn more.";

    private static final String DISABLED_FACT_DESCRIPTION =
            "Data can't contain text, only a numerical value or amount.";

    private static final String ATTRIBUTE_INFO = "Represents non-measurable descriptors "
            + "(for example, name, title, phone) by which you break down your data.";

    private static final String FACT_INFO = "Represents the value or amount that you want to measure.";

    @Test(dependsOnMethods = {"createProject"})
    public void checkNoHeaderCsvFile() throws IOException {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        if (NO_HEADER.getFilePath().isEmpty()) {
            NO_HEADER.saveToDisc(testParams.getCsvFolder());
        }

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(NO_HEADER.getFilePath());
        checkDataPreview(dataPreviewPage, NO_HEADER);
        takeScreenshot(browser, "empty-column-name-in-" + NO_HEADER.getFileName(), getClass());

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names are invalid");

        final List<String> columnNames = asList("Attribute", "Date", "Measure");
        waitForFragmentVisible(dataPreviewPage).getDataPreviewTable().setColumnsName(columnNames);
        takeScreenshot(browser, "set-column-names-" + NO_HEADER.getFileName(), getClass());

        assertFalse(waitForFragmentVisible(dataPreviewPage).isIntegrationButtonDisabled(),
                "Add data button should be enabled");

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(NO_HEADER);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "dataset-uploaded-" + datasetName, getClass());

        final DatasetDetailPage datasetDetailPage = waitForFragmentVisible(datasetsListPage)
                .openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, columnNames, NO_HEADER.getColumnTypes());

        datasetDetailPage.clickBackButton();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFieldTypeDetection() {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(PAYROLL.getFilePath());
        checkDataPreview(dataPreviewPage, PAYROLL);
        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(PAYROLL);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "dataset-uploaded-" + datasetName, getClass());

        final DatasetDetailPage datasetDetailPage = waitForFragmentVisible(datasetsListPage)
                .openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());

        datasetDetailPage.clickBackButton();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void changeColumnType() {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(PAYROLL.getFilePath());
        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        final String columnNameToChangeType = "Paydate";

        dataPreviewTable.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        final List<String> changedTypes = PAYROLL.changeColumnType(columnNameToChangeType, ColumnType.ATTRIBUTE);

        checkDataPreview(dataPreviewPage, PAYROLL.getColumnNames(), changedTypes);
        takeScreenshot(browser, "changed-type-dataset-preview-" + PAYROLL.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(PAYROLL);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "changed-type-dataset-uploaded-" + datasetName, getClass());

        final DatasetDetailPage datasetDetailPage = waitForFragmentVisible(datasetsListPage)
                .openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), changedTypes);

        waitForFragmentVisible(datasetDetailPage).clickBackButton();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvFileWithMultipleHeaderRows() {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();
        final CsvFile fileToUpload = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/multiple.column.name.rows.csv"))
                    .setColumnTypes("Measure", "Attribute", "Attribute", "Measure", "Measure");

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(fileToUpload.getFilePath());
        dataPreviewPage.selectHeader();

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        final List<WebElement> nonSelectablePreHeaderRows = dataPreviewTable.getNonSelectablePreHeaderRows();

        assertEquals(nonSelectablePreHeaderRows.size(), 3);
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(0),
                containsInAnyOrder("id1", "name1", "lastname1", "age1", "Amount1"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(1),
                containsInAnyOrder("id2", "name2", "lastname2", "age2", "Amount2"));
        assertThat(dataPreviewTable.getNonSelectablePreHeaderRowCells(2),
                containsInAnyOrder("id3", "name3", "lastname3", "age3", "Amount3"));
        assertEquals(dataPreviewPage.getWarningMessage(),
                "First 3 rows cannot be set as a header. A header must be followed by rows containing at least one number.");

        takeScreenshot(browser, "set-header-to-" + fileToUpload.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();

        checkDataPreview(dataPreviewPage, fileToUpload);
        takeScreenshot(browser, "dataset-preview-" + fileToUpload.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(fileToUpload);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "multi-header-dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, fileToUpload.getColumnNames(), fileToUpload.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void setCustomHeader() {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(PAYROLL.getFilePath());
        dataPreviewPage.selectHeader()
            .getRowSelectionTable()
            .getRow(3)
            .click();

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertEquals(dataPreviewTable.getPreHeaderRows().size(), 3);

        final List<String> customHeaderColumns = Lists.newArrayList("Nowmer", "Sheri", "Graduate Degree",
                "President", "Foodz, Inc.", "Washington", "Spokane", "2006-03-01", "10230");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
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

        takeScreenshot(browser, "custom-header-" + PAYROLL.getFileName(), getClass());

        checkDataPreview(dataPreviewPage, customHeaderColumns, PAYROLL.getColumnTypes());
        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");

        takeScreenshot(browser, "saved-custom-header-" + PAYROLL.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(PAYROLL);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 

        final int numberOfRows = (int) (PAYROLL.getDataRowCount() - 3); // All rows above header shouldn't be added

        assertTrue(waitForFragmentVisible(datasetsListPage)
                .getMyDatasetsTable()
                .getDataset(datasetName)
                .getStatus()
                .equals(format("%s rows, %s data fields", numberOfRows, String.valueOf(PAYROLL_COLUMN_TYPES.length))),
                "Incorrect row/colum number!");

        takeScreenshot(browser, "dataset-uploaded" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelChangeColumnNames() {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(PAYROLL.getFilePath());
        dataPreviewPage.selectHeader()
            .getRowSelectionTable()
            .getRow(3)
            .click();

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertEquals(dataPreviewTable.getPreHeaderRows().size(), 3);
        assertThat(dataPreviewTable.getPreHeaderRowCells(0), contains(PAYROLL.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getPreHeaderRowCells(1),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-01-01", "10230"));
        assertThat(dataPreviewTable.getPreHeaderRowCells(2),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-02-01", "10230"));
        assertThat(dataPreviewTable.getHeaderColumns(),
                contains("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                        "Spokane", "2006-03-01", "10230"));

        takeScreenshot(browser, "set-header-" + PAYROLL.getFileName(), getClass());

        dataPreviewPage.cancelTriggerIntegration();

        checkDataPreview(dataPreviewPage, PAYROLL);
        takeScreenshot(browser, "cancel-custom-header-" + PAYROLL.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(PAYROLL);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void setHeaderForNoHeaderCsvFile() throws IOException {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        if (NO_HEADER.getFilePath().isEmpty()) {
            NO_HEADER.saveToDisc(testParams.getCsvFolder());
        }

        final DataPreviewPage dataPreviewPage = datasetsListPage.uploadFile(NO_HEADER.getFilePath());
        checkDataPreview(dataPreviewPage, NO_HEADER);

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewTable.isEmptyColumnNameError(0), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(1), "Error is not shown!");
        assertTrue(dataPreviewTable.isEmptyColumnNameError(2), "Error is not shown!");

        dataPreviewTable.getColumnNameInputs().get(1).click();
        assertEquals(getErrorBubbleMessage(), EMPTY_COLUMN_NAME_ERROR);

        waitForFragmentVisible(dataPreviewPage)
            .selectHeader()
            .getRowSelectionTable()
            .getRow(0)
            .click();

        final List<String> customHeaderColumns = Lists.newArrayList("Xen", "01/01/2006", "29");
        assertThat(dataPreviewTable.getHeaderColumns(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        dataPreviewTable.changeColumnName("01 01 2006", "Paydate");
        dataPreviewTable.changeColumnName("29", "Amount");
        customHeaderColumns.set(customHeaderColumns.indexOf("01/01/2006"), "Paydate");
        customHeaderColumns.set(customHeaderColumns.indexOf("29"), "Amount");

        assertFalse(dataPreviewTable.isColumnNameError("Paydate"), "Error is still shown!");
        assertFalse(dataPreviewTable.isColumnNameError("Amount"), "Error is still shown!");
        takeScreenshot(browser, "custom-header-" + NO_HEADER.getFileName(), getClass());

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final String datasetName = getNewDataset(NO_HEADER);

        assertEquals(datasetsListPage.getMyDatasetsCount(), datasetCountBeforeUpload + 1);

        assertTrue(datasetsListPage.getMyDatasetsTable()
            .getDataset(datasetName)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX)); 
        takeScreenshot(browser, "dataset-uploaded-" + datasetName, getClass());

        waitForFragmentVisible(datasetsListPage).openDatasetDetailPage(datasetName);
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(datasetName, customHeaderColumns, NO_HEADER.getColumnTypes());
    }

    @DataProvider(name = "csvDataProvider")
    public Object[][] csvDataProvider() throws IOException {
        final CsvFile lessThan50Rows = new CsvFile("data.less.than.50rows")
            .columns(new CsvFile.Column("Measure", "Measure"))
            .rows("10")
            .rows("20");
        lessThan50Rows.saveToDisc(testParams.getCsvFolder());

        return new Object[][] {
            {PAYROLL, "Previewing first 50 rows out of total %s."},
            {lessThan50Rows, "Viewing all %s rows of the file."}
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "csvDataProvider")
    public void checkPreviewPageDisplayWithMaximun50Row(CsvFile csvFile, String rowCountMessage) {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(csvFile.getFilePath());

        takeScreenshot(browser, csvFile.getFileName() + " dislays with correct rows: " + csvFile.getDataRowCount(), getClass());
        assertEquals(dataPreviewPage.getRowCountMessage(), format(rowCountMessage, csvFile.getDataRowCount()));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void testDropdownMenu() throws IOException {
        // This test case just need a simple csv data which has enough column type: Attribute, Fact, and Date
        final CsvFile csvFile = new CsvFile("dropdown.menu")
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Paydate"),
                        new CsvFile.Column("Amount"))
                .rows("Nowmer", "2006-01-01", "10230");
        csvFile.saveToDisc(testParams.getCsvFolder());

        try {
            final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(csvFile.getFilePath());

            WebElement attribute = getItemFromSpecificDropdown(dataPreviewPage, ATTRIBUTE, ColumnType.ATTRIBUTE);
            WebElement fact = getItemFromSpecificDropdown(dataPreviewPage, FACT, ColumnType.ATTRIBUTE);
            WebElement date = getItemFromSpecificDropdown(dataPreviewPage, DATE, ColumnType.ATTRIBUTE);

            takeScreenshot(browser, "Attribute dropdown menu", getClass());
            assertFalse(isItemDisable(attribute), "Attribute item in dropdown is disabled");

            assertTrue(isItemDisable(fact), "Fact item in dropdown is not disabled");
            assertTrue(isItemDisable(date), "Date item in dropdown is not disabled");

            assertEquals(getItemInfo(attribute), ATTRIBUTE_INFO);
            assertEquals(getDisabledItemDescription(fact), DISABLED_FACT_DESCRIPTION);
            assertEquals(getDisabledItemDescription(date), DISABLED_DATE_DESCRIPTION);

            fact = getItemFromSpecificDropdown(dataPreviewPage, FACT, ColumnType.DATE);

            takeScreenshot(browser, "Date dropdown menu", getClass());
            assertTrue(isItemDisable(fact), "Fact item in dropdown is not disabled");

            fact = getItemFromSpecificDropdown(dataPreviewPage, FACT, ColumnType.FACT);
            date = getItemFromSpecificDropdown(dataPreviewPage, DATE, ColumnType.FACT);

            takeScreenshot(browser, "Fact dropdown menu", getClass());
            assertTrue(isItemDisable(date), "Date item in dropdown is not disabled");
            assertEquals(getItemInfo(fact), FACT_INFO);

        } finally {
            Files.deleteIfExists(Paths.get(csvFile.getFilePath()));
        }
    }

    @Test(dependsOnMethods = "createProject")
    public void checkScrollbarShowInPreviewTable() {
        final CsvFile csvFileWithManyColumnRow = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/unsupported.date.formats.csv"));


        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(csvFileWithManyColumnRow.getFilePath());

        takeScreenshot(browser, "Scrollbars-show-in-preview-table", getClass());

        assertTrue(dataPreviewPage.getDataPreviewTable().isVerticalScrollbarVisible(),
                "Vertical scrollbar does not show in preview table");

        assertTrue(dataPreviewPage.getDataPreviewTable().isHorizontalScrollbarVisible(),
                "Horizontal scrollbar does not show in preview table");
    }

    private void checkDataPreview(DataPreviewPage dataPreviewPage, CsvFile csvFile) {
        checkDataPreview(dataPreviewPage, csvFile.getColumnNames(), csvFile.getColumnTypes());
    }

    private void checkDataPreview(DataPreviewPage dataPreviewPage, List<String> expectedColumnNames,
            List<String> expectedColumnTypes) {
        final DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        assertThat(dataPreviewTable.getColumnNames(), containsInAnyOrder(simplifyHeaderNames(expectedColumnNames).toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
    }

    private String getErrorBubbleMessage() {
        return waitForElementVisible(cssSelector(".bubble-negative .content"), browser).getText();
    }

    private WebElement getItemFromSpecificDropdown(DataPreviewPage dataPreviewPage, String item, ColumnType typeDropdown) {
        return dataPreviewPage.getDataPreviewTable()
                .getColumnTypeDropdown(typeDropdown)
                .getItemElement(item);
    }

    private boolean isItemDisable(WebElement item) {
        return item.getAttribute("class").contains("is-disabled");
    }

    private String getDisabledItemDescription(WebElement item) {
        return item.findElement(className("type-desc")).getText();
    }

    private String getItemInfo(WebElement item) {
        new Actions(browser)
                .moveToElement(item.findElement(className("icon-circle-question")))
                .perform();

        return waitForElementVisible(cssSelector(".overlay-wrapper .content"), browser).getText();
    }
}
