package com.gooddata.qa.graphene.enums;

public enum TimeZone {
    GMT_01_00_Paris("(GMT+01:00) Paris");

    private String timeZone;
    TimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return timeZone;
    }

    public enum Time {
        AM_12_00("12:00 AM");

        private String time;
        Time(String time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return time;
        }
    }
}
