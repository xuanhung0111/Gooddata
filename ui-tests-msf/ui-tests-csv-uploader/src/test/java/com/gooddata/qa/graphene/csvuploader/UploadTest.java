package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getProductionProjectModelView;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
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

    @Test(dependsOnGroups = {"createProject"})
    public void checkUploadedDatasetAtManagePage() {
        final Dataset dataset = uploadCsv(PAYROLL);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));

        initManagePage();
        ObjectsTable.getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser).selectObject(datasetName);
        waitForFragmentVisible(datasetDetailPage);
        assertThat(datasetDetailPage.getAttributes(), containsInAnyOrder("Lastname", "Firstname", "Education",
                "Position", "Department", "State", "County", format("Records of %s", datasetName)));
        assertThat(datasetDetailPage.getFacts(), containsInAnyOrder("Amount"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkOnlyUploadedDatasetSync() {
        Dataset dataset = uploadCsv(PAYROLL);
        final String firstDatasetUploadName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList(firstDatasetUploadName, "Date (Paydate)"));

        initManagePage();
        ObjectsTable.getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser).selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        dataset = uploadCsv(PAYROLL);
        final String secondDatasetUploadName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.add(secondDatasetUploadName);

        initManagePage();
        ObjectsTable.getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser).selectObject(firstDatasetUploadName);
        assertEquals(waitForFragmentVisible(datasetDetailPage).getLatestUploadDate(), latestUploadDate);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadOneCsvFileMultipleTime() {
        Dataset dataset = uploadCsv(PAYROLL);
        final String firstDatasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList(firstDatasetName, "Date (Paydate)"));

        dataset = uploadCsv(PAYROLL);
        final String secondDatasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.add(secondDatasetName);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDatasetAnalyzeLink() {
        final Dataset dataset = uploadCsv(PAYROLL);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));

        final String adReportLink = format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertEquals(datasetsListPage.getDatasetAnalyzeLink(datasetName), adReportLink);
        takeScreenshot(browser, "dataset-uploaded-" + datasetName, getClass());

        assertEquals(waitForFragmentVisible(datasetsListPage)
                .openDatasetDetailPage(datasetName)
                .getDatasetAnalyzeLink(), adReportLink);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkBasicUploadProgress() {
        final Dataset dataset = uploadCsv(PAYROLL);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        takeScreenshot(browser, "Successful-upload-data-to-dataset-" + datasetName, getClass());
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadWithoutAttributeCSV() throws IOException {
        final CsvFile fileToUpload = new CsvFile("without attribute")
            .columns(new CsvFile.Column("Amount", "Measure"))
            .rows("10")
            .rows("20");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        final Dataset dataset = uploadCsv(fileToUpload);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        takeScreenshot(browser, "Successful-Payroll-without-attribute-csv-upload-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadWithoutDateCSV() throws IOException {
        final CsvFile fileToUpload = new CsvFile("without date")
            .columns(new CsvFile.Column("state", "Attribute"),
                    new CsvFile.Column("county", "Attribute"),
                    new CsvFile.Column("name", "Attribute"),
                    new CsvFile.Column("censusarea", "Measure"))
            .rows("0400000US05", "", "Arkansas", "52035.477")
            .rows("", "0500000US13059", "Clarke", "119.2");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        final Dataset dataset = uploadCsv(fileToUpload);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        takeScreenshot(browser, "without-date-csv-upload-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(enabled = false, dependsOnGroups = {"createProject"})
    public void uploadSpecialUnicodeCharacterColumnName() {
        final DataPreviewPage dataPreviewPage = initDataUploadPage().uploadFile(PAYROLL.getFilePath());

        final DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
        dataPreviewTable.changeColumnName("Firstname", "A~!@#$%^&*()<>/?;'");
        dataPreviewTable.changeColumnName("Lastname", "kiểm tra ký tự đặc biệt");
        takeScreenshot(browser, "check-special-character-column-name", this.getClass());

        final List<String> customHeaderColumns = Lists.newArrayList(PAYROLL.getColumnNames());
        customHeaderColumns.set(customHeaderColumns.indexOf("Firstname"), "A~!@#$%^&*()<>/?;'");
        customHeaderColumns.set(customHeaderColumns.indexOf("Lastname"), "kiểm tra ký tự đặc biệt");

        assertThat(dataPreviewTable.getColumnNames(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        final Dataset dataset = waitForFragmentVisible(datasetsListPage)
                .getMyDatasetsTable()
                .getDataset(getNewDataset(PAYROLL));
        final String datasetName = dataset.getName();
        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.add(datasetName);

        datasetsListPage.openDatasetDetailPage(datasetName);
        checkCsvDatasetDetail(datasetName, customHeaderColumns, PAYROLL.getColumnTypes());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadNegativeNumber() {
        final CsvFile fileToUpload = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.negative.number.csv"))
                .setColumnTypes(PAYROLL_COLUMN_TYPES);

        final Dataset dataset = uploadCsv(fileToUpload);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList(datasetName, "Date (Paydate)"));

        createMetric("Min of Amount", format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Education")
                .withWhats("Min of Amount"), "Report with negative number");

        takeScreenshot(browser, "report-with-negative-number", this.getClass());

        log.info("Check the negative number in report!");
        final List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        final List<Integer> metricIndexes = asList(0, 1, 2, 3, 4);
        final List<Double> expectedMetricValues = asList(-6080.0, -10230.0, -3330.0, -6630.0, -4670.0);
        assertMetricValuesInReport(metricIndexes, metricValues, expectedMetricValues);

        log.info("Negative numbers are displayed well in report!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void uploadNullNumber() throws IOException {
        final CsvFile fileToUpload = new CsvFile("null number")
            .columns(new CsvFile.Column("Attribute", "Attribute"),
                    new CsvFile.Column("Measure", "Measure"),
                    new CsvFile.Column("Nullable", "Measure"))
            .rows("Conan", "-10230", null)
            .rows("Luffy", "5020", null)
            .rows("Bleach", "-336.96", "20");
        fileToUpload.saveToDisc(testParams.getCsvFolder());

        final Dataset dataset = uploadCsv(fileToUpload);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.add(datasetName);

        createMetric("Sum of Nullable", format("SELECT SUM([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Nullable"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Attribute")
                .withWhats("Sum of Nullable"), "Report with null number");

        takeScreenshot(browser, "report-with-null-number", this.getClass());

        log.info("Check the null number in report!");
        final List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        final List<Integer> metricIndexes = asList(1, 2);
        assertEmptyMetricInReport(metricIndexes, metricValues);

        log.info("Null numbers are displayed well in report!");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkCSVUploaderWithLDMModeler() throws ParseException, JSONException, IOException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));

        final Dataset dataset = uploadCsv(PAYROLL);
        final String datasetName = dataset.getName();

        assertTrue(dataset.getStatus().matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
        datasetNames.addAll(asList("opportunity", "person", datasetName, "Date (Paydate)"));

        final JSONObject onlyProductionDataModel = getProductionProjectModelView(getRestApiClient(),
                testParams.getProjectId(), false);
        final JSONObject allDataModel = getProductionProjectModelView(getRestApiClient(), testParams.getProjectId(),
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
        ObjectsTable.getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser).selectObject(datasetName);
        datasetDetailPage.deleteObject();
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
