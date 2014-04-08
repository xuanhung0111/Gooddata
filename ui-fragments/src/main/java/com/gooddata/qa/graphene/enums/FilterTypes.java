package com.gooddata.qa.graphene.enums;

public enum FilterTypes {
    ATTRIBUTE("attribute filter", "addFilterSelectList"),
    RANK("rank filter", "addRankFilter"),
    RANGE("range filter", "addRangeFilter"),
    PROMPT("prompt filter", "addPromtFiter");

    private final String name;
    private final String label;

    private FilterTypes(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}
