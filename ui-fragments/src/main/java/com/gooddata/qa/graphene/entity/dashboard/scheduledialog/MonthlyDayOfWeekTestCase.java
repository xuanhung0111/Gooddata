package com.gooddata.qa.graphene.entity.dashboard.scheduledialog;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;

public class MonthlyDayOfWeekTestCase extends AbstractRecurrenceTestCase {
    public final WeekOfMonth week;
    public final DayOfWeek dayOfWeek;

    public MonthlyDayOfWeekTestCase(String message, DayTime time, DayOfWeek dayOfWeek, WeekOfMonth week) {
        super(RecurrenceType.MONTHLY_DAY_OF_WEEK, time, message);
        this.week = week;
        this.dayOfWeek = dayOfWeek;
    }

    @java.lang.Override
    public void setDialogConfiguration(DashboardScheduleDialog dashboardScheduleDialog) {
        setDialogBasics(dashboardScheduleDialog);
        dashboardScheduleDialog.selectMonthlyOn(MonthRecurrenceType.DAY_OF_WEEK.getValue());
        dashboardScheduleDialog.selectRepeatEvery(week.getValue());
        dashboardScheduleDialog.selectDayOfWeek(dayOfWeek.getValue());
    }
}
