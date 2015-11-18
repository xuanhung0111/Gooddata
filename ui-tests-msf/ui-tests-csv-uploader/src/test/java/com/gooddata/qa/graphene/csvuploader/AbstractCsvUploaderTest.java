package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.hasItem;
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

import org.apache.commons.lang.WordUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadProgressDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.InsufficientAccessRightsPage;
import com.gooddata.qa.utils.http.RestUtils;
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
    private static final String ADDING_DATA_FROM_MESSAGE = "Adding data from \"%s\" ...";
    /**
     * Successful load contains information about number of rows and columns,
     * so status message of such load should match the following regular expression.
     */
    protected static final String SUCCESSFUL_STATUS_MESSAGE_REGEX = ".*rows.*data\\s+fields.*";

    protected static final List<String> PAYROLL_COLUMN_TYPES = Lists.newArrayList("Attribute", "Attribute",
            "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date", "Measure");
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

    @FindBy(className = "s-progress-dialog")
    protected FileUploadProgressDialog fileUploadProgressDialog;

    @FindBy(className = "s-dataset-delete-dialog")
    protected DatasetDeleteDialog datasetDeleteDialog;

    @FindBy(className = "s-insufficient-access-rights")
    protected InsufficientAccessRightsPage insufficientAccessRightsPage;

    //TODO remove enabling AD feature flag when the AD is enabled by default
    // this temporarily solves the problem that AD is not enabled for new users and on new PIs
    @Test(alwaysRun = true, dependsOnMethods = {"createProject"})
    public void enableAnalyticalDesigner() throws JSONException {
        setADFeatureFlag(true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownCsvUploaderTest() throws JSONException {
        // the AD feature flag should be always disabled for the current project to cover test runs which are reusing
        // existing projects
        setADFeatureFlag(false);
    }

    protected void checkDataPreview(CsvFile csvFile) {
        checkDataPreview(csvFile.getColumnNames(), csvFile.getColumnTypes());
    }

    protected void checkDataPreview(List<String> expectedColumnNames, List<String> expectedColumnTypes) {
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();

        assertThat(dataPreviewTable.getColumnNames(), containsInAnyOrder(expectedColumnNames.toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
    }

    protected void checkCsvDatasetDetail(CsvFile csvFile, String datasetName) {
        checkCsvDatasetDetail(datasetName, csvFile.getColumnNames(), csvFile.getColumnTypes());
    }

    protected void checkCsvDatasetDetail(String datasetName, List<String> expectedColumnNames,
            List<String> expectedColumnTypes) {
        waitForFragmentVisible(csvDatasetDetailPage);

        assertThat(csvDatasetDetailPage.getDatasetName(), is(datasetName));
        assertThat(csvDatasetDetailPage.getColumnNames(), containsInAnyOrder(expectedColumnNames.toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
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
        
        assertThat(datasetsListPage.waitForProgressMessageBar().getText(),
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
    
    protected void removeDataset(CsvFile csvFile, String datasetName) {
        final int datasetCountBeforeDelete = datasetsListPage.getMyDatasetsCount();

        datasetsListPage.getMyDatasetsTable().getDatasetDeleteButton(datasetName).click();
        takeScreenshot(browser, DELETE_DATASET_DIALOG_NAME, getClass());
        waitForFragmentVisible(datasetDeleteDialog).clickDelete();

        waitForExpectedDatasetsCount(datasetCountBeforeDelete - 1);
    }

    protected void removeDatasetFromUploadHistory(CsvFile csvFile, String datasetName) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        assertThat(fileUpload.isPresent(), is(true));
        fileUpload.get().removeDatasetName(datasetName);
    }

    protected void uploadBadCsv(CsvFile csvFile) {
        uploadFile(csvFile);

        // the processing should not go any further but display validation error directly in File Upload Dialog
        assertThat(waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors(),
                hasItem(createErrorFailedToUploadFile(csvFile)));

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "validation-errors", csvFile.getFileName()), getClass());

        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
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

    protected void checkForDatasetRemoved(final String csvDatasetName) {
        Predicate<WebDriver> datasetSuccessfullyRemoved = input ->
                waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetRow(csvDatasetName) == null;

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + csvDatasetName + "' has not been removed from the dataset list.")
                .until(datasetSuccessfullyRemoved);
    }

    protected void doUploadFromDialog(CsvFile csvFile) {
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "initial-state", csvFile.getFileName()), getClass());

        waitForFragmentVisible(fileUploadDialog).pickCsvFile(csvFile.getCsvFileToUpload());

        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "csv-file-picked", csvFile.getFileName()), getClass());

        fileUploadDialog.clickUploadButton();

        waitForFragmentVisible(fileUploadProgressDialog);
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "upload-in-progress", csvFile.getFileName()), getClass());
    }

    /**
     * Sets the Analytical Designer feature flag to be turned on/off for the current project.
     */
    private void setADFeatureFlag(boolean isEnabled) throws JSONException {
        RestUtils.setFeatureFlagsToProject(getRestApiClient(), testParams.getProjectId(),
                RestUtils.FeatureFlagOption.createFeatureClassOption(
                        ProjectFeatureFlags.ANALYTICAL_DESIGNER.getFlagName(), isEnabled));
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

    protected String createErrorFailedToUploadFile(CsvFile csvFile) {
        return String.format("Failed to upload the \"%s\" file.", csvFile.getFileName());
    }

    protected enum CsvFile {
        PAYROLL("payroll"),
        PAYROLL_CHANGE_TYPE("payroll.change.column.type"),
        PAYROLL_CHECK_SYNC_DATA("payroll.sync.data"),
        PAYROLL_CHECK_NOT_SYNC_DATA("payroll.not.sync.data"),
        PAYROLL_UPLOAD_MULTIPLE_TIMES("payroll.upload.multiple.times"),
        PAYROLL_UPLOAD_AFTER_DELETE_DATASET("payroll.upload.after.delete.dataset"),
        PAYROLL_REFRESH("payroll.refresh"),
        PAYROLL_REFRESH_BAD("payroll.refresh.bad"),
        PAYROLL_BY_PROJECT_OWNER("payroll.by.project.owner"),
        PAYROLL_BY_OTHER_ADMIN("payroll.by.other.admin"),
        MULTIPLE_COLUMN_NAME_ROWS("multiple.column.name.rows", Lists.newArrayList("Id4", "Name4", "Lastname4",
                "Age4", "Amount4"), Lists.newArrayList(Lists.newArrayList("Measure", "Attribute", "Attribute",
                "Measure", "Measure")), 5),
        /** This csv file has incorrect column count (one more than expected) on the line number 2. */
        BAD_STRUCTURE("payroll.bad", Collections.<String>emptyList(), Collections.<String>emptyList(), 0),
        NO_HEADER("payroll.no.header", Collections.nCopies(9, ""), PAYROLL_COLUMN_TYPES, PAYROLL_DATA_ROW_COUNT),
        TOO_LARGE_FILE("payroll.too.large"),
        WITHOUT_FACT("payroll.without.fact"),
        CRAZY_DATA("crazy.data"),
        TOO_MANY_COLUMNS("too.many.columns"),
        TOO_LONG_FIELD("payroll.too.long.field"),
        PAYROLL_TOO_LONG_FACT_VALUE("payroll.too.long.fact.value");

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
