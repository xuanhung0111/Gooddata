package com.gooddata.qa.graphene.enums;

public enum DashFilterTypes {
    ATTRIBUTE("attribute filter"),
    PROMPT("prompt filter");

    private final String label;


    private DashFilterTypes(String label) {
        this.label = label;
    }

    public String getlabel() {
        return label;
    }


}
