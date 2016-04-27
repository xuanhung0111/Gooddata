/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static com.gooddata.qa.utils.mail.ImapUtils.areMessagesArrived;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage.RepeatTime;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;

@Test(groups = {"GoodSalesUnsubscribe"}, description = "Tests for GoodSales project - unsubscribe in GD platform")
public class GoodSalesUnsubscribeTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String UNSUBSCRIBE_PATTERN =
            ".*If you no longer want to receive it, <a href=\"([^\"]*)\">click here</a>.*";

    private String reportTitle = "UI Graphene core Report";

    @FindBy(css = ".standalone-message.unsubscribed .title")
    private WebElement unsubscribedTitle;

    @FindBy(css = ".standalone-message.unsubscribed .description")
    private WebElement unsubscribedDescription;

    @FindBy(css = ".logoArea-big img")
    private WebElement unsubscribedLogo;

    @BeforeClass
    public void setUp() throws Exception {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        reportTitle = reportTitle + identification;
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), reportTitle,
                "Unsubscribe bcc test - report.", "Activities by Type", ExportFormat.CSV, RepeatTime.DAILY);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyCreatedSchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfGlobalSchedules(), 1, "Schedule is properly created.");
        takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMail() throws IOException {
        initEmailSchedulesPage();
        String reportScheduleUri = emailSchedulesPage.getScheduleMailUriByName(reportTitle);
        setBcc(reportScheduleUri, new String[] { getBccEmail() });
        updateRecurrencyString(reportScheduleUri);
    }

    @Test(dependsOnMethods = {"updateScheduledMail"})
    public void waitForMessageAndUnsubscribe() throws MessagingException, IOException {
        ScheduleMailPssClient pssClient = new ScheduleMailPssClient(getRestApiClient(), testParams.getProjectId());
        
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("ACCELERATE scheduled mails processing");
            pssClient.accelerate();

            // wait for expected messages to arrive
            int expectedMessageCount = 2;
            List<Message> emailMessages =
                    waitForMessages(imapClient, GDEmails.NOREPLY, reportTitle, expectedMessageCount);

            // get and visit unsubscribe links in each of the received mails
            for (Message message: emailMessages) {
                assertEquals(message.getAllRecipients().length, 1, "There is exactly one recipient.");
                String messageBody = ImapClient.getEmailBody(message);
                String unsubscribeLink = getUnsubscribeLink(messageBody, UNSUBSCRIBE_PATTERN);
                visitUnsubscribeLink(unsubscribeLink);
                takeScreenshot(browser, "Goodsales-schedules-unsubscribe-link-" + message.getAllRecipients()[0],
                    this.getClass());
            }
            initEmailSchedulesPage();
            updateRecurrencyString(emailSchedulesPage.getScheduleMailUriByName(reportTitle));
            pssClient.accelerate();

            // check that no more email is sent
            assertFalse(areMessagesArrived(imapClient, GDEmails.NOREPLY, reportTitle, expectedMessageCount + 1),
                    "Expected no more email will be sent but it's not!");

        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            pssClient.decelerate();
        }
    }

    @Test(dependsOnMethods = {"waitForMessageAndUnsubscribe"})
    public void verifySuccesOfUnsubscribe() {
        initEmailSchedulesPage();
        String unsubscribed = emailSchedulesPage.getUnsubscribed(reportTitle);
        assertTrue(unsubscribed.contains(imapUser),
                "The 'To' email is in the list of unsubscribed users. Expected '" + imapUser + "', found '"
                        + unsubscribed + "'.");
        assertTrue(unsubscribed.contains(getBccEmail()),
                "The 'Bcc' user is in the list of unsubscribed users. Expected '" + getBccEmail() + "', found '"
                        + unsubscribed + "'.");
        takeScreenshot(browser, "Goodsales-schedules-unsubscribed", this.getClass());
    }

    // check email source for unsubscribe link and return this link
    private String getUnsubscribeLink(String emailSource, String unsubscribePattern) {
        Pattern pattern = Pattern.compile(unsubscribePattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(emailSource);
        assertTrue(matcher.matches(), "There is a link to unsubscribe from scheduled e-mail.");
        return matcher.group(1);
    }

    // navigate browser to the unsubscribe link and check that unsubscribe succeeded
    private void visitUnsubscribeLink(String unsubscribeLink) {
        browser.get(unsubscribeLink);
        waitForElementPresent(unsubscribedTitle);
        assertEquals(unsubscribedTitle.getText(), "You have been unsubscribed",
                "There is a proper title on unsubscribe page.");
        waitForElementPresent(unsubscribedDescription);
        assertTrue(unsubscribedDescription.getText().contains("\"" + reportTitle + "\""),
            "Unsubscribed description contains a subject in quotes. Expected '" + reportTitle + "', found '"
                    + unsubscribedDescription + "'.");
        waitForElementPresent(unsubscribedLogo);
        assertEquals(unsubscribedLogo.getAttribute("alt"), "GoodData",
                "Attribute 'alt' in the logo contains GoodData.");
    }

    private String getBccEmail() {
        return imapUser.replace("@", "+bcc@");
    }
}
