package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.io.IOException;
import java.util.List;

import javax.mail.Message;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;

public class NotificationTest extends AbstractCsvUploaderTest {

    private static final String GOODDATA_SUPPORT_URL = "https://support.gooddata.com";
    private static final String PROJECT_PAGE_URL = "https://%s/#s=/gdc/projects/%s";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Csv-uploader-notification-test";

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = "createProject", groups = "precondition")
    public void inviteUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);

        logout();
        signInAtUI(imapUser, imapPassword);
    }

    @Test(dependsOnGroups = "precondition", groups = "csv")
    public void checkNotificationForSuccessfulUpload() {
        final String datasetName = uploadCsv(PAYROLL).getName();

        takeScreenshot(browser, "Upload-csv-file-to-check-successful-notification" + PAYROLL.getFileName(), getClass());

        checkSuccessfulNotification(getSuccessfulNotification(1), datasetName);
    }

    @Test(dependsOnMethods = {"checkNotificationForSuccessfulUpload"}, groups = "csv")
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

            return Jsoup.parse(ImapUtils.getEmailBody(Iterables.getLast(notifications)));

        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when checking notification content!", e);
        }
    }

    private Document getSuccessfulNotification(int expectedMessageCount) {
        String subject = format("New data is ready to use in the %s project", projectTitle);
        return getNotification(subject, expectedMessageCount);
    }
}
