/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"GoodSalesUnsubscribe"}, description = "Tests for GoodSales project - unsubscribe in GD platform")
public class GoodSalesUnsubscribeTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String FROM = "noreply@gooddata.com";

    private static final int TIMEOUT_MINUTES = 4;
    // mailbox polling interval in miliseconds
    private static final int MAILBOX_POLL_INTERVAL_MILISECONDS = 10000;

    private String reportTitle = "UI-Graphene-core-Report";

    @FindBy(css = ".standalone-message.unsubscribed .title")
    private WebElement unsubscribedTitle;

    @BeforeClass
    public void setUp() throws Exception {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        reportTitle = reportTitle + identification;

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedules"})
    public void verifyEmptySchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfSchedules(), 0, "There are some not expected schedules");
        Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
    }


    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), reportTitle, "Unsubscribe bcc test - report.",
                "Activities by Type", ExportFormat.CSV);
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyCreatedSchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfSchedules(), 1, "1 schedules weren't created properly");
        Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMail() throws Exception {
        initEmailSchedulesPage();
        String reportScheduleUri = emailSchedulesPage.getScheduleMailUriByName(reportTitle);
        setBcc(reportScheduleUri, new String[] { getBccEmail() });
        updateRecurrencyString(reportScheduleUri);
    }

    @Test(groups = {"tests"}, dependsOnMethods = {"updateScheduledMail"})
    public void waitForMessageAndUnsubscribe() throws Exception {
        ScheduleMailPssClient pssClient = new ScheduleMailPssClient(getRestApiClient(), testParams.getProjectId());
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            System.out.println("ACCELERATE scheduled mails processing");
            pssClient.accelerate();

            // wait for expected messages to arrive
            int expectedMessageCount = 2;
            Message[] emailMessages = getMessagesFromInbox(imapClient, FROM, reportTitle, expectedMessageCount);

            // get and visit unsubscribe links in each of the received mails
            for (Message message: emailMessages) {
                assertEquals(message.getAllRecipients().length, 1, "Expected one recipient.");
                String messageBody = ImapClient.getEmailBody(message);
                String unsubscribeLink = getUnsubscribeLink(messageBody);
                visitUnsubscribeLink(unsubscribeLink);
                Screenshots.takeScreenshot(
                    browser,
                    "Goodsales-schedules-unsubscribe-link-" + message.getAllRecipients()[0],
                    this.getClass()
                );
            }
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            pssClient.decelerate();
            imapClient.close();
        }
    }

    @Test(groups = {"tests"}, dependsOnMethods = {"waitForMessageAndUnsubscribe"})
    public void verifySuccesOfUnsubscribe() throws Exception {
        initEmailSchedulesPage();
        String unsubscribed = emailSchedulesPage.getUnsubscribed(reportTitle);
        assertTrue(unsubscribed.contains(testParams.getUser()), "User in 'To' is not unsubscribed.");
        assertTrue(unsubscribed.contains(getBccEmail()), "User in 'Bcc' is not unsubscribed.");
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-unsubscribed", this.getClass());
        successfulTest = true;
    }

    // check email source for unsubscribe link and return this link
    private String getUnsubscribeLink(String emailSource) {
        Pattern pattern = Pattern.compile(".*If you no longer want to receive it, <a href=\"([^\"]*)\">click here</a>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(emailSource);
        assertTrue(matcher.matches(), "There is no link to unsubscribe from scheduled e-mail.");
        return matcher.group(1);
    }

    // navigate browser to the unsubscribe link and check that unsubscribe succeeded
    private void visitUnsubscribeLink(String unsubscribeLink) {
        browser.get(unsubscribeLink);
        waitForElementPresent(unsubscribedTitle);
        assertEquals(unsubscribedTitle.getText(), "You have been unsubscribed", "Unsubscribed message does not match.");
    }

    private Message[] getMessagesFromInbox(ImapClient imapClient, String from, String subject, int expectedMessagesCount)
            throws InterruptedException {
        Message[] reportMessages = new Message[0];

        int loops = 0,
            maxLoops = 60000 / MAILBOX_POLL_INTERVAL_MILISECONDS * TIMEOUT_MINUTES;

        while (reportMessages.length < expectedMessagesCount && loops < maxLoops) {
            System.out.println("Waiting for messages, try " + (loops + 1));

            Message[] receivedMessages = imapClient.getMessagesFromInbox(from, subject);
            reportMessages = ArrayUtils.addAll(reportMessages, receivedMessages);

            if (reportMessages.length >= expectedMessagesCount) {
                System.out.println(String.format("Report export messages from %s arrived (subj: %s)", from, subject));
                break;
            }

            Thread.sleep(MAILBOX_POLL_INTERVAL_MILISECONDS);
            loops++;
        }

        return reportMessages;
    }

    private String getBccEmail() {
        return testParams.getUser().replace("@", "+bcc@");
    }
}
