/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR_WARNING;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.logRedBarMessageInfo;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_INCOMPUTABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NO_DATA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOO_LARGE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createSimpleMufObjByUri;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.updateEmailOfAccount;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.java.Builder;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
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
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage.RepeatTime;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.base.Predicate;

public class GoodSalesEmailSchedulesFullTest extends AbstractGoodSalesEmailSchedulesTest {

    private String emptyDashboardTitle = "Empty-Dashboard";
    private String filteredVariableReportTitle = "Filtered-Variable-Report";
    private String numericVariableReportTitle = "Numeric-Variable-Report";
    private String mufReportTitle = "MUF-Report";
    private String noDataReportTitle = REPORT_NO_DATA;
    private String incomputableReportTitle = REPORT_INCOMPUTABLE;
    private String tooLargeReportTitle = REPORT_TOO_LARGE;

    private static final String DASHBOARD_HAVING_TAB = "Dashboard having tab";
    private static final String OTHER_DASHBOARD_HAVING_TAB = "Other dashboard having tab";
    private static final String EDITOR_EMAIL = "editoremail@gooddata.com";

    private Map<String, List<Message>> messages;
    private Map<String, MessageContent> attachments = new HashMap<String, MessageContent>();

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        String editor = createAndAddUserToProject(UserRoles.EDITOR);
        updateEmailOfAccount(getRestApiClient(), testParams.getUserDomain(), editor, EDITOR_EMAIL);
    }

    @BeforeClass(alwaysRun = true)
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

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        getReportCreator().createEmptyReport();
        getReportCreator().createIncomputableReport();
        getReportCreator().createTooLargeReport();
        getReportCreator().createActivitiesByTypeReport();

        createSimpleDashboard(DASHBOARD_HAVING_TAB);
        createSimpleDashboard(OTHER_DASHBOARD_HAVING_TAB);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void signInImapUser() throws JSONException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }

    /**
     * Automate testscase for Jira ticket CL-12102
     * testcase is "disable a user which is the only one used in schedule email, then try to save the schedule email
     * with empty Emails To field, and make sure schedule email to save success after add a new email".
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    @Test(dependsOnMethods = {"signInImapUser"}, groups = {"schedules"})
    public void verifyEmailToWhenOnlyUserIsDisabled() throws JSONException, ParseException, IOException {
        //automate testscase for Jira ticket CL-12102
        String dashboard = "dashboard " + generateHashString();
        EmailSchedulePage emailSchedulePage = initEmailSchedulesPage().scheduleNewDashboardEmail(
                singletonList(testParams.getUser()), dashboard, "Scheduled email test - dashboard.",
                singletonList(DASHBOARD_HAVING_TAB));
        UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getUser(), UserManagementRestUtils.UserStatus.DISABLED);
        try {
            assertFalse(emailSchedulePage.openSchedule(dashboard).getEmailToListItem().contains(testParams.getUser()));

            emailSchedulePage.trySaveSchedule();
            assertEquals(emailSchedulePage.getValidationErrorMessages(), "Should not be empty");

            emailSchedulePage.changeEmailTo(dashboard, singletonList(imapUser));
            UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getUser(), UserManagementRestUtils.UserStatus.ENABLED);
            assertEquals(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(), asList(imapUser, testParams.getUser()));
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboard);
            UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getUser(), UserManagementRestUtils.UserStatus.ENABLED);
        }
    }

    /**
     * Automate testscase for Jira ticket CL-12102
     * testcase is "disable a user which is one of used users in schedule email, then change dashboard and save schedule".
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    @Test(dependsOnMethods = {"signInImapUser"}, groups = {"schedules"})
    public void verifyEmailToWhenOneOfUsersIsDisabled() throws JSONException, ParseException, IOException {
        String dashboard = "dashboard " + generateHashString();
        EmailSchedulePage emailSchedulePage = initEmailSchedulesPage();
        emailSchedulePage.scheduleNewDashboardEmail(asList(imapUser, testParams.getUser()), dashboard,
                "Scheduled email test - dashboard.", singletonList(DASHBOARD_HAVING_TAB));
        UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getUser(), UserManagementRestUtils.UserStatus.DISABLED);
        try {
            assertFalse(emailSchedulePage.openSchedule(dashboard).getEmailToListItem().contains(testParams.getUser()));
            emailSchedulePage.changeDashboards(dashboard, asList(DASHBOARD_HAVING_TAB, OTHER_DASHBOARD_HAVING_TAB));
            UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getUser(), UserManagementRestUtils.UserStatus.ENABLED);
            assertEquals(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(),
                    asList(imapUser, testParams.getUser()));
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboard);
            UserManagementRestUtils.updateUserStatusInProject(getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getUser(), UserManagementRestUtils.UserStatus.ENABLED);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void createEmptyDashboardSchedule() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Empty dashboard");
        initEmailSchedulesPage().scheduleNewDashboardEmail(singletonList(imapUser), emptyDashboardTitle,
                "Scheduled email test - empty dashboard.", singletonList("First Tab"));
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-empty-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"createEmptyDashboardSchedule"}, groups = {"schedules"})
    public void checkEmailToField() {
        assertEquals(initEmailSchedulesPage().openNewSchedule().getEmailToListItem().size(),0);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void deleteDashboardUsedInSchedule() {
        String dashboardTitle = "Schedule dashboard";
        initEmailSchedulesPage().scheduleNewDashboardEmail(singletonList(imapUser), dashboardTitle,
                "Scheduled email test - dashboard.", singletonList(DASHBOARD_HAVING_TAB));

        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_HAVING_TAB);
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
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboardTitle);
        }
    }

    @Test(dependsOnMethods = {"createEmptyDashboardSchedule"}, groups = {"schedules"})
    public void duplicateSchedule() {
        waitForFragmentVisible(initEmailSchedulesPage()).duplicateSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnMethods = {"duplicateSchedule"}, groups = {"schedules"})
    public void deleteSchedule() {
        waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleEmptyReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), noDataReportTitle,
                "Scheduled email test - no data report.", REPORT_NO_DATA, ExportFormat.ALL);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-no-data-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleIncomputableReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), incomputableReportTitle,
                "Scheduled email test - incomputable report.", REPORT_INCOMPUTABLE, ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-incomputable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleTooLargeReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), tooLargeReportTitle,
                "Scheduled email test - too large report.", REPORT_TOO_LARGE, ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-too-large-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleReportApplyFilteredVariable() {
        initVariablePage().createVariable(new AttributeVariable("FVariable")
            .withAttribute(ATTR_ACTIVITY_TYPE).withAttributeValues("Email"));

        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName("Filtered variable report")
                .withHows(ATTR_ACTIVITY_TYPE).withWhats(METRIC_NUMBER_OF_ACTIVITIES);
        createReport(rd, "Filtered variable report");
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FVariable", "Email"));
        reportPage.saveReport();

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), filteredVariableReportTitle,
                "Scheduled email test - Filtered variable report.", "Filtered variable report",
                ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-filtered-variable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleReportApplyNumericVariable() {
        String variableUri = initVariablePage().createVariable(new NumericVariable("NVariable").withDefaultNumber(2012));

        String report = "Sum amount in 2012";
        String expression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri(), variableUri);

        Metric metric = createMetric(report, expression, "#,##0");
        Attribute yearSnapshot = getAttributeByIdentifier("snapshot.year");
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(yearSnapshot.getDefaultDisplayForm().getUri(), yearSnapshot.getTitle())),
                singletonList(new MetricElement(metric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), numericVariableReportTitle,
                "Scheduled email test - Numeric variable report.", report, ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-numeric-variable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleMufReport() throws IOException, JSONException {
        initEmailSchedulesPage();
        Attribute product = getAttributeByTitle(ATTR_PRODUCT);
        Metric amountMetric = getMetricByTitle(METRIC_AMOUNT);
        String report = "MUF report";

        final String explorerUri = getMdService().getAttributeElements(product).stream()
                .filter(e -> e.getTitle().equals("Explorer")).findFirst().get().getUri();

        Map<String, Collection<String>> conditions = new HashMap();
        conditions.put(product.getUri(), singletonList(explorerUri));
        String mufUri =
                createSimpleMufObjByUri(getRestApiClient(), getProject().getId(), "Product user filter", conditions);
        addMufToUser(getRestApiClient(), getProject().getId(),
                UserManagementRestUtils.getUserProfileUri(
                        getDomainUserRestApiClient(), testParams.getUserDomain(), imapUser),
                mufUri);

        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(product.getDefaultDisplayForm().getUri(), product.getTitle())),
                singletonList(new MetricElement(amountMetric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(asList(imapUser, testParams.getUser(), EDITOR_EMAIL), mufReportTitle,
                "Scheduled email test - MUF report.", report, ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-muf-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void deleteReport() {
        String title = "verify-UI-title";
        String report = "# test report";

        initReportsPage();
        Metric amountMetric = getMetricByTitle(METRIC_AMOUNT);

        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                Collections.<AttributeInGrid>emptyList(),
                singletonList(new MetricElement(amountMetric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", report, ExportFormat.ALL);

        try {
            initReportsPage()
                .tryDeleteReports(report);
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
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(title);
            initReportsPage()
                .deleteReports(report);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void editSchedule() {
        String title = "verify-UI-title";
        String updatedTitle = title + "Updated";
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", REPORT_ACTIVITIES_BY_TYPE, ExportFormat.SCHEDULES_EMAIL_CSV);

        try {
            EmailSchedulePage.getInstance(browser).openSchedule(title)
                .setSubject(updatedTitle)
                .setMessage("Scheduled email test - report. (Updated)")
                .selectReportFormat(ExportFormat.ALL)
                .saveSchedule();
            assertFalse(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(title));
            assertTrue(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(updatedTitle));

            assertEquals(EmailSchedulePage.getInstance(browser).openSchedule(updatedTitle).getMessageFromInput(),
                    "Scheduled email test - report. (Updated)");
            assertEquals(EmailSchedulePage.getInstance(browser).getSelectedFormats(), asList("Inline message", "PDF", "Excel (XLSX)", "CSV"));
        } finally {
            if (initEmailSchedulesPage().isGlobalSchedulePresent(updatedTitle)) {
                waitForFragmentVisible(EmailSchedulePage.getInstance(browser)).deleteSchedule(updatedTitle);
                return;
            }
            waitForFragmentVisible(EmailSchedulePage.getInstance(browser)).deleteSchedule(title);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void changeScheduleTime() {
        String title = "verify-UI-title";
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", REPORT_ACTIVITIES_BY_TYPE, ExportFormat.ALL);

        try {
            String timeDescription = "";
            for (RepeatTime time : RepeatTime.values()) {
                timeDescription = EmailSchedulePage.getInstance(browser).openSchedule(title)
                    .changeTime(time)
                    .getTimeDescription();
                EmailSchedulePage.getInstance(browser).saveSchedule();
                assertEquals(EmailSchedulePage.getInstance(browser).getScheduleDescription(title),
                        format(title + " (%s)", timeDescription));
            }
        } finally {
            EmailSchedulePage.getInstance(browser).deleteSchedule(title);
        }
    }

    @Test(dependsOnGroups = {"schedules", "verify-UI"})
    public void verifyCreatedSchedules() {
        assertEquals(initEmailSchedulesPage().getNumberOfGlobalSchedules(), messages.size(),
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
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("ACCELERATE scheduled mails processing");
            ScheduleEmailRestUtils.accelerate(getRestApiClient(imapUser, imapPassword), testParams.getProjectId());
            checkMailbox(imapClient);
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            ScheduleEmailRestUtils.decelerate(getRestApiClient(imapUser, imapPassword), testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyEmptyDashboardSchedule() {
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
                REPORT_NO_DATA, "html");
        assertTrue(attachments.get(noDataReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyIncomputableReport() {
        String error = format("Report '%s' you wanted to export to '%s' format is not currently computable",
                REPORT_INCOMPUTABLE, "pdf");
        assertTrue(attachments.get(incomputableReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyTooLargeReport() {
        String error = format("Report '%s' cannot be exported to '%s' format as it is too large", REPORT_TOO_LARGE,
                "pdf");
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

    @Test(dependsOnMethods = {"waitForScheduleMessages"})
    public void verifyEmailToWhenUserHasDifferentLoginAndEmail() {
        //To test for issue CL-12072
        assertEquals(initEmailSchedulesPage().openSchedule(mufReportTitle).getEmailToListItem(),
                asList(imapUser, testParams.getUser(), EDITOR_EMAIL));
        Screenshots.takeScreenshot(browser, "verify email", getClass());
    }

    private void createSimpleDashboard(String title) throws IOException {
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(title);
            dash.addTab(Builder.of(Tab::new).with(tab -> tab.setTitle("Tab")).build());
        }).build();

        DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(), dashboard.getMdObject());
    }

    private void updateRecurrencies(Map<String, List<Message>> messages) throws IOException {
        for (String recurrency : messages.keySet()) {
            updateRecurrencyString(EmailSchedulePage.getInstance(browser).getScheduleMailUriByName(recurrency));
        }
    }

    private void checkMailbox(ImapClient imapClient) throws MessagingException, IOException {
        getMessagesFromInbox(imapClient);
        saveMessageAttachments(messages);
    }

    private void saveMessageAttachments(Map<String, List<Message>> messages) throws MessagingException, IOException {
        for (String title : messages.keySet()) {
            System.out.println("Saving message ...");
            ImapUtils.saveMessageAttachments(messages.get(title).get(0), attachmentsDirectory);
            attachments.put(title, new MessageContent().setBody(ImapUtils.getEmailBody(messages.get(title).get(0))));
            List<Part> attachmentParts = ImapUtils.getAttachmentParts(messages.get(title).get(0));
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
            messages.put(title, waitForMessages(imapClient, GDEmails.NOREPLY, title, 1));
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
        private List<SavedAttachment> savedAttachments;
        private String body;

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
