/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.logRedBarMessageInfo;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WATERFALL_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
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

import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Joiner;

public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String SCHEDULE_WITHOUT_RECIPIENTS = "Schedule without recipient";
    private static final String SCHEDULE_WITH_INTERNAL_RECIPIENTS =
            "Schedule with recipents are member of project";
    private static final String SCHEDULE_WITH_EXTERNAL_RECIPIENTS = "Schedule with recipents are external";
    private static final String SCHEDULE_WITH_INVALID_RECIPIENTS = "Schedule with invalid recipient";
    private static final String SCHEDULE_WITH_MORE_THAN_10_RECIPIENTS = "Schedule with more than 10 recipients";
    private static final String PUBLIC_SCHEDULE = "Public schedule test";
    private static final String DELETE = "Delete";

    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> CUSTOM_RECIPIENTS = asList("bear+1@gooddata.com", "bear+2@gooddata.com");
    private final List<String> SCHEDULED_TABS = asList(DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_WHATS_CHANGED);
    private final String SCHEDULE_INFO = "^This dashboard will be sent daily at 12:30 AM .* to %s "
            + "and 2 other recipients as a PDF attachment. It will include all currently active filters.$";
    private final String DASHBOARD_HAVING_MANY_TABS = "Dashboard having many tabs";
    private DateTimeZone tz = DateTimeZone.getDefault();

    private String externalUser;
    private List<String> tabNames = asList("Tab 1", "Tab 2", "Tab 3", "Tab 4", "Tab 5", "Tab 6", "Tab 7");

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales schedule dashboard test";
    }


    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
        externalUser = createDynamicUserFrom(testParams.getUser());
    }

    @Override
    protected void customizeProject() throws Throwable {
        disableHideDashboardScheduleFlag();
        enableDashboardScheduleRecipientsFlag();

        getReportCreator().createActivitiesByTypeReport();
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_MANY_TABS);
            tabNames.forEach(name -> dash.addTab(Builder.of(Tab::new).with(tab -> tab.setTitle(name)).build()));
        }).build();

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(dashboard.getMdObject());
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DASHBOARD_ACCESS_CONTROL, true);

        initDashboardsPage();
        dashboardsPage.addSampleTextToDashboard().publishDashboard(true);
    }

    // prepare viewer user and login
    @Test(dependsOnGroups = {"createProject"}, groups = {"schedules"})
    public void createDashboardSchedule() throws JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            RestClient restClient = new RestClient(
                    new RestProfile(testParams.getHost(), testParams.getViewerUser(), testParams.getPassword(), true));
            String userUri = restClient.getAccountService().getCurrent().getUri();
            initDashboardsPage();

            DashboardScheduleDialog dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();
            dashboardScheduleDialog.showCustomForm();

            dashboardScheduleDialog.selectTabs(new int[] { 1 });
            assertEquals(dashboardScheduleDialog.getCustomEmailSubject(), dashboardsPage.getDashboardName()
                    + " Dashboard - " + tabNames.get(1), "Update of one Tab is reflected in subject.");

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
            logoutAndLoginAs(true, UserRoles.ADMIN);
            initEmailSchedulesPage();
            assertDashboardScheduleInfo(CUSTOM_SUBJECT, userUri, CUSTOM_RECIPIENTS);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void deleteDashboardUsedInSchedule() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(DASHBOARD_HAVING_MANY_TABS);
        dashboardsPage.editDashboard();
        final DashboardEditBar editBar = dashboardsPage.getDashboardEditBar();
        editBar.tryToDeleteDashboard();
        Graphene.waitGui().until(browser -> browser.findElements(BY_RED_BAR).size() != 0);
        assertEquals(waitForElementVisible(browser.findElement(BY_RED_BAR)).getText(), CANNOT_DELETE_DASHBOARD_MESSAGE);
        logRedBarMessageInfo(browser);
        waitForElementVisible(By.cssSelector("div#status .s-btn-dismiss"), browser).click();
        editBar.cancelDashboard();
    }

    @Test(dependsOnGroups = {"schedules"})
    public void disableScheduleOnEmptyDashboardTab() {
        initDashboardsPage();
        checkScheduleButtonStatusThroughTabs();
    }

    @Test(dependsOnGroups = {"schedules"}, alwaysRun = true)
    public void verifyRecipientsOfSchedule() throws JSONException, ParseException, IOException {
        initEmailSchedulesPage();

        // get schedules via api, dashboard-based are not visible on manage page
        String uri = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId())
                .getScheduleUri(CUSTOM_SUBJECT);

        String[] parts = uri.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        JSONObject schedule = getObjectByID(id);
        JSONObject scheduledMailContent = schedule.getJSONObject("scheduledMail").getJSONObject("content");

        JSONArray toJson = scheduledMailContent.getJSONArray("to");
        Set<String> to = new HashSet<>();
        for (int i = 0; i < toJson.length(); i++) {
            to.add(toJson.getString(i));
        }

        String subject = scheduledMailContent.getString("subject");
        String body = scheduledMailContent.getString("body");

        JSONArray attachmentsJson = scheduledMailContent.getJSONArray("attachments");
        JSONObject attachment = attachmentsJson.getJSONObject(0);
        JSONArray tabs = attachment.getJSONObject("dashboardAttachment").getJSONArray("tabs");

        JSONArray recipientsJson = scheduledMailContent.getJSONArray("bcc");
        Set<String> recipients = new HashSet<>();
        for (int i = 0; i < recipientsJson.length(); i++) {
            recipients.add(recipientsJson.getString(i));
        }

        String timeZoneId = scheduledMailContent
                .getJSONObject("when")
                .getString("timeZone");
        DateTimeZone tzFromObj = DateTimeZone.forID(timeZoneId);

        // verify bcc
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-mdObject", this.getClass());

        assertEquals(to, new HashSet<>(asList(testParams.getViewerUser())), "Current user is in 'To' field");
        assertEquals(subject, CUSTOM_SUBJECT, "Custom subject is in 'Subject' input field.");
        assertEquals(body, CUSTOM_MESSAGE, "Custom message is in 'Message' input field.");
        assertEquals(tabs.length(), SCHEDULED_TABS.size(), "Expecting two tabs to be attached to the scheduled mail");

        assertEquals(recipients, new HashSet<>(CUSTOM_RECIPIENTS), "Recipients do not match.");
        assertEquals(tz.getStandardOffset(System.currentTimeMillis()), tzFromObj.getStandardOffset(System.currentTimeMillis()), "Timezones do not match");
    }

    @Test(dependsOnGroups = {"schedules"}, alwaysRun = true)
    public void verifyScheduleButtonPresenceOnEmbeddedDashboard() throws JSONException {
        initDashboardsPage();
        EmbedDashboardDialog ded = dashboardsPage.openEmbedDashboardDialog();
        String embeddedDashboardUri = ded.getPreviewURI();
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            this.browser.get(embeddedDashboardUri);

            // wait for embedded dashboard to be fully loaded before checking
            waitForElementPresent(By.cssSelector(".embedded.s-dashboardLoaded"), this.browser);
            assertTrue(dashboardsPage.isScheduleButtonVisible(), "Schedule button should be visible");

            checkScheduleButtonStatusThroughTabs();
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void preparePublicAndPrivateSchedules() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(DASHBOARD_HAVING_MANY_TABS);
        createDashboardSchedule(SCHEDULE_WITHOUT_RECIPIENTS, Collections.emptyList());
        createDashboardSchedule(SCHEDULE_WITH_INTERNAL_RECIPIENTS,
                asList(testParams.getEditorUser(), testParams.getViewerUser()));
        createDashboardSchedule(SCHEDULE_WITH_EXTERNAL_RECIPIENTS, asList(externalUser));
        createDashboardSchedule(SCHEDULE_WITH_INVALID_RECIPIENTS, asList("invalid"),
                "Incorrect format. Enter a list of comma-separated email addresses");
        List<String> recipients = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            recipients.add("bear+" + i + "@gooddata.com");
        }
        createDashboardSchedule(SCHEDULE_WITH_MORE_THAN_10_RECIPIENTS, recipients,
                "Maximum of ten recipients allowed. Remove some recipients.");

        String schedule = "Public schedule test";
        initEmailSchedulesPage().scheduleNewDashboardEmail(asList(testParams.getUser()), schedule,
                "Scheduled email test - dashboard.", asList(DASHBOARD_HAVING_MANY_TABS));
    }

    @Test(dependsOnMethods = {"preparePublicAndPrivateSchedules"})
    public void createPrivateDashboardSchedules() throws JSONException {
        String userUri = new RestClient(getProfile(Profile.ADMIN)).getAccountService().getCurrent().getUri();
        refreshSchedulesPage();
        assertDashboardScheduleInfo(SCHEDULE_WITHOUT_RECIPIENTS,
                userUri, Collections.emptyList());
        assertDashboardScheduleInfo(SCHEDULE_WITH_INTERNAL_RECIPIENTS,
                userUri, asList(testParams.getEditorUser(), testParams.getViewerUser()));
        assertDashboardScheduleInfo(SCHEDULE_WITH_EXTERNAL_RECIPIENTS,
                userUri, asList(externalUser));
    }

    @Test(dependsOnMethods = {"preparePublicAndPrivateSchedules"})
    public void checkVariousCombinationsOfFeatureFlag() throws JSONException {
        try {
            disableHideDashboardScheduleFlag();
            disableDashboardScheduleRecipientsFlag();

            assertFalse(refreshSchedulesPage().isBccColumnPresent(), "Bcc columns were not displayed as expected!");
            assertTrue(EmailSchedulePage.getInstance(browser).isPrivateSchedulesTableVisible(),
                    "Private Schedules Created on Dashboard table was not displayed!");
            assertTrue(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(PUBLIC_SCHEDULE),
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

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleEmailsForPublicAndPrivateDashboard() throws JSONException {
        String publicDashboard = "Public Dashboard Test";
        String privateDashboard = "Private Dashboard Test";

        initDashboardsPage();
        dashboardsPage.addNewDashboard(publicDashboard);
        dashboardsPage.publishDashboard(true);
        dashboardsPage.editDashboard();
        DashboardEditBar editDashboardBar =  dashboardsPage.getDashboardEditBar();
        editDashboardBar.addReportToDashboard(REPORT_ACTIVITIES_BY_TYPE);
        editDashboardBar.saveDashboard();
        createDefaultDashboardSchedule();

        initDashboardsPage();
        dashboardsPage.addNewDashboard(privateDashboard);
        dashboardsPage.addSampleTextToDashboard().publishDashboard(false);
        createDefaultDashboardSchedule();
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            assertTrue(initEmailSchedulesPage().isPrivateSchedulePresent(publicDashboard),
                    "Schedule of public dashboard " + publicDashboard + " was not present!");
            assertTrue(EmailSchedulePage.getInstance(browser).isPrivateSchedulePresent(privateDashboard),
                    "Schedule of private dashboard " + privateDashboard + " was not present!");
            assertTrue(EmailSchedulePage.getInstance(browser).isPrivateSchedulesTableVisible(),
                    "Private schedule table was not present!");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteUserOnPrivateScheduledEmails() throws ParseException, JSONException, IOException {
        String scheduleEmail = testParams.getUser().replace("@", "+schedule@");
        String userA = createDynamicUserFrom(scheduleEmail);
        String userB = createDynamicUserFrom(scheduleEmail);
        String scheduleUserA = "Schedule with deleted bcc email";
        String scheduleUserB = "Schedule with deleted author";
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        try {
            String userUri = new RestClient(getProfile(Profile.ADMIN)).getAccountService().getCurrent().getUri();
            userManagementRestRequest.addUserToProject(userA, UserRoles.EDITOR);
            userManagementRestRequest.addUserToProject(userB, UserRoles.ADMIN);

            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_HAVING_MANY_TABS);
            createDashboardSchedule(scheduleUserA, asList(userA));
            userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), userA);

            initEmailSchedulesPage();
            assertDashboardScheduleInfo(scheduleUserA, userUri, asList(userA));

            logout();
            signInAtGreyPages(userB, testParams.getPassword());
            initDashboardsPage();
            createDashboardSchedule(scheduleUserA, Collections.emptyList());

            logoutAndLoginAs(true, UserRoles.ADMIN);
            userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), userB);

            assertFalse(initEmailSchedulesPage().isPrivateSchedulePresent(scheduleUserB),
                    "Schedule of deleted user was not hidden.");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private void checkOnlyPublicScheduleVisible() {
        assertTrue(refreshSchedulesPage().isGlobalSchedulePresent(PUBLIC_SCHEDULE), "Public schedule was displayed!");

        assertFalse(EmailSchedulePage.getInstance(browser).isPrivateSchedulesTableVisible(),
                "Private Schedules Created on Dashboard table was not hidden");

        assertFalse(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(SCHEDULE_WITHOUT_RECIPIENTS),
                "Private Schedule" + SCHEDULE_WITHOUT_RECIPIENTS + " was not displayed as expected!");
        assertFalse(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(SCHEDULE_WITH_EXTERNAL_RECIPIENTS),
                "Private Schedule" + SCHEDULE_WITH_EXTERNAL_RECIPIENTS + " was not displayed as expected!");
        assertFalse(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(SCHEDULE_WITH_INTERNAL_RECIPIENTS),
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

    private void enableHideDashboardScheduleFlag() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.HIDE_DASHBOARD_SCHEDULE, true);
    }

    private void disableHideDashboardScheduleFlag() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.HIDE_DASHBOARD_SCHEDULE, false);
    }

    private void enableDashboardScheduleRecipientsFlag() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, true);
    }

    private void disableDashboardScheduleRecipientsFlag() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, false);
    }

    private void assertDashboardScheduleInfo(String title, String authorUri, Collection<String> bccEmails) {
        assertTrue(EmailSchedulePage.getInstance(browser).isPrivateSchedulePresent(title), "Dashboard schedule was not displayed.");
        WebElement schedule = EmailSchedulePage.getInstance(browser).getPrivateSchedule(title);

        assertEquals(EmailSchedulePage.getInstance(browser).getAuthorUriOfSchedule(schedule), authorUri,
                "Author uri of schedule mail was not correct!");

        assertEquals(EmailSchedulePage.getInstance(browser).getBccEmailsOfPrivateSchedule(schedule),
                Joiner.on(", ").join(bccEmails), "List of bcc emails was not correct.");

        List<String> controls = EmailSchedulePage.getInstance(browser).getControlsOfSchedule(schedule);
        assertEquals(controls.size(), 1, "List of controls button was not correct.");
        assertEquals(controls.get(0), DELETE, "Control button text was not correct.");
    }

    private EmailSchedulePage refreshSchedulesPage() {
        browser.navigate().refresh();
        return initEmailSchedulesPage();
    }

    private void checkScheduleButtonStatusThroughTabs() {
        DashboardTabs tabs = dashboardsPage.getTabs();
        int size = tabs.getNumberOfTabs();

        assertFalse(dashboardsPage.isScheduleButtonDisabled(), "Schedule button should be enabled");

        for (int i = 1; i < size; i++) {
            tabs.getTab(i).open();
            assertTrue(dashboardsPage.isScheduleButtonDisabled());
        }
    }
}
