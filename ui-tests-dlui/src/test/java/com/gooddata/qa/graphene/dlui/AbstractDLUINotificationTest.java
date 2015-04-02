package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import com.beust.jcommander.internal.Lists;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class AbstractDLUINotificationTest extends AbstractAnnieDialogTest {

    private static final String FAILED_DATA_ADDING_NOTIFICATION_SUBJECT =
            "Error adding new data to %s project";
    private static final String SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT =
            "New data is ready to use in the %s project";

    private static final String FROM = "no-reply@gooddata.com";

    private static final String GD_PROJECT_LINK = "https://%s/#s=/gdc/projects/%s";
    private static final String GD_SUPPORT_LINK = "https://support.gooddata.com";

    protected String imapEditorUser;
    protected String imapEditorPassword;

    protected void checkSuccessfulDataAddingEmail(long requestTime, String... fieldNames) {
        String subject =
                String.format(SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT, getWorkingProject()
                        .getProjectName());
        assertSuccessDataAddingEmailContent(getEmailOfAdminUser(subject, requestTime), fieldNames);
    }

    protected void checkSuccessfulDataAddingEmailForEditor(long requestTime, String... fieldNames) {
        String subject =
                String.format(SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT, getWorkingProject()
                        .getProjectName());
        assertSuccessDataAddingEmailContent(getEmailOfEditorUser(subject, requestTime), fieldNames);
    }

    protected void checkFailedDataAddingEmail(long requestTime, String... fieldNames) {
        String subject =
                String.format(FAILED_DATA_ADDING_NOTIFICATION_SUBJECT, getWorkingProject()
                        .getProjectName());
        assertFailedDataAddingEmailContent(getEmailOfAdminUser(subject, requestTime), fieldNames);
    }

    protected void checkFailedDataAddingEmailForEditor(long requestTime, String... fieldNames) {
        String subject =
                String.format(FAILED_DATA_ADDING_NOTIFICATION_SUBJECT, getWorkingProject()
                        .getProjectName());
        assertFailedDataAddingEmailContentForEditor(getEmailOfEditorUser(subject, requestTime),
                fieldNames);
    }

    private Document getEmailOfEditorUser(String subject, long requestTime) {
        ImapClient imapClient = new ImapClient(imapHost, imapEditorUser, imapEditorPassword);
        return getEmailContent(subject, requestTime, imapClient);
    }

    private Document getEmailOfAdminUser(String subject, long requestTime) {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        return getEmailContent(subject, requestTime, imapClient);
    }

    private Document getEmailContent(String subject, long requestTime, ImapClient imapClient) {
        try {
            System.out.println("Waiting for notification...");
            Message notification = getNotification(imapClient, subject, requestTime);
            Document message = Jsoup.parse(ImapClient.getEmailBody(notification));
            System.out.println("Time request: " + notification.getReceivedDate().getTime());
            return message;
        } catch (Exception e) {
            throw new IllegalStateException("There is an execption when getting email!", e);
        } finally {
            imapClient.close();
        }
    }

    private void assertSuccessDataAddingEmailContent(Document message, String... fieldNames) {
        String exploreNewData = "Explore the newly added data.";
        String requestTimeText = "You requested the fields at ";
        String expectedExploreNewDataLink =
                String.format(GD_PROJECT_LINK + "|analysisPage|empty-report|empty-report",
                        testParams.getHost(), getWorkingProject().getProjectId());

        assertEquals(message.getElementsContainingOwnText(exploreNewData).attr("href"),
                expectedExploreNewDataLink, "Incorrect empty report link in email content!");
        assertEquals(message.getElementsContainingOwnText(getWorkingProject().getProjectName())
                .attr("href"), getWorkingProjectLink(), "Incorrect project link in email content!");
        String requestTimeMessage = message.getElementsContainingOwnText(requestTimeText).text();
        assertTrue(isThisDateValid(requestTimeMessage.replace(requestTimeText, "")),
                "Invalid time format: " + requestTimeMessage);
        assertEquals(message.getElementsContainingOwnText(GD_SUPPORT_LINK).attr("href"),
                GD_SUPPORT_LINK, "Incorrect support link in email content!");

        List<Element> fieldNameElements = message.getElementsByTag("li");
        assertEquals(fieldNameElements.size(), fieldNames.length, "Incorrect number of fields");
        checkFieldListInEmail(fieldNameElements, fieldNames);
    }

    private void assertFailedDataAddingEmailContent(Document message, String... fieldNames) {
        String emailUser = testParams.getUser();
        String gdSupportLinkTitle = "GoodData Customer Support";

        assertNotNull(message.getElementsContainingOwnText(emailUser));
        assertEquals(message.getElementsContainingOwnText(getWorkingProject().getProjectName())
                .attr("href"), getWorkingProjectLink(), "Incorrect project link in email content!");
        RestUtils.verifyValidLink(getRestApiClient(),
                message.getElementsContainingOwnText("execution log").attr("href"));
        assertEquals(message.getElementsContainingOwnText(gdSupportLinkTitle).attr("href"),
                GD_SUPPORT_LINK);

        List<Element> fieldNameElements = message.getElementsByTag("li");
        assertEquals(fieldNameElements.size(), fieldNames.length, "Incorrect number of fields");
        checkFieldListInEmail(fieldNameElements, fieldNames);
    }

    private void assertFailedDataAddingEmailContentForEditor(Document message, String... fieldNames) {
        String GDSupportLink = "https://support.gooddata.com";

        assertEquals(message.getElementsContainingOwnText(getWorkingProject().getProjectName())
                .attr("href"), getWorkingProjectLink(), "Incorrect project link in email content!");
        assertTrue(message.getElementsContainingOwnText("execution log").isEmpty());
        assertEquals(message.getElementsContainingOwnText(GDSupportLink).attr("href"),
                "https://support.gooddata.com");

        List<Element> fieldNameElements = message.getElementsByTag("li");
        assertEquals(fieldNameElements.size(), fieldNames.length, "Incorrect number of fields");
        checkFieldListInEmail(fieldNameElements, fieldNames);
    }

    private void checkFieldListInEmail(Iterable<Element> fieldNameElements, String... fieldNames) {
        for (final String fieldName : fieldNames) {
            assertTrue(Iterables.any(fieldNameElements, new Predicate<Element>() {

                @Override
                public boolean apply(Element element) {
                    return element.text().equals(fieldName);
                }
            }), "Can not find field name: " + fieldName);
        }
    }

    private static Message getNotification(final ImapClient imapClient, final String subject,
            long requestTime) throws MessagingException {
        Message[] notifications = new Message[0];

        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(5, TimeUnit.SECONDS)
                .withMessage("Notification is not sent!").until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver browser) {
                        System.out.println("Waiting for notification...");
                        return imapClient.getMessagesFromInbox(FROM, subject).length > 0;
                    }
                });
        notifications = imapClient.getMessagesFromInbox(FROM, subject);

        List<Message> expectedNotification = Lists.newArrayList();
        for (Message notification : notifications) {
            if (notification.getReceivedDate().getTime() > requestTime)
                expectedNotification.add(notification);
        }
        assertTrue(expectedNotification.size() > 0, "The notification was not sent!");
        assertEquals(1, expectedNotification.size(), "More than one notification were sent!");

        return expectedNotification.get(0);
    }

    private boolean isThisDateValid(String time) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm:ss a ZZZ");
        try {
            format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getWorkingProjectLink() {
        return String.format(GD_PROJECT_LINK, testParams.getHost(), getWorkingProject()
                .getProjectId());
    }
}
