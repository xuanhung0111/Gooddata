package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.google.common.base.Predicate;

public class DatasetDetailTest extends AbstractCsvUploaderTest {

    private static String PAYROLL_DATASET_NAME = PAYROLL.getDatasetNameOfFirstUpload();
    private static final long PAYROLL_FILE_SIZE_MINIMUM = 476L;

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvUploadHappyPath() {
        assertTrue(uploadCsv(PAYROLL)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        final DatasetDetailPage datasetDetailPage = openDatasetDetailsPage();
        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(PAYROLL_DATASET_NAME, PAYROLL.getColumnNames(), PAYROLL.getColumnTypes());

        datasetDetailPage.clickBackButton();
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkDatasetDetailPage() {
        final DatasetDetailPage datasetDetailPage = openDatasetDetailsPage().downloadTheLatestCsvFileUpload();

        final File downloadedCsvFile = new File(testParams.getDownloadFolder(), PAYROLL.getFileName());

        final Predicate<WebDriver> fileDownloadComplete = browser ->
            downloadedCsvFile.length() > PAYROLL_FILE_SIZE_MINIMUM;
        Graphene.waitGui()
            .withTimeout(3, TimeUnit.MINUTES)
            .pollingEvery(10, TimeUnit.SECONDS)
            .until(fileDownloadComplete);

        log.info("Download file size: " + downloadedCsvFile.length());
        log.info("Download file path: " + downloadedCsvFile.getPath());
        log.info("Download file name: " + downloadedCsvFile.getName());

        final String createdDateTime = datasetDetailPage.getCreatedDateTime().replaceAll("Created by.*on\\s+", "");

        try {
            DateTimeFormatter.ofPattern("'Today at' h:mm a").parse(createdDateTime);
        } catch (DateTimeParseException e) {
            fail("Incorrect format of created date time: " + createdDateTime);
        }
    }

    private DatasetDetailPage openDatasetDetailsPage() {
        initDataUploadPage();
        return datasetsListPage.openDatasetDetailPage(PAYROLL_DATASET_NAME);
    }
}
