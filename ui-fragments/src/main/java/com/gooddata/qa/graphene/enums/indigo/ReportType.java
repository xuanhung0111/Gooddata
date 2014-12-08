package com.gooddata.qa.graphene.enums.indigo;

import org.apache.commons.lang.WordUtils;
import org.openqa.selenium.By;

public enum ReportType {

    TABLE("table"),
    COLUMN_CHART("column"),
    LINE_CHART("line"),
    BAR_CHART("bar");

    private String label;

    private ReportType(String label) {
        this.label = label;
    }

    public By getLocator() {
        return By.cssSelector(".vis-type-" + label);
    }

    @Override
    public String toString() {
        return WordUtils.capitalize(label) + (this == TABLE ? "" : " chart");
    };
}
