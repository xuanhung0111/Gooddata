package com.gooddata.qa.graphene.enums;

public enum DateGranularity {
    DAY("Day"),
    WEEK_SUN_SAT("Week (Sun-Sat)"),
    MONTH("Month"),
    QUARTER("Quarter"),
    YEAR("Year");

    private String type;

    private DateGranularity(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
