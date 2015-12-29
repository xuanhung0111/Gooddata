package com.gooddata.qa.graphene.entity.filter;

import static java.lang.Float.compare;

import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.metrics.FilterMetric;

public class RangeFilterItem extends FilterItem {

    private String metric;
    private RangeType rangeType;
    private int rangeNumber;
    private FilterMetric filter;
    private List<String> attributes;

    RangeFilterItem(RangeType rangeType, int rangeNumber, String metric, String...attributes) {
        this.rangeType = rangeType;
        this.rangeNumber = rangeNumber;
        this.metric = metric;
        this.attributes = Arrays.asList(attributes);
    }

    RangeFilterItem(String metric, String...attributes) {
        this(RangeType.IS_GREATER_THAN_OR_EQUAL_TO, 0, metric, attributes);
    }

    public RangeFilterItem withAttributes(String...attributes) {
        this.attributes = Arrays.asList(attributes);
        return this;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public RangeFilterItem withMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public String getMetric() {
        return metric;
    }

    public RangeFilterItem withRangeType(RangeType rangeType) {
        this.rangeType = rangeType;
        return this;
    }

    public RangeType getRangeType() {
        return rangeType;
    }

    public RangeFilterItem withRangeNumber(int rangeNumber) {
        this.rangeNumber = rangeNumber;
        return this;
    }

    public int getRangeNumber() {
        return rangeNumber;
    }

    public RangeFilterItem withFilter(FilterMetric filter) {
        this.filter = filter;
        return this;
    }

    public FilterMetric getFilter() {
        return filter;
    }

    public enum RangeType {
        IS_GREATER_THAN_OR_EQUAL_TO {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) >= 0;
            }
        },
        IS_EQUAL_TO {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) == 0;
            }
        },
        IS_NOT_EQUAL_TO {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) != 0;
            }
        },
        IS_LESS_THAN {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) == -1;
            }
        },
        IS_GREATER_THAN {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) == 1;
            }
        },
        IS_LESS_THAN_OR_EQUAL_TO {
            @Override
            public boolean isMetricValueInRange(float metricValue, int rangeNumber) {
                return compare(metricValue, rangeNumber * 1F) <= 0;
            }
        };

        @Override
        public String toString() {
            return name().toLowerCase().replaceAll("_", " ");
        }

        public abstract boolean isMetricValueInRange(float metricValue, int rangeNumber);
    }
}
