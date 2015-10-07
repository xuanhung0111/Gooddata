package com.gooddata.qa.graphene.entity.kpi;

public class KpiConfiguration {

    private final String metric;
    private final String dateDimension;
    private final String comparison;
    private final String drillTo;

    private KpiConfiguration(final String newMetric, final String newDateDimension, final String newComparison, final String newDrillTo) {
        this.metric = newMetric;
        this.dateDimension = newDateDimension;
        this.comparison = newComparison;
        this.drillTo = newDrillTo;
    }

    public String getMetric() {
        return metric;
    }

    public String getDateDimension() {
        return dateDimension;
    }

    public String getComparison() {
        return comparison;
    }

    public String getDrillTo() {
        return drillTo;
    }

    public boolean hasComparison() {
        return comparison != null;
    }

    public boolean hasDrillTo() {
        return drillTo != null;
    }

    public static class Builder {

        private String nestedMetric;
        private String nestedDateDimension;
        private String nestedComparison = null;
        private String nestedDrillTo = null;

        public Builder metric(String newMetric) {
            this.nestedMetric = newMetric;
            return this;
        }

        public Builder dateDimension(String newDateDimension) {
            this.nestedDateDimension = newDateDimension;
            return this;
        }

        public Builder comparison(String newComparison) {
            this.nestedComparison = newComparison;
            return this;
        }

        public Builder drillTo(String newDrillTo) {
            this.nestedDrillTo = newDrillTo;
            return this;
        }

        public KpiConfiguration build() {
            return new KpiConfiguration(nestedMetric, nestedDateDimension, nestedComparison, nestedDrillTo);
        }
    }
}
