package com.gooddata.qa.graphene.enums;

public enum LegendPosition {
    AUTO_DEFAULT("auto (default)", ".s-auto__default_"),
    TOP("top", ".dropdown-icon-legend-top"),
    BOTTOM("bottom", ".dropdown-icon-legend-bottom"),
    RIGHT("right", ".dropdown-icon-legend-right"),
    LEFT("left", ".dropdown-icon-legend-left");
    private String position;
    private String cssPosition;

    LegendPosition(String position, String cssPosition) {
        this.position = position;
        this.cssPosition = cssPosition;
    }

    public String getCssPosition() {
        return cssPosition;
    }

    public String getPosition() {
        return position;
    }
}
