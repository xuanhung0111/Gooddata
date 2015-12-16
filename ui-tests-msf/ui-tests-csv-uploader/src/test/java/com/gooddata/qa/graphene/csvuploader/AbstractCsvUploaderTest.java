package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadProgressDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.InsufficientAccessRightsPage;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AbstractCsvUploaderTest extends AbstractMSFTest {

    protected static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    protected static final String DATA_PAGE_NAME = "data-page";
    protected static final String DATASET_DETAIL_PAGE_NAME = "dataset-detail";
    protected static final String DATA_PREVIEW_PAGE = "data-preview";
    protected static final String DELETE_DATASET_DIALOG_NAME = "delete-dataset-dialog";
    protected static final String AD_REPORT_LINK = "https://%s/analyze/#/%s/reportId/edit?dataset=%s";
    protected static final String CSV_DATASET_DETAIL_PAGE_URI_TEMPLATE = DATA_UPLOAD_PAGE_URI_TEMPLATE + "/%s";
    protected static final String DATASET_LINK= "https://%s/data/#/projects/%s/datasets/%s";
    private static final String ADDING_DATA_FROM_MESSAGE = "Adding data from \"%s\" ...";
    protected static final String UPDATE_DATA_MESSAGE = "Updating data from \"%s\" ...";
    protected static final String SUCCESSFUL_DATA_MESSAGE = "Data has been loaded successfully to \"%s\". Start analyzing!";
    /**
     * Successful load contains information about number of rows and columns,
     * so status message of such load should match the following regular expression.
     */
    protected static final String SUCCESSFUL_STATUS_MESSAGE_REGEX = ".*rows.*data\\s+fields.*";

    protected static final List<String> PAYROLL_COLUMN_TYPES = Lists.newArrayList("Attribute", "Attribute",
            "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)", "Measure");
    protected static final List<String> PAYROLL_COLUMN_NAMES = Lists.newArrayList("Lastname", "Firstname",
            "Education", "Position", "Department", "State", "County", "Paydate", "Amount");

    private static final long PAYROLL_DATA_ROW_COUNT = 3876;

    protected List<UploadHistory> uploadHistory = Lists.newArrayList();

    @FindBy(className = "s-upload-dialog")
    protected FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    protected DataPreviewPage dataPreviewPage;

    @FindBy(className = "s-dataset-detail")
    protected DatasetDetailPage csvDatasetDetailPage;
    
    @FindBy(className = "gd-messages")
    protected DatasetMessageBar csvDatasetMessageBar;

    @FindBy(className = "s-progress-dialog")
    protected FileUploadProgressDialog fileUploadProgressDialog;

    @FindBy(className = "s-dataset-delete-dialog")
    protected DatasetDeleteDialog datasetDeleteDialog;

    @FindBy(className = "s-insufficient-access-rights")
    protected InsufficientAccessRightsPage insufficientAccessRightsPage;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    protected void checkCsvDatasetDetail(CsvFile csvFile, String datasetName) {
        checkCsvDatasetDetail(datasetName, csvFile.getColumnNames(), csvFile.getColumnTypes());
    }

    protected void checkCsvDatasetDetail(String datasetName, List<String> expectedColumnNames,
            List<String> expectedColumnTypes) {
        waitForFragmentVisible(csvDatasetDetailPage);

        assertThat(csvDatasetDetailPage.getDatasetName(), is(datasetName));
        assertThat(csvDatasetDetailPage.getColumnNames(), 
                containsInAnyOrder(simplifyHeaderNames(expectedColumnNames).toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
    }
    
    protected List<String> simplifyHeaderNames(List<String> columnNames) {
        return columnNames.stream()
                .map (e -> e.replaceAll("[\\,,\\-,.]", ""))
                .collect(Collectors.toList());
    }

    protected void checkCsvUpload(CsvFile csvFile, Consumer<CsvFile> uploadCsvFunction, boolean newDatasetExpected) {
        initDataUploadPage();

        final int datasetCountBeforeUpload = datasetsListPage.getMyDatasetsCount();

        uploadCsvFunction.accept(csvFile);

        waitForExpectedDatasetsCount(newDatasetExpected ? datasetCountBeforeUpload + 1 : datasetCountBeforeUpload);
    }

    protected void uploadCsv(CsvFile csvFile) {
        uploadFile(csvFile);

        takeScreenshot(browser, toScreenshotName("before-data-preview", csvFile.getFileName()), getClass());

        waitForFragmentVisible(dataPreviewPage);
        assertThat(dataPreviewPage.getRowCountMessage(), containsString(Long.toString(csvFile.getDataRowCount())));

        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, csvFile.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();
        
        assertThat(csvDatasetMessageBar.waitForProgressMessageBar().getText(),
                is(String.format(ADDING_DATA_FROM_MESSAGE, csvFile.getFileName())));
    }

    protected String getNewDataset(CsvFile csvFile) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        if(fileUpload.isPresent())
            return fileUpload.get().addDatasetName();
        uploadHistory.add(new UploadHistory(csvFile));
        return csvFile.getDatasetNameOfFirstUpload();
    }

    protected String getDatasetId(String datasetName) {
        String newDatasetId =
                "dataset.csv_"
                        + WordUtils.uncapitalize(datasetName).replace(" ", "_").replace("_(", "").replace(")", "");

        return newDatasetId;
    }

    protected void uploadFile(CsvFile csvFile) {
        waitForFragmentVisible(datasetsListPage).clickAddDataButton();

        doUploadFromDialog(csvFile);
    }

    protected void waitForExpectedDatasetsCount(final int expectedDatasetsCount) {
        Predicate<WebDriver> datasetsCountEqualsExpected = input ->
            waitForFragmentVisible(datasetsListPage).getMyDatasetsCount() == expectedDatasetsCount;

        Graphene.waitGui(browser)
                .withMessage("Dataset count <" + waitForFragmentVisible(datasetsListPage).getMyDatasetsCount()
                        + "> in the dataset list doesn't match expected value <" + expectedDatasetsCount + ">.")
                .until(datasetsCountEqualsExpected);
    }

    protected void waitForDatasetName(final String expectedDatasetName) {
        Predicate<WebDriver> datasetHasExpectedName = input -> {
            final List<String> datasetNames = waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetNames();
            return datasetNames.contains(expectedDatasetName);
        };

        Graphene.waitGui(browser)
                .withMessage(String.format("Expected dataset in list is missing. Expected: %s, but was: %s.",
                        expectedDatasetName, datasetsListPage.getMyDatasetsTable().getDatasetNames()))
                .until(datasetHasExpectedName);
    }

    protected void waitForDatasetStatus(final String datasetName, final String expectedStatusMessageRegex) {
        Predicate<WebDriver> datasetHasSuccessfulStatus = input -> {
            final String datasetStatus =
                    waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetStatus(datasetName);
            return isNotEmpty(datasetStatus) && datasetStatus.matches(expectedStatusMessageRegex);
        };

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + datasetName + "' has incorrect status. "
                        + "Expected: '" + expectedStatusMessageRegex
                        + "', but was: '" + datasetsListPage.getMyDatasetsTable().getDatasetStatus(datasetName)
                        + "'.")
                .until(datasetHasSuccessfulStatus);
    }

    protected void doUploadFromDialog(CsvFile csvFile) {
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "initial-state", csvFile.getFileName()), getClass());

        waitForFragmentVisible(fileUploadDialog).pickCsvFile(csvFile.getCsvFileToUpload());

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "csv-file-picked", csvFile.getFileName()), getClass());

        fileUploadDialog.clickUploadButton();

        waitForFragmentVisible(fileUploadProgressDialog);
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "upload-in-progress", csvFile.getFileName()), getClass());
    }
    
    protected void refreshCsv(CsvFile refreshData, String datasetName, boolean isOwner) {
        doUploadFromDialog(refreshData);

        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, refreshData.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();
        
        assertThat(csvDatasetMessageBar.waitForProgressMessageBar().getText(),
                is(String.format(UPDATE_DATA_MESSAGE, refreshData.getFileName())));
        
        // TODO workaround for bug: MSF-9563 Green message isn't shown after updating successfully by other admin
        if (isOwner) {
            assertThat(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    is(String.format(SUCCESSFUL_DATA_MESSAGE, datasetName)));
        }
    }

    public class UploadHistory {
        private CsvFile csvFile;
        private Map<String, Boolean> datasetNames = new HashMap<>();

        public UploadHistory(CsvFile csvFile) {
            this.csvFile = csvFile;
            datasetNames.put(csvFile.getDatasetNameOfFirstUpload(), true);
        }

        public CsvFile getCsvFile() {
            return csvFile;
        }

        public String addDatasetName() {
            for (Entry<String, Boolean> entry : datasetNames.entrySet()) {
                if (!entry.getValue()) {
                    entry.setValue(true);
                    return entry.getKey();
                }
            }

            String newDatasetName = csvFile.getDatasetName(getUploadTime());
            datasetNames.put(newDatasetName, true);
            return newDatasetName;
        }

        public long getUploadTime() {
            return datasetNames.size();
        }

        public void removeDatasetName(String datasetName) {
            for (Entry<String, Boolean> entry : datasetNames.entrySet()) {
                if (entry.getKey().equals(datasetName)) {
                    entry.setValue(false);
                }
            }
        }
    }

    protected enum CsvFile {
        PAYROLL("payroll"),
        PAYROLL_REFRESH("payroll.refresh"),
        PAYROLL_REFRESH_BAD("payroll.refresh.bad"),
        MULTIPLE_COLUMN_NAME_ROWS("multiple.column.name.rows", Lists.newArrayList("Id4", "Name4", "Lastname4",
                "Age4", "Amount4"), Lists.newArrayList("Measure", "Attribute", "Attribute", "Measure", "Measure"), 5),
        /** This csv file has incorrect column count (one more than expected) on the line number 2. */
        BAD_STRUCTURE("payroll.bad", Collections.<String>emptyList(), Collections.<String>emptyList(), 0),
        NO_HEADER("payroll.no.header", Collections.nCopies(9, ""), PAYROLL_COLUMN_TYPES, PAYROLL_DATA_ROW_COUNT),
        TOO_LARGE_FILE("payroll.too.large"),
        WITHOUT_FACT("payroll.without.fact"),
        INVALID_DELIMITER("invalid.delimiter"),
        CRAZY_DATA("crazy.data"),
        TOO_MANY_COLUMNS("too.many.columns"),
        TOO_LONG_FIELD("payroll.too.long.field"),
        PAYROLL_TOO_LONG_FACT_VALUE("payroll.too.long.fact.value"),
        PAYROLL_NEGATIVE_NUMBER("payroll.negative.number", PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES, 3568),
        PAYROLL_NULL_NUMBER("payroll.null.number", Lists.newArrayList("Lastname", "Firstname",
                "Education", "Position", "Department", "State", "County", "Paydate", "Amount", "Amount1"),
                Lists.newArrayList("Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                        "Attribute", "Date [2015-12-31]", "Measure", "Measure"), PAYROLL_DATA_ROW_COUNT),
        WITHOUT_ATTRIBUTE("without.attribute", Lists.newArrayList("Amount"), Lists.newArrayList("Measure"), 44),
        WITHOUT_DATE("without.date", Lists.newArrayList("state","county","name","censusarea"), 
                Lists.newArrayList("Attribute", "Attribute", "Attribute", "Measure"), 3273);

        private final String name;
        private final List<String> columnNames = Lists.newArrayList();
        private final List<String> columnTypes = Lists.newArrayList();
        //number of rows with data (rows with facts)
        private final long dataRowCount;
    
        private CsvFile(String fileName) {
            this(fileName, PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES, PAYROLL_DATA_ROW_COUNT);
        }
    
        private CsvFile(String fileName, List<String> columnNames, List<String> columnTypes, long dataRowCount) {
            this.name = fileName;
            this.columnNames.addAll(columnNames);
            this.columnTypes.addAll(columnTypes);
            this.dataRowCount = dataRowCount;
        }
    
        public String getFileName() {
            return this.name + ".csv";
        }

        public String getDatasetNameOfFirstUpload() {
            String datasetName = this.name.replace(".", " ");
            return WordUtils.capitalize(datasetName);
        }
        
        public String getDatasetName(long datasetIndex) {
            assertThat(datasetIndex > 0, is(true));
            return String.format("%s (%s)", getDatasetNameOfFirstUpload(), datasetIndex);
        }
    
        public List<String> getColumnNames() {
            return Collections.unmodifiableList(this.columnNames);
        }
        
        public List<String> changeColumnType(String columnName, ColumnType type) {
            int columnIndex = getColumnNames().indexOf(columnName);
            List<String> changedColumnTypes = Lists.newArrayList(this.columnTypes);
            changedColumnTypes.set(columnIndex, type.getVisibleText());
            return changedColumnTypes;
        }
    
        public List<String> getColumnTypes() {
            return Collections.unmodifiableList(this.columnTypes);
        }

        public String getCsvFileToUpload() {
            return ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + this.getFileName());
        }

        public long getDataRowCount() {
            return dataRowCount;
        }
    }
}
