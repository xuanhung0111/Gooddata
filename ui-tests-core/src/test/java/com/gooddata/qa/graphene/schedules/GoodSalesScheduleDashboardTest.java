/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private final String FEATURE_FLAG_COOKIE_NAME = "GDC-FEATURE-DASHBOARD-SCHEDULE";
    private final Cookie FEATURE_FLAG_COOKIE = new Cookie(FEATURE_FLAG_COOKIE_NAME, "1");
    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> SCHEDULED_DASHBOARDS = Arrays.asList("Waterfall Analysis", "What's Changed");
    private final String SCHEDULE_INFO = "This report will be sent daily at 12:30 AM PST to %s as a PDF file attachment.";
    private final String SCHEDULE_TIME_MANAGE_PAGE = "Daily at 12:30am PT";

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @BeforeMethod
    public void addFeatureCookie() {
        // TODO remove after adding proper feature flag
        this.browser.manage().addCookie(FEATURE_FLAG_COOKIE);
    }

    @AfterMethod
    public void removeFeatureCookie() {
        // TODO remove after adding proper feature flag
        this.browser.manage().deleteCookieNamed(FEATURE_FLAG_COOKIE_NAME);
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
        scheduleDashboard.selectTabs(new int[] {1, 5, 6});
        String[] tabNames = expectedGoodSalesDashboardsAndTabs.get("Pipeline Analysis");
        assertEquals(
            scheduleDashboard.getCustomEmailSubject(),
            "Scheduled report: Tabs: " + tabNames[1] + ", " + tabNames[5] + ", " + tabNames[6],
            "Update of Tabs is reflected in subject."
        );
        scheduleDashboard.setCustomEmailSubject(CUSTOM_SUBJECT);
        scheduleDashboard.selectTabs(new int[] {1, 2});
        assertEquals(
            scheduleDashboard.getCustomEmailSubject(),
            CUSTOM_SUBJECT,
            "Update of Tabs is not reflected in subject."
        );
        scheduleDashboard.selectTime(1);
        String infoText = scheduleDashboard.getInfoText();
        assertTrue(
            infoText.contains(String.format(SCHEDULE_INFO, testParams.getViewerUser())),
            "Custom time is in info message, expected " + String.format(SCHEDULE_INFO, testParams.getViewerUser()) + ", found " + infoText + "."
        );
        // check time in info text
        scheduleDashboard.setCustomEmailMessage(CUSTOM_MESSAGE);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());
        scheduleDashboard.schedule();
    }

    // login and test as admin
    @Test(dependsOnGroups = {"schedules"}, groups = {"tests"})
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

    @Test(dependsOnGroups = {"schedules"}, groups = {"tests"})
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

        successfulTest = true;

        // re-login as admin for successfull project wipe; get to non-embedded to logout first
        initDashboardsPage();
        loginAs(UserRoles.ADMIN);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-embedded-dashboard", this.getClass());
    }
}
