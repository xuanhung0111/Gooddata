package com.gooddata.qa.graphene.enums.metrics;

public enum GranularityMetricTypes {
    BY("BY", 3),
    BY_ALL_ATTRIBUTE("BY ALL attributes", 3),
    BY_ALL("BY ALL IN ALL OTHER DIMENSIONS", 2),
    BY_ATTR_ALL_OTHER("BY Attr, ALL OTHER", 3),
    FOR_NEXT("FOR Next", 2),
    FOR_PREVIOUS("FOR Previous", 2),
    FOR_NEXT_PERIOD("FOR NextPeriod", 2),
    FOR_PREVIOUS_PERIOD("FOR PreviousPeriod", 2),
    BY_ALL_EXCEPT("BY ALL IN ALL OTHER DIMENSIONS EXCEPT (FOR)", 2),
    WITHIN("WITHIN", 2);
    
    private final String label;
    private final int parametersCount;

    private GranularityMetricTypes(String label, int parametersCount) {
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
