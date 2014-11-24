package com.gooddata.qa.graphene.entity.filter;

import com.gooddata.qa.graphene.enums.metrics.FilterMetric;

public class RankingFilterItem extends FilterItem {

    private ResultSize size;
    private String attribute;
    private String metric;
    private FilterMetric filter;

    RankingFilterItem(ResultSize size, String attribute, String metric) {
        this.size = size;
        this.attribute = attribute;
        this.metric = metric;
    }

    RankingFilterItem(String attribute, String metric) {
        this(ResultSize.TOP.withSize(3), attribute, metric);
    }

    public void setSize(ResultSize size) {
        this.size = size;
    }

    public ResultSize getSize() {
        return size;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getMetric() {
        return metric;
    }

    public void setFilter(FilterMetric filter) {
        this.filter = filter;
    }

    public FilterMetric getFilter() {
        return filter;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public enum ResultSize {
        TOP,
        BOTTOM;

        private int size;

        private ResultSize(int size) {
            this.size = size;
        }

        private ResultSize() {
            this.size = 3;
        }

        public ResultSize withSize(int size) {
            this.size = size;
            return this;
        }

        public int getSize() {
            return size;
        }
    }
}
