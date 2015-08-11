package com.gooddata.qa.graphene.enums.report;

public enum ReportTypes {

    HEADLINE("Headline report", "oneNumberContainerTab"),
    TABLE("Table", "gridContainerTab"),
    LINE("Line chart"),
    AREA("Area chart"),
    STACKED_AREA("Stacked Area chart"),
    BAR("Bar chart"),
    STACKED_BAR("Stacked Bar chart"),
    BULLET("Bullet chart"),
    WATERFALL("Waterfall chart"),
    FUNNEL("Funnel chart"),
    PIE("Pie chart"),
    DONUT("Donut chart"),
    SCATTER("Scatter chart"),
    BUBBLE("Bubble chart");

    private final String name;

    private final String containerTabId;

    private ReportTypes(String name, String containerTabId) {
        this.name = name;
        this.containerTabId = containerTabId;
    }

    private ReportTypes(String name) {
        this(name, "chartContainerTab");
    }

    public String getName() {
        return name;
    }

    public String getContainerTabId() {
        return containerTabId;
    }
}
