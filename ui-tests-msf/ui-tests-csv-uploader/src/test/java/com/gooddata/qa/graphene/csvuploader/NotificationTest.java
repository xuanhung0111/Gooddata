package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.util.List;

import javax.mail.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.collect.Iterables;

public class NotificationTest extends AbstractCsvUploaderTest {

    private static final String GOODDATA_SUPPORT_URL = "https://support.gooddata.com";

    private static final String MANAGE_DATASETS_PAGE_URL = "https://%s/#s=/gdc/projects/%s|dataPage|dataSets";
    private static final String PROJECT_PAGE_URL = "https://%s/#s=/gdc/projects/%s";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-notification-test-" + System.currentTimeMillis();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForSuccessfulUpload() {
        final String datasetName = uploadCsv(PAYROLL).getName();

        takeScreenshot(browser, "Upload-csv-file-to-check-successful-notification" + PAYROLL.getFileName(), getClass());

        checkSuccessfulNotification(getSuccessfulNotification(1), datasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForFailedUpload() {
        final String projectId = testParams.getProjectId();
        String GoodSalesProjectID = "";

        try {
            GoodSalesProjectID = ProjectRestUtils.createProject(getGoodDataClient(), projectTitle,
                    GOODSALES_TEMPLATE, testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                    testParams.getProjectEnvironment());

            testParams.setProjectId(GoodSalesProjectID);

            try {
                uploadCsv(PAYROLL);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "Uploading csv file is FAILED!");
            }

            assertEquals(DatasetMessageBar.getInstance(browser).waitForErrorMessageBar().getText(),
                    format("Failed to add data from \"%s\" due to internal error. Check your email for "
                            + "instructions or contact support.", PAYROLL.getDatasetNameOfFirstUpload()));

            takeScreenshot(browser, "Upload-csv-file-to-check-failed-notification-" + PAYROLL.getFileName(), getClass());

            checkFailureNotification(getFailedNotification(1));
        } finally {
            testParams.setProjectId(projectId);

            if (!GoodSalesProjectID.isEmpty()) {
                ProjectRestUtils.deleteProject(getGoodDataClient(), GoodSalesProjectID);
            }
        }
    }

    @Test(dependsOnMethods = {"checkNotificationForSuccessfulUpload"})
    public void checkNotificationForSuccessfulUpdate() {
        final String datasetName = PAYROLL.getDatasetNameOfFirstUpload();

        final Dataset dataset = initDataUploadPage()
            .getMyDatasetsTable()
            .getDataset(datasetName);

        updateCsvInDetailPage(PAYROLL_REFRESH, dataset, true);

        takeScreenshot(browser, "Update-csv-check-successful-notification" + PAYROLL_REFRESH.getFileName(), getClass());

        checkSuccessfulNotification(getSuccessfulNotification(2), datasetName);
    }

    private void checkSuccessfulNotification(Document message, String datasetName) {
        checkGeneralNotification(message);
        String datasetUrl = format(DATASET_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertEquals(message.getElementsContainingText(datasetName).attr("href"), datasetUrl);

        String analysisUrl = format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertEquals(message.getElementsMatchingOwnText("Explore the newly added data").attr("href"),
                analysisUrl);
    }

    private void checkFailureNotification(Document message) {
        checkGeneralNotification(message);
        String datasetManageUrl = format(MANAGE_DATASETS_PAGE_URL, testParams.getHost(),
                testParams.getProjectId());
        assertEquals(message.getElementsContainingText("delete the loaded file").attr("href"), datasetManageUrl);
    }

    private void checkGeneralNotification(Document message) {
        String projectUrl = format(PROJECT_PAGE_URL, testParams.getHost(), testParams.getProjectId());
        assertEquals(message.getElementsMatchingOwnText(projectTitle).attr("href"), projectUrl);
        assertEquals(message.getElementsMatchingOwnText(GOODDATA_SUPPORT_URL).attr("href"),
                GOODDATA_SUPPORT_URL);
    }

    private Document getNotification(String subject, int expectedMessageCount) {
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            List<Message> notifications = 
                    waitForMessages(imapClient, GDEmails.NO_REPLY, subject, expectedMessageCount);

            return Jsoup.parse(ImapClient.getEmailBody(Iterables.getLast(notifications)));

        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when checking notification content!", e);
        }
    }

    private Document getSuccessfulNotification(int expectedMessageCount) {
        String subject = format("New data is ready to use in the %s project", projectTitle);
        return getNotification(subject, expectedMessageCount);
    }

    private Document getFailedNotification(int expectedMessageCount) {
        String subject = format("Error adding new data to %s project", projectTitle);
        return getNotification(subject, expectedMessageCount);
    }
}
