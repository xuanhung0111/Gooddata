/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.mail;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.Validate.notNull;
import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.gooddata.qa.graphene.enums.GDEmails;

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
    public List<Message> getMessagesFromInbox(String from, String subject, Date receiveTime) throws MessagingException {
        notNull(from, "Sender cannot be null");
        notNull(subject, "Subject cannot be null");

        Folder inboxFolder = getInboxFolder();

        SearchTerm fromTerm = new FromStringTerm(from);
        SearchTerm subjectTerm = new SubjectTerm(subject);

        inboxFolder.open(Folder.READ_ONLY);

        return Stream.of(inboxFolder.search(new AndTerm(fromTerm, subjectTerm)))
                .filter(message -> isMessageReceivedAfter(message, receiveTime))
                .collect(toList());
    }

    public List<Message> getMessagesFromInbox(GDEmails from, String subject, Date receiveTime) throws MessagingException {
        return getMessagesFromInbox(from.getEmailAddress(), subject, receiveTime);
    }

    public List<Message> getMessagesFromInbox(GDEmails from, String subject) throws MessagingException {
        return getMessagesFromInbox(from, subject, null);
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
            if (inboxFolder.isOpen()) {
                inboxFolder.close(false);
            }
        }
    }

    public int getMessagesCount(GDEmails from, String subject, Date receivedTime) throws MessagingException {
        return getMessagesCount(from.getEmailAddress(), subject, receivedTime);
    }

    public int getMessagesCount(GDEmails from, String subject) throws MessagingException {
        return getMessagesCount(from, subject, null);
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

    private boolean isMessageReceivedAfter(Message message, Date date) {
        if (isNull(date)) return true;

        try {
            return message.getReceivedDate().compareTo(date) >= 0;
        } catch (MessagingException e) {
            throw new RuntimeException("Error on getting message receive time");
        }
    }
}
