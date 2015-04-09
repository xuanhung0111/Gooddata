package com.gooddata.qa.graphene.enums;

public enum DWHDriver {

    PG,
    VERTICA;

    public static DWHDriver getDriverByName(String dwhDriver) {
        if (dwhDriver != null && dwhDriver.length() > 0) {
            for (DWHDriver driver : values()) {
                if (driver.toString().toLowerCase().equals(dwhDriver)) return driver;
            }
        }
        return PG;
    }
}
