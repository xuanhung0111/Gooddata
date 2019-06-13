package com.gooddata.qa.utils.snowflake;

public class DatabaseColumn {
    String title;
    String dataType;
    String extraParams = "";

    public DatabaseColumn(String title, String dataType, String... extraParams) {
        // add extra params
        for (String s : extraParams) {
            this.extraParams = this.extraParams + " " + s;
        }

        this.title = title;
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return this.title + " " + dataType + extraParams;

    }
}
