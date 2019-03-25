package com.gooddata.qa.graphene.enums.indigo;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum AggregationItem {
    SUM("Sum", "sum", "Sum"),
    MAX("Max", "max", "Max"),
    MIN("Min", "min", "Min"),
    AVG("Avg", "avg", "Avg"),
    MEDIAN("Median", "med", "Median"),
    ROLLUP("Rollup (Total)", "nat", "Total");

    private String fullName;
    private String metadataName;
    private String rowName;

    AggregationItem(String fullName, String metadataName, String rowName) {
        this.fullName = fullName;
        this.metadataName = metadataName;
        this.rowName = rowName;
    }

    public static AggregationItem fromString(String fullName) {
        for (AggregationItem aggregation : AggregationItem.values()) {
            if (aggregation.getFullName().equals(fullName)) {
                return aggregation;
            }
        }
        throw new IllegalArgumentException("Displayed name does not match with any AggregationItem");
    }

    public static List<String> getAllRowNames() {
        return Arrays.stream(AggregationItem.values()).map(AggregationItem::getRowName).collect(toList());
    }

    public static List<String> getAllFullNames() {
        return Arrays.stream(AggregationItem.values()).map(AggregationItem::getFullName).collect(toList());
    }

    public String getFullName() {
        return fullName;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public String getRowName() {
        return rowName;
    }
}
