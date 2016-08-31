package com.gooddata.qa.utils.mail;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.gooddata.qa.graphene.entity.mail.Email;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.google.common.collect.Iterables;
import com.sun.mail.util.BASE64DecoderStream;

public class ImapUtils {

    private static final int MAILBOX_POLL_INTERVAL_IN_SECONDS = 30;

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

        return imapClient.getMessagesFromInbox(from, subject, receivedTime);
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

    public static DateTime getMessageReceiveDate(Message message) {
        try {
            return new DateTime(message.getReceivedDate());

        } catch (MessagingException e) {
            throw new RuntimeException("Error on getting message receive date");
        }
    }

    public static List<Part> getAttachmentParts(Message message) {
        List<Part> partList = new ArrayList<Part>();

        try {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    partList.add(bodyPart);
                }
            }
            return partList;
        } catch (IOException e) {
            throw new RuntimeException("Data handler exception when checking message attachments", e);
        } catch (MessagingException e) {
            throw new RuntimeException("Session issue when checking message attachments", e);
        }
    }

    public static String getEmailBody(Part message) throws MessagingException, IOException {
        if (message.isMimeType("text/*")) {
            Object content = message.getContent();
            if (content instanceof BASE64DecoderStream) {
                return null;
            } else {
                return (String) message.getContent();
            }
        }
        if (message.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) message.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getEmailBody(bp);
                    }
                } else if (bp.isMimeType("text/html")) {
                    String s = getEmailBody(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    String s = getEmailBody(bp);
                    if (s != null) {
                        return s;
                    }
                }
            }
            return text;
        } else if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getEmailBody(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    public static void saveMessageAttachments(Message message, File outputDirectory) {
        List<Part> parts = getAttachmentParts(message);
        for (Part part : parts) {
            savePartAttachments(part, outputDirectory);
        }
    }

    public static void savePartAttachments(Part part, File outputDirectory) {
        try {
            if (!Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                throw new RuntimeException("Part without attachment");
            }
            if (StringUtils.isEmpty(part.getFileName())) {
                throw new RuntimeException("Attachment filename is empty");
            }
            File outputFile = new File(outputDirectory, MimeUtility.decodeText(part.getFileName()));
            FileUtils.forceMkdir(outputDirectory);

            saveFile(outputFile, part.getInputStream());

        } catch (MessagingException e) {
            throw new RuntimeException("Cannot get part disposition", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding in filename of attachment", e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create directory or cannot get attachment stream", e);
        }
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

    private static void saveFile(File outputFile, InputStream input) {
        try {
            System.out.println("Saving attachment to file " + outputFile.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            IOUtils.copy(input, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save file " + outputFile.getAbsoluteFile(), e);
        }
    }
}
