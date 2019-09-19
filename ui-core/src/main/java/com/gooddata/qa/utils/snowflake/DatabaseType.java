package com.gooddata.qa.utils.snowflake;

public enum DatabaseType {
    REDSHIFT("redshift"),
    SNOWFLAKE("snowflake"),
    BIGQUERY("bigQuery");

    private String name;

    DatabaseType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    String getName() {
        return name;
    }
}
