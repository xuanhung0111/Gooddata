package com.gooddata.qa.graphene.enums;

public enum ResourceDirectory {

    IMAGES("images"),
    ZIP_FILES("zip-file"),
    PAYROLL_CSV("payroll-csv"),
    UPLOAD_CSV("upload-csv"),
    MAQL_FILES("maql-file"),
    API_RESOURCES("api-resources"),
    SQL_FILES("sql-file"),
    DYNAMIC_IMAGES("dynamic-images");

    private String name;

    private ResourceDirectory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
