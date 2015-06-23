package com.gooddata.qa.graphene.enums.indigo;

public enum FieldType {
    METRIC("type-metric"),
    FACT("type-fact"),
    ATTRIBUTE("type-attribute"),
    DATE("type-date");

    private String type;

    private FieldType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
