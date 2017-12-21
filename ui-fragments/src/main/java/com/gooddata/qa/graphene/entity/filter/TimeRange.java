package com.gooddata.qa.graphene.entity.filter;

import com.gooddata.qa.graphene.fragments.common.SelectTimeRangePanel.Range;

public class TimeRange {

    private Range range;
    private int number;
    private Time time;

    /**
     * Using to set past/future time range.
     * @param number Have to different zero value
     * @param time example: YEAR_AGO, YEARS_IN_THE_FUTURE
     */
    public static final TimeRange from(int number, Time time) {
        if (number == 0) throw new IllegalStateException("Zero is invalid input with time selection");
        return new TimeRange(Range.FROM, number, time);
    }

    /**
     * Just using for current time range.
     * @param time example: THIS_YEAR
     */
    public static final TimeRange from(Time time) {
        return new TimeRange(Range.FROM, 0, time);
    }

    /**
     * Using to set past/future time range.
     * @param number Have to different zero value
     * @param time example: YEAR_AGO, YEARS_IN_THE_FUTURE
     */
    public static final TimeRange to(int number, Time time) {
        return new TimeRange(Range.TO, number, time);
    }

    /**
     * Just using for current time range.
     * @param time example: THIS_YEAR
     */
    public static final TimeRange to(Time time) {
        return new TimeRange(Range.TO, 0, time);
    }

    public int getNumber() {
        return number;
    }

    public Time getTime() {
        return time;
    }

    public Range getRange() {
        return range;
    }

    private TimeRange(Range range, int number, Time time) {
        this.range = range;
        this.number = number;
        this.time = time;
    }

    public enum Time {
        YEARS_AGO("years ago"),
        THIS_YEAR("this year"),
        YEARS_IN_THE_FUTURE("years in the future");

        private String name;

        Time(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
