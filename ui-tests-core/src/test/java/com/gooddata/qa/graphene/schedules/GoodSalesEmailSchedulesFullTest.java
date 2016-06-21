/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR_WARNING;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.logRedBarMessageInfo;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createSimpleMufObjByUri;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage.RepeatTime;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

@Test(groups = {"GoodSalesSchedules"},
      description = "Tests for GoodSales project (email schedules functionality) in GD platform")
public class GoodSalesEmailSchedulesFullTest extends AbstractGoodSalesEmailSchedulesTest {

    private String emptyDashboardTitle = "Empty-Dashboard";
    private String noDataReportTitle = "No-Data-Report";
    private String incomputableReportTitle = "Incomputable-Report";
    private String tooLargeReportTitle = "Too-Large-Report";
    private String filteredVariableReportTitle = "Filtered-Variable-Report";
    private String numericVariableReportTitle = "Numeric-Variable-Report";
    private String mufReportTitle = "MUF-Report";

    private int scheduledTimeInMinute;

    private Map<String, List<Message>> messages;
    private Map<String, MessageContent> attachments = new HashMap<String, MessageContent>();

    private static final String NO_DATA_REPORT = "No data report";
    private static final String INCOMPUTABLE_REPORT = "Incomputable report";
    private static final String TOO_LARGE_REPORT = "Too large report";

    @BeforeClass
    public void setUp() {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory = new File(getProperty("maven.project.build.directory", "./target/attachments"));

        List<Message> emptyMessage = Collections.emptyList();
        messages = new HashMap<String, List<Message>>();
        messages.put(emptyDashboardTitle += identification, emptyMessage);
        messages.put(noDataReportTitle += identification, emptyMessage);
        messages.put(incomputableReportTitle += identification, emptyMessage);
        messages.put(tooLargeReportTitle += identification, emptyMessage);
        messages.put(filteredVariableReportTitle += identification, emptyMessage);
        messages.put(numericVariableReportTitle += identification, emptyMessage);
        messages.put(mufReportTitle += identification, emptyMessage);
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createEmptyDashboardSchedule() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Empty dashboard");
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), emptyDashboardTitle,
                "Scheduled email test - empty dashboard.", "First Tab");
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-empty-dashboard", this.getClass());
        DateTime dateTime = new DateTime();
        // gets minute of schedule time
        scheduledTimeInMinute = dateTime.getMinuteOfHour();
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void deleteDashboardUsedInSchedule() {
        String dashboardTitle = "Schedule dashboard";
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), dashboardTitle,
                "Scheduled email test - dashboard.", "Outlook");

        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard("Pipeline Analysis");
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().tryToDeleteDashboard();
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return browser.findElements(BY_RED_BAR).size() != 0;
                }
            });
            assertEquals(browser.findElement(BY_RED_BAR).getText(), CANNOT_DELETE_DASHBOARD_MESSAGE);
            logRedBarMessageInfo(browser);
            waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
            dashboardsPage.getDashboardEditBar().cancelDashboard();
        } finally {
            initEmailSchedulesPage();
            waitForFragmentVisible(emailSchedulesPage).deleteSchedule(dashboardTitle);
        }
    }

    @Test(dependsOnMethods = {"createEmptyDashboardSchedule"}, groups = {"schedules"})
    public void duplicateSchedule() {
        initEmailSchedulesPage();
        waitForFragmentVisible(emailSchedulesPage).duplicateSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnMethods = {"duplicateSchedule"}, groups = {"schedules"})
    public void deleteSchedule() {
        initEmailSchedulesPage();
        waitForFragmentVisible(emailSchedulesPage).deleteSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleEmptyReport() {
        initReportsPage();
        String expression = "SELECT AVG([/gdc/md/${pid}/obj/1145]) where [/gdc/md/${pid}/obj/1093] not in "
                + "([/gdc/md/${pid}/obj/1093/elements?id=13], [/gdc/md/${pid}/obj/1093/elements?id=7], "
                + "[/gdc/md/${pid}/obj/1093/elements?id=11])";

        Metric metric = getMdService().createObj(getProject(), new Metric("NO DATA",
                expression.replace("${pid}", testParams.getProjectId()), "#,##0"));
        ReportDefinition definition = GridReportDefinitionContent.create(NO_DATA_REPORT,
                singletonList("metricGroup"), Collections.<AttributeInGrid>emptyList(),
                singletonList(new GridElement(metric.getUri(), "metric")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), noDataReportTitle,
                "Scheduled email test - no data report.", NO_DATA_REPORT, ExportFormat.ALL);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-no-data-report", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleIncomputableReport() {
        initReportsPage();
        String amountMetricUri = getMdService().getObjUri(getProject(), Metric.class, identifier("ah1EuQxwaCqs"));
        Attribute activity = getMdService().getObj(getProject(), Attribute.class, identifier("attr.activity.id"));
        ReportDefinition definition = GridReportDefinitionContent.create(INCOMPUTABLE_REPORT,
                singletonList("metricGroup"),
                singletonList(new AttributeInGrid(activity.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(amountMetricUri, "metric")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), incomputableReportTitle,
                "Scheduled email test - incomputable report.", INCOMPUTABLE_REPORT, ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-incomputable-report", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleTooLargeReport() {
        initReportsPage();
        Attribute account = getMdService().getObj(getProject(), Attribute.class, identifier("attr.account.id"));
        Attribute activity = getMdService().getObj(getProject(), Attribute.class, identifier("attr.activity.id"));
        Attribute activityType =
                getMdService().getObj(getProject(), Attribute.class, identifier("attr.activity.activitytype"));
        ReportDefinition definition = GridReportDefinitionContent.create(TOO_LARGE_REPORT,
                singletonList("metricGroup"),
                asList(new AttributeInGrid(account.getDefaultDisplayForm().getUri()),
                        new AttributeInGrid(activity.getDefaultDisplayForm().getUri()),
                        new AttributeInGrid(activityType.getDefaultDisplayForm().getUri())),
                Collections.<GridElement>emptyList());
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), tooLargeReportTitle,
                "Scheduled email test - incomputable report.", TOO_LARGE_REPORT, ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-too-large-report", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleReportApplyFilteredVariable() {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable("FVariable")
            .withAttribute("Activity Type").withAttributeElements("Email"));

        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName("Filtered variable report")
                .withHows("Activity Type").withWhats("# of Activities");
        createReport(rd, "Filtered variable report");
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FVariable", "Email"));
        reportPage.saveReport();

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), filteredVariableReportTitle,
                "Scheduled email test - Filtered variable report.", "Filtered variable report", ExportFormat.CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-filtered-variable-report", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleReportApplyNumericVariable() {
        initVariablePage();
        String variableUri = variablePage.createVariable(new NumericVariable("NVariable").withDefaultNumber(2012));

        String report = "Sum amount in 2012";
        String expression = "SELECT SUM ([/gdc/md/${pid}/obj/1279])"
                + " WHERE [/gdc/md/${pid}/obj/513] = [" + variableUri + "]";
        Metric metric = getMdService().createObj(getProject(), new Metric(report,
                expression.replace("${pid}", testParams.getProjectId()), "#,##0"));
        Attribute yearSnapshot = getMdService().getObj(getProject(), Attribute.class, identifier("snapshot.year"));
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList("metricGroup"),
                singletonList(new AttributeInGrid(yearSnapshot.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(metric.getUri(), "metric")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), numericVariableReportTitle,
                "Scheduled email test - Numeric variable report.", report, ExportFormat.CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-numeric-variable-report", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void scheduleMufReport() throws IOException, JSONException {
        initEmailSchedulesPage();
        Attribute product = getMdService().getObj(getProject(), Attribute.class, identifier("attr.product.id"));
        String amountUri = getMdService().getObjUri(getProject(), Metric.class, identifier("ah1EuQxwaCqs"));
        String report = "MUF report";

        final String explorerUri = getMdService().getAttributeElements(product).stream()
                .filter(e -> e.getTitle().equals("Explorer")).findFirst().get().getUri();

        Map<String, Collection<String>> conditions = new HashMap<String, Collection<String>>();
        conditions.put(product.getUri(), singletonList(explorerUri));
        String mufUri =
                createSimpleMufObjByUri(getRestApiClient(), getProject().getId(), "Product user filter", conditions);
        addMufToUser(getRestApiClient(), getProject().getId(), testParams.getUser(), mufUri);

        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList("metricGroup"),
                singletonList(new AttributeInGrid(product.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(amountUri, "metric")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), mufReportTitle,
                "Scheduled email test - MUF report.", report, ExportFormat.CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-muf-report", this.getClass());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"verify-UI"})
    public void deleteReport() {
        String title = "verify-UI-title";
        String report = "# test report";

        initReportsPage();
        String amountMetricUri = getMdService().getObjUri(getProject(), Metric.class, identifier("ah1EuQxwaCqs"));
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList("metricGroup"),
                Collections.<AttributeInGrid>emptyList(),
                singletonList(new GridElement(amountMetricUri, "metric")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), title,
                "Scheduled email test - report.", report, ExportFormat.ALL);

        try {
            initReportsPage();
            waitForFragmentVisible(reportsPage).tryDeleteReports(report);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return browser.findElements(BY_RED_BAR_WARNING).size() != 0;
                }
            });
            assertEquals(browser.findElement(BY_RED_BAR_WARNING).getText(), "0 report(s) deleted."
                    + " 1 report(s) are in use on a dashboard or an email distribution list and were not deleted.");
            logRedBarMessageInfo(browser);
            waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
        } finally {
            initEmailSchedulesPage();
            waitForFragmentVisible(emailSchedulesPage).deleteSchedule(title);
            initReportsPage();
            waitForFragmentVisible(reportsPage).deleteReports(report);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"verify-UI"})
    public void editSchedule() {
        String title = "verify-UI-title";
        String updatedTitle = title + "Updated";
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), title,
                "Scheduled email test - report.", "Actual QTD", ExportFormat.CSV);

        try {
            emailSchedulesPage.openSchedule(title)
                .setSubject(updatedTitle)
                .setMessage("Scheduled email test - report. (Updated)")
                .selectReportFormat(ExportFormat.ALL)
                .saveSchedule();
            assertFalse(emailSchedulesPage.isGlobalSchedulePresent(title));
            assertTrue(emailSchedulesPage.isGlobalSchedulePresent(updatedTitle));

            assertEquals(emailSchedulesPage.openSchedule(updatedTitle).getMessageFromInput(),
                    "Scheduled email test - report. (Updated)");
            assertEquals(emailSchedulesPage.getSelectedFormats(), asList("Inline message", "PDF",
                    "Excel (XLS)", "Excel (XLSX)", "CSV"));
        } finally {
            initEmailSchedulesPage();
            if (emailSchedulesPage.isGlobalSchedulePresent(updatedTitle)) {
                waitForFragmentVisible(emailSchedulesPage).deleteSchedule(updatedTitle);
                return;
            }
            waitForFragmentVisible(emailSchedulesPage).deleteSchedule(title);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"verify-UI"})
    public void changeScheduleTime() {
        String title = "verify-UI-title";
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), title,
                "Scheduled email test - report.", "Actual QTD", ExportFormat.ALL);

        try {
            String timeDescription = "";
            for (RepeatTime time : RepeatTime.values()) {
                timeDescription = emailSchedulesPage.openSchedule(title)
                    .changeTime(time)
                    .getTimeDescription();
                emailSchedulesPage.saveSchedule();
                assertEquals(emailSchedulesPage.getScheduleDescription(title),
                        format(title + " (%s)", timeDescription));
            }
        } finally {
            waitForFragmentVisible(emailSchedulesPage).deleteSchedule(title);
        }
    }

    @Test(dependsOnGroups = {"schedules", "verify-UI"})
    public void verifyCreatedSchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfGlobalSchedules(), messages.size(),
                "Schedules are properly created.");
        takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMailRecurrency() throws IOException {
        initEmailSchedulesPage();
        updateRecurrencies(messages);
    }

    @Test(dependsOnMethods = {"updateScheduledMailRecurrency"})
    public void waitForScheduleMessages() throws MessagingException, IOException {
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

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyEmptyDashboardSchedule() {
        System.out.println("Email checks ...");
        int arrivedMessageCount = attachments.get(emptyDashboardTitle).totalMessage;
        if (arrivedMessageCount == 2) {
            if ((scheduledTimeInMinute <= 59 && scheduledTimeInMinute >= 50) || 
                    (scheduledTimeInMinute <= 29 && scheduledTimeInMinute >= 20)) {
                log.info("scheduled email is set up so close to the default scheduled time. So we get 2 emails.");
            } else {
                fail("ERROR: There are two message dashboard message arrived instead one.");
            }
        } else {
            assertEquals(arrivedMessageCount, 1, "ERROR: Dashboard message arrived.");
        }
        assertEquals(attachments.get(emptyDashboardTitle).savedAttachments.size(), 1,
                "ERROR: Dashboard message does not have correct number of attachments.");
        assertTrue(attachments.get(emptyDashboardTitle).savedAttachments.get(0).contentType
                .contains("application/pdf".toUpperCase()),
                "ERROR: Dashboard attachment does not have PDF content type.");
        verifyAttachment(attachments.get(emptyDashboardTitle).savedAttachments.get(0), "PDF", 22000);
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyNoDataReport() {
        String error = format("Report '%s' produced an empty result during conversion to '%s' format",
                NO_DATA_REPORT, "html");
        assertTrue(attachments.get(noDataReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyIncomputableReport() {
        String error = format("ERROR: report '%s' did not export to '%s' format", INCOMPUTABLE_REPORT, "pdf");
        assertTrue(attachments.get(incomputableReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyTooLargeReport() {
        String error = format("ERROR: report '%s' did not export to '%s' format", TOO_LARGE_REPORT, "pdf");
        assertTrue(attachments.get(tooLargeReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyFilteredVariableReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(filteredVariableReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("Email", "33920")),
                "Data in report is not correct!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyNumericVariableReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(numericVariableReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("2012", "38596194.86")),
                "Data in report is not correct!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyMufReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(mufReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("Explorer", "38596194.86")),
                "Data in report is not correct!");
    }

    private void updateRecurrencies(Map<String, List<Message>> messages) throws IOException {
        for (String recurrency : messages.keySet()) {
            updateRecurrencyString(emailSchedulesPage.getScheduleMailUriByName(recurrency));
        }
    }

    private void checkMailbox(ImapClient imapClient) throws MessagingException, IOException {
        getMessagesFromInbox(imapClient);
        saveMessageAttachments(messages);
    }

    private void saveMessageAttachments(Map<String, List<Message>> messages) throws MessagingException, IOException {
        for (String title : messages.keySet()) {
            System.out.println("Saving message ...");
            ImapClient.saveMessageAttachments(messages.get(title).get(0), attachmentsDirectory);
            attachments.put(title,
                    new MessageContent().setTotalMessage(messages.get(title).size())
                        .setBody(ImapClient.getEmailBody(messages.get(title).get(0))));
            List<Part> attachmentParts = ImapClient.getAttachmentParts(messages.get(title).get(0));
            List<SavedAttachment> savedAttachments = new ArrayList<SavedAttachment>();
            for (Part part : attachmentParts) {
                savedAttachments.add(new SavedAttachment().setContentType(part.getContentType())
                        .setSize(part.getSize()).setFileName(part.getFileName()));
            }
            attachments.get(title).setSavedAttachments(savedAttachments);
        }
    }

    private List<String> getCsvContent(File csvFile) throws IOException {
        List<String> reportResult = new ArrayList<String>();
        ICsvListReader listReader = null;
        List<String> result;

        try {
            listReader = new CsvListReader(new FileReader(csvFile), CsvPreference.STANDARD_PREFERENCE);

            listReader.getHeader(true);
            while ((result = listReader.read()) != null) {
                reportResult.addAll(result);
            }

            return reportResult;
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }

    private void getMessagesFromInbox(ImapClient imapClient) throws MessagingException {
        for (String title : messages.keySet()) {
            messages.put(title, waitForMessages(imapClient, GDEmails.NOREPLY, title));
        }
    }

    private void verifyAttachment(SavedAttachment attachment, String type, long minimalSize) {
        assertTrue(attachment.size > minimalSize, "The attachment (" + type + ") has the expected minimal size."
                + " Expected " + minimalSize + "B, found " + attachment.size + "B.");
    }

    private class SavedAttachment {
        private String contentType;
        private int size;
        private String fileName;

        public SavedAttachment setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public SavedAttachment setSize(int size) {
            this.size = size;
            return this;
        }

        public SavedAttachment setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
    }

    private class MessageContent {
        private int totalMessage;
        private List<SavedAttachment> savedAttachments;
        private String body;

        public MessageContent setTotalMessage(int totalMessage) {
            this.totalMessage = totalMessage;
            return this;
        }

        public MessageContent setSavedAttachments(List<SavedAttachment> savedAttachments) {
            this.savedAttachments = savedAttachments;
            return this;
        }

        public MessageContent setBody(String body) {
            this.body = body;
            return this;
        }
    }
}
