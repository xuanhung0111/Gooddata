package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataTypeSelect.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;

public class UploadErrorTest extends AbstractCsvUploaderTest {

    private static final String TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR =
            "Row %d contains a value that exceeds the limit of 255 characters, or has more than the limit of 250 columns.";
    private static final String ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW =
            "Row %s contains more columns than the header row.";
    private static final String DATA_WITHOUT_FACT =
            "The file seems to contain no numerical data for analysis on first 20 rows. " +
            "Try removing comments and multi-row headers from the beginning of the file. " +
            "Only files with at least one numerical column are supported.";

    private static final CsvFile BAD_STRUCTURE_FILE = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.bad.csv"));

    @DataProvider(name = "errorCsvFileProvider")
    public Object[][] errorCsvFileProvider() {
        final CsvFile withoutFact = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.without.fact.csv"));

        final CsvFile invalidDelimiter = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/invalid.delimiter.csv"));

        final CsvFile tooManyColumns = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/too.many.columns.csv"));

        final CsvFile tooLongField = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.too.long.field.csv"));

        final CsvFile crazyData = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/crazy.data.csv"));

        return new Object[][]{
                {withoutFact, asList(DATA_WITHOUT_FACT)},
                {invalidDelimiter, asList(DATA_WITHOUT_FACT)},
                {BAD_STRUCTURE_FILE, asList(format(ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW, 2))},
                {tooManyColumns, asList(format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 1))},
                {tooLongField, asList(format(TOO_LONG_COLUMN_OR_TOO_MANY_COLUMNS_ERROR, 2))},
                {crazyData, asList("There are 5 rows containing less columns than the header row: 44-48.",
                        "There are 5 rows without at least one numerical value: 44-48. "
                                + "Each row must contain numerical data for analysis.")}
        };
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCsvBadFormat() {
        uploadCsvFileWithErrors(BAD_STRUCTURE_FILE,
                asList(format(ROW_CONTAINS_MORE_COLUMNS_THAN_THE_HEADER_ROW, 2)));

        assertTrue(DatasetsListPage.getInstance(browser).isMyDatasetsEmpty(),
                "Csv with bad format should not allow to upload");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkNoFactAndNumericColumnNameCsvConfig() {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(PAYROLL_LESS_COLUMNS.getFilePath());

        final String factColumnName = "Amount";

        waitForFragmentVisible(dataPreviewPage)
            .getDataPreviewTable()
            .changeColumnType(factColumnName, ColumnType.ATTRIBUTE);

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Mark at least one column as measure. Only files with at least one numerical column are supported."));

        dataPreviewPage.getDataPreviewTable()
            .changeColumnType(factColumnName, ColumnType.FACT);

        dataPreviewPage.selectHeader()
            .getRowSelectionTable()
            .getRow(3)
            .click(); // select data row as header

        dataPreviewPage.triggerIntegration(); // confirm header row

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names start with numbers");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCsvDuplicateColumnNames() {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(PAYROLL.getFilePath());

        // set up the same names
        final String columnName = "the same";
        final DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        dataPreviewTable.changeColumnName(0, columnName);
        dataPreviewTable.changeColumnName(1, columnName);
        takeScreenshot(browser, "columnNameValidationErrors", getClass());

        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when columns have the same names");

        // fix it by editing the first column
        dataPreviewTable.changeColumnName(0, RandomStringUtils.randomAlphabetic(20));
        takeScreenshot(browser, "columnNamesValid", getClass());

        assertFalse(dataPreviewPage.isIntegrationButtonDisabled(), "Add data button should be enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadTooLargeCsvFile() throws IOException {
        final String filePath = new CsvFile("too large")
            .columns(new CsvFile.Column("Measure", "Measure"))
            .rows("10")
            .saveToDisc(testParams.getCsvFolder());
        final File tooLargeFile = new File(filePath);
        final RandomAccessFile file = new RandomAccessFile(tooLargeFile, "rw");

        try {
            file.setLength(1100 * 1024 * 1024);

            final FileUploadDialog fileUploadDialog = initDataUploadPage().clickAddDataButton();
            fileUploadDialog.pickCsvFile(filePath);

            // this is workaround for bug MSF-9734
            String validationErrorMessage = fileUploadDialog.getValidationErrorMessage();
            takeScreenshot(browser, "validation-errors-upload-file-larger-1GB", getClass());

            assertEquals(validationErrorMessage,
                    "The selected file is larger than 1 GB. Try uploading a subset of the data from the file.");

            checkButtonOnErrorUploadDialog(fileUploadDialog);
        } finally {
            file.setLength(0);
            file.close();
            tooLargeFile.delete();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "errorCsvFileProvider")
    public void uploadCsvFileWithErrors(CsvFile csvFile, List<String> errorMessages) {
        final FileUploadDialog fileUploadDialog = initDataUploadPage().tryUploadFile(csvFile.getFilePath());

        final List<String> backendValidationErrors = fileUploadDialog.getBackendValidationErrors();
        assertThat(backendValidationErrors, hasItem(format("Failed to upload the \"%s\" file.", csvFile.getFileName())));
        errorMessages.stream().forEach(error -> assertThat(backendValidationErrors, hasItem(error)));
        takeScreenshot(browser, "validation-errors-" + csvFile.getFileName(), getClass());

        checkButtonOnErrorUploadDialog(fileUploadDialog);
    }

    private void checkButtonOnErrorUploadDialog(FileUploadDialog fileUploadDialog) {
        assertTrue(waitForFragmentVisible(fileUploadDialog).isUploadButtonDisabled());
        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
    }
}
