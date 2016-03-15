package com.gooddata.qa.graphene.entity.kpi;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;

public class KpiMDConfiguration {

    private final String title;
    private final String metric;
    private final String dateDataSet;
    private final ComparisonType comparisonType;
    private final ComparisonDirection comparisonDirection;
    private final String drillToDashboard;
    private final String drillToDashboardTab;

    private KpiMDConfiguration(
        final String newTitle,
        final String newMetric,
        final String newDateDataSet,
        final ComparisonType newComparisonType,
        final ComparisonDirection newComparisonDirection,
        final String newDrillToDashboard,
        final String newDrillToDashboardTab)
    {
        this.title = newTitle;
        this.metric = newMetric;
        this.dateDataSet = newDateDataSet;
        this.comparisonType = newComparisonType;
        this.comparisonDirection = newComparisonDirection;
        this.drillToDashboard = newDrillToDashboard;
        this.drillToDashboardTab = newDrillToDashboardTab;
    }

    public String getTitle() {
        return title;
    }

    public String getMetric() {
        return metric;
    }

    public String getDateDataSet() {
        return dateDataSet;
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public ComparisonDirection getComparisonDirection() {
        return comparisonDirection;
    }

    public String getDrillToDashboard() {
        return drillToDashboard;
    }

    public String getDrillToDashboardTab() {
        return drillToDashboardTab;
    }

    public boolean hasComparison() {
        return comparisonType != ComparisonType.NO_COMPARISON;
    }

    public boolean hasDrillTo() {
        return drillToDashboard != null && drillToDashboardTab != null;
    }

    public static class Builder {

        private String nestedTitle;
        private String nestedMetric;
        private String nestedDateDataSet;
        private ComparisonType nestedComparisonType;
        private ComparisonDirection nestedComparisonDirection;
        private String nestedDrillToDashboard = null;
        private String nestedDrillToDashboardTab;

        public Builder title(String newTitle) {
            this.nestedTitle = newTitle;
            return this;
        }

        public Builder metric(String newMetric) {
            this.nestedMetric = newMetric;
            return this;
        }

        public Builder dateDataSet(String newDateDataSet) {
            this.nestedDateDataSet = newDateDataSet;
            return this;
        }

        public Builder comparisonType(ComparisonType newComparisonType) {
            this.nestedComparisonType = newComparisonType;
            return this;
        }

        public Builder comparisonDirection(ComparisonDirection newComparisonDirection) {
            this.nestedComparisonDirection = newComparisonDirection;
            return this;
        }

        public Builder drillToDashboard(String newDrillToDashboard) {
            this.nestedDrillToDashboard = newDrillToDashboard;
            return this;
        }

        public Builder drillToDashboardTab(String newDrillToDashboardTab) {
            this.nestedDrillToDashboardTab = newDrillToDashboardTab;
            return this;
        }

        public KpiMDConfiguration build() {
            return new KpiMDConfiguration(
                    nestedTitle,
                    nestedMetric,
                    nestedDateDataSet,
                    nestedComparisonType,
                    nestedComparisonDirection,
                    nestedDrillToDashboard,
                    nestedDrillToDashboardTab);
        }
    }
}
