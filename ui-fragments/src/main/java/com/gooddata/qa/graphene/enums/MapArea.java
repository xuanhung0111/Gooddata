package com.gooddata.qa.graphene.enums;

public enum MapArea {
    ALL_DATA("Include all data", "s-include_all_data"),
    WORLD("World", "s-world"),
    AFRICA("Africa", "s-africa"),
    AMERICAN_NORTH("America (North)", "s-america__north_"),
    AMERICAN_SOUTH("America (South)", "s-america__south_"),
    ASIA("Asia", "s-asia"),
    AUSTRALIA("Australia", "s-australia"),
    EUROPE("Europe", "s-europe");
    private String name;
    private String cssSelector;

    MapArea(String name, String cssSelector) {
        this.name = name;
        this.cssSelector = cssSelector;
    }

    public String getCssSelector() {
        return cssSelector;
    }

    public String getCountryName() {
        return name;
    }
}
