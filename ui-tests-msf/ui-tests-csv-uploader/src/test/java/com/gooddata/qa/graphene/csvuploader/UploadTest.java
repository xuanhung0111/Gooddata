package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getProductionProjectModelView;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.collect.Lists;

public class UploadTest extends AbstractCsvUploaderTest {

    private List<String> datasetNames = Lists.newArrayList();

    @AfterMethod
    public void removeCreatedDatasets(Method m) {
        if (!m.getDeclaringClass().equals(this.getClass()))
            return;
        if (!datasetNames.isEmpty()) {
            deleteDatasets(datasetNames);
        }
    }

    @BeforeMethod
    public void clearDatasetNames(Method m) {
        datasetNames.clear();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkUploadedDatasetAtManagePage() {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String datasetName = getNewDataset(PAYROLL);

        waitForDatasetName(datasetName);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));

        initManagePage();
        waitForFragmentVisible(datasetsTable).selectObject(datasetName);
        waitForFragmentVisible(datasetDetailPage);
        assertThat(datasetDetailPage.getAttributes(), containsInAnyOrder("Lastname", "Firstname", "Education",
                "Position", "Department", "State", "County", format("Records of %s", datasetName)));
        assertThat(datasetDetailPage.getFacts(), containsInAnyOrder("Amount"));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOnlyUploadedDatasetSync() throws JSONException {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String firstDatasetUploadName = getNewDataset(PAYROLL);
        waitForDatasetName(firstDatasetUploadName);
        waitForDatasetStatus(firstDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(asList(firstDatasetUploadName, "Date (Paydate)"));

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String secondDatasetUploadName = getNewDataset(PAYROLL);
        waitForDatasetName(secondDatasetUploadName);
        waitForDatasetStatus(secondDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.add(secondDatasetUploadName);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        assertEquals(waitForFragmentVisible(datasetDetailPage).getLatestUploadDate(), latestUploadDate);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadOneCsvFileMultipleTime() {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String firstDatasetName = getNewDataset(PAYROLL);

        waitForDatasetName(firstDatasetName);
        waitForDatasetStatus(firstDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", firstDatasetName),
                getClass());
        datasetNames.addAll(asList(firstDatasetName, "Date (Paydate)"));

        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(PAYROLL);

        waitForDatasetName(secondDatasetName);
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
        datasetNames.add(secondDatasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDatasetAnalyzeLink() {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String datasetName = getNewDataset(PAYROLL);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));
        String adReportLink = format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertEquals(datasetsListPage.getDatasetAnalyzeLink(datasetName), adReportLink);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        assertEquals(waitForFragmentVisible(csvDatasetDetailPage).getDatasetAnalyzeLink(), adReportLink);
        takeScreenshot(browser, toScreenshotName(DATASET_DETAIL_PAGE_NAME, datasetName), getClass());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkBasicUploadProgress() {
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String datasetName = getNewDataset(PAYROLL);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", PAYROLL.getFileName()), getClass());

        assertEquals(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                format(SUCCESSFUL_DATA_MESSAGE, datasetName));
        takeScreenshot(browser, toScreenshotName("Successful-upload-data-to-dataset", datasetName), getClass());
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadWithoutAttributeCSV() throws IOException {
        final CsvFile fileToUpload = new CsvFile("without attribute")
            .columns(new CsvFile.Column("Amount", "Measure"))
            .rows("10")
            .rows("20");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        final String datasetName = getNewDataset(fileToUpload);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", fileToUpload.getFileName()), getClass());

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, "Successful-Payroll-without-attribute-csv-upload" + "-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadWithoutDateCSV() throws IOException {
        final CsvFile fileToUpload = new CsvFile("without date")
            .columns(new CsvFile.Column("state", "Attribute"),
                    new CsvFile.Column("county", "Attribute"),
                    new CsvFile.Column("name", "Attribute"),
                    new CsvFile.Column("censusarea", "Measure"))
            .rows("0400000US05", "", "Arkansas", "52035.477")
            .rows("", "0500000US13059", "Clarke", "119.2");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        final String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Screenshots.takeScreenshot(browser, "without-date-csv-upload" + "-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void uploadSpecialUnicodeCharacterColumnName() {
        initDataUploadPage();
        uploadFile(PAYROLL);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        dataPreviewTable.changeColumnName("Firstname", "A~!@#$%^&*()<>/?;'");
        dataPreviewTable.changeColumnName("Lastname", "kiểm tra ký tự đặc biệt");
        takeScreenshot(browser, "check-special-character-column-name", this.getClass());

        List<String> customHeaderColumns = Lists.newArrayList(PAYROLL.getColumnNames());
        customHeaderColumns.set(customHeaderColumns.indexOf("Firstname"), "A~!@#$%^&*()<>/?;'");
        customHeaderColumns.set(customHeaderColumns.indexOf("Lastname"), "kiểm tra ký tự đặc biệt");

        assertThat(dataPreviewTable.getColumnNames(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(PAYROLL);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.add(datasetName);

        datasetsListPage.clickDatasetDetailButton(datasetName);
        waitForFragmentVisible(csvDatasetDetailPage);
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL.getColumnTypes());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadNegativeNumber() throws IOException {
        final CsvFile fileToUpload = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.negative.number.csv"))
                .setColumnTypes(PAYROLL_COLUMN_TYPES);

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));

        createMetric("Min of Amount", format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Education")
                .withWhats("Min of Amount"), "Report with negative number");

        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        Screenshots.takeScreenshot(browser, "report-with-negative-number", this.getClass());
        log.info("Check the negative number in report!");
        List<Integer> metricIndexes = asList(0, 1, 2, 3, 4);
        List<Double> expectedMetricValues = asList(-6080.0, -10230.0, -3330.0, -6630.0, -4670.0);
        this.assertMetricValuesInReport(metricIndexes, metricValues, expectedMetricValues);
        log.info("Negative numbers are displayed well in report!");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadNullNumber() throws IOException {
        final CsvFile fileToUpload = new CsvFile("null number")
            .columns(new CsvFile.Column("Attribute", "Attribute"),
                    new CsvFile.Column("Measure", "Measure"),
                    new CsvFile.Column("Nullable", "Measure"))
            .rows("Conan", "-10230", null)
            .rows("Luffy", "5020", null)
            .rows("Bleach", "-336.96", "20");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.add(datasetName);

        createMetric("Sum of Nullable", format("SELECT SUM([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Nullable"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Attribute")
                .withWhats("Sum of Nullable"), "Report with null number");

        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        Screenshots.takeScreenshot(browser, "report-with-null-number", this.getClass());
        log.info("Check the null number in report!");
        List<Integer> metricIndexes = asList(1, 2);
        this.assertEmptyMetricInReport(metricIndexes, metricValues);
        log.info("Null numbers are displayed well in report!");
    }

    @Test(dependsOnMethods = "createProject")
    public void checkCSVUploaderWithLDMModeler() throws ParseException, JSONException, IOException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        String datasetName = getNewDataset(PAYROLL);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(asList("opportunity", "person", datasetName, "Date (Paydate)"));
        JSONObject onlyProductionDataModel = getProductionProjectModelView(getRestApiClient(),
                testParams.getProjectId(), false);
        JSONObject allDataModel = getProductionProjectModelView(getRestApiClient(), testParams.getProjectId(),
                true);
        assertThat(getListOfDatasets(onlyProductionDataModel), containsInAnyOrder("opportunity", "person"));
        assertThat(getListOfDatasets(allDataModel), containsInAnyOrder("opportunity", "person", datasetName));
    }

    private void assertMetricValuesInReport(List<Integer> metricIndexes, List<Float> metricValues,
                                            List<Double> expectedMetricValues) {
        int index = 0;
        for (int metricIndex : metricIndexes) {
            assertEquals(metricValues.get(metricIndex).doubleValue(), expectedMetricValues.get(index));
            index++;
        }
    }

    private void assertEmptyMetricInReport(List<Integer> metricIndexes, List<Float> metricValues) {
        for (int metricIndex : metricIndexes) {
            assertEquals(metricValues.get(metricIndex).doubleValue(), 0.0);
        }
    }

    private void deleteDatasets(List<String> datasets) {
        datasets.stream().forEach(this::deleteDataset);
    }

    private void deleteDataset(String datasetName) {
        initManagePage();
        datasetsTable.selectObject(datasetName);
        datasetDetailPage.deleteDataset();
    }

    private List<String> getListOfDatasets(JSONObject dataModel) throws JSONException {
        List<String> datasetNames = new ArrayList<String>();
        JSONArray datasets = dataModel.getJSONObject("projectModelView").getJSONObject("model")
                .getJSONObject("projectModel").getJSONArray("datasets");
        for (int i = 0; i < datasets.length(); i++) {
            JSONObject object = datasets.getJSONObject(i).getJSONObject("dataset");
            datasetNames.add(object.getString("title"));
        }
        return datasetNames;
    }
}
