package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import javax.mail.Message;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;

import static org.hamcrest.CoreMatchers.is;

public class NotificationTest extends AbstractCsvUploaderTest {

    private static final String GOODDATA_SUPPORT_URL = "https://support.gooddata.com";

    private static final String MANAGE_DATASETS_PAGE_URL = "https://%s/#s=/gdc/projects/%s|dataPage|dataSets";
    private static final String PROJECT_PAGE_URL = "https://%s/#s=/gdc/projects/%s";
    private static final String DATA_LINK_IN_EMAIL = "https://%s/data/#/projects/%s/datasets/%s";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-notification-test-" + System.currentTimeMillis();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject","enableAnalyticalDesigner"})
    public void checkNotificationForSuccessfulUpload() {
        CsvFile fileToUpload = CsvFile.PAYROLL;

        checkCsvUpload(fileToUpload, this::uploadCsv, true);
        takeScreenshot(browser,
                toScreenshotName("Upload-csv-file-to-check-successful-notification", fileToUpload.getFileName()),
                getClass());

        String datasetName = getNewDataset(fileToUpload);
        checkSuccessfulNotification(getSuccessfulNotification(), datasetName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkNotificationForFailedUpload() throws JSONException, JSONException, IOException {
        String projectId = testParams.getProjectId();
        String GoodSalesProjectID = "";
        try {
            GoodSalesProjectID = RestUtils.createProject(getRestApiClient(), projectTitle, projectTitle,
                    GOODSALES_TEMPLATE, testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                    testParams.getProjectEnvironment());
            testParams.setProjectId(GoodSalesProjectID);
            CsvFile fileToUpload = CsvFile.PAYROLL;
        
            checkCsvUpload(fileToUpload, this::uploadCsv, false);
            
            assertThat(datasetsListPage.waitForErrorMessageBar().getText(),
                    is(String.format("Failed to add data from \"%s\" due to internal error. Check your email for "
                            + "instructions or contact support.", fileToUpload.getDatasetNameOfFirstUpload())));
            takeScreenshot(browser,
                    toScreenshotName("Upload-csv-file-to-check-failed-notification", fileToUpload.getFileName()),
                    getClass());

            checkFailureNotification(getFailedNotification());
        } finally {
            testParams.setProjectId(projectId);
            if (!GoodSalesProjectID.isEmpty()) {
                RestUtils.deleteProject(getRestApiClient(), GoodSalesProjectID);
            }
        }
    }

    private void checkSuccessfulNotification(Document message, String datasetName) {
        checkGeneralNotification(message);
        String datasetUrl = String.format(DATA_LINK_IN_EMAIL, testParams.getHost(), testParams.getProjectId(),
                        getDatasetId(datasetName));
        assertThat(message.getElementsContainingText(datasetName).attr("href"), is(datasetUrl));
        
        String analysisUrl = String.format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
                getDatasetId(datasetName));
        assertThat(message.getElementsMatchingOwnText("Explore the newly added data").attr("href"), 
                is(analysisUrl));
    }
    
    private void checkFailureNotification(Document message) {
        checkGeneralNotification(message);
        String datasetManageUrl = String.format(MANAGE_DATASETS_PAGE_URL, testParams.getHost(), 
                testParams.getProjectId());
        assertThat(message.getElementsContainingText("delete the loaded file").attr("href"), is(datasetManageUrl));
    }
    
    private void checkGeneralNotification(Document message) {
        String projectUrl = String.format(PROJECT_PAGE_URL, testParams.getHost(), testParams.getProjectId());
        assertThat(message.getElementsMatchingOwnText(projectTitle).attr("href"), is(projectUrl));
        
        assertThat(message.getElementsMatchingOwnText(GOODDATA_SUPPORT_URL).attr("href"),
                is(GOODDATA_SUPPORT_URL));
    }

    private Document getNotification(String subject) {
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            Message notification = getNotification(imapClient, subject);
            return Jsoup.parse(ImapClient.getEmailBody(notification));
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when checking notification content!", e);
        }
    }

    private Document getSuccessfulNotification() {
        String subject = String.format("New data is ready to use in the %s project", projectTitle);

        return getNotification(subject);
    }

    private Document getFailedNotification() {
        String subject = String.format("Error adding new data to %s project", projectTitle);

        return getNotification(subject);
    }

    private static Message getNotification(final ImapClient imapClient, final String subject) {
        Collection<Message> notifications = ImapUtils.waitForMessage(imapClient, GDEmails.NO_REPLY, subject);
        assertTrue(notifications.size() == 1, "More than 1 notification!");

        return Iterables.getLast(notifications);
    }
}
