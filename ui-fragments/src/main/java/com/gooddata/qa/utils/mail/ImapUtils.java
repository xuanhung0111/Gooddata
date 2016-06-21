package com.gooddata.qa.utils.mail;

import static java.util.stream.Collectors.toList;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.mail.ImapClient.isMessageReceivedAfter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.gooddata.qa.graphene.entity.mail.Email;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.google.common.collect.Iterables;

public class ImapUtils {

    private static final int MAILBOX_POLL_INTERVAL_IN_SECONDS = 15;

    private ImapUtils() {
    }

    /**
     * Checking mailbox if new messages arrive and match with expected count.
     * 
     * @param imapClient
     * @param from
     * @param subject
     * @param expectedMessageCount
     * @return true if new messages arrived. Otherwise, return false
     * @throws MessagingException
     */
    public static boolean areMessagesArrived(ImapClient imapClient, GDEmails from, String subject,
            int expectedMessageCount) throws MessagingException {
        return areMessagesArrived(imapClient, from, subject, null, expectedMessageCount);
    }

    /**
     * Wait and get new messages come to inbox that match with expected count in a period of time
     * 
     * @param imapClient
     * @param from
     * @param subject
     * @param receivedTime
     * @param expectedMessageCount
     * @return a list of messages
     * @throws MessagingException
     */
    public static List<Message> waitForMessages(ImapClient imapClient, GDEmails from, String subject,
            Date receivedTime, int expectedMessageCount) throws MessagingException {
        if (!areMessagesArrived(imapClient, from, subject, receivedTime, expectedMessageCount))
            throw new RuntimeException("No new message arrives as expected");

        return imapClient.getMessagesFromInbox(from, subject)
                .stream()
                .filter(message -> isMessageReceivedAfter(message, receivedTime))
                .collect(toList());
    }

    /**
     * Wait and get new messages come to inbox that match with expected count
     * 
     * @param imapClient
     * @param from
     * @param subject
     * @param expectedMessageCount
     * @return a list of messages
     * @throws MessagingException
     */
    public static List<Message> waitForMessages(ImapClient imapClient,GDEmails from, String subject,
            int expectedMessageCount) throws MessagingException {
        return waitForMessages(imapClient, from, subject, null, expectedMessageCount);
    }

    /**
     * Wait and get any new messages come to inbox
     * 
     * @param imapClient
     * @param from
     * @param subject
     * @return a list of messages
     * @throws MessagingException
     */
    public static List<Message> waitForMessages(ImapClient imapClient, GDEmails from, String subject)
            throws MessagingException {
        return waitForMessages(imapClient, from, subject, null, -1);
    }

    public static Email getLastEmail(final ImapClient imapClient, final GDEmails from,
            final String subject, final int expectedMessageCount) throws MessagingException, IOException {
        Collection<Message> messages = waitForMessages(imapClient, from, subject, expectedMessageCount);
        return Email.getInstance(Iterables.getLast(messages));
    }

    /**
     * Checking mailbox if new messages arrive and match with expected count in a period of time
     * Note: when expectedMessageCount = -1, this param is simply skipped.
     * 
     * @param imapClient
     * @param from
     * @param subject
     * @param receivedTime
     * @param expectedMessageCount
     * @return true if new messages arrived. Otherwise, return false
     * @throws MessagingException
     */
    private static boolean areMessagesArrived(ImapClient imapClient, GDEmails from, String subject,
            Date receivedTime, int expectedMessageCount) throws MessagingException {
        for (int loop = 0; loop < from.getMaxWaitingTimeInMinute() * 60 / MAILBOX_POLL_INTERVAL_IN_SECONDS; loop++) {
            final int messageCount = imapClient.getMessagesCount(from, subject, receivedTime);

            if (expectedMessageCount == -1 && messageCount > 0) {
                System.out.println("New message with subject: " + subject + " arrived");
                return true;
            }

            if (expectedMessageCount != -1 && messageCount == expectedMessageCount) {
                System.out.println("New message with subject: " + subject + " arrived");
                return true;
            }

            System.out.println("Waiting for new message...");
            sleepTightInSeconds(MAILBOX_POLL_INTERVAL_IN_SECONDS);
        }

        System.out.println("New message with subject: " + subject + " does not arrive");
        return false;
    }
}
