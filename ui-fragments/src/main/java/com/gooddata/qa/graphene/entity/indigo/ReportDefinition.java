package com.gooddata.qa.graphene.entity.indigo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class ReportDefinition {

    private ReportType type;
    private List<String> metrics;
    private List<String> categories;
    private List<String> filters;
    private boolean showInPercents;

    public ReportDefinition() {
        metrics = new ArrayList<String>();
        categories = new ArrayList<String>();
        filters = new ArrayList<String>();
        type = ReportType.COLUMN_CHART;
        showInPercents = false;
    }

    public ReportDefinition withMetrics(String... metrics) {
        this.metrics.addAll(Arrays.asList(metrics));
        return this;
    }

    public ReportDefinition withCategories(String... categories) {
        this.categories.addAll(Arrays.asList(categories));
        return this;
    }

    public ReportDefinition withType(ReportType type) {
        this.type = type;
        return this;
    }

    public ReportDefinition withFilters(String... filters) {
        this.filters.addAll(Arrays.asList(filters));
        return this;
    }

    public ReportDefinition withShowInPercents() {
        this.showInPercents = true;
        return this;
    }

    public List<String> getMetrics() {
        return new ArrayList<String>(metrics);
    }

    public List<String> getCategories() {
        return new ArrayList<String>(categories);
    }

    public ReportType getType() {
        return type;
    }

    public List<String> getFilters() {
        return new ArrayList<String>(filters);
    }

    public boolean isShowInPercents() {
        return showInPercents;
    }
}
