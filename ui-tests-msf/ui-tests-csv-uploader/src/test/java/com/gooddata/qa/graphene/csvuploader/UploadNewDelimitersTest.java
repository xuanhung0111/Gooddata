package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.*;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertTrue;

public class UploadNewDelimitersTest extends AbstractCsvUploaderTest  {

    public static final CsvPreference PIPE_PREFERENCE = new CsvPreference.Builder('"', '|', "\r\n").build();
    public static final CsvPreference SEMICOLON_PREFERENCE = new CsvPreference.Builder('"', ';', "\r\n").build();
    public static final CsvPreference TAB_PREFERENCE = new CsvPreference.Builder('"', '\t', "\r\n").build();

    @DataProvider(name = "delimiterDataProvider")
    public Object[][] delimiterDataProvider() {
        final CsvFile payrollPipe = CsvFile.loadFileUsingSpecialCharacter(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.pipe.csv"), PIPE_PREFERENCE)
                .setColumnTypes("Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)"
                        , "Measure");

        final CsvFile payrollSemicolon = CsvFile.loadFileUsingSpecialCharacter(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.semicolon.csv"), SEMICOLON_PREFERENCE)
                .setColumnTypes("Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)"
                        , "Measure");

        final CsvFile payrollTab = CsvFile.loadFileUsingSpecialCharacter(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.tab.csv"), TAB_PREFERENCE)
                .setColumnTypes("Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)"
                        , "Measure");

        return new Object[][]{
                {payrollPipe},
                {payrollSemicolon},
                {payrollTab}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "delimiterDataProvider")
    public void uploadDelimiterTest(CsvFile fileToUpload) {
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
}
