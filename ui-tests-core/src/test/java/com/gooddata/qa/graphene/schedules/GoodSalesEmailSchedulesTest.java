/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;

public class GoodSalesEmailSchedulesTest extends AbstractGoodSalesEmailSchedulesTest {

    private String reportTitle = "Normal-Report";
    private String dashboardTitle = "UI-Graphene-core-Dashboard";

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
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage().scheduleNewReportEmail(testParams.getUser(), reportTitle,
                "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
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
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("ACCELERATE scheduled mails processing");
            ScheduleEmailRestUtils.accelerate(getRestApiClient(), testParams.getProjectId());
            checkMailbox(imapClient);
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            ScheduleEmailRestUtils.decelerate(getRestApiClient(), testParams.getProjectId());
        }
    }

    private void checkMailbox(ImapClient imapClient) throws MessagingException {
        List<Message> reportMessages = waitForMessages(imapClient, GDEmails.NOREPLY, reportTitle, 1);
        List<Message> dashboardMessages = waitForMessages(imapClient, GDEmails.NOREPLY, dashboardTitle, 1);

        System.out.println("Saving dashboard message ...");
        ImapUtils.saveMessageAttachments(dashboardMessages.get(0), attachmentsDirectory);

        System.out.println("Saving report messages ...");
        ImapUtils.saveMessageAttachments(reportMessages.get(0), attachmentsDirectory);

        // REPORT EXPORT
        List<Part> reportAttachmentParts = ImapUtils.getAttachmentParts(reportMessages.get(0));
        assertEquals(reportAttachmentParts.size(), 3, "Report message has correct number of attachments.");

        Part pdfPart = findPartByContentType(reportAttachmentParts, "application/pdf");
        verifyAttachment(pdfPart, "PDF", 3200);

        Part xlsxPart = findPartByContentType(reportAttachmentParts,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verifyAttachment(xlsxPart, "XLSX", 7500);

        Part csvPart = findPartByContentType(reportAttachmentParts, "text/csv");
        verifyAttachment(csvPart, "CSV", 120);

        // DASHBOARD EXPORT
        List<Part> dashboardAttachmentParts = ImapUtils.getAttachmentParts(dashboardMessages.get(0));
        assertEquals(dashboardAttachmentParts.size(), 1, "Dashboard message has correct number of attachments.");
        assertTrue(dashboardAttachmentParts.get(0).getContentType().contains("application/pdf".toUpperCase()),
                "Dashboard attachment has PDF content type.");
        verifyAttachment(dashboardAttachmentParts.get(0), "PDF", 67000);
    }

    private void verifyAttachment(Part attachment, String type, long minimalSize) throws MessagingException {
        assertTrue(attachment.getSize() > minimalSize, "The attachment (" + type
                + ") has the expected minimal size. Expected " + minimalSize + "B, found " + attachment.getSize()
                + "B.");
    }
}
