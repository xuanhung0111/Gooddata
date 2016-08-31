package com.gooddata.qa.graphene.entity.mail;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.gooddata.qa.utils.mail.ImapUtils;

/**
 *This class is used for containing an email content 
 */
public class Email {

    private String from;
    private String subject;
    private String body;

    private Email(String from, String subject, String body) {
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    public static Email getInstance(Message message) throws MessagingException, IOException {
        return new Email(InternetAddress.toString(message.getFrom()),
                message.getSubject(), ImapUtils.getEmailBody(message));
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
