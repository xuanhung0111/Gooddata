package com.gooddata.qa.graphene.entity.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;

public class UiReportDefinition {

    private String name;
    private ReportTypes type;
    private List<HowItem> hows;
    private List<WhatItem> whats;
    private List<FilterItem> filters;

    public UiReportDefinition() {
        hows = new ArrayList<HowItem>();
        whats = new ArrayList<WhatItem>();
        filters = new ArrayList<FilterItem>();
        type = ReportTypes.TABLE;
    }

    public boolean shouldAddWhatToReport() {
        return !whats.isEmpty();
    }

    public boolean shouldAddHowToReport() {
        return !hows.isEmpty();
    }

    public boolean shouldAddFilterToReport() {
        return !filters.isEmpty();
    }

    public String getName() {
        return name;
    }

    public ReportTypes getType() {
        return type;
    }

    public List<WhatItem> getWhats() {
        return whats;
    }

    public List<HowItem> getHows() {
        return hows;
    }

    public List<FilterItem> getFilters() {
        return filters;
    }

    public UiReportDefinition withName(String name) {
        this.name = name;
        return this;
    }

    public UiReportDefinition withType(ReportTypes type) {
        this.type = type;
        return this;
    }

    public UiReportDefinition withHows(String... how) {
        for (String attribute : how) {
            this.hows.add(new HowItem(attribute));
        }
        return this;
    }

    public UiReportDefinition withHows(HowItem... how) {
        this.hows.addAll(Arrays.asList(how));
        return this;
    }

    public UiReportDefinition withWhats(String... what) {
        for (String metric : what) {
            this.whats.add(new WhatItem(metric));
        }
        return this;
    }

    public UiReportDefinition withWhats(WhatItem... what) {
        this.whats.addAll(Arrays.asList(what));
        return this;
    }

    public UiReportDefinition withFilters(FilterItem... filter) {
        this.filters.addAll(Arrays.asList(filter));
        return this;
    }
}
