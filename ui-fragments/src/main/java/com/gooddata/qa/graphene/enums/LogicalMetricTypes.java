package com.gooddata.qa.graphene.enums;

public enum LogicalMetricTypes {
    AND("AND", 5),
    OR("OR", 5),
    CASE("CASE", 4),
    IF("IF", 3),
    NOT("NOT", 3);
    
    private final String label;
    private final int parameter;

    private LogicalMetricTypes(String label, int parameter) {
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
