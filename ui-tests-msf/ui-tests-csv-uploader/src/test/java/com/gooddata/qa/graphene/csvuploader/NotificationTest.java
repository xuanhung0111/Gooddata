package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import javax.mail.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
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
        checkCsvUpload(PAYROLL, this::uploadCsv, true);
        takeScreenshot(browser,
                toScreenshotName("Upload-csv-file-to-check-successful-notification", PAYROLL.getFileName()),
                getClass());

        String datasetName = getNewDataset(PAYROLL);
        checkSuccessfulNotification(getSuccessfulNotification(1), datasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForFailedUpload() {
        String projectId = testParams.getProjectId();
        String GoodSalesProjectID = "";
        try {
            GoodSalesProjectID = ProjectRestUtils.createProject(getGoodDataClient(), projectTitle,
                    GOODSALES_TEMPLATE, testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                    testParams.getProjectEnvironment());
            testParams.setProjectId(GoodSalesProjectID);

            checkCsvUpload(PAYROLL, this::uploadCsv, false);

            assertEquals(csvDatasetMessageBar.waitForErrorMessageBar().getText(),
                    format("Failed to add data from \"%s\" due to internal error. Check your email for "
                            + "instructions or contact support.", PAYROLL.getDatasetNameOfFirstUpload()));
            takeScreenshot(browser,
                    toScreenshotName("Upload-csv-file-to-check-failed-notification", PAYROLL.getFileName()),
                    getClass());

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
        initDataUploadPage();

        String datasetName = PAYROLL.getDatasetNameOfFirstUpload();
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();

        waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();

        refreshCsv(PAYROLL_REFRESH, datasetName, true);
        takeScreenshot(browser,
                toScreenshotName("Update-csv-file-to-check-successful-notification", PAYROLL_REFRESH.getFileName()),
                getClass());

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
            Message notification = getNotification(imapClient, subject, expectedMessageCount);
            return Jsoup.parse(ImapClient.getEmailBody(notification));
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

    private static Message getNotification(final ImapClient imapClient, final String subject, int exptectedMessageCount) {
        Collection<Message> notifications = ImapUtils.waitForMessage(imapClient, GDEmails.NO_REPLY, subject);
        assertTrue(notifications.size() == exptectedMessageCount, "Number of notifcation is wrong!");
        return Iterables.getLast(notifications);
    }
}
