package com.gooddata.qa.graphene.entity.dashboard.scheduledialog;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;

abstract public class AbstractRecurrenceTestCase {
    protected final String message;

    public final RecurrenceType recurrence;
    public final DayTime time;

    public AbstractRecurrenceTestCase(RecurrenceType recurrence, DayTime time, String message) {
        this.recurrence = recurrence;
        this.time = time;
        this.message = message;
    }
    
    public static enum RecurrenceType {
        DAILY(0),
        WEEKLY(1),
        MONTHLY_DAY_OF_MONTH(2),
        MONTHLY_DAY_OF_WEEK(2);

        private final int value;

        private RecurrenceType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum MonthRecurrenceType {
        DAY_OF_MONTH(0),
        DAY_OF_WEEK(1);

        private final int value;

        private MonthRecurrenceType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum WeekOfMonth {
        FIRST(0),
        SECOND(1),
        THIRD(2),
        FOURTH(3);

        private final int value;

        private WeekOfMonth(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum EveryXWeeks {
        ONE(0),
        TWO(1),
        THREE(2),
        FOUR(3);

        private final int value;

        private EveryXWeeks(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum DayOfWeek {
        MONDAY(0),
        TUESDAY(1),
        WEDNESDAY(2),
        THURSDAY(3),
        FRIDAY(4),
        SATURDAY(5),
        SUNDAY(6);

        private final int value;

        private DayOfWeek(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum DayTime {
        AM_12_30(1);

        private final int value;

        private DayTime(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static int MONTH_FIRST_DAY = 1;
    public static int MONTH_MAX_DAYS = 31;
    public static int MONTH_LAST_DAY = -1;
    public static int[] MONTH_DAYS = generateMonthDays();

    private static int[] generateMonthDays() {
        int[] monthDays = new int[MONTH_MAX_DAYS+1];

        monthDays[0] = MONTH_LAST_DAY;

        for (int day = MONTH_FIRST_DAY; day < MONTH_MAX_DAYS; day++) {
            monthDays[day] = day - 1;
        }

        return monthDays;
    }

    protected void setDialogBasics(DashboardScheduleDialog dashboardScheduleDialog) {
        dashboardScheduleDialog.selectFrequency(recurrence.getValue());
        dashboardScheduleDialog.selectTime(time.getValue());
    }

    public String getMessage() {
        return message;
    }

    abstract public void setDialogConfiguration(DashboardScheduleDialog dashboardScheduleDialog);
}
