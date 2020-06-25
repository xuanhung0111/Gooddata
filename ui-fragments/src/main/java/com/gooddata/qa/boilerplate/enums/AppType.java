package com.gooddata.qa.boilerplate.enums;

public enum AppType {
    BOILER_PLATE("boilerplate"),
    REACT("react");

    private String type;

    private AppType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
