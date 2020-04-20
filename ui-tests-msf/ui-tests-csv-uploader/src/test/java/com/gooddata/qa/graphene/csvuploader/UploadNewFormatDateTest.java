package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertTrue;

public class UploadNewFormatDateTest extends AbstractCsvUploaderTest {
    private static final CsvFile NEW_FORMAT_FILE = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/new.format.csv"))
            .setColumnTypes(
                    "Date (Day-Month-Year)", "Date (Day Month Year)", "Date (Day/Month/Year)", "Date (Day.Month.Year)",
                    "Date (Month-Day-Year)", "Date (Month Day Year)", "Date (Month/Day/Year)", "Date (Month.Day.Year)",
                    "Date (Year-Month-Day)", "Date (Year Month Day)", "Date (Year/Month/Day)", "Date (Year.Month.Day)",
                    "Date (Month Day, Year)", "Measure"
            );

    @DataProvider(name = "dateDataProvider")
    public Object[][] dateDataProvider() {
        final CsvFile datesYY = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/new.format.csv"))
                .setColumnTypes(
                        "Date (Day-Month-Year)", "Date (Day Month Year)", "Date (Day/Month/Year)", "Date (Day.Month.Year)",
                        "Date (Month-Day-Year)", "Date (Month Day Year)", "Date (Month/Day/Year)", "Date (Month.Day.Year)",
                        "Date (Year-Month-Day)", "Date (Year Month Day)", "Date (Year/Month/Day)", "Date (Year.Month.Day)",
                        "Date (Month Day, Year)", "Measure"
                );

        final CsvFile unsupportedDateFormats = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/unsupported.new.format.csv"))
                .setColumnTypes("Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                        "Attribute", "Attribute","Attribute", "Attribute", "Attribute", "Attribute","Attribute", "Measure");

        return new Object[][]{
                {datesYY},
                {unsupportedDateFormats}
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

        final Dataset dataset = DatasetsListPage.getInstance(browser).getMyDatasetsTable().getDataset(getNewDataset(fileToUpload));
        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        final DatasetDetailPage csvDatasetDetailPage = dataset.openDetailPage();
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
    }
    
    @Test(dependsOnMethods = {"uploadDateDatasetWithDifferentFormats"})
    public void testLoadUnsupportDate_upload_failed() {
        final Dataset dataset = uploadCsv(NEW_FORMAT_FILE);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        updateCsv(NEW_FORMAT_FILE, datasetName, true);
        // wait for data sets page loaded
        DatasetsListPage.getInstance(browser);

        final CsvFile dateInvalid = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/new.format.invalid.csv"));
        final FileUploadDialog fileUploadDialog = dataset.clickUpdateButton();

        fileUploadDialog.pickCsvFile(dateInvalid.getFilePath())
                .clickUploadButton();

        final List<String> backendValidationErrors = waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", dateInvalid.getFileName())));
    }
}
