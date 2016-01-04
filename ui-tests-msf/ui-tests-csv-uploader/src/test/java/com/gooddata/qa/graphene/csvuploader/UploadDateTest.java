package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.DateFormat;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.collect.Lists;

/**
 * Reference the document to know the supported date formats
 * https://help.gooddata.com/display/doc/Add+Data+from+a+File+to+a+Project#AddDatafromaFiletoaProject-SupportedDateFormats
 */
public class UploadDateTest extends AbstractCsvUploaderTest {

    @DataProvider(name = "dateDataProvider")
    public Object[][] dateDataProvider() {
        return new Object[][]{
                {CsvFile.DATE_YYYY},
                {CsvFile.DATE_YY},
                {CsvFile.DATE_YY_AND_YYYY},
                {CsvFile.UNSUPPORTED_DATE_FORMAT},
                {CsvFile.AMBIGUOUS_DATE_START_WITH_YEAR}
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "dateDataProvider")
    public void uploadDateDatasetWithDifferentFormats(CsvFile fileToUpload) {
        initDataUploadPage();
        uploadFile(fileToUpload);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        takeScreenshot(browser, toScreenshotName("data-preview", fileToUpload.getFileName()), getClass());
        assertThat(dataPreviewTable.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
        dataPreviewPage.triggerIntegration();
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage);
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
    }

    @DataProvider(name = "ambiguousDateDataProvider")
    public Object[][] ambiguousDateDataProvider() {
        return new Object[][]{
                {CsvFile.AMBIGUOUS_DATE_YEARDAY_DAYYEAR, Lists.newArrayList(DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_DOT,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT, DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_SLASH,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH, DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN), 2},
                {CsvFile.AMBIGUOUS_DATE_MONTHDAY_DAYMONTH, Lists.newArrayList(DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT,
                        DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN, DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT, DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_HYPHEN,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SLASH), 2},
                {CsvFile.AMBIGUOUS_DATE_3_FORMATS, Lists.newArrayList(DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT,
                        DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN, DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                        DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SPACE), 3},
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "ambiguousDateDataProvider")
    public void uploadAmbigousDateDataset(CsvFile fileToUpload, List<DateFormat> dateFormats, int dateFormatCounts) {
        initDataUploadPage();
        uploadFile(fileToUpload);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        takeScreenshot(browser, toScreenshotName("data-preview", fileToUpload.getFileName()), getClass());
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
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage);
        List<String> columnTypes = dateFormats.stream()
                .map(DateFormat::getColumnType)
                .collect(Collectors.toList());
        columnTypes.add("Measure");
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(columnTypes.toArray()));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void updateDateDataset() {
        initDataUploadPage();
        CsvFile fileToUpload = CsvFile.DATE_YYYY;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        refreshCsv(CsvFile.DATE_YYYY, datasetName, true);
        waitForFragmentVisible(datasetsListPage);

        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        doUploadFromDialog(CsvFile.DATE_INVALID_YYYY);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(String.format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", CsvFile.DATE_INVALID_YYYY.getFileName())));
    }
}
