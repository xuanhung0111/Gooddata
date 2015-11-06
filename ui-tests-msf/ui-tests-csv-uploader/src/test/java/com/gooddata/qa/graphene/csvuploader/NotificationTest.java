package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.function.Consumer;

import javax.mail.Message;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;

import static org.hamcrest.CoreMatchers.is;

public class NotificationTest extends AbstractCsvUploaderTest {

    private static final String GOODDATA_SUPPORT_URL = "https://support.gooddata.com";

    private static final String MANAGE_DATASETS_PAGE_URL = "https://%s/#s=/gdc/projects/%s|dataPage|dataSets";
    private static final String AD_REPORT_LINK = "https://%s/analyze/#/%s/reportId/edit";
    private static final String PROJECT_PAGE_URL = "https://%s/#s=/gdc/projects/%s";
    private static final String DATA_SECTION_PAGE_URL = "https://%s/data/#/projects/%s/datasets";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-notification-test-" + System.currentTimeMillis();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForSuccessfulUpload() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        takeScreenshot(browser,
                toScreenshotName("Upload-csv-file-to-check-successful-notification", fileToUpload.getFileName()),
                getClass());

        checkNotification(getSuccessfulNotification(), this::checkSuccessfulNotification);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForFailedUpload() throws JSONException {
        CsvFile fileToUpload = CsvFile.PAYROLL_TOO_LONG_FACT_VALUE;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        takeScreenshot(browser,
                toScreenshotName("Upload-csv-file-to-check-failed-notification", fileToUpload.getFileName()),
                getClass());

        checkNotification(getFailedNotification(), this::checkFailedNotification);
    }

    private void checkNotification(Document message, Consumer<Document> checkContent) {
        String datasetUrl = String.format(DATA_SECTION_PAGE_URL, testParams.getHost(), testParams.getProjectId());
        assertThat(message.getElementsContainingText(".csv").attr("href"), is(datasetUrl));

        String projectUrl = String.format(PROJECT_PAGE_URL, testParams.getHost(), testParams.getProjectId());
        assertThat(message.getElementsMatchingOwnText(projectTitle).attr("href"), is(projectUrl));

        assertThat(message.getElementsMatchingOwnText(GOODDATA_SUPPORT_URL).attr("href"), is(GOODDATA_SUPPORT_URL));

        checkContent.accept(message);
    }

    private Document getNotification(String subject) {
        Document message = null;
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            Message notification = getNotification(imapClient, subject);
            message = Jsoup.parse(ImapClient.getEmailBody(notification));
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when checking notification content!", e);
        } finally {
            imapClient.close();
        }
        return message;
    }

    private Document getSuccessfulNotification() {
        String subject = String.format("New data is ready to use in the %s project", projectTitle);

        return getNotification(subject);
    }

    private Document getFailedNotification() {
        String subject = String.format("Error adding new data to %s project", projectTitle);

        return getNotification(subject);
    }

    private void checkSuccessfulNotification(Document message) {
        String emptyReportUrl = String.format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId());
        assertThat(message.getElementsMatchingOwnText("Explore the newly added data").attr("href"),
                is(emptyReportUrl));
    }

    private void checkFailedNotification(Document message) {
        String datasetsPageUrl =
                String.format(MANAGE_DATASETS_PAGE_URL, testParams.getHost(), testParams.getProjectId());
        assertThat(message.getElementsMatchingOwnText("delete the uploaded file").attr("href"),
                is(datasetsPageUrl));
    }

    private static Message getNotification(final ImapClient imapClient, final String subject) {
        Collection<Message> notifications = ImapUtils.waitForMessage(imapClient, GDEmails.NO_REPLY, subject);
        assertTrue(notifications.size() == 1, "More than 1 notification!");

        return Iterables.getLast(notifications);
    }
}
