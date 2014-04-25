package com.gooddata.qa.graphene.enums;

public enum AggregationMetricTypes {
    AVG("AVG", 1),
    RUNAVG("RUNAVG", 1),
    MAX("MAX", 1),
    RUNMAX("RUNMAX", 1),
    MIN("MIN", 1),
    RUNMIN("RUNMIN", 1),
    SUM("SUM",1),
    RUNSUM("RUNSUM",1),
    MEDIAN("MEDIAN", 1),
    CORREL("CORREL", 2),
    COUNT("COUNT", 2),
    COVAR("COVAR", 2),
    COVARP("COVARP",2),
    INTERCEPT("INTERCEPT", 2),
    PERCENTILE("PERCENTILE", 2),
    RSQ("RSQ",2),
    SLOPE("SLOPE", 2),
    STDEV("STDEV", 2),
    STDEVP("STDEVP", 2),
    VAR("VAR", 1),
    VARP("VARP",1);
    
    private final String label;
    private final int parameter;

    private AggregationMetricTypes(String label, int parameter) {
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
