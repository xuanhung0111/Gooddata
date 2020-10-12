package com.gooddata.qa.graphene.enums;

public enum ZoomValue {
    TWENTY_FIVE("25%", "matrix(0.25,0,0,0.25,0,0)"),
    FIFTY("50%", "matrix(0.5,0,0,0.5,0,0)"),
    SEVENTY_FIVE("75%", "matrix(0.75,0,0,0.75,0,0)"),
    ONE_HUNDRED("100%", "matrix(1,0,0,1,0,0)"),
    ONR_HUNDRED_TWENTY_FIVE("125%", "matrix(1.25,0,0,1.25,0,0)"),
    ZOOM_TO_FIT("Zoom To Fit", "zoomToFit");

    private String zoomValue;
    private String transform;

    ZoomValue(String zoomValue, String transform) {
        this.zoomValue = zoomValue;
        this.transform = transform;
    }

    public String getZoomValue() {
        return this.zoomValue;
    }

    public String getTransformLayer() {
        return this.transform;
    }
}
