package com.gooddata.qa.utils.mail;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.entity.mail.Email;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ImapUtils {

    public static Collection<Message> waitForMessage(final ImapClient imapClient,
            final GDEmails from, final String subject) {
        return waitForMessageWithExpectedCount(imapClient, from, subject, 0);
    }

    public static Collection<Message> waitForMessageWithExpectedCount(final ImapClient imapClient,
            final GDEmails from, final String subject, final int expectedMessageCount) {
        return waitForMessageWithExpectedReceivedTimeAndCount(imapClient, from, subject, 0,
                expectedMessageCount);
    }

    public static Collection<Message> waitForMessageWithExpectedReceivedTime(
            final ImapClient imapClient, final GDEmails from, final String subject,
            final long receivedTime) {
        return waitForMessageWithExpectedReceivedTimeAndCount(imapClient, from, subject,
                receivedTime, 0);
    }

    private static Collection<Message> waitForMessageWithExpectedReceivedTimeAndCount(
            final ImapClient imapClient, final GDEmails from, final String subject,
            final long receivedTime, final int expectedMessageCount) {
        Graphene.waitGui().withTimeout(from.getMaxWaitingTimeInMinute(), TimeUnit.MINUTES).pollingEvery(15, TimeUnit.SECONDS)
                .withMessage("Notification is not sent!").until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver browser) {
                        System.out.println("Waiting for notification...");
                        Collection<Message> expectedMessages =
                                getMessageWithExpectedReceivedTime(imapClient, from, subject,
                                        receivedTime);
                        return expectedMessages.size() > expectedMessageCount;
                    }
                });

        return getMessageWithExpectedReceivedTime(imapClient, from, subject, receivedTime);
    }

    public static Collection<Message> getMessageWithExpectedReceivedTime(
            final ImapClient imapClient, final GDEmails from, final String subject,
            final long receivedTime) {
        Collection<Message> messages =
                Lists.newArrayList(imapClient.getMessagesFromInbox(from.getEmailAddress(), subject));

        if (receivedTime == 0)
            return messages;

        return Collections2.filter(messages, new Predicate<Message>() {

            @Override
            public boolean apply(Message message) {
                try {
                    return message.getReceivedDate().getTime() > receivedTime;
                } catch (MessagingException e) {
                    throw new IllegalStateException("There is an exeception when getting email!", e);
                }
            }
        });
    }

    public static Email getLastEmail(final String host, final String email,
            final String password, final GDEmails from,
            final String subject, final int expectedMessageCount) {

        return ImapClientAction.Utils.doActionWithImapClient(host, email, password, (imapClient) -> {
            Collection<Message> messages = waitForMessageWithExpectedCount(imapClient,
                    from, subject, expectedMessageCount);
            return Email.getInstance(Iterables.getLast(messages));
        });
    }

}
