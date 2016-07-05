/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.logRedBarMessageInfo;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WATERFALL_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForSchedulesPageLoaded;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String SCHEDULE_WITHOUT_RECIPIENTS = "Schedule without recipient";
    private static final String SCHEDULE_WITH_INTERNAL_RECIPIENTS = 
            "Schedule with recipents are member of project";
    private static final String SCHEDULE_WITH_EXTERNAL_RECIPIENTS = "Schedule with recipents are external";
    private static final String SCHEDULE_WITH_INVALID_RECIPIENTS = "Schedule with invalid recipient";
    private static final String SCHEDULE_WITH_MORE_THAN_10_RECIPIENTS = "Schedule with more than 10 recipients";
    private static final String PUBLIC_SCHEDULE = "Public schedule test";
    private static final String PIPELINE_ANALYSIS_DASHBOARD = "Pipeline Analysis";
    private static final String DELETE = "Delete";

    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> CUSTOM_RECIPIENTS = asList("bear+1@gooddata.com", "bear+2@gooddata.com");
    private final List<String> SCHEDULED_TABS = asList(DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_WHATS_CHANGED);
    private final String SCHEDULE_INFO = "^This dashboard will be sent daily at 12:30 AM .* to %s "
            + "and 2 other recipients as a PDF attachment.$";
    private DateTimeZone tz = DateTimeZone.getDefault();

    @BeforeClass
    public void addUsers() {
        projectTitle = "GoodSales schedule dashboard test";
        addUsersWithOtherRoles = true;
    }

    // login with defined user role, fail test on error
    private void loginAs(UserRoles userRole) throws JSONException {
        logout();
        signIn(true, userRole); // login with gray pages to reload application and have feature flag set
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"prepareTests", "schedules"})
    public void setFeatureFlags() throws JSONException, IOException {
        disableHideDashboardScheduleFlag();
        enableDashboardScheduleRecipientsFlag();

        // need time for system apply feature flags setting
        sleepTightInSeconds(3);
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"prepareTests", "schedules"})
    public void publishDashboard() {
        initDashboardsPage();
        dashboardsPage.publishDashboard(true);
    }

    // prepare viewer user and login
    @Test(dependsOnGroups = {"prepareTests"}, groups = {"schedules"})
    public void createDashboardSchedule() throws JSONException {
        try {
            loginAs(UserRoles.VIEWER);
            GoodData goodDataClient = getGoodDataClient(testParams.getViewerUser(), 
                    testParams.getViewerPassword());
            String userUri = goodDataClient.getAccountService().getCurrent().getUri();
            initDashboardsPage();

            DashboardScheduleDialog dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();
            dashboardScheduleDialog.showCustomForm();

            dashboardScheduleDialog.selectTabs(new int[] { 1 });
            String[] tabNames = expectedGoodSalesDashboardsAndTabs.get(PIPELINE_ANALYSIS_DASHBOARD);
            assertEquals(dashboardScheduleDialog.getCustomEmailSubject(), dashboardsPage.getDashboardName()
                    + " Dashboard - " + tabNames[1], "Update of one Tab is reflected in subject.");

            dashboardScheduleDialog.selectTabs(new int[] { 1, 5, 6 });
            assertEquals(dashboardScheduleDialog.getCustomEmailSubject(), dashboardsPage.getDashboardName()
                    + " Dashboard", "Update of multiple Tabs is reflected in subject.");

            dashboardScheduleDialog.setCustomEmailSubject(CUSTOM_SUBJECT);
            dashboardScheduleDialog.setCustomRecipients(CUSTOM_RECIPIENTS);
            dashboardScheduleDialog.selectTabs(new int[] { 1, 2 });

            assertEquals(dashboardScheduleDialog.getCustomEmailSubject(), CUSTOM_SUBJECT,
                    "Update of Tabs is not reflected in subject.");
            dashboardScheduleDialog.selectTime(1);
            String expectedRegularExpression = String.format(SCHEDULE_INFO, testParams.getViewerUser())
                                                    .replaceAll("\\+", "\\\\+");
            String infoText = dashboardScheduleDialog.getInfoText();
            assertTrue(infoText.matches(expectedRegularExpression),
                    "Custom time is in info message, expected regular expression: " + 
                    expectedRegularExpression + ", found " + infoText + ".");
            // check time in info text
            dashboardScheduleDialog.setCustomEmailMessage(CUSTOM_MESSAGE);
            Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());
            dashboardScheduleDialog.schedule();
            // check dashboard schedule at manage page
            loginAs(UserRoles.ADMIN);
            initEmailSchedulesPage();
            assertDashboardScheduleInfo(CUSTOM_SUBJECT, userUri, CUSTOM_RECIPIENTS);
        } finally {
            loginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void deleteDashboardUsedInSchedule() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(PIPELINE_ANALYSIS_DASHBOARD);
        dashboardsPage.editDashboard();
        final DashboardEditBar editBar = dashboardsPage.getDashboardEditBar();
        editBar.tryToDeleteDashboard();
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver browser) {
                return browser.findElements(BY_RED_BAR).size() != 0;
            }
        });
        assertEquals(browser.findElement(BY_RED_BAR).getText(), CANNOT_DELETE_DASHBOARD_MESSAGE);
        logRedBarMessageInfo(browser);
        waitForElementVisible(By.cssSelector("div#status .s-btn-dismiss"), browser).click();
        editBar.cancelDashboard();
    }

    @Test(dependsOnGroups = {"schedules"}, alwaysRun = true)
    public void verifyRecipientsOfSchedule() throws JSONException, ParseException, IOException {
        loginAs(UserRoles.ADMIN);
        initEmailSchedulesPage();

        // get schedules via api, dashboard-based are not visible on manage page
        String uri = getScheduleUri(CUSTOM_SUBJECT);

        String[] parts = uri.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        JSONObject schedule = getObjectByID(id);
        JSONObject scheduledMailContent = schedule.getJSONObject("scheduledMail").getJSONObject("content");

        JSONArray toJson = scheduledMailContent.getJSONArray("to");
        Set<String> to = new HashSet<String>();
        for (int i = 0; i < toJson.length(); i++) {
            to.add(toJson.getString(i));
        }

        String subject = scheduledMailContent.getString("subject");
        String body = scheduledMailContent.getString("body");

        JSONArray attachmentsJson = scheduledMailContent.getJSONArray("attachments");
        JSONObject attachment = attachmentsJson.getJSONObject(0);
        JSONArray tabs = attachment.getJSONObject("dashboardAttachment").getJSONArray("tabs");

        JSONArray recipientsJson = scheduledMailContent.getJSONArray("bcc");
        Set<String> recipients = new HashSet<String>();
        for (int i = 0; i < recipientsJson.length(); i++) {
            recipients.add(recipientsJson.getString(i));
        }

        String timeZoneId = scheduledMailContent
                .getJSONObject("when")
                .getString("timeZone");
        DateTimeZone tzFromObj = DateTimeZone.forID(timeZoneId);

        // verify bcc
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-mdObject", this.getClass());

        assertEquals(to, new HashSet<String>(asList(testParams.getViewerUser())), "Current user is in 'To' field");
        assertEquals(subject, CUSTOM_SUBJECT, "Custom subject is in 'Subject' input field.");
        assertEquals(body, CUSTOM_MESSAGE, "Custom message is in 'Message' input field.");
        assertEquals(tabs.length(), SCHEDULED_TABS.size(), "Expecting two tabs to be attached to the scheduled mail");

        assertEquals(recipients, new HashSet<String>(CUSTOM_RECIPIENTS), "Recipients do not match.");
        assertEquals(tz.getStandardOffset(System.currentTimeMillis()), tzFromObj.getStandardOffset(System.currentTimeMillis()), "Timezones do not match");
    }

    @Test(dependsOnGroups = {"schedules"}, alwaysRun = true)
    public void verifyScheduleButtonPresenceOnEmbeddedDashboard() throws JSONException {
        // get embed link as admin (not accessible for viewer)
        loginAs(UserRoles.ADMIN);
        initDashboardsPage();
        EmbedDashboardDialog ded = dashboardsPage.openEmbedDashboardDialog();
        String embeddedDashboardUri = ded.getPreviewURI();

        try {
            loginAs(UserRoles.VIEWER);
            this.browser.get(embeddedDashboardUri);

            // wait for embedded dashboard to be fully loaded before checking
            waitForElementPresent(By.cssSelector(".embedded.s-dashboardLoaded"), this.browser);
            assertTrue(dashboardsPage.isScheduleButtonVisible());

            // re-login as admin for successfull project wipe; get to non-embedded to logout first
            initDashboardsPage();
            loginAs(UserRoles.ADMIN);
            Screenshots.takeScreenshot(browser, "Goodsales-schedules-embedded-dashboard", this.getClass());
        } finally {
            loginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"prepareTests"})
    public void preparePublicAndPrivateSchedules() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(PIPELINE_ANALYSIS_DASHBOARD);
        createDashboardSchedule(SCHEDULE_WITHOUT_RECIPIENTS, Collections.<String>emptyList());
        createDashboardSchedule(SCHEDULE_WITH_INTERNAL_RECIPIENTS,
                asList(testParams.getEditorUser(), testParams.getViewerUser()));
        createDashboardSchedule(SCHEDULE_WITH_EXTERNAL_RECIPIENTS, asList(imapUser));
        createDashboardSchedule(SCHEDULE_WITH_INVALID_RECIPIENTS, asList("invalid"),
                "Incorrect format. Enter a list of comma-separated email addresses");
        List<String> recipients = new ArrayList<String>();
        for (int i = 1; i <= 12; i++) {
            recipients.add("bear+" + i + "@gooddata.com");
        }
        createDashboardSchedule(SCHEDULE_WITH_MORE_THAN_10_RECIPIENTS, recipients,
                "Maximum of ten recipients allowed. Remove some recipients.");

        initEmailSchedulesPage();
        String schedule = "Public schedule test";
        emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), schedule,
                "Scheduled email test - dashboard.", "Outlook");
    }

    @Test(dependsOnMethods = {"preparePublicAndPrivateSchedules"})
    public void createPrivateDashboardSchedules() throws JSONException {
        initEmailSchedulesPage();
        String userUri = getGoodDataClient().getAccountService().getCurrent().getUri();
        refreshSchedulesPage();
        assertDashboardScheduleInfo(SCHEDULE_WITHOUT_RECIPIENTS, 
        		userUri, Collections.<String>emptyList());
        assertDashboardScheduleInfo(SCHEDULE_WITH_INTERNAL_RECIPIENTS,
        		userUri, asList(testParams.getEditorUser(), testParams.getViewerUser()));
        assertDashboardScheduleInfo(SCHEDULE_WITH_EXTERNAL_RECIPIENTS,
        		userUri, asList(imapUser));
    }

    @Test(dependsOnMethods = {"preparePublicAndPrivateSchedules"})
    public void checkVariousCombinationsOfFeatureFlag() throws JSONException, IOException {
        try {
            disableHideDashboardScheduleFlag();
            disableDashboardScheduleRecipientsFlag();

            initEmailSchedulesPage();
            refreshSchedulesPage();
            assertFalse(emailSchedulesPage.isBccColumnPresent(), "Bcc columns were not displayed as expected!");
            assertTrue(emailSchedulesPage.isPrivateSchedulesTableVisible(),
                    "Private Schedules Created on Dashboard table was not displayed!");
            assertTrue(emailSchedulesPage.isGlobalSchedulePresent(PUBLIC_SCHEDULE),
                    "Public schedule was not displayed!");

            enableHideDashboardScheduleFlag();
            enableDashboardScheduleRecipientsFlag();
            checkOnlyPublicScheduleVisible();

            enableHideDashboardScheduleFlag();
            disableDashboardScheduleRecipientsFlag();
            checkOnlyPublicScheduleVisible();
        } finally {
            disableHideDashboardScheduleFlag();
            enableDashboardScheduleRecipientsFlag();
        }
    }

    @Test(dependsOnGroups = {"prepareTests"})
    public void createScheduleEmailsForPublicAndPrivateDashboard() throws JSONException {
        String publicDashboard = "Public Dashboard Test";
        String privateDashboard = "Private Dashboard Test";

        initDashboardsPage();
        dashboardsPage.addNewDashboard(publicDashboard);
        dashboardsPage.publishDashboard(true);
        dashboardsPage.editDashboard();
        DashboardEditBar editDashboardBar =  dashboardsPage.getDashboardEditBar();
        editDashboardBar.addReportToDashboard("Activities by Type");
        editDashboardBar.saveDashboard();
        createDefaultDashboardSchedule();

        initDashboardsPage();
        dashboardsPage.addNewDashboard(privateDashboard);
        dashboardsPage.publishDashboard(false);
        createDefaultDashboardSchedule();

        try {
            loginAs(UserRoles.EDITOR);
            initEmailSchedulesPage();
            assertTrue(emailSchedulesPage.isPrivateSchedulePresent(publicDashboard), 
                    "Schedule of public dashboard " + publicDashboard + " was not present!");
            assertTrue(emailSchedulesPage.isPrivateSchedulePresent(privateDashboard),
                    "Schedule of private dashboard " + privateDashboard + " was not present!");
            assertTrue(emailSchedulesPage.isPrivateSchedulesTableVisible(),
                    "Private schedule table was not present!");
        } finally {
            loginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"prepareTests"})
    public void deleteUserOnPrivateScheduledEmails() throws ParseException, JSONException, IOException {
        String userA = "qa+test+schedule+a@gooddata.com";
        String userB = "qa+test+schedule+b@gooddata.com";
        String scheduleUserA = "Schedule with deleted bcc email";
        String scheduleUserB = "Schedule with deleted author";
        String userAUri = UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), userA,
                testParams.getPassword());
        String userBUri = UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), userB,
                testParams.getPassword());

        try {
            String userUri = getGoodDataClient().getAccountService().getCurrent().getUri();
            UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), userA, UserRoles.EDITOR);
            UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), userB, UserRoles.ADMIN);

            initDashboardsPage();
            dashboardsPage.selectDashboard(PIPELINE_ANALYSIS_DASHBOARD);
            createDashboardSchedule(scheduleUserA, asList(userA));
            UserManagementRestUtils.deleteUserByUri(getRestApiClient(), userAUri);

            initEmailSchedulesPage();
            assertDashboardScheduleInfo(scheduleUserA, userUri, asList(userA));

            logout();
            signInAtGreyPages(userB, testParams.getPassword());
            initDashboardsPage();
            createDashboardSchedule(scheduleUserA, Collections.<String>emptyList());

            logout();
            signIn(true, UserRoles.ADMIN);
            UserManagementRestUtils.deleteUserByUri(getRestApiClient(), userBUri);

            initEmailSchedulesPage();
            assertFalse(emailSchedulesPage.isPrivateSchedulePresent(scheduleUserB),
                    "Schedule of deleted user was not hidden.");
        } finally {
            loginAs(UserRoles.ADMIN);
            UserManagementRestUtils.deleteUserByUri(getRestApiClient(), userAUri);
            UserManagementRestUtils.deleteUserByUri(getRestApiClient(), userBUri);
        }
    }

    private void checkOnlyPublicScheduleVisible() {
        initEmailSchedulesPage();
        refreshSchedulesPage();
        assertTrue(emailSchedulesPage.isGlobalSchedulePresent(PUBLIC_SCHEDULE), "Public schedule was displayed!");

        assertFalse(emailSchedulesPage.isPrivateSchedulesTableVisible(),
                "Private Schedules Created on Dashboard table was not hidden");

        assertFalse(emailSchedulesPage.isGlobalSchedulePresent(SCHEDULE_WITHOUT_RECIPIENTS),
                "Private Schedule" + SCHEDULE_WITHOUT_RECIPIENTS + " was not displayed as expected!");
        assertFalse(emailSchedulesPage.isGlobalSchedulePresent(SCHEDULE_WITH_EXTERNAL_RECIPIENTS),
                "Private Schedule" + SCHEDULE_WITH_EXTERNAL_RECIPIENTS + " was not displayed as expected!");
        assertFalse(emailSchedulesPage.isGlobalSchedulePresent(SCHEDULE_WITH_INTERNAL_RECIPIENTS),
                "Private Schedule" + SCHEDULE_WITH_INTERNAL_RECIPIENTS + " was not displayed as expected!");
    }

    private void createDashboardSchedule(String subject, List<String> recipients, String error) {
        initDashboardsPage();
        DashboardScheduleDialog dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();
        dashboardScheduleDialog.showCustomForm();
        dashboardScheduleDialog.selectTabs(new int[]{1, 5, 6});
        dashboardScheduleDialog.setCustomEmailSubject(subject);
        dashboardScheduleDialog.setCustomRecipients(recipients);
        dashboardScheduleDialog.selectTime(1);
        Screenshots.takeScreenshot(browser, "Screenshot " + subject, this.getClass());
        if(!dashboardScheduleDialog.schedule()) {
            assertEquals(dashboardScheduleDialog.getErrorMessage(), error);
            dashboardScheduleDialog.cancelSchedule();
        } else {
            waitForElementVisible(cssSelector("#status .box-success button"), browser).click();
        }
    }

    private void createDashboardSchedule(String subject, List<String> recipients) {
        createDashboardSchedule(subject, recipients, "");
    }

    private void createDefaultDashboardSchedule() {
        initDashboardsPage();
        DashboardScheduleDialog dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();
        dashboardScheduleDialog.schedule();
        waitForElementVisible(cssSelector("#status .box-success button"), browser).click();
    }

    private String getScheduleUri(String scheduleTitle) throws JSONException, IOException {
        final String schedulesUri = "/gdc/md/" + testParams.getProjectId() + "/query/scheduledmails";
        final JSONArray schedules = RestUtils.getJsonObject(getRestApiClient(), schedulesUri)
            .getJSONObject("query")
            .getJSONArray("entries");

        for (int i = 0, n = schedules.length(); i < n; i++) {
            final JSONObject schedule = schedules.getJSONObject(i);
            if (schedule.getString("title").equals(scheduleTitle)) {
                return schedule.getString("link");
            }
        }
        return null;
    }

    private void enableHideDashboardScheduleFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.HIDE_DASHBOARD_SCHEDULE, true);
    }

    private void disableHideDashboardScheduleFlag() throws JSONException, IOException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.HIDE_DASHBOARD_SCHEDULE, false);
    }

    private void enableDashboardScheduleRecipientsFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, true);
    }

    private void disableDashboardScheduleRecipientsFlag() throws JSONException, IOException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, false);
    }

    private void assertDashboardScheduleInfo(String title, String authorUri, Collection<String> bccEmails) {
        assertTrue(emailSchedulesPage.isPrivateSchedulePresent(title), "Dashboard schedule was not displayed.");
        WebElement schedule = emailSchedulesPage.getPrivateSchedule(title);

        assertEquals(emailSchedulesPage.getAuthorUriOfSchedule(schedule), authorUri,
                "Author uri of schedule mail was not correct!");

        assertEquals(emailSchedulesPage.getBccEmailsOfPrivateSchedule(schedule),
                Joiner.on(", ").join(bccEmails), "List of bcc emails was not correct.");

        List<String> controls = emailSchedulesPage.getControlsOfSchedule(schedule);
        assertEquals(controls.size(), 1, "List of controls button was not correct.");
        assertEquals(controls.get(0), DELETE, "Control button text was not correct.");
    }

    private void refreshSchedulesPage() {
        browser.navigate().refresh();
        waitForSchedulesPageLoaded(browser);
    }
}
