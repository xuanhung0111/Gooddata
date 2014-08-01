package com.gooddata.qa.graphene.enums.metrics;

public enum MetricTypes {
    // Numeric
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
    SQRT("SQRT", 1),
    SUBTRACTION("+, -, *, /", 2),
    // Aggregation
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
    RUNSTDEV("RUNSTDEV", 1),
    VAR("VAR", 1),
    RUNVAR("RUNVAR",1),
    // Filter
    EQUAL("= (equals)", 3),
    DOES_NOT_EQUAL("<> (does not equal)", 3),
    GREATER("> (greater)", 3),
    LESS("< (less)", 3),
    GREATER_OR_EQUAL(">= (greater or equal)", 3),
    LESS_OR_EQUAL("<= (less or equal)", 3),
    BETWEEN ("BETWEEN", 4),
    NOT_BETWEEN("NOT BETWEEN", 4),
    IN("IN", 4),
    NOT_IN("NOT IN", 4),
    // TODO: TOP("TOP", 3),
    // TODO: BOTTOM("BOTTOM", 3),
    WITHOUT_PF("WITHOUT PARENT FILTER", 3),
    // Logical
    AND("AND", 5),
    OR("OR", 5),
    CASE("CASE", 4),
    IF("IF", 3),
    NOT("NOT", 3),
    // Granularity
    BY("BY", 3),
    BY_ALL_ATTRIBUTE("BY ALL attributes", 3),
    BY_ALL("BY ALL IN ALL OTHER DIMENSIONS", 2),
    BY_ATTR_ALL_OTHER("BY Attr, ALL OTHER", 3),
    FOR_NEXT("FOR Next", 2),
    FOR_PREVIOUS("FOR Previous", 2),
    FOR_NEXT_PERIOD("FOR NextPeriod", 2),
    FOR_PREVIOUS_PERIOD("FOR PreviousPeriod", 2),
    BY_ALL_EXCEPT("BY ALL IN ALL OTHER DIMENSIONS EXCEPT (FOR)", 2);

    private final String label;
    private final int parametersCount;

    private MetricTypes(String label, int parametersCount) {
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