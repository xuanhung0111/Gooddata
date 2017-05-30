package com.gooddata.qa.mdObjects.dashboard.filter;

public enum FilterType {
    LIST("list"),
    TIME("time"),
    FLOATING("floating");

    private String value;

    FilterType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
