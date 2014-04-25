package com.gooddata.qa.graphene.enums.metrics;

public enum LogicalMetricTypes {
    AND("AND", 5),
    OR("OR", 5),
    CASE("CASE", 4),
    IF("IF", 3),
    NOT("NOT", 3);
    
    private final String label;
    private final int parametersCount;

    private LogicalMetricTypes(String label, int parametersCount) {
        this.label = label;
        this.parametersCount = parametersCount;
    }

    public String getLabel() {
        return label;
    }
    
    public int getParametersCount() {
        return parametersCount;
    }
}
