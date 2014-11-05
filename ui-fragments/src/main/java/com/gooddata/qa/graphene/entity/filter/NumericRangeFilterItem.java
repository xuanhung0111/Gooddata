package com.gooddata.qa.graphene.entity.filter;

import com.gooddata.qa.graphene.enums.metrics.FilterMetric;

public class NumericRangeFilterItem extends FilterItem {

    private String attribute;
    private String metric;
    private Range range;
    private FilterMetric filter;

    NumericRangeFilterItem(String attribute, String metric, Range range) {
        this.attribute = attribute;
        this.metric = metric;
        this.range = range;
    }

    NumericRangeFilterItem(String attribute, String metric) {
        this(attribute, metric, Range.IS_GREATER_THAN_OR_EQUAL_TO.withNumber(0));
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getMetric() {
        return metric;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Range getRange() {
        return range;
    }

    public void setFilter(FilterMetric filter) {
        this.filter = filter;
    }

    public FilterMetric getFilter() {
        return filter;
    }

    public enum Range {
        IS_GREATER_THAN_OR_EQUAL_TO,
        IS_EQUAL_TO,
        IS_NOT_EQUAL_TO,
        IS_LESS_THAN,
        IS_GREATER_THAN,
        IS_LESS_THAN_OR_EQUAL_TO;

        private int range;

        private Range() {
            range = 0;
        }

        public Range withNumber(int range) {
            this.range = range;
            return this;
        }

        public int getNumber() {
            return range;
        }
    }
}
