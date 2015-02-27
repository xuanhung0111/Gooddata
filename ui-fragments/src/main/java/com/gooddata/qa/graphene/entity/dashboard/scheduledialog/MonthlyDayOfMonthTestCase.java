package com.gooddata.qa.graphene.entity.dashboard.scheduledialog;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;

public class MonthlyDayOfMonthTestCase extends AbstractRecurrenceTestCase {
    public final int dayOfMonth;

    public MonthlyDayOfMonthTestCase(String message, DayTime time, int dayOfMonth) {
        super(RecurrenceType.MONTHLY_DAY_OF_MONTH, time, message);
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public void setDialogConfiguration(DashboardScheduleDialog dashboardScheduleDialog) {
        setDialogBasics(dashboardScheduleDialog);
        dashboardScheduleDialog.selectMonthlyOn(MonthRecurrenceType.DAY_OF_MONTH.getValue());
        dashboardScheduleDialog.selectDayOfMonth(dayOfMonth);
    }
}
