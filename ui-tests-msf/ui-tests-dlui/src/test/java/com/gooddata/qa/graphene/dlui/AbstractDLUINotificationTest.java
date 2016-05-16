package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class AbstractDLUINotificationTest extends AbstractAnnieDialogTest {

    protected static final String FAILED_DATA_ADDING_NOTIFICATION_SUBJECT =
            "Error adding new data to %s project";
    protected static final String SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT =
            "New data is ready to use in the %s project";

    private static final String GD_PROJECT_LINK = "https://%s/#s=/gdc/projects/%s";
    private static final String AD_REPORT_LINK = "https://%s/analyze/#/%s/reportId/edit";
    private static final String GD_SUPPORT_LINK = "https://support.gooddata.com";

    protected String imapEditorUser;
    protected String imapEditorPassword;
    
    protected void checkSuccessfulDataAddingNotification(UserRoles role, Date receivedTime,
            String... fieldNames) {
        Document message =
                role.equals(UserRoles.ADMIN) ? getEmailOfAdminUser(
                        SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT, receivedTime)
                        : getEmailOfEditorUser(SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT,
                                receivedTime);
        assertSuccessDataAddingEmailContent(message, fieldNames);
    }

    protected void checkSuccessfulDataAddingEmail(Date receivedTime, String... fieldNames) {
        assertSuccessDataAddingEmailContent(
                getEmailOfAdminUser(SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT, receivedTime),
                fieldNames);
    }

    protected void checkSuccessfulDataAddingEmailForEditor(Date receivedTime, String... fieldNames) {
        assertSuccessDataAddingEmailContent(
                getEmailOfEditorUser(SUCCESSFUL_DATA_ADDING_NOTIFICATION_SUBJECT, receivedTime),
                fieldNames);
    }

    protected void checkFailedDataAddingEmail(Date receivedTime, String... fieldNames) {
        assertFailedDataAddingEmailContent(
                getEmailOfAdminUser(FAILED_DATA_ADDING_NOTIFICATION_SUBJECT, receivedTime),
                fieldNames);
    }

    protected void checkFailedDataAddingEmailForEditor(Date receivedTime, String... fieldNames) {
        assertFailedDataAddingEmailContentForEditor(
                getEmailOfEditorUser(FAILED_DATA_ADDING_NOTIFICATION_SUBJECT, receivedTime),
                fieldNames);
    }

    private Document getEmailOfEditorUser(String subject, Date receivedTime) {
        ImapClient imapClient = new ImapClient(imapHost, imapEditorUser, imapEditorPassword);
        return getEmailContent(subject, receivedTime, imapClient);
    }

    private Document getEmailOfAdminUser(String subject, Date receivedTime) {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        return getEmailContent(subject, receivedTime, imapClient);
    }

    private Document getEmailContent(String subject, Date receivedTime, ImapClient imapClient) {
        try {
            System.out.println("Waiting for notification...");
            Message notification = getNotification(imapClient, subject, receivedTime);
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
        String receivedTimeText = "You requested the fields at ";
        String expectedExploreNewDataLink =
                String.format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId());

        assertEquals(getElementLink(message, exploreNewData), expectedExploreNewDataLink,
                "Incorrect empty report link in email content!");
        assertEquals(getElementLink(message, testParams.getProjectId()),
                getWorkingProjectLink(), "Incorrect project link in email content!");

        String receivedTimeMessage = message.getElementsContainingOwnText(receivedTimeText).text();
        assertTrue(isThisDateValid(receivedTimeMessage.replace(receivedTimeText, "")),
                "Invalid time format: " + receivedTimeMessage);

        assertEquals(getElementLink(message, GD_SUPPORT_LINK), GD_SUPPORT_LINK,
                "Incorrect support link in email content!");

        checkFieldListInEmail(message, fieldNames);
    }

    private void assertFailedDataAddingEmailContent(Document message, String... fieldNames) {
        String gdSupportLinkTitle = "GoodData Customer Support";

        assertEquals(getElementLink(message, testParams.getProjectId()),
                getWorkingProjectLink(), "Incorrect project link in email content!");
        verifyValidLink(getRestApiClient(), getElementLink(message, "execution log"));
        assertEquals(getElementLink(message, gdSupportLinkTitle), GD_SUPPORT_LINK,
                "Incorrect support link in email content!");

        checkFieldListInEmail(message, fieldNames);
    }

    private void assertFailedDataAddingEmailContentForEditor(Document message, String... fieldNames) {
        assertEquals(getElementLink(message, testParams.getProjectId()),
                getWorkingProjectLink(), "Incorrect project link in email content!");
        assertTrue(message.getElementsContainingOwnText("execution log").isEmpty());
        assertEquals(getElementLink(message, GD_SUPPORT_LINK), GD_SUPPORT_LINK,
                "Incorrect support link in email content!");

        checkFieldListInEmail(message, fieldNames);
    }

    private String getElementLink(Document message, String key) {
        if (message.getElementsContainingOwnText(key).isEmpty())
            throw new IllegalStateException("Cannot find element with the key: " + key);
        return message.getElementsContainingOwnText(key).attr("href");
    }

    private void checkFieldListInEmail(Document message, String... fieldNames) {
        List<Element> fieldNameElements = message.getElementsByTag("li");
        assertEquals(fieldNameElements.size(), fieldNames.length, "Incorrect number of fields");
        for (final String fieldName : fieldNames) {
            assertTrue(Iterables.any(fieldNameElements, new Predicate<Element>() {

                @Override
                public boolean apply(Element element) {
                    return element.text().equals(fieldName);
                }
            }), "Can not find field name: " + fieldName);
        }
    }

    private Message getNotification(final ImapClient imapClient, final String subject,
            Date receivedTime) throws MessagingException {
        Collection<Message> notifications =
                ImapUtils.waitForMessages(imapClient,
                        GDEmails.NO_REPLY,
                        String.format(subject, testParams.getProjectId()), receivedTime, 1);

        return Iterables.getLast(notifications);
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
        return String.format(GD_PROJECT_LINK, testParams.getHost(), testParams.getProjectId());
    }
}
