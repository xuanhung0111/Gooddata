package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDeleteDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.fragments.csvuploader.InsufficientAccessRightsPage;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AbstractCsvUploaderTest extends AbstractMSFTest {

    protected static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    protected static final String DATA_PAGE_NAME = "data-page";
    protected static final String DATASET_DETAIL_PAGE_NAME = "dataset-detail";
    protected static final String DATA_PREVIEW_PAGE = "data-preview";
    protected static final String AD_REPORT_LINK = "https://%s/analyze/#/%s/reportId/edit?dataset=%s";
    protected static final String CSV_DATASET_DETAIL_PAGE_URI_TEMPLATE = DATA_UPLOAD_PAGE_URI_TEMPLATE + "/%s";
    protected static final String DATASET_LINK = "https://%s/data/#/projects/%s/datasets/%s";
    protected static final String SUCCESSFUL_DATA_MESSAGE = "Data has been loaded successfully to \"%s\". Start analyzing!";
    /**
     * Successful load contains information about number of rows and columns,
     * so status message of such load should match the following regular expression.
     */
    protected static final String SUCCESSFUL_STATUS_MESSAGE_REGEX = "\\d+ row[s]?, \\d+ data field[s]?";

    protected static final String[] PAYROLL_COLUMN_TYPES = {"Attribute", "Attribute", "Attribute", "Attribute",
        "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)", "Measure"};

    protected static final CsvFile PAYROLL = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"))
            .setColumnTypes(PAYROLL_COLUMN_TYPES);

    protected static final CsvFile PAYROLL_REFRESH = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.refresh.csv"))
            .setColumnTypes(PAYROLL_COLUMN_TYPES);

    protected List<UploadHistory> uploadHistory = Lists.newArrayList();

    @FindBy(className = "s-upload-dialog")
    protected FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    protected DataPreviewPage dataPreviewPage;

    @FindBy(className = "s-dataset-detail")
    protected DatasetDetailPage csvDatasetDetailPage;

    @FindBy(className = "gd-messages")
    protected DatasetMessageBar csvDatasetMessageBar;

    @FindBy(className = "s-dataset-delete-dialog")
    protected DatasetDeleteDialog datasetDeleteDialog;

    @FindBy(className = "s-insufficient-access-rights")
    protected InsufficientAccessRightsPage insufficientAccessRightsPage;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    public static final List<String> simplifyHeaderNames(List<String> columnNames) {
        return columnNames.stream()
                .map(e -> e.replaceAll("[\\,,\\-,.]", ""))
                .collect(toList());
    }

    public static final String getDatasetId(String datasetName) {
        return "dataset.csv_" + WordUtils.uncapitalize(datasetName).replace(" ", "_").replace("_(", "").replace(")", "");
    }

    protected void checkCsvDatasetDetail(String datasetName, List<String> expectedColumnNames,
                                         List<String> expectedColumnTypes) {
        assertEquals(waitForFragmentVisible(csvDatasetDetailPage).getDatasetName(), datasetName);
        assertThat(csvDatasetDetailPage.getColumnNames(),
                containsInAnyOrder(simplifyHeaderNames(expectedColumnNames).toArray()));
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

        assertThat(waitForFragmentVisible(dataPreviewPage).getRowCountMessage(),
                containsString(Long.toString(csvFile.getDataRowCount())));
        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, csvFile.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();
    }

    protected String getNewDataset(CsvFile csvFile) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        if (fileUpload.isPresent())
            return fileUpload.get().addDatasetName();
        uploadHistory.add(new UploadHistory(csvFile));
        return csvFile.getDatasetNameOfFirstUpload();
    }

    protected void removeDatasetFromUploadHistory(CsvFile csvFile, String datasetName) {
        Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        assertTrue(fileUpload.isPresent());
        fileUpload.get().removeDatasetName(datasetName);
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
        waitForFragmentVisible(fileUploadDialog).pickCsvFile(csvFile.getFilePath());
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "csv-file-picked", csvFile.getFileName()), getClass());
        fileUploadDialog.clickUploadButton();
        takeScreenshot(browser, toScreenshotName(UPLOAD_DIALOG_NAME, "upload-in-progress", csvFile.getFileName()), getClass());
        if (!isElementPresent(className("s-progress-dialog"), browser)) {
            log.warning("Progress dialog is not show or graphene is too slow to capture it!");
        }
    }

    protected void refreshCsv(CsvFile refreshData, String datasetName, boolean isOwner) {
        doUploadFromDialog(refreshData);
        waitForFragmentVisible(dataPreviewPage);
        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, refreshData.getFileName()), getClass());
        dataPreviewPage.triggerIntegration();

        // TODO workaround for bug: MSF-9563 Green message isn't shown after updating successfully by other admin
        if (isOwner) {
            assertEquals(csvDatasetMessageBar.waitForSuccessMessageBar().getText(),
                    format(SUCCESSFUL_DATA_MESSAGE, datasetName));
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
}
