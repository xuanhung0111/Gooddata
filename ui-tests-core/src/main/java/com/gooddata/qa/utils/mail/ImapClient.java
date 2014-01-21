/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.mail;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Simple IMAP mailbox checker to detect arrived messages
 */
public class ImapClient {

    private final String host;
    private final String email;
    private final String password;

    private Store store;

    public ImapClient(String host, String email, String password) {
        this.host = host;
        this.email = email;
        this.password = password;
    }

    public Message[] getMessagesFromInbox(String from, String subject) {
        notNull(from, "Sender cannot be null");
        notNull(subject, "Subject cannot be null");

        Folder inboxFolder = getInboxFolder();

        Message messages[];
        try {
            SearchTerm fromTerm = new FromStringTerm(from);
            SearchTerm subjectTerm = new SubjectTerm(subject);
            inboxFolder.open(Folder.READ_ONLY);
            messages = inboxFolder.search(new AndTerm(fromTerm, subjectTerm));
            if (messages.length == 0) {
                inboxFolder.close(false);
            }
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot get messages from inbox", e);
        }

        return messages;
    }

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

    public static List<Part> getAttachmentParts(Message message) {
        List<Part> partList = new ArrayList<Part>();

        try {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
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
}
