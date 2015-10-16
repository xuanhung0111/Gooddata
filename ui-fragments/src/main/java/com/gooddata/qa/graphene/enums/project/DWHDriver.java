package com.gooddata.qa.graphene.enums.project;

public enum DWHDriver {

    PG("Pg"),
    VERTICA("vertica");

    private final String value;

    private DWHDriver(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DWHDriver getDriverByName(String dwhDriver) {
        if (dwhDriver != null && dwhDriver.length() > 0) {
            for (DWHDriver driver : values()) {
                if (driver.toString().toLowerCase().equals(dwhDriver)) return driver;
            }
        }
        return PG;
    }
}
