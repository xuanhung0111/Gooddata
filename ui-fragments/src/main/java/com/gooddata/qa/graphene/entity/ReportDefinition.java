package com.gooddata.qa.graphene.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.ReportTypes;

public class ReportDefinition {

    private String name;
    private ReportTypes type;
    private List<HowItem> hows;
    private List<WhatItem> whats;
    private List<FilterItem> filters;

    public ReportDefinition() {
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

    public ReportDefinition withName(String name) {
        this.name = name;
        return this;
    }

    public ReportDefinition withType(ReportTypes type) {
        this.type = type;
        return this;
    }

    public ReportDefinition withHows(String... how) {
        for (String attribute : how) {
            this.hows.add(new HowItem(attribute));
        }
        return this;
    }

    public ReportDefinition withHows(HowItem... how) {
        this.hows.addAll(Arrays.asList(how));
        return this;
    }

    public ReportDefinition withWhats(String... what) {
        for (String metric : what) {
            this.whats.add(new WhatItem(metric));
        }
        return this;
    }

    public ReportDefinition withWhats(WhatItem... what) {
        this.whats.addAll(Arrays.asList(what));
        return this;
    }

    public ReportDefinition withFilters(FilterItem... filter) {
        this.filters.addAll(Arrays.asList(filter));
        return this;
    }
}
