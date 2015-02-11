package com.gooddata.qa.graphene.entity.dashboard.scheduledialog;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;

public class WeeklyTestCase extends AbstractRecurrenceTestCase {
    public final EveryXWeeks weekFrequency;
    public final DayOfWeek[] days;

    public WeeklyTestCase(String message, DayTime time,  DayOfWeek[] days, EveryXWeeks weekFrequency) {
        super(RecurrenceType.WEEKLY, time, message);
        this.weekFrequency = weekFrequency;
        this.days = days;
    }

    private int[] getDaysAsInts() {
        int[] intDays = new int[days.length];

        for (int i = 0; i < days.length; i++) {
            intDays[i] = days[i].getValue();
        }

        return intDays;
    }

    @java.lang.Override
    public void setDialogConfiguration(DashboardScheduleDialog dashboardScheduleDialog) {
        setDialogBasics(dashboardScheduleDialog);
        dashboardScheduleDialog.selectWeeklyEvery(weekFrequency.getValue());
        dashboardScheduleDialog.selectWeeklyOnDay(getDaysAsInts());
    }
}
