package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;

public class UploadErrorTest extends AbstractCsvUploaderTest {

    private static final CsvFile PAYROLL_FILE = CsvFile.PAYROLL;

    private static final String TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR = "Row %d contains a value that exceeds the limit of 255 characters, or has more than the limit of 250 columns.";
    private static final String ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW = "Row %s contains more columns than the header row.";
    private static final String DATA_WITHOUT_FACT = "The file seems to contain no numerical data for analysis on first 20 rows. " +
            "Try removing comments and multi-row headers from the beginning of the file. " +
            "Only files with at least one numerical column are supported.";

    @DataProvider(name = "errorCsvFileProvider")
    public Object[][] errorCsvFileProvider() {
        return new Object[][] {
            {CsvFile.WITHOUT_FACT, Arrays.asList(DATA_WITHOUT_FACT)},
            {CsvFile.INVALID_DELIMITER, Arrays.asList(DATA_WITHOUT_FACT)},
            {CsvFile.BAD_STRUCTURE, Arrays.asList(String.format(ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW, 2))},
            {CsvFile.TOO_MANY_COLUMNS, Arrays.asList(String.format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 1))},
            {CsvFile.TOO_LONG_FIELD, Arrays.asList(String.format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 2))},
            {CsvFile.CRAZY_DATA, Arrays.asList( "There are 5 rows containing less columns than the header row: 44-48.",
                    "There are 5 rows without at least one numerical value: 44-48. "
                            + "Each row must contain numerical data for analysis.")}
        };
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvBadFormat() throws Exception {
        CsvFile fileToUpload = CsvFile.BAD_STRUCTURE;
        uploadCsvFileWithErrors(fileToUpload, 
                Arrays.asList(String.format(ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW, 2)));

        String datasetName = fileToUpload.getDatasetNameOfFirstUpload();
        assertThat("Dataset with name '" + datasetName + "' should not be in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                not(hasItem(datasetName)));
    }
    
    @Test(dependsOnMethods = {"createProject"})
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
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");
    }
    
    @Test(dependsOnMethods = {"createProject"})
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

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(), containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when columns have the same names");

        // fix it by editing the first column
        dataPreviewTable.changeColumnName(0, RandomStringUtils.randomAlphabetic(20));

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "columnNamesValid"), getClass());

        assertFalse(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be enabled");
    }
    
    @Test(dependsOnMethods = {"createProject"})
    public void uploadTooLargeCsvFile() throws IOException {
        CsvFile fileToUpload = CsvFile.TOO_LARGE_FILE;
        String csvFileName = fileToUpload.getFileName();
        File tooLargeFile = new File(fileToUpload.getCsvFileToUpload());
        RandomAccessFile file = new RandomAccessFile(tooLargeFile, "rw");
        try {
            file.setLength(1100 * 1024 * 1024);

            initDataUploadPage();
            waitForFragmentVisible(datasetsListPage).clickAddDataButton();
            waitForFragmentVisible(fileUploadDialog).pickCsvFile(fileToUpload.getCsvFileToUpload());
            
            // this is workaround for bug MSF-9734
            String validationErrorMessage = waitForFragmentVisible(fileUploadDialog).getValidationErrorMessage();
            assertThat(validationErrorMessage,
                    is("The selected file is larger than 1 GB. Try uploading a subset of the data from the file."));
//            List<String> backendValidationErrors = waitForFragmentVisible(fileUploadDialog)
//                    .getBackendValidationErrors();
//            assertThat(backendValidationErrors,
//                    hasItems("The selected file is larger than 1 GB. "
//                            + "Try uploading a subset of the data from the file."));
            takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", csvFileName),
                    getClass());

            checkButtonOnErrorUploadDialog();
        } finally {
            file.setLength(0);
            file.close();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "errorCsvFileProvider")
    public void uploadCsvFileWithErrors(CsvFile fileToUpload, List<String> errorMessages) {
        initDataUploadPage();
        uploadFile(fileToUpload);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors, hasItem(createErrorFailedToUploadFile(fileToUpload)));
        errorMessages.stream().forEach(error -> assertThat(backendValidationErrors, hasItem(error)));
        takeScreenshot(browser,
                toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", fileToUpload.getFileName()), getClass());

        checkButtonOnErrorUploadDialog();
    }

    private String createErrorFailedToUploadFile(CsvFile csvFile) {
        return String.format("Failed to upload the \"%s\" file.", csvFile.getFileName());
    }

    private void checkButtonOnErrorUploadDialog() {
        assertThat(waitForFragmentVisible(fileUploadDialog).isUploadButtonDisabled(), is(true));
        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
        waitForFragmentNotVisible(fileUploadDialog);
    }
}
