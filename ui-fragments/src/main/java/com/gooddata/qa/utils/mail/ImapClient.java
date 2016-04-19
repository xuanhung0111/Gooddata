/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.mail;

import static org.apache.commons.lang.Validate.notNull;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.sun.mail.util.BASE64DecoderStream;

/**
 * Simple IMAP mailbox checker to detect arrived messages
 */
public class ImapClient implements Closeable {

    private final String host;
    private final String email;
    private final String password;

    private Store store;

    public ImapClient(String host, String email, String password) {
        this.host = host;
        this.email = email;
        this.password = password;
    }

    /**
     * Just use this action alone when ensure the expected message has arrived.
     * Otherwise, should handle time waiting for new message arrived by using loop to wait until message
     * from inbox reach the expected count.
     * In this case, ImapClient#getMessagesCount is recommended to use first
     */
    public List<Message> getMessagesFromInbox(String from, String subject) {
        notNull(from, "Sender cannot be null");
        notNull(subject, "Subject cannot be null");

        Folder inboxFolder = getInboxFolder();
        Message[] messages;

        try {
            SearchTerm fromTerm = new FromStringTerm(from);
            SearchTerm subjectTerm = new SubjectTerm(subject);

            inboxFolder.open(Folder.READ_ONLY);
            messages = inboxFolder.search(new AndTerm(fromTerm, subjectTerm));

            if (messages.length == 0) {
                inboxFolder.close(false);
            }

            return asList(messages);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot get messages from inbox", e);
        }
    }

    public List<Message> getMessagesFromInbox(GDEmails from, String subject) {
        return getMessagesFromInbox(from.getEmailAddress(), subject);
    }

    /**
     * Get the total messages count from inbox with expected receive time.
     * This will get all messages without receive time when this object is null
     */
    public int getMessagesCount(String from, String subject, Date receivedTime) throws MessagingException {
        Folder inboxFolder = getInboxFolder();

        SearchTerm fromTerm = new FromStringTerm(from);
        SearchTerm subjectTerm = new SubjectTerm(subject);

        try {
            inboxFolder.open(Folder.READ_ONLY);

            return (int) Stream.of(inboxFolder.search(new AndTerm(fromTerm, subjectTerm)))
                    .filter(message -> isMessageReceivedAfter(message, receivedTime))
                    .count();

        } finally {
            inboxFolder.close(false);
        }
    }

    public int getMessagesCount(GDEmails from, String subject, Date receivedTime) throws MessagingException {
        return getMessagesCount(from.getEmailAddress(), subject, receivedTime);
    }

    public int getMessagesCount(GDEmails from, String subject) throws MessagingException {
        return getMessagesCount(from, subject, null);
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
        List<Part> parts = ImapClient.getAttachmentParts(message);
        for (Part part : parts) {
            ImapClient.savePartAttachments(part, outputDirectory);
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

    public static boolean isMessageReceivedAfter(Message message, Date date) {
        if (isNull(date)) return true;

        try {
            return message.getReceivedDate().compareTo(date) >= 0;
        } catch (MessagingException e) {
            throw new RuntimeException("Error on getting message receive time");
        }
    }

    @Override
    public void close() {
        try {
            getStore().close();
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot close store", e);
        }
    }

    private Folder getInboxFolder() {
        try {
            return getStore().getFolder("INBOX");
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot get inbox folder", e);
        }
    }

    private Store getStore() {
        if (store == null) {
            try {
                Session session = Session.getDefaultInstance(getConnectProperties(), null);
                store = session.getStore("imaps");
                store.connect(host, email, password);
            } catch (NoSuchProviderException e) {
                throw new RuntimeException("IMAP provider is not available", e);
            } catch (MessagingException e) {
                throw new RuntimeException("Cannot connect to IMAP store, check imap hostname and credentials", e);
            }
        }
        return store;
    }

    private Properties getConnectProperties() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.port", "993");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.connectionpooltimeout", "180000");
        // props.setProperty("mail.debug", "true");
        return props;
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
