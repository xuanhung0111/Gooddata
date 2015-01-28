/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> CUSTOM_RECIPIENTS = Arrays.asList("bear+1@gooddata.com", "bear+2@gooddata.com");
    private final List<String> SCHEDULED_DASHBOARDS = Arrays.asList("Waterfall Analysis", "What's Changed");
    private final String SCHEDULE_INFO = "This dashboard will be sent daily at 12:30 AM %s to %s and 2 other recipients as a PDF attachment.";
    private final String SCHEDULE_TIME_MANAGE_PAGE = "Daily at 12:30 AM PT";
    private DateTimeZone tz = DateTimeZone.getDefault();

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    // login with defined user role, fail test on error
    // TODO: why not login via grey pages?
    private void loginAs(UserRoles userRole) throws JSONException {
        logout();
        signIn(false, userRole);
    }

    // prepare viewer user and login
    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule () throws JSONException {
        loginAs(UserRoles.VIEWER);
        initDashboardsPage();
        DashboardScheduleDialog scheduleDashboard = dashboardsPage.scheduleDashboard();
        scheduleDashboard.showCustomForm();

        scheduleDashboard.selectTabs(new int[] {1});
        String[] tabNames = expectedGoodSalesDashboardsAndTabs.get("Pipeline Analysis");
        assertEquals(
                scheduleDashboard.getCustomEmailSubject(),
                dashboardsPage.getDashboardName() + " Dashboard - " + tabNames[1],
                "Update of one Tab is reflected in subject."
        );

        scheduleDashboard.selectTabs(new int[] {1, 5, 6});
        assertEquals(
            scheduleDashboard.getCustomEmailSubject(),
            dashboardsPage.getDashboardName() + " Dashboard",
            "Update of multiple Tabs is reflected in subject."
        );

        scheduleDashboard.setCustomEmailSubject(CUSTOM_SUBJECT);
        scheduleDashboard.setCustomRecipients(CUSTOM_RECIPIENTS);
        scheduleDashboard.selectTabs(new int[] {1, 2});
        assertEquals(
            scheduleDashboard.getCustomEmailSubject(),
            CUSTOM_SUBJECT,
            "Update of Tabs is not reflected in subject."
        );
        scheduleDashboard.selectTime(1);
        String infoText = scheduleDashboard.getInfoText();
        String tzId = tz.getShortName(DateTimeUtils.currentTimeMillis());
        assertTrue(
            infoText.contains(String.format(SCHEDULE_INFO, tzId, testParams.getViewerUser())),
            "Custom time is in info message, expected " + String.format(SCHEDULE_INFO, tzId, testParams.getViewerUser()) + ", found " + infoText + "."
        );
        // check time in info text
        scheduleDashboard.setCustomEmailMessage(CUSTOM_MESSAGE);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());
        scheduleDashboard.schedule();
    }

    // login and test as admin
    @Test(dependsOnGroups = {"schedules"})
    public void verifyDashboardSchedule() throws JSONException {
        loginAs(UserRoles.ADMIN);
        initEmailSchedulesPage();
        emailSchedulesPage.openSchedule(CUSTOM_SUBJECT);
        assertEquals(emailSchedulesPage.getToFromInput(), testParams.getViewerUser(), "Current user is in 'To' input.");
        assertEquals(emailSchedulesPage.getSubjectFromInput(), CUSTOM_SUBJECT, "Custom subject is in 'Subject' input field.");
        assertEquals(emailSchedulesPage.getMessageFromInput(), CUSTOM_MESSAGE, "Custom message is in 'Message' input field.");
        assertEquals(emailSchedulesPage.getAttachedDashboards(), SCHEDULED_DASHBOARDS, "The selected dashboards are attached to scheduled e-mail.");
        String timeDescription = emailSchedulesPage.getTimeDescription();
        assertTrue(
            timeDescription.contains(SCHEDULE_TIME_MANAGE_PAGE),
            "Time description contains the given time. Expected '" + SCHEDULE_TIME_MANAGE_PAGE + "', found '" + timeDescription + "'."
        );
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyRecipientsOfSchedule() throws JSONException, InterruptedException {
        loginAs(UserRoles.ADMIN);
        initEmailSchedulesPage();
        // get object
        String uri = emailSchedulesPage.getScheduleMailUriByName(CUSTOM_SUBJECT);
        String[] parts = uri.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        JSONObject schedule = getObjectByID(id);
        JSONArray recipientsJson = schedule.getJSONObject("scheduledMail").getJSONObject("content").getJSONArray("bcc");
        Set<String> recipients = new HashSet<String>();
        for(int i = 0; i < recipientsJson.length(); i++) {
            recipients.add(recipientsJson.getString(i));
        }
        String timeZoneId = schedule.getJSONObject("scheduledMail")
                                      .getJSONObject("content")
                                      .getJSONObject("when")
                                      .getString("timeZone");
        DateTimeZone tzFromObj = DateTimeZone.forID(timeZoneId);

        // verify bcc
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-mdObject", this.getClass());
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
}
