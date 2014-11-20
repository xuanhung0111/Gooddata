/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.Cookie;
import org.testng.annotations.Test;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private final String FEATURE_FLAG_COOKIE_NAME = "GDC-FEATURE-DASHBOARD-SCHEDULE";
    private final Cookie FEATURE_FLAG_COOKIE = new Cookie(FEATURE_FLAG_COOKIE_NAME, "1");
    private final String CUSTOM_SUBJECT = "Extremely useful subject";
    private final String CUSTOM_MESSAGE = "Extremely useful message";
    private final List<String> SCHEDULED_DASHBOARDS = Arrays.asList("Waterfall Analysis", "What's Changed");
    private final String SCHEDULE_TIME = "Daily at 12:30am";

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() {
        // TODO remove after adding proper feature flag
        this.browser.manage().addCookie(FEATURE_FLAG_COOKIE);
        initDashboardsPage();
        DashboardScheduleDialog scheduleDashboard = dashboardsPage.scheduleDashboard();
        scheduleDashboard.showCustomForm();
        scheduleDashboard.selectTabs(new int[] {1, 5, 6});
        String[] tabNems = expectedGoodSalesDashboardsAndTabs.get("Pipeline Analysis");
        assertEquals(
            scheduleDashboard.getCustomEmailSubject(),
            "Scheduled report: Tabs: " + tabNems[1] + ", " + tabNems[5] + ", " + tabNems[6],
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
            infoText.contains(SCHEDULE_TIME.toLowerCase()),
            "Custom time is in info message, expected " + SCHEDULE_TIME.toLowerCase() + ", found " + infoText + "."
        );
        // check time in info text
        scheduleDashboard.setCustomEmailMessage(CUSTOM_MESSAGE);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());
        scheduleDashboard.schedule();
    }

    @Test(dependsOnGroups = {"schedules"}, groups = {"tests"})
    public void verifyDashboardSchedule() {
        initEmailSchedulesPage();
        emailSchedulesPage.openSchedule(CUSTOM_SUBJECT);
        assertEquals(emailSchedulesPage.getToFromInput(), testParams.getUser(), "Current user is in 'To' input.");
        assertEquals(emailSchedulesPage.getSubjectFromInput(), CUSTOM_SUBJECT, "Custom subject is in 'Subject' input field.");
        assertEquals(emailSchedulesPage.getMessageFromInput(), CUSTOM_MESSAGE, "Custom message is in 'Message' input field.");
        assertEquals(emailSchedulesPage.getAttachedDashboards(), SCHEDULED_DASHBOARDS, "The selected dashboards are attached to scheduled e-mail.");
        String timeDescription = emailSchedulesPage.getTimeDescription();
        assertTrue(
            timeDescription.contains(SCHEDULE_TIME),
            "Time description contains the given time. Expected '" + SCHEDULE_TIME + "', found '" + timeDescription + "'."
        );
        // TODO remove after adding proper feature flag
        this.browser.manage().deleteCookieNamed(FEATURE_FLAG_COOKIE_NAME);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
        successfulTest = true;
    }
}
