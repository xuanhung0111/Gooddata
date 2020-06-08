package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.*;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static java.util.Arrays.asList;

import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertTrue;

public class AllowBigNumberTest extends AbstractCsvUploaderTest {
    private static final List<String> expectedResult = asList("123456789123.123", "123456789123.123", "123456789123.009",
            "123456789123.001", "123456789123", "123456789123", "12345678912", "-123456789123");
    private final String AMOUNT_COLUMN = "Amount";

    private static final CsvFile bigNumber = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/allow.big.number.csv"));

    @Test(dependsOnGroups = {"createProject"})
    public void checkUploadedDatasetWithValidBigNumber() {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(bigNumber.getFilePath());
        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        final List<String> previewDataList = dataPreviewTable.getListValueByColumnName(AMOUNT_COLUMN);
        assertEquals(previewDataList, expectedResult);

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final Dataset dataset = DatasetsListPage.getInstance(browser).getMyDatasetsTable().getDataset(getNewDataset(bigNumber));
        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));

        createMetric("Sum Amount", format("SELECT SUM([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.000");
        createReport(new UiReportDefinition().withName("Report with big number").withHows("User")
                .withWhats("Sum Amount"), "Report with big number");

        takeScreenshot(browser, "report-with-big-number", this.getClass());

        log.info("Check the big number in report!");
        final List<String> metricValues = reportPage.getTableReport().getRawMetricValues();
        final List<Integer> metricIndexes = asList(0, 1, 2, 3, 4, 5, 6);
        final List<String> expectedMetricValues = asList("123,456,789,123.123", "123,456,789,123.123", "123,456,789,123.009", "123,456,789,123.001"
                , "123,456,789,123.000", "123,456,789,123.000", "12,345,678,912.000", "-123,456,789,123.000");
        assertMetricBigValuesInReport(metricIndexes, metricValues, expectedMetricValues);

        log.info("Big numbers are displayed well in report!");
    }

    @Test(dependsOnMethods = {"checkUploadedDatasetWithValidBigNumber"})
    public void updateDatasetWithInvalidBigNumber() {
        final Dataset dataset = uploadCsv(bigNumber);
        final String datasetName = dataset.getName();

        updateCsv(bigNumber, datasetName, true);
        // wait for data sets page loaded
        DatasetsListPage.getInstance(browser);

        final CsvFile invalidBigNumber = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/invalid.big.number.csv"));
        final FileUploadDialog fileUploadDialog = dataset.clickUpdateButton();

        fileUploadDialog.pickCsvFile(invalidBigNumber.getFilePath())
                .clickUploadButton();

        final List<String> backendValidationErrors = waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        log.info("Backend return: " + backendValidationErrors.toString());
        assertThat(backendValidationErrors,
                hasItems(format("Failed to upload the \"%s\" file.", invalidBigNumber.getFileName()), "Row 9 doesn't contain a" +
                        " numerical value. Each row must contain numerical data for analysis."));
    }

    private void assertMetricBigValuesInReport(List<Integer> metricIndexes, List<String> metricValues,
                                               List<String> expectedMetricValues) {
        int index = 0;
        for (int metricIndex : metricIndexes) {
            assertEquals(metricValues.get(metricIndex), expectedMetricValues.get(index));
            index++;
        }
    }
}
