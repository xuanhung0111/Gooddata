package com.gooddata.qa.graphene.enums.indigo;

import org.apache.commons.lang.WordUtils;
import org.openqa.selenium.By;

public enum ReportType {

    TABLE("table"),
    COLUMN_CHART("column"),

    LINE_CHART("line") {
        @Override
        public String getMetricMessage() {
            return "TO ADD ADDITIONAL SERIES, REMOVE FROM SEGMENT BY";
        }

        @Override
        public String getStackByMessage() {
            return "TO SEGMENT BY, A VISUALIZATION CAN HAVE ONLY ONE SERIES";
        }
    },

    BAR_CHART("bar");

    private String label;

    private ReportType(String label) {
        this.label = label;
    }

    public By getLocator() {
        return By.cssSelector(".vis-type-" + label);
    }

    public String getMetricMessage() {
        return "TO ADD ADDITIONAL SERIES, REMOVE FROM STACK BY";
    }

    public String getStackByMessage() {
        return "TO STACK BY, A VISUALIZATION CAN HAVE ONLY ONE SERIES";
    }

    @Override
    public String toString() {
        return WordUtils.capitalize(label) + (this == TABLE ? "" : " chart");
    }
}
