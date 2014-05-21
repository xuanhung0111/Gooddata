package com.gooddata.qa.graphene.enums.metrics;

public enum SimpleMetricTypes {
    SUM("SUM"),
    MIN("MIN"),
    MAX("MAX"),
    AVG("AVG"),
    MEDIAN("MEDIAN"),
    RUNSUM("RUNSUM"),
    RUNMIN("RUNMIN"),
    RUNMAX("RUNMAX"),
    RUNAVG("RUNAVG");
    
    private final String label;

    private SimpleMetricTypes(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
}
