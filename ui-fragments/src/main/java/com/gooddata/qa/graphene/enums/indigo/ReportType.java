package com.gooddata.qa.graphene.enums.indigo;

import org.apache.commons.lang.WordUtils;
import org.openqa.selenium.By;

public enum ReportType {

    TABLE("table"),
    COLUMN_CHART("column"),
    PIE_CHART("pie"),
    TREE_MAP("treemap"),
    STACKS_AREA_CHART("area"),
    HEADLINE("headline"),
    SCATTER_PLOT("scatter"),
    BUBBLE_CHART("bubble"),
    DONUT_CHART("donut"),
    HEAT_MAP("heatmap"),
    LINE_CHART("line") {
        @Override
        public String getMetricMessage() {
            return "TO ADD ADDITIONAL MEASURE, REMOVE FROM SEGMENT BY";
        }

        @Override
        public String getStackByMessage() {
            return "TO SEGMENT BY, AN INSIGHT CAN HAVE ONLY ONE MEASURE";
        }
    },

    HEAD_LINE("headline"),
    BAR_CHART("bar"),
    STACKED_AREA_CHART("area") {
        @Override
        public String getStackByMessage() {
            return "TO STACK BY, AN INSIGHT CAN HAVE ONLY ONE ATTRIBUTE IN VIEW BY";
        }
    };

    private String label;

    ReportType(String label) {
        this.label = label;
    }

    public By getLocator() {
        return By.cssSelector(".gd-vis-type-" + label);
    }

    public String getMetricMessage() {
        return "TO ADD ADDITIONAL MEASURE, REMOVE FROM STACK BY";
    }

    public String getStackByMessage() {
        return "TO STACK BY, AN INSIGHT CAN HAVE ONLY ONE MEASURE";
    }

    public String getViewbyByMessage() {
        return "TO VIEW BY ANOTHER ATTRIBUTE, AN INSIGHT CAN HAVE ONLY ONE MEASURE";
    }

    public String getExtendedStackByMessage() {
        return "TO STACK BY AN ATTRIBUTE, AN INSIGHT CAN HAVE ONLY ONE MEASURE";
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return WordUtils.capitalize(label) + (this == TABLE ? "" : " chart");
    }
}
