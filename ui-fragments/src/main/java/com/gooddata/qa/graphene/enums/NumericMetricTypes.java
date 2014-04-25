package com.gooddata.qa.graphene.enums;

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
    private final int parameter;

    private NumericMetricTypes(String label, int parameter) {
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
