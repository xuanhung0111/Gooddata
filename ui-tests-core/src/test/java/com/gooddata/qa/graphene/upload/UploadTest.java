package com.gooddata.qa.graphene.upload;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns.OptionDataType;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups= { "uploadTests" }, description = "Overall tests for upload.html page in GD platform")
public class UploadTest extends AbstractUploadTest {

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "valid-delimiter"})
    public void uploadDelimiterColon() throws InterruptedException {
        uploadFileAndClean("delimiter-colon");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "valid-delimiter"})
    public void uploadDelimiterComma() throws InterruptedException {
        uploadFileAndClean("delimiter-comma");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "valid-delimiter"})
    public void uploadDelimiterPipe() throws InterruptedException {
        uploadFileAndClean("delimiter-pipe");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "valid-delimiter"})
    public void uploadDelimiterSemicolon() throws InterruptedException {
        uploadFileAndClean("delimiter-semicolon");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "valid-delimiter"})
    public void uploadDelimiterTab() throws InterruptedException {
        uploadFileAndClean("delimiter-tab");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-case"})
    public void uploadSpecialUnicodeCharacterColumnName() throws InterruptedException {
        try {
            selectFileToUpload("payroll");
            Screenshots.takeScreenshot(browser, "check-special-character-column-name", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            uploadColumns.setColumnName(0, "~!@#$%^&*()<>/?;'");
            uploadColumns.setColumnName(1, "kiểm tra ký tự đặc biệt");
            Screenshots.takeScreenshot(browser, "upload-definition-check-special-character-column-name",
                    this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            waitForDashboardPageLoaded(browser);
            Screenshots.takeScreenshot(browser, "check-special-character-column-name" + "-dashboard",
                    this.getClass());
            checkAttributeName("~!@#$%^&*()<>/?;'");
            Screenshots.takeScreenshot(browser, "attribute-name-with-special-character", this.getClass());
            checkAttributeName("kiểm tra ký tự đặc biệt");
            Screenshots.takeScreenshot(browser, "attribute-name-with-unicode-character", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("payroll", "Date (payroll_paydate)");
            cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-case"})
    public void uploadNegativeNumber() throws InterruptedException {
        try {
            selectFileToUpload("payroll-negative-number");
            Screenshots.takeScreenshot(browser, "payroll-negative-number-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnType(8), UploadColumns.OptionDataType.NUMBER.getOptionLabel());
            Screenshots.takeScreenshot(browser, "upload-definition-payroll-negative-number", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            waitForDashboardPageLoaded(browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-negative-number-csv-upload" + "-dashboard",
                    this.getClass());
            addEmptyDashboard();

            // Create a report and check negative value
            List<String> what = new ArrayList<String>();
            what.add("Min of Amount");
            List<String> how = new ArrayList<String>();
            how.add("Education");
            prepareReport("Report with negative number", ReportTypes.TABLE, what, how);
            List<Float> metricValues = report.getMetricElements();
            Screenshots.takeScreenshot(browser, "report-with-negative-number", this.getClass());
            System.out.println("Check the negative number in report!");
            List<Integer> metricIndexes = Arrays.asList(0, 1, 2, 3, 4);
            List<Double> expectedMetricValues = Arrays.asList(-6080.0, -10230.0, -3330.0, -6630.0, -4670.0);
            this.assertMetricValuesInReport(metricIndexes, metricValues, expectedMetricValues);
            System.out.println("Negative numbers are displayed well in report!");
        } finally {
            List<String> datasets =
                    Arrays.asList("payroll-negative-number", "Date (payroll-negative-number_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-case"})
    public void uploadNullNumber() throws InterruptedException {
        try {
            this.selectFileToUpload("payroll-null-number");
            Screenshots.takeScreenshot(browser, "payroll-null-number-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnType(8), UploadColumns.OptionDataType.NUMBER.getOptionLabel());
            Screenshots.takeScreenshot(browser, "upload-definition-payroll-null-number", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            waitForDashboardPageLoaded(browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-null-number-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();

            // Check null value in report
            List<String> what = new ArrayList<String>();
            what.add("Sum of Amount");
            List<String> how = new ArrayList<String>();
            how.add("Lastname");
            prepareReport("Report with null number", ReportTypes.TABLE, what, how);
            Screenshots.takeScreenshot(browser, "report-with-null-number", this.getClass());
            List<Float> metricValues = report.getMetricElements();
            System.out.println("Check the null number in report!");
            List<Integer> metricIndexes = Arrays.asList(0, 2, 4, 5, 6, 7, 8);
            this.assertEmptyMetricInReport(metricIndexes, metricValues);
            System.out.println("Null numbers are displayed well in report!");
        } finally {
            List<String> datasets = Arrays.asList("payroll-null-number", "Date (payroll-null-number_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-change-column-type"})
    public void uploadChangeColumnType() throws InterruptedException {
        try {
            Map<Integer, OptionDataType> columnIndexAndType = new HashMap<Integer, OptionDataType>();
            columnIndexAndType.put(2, OptionDataType.TEXT);
            columnIndexAndType.put(3, OptionDataType.TEXT);
            columnIndexAndType.put(4, OptionDataType.NUMBER);
            uploadCSV(getFilePathFromResource("/" + UPLOAD_CSV + "/change-column-type.csv"), columnIndexAndType,
                    "change-column-type");
            addEmptyDashboard();

            // Check non-number value in report
            List<String> what = new ArrayList<String>();
            what.add("Sum of Age");
            List<String> how = new ArrayList<String>();
            how.add("CustomerID");
            prepareReport("Report with Age", ReportTypes.TABLE, what, how);
            Screenshots.takeScreenshot(browser, "report-with-null-matric-value", this.getClass());
            List<Float> metricValues = report.getMetricElements();
            List<Integer> metricIndexes = Arrays.asList(2, 4, 6, 9, 13, 14, 16);
            assertEmptyMetricInReport(metricIndexes, metricValues);
            System.out.print("Non-number values are ignored, they are displayed as null cell in report.");
        } finally {
            List<String> datasets = Arrays.asList("change-column-type");
            cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-error"})
    public void uploadCheckEmptyColumnName() throws InterruptedException {
        this.selectFileToUpload("payroll");
        Screenshots.takeScreenshot(browser, "check-empty-field-csv-upload", this.getClass());
        UploadColumns uploadColumns = upload.getUploadColumns();
        uploadColumns.setColumnName(0, "");
        loadButton.click();
        checkErrorColumn(uploadColumns, 0, true, "can't be blank");
        Screenshots.takeScreenshot(browser, "check-empty-column-name-upload", this.getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-error"})
    public void uploadCheckUniqueColumnName() throws InterruptedException {
        this.selectFileToUpload("payroll");
        Screenshots.takeScreenshot(browser, "check-unique-field-csv-upload", this.getClass());
        UploadColumns uploadColumns = upload.getUploadColumns();
        uploadColumns.setColumnName(0, "sameName");
        uploadColumns.setColumnName(1, "sameName");
        loadButton.click();
        checkErrorColumn(uploadColumns, 0, true, "not unique");
        checkErrorColumn(uploadColumns, 1, false, null);
        Screenshots.takeScreenshot(browser, "check-unique-column-name-upload", this.getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-error"})
    public void uploadIncorrectCSVFile() throws InterruptedException {
        uploadInvalidCSVFile("payroll-incorrect-header", "UNABLE TO IMPORT FILE. NUMBER OF COLUMNS MISMATCH.",
                "Line 2 contains a different number of columns than the header.",
                "See CSV formatting guidelines or contact Support if problems persist.");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-error"})
    public void uploadTooLargeCSVFile() throws InterruptedException {
        uploadInvalidCSVFile("payroll-too-large", "THIS FILE IS TOO BIG TO UPLOAD.",
                "The maximum upload size is 20MB.", null);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadNoHeaderCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("payroll-no-header");
            Screenshots.takeScreenshot(browser, "no-header-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            List<Integer> columnIndexes = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
            List<String> columnNames =
                    Arrays.asList("Nowmer", "Sheri", "Graduate Degree", "President", "Foodz, Inc.", "Washington",
                            "Spokane", "2006-01-01", "10230");
            uploadColumns.assertColumnsName(columnIndexes, columnNames);
            Screenshots.takeScreenshot(browser, "upload-definition-payroll-no-header", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-no-header-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("payroll-no-header", "Date (payroll-no-header_column8)");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadWithoutFactCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("payroll-without-fact");
            Screenshots.takeScreenshot(browser, "without-fact-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getNumberOfColumns(), 7);
            List<Integer> columnIndexes = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
            List<String> dataTypes = Arrays.asList("TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT");
            uploadColumns.assertColumnsType(columnIndexes, dataTypes);
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-without-fact-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("payroll-without-fact");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadWithoutAttributeCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("without-attribute");
            Screenshots.takeScreenshot(browser, "without-attribute-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getNumberOfColumns(), 1);
            assertEquals(uploadColumns.getColumnName(0), "Amount");
            assertEquals(uploadColumns.getColumnType(0), UploadColumns.OptionDataType.NUMBER.getOptionLabel());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-without-attribute-csv-upload" + "-dashboard",
                    this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("without-attribute");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadDateEUCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("payroll-Date-EU");
            Screenshots.takeScreenshot(browser, "payroll-Date-EU-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnName(7), "Paydate");
            assertEquals(uploadColumns.getColumnType(7), UploadColumns.OptionDataType.DATE.getOptionLabel());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-Date-EU-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("payroll-Date-EU", "Date (payroll-Date-EU_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadDateUSCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("payroll-Date-US");
            Screenshots.takeScreenshot(browser, "payroll-Date-US-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnName(7), "Paydate");
            assertEquals(uploadColumns.getColumnType(7), UploadColumns.OptionDataType.DATE.getOptionLabel());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "payroll-Date-US-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("payroll-Date-US", "Date (payroll-Date-US_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-special-kind-of-csv"})
    public void uploadWithoutDateCSV() throws InterruptedException {
        try {
            this.selectFileToUpload("without-Date");
            Screenshots.takeScreenshot(browser, "without-Date-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getNumberOfColumns(), 4);
            List<Integer> columnIndexes = Arrays.asList(0, 1, 2, 3);
            List<String> dataTypes = Arrays.asList("TEXT", "TEXT", "TEXT", "NUMBER");
            uploadColumns.assertColumnsType(columnIndexes, dataTypes);
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "without-Date-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("without-Date");
            this.cleanDashboardAndDatasets(datasets);
        }

    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-different-date-formats"})
    public void uploadDifferentDate1() throws InterruptedException {
        try {
            uploadDifferentDateFormat("payroll-dd-mm-yy");
        } finally {
            List<String> datasets = Arrays.asList("payroll-dd-mm-yy", "Date (payroll-dd-mm-yy_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-different-date-formats"})
    public void uploadDifferentDate2() throws InterruptedException {
        try {
            uploadDifferentDateFormat("payroll-ddmmyy");
        } finally {
            List<String> datasets = Arrays.asList("payroll-ddmmyy", "Date (payroll-ddmmyy_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-different-date-formats"})
    public void uploadDifferentDate3() throws InterruptedException {
        try {
            uploadDifferentDateFormat("payroll-mm-dd-yy");
        } finally {
            List<String> datasets = Arrays.asList("payroll-mm-dd-yy", "Date (payroll-mm-dd-yy_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-different-date-formats"})
    public void uploadDifferentDate4() throws InterruptedException {
        try {
            uploadDifferentDateFormat("payroll-mmddyy");
        } finally {
            List<String> datasets = Arrays.asList("payroll-mmddyy", "Date (payroll-mmddyy_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-different-date-formats"})
    public void uploadDifferentDate5() throws InterruptedException {
        try {
            uploadDifferentDateFormat("payroll-yyyymmdd");
        } finally {
            List<String> datasets = Arrays.asList("payroll-yyyymmdd", "Date (payroll-yyyymmdd_paydate)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"auto-guessed-ID-column"})
    public void uploadAutoGuessedID() throws InterruptedException {
        try {
            this.selectFileToUpload("auto-guessed-ID");
            Screenshots.takeScreenshot(browser, "auto-guessed-ID-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnName(2), "AttrID");
            assertEquals(uploadColumns.getColumnType(2), UploadColumns.OptionDataType.TEXT.getOptionLabel());
            Screenshots.takeScreenshot(browser, "upload-definition-auto-guessed-ID", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "auto-guessed-ID-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();

            // Check ID field in report
            List<String> what = new ArrayList<String>();
            List<String> how = new ArrayList<String>();
            how.add("AttrID");
            prepareReport("Report with auto-guessed ID field", ReportTypes.TABLE, what, how);
            List<String> attributeElements = report.getAttributeElements();
            Screenshots.takeScreenshot(browser, "report-with-auto-guessed-ID", this.getClass());
            System.out.println("Check the auto-guessed ID in report!");
            List<Integer> attributeIndexes = Arrays.asList(0, 1, 2, 3);
            List<String> expectedAttribueElements = Arrays.asList("0120", "0124", "0223", "0423");
            this.assertAttributeElementsInReport(attributeIndexes, attributeElements, expectedAttribueElements);
            System.out.println("Auto-guessed ID field is displayed well in report!");
        } finally {
            List<String> datasets = Arrays.asList("auto-guessed-ID");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"auto-guessed-ID-column"})
    public void uploadAutoGuessedManyID() throws InterruptedException {
        try {
            this.selectFileToUpload("many-column-ID");
            Screenshots.takeScreenshot(browser, "auto-guessed-many-ID-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getColumnName(1), "NumberID");
            assertEquals(uploadColumns.getColumnType(1), UploadColumns.OptionDataType.TEXT.getOptionLabel());
            assertEquals(uploadColumns.getColumnName(2), "NumID");
            assertEquals(uploadColumns.getColumnType(2), UploadColumns.OptionDataType.NUMBER.getOptionLabel());
            Screenshots.takeScreenshot(browser, "upload-definition-auto-guessed-many-ID", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "auto-guessed-many-ID-csv-upload" + "-dashboard", this.getClass());
            addEmptyDashboard();

            // Check ID field in report
            List<String> what = new ArrayList<String>();
            what.add("Sum of NumID");
            List<String> how = new ArrayList<String>();
            how.add("NumberID");
            prepareReport("Report with auto-guessed ID field", ReportTypes.TABLE, what, how);
            List<String> attributeElements = report.getAttributeElements();
            List<Float> metricValues = report.getMetricElements();
            Screenshots.takeScreenshot(browser, "report-with-auto-guessed-many-ID", this.getClass());
            System.out.println("Check the auto-guessed many ID in report!");
            List<Integer> indexes = Arrays.asList(0, 1, 2, 3);
            List<String> expectedAttribueElements = Arrays.asList("012", "053", "12", "5");
            this.assertAttributeElementsInReport(indexes, attributeElements, expectedAttribueElements);
            List<Double> expectedMetricValues = Arrays.asList(102.0, 17.0, 13.0, 8.0);
            this.assertMetricValuesInReport(indexes, metricValues, expectedMetricValues);
            System.out.println("Auto-guessed ID field is displayed well in report!");
        } finally {
            List<String> datasets = Arrays.asList("many-column-ID");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"crazy-footer"})
    public void uploadDataWithCrazyFooter() throws InterruptedException {
        try {
            this.selectFileToUpload("data-with-crazy-footer");
            Screenshots.takeScreenshot(browser, "data-with-crazy-footer-csv-upload", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            List<String> columnNames =
                    Arrays.asList("Renewal Status", "Owner Role", "Opportunity Owner", "Account Name",
                            "Forecast Category", "Opportunity Name", "Fiscal Period",
                            "Total first year potential", "Probability (%)", "Total first year expected", "Age",
                            "Close Date", "Effective Contract End Date", "Reason for Renewal Status",
                            "Created Date", "Next Step", "Product Line", "Stage");
            List<Integer> columnIndexes =
                    Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
            uploadColumns.assertColumnsName(columnIndexes, columnNames);
            List<String> dataTypes =
                    Arrays.asList("TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT", "NUMBER", "NUMBER",
                            "NUMBER", "TEXT", "DATE", "DATE", "TEXT", "DATE", "TEXT", "TEXT", "TEXT");
            uploadColumns.assertColumnsType(columnIndexes, dataTypes);
            Screenshots.takeScreenshot(browser, "upload-definition-data-with-crazy-footer", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            Screenshots.takeScreenshot(browser, "data-with-crazy-footer-csv-upload" + "-dashboard",
                    this.getClass());
            addEmptyDashboard();
        } finally {
            List<String> datasets =
                    Arrays.asList("data-with-crazy-footer", "Date (data-with-crazy-footer_closedate)",
                            "Date (data-with-crazy-footer_createddate)",
                            "Date (data-with-crazy-footer_effectivecontractendda)");
            this.cleanDashboardAndDatasets(datasets);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"upload-delimiter", "invalid-delimiter"})
    public void uploadInvalidDelimiter() throws InterruptedException {
        try {
            this.selectFileToUpload("delimiter-invalid");
            Screenshots.takeScreenshot(browser, "check-invalid-delimiter", this.getClass());
            UploadColumns uploadColumns = upload.getUploadColumns();
            assertEquals(uploadColumns.getNumberOfColumns(), 1);
            assertEquals(uploadColumns.getColumnName(0), "Name/Value/Value2");
            assertEquals(uploadColumns.getColumnType(0), UploadColumns.OptionDataType.TEXT.getOptionLabel());
            Screenshots.takeScreenshot(browser, "upload-definition-delimiter-invalid", this.getClass());
            upload.confirmloadCsv();
            waitForElementVisible(BY_UPLOAD_DASHBOARD, browser);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            addEmptyDashboard();
        } finally {
            List<String> datasets = Arrays.asList("delimiter-invalid");
            this.cleanDashboardAndDatasets(datasets);
        }
    }
}
