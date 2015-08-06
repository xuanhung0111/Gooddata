package com.gooddata.qa.graphene.entity.report;

public class WhatItem {

    private String metric;
    private String drillStep;
    private String metricFormat;

    public WhatItem(String metric, String drillStep, String metricFormat) {
        this.metric = metric;
        this.drillStep = drillStep;
        this.metricFormat = metricFormat;
    }

    public WhatItem(String metric) {
        this(metric, null, null);
    }

    public WhatItem(String metric, String drillStep) {
        this(metric, drillStep, null);
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getMetric() {
        return metric;
    }

    public void setDrillStep(String drillStep) {
        this.drillStep = drillStep;
    }

    public String getDrillStep() {
        return drillStep;
    }

    public void setMetricFormat(String metricFormat) {
        this.metricFormat = metricFormat;
    }

    public String getMetricFormat() {
        return metricFormat;
    }
}
