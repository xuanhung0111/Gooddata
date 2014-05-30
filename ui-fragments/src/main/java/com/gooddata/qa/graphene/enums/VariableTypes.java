package com.gooddata.qa.graphene.enums;

public enum VariableTypes {
    ATTRIBUTE("Filtered Variable"),
    NUMERIC("Numerical Variable");

    private final String label;

    private VariableTypes(String label) {
	this.label = label;
    }

    public String getlabel() {
	return label;
    }

}
