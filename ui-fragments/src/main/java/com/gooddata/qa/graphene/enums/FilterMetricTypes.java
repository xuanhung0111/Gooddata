package com.gooddata.qa.graphene.enums;

public enum FilterMetricTypes {
    EQUAL("= (equals)", 3),
    BETWEEN ("BETWEEN", 4),
    NOT_BETWEEN("NOT BETWEEN", 4),
    IN("IN", 4),
    NOT_IN("NOT IN", 4),
    TOP("TOP", 3),
    BOTTOM("BOTTOM", 3),
    WITHOUT_PF("WITHOUT PARENT FILTER", 3),
    THIS("{This}", 2),
    PREVIOUS("{Previous}", 2),
    NEXT("{Next}", 2);
    
    private final String label;
    private final int parameter;

    private FilterMetricTypes(String label, int parameter) {
        this.label = label;
        this.parameter = parameter;
    }

    public String getlabel() {
        return label;
    }
    
    public int getParameter() {
        return parameter;
    }
}
