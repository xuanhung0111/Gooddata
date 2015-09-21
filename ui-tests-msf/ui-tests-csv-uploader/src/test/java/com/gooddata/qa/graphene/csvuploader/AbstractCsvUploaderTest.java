package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang.WordUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadProgressDialog;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AbstractCsvUploaderTest extends AbstractMSFTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/projects/%s/datasets";

    protected static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    protected static final String DATA_PAGE_NAME = "data-page";
    protected static final String DATASET_DETAIL_PAGE_NAME = "dataset-detail";
    protected static final String DATA_PREVIEW_PAGE = "data-preview";
    protected static final String DELETE_DATASET_DIALOG_NAME = "delete-dataset-dialog";

    protected static final String SUCCESSFUL_STATUS_MESSAGE = "Data uploaded successfully";

    private static final List<String> PAYROLL_COLUMN_TYPES = Lists.newArrayList("Attribute", "Attribute",
            "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Date", "Measure");
    private static final List<String> PAYROLL_COLUMN_NAMES = Lists.newArrayList("Lastname", "Firstname",
            "Education", "Position", "Department", "State", "County", "Paydate", "Amount");
    
    protected List<UploadHistory> uploadHistory = Lists.newArrayList();

    @FindBy(className = "s-datasets-list")
    protected DatasetsListPage datasetsListPage;

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

    protected void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(datasetsListPage);
    }
    
    protected void uploadCsv(CsvFile csvFile) {
        uploadFile(csvFile);

        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, csvFile.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();
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
                contains("Failed to upload the " + csvFile.getFileName()
                        + " file. Some rows contain more column than the others. Upload a different CSV file."));

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

    protected void waitForDatasetStatus(final String datasetName, final String expectedStatusMessage) {
        Predicate<WebDriver> datasetHasSuccessfulStatus = input -> {
            final String datasetStatus =
                    waitForFragmentVisible(datasetsListPage).getMyDatasetsTable().getDatasetStatus(datasetName);
            return isNotEmpty(datasetStatus) && datasetStatus.contains(expectedStatusMessage);
        };

        Graphene.waitGui(browser)
                .withMessage("Dataset '" + datasetName + "' has incorrect status. "
                        + "Expected: '" + expectedStatusMessage
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
        PAYROLL_CHANGE_TYPE("payroll.change.column.type"),
        PAYROLL_CHECK_SYNC_DATA("payroll.sync.data"),
        PAYROLL_CHECK_NOT_SYNC_DATA("payroll.not.sync.data"),
        PAYROLL_UPLOAD_MULTIPLE_TIMES("payroll.upload.multiple.times"),
        PAYROLL_UPLOAD_AFTER_DELETE_DATASET("payroll.upload.after.delete.dataset"),
        PAYROLL_REFRESH("payroll.refresh"),
        PAYROLL_REFRESH_BAD("payroll.refresh.bad"),
        /** This csv file has incorrect column count (one more than expected) on the line number 2. */
        BAD_STRUCTURE("payroll.bad", Collections.<String>emptyList(), Collections.<String>emptyList()),
        NO_HEADER("payroll.no.header", Collections.nCopies(9, ""), PAYROLL_COLUMN_TYPES),
        TOO_LARGE_FILE("payroll.too.large");
    
        private final String name;
        private final List<String> columnNames = Lists.newArrayList();
        private final List<String> columnTypes = Lists.newArrayList();
    
        private CsvFile(String fileName) {
            this(fileName, PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES);
        }
    
        private CsvFile(String fileName, List<String> columnNames, List<String> columnTypes) {
            this.name = fileName;
            this.columnNames.addAll(columnNames);
            this.columnTypes.addAll(columnTypes);
        }
    
        public String getFileName() {
            return this.name + ".csv";
        }
    
        public String getDatasetNameOfFirstUpload() {
            String datasetName = this.name.replace(".", " ");
            return "Csv " + WordUtils.capitalize(datasetName);
        }
        
        public String getDatasetName(long datasetIndex) {
            assertThat(datasetIndex > 0, is(true));
            return getDatasetNameOfFirstUpload() + String.valueOf(datasetIndex);
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
    }
}
