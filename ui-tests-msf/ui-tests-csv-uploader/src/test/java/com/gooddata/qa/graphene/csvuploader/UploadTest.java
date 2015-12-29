package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
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

    @Test(dependsOnMethods = { "createProject" })
    public void checkUploadedDatasetAtManagePage() {
        CsvFile fileToUpload = CsvFile.PAYROLL;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", datasetName), getClass());
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(Lists.newArrayList(datasetName, "Date (Paydate)"));

        initManagePage();
        waitForFragmentVisible(datasetsTable).selectObject(datasetName);
        waitForFragmentVisible(datasetDetailPage);
        assertThat(datasetDetailPage.getAttributes(), containsInAnyOrder("Lastname", "Firstname", "Education",
                "Position", "Department", "State", "County", String.format("Records of %s", datasetName)));
        assertThat(datasetDetailPage.getFacts(), containsInAnyOrder("Amount"));
    }

    @Test(dependsOnMethods = { "createProject" })
    public void checkOnlyUploadedDatasetSync() throws JSONException {
        CsvFile fileToUploadFirst = CsvFile.PAYROLL;
        checkCsvUpload(fileToUploadFirst, this::uploadCsv, true);
        String firstDatasetUploadName = getNewDataset(fileToUploadFirst);
        waitForDatasetName(firstDatasetUploadName);
        waitForDatasetStatus(firstDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(Lists.newArrayList(firstDatasetUploadName, "Date (Paydate)"));

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        String latestUploadDate = waitForFragmentVisible(datasetDetailPage).getLatestUploadDate();

        CsvFile secondFileUpload = CsvFile.PAYROLL;
        checkCsvUpload(secondFileUpload, this::uploadCsv, true);
        String secondDatasetUploadName = getNewDataset(secondFileUpload);
        waitForDatasetName(secondDatasetUploadName);
        waitForDatasetStatus(secondDatasetUploadName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.add(secondDatasetUploadName);

        initManagePage();
        datasetsTable.selectObject(firstDatasetUploadName);
        assertThat(waitForFragmentVisible(datasetDetailPage).getLatestUploadDate(), is(latestUploadDate));
    }

    @Test(dependsOnMethods = { "createProject" })
    public void uploadOneCsvFileMultipleTime() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String firstDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(firstDatasetName);
        waitForDatasetStatus(firstDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", firstDatasetName),
                getClass());
        datasetNames.addAll(Lists.newArrayList(firstDatasetName, "Date (Paydate)"));

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String secondDatasetName = getNewDataset(fileToUpload);

        waitForDatasetName(secondDatasetName);
        waitForDatasetStatus(secondDatasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", secondDatasetName),
                getClass());
        datasetNames.add(secondDatasetName);
    }

    @Test(dependsOnMethods = { "createProject" })
    public void checkDatasetAnalyzeLink() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(Lists.newArrayList(datasetName, "Date (Paydate)"));
        String adReportLink = String.format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertThat(datasetsListPage.getDatasetAnalyzeLink(datasetName), is(adReportLink));
        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", datasetName), getClass());

        waitForFragmentVisible(datasetsListPage).clickDatasetDetailButton(datasetName);
        assertThat(waitForFragmentVisible(csvDatasetDetailPage).getDatasetAnalyzeLink(), is(adReportLink));
        takeScreenshot(browser, toScreenshotName(DATASET_DETAIL_PAGE_NAME, datasetName), getClass());
    }

    @Test(dependsOnMethods = { "createProject" })
    public void checkBasicUploadProgress() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", fileToUpload.getFileName()), getClass());

        assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                is(String.format(SUCCESSFUL_DATA_MESSAGE, datasetName)));
        takeScreenshot(browser, toScreenshotName("Successful-upload-data-to-dataset", datasetName), getClass());
        datasetNames.addAll(Lists.newArrayList(datasetName, "Date (Paydate)"));
    }

    @Test(dependsOnMethods = { "createProject" })
    public void uploadWithoutAttributeCSV() {
        CsvFile fileToUpload = CsvFile.WITHOUT_ATTRIBUTE;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);

        takeScreenshot(browser, toScreenshotName("Upload-progress-of", fileToUpload.getFileName()), getClass());

        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        takeScreenshot(browser, "Successful-Payroll-without-attribute-csv-upload" + "-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(dependsOnMethods = { "createProject" })
    public void uploadWithoutDateCSV() {
        CsvFile fileToUpload = CsvFile.WITHOUT_DATE;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Screenshots.takeScreenshot(browser, "without-date-csv-upload" + "-dashboard", this.getClass());
        datasetNames.add(datasetName);
    }

    @Test(enabled = false, dependsOnMethods = { "createProject" })
    public void uploadSpecialUnicodeCharacterColumnName() {
        initDataUploadPage();
        CsvFile fileToUpload = CsvFile.PAYROLL;
        uploadFile(fileToUpload);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        dataPreviewTable.changeColumnName("Firstname", "A~!@#$%^&*()<>/?;'");
        dataPreviewTable.changeColumnName("Lastname", "kiểm tra ký tự đặc biệt");
        takeScreenshot(browser, "check-special-character-column-name", this.getClass());

        List<String> customHeaderColumns = Lists.newArrayList(CsvFile.PAYROLL.getColumnNames());
        customHeaderColumns.set(customHeaderColumns.indexOf("Firstname"), "A~!@#$%^&*()<>/?;'");
        customHeaderColumns.set(customHeaderColumns.indexOf("Lastname"), "kiểm tra ký tự đặc biệt");

        assertThat(dataPreviewTable.getColumnNames(), contains(customHeaderColumns.toArray()));

        dataPreviewPage.triggerIntegration();

        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.add(datasetName);

        datasetsListPage.clickDatasetDetailButton(datasetName);
        waitForFragmentVisible(csvDatasetDetailPage);
        checkCsvDatasetDetail(datasetName, customHeaderColumns, CsvFile.PAYROLL.getColumnTypes());
    }

    @Test(dependsOnMethods = { "createProject" })
    public void uploadNegativeNumber() {
        CsvFile fileToUpload = CsvFile.PAYROLL_NEGATIVE_NUMBER;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(Lists.newArrayList(datasetName, "Date (Paydate)"));
        
        createMetric("Min of Amount", format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Education")
                .withWhats("Min of Amount"), "Report with negative number");

        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        Screenshots.takeScreenshot(browser, "report-with-negative-number", this.getClass());
        System.out.println("Check the negative number in report!");
        List<Integer> metricIndexes = Arrays.asList(0, 1, 2, 3, 4);
        List<Double> expectedMetricValues = Arrays.asList(-6080.0, -10230.0, -3330.0, -6630.0, -4670.0);
        this.assertMetricValuesInReport(metricIndexes, metricValues, expectedMetricValues);
        System.out.println("Negative numbers are displayed well in report!");
    }

    @Test(dependsOnMethods = { "createProject" })
    public void uploadNullNumber() {
        CsvFile fileToUpload = CsvFile.PAYROLL_NULL_NUMBER;
        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        datasetNames.addAll(Lists.newArrayList(datasetName, "Date (Paydate)"));
        
        createMetric("Sum of Amount", format("SELECT SUM([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount1"))), "#,##0.00");
        createReport(new UiReportDefinition().withName("Report with negative number").withHows("Lastname")
                .withWhats("Sum of Amount"), "Report with null number");

        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        Screenshots.takeScreenshot(browser, "report-with-null-number", this.getClass());
        System.out.println("Check the null number in report!");
        List<Integer> metricIndexes = Arrays.asList(0, 1, 2, 3);
        this.assertEmptyMetricInReport(metricIndexes, metricValues);
        System.out.println("Null numbers are displayed well in report!");
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
}
