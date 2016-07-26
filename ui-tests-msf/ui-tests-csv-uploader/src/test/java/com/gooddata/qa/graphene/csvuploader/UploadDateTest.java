package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DateFormatSelect.DateFormat;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;

/**
 * Reference the document to know the supported date formats
 * https://help.gooddata.com/display/doc/Add+Data+from+a+File+to+a+Project#AddDatafromaFiletoaProject-SupportedDateFormats
 */
public class UploadDateTest extends AbstractCsvUploaderTest {

    private static final CsvFile DATE_YYYY_FILE = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/24dates.yyyy.csv"))
            .setColumnTypes("Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                    "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                    "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)", "Date (Month Day Year)",
                    "Date (Day Month Year)", "Date (Year Month Day)", "Date (Month.Day.Year)", "Date (Day.Month.Year)",
                    "Date (Year.Month.Day)", "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                    "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)", "Date (Month Day Year)",
                    "Date (Day Month Year)", "Date (Year Month Day)", "Measure");

    @DataProvider(name = "dateDataProvider")
    public Object[][] dateDataProvider() {
        final CsvFile datesYY = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/24dates.yyyy.csv"))
                .setColumnTypes("Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                        "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                        "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)",
                        "Date (Month Day Year)", "Date (Day Month Year)", "Date (Year Month Day)", 
                        "Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                        "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                        "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)",
                        "Date (Month Day Year)", "Date (Day Month Year)", "Date (Year Month Day)", "Measure");

        final CsvFile dateYYYY = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/date.yyyymmdd.yymmdd.csv"))
                .setColumnTypes("Date (YearMonthDay)", "Date (YearMonthDay)", "Measure");

        final CsvFile unsupportedDateFormats = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/unsupported.date.formats.csv"))
                .setColumnTypes("Measure", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                        "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                        "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Measure");

        final CsvFile ambiguousDate = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/8ambiguous.dates.starting.with.year.csv"))
                .setColumnTypes("Date (Year-Month-Day)", "Date (Year/Month/Day)", "Date (Year.Month.Day)",
                        "Date (Year Month Day)", "Date (Year-Month-Day)", "Date (Year/Month/Day)",
                        "Date (Year.Month.Day)", "Date (Year Month Day)", "Measure");

        return new Object[][]{
                {DATE_YYYY_FILE},
                {datesYY},
                {dateYYYY},
                {unsupportedDateFormats},
                {ambiguousDate}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "dateDataProvider")
    public void uploadDateDatasetWithDifferentFormats(CsvFile fileToUpload) {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(fileToUpload.getFilePath());

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        takeScreenshot(browser, "data-preview-" + fileToUpload.getFileName(), getClass());
        assertThat(dataPreviewTable.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final Dataset dataset = datasetsListPage.getMyDatasetsTable().getDataset(getNewDataset(fileToUpload));
        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        final DatasetDetailPage csvDatasetDetailPage = dataset.openDetailPage();
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
    }

    @DataProvider(name = "ambiguousDateDataProvider")
    public Object[][] ambiguousDateDataProvider() {
        final CsvFile dateYear = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/6ambiguous.dates.yearday.dayyear.csv"))
                .setColumnTypes("Date", "Date", "Date", "Date", "Date", "Date", "Measure");

        final CsvFile dateMonth = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/6ambiguous.dates.monthday.daymotnh.csv"))
                .setColumnTypes("Date", "Date", "Date", "Date", "Date", "Date", "Measure");

        final CsvFile dateFormat = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/4ambiguous.dates.3formats.csv"))
                .setColumnTypes("Date", "Date", "Date", "Date", "Measure");

        return new Object[][]{
                {dateYear,
                 asList(DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_DOT,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT,
                        DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_SLASH,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                        DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN),
                 2},
                {dateMonth,
                 asList(DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_HYPHEN,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SLASH),
                 2},
                {dateFormat,
                 asList(DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT,
                        DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SPACE),
                 3},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "ambiguousDateDataProvider")
    public void uploadAmbigousDateDataset(CsvFile fileToUpload, List<DateFormat> dateFormats, int dateFormatCounts) {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(fileToUpload.getFilePath());

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        takeScreenshot(browser, "data-preview-" + fileToUpload.getFileName(), getClass());

        assertThat(dataPreviewTable.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names are invalid");

        int indexColumn = 0;
        for (DateFormat dateFormat : dateFormats) {
            assertEquals(dataPreviewTable.getColumnDateFormatCount(indexColumn), dateFormatCounts,
                    "Wrong ambiguous formats");
            dataPreviewTable.changeColumnDateFormat(indexColumn, dateFormat);
            indexColumn++;
        }
        assertFalse(dataPreviewPage.hasPreviewPageErrorMessage(), "Error in preview page should not be shown");

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final Dataset dataset = datasetsListPage.getMyDatasetsTable().getDataset(getNewDataset(fileToUpload));
        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        final DatasetDetailPage csvDatasetDetailPage = dataset.openDetailPage();

        final List<String> columnTypes = dateFormats.stream().map(DateFormat::getColumnType).collect(toList());
        columnTypes.add("Measure");

        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(columnTypes.toArray()));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void updateDateDataset() {
        final Dataset dataset = uploadCsv(DATE_YYYY_FILE);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        updateCsv(DATE_YYYY_FILE, datasetName, true);
        waitForFragmentVisible(datasetsListPage);

        final CsvFile dateInvalidYYYY = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/24dates.yyyy.invalid.csv"));
        final FileUploadDialog fileUploadDialog = dataset.clickUpdateButton();

        fileUploadDialog.pickCsvFile(dateInvalidYYYY.getFilePath())
            .clickUploadButton();

        final List<String> backendValidationErrors = waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", dateInvalidYYYY.getFileName())));
    }
}
