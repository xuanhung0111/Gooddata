/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.joda.time.DateTime.now;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.apache.commons.lang.math.IntRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;

public class GoodSalesEmailSchedulesTest extends AbstractGoodSalesEmailSchedulesTest {

    private String reportTitle = "Normal-Report";
    private String dashboardTitle = "UI-Graphene-core-Dashboard";

    private int dashboardScheduleTimeInMinute;
    private int reportScheduleTimeInMunite;

    @BeforeClass
    public void setUp() {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        reportTitle = reportTitle + identification;
        dashboardTitle = dashboardTitle + identification;
        attachmentsDirectory =
                new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() {
        initEmailSchedulesPage().scheduleNewDahboardEmail(testParams.getUser(), dashboardTitle,
                "Scheduled email test - dashboard.", "Outlook");
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());

        dashboardScheduleTimeInMinute = now().getMinuteOfHour();
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage().scheduleNewReportEmail(testParams.getUser(), reportTitle,
                "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());

        reportScheduleTimeInMunite = now().getMinuteOfHour();
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyCreatedSchedules() {
        assertEquals(initEmailSchedulesPage().getNumberOfGlobalSchedules(), 2, "Schedules are properly created.");
        Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMailRecurrency() throws IOException {
        String reportScheduleUri = initEmailSchedulesPage().getScheduleMailUriByName(reportTitle);
        String dashboardScheduleUri = EmailSchedulePage.getInstance(browser).getScheduleMailUriByName(dashboardTitle);
        updateRecurrencyString(reportScheduleUri);
        updateRecurrencyString(dashboardScheduleUri);
    }

    @Test(dependsOnMethods = {"updateScheduledMailRecurrency"})
    public void waitForMessages() throws MessagingException {
        ScheduleMailPssClient pssClient = new ScheduleMailPssClient(getRestApiClient(), testParams.getProjectId());
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("ACCELERATE scheduled mails processing");
            pssClient.accelerate();
            checkMailbox(imapClient);
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            pssClient.decelerate();
        }
    }

    private void checkMailbox(ImapClient imapClient) throws MessagingException {
        List<Message> reportMessages = ImapUtils.waitForMessages(imapClient, GDEmails.NOREPLY, reportTitle);
        List<Message> dashboardMessages = ImapUtils.waitForMessages(imapClient, GDEmails.NOREPLY, dashboardTitle);

        verifyNumberOfEmail(reportScheduleTimeInMunite, reportMessages.size());
        verifyNumberOfEmail(dashboardScheduleTimeInMinute, dashboardMessages.size());

        System.out.println("Saving dashboard message ...");
        ImapClient.saveMessageAttachments(dashboardMessages.get(0), attachmentsDirectory);

        System.out.println("Saving report messages ...");
        ImapClient.saveMessageAttachments(reportMessages.get(0), attachmentsDirectory);

        // REPORT EXPORT
        List<Part> reportAttachmentParts = ImapClient.getAttachmentParts(reportMessages.get(0));
        assertEquals(reportAttachmentParts.size(), 4, "Report message has correct number of attachments.");

        Part pdfPart = findPartByContentType(reportAttachmentParts, "application/pdf");
        verifyAttachment(pdfPart, "PDF", 3200);

        Part xlsPart = findPartByContentType(reportAttachmentParts, "application/vnd.ms-excel");
        verifyAttachment(xlsPart, "XLS", 7700);

        Part xlsxPart = findPartByContentType(reportAttachmentParts,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verifyAttachment(xlsxPart, "XLSX", 7500);

        Part csvPart = findPartByContentType(reportAttachmentParts, "text/csv");
        verifyAttachment(csvPart, "CSV", 120);

        // DASHBOARD EXPORT
        List<Part> dashboardAttachmentParts = ImapClient.getAttachmentParts(dashboardMessages.get(0));
        assertEquals(dashboardAttachmentParts.size(), 1, "Dashboard message has correct number of attachments.");
        assertTrue(dashboardAttachmentParts.get(0).getContentType().contains("application/pdf".toUpperCase()),
                "Dashboard attachment has PDF content type.");
        verifyAttachment(dashboardAttachmentParts.get(0), "PDF", 67000);
    }

    private void verifyNumberOfEmail(int scheduleTimeInMinute, int numberOfEmail) {
        if (numberOfEmail == 1) {
            return;
        }

        if (numberOfEmail == 2) {
            if (new IntRange(20, 29).containsInteger(scheduleTimeInMinute) ||
                    new IntRange(50, 59).containsInteger(scheduleTimeInMinute)) {
                log.info("scheduled email is set up so close to the default scheduled time. So we get 2 emails.");
                return;
            }
        }

        throw new RuntimeException("There are many messages arrived instead one.");
    }

    private void verifyAttachment(Part attachment, String type, long minimalSize) throws MessagingException {
        assertTrue(attachment.getSize() > minimalSize, "The attachment (" + type
                + ") has the expected minimal size. Expected " + minimalSize + "B, found " + attachment.getSize()
                + "B.");
    }
}
