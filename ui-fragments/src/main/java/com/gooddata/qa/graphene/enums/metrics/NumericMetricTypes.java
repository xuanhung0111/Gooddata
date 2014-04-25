package com.gooddata.qa.graphene.enums.metrics;

public enum NumericMetricTypes {
    ABS("ABS",1),
    EXP("EXP", 1),
    IFNULL("IFNULL",1),
    LOG("LOG", 1),
    LN("LN", 1),
    POWER("POWER", 1),
    RANK("RANK", 1),
    ROUND("ROUND", 1),
    FLOOR("FLOOR", 1),
    CEILING("CEILING", 1),
    TRUNC("TRUNC", 1),
    SIGN("SIGN", 1),
    SQRT("SQRT", 1);
    
    private final String label;
    private final int parametersCount;

    private NumericMetricTypes(String label, int parametersCount) {
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
