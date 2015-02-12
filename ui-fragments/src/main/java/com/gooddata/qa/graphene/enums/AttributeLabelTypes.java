package com.gooddata.qa.graphene.enums;

public enum AttributeLabelTypes {
    TEXT("Text", false),
    IMAGE("Image", false),
    HYPERLINK("Hyperlink", false),
    GEO_PUSHPIN("Geo pushpin", true),
    AUS_STATE_NAME("Australia States (Name)", true),
    AUS_STATE_ISO("Australia States (ISO code)", true),
    US_STATE_NAME("US States (Name)", true),
    US_STATE_CENSUS_ID("US States (US Census ID)", true),
    US_STATE_CODE("US States (2-letter code)", true),
    US_COUNTY_CENSUS_ID("US Counties (US Census ID)", true),
    WORLD_COUNTRIES_NAME("World countries (Name)", true),
    WORLD_COUNTRIES_ISO2("World countries (ISO a2)", true),
    WORLD_COUNTRIES_ISO3("World countries (ISO a3)", true),
    CZ_DISTRICT_NAME("Czech Districts (Name)", true),
    CZ_DISTRICT_NAME_WO_DIAC("Czech Districts (Name without diacritics)", true),
    CZ_DISTRICT_NUTS4("Czech Districts (NUTS 4)", true),
    CZ_DISTRICT_KNOK("Czech Districts (KNOK)", true);

    private final String label;
    private boolean geoLabel;

    private AttributeLabelTypes(String label, boolean geoLabel) {
        this.label = label;
        this.geoLabel = geoLabel;
    }

    public String getlabel() {
        return label;
    }

    public boolean isGeoLabel() {
        return geoLabel;
    }

}
