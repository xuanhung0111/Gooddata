/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

import com.gooddata.qa.utils.http.RestApiClient;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> CUSTOM_RECIPIENTS = Arrays.asList("bear+1@gooddata.com", "bear+2@gooddata.com");
    private final List<String> SCHEDULED_TABS = Arrays.asList("Waterfall Analysis", "What's Changed");
    private final String SCHEDULE_INFO = "This dashboard will be sent daily at 12:30 AM %s to %s and 2 other recipients as a PDF attachment.";
    private DateTimeZone tz = DateTimeZone.getDefault();

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    // login with defined user role, fail test on error
    private void loginAs(UserRoles userRole) throws JSONException {
        logout();
        signIn(true, userRole); // login with gray pages to reload application and have feature flag set
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void setFeatureFlags() throws JSONException, IOException, InterruptedException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE);
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS);

        // need time for system apply feature flags setting
        Thread.sleep(3000);
    }

    // prepare viewer user and login
    @Test(dependsOnMethods = {"setFeatureFlags"}, groups = {"schedules"})
    public void createDashboardSchedule() throws JSONException {
        loginAs(UserRoles.VIEWER);
        initDashboardsPage();

        DashboardScheduleDialog dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();
        dashboardScheduleDialog.showCustomForm();

        dashboardScheduleDialog.selectTabs(new int[]{1});
        String[] tabNames = expectedGoodSalesDashboardsAndTabs.get("Pipeline Analysis");
        assertEquals(
                dashboardScheduleDialog.getCustomEmailSubject(),
                dashboardsPage.getDashboardName() + " Dashboard - " + tabNames[1],
                "Update of one Tab is reflected in subject."
        );

        dashboardScheduleDialog.selectTabs(new int[]{1, 5, 6});
        assertEquals(
                dashboardScheduleDialog.getCustomEmailSubject(),
                dashboardsPage.getDashboardName() + " Dashboard",
                "Update of multiple Tabs is reflected in subject."
        );

        dashboardScheduleDialog.setCustomEmailSubject(CUSTOM_SUBJECT);
        dashboardScheduleDialog.setCustomRecipients(CUSTOM_RECIPIENTS);
        dashboardScheduleDialog.selectTabs(new int[]{1, 2});

        assertEquals(
                dashboardScheduleDialog.getCustomEmailSubject(),
                CUSTOM_SUBJECT,
                "Update of Tabs is not reflected in subject."
        );
        dashboardScheduleDialog.selectTime(1);
        String infoText = dashboardScheduleDialog.getInfoText();
        String tzId = tz.getShortName(DateTimeUtils.currentTimeMillis());
        assertTrue(
                infoText.contains(String.format(SCHEDULE_INFO, tzId, testParams.getViewerUser())),
                "Custom time is in info message, expected " + String.format(SCHEDULE_INFO, tzId, testParams.getViewerUser()) + ", found " + infoText + "."
        );
        // check time in info text
        dashboardScheduleDialog.setCustomEmailMessage(CUSTOM_MESSAGE);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());
        dashboardScheduleDialog.schedule();
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyRecipientsOfSchedule() throws JSONException, InterruptedException, ParseException, IOException {
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

        assertEquals(to, new HashSet<String>(Arrays.asList(testParams.getViewerUser())), "Current user is in 'To' field");
        assertEquals(subject, CUSTOM_SUBJECT, "Custom subject is in 'Subject' input field.");
        assertEquals(body, CUSTOM_MESSAGE, "Custom message is in 'Message' input field.");
        assertEquals(tabs.length(), SCHEDULED_TABS.size(), "Expecting two tabs to be attached to the scheduled mail");

        assertEquals(recipients, new HashSet<String>(CUSTOM_RECIPIENTS), "Recipients do not match.");
        assertEquals(tz.getStandardOffset(System.currentTimeMillis()), tzFromObj.getStandardOffset(System.currentTimeMillis()), "Timezones do not match");
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyScheduleButtonPresenceOnEmbeddedDashboard() throws InterruptedException, JSONException {
        // get embed link as admin (not accessible for viewer)
        loginAs(UserRoles.ADMIN);
        initDashboardsPage();
        DashboardEmbedDialog ded = dashboardsPage.embedDashboard();
        String embeddedDashboardUri = ded.getPreviewURI();

        // switch to viewer and visit the embed link
        loginAs(UserRoles.VIEWER);
        this.browser.get(embeddedDashboardUri);

        // wait for embedded dashboard to be fully loaded before checking
        waitForElementPresent(By.cssSelector(".embedded.s-dashboardLoaded"), this.browser);
        assertTrue(dashboardsPage.isScheduleButtonVisible());

        // re-login as admin for successfull project wipe; get to non-embedded to logout first
        initDashboardsPage();
        loginAs(UserRoles.ADMIN);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-embedded-dashboard", this.getClass());
    }

    private String getScheduleUri(String scheduleTitle) throws JSONException, IOException {
        String schedulesUri = "/gdc/md/" + testParams.getProjectId() + "/query/scheduledmails";

        // re-initialize of the rest client is necessary to prevent execution freeze
        restApiClient = null;
        final RestApiClient rac = getRestApiClient();
        final HttpGet getRequest = rac.newGetMethod(schedulesUri);
        final HttpResponse getResponse = rac.execute(getRequest);
        JSONArray schedules = new JSONObject(EntityUtils.toString(getResponse.getEntity())).getJSONObject("query").getJSONArray("entries");

        String uri = null;
        for (int i = 0; i < schedules.length(); i++) {
            JSONObject schedule = schedules.getJSONObject(i);
            if (schedule.getString("title").equals(scheduleTitle)) {
                uri = schedule.getString("link");
                break;
            }
        }
        return uri;
    }

}
