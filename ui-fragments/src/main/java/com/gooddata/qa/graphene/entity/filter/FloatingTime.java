package com.gooddata.qa.graphene.entity.filter;

public class FloatingTime {
    private Time time;
    private int rangeNumber;

    public FloatingTime(Time time) {
        this.time = time;
        this.rangeNumber = 0;
    }

    public FloatingTime withTime(Time time) {
        this.time = time;
        return this;
    }

    public Time getTime() {
        return time;
    }

    public FloatingTime withRangeNumber(int rangeNumber) {
        this.rangeNumber = rangeNumber;
        return this;
    }

    public int getRangeNumber() {
        return rangeNumber;
    }

    public enum Time {
        THIS_YEAR,
        LAST_YEAR,
        NEXT_YEAR,
        YEARS_AGO,
        YEARS_IN_THE_FUTURE;

        @Override
        public String toString() {
            return name().toLowerCase().replaceAll("_", " ");
        }
    }
}
