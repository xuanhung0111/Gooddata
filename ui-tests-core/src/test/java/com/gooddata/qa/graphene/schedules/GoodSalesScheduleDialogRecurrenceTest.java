/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.openqa.selenium.Cookie;
import org.testng.annotations.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDialogRecurrenceTest extends AbstractGoodSalesEmailSchedulesTest {
    private enum RecurrenceType { WEEKLY, MONTHLY_DAY_OF_MONTH, MONTHLY_DAY_OF_WEEK };
    private final Object[][] CASES = new Object[][]{
            {RecurrenceType.WEEKLY, 0, new int[]{0, 1}, "weekly on Mon, Tue at 12:30 AM"},
            {RecurrenceType.WEEKLY, 1, new int[]{2}, "every 2 weeks on Wed at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_MONTH, 1,  "monthly on day 2 at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_WEEK, 0, 1, "monthly on First Tuesday at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_WEEK, 2, 6, "monthly on Third Sunday at 12:30 AM"}
    };
    private final String CUSTOM_SUBJECT = "Test subject";
    private final String SCHEDULE_INFO = "This report will be sent %s PST to %s as a PDF attachment.";
    private final String FEATURE_FLAG_COOKIE_NAME = "GDC-FEATURE-DASHBOARD-SCHEDULE";
    private final Cookie FEATURE_FLAG_COOKIE = new Cookie(FEATURE_FLAG_COOKIE_NAME, "1");
    private DashboardScheduleDialog scheduleDashboard;

    @BeforeMethod
    public void addFeatureCookie() {
        // TODO remove after adding proper feature flag
        browser.manage().addCookie(FEATURE_FLAG_COOKIE);
    }

    @AfterMethod
    public void removeFeatureCookie() {
        // TODO remove after adding proper feature flag
        browser.manage().deleteCookieNamed(FEATURE_FLAG_COOKIE_NAME);
    }

    @Test(dependsOnGroups = {"schedules"}, groups = {"tests"})
    public void switchRecurrences () throws IllegalArgumentException {
        initDashboardsPage();
        scheduleDashboard = dashboardsPage.scheduleDashboard();
        for (Object[] testCase : CASES) {
            RecurrenceType recurrence = (RecurrenceType) testCase[0];
            switch (recurrence) {
                case WEEKLY:
                    _testWeekly(((Integer) testCase[1]), ((int[]) testCase[2]), ((String) testCase[3]));
                    break;
                case MONTHLY_DAY_OF_WEEK:
                case MONTHLY_DAY_OF_MONTH:
                    _testMonthly(testCase);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot test recurrence: " + recurrence);

            }
        }
    }

    private void _testWeekly(int when, int[] days, String scheduleInfo) {
        scheduleDashboard.selectFrequency(1); // weekly
        scheduleDashboard.selectWeeklyEvery(when);
        scheduleDashboard.selectWeeklyOnDay(days);
        scheduleDashboard.selectTime(1); // 12:30pm
        String infoText = scheduleDashboard.getInfoText();
        String fullText = String.format(SCHEDULE_INFO, scheduleInfo, testParams.getUser());
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog-recurrence-weekly-" + scheduleInfo, this.getClass());
        assertEquals(fullText, infoText, "Custom time is in info message");
    }

    private void _testMonthly(Object[] testCase) {
        scheduleDashboard.selectFrequency(2); // monthly
        String scheduleInfo = "";
        if (RecurrenceType.MONTHLY_DAY_OF_MONTH.equals(testCase[0])) {
            scheduleDashboard.selectMonthlyOn(0); // day of month
            scheduleDashboard.selectDayOfMonth((Integer) testCase[1]); // which day of month
            scheduleInfo = (String) testCase[2];
        } else if (RecurrenceType.MONTHLY_DAY_OF_WEEK.equals(testCase[0])) {
            scheduleDashboard.selectMonthlyOn(1); // day of week
            scheduleDashboard.selectRepeatEvery((Integer) testCase[1]); // repeat every X weeks
            scheduleDashboard.selectDayOfWeek((Integer) testCase[2]); // on which day
            scheduleInfo = (String) testCase[3];
        }
        scheduleDashboard.selectTime(1); // 12:30pm
        String infoText = scheduleDashboard.getInfoText();
        String fullText = String.format(SCHEDULE_INFO, scheduleInfo, testParams.getUser());
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog-recurrence-monthly-" + scheduleInfo, this.getClass());
        assertEquals(fullText, infoText, "Custom time is in info message");
    }
}
