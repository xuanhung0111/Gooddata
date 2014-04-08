package com.gooddata.qa.graphene.enums;


public enum ReportTypes {

    HEADLINE("Headline report", "oneNumberContainerTab"),
    TABLE("Table", "gridContainerTab"),
    LINE("Line chart", null),
    AREA("Area chart", null),
    STACKED_AREA("Stacked Area chart", null),
    BAR("Bar chart", null),
    STACKED_BAR("Stacked Bar chart", null),
    BULLET("Bullet chart", null),
    WATERFALL("Waterfall chart", null),
    FUNNEL("Funnel chart", null),
    PIE("Pie chart", null),
    DONUT("Donut chart", null),
    SCATTER("Scatter chart", null),
    BUBBLE("Bubble chart", null);

    private final String name;

    private final String containerTabId;

    private ReportTypes(String name, String containerTabId) {
        this.name = name;
        if (containerTabId == null) {
            containerTabId = "chartContainerTab";
        }
        this.containerTabId = containerTabId;
    }

    public String getName() {
        return name;
    }

    public String getContainerTabId() {
        return containerTabId;
    }
}
