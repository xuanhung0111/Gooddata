package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.WordUtils;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.google.common.collect.Lists;

public class AbstractCsvUploaderTest extends AbstractProjectTest {

    protected static final String DATASET_DETAIL_PAGE_NAME = "dataset-detail";
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

    protected static final String[] PAYROLL_LESS_COLUMNS_COLUMN_TYPES = {"Attribute", "Attribute",
            "Date (Year-Month-Day)", "Measure"};

    protected static final CsvFile PAYROLL_LESS_COLUMNS = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.less.columns.csv"))
            .setColumnTypes(PAYROLL_LESS_COLUMNS_COLUMN_TYPES);

    protected static final CsvFile PAYROLL_REFRESH = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.refresh.csv"))
            .setColumnTypes(PAYROLL_COLUMN_TYPES);

    protected List<UploadHistory> uploadHistory = Lists.newArrayList();

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    public Dataset uploadCsv(CsvFile csvFile) {
        final int datasetCountBeforeUpload = initDataUploadPage().getMyDatasetsCount();

        DatasetsListPage.getInstance(browser).uploadFile(csvFile.getFilePath())
            .triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        if (DatasetsListPage.getInstance(browser).getMyDatasetsCount() == datasetCountBeforeUpload) {
            throw new RuntimeException("Uploading csv file is FAILED!");
        }

        return DatasetsListPage.getInstance(browser).getMyDatasetsTable().getDataset(getNewDataset(csvFile));
    }

    public DatasetDetailPage updateCsvInDetailPage(CsvFile updateData, Dataset dataset, boolean isOwner) {
        final DatasetDetailPage page;
        if (!isElementPresent(DatasetDetailPage.LOCATOR, browser)) {
            page = dataset.openDetailPage();
        } else {
            page = DatasetDetailPage.getInstance(browser);
        }

        final String datasetName = page.getDatasetName();

        page.updateCsv(updateData.getFilePath())
            .triggerIntegration();
        DatasetDetailPage.waitForProgressItemLoaded(browser);

        // TODO workaround for bug: MSF-9563 Green message isn't shown after updating successfully by other admin
        if (isOwner) {
            assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                    format(SUCCESSFUL_DATA_MESSAGE, datasetName));
        }

        return page;
    }

    public void updateCsv(CsvFile updateData, String datasetName, boolean isOwner) {
        updateCsvDataset(datasetName, updateData.getFilePath());

        // TODO workaround for bug: MSF-9563 Green message isn't shown after updating successfully by other admin
        if (isOwner) {
            assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                    format(SUCCESSFUL_DATA_MESSAGE, datasetName));
        }
    }

    public static final List<String> simplifyHeaderNames(List<String> columnNames) {
        return columnNames.stream()
                .map(e -> e.replaceAll("[\\,,\\-,.]", ""))
                .collect(toList());
    }

    public static final String getDatasetId(String datasetName) {
        return "dataset.csv_" + WordUtils.uncapitalize(datasetName).replace(" ", "_").replace("_(", "").replace(")", "");
    }

    public String getNewDataset(CsvFile csvFile) {
        final Optional<UploadHistory> fileUpload = uploadHistory.stream()
                .filter(upload -> upload.getCsvFile() == csvFile)
                .findAny();
        if (fileUpload.isPresent())
            return fileUpload.get().addDatasetName();
        uploadHistory.add(new UploadHistory(csvFile));
        return csvFile.getDatasetNameOfFirstUpload();
    }

    public void checkCsvDatasetDetail(String datasetName, List<String> expectedColumnNames,
                                         List<String> expectedColumnTypes) {
        final DatasetDetailPage page = DatasetDetailPage.getInstance(browser);

        assertEquals(page.getDatasetName(), datasetName);
        assertThat(page.getColumnNames(), containsInAnyOrder(simplifyHeaderNames(expectedColumnNames).toArray()));
        assertThat(page.getColumnTypes(), containsInAnyOrder(expectedColumnTypes.toArray()));
    }

    public void removeDatasetFromUploadHistory(final CsvFile csvFile, final String datasetName) {
        uploadHistory.stream()
            .filter(upload -> upload.getCsvFile() == csvFile)
            .findAny()
            .get()
            .removeDatasetName(datasetName);
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
