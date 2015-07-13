package com.gooddata.qa.graphene.enums.metrics;

import java.util.Collection;

public enum MetricTypes {

    // Numeric
    ABS("SELECT ABS(__metric__)", "Numeric"),
    EXP("SELECT EXP(__metric__)", "Numeric"),
    IFNULL("SELECT IFNULL(__metric__,0)", "Numeric"),
    LOG("SELECT LOG(__metric__)", "Numeric"),
    LN("SELECT LN(__metric__)", "Numeric"),
    POWER("SELECT POWER(__metric__,1)", "Numeric"),
    RANK("SELECT RANK(__metric__)", "Numeric"),
    ROUND("SELECT ROUND(__metric__)", "Numeric"),
    FLOOR("SELECT FLOOR(__metric__)", "Numeric"),
    CEILING("SELECT CEILING(__metric__)", "Numeric"),
    TRUNC("SELECT TRUNC(__metric__)", "Numeric"),
    SIGN("SELECT SIGN(__metric__)", "Numeric"),
    SQRT("SELECT SQRT(__metric__)", "Numeric"),
    SUBTRACTION("+, -, *, /", "SELECT __metric__ - __metric__", "Numeric"),

    // Aggregation
    AVG("SELECT AVG(__fact__)", "Aggregation"),
    RUNAVG("SELECT RUNAVG(__fact__)", "Aggregation"),
    MAX("SELECT MAX(__fact__)", "Aggregation"),
    RUNMAX("SELECT RUNMAX(__fact__)", "Aggregation"),
    MIN("SELECT MIN(__fact__)", "Aggregation"),
    RUNMIN("SELECT RUNMIN(__fact__)", "Aggregation"),
    SUM("SELECT SUM(__fact__)", "Aggregation"),
    RUNSUM("SELECT RUNSUM(__fact__)", "Aggregation"),
    MEDIAN("SELECT MEDIAN(__fact__)", "Aggregation"),
    CORREL("SELECT CORREL(__fact__,__fact__)", "Aggregation"),
    COUNT("SELECT COUNT(__attr__,__attr__)", "Aggregation"),
    COVAR("SELECT COVAR(__fact__,__fact__)", "Aggregation"),
    COVARP("SELECT COVARP(__fact__,__fact__)", "Aggregation"),
    PERCENTILE("SELECT PERCENTILE(__fact__,0.25)", "Aggregation"),
    RSQ("SELECT RSQ(__fact__,__fact__)", "Aggregation"),
    STDEV("SELECT STDEV(__fact__)", "Aggregation"),
    RUNSTDEV("SELECT RUNSTDEV(__fact__)", "Aggregation"),
    VAR("SELECT VAR(__fact__)", "Aggregation"),
    RUNVAR("SELECT RUNVAR(__fact__)", "Aggregation"),

    // Filter
    EQUAL("= (equals)", "SELECT __metric__ WHERE __attr__ = __attrValue__", "Filters"),
    DOES_NOT_EQUAL("<> (does not equal)", "SELECT __metric__ WHERE __attr__ <> __attrValue__", "Filters"),
    GREATER("> (greater)", "SELECT __metric__ WHERE __attr__ > __attrValue__", "Filters"),
    LESS("< (less)", "SELECT __metric__ WHERE __attr__ < __attrValue__", "Filters"),
    GREATER_OR_EQUAL(">= (greater or equal)", "SELECT __metric__ WHERE __attr__ >= __attrValue__", "Filters"),
    LESS_OR_EQUAL("<= (less or equal)", "SELECT __metric__ WHERE __attr__ <= __attrValue__", "Filters"),
    BETWEEN("SELECT __metric__ WHERE __attr__ BETWEEN __attrValue__ AND __attrValue__", "Filters"),
    NOT_BETWEEN(
            "NOT BETWEEN",
            "SELECT __metric__ WHERE __attr__ NOT BETWEEN __attrValue__ AND __attrValue__",
            "Filters"),
    IN("SELECT __metric__ WHERE __attr__ IN (__attrValue__, __attrValue__)", "Filters"),
    NOT_IN("NOT IN", "SELECT __metric__ WHERE __attr__ NOT IN (__attrValue__, __attrValue__)", "Filters"),
    WITHOUT_PF(
            "WITHOUT PARENT FILTER",
            "SELECT __metric__ - (SELECT __metric__ BY ALL __attr__ WITHOUT PARENT FILTER)",
            "Filters"),

    // Logical
    AND("SELECT __metric__ WHERE __attr__ = __attrValue__ AND __attr__ = __attrValue__", "Logical"),
    OR("SELECT __metric__ WHERE __attr__ = __attrValue__ OR __attr__ = __attrValue__", "Logical"),
    CASE("SELECT CASE WHEN __metric__ > __metric__ THEN 1, WHEN __metric__ < __metric__ THEN 2 ELSE 3 END", "Logical"),
    IF("SELECT IF __metric__ > 0.5 THEN __metric__ * 10 ELSE __metric__ / 10 END", "Logical"),
    NOT("SELECT __metric__ WHERE NOT (__attr__ = __attrValue__)", "Logical"),

    // Granularity
    BY("SELECT __metric__ / (SELECT __metric__ BY __attr__)", "Granularity"),
    BY_ALL_ATTRIBUTE("BY ALL attributes", "SELECT __metric__ / (SELECT __metric__ BY ALL __attr__)", "Granularity"),
    BY_ALL(
            "BY ALL IN ALL OTHER DIMENSIONS",
            "SELECT __metric__ / (SELECT __metric__ BY ALL IN ALL OTHER DIMENSIONS)",
            "Granularity"),
    BY_ATTR_ALL_OTHER(
            "BY Attr, ALL OTHER",
            "SELECT __metric__ / (SELECT __metric__ BY __attr__, ALL IN ALL OTHER DIMENSIONS)",
            "Granularity"),
    FOR_NEXT("FOR Next", "SELECT __metric__ FOR Next(__attr__)", "Granularity"),
    FOR_PREVIOUS("FOR Previous", "SELECT __metric__ FOR Previous(__attr__)", "Granularity"),
    FOR_NEXT_PERIOD("FOR NextPeriod", "SELECT __metric__ FOR NextPeriod(__attr__)", "Granularity"),
    FOR_PREVIOUS_PERIOD("FOR PreviousPeriod", "SELECT __metric__ FOR PreviousPeriod(__attr__)", "Granularity"),
    BY_ALL_EXCEPT(
            "BY ALL IN ALL OTHER DIMENSIONS EXCEPT (FOR)",
            "SELECT __metric__ BY ALL IN ALL OTHER DIMENSIONS EXCEPT __attr__",
            "Granularity");

    private final String label;
    private final String maql;
    private final String type;

    private MetricTypes(String label, String maql, String type) {
        this.label = label;
        this.maql = maql;
        this.type = type;
    }

    private MetricTypes(String maql, String type) {
        label = name();
        this.maql = maql;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public String getMaql() {
        return maql;
    }

    public String getType() {
        return type;
    }

    public boolean in(Collection<MetricTypes> types) {
        for (MetricTypes metric : types) {
            if (label.equals(metric.label))
                return true;
        }
        return false;
    }
}
