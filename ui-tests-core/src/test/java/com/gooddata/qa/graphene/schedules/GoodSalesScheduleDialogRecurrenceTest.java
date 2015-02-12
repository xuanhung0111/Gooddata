/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDialogRecurrenceTest extends AbstractGoodSalesEmailSchedulesTest {
    private enum RecurrenceType { WEEKLY, MONTHLY_DAY_OF_MONTH, MONTHLY_DAY_OF_WEEK };
    private final Object[][] CASES = new Object[][]{
            {RecurrenceType.WEEKLY, 0, new int[]{0, 1}, "weekly on Monday, Tuesday at 12:30 AM"},
            {RecurrenceType.WEEKLY, 1, new int[]{2}, "every 2 weeks on Wednesday at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_MONTH, 1, null, "monthly on day 2 at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_WEEK, 0, 1, "monthly on the first Tuesday at 12:30 AM"},
            {RecurrenceType.MONTHLY_DAY_OF_WEEK, 2, 6, "monthly on the third Sunday at 12:30 AM"}
    };
    private final String SCHEDULE_INFO = "This dashboard will be sent %s %s to %s as a PDF attachment.";
    private DashboardScheduleDialog scheduleDashboard;

    @Test(dependsOnMethods =  {"verifyEmptySchedules"}, groups = {"schedules"})
    public void setFeatureFlags () throws JSONException, IOException, InterruptedException {
        RestUtils.setFeatureFlagsToProject(getRestApiClient(), testParams.getProjectId(),
                RestUtils.FeatureFlagOption.createFeatureClassOption("dashboardSchedule", true)
        );
        logout();
    }

    @Test(dependsOnGroups = {"schedules"})
    public void switchRecurrences () throws IllegalArgumentException, JSONException {
        signIn(true, UserRoles.ADMIN); // login with gray pages to reload application and have feature flag set
        initDashboardsPage();
        scheduleDashboard = dashboardsPage.scheduleDashboard();
        for (Object[] testCase : CASES) {
            RecurrenceType recurrence = (RecurrenceType) testCase[0];
            switch (recurrence) {
                case WEEKLY:
                    System.out.println("Running weekly test case: " + testCase[3]);
                    testWeekly(((Integer) testCase[1]), ((int[]) testCase[2]), ((String) testCase[3]));
                    break;
                case MONTHLY_DAY_OF_WEEK:
                case MONTHLY_DAY_OF_MONTH:
                    System.out.println("Running monthly test case: " + testCase[3]);
                    testMonthly(testCase);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot test recurrence: " + recurrence);

            }
        }
    }

    private String getTimezoneShortName() {
        return DateTimeZone.getDefault().getShortName(DateTimeUtils.currentTimeMillis());
    }

    private void testWeekly(int when, int[] days, String scheduleInfo) {
        System.out.println(" - test weekly");
        scheduleDashboard.selectFrequency(1); // weekly
        System.out.println(" -- frequency selected");
        scheduleDashboard.selectWeeklyEvery(when);
        System.out.println(" -- when selected");
        scheduleDashboard.selectWeeklyOnDay(days);
        System.out.println(" -- days selected");
        scheduleDashboard.selectTime(1); // 12:30pm
        System.out.println(" -- time selected");
        String infoText = scheduleDashboard.getInfoText();
        String fullText = String.format(SCHEDULE_INFO, scheduleInfo, getTimezoneShortName(), testParams.getUser());
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog-recurrence-weekly-" + scheduleInfo, this.getClass());
        assertEquals(infoText, fullText, "Custom time is in info message");
    }

    private void testMonthly(Object[] testCase) {
        System.out.println(" - test monthly");
        scheduleDashboard.selectFrequency(2); // monthly
        System.out.println(" -- frequency selected");
        String scheduleInfo = "";
        if (RecurrenceType.MONTHLY_DAY_OF_MONTH.equals(testCase[0])) {
            scheduleDashboard.selectMonthlyOn(0); // day of month
            scheduleDashboard.selectDayOfMonth((Integer) testCase[1]); // which day of month
            scheduleInfo = (String) testCase[3];
            System.out.println(" -- day of month selected");
        } else if (RecurrenceType.MONTHLY_DAY_OF_WEEK.equals(testCase[0])) {
            scheduleDashboard.selectMonthlyOn(1); // day of week
            scheduleDashboard.selectRepeatEvery((Integer) testCase[1]); // repeat every X weeks
            scheduleDashboard.selectDayOfWeek((Integer) testCase[2]); // on which day
            scheduleInfo = (String) testCase[3];
            System.out.println(" -- day of week selected");
        }
        scheduleDashboard.selectTime(1); // 12:30pm
        System.out.println(" -- time selected");
        String infoText = scheduleDashboard.getInfoText();
        String fullText = String.format(SCHEDULE_INFO, scheduleInfo, getTimezoneShortName(), testParams.getUser());
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog-recurrence-monthly-" + scheduleInfo, this.getClass());
        assertEquals(infoText, fullText, "Custom time is in info message");
    }
}
