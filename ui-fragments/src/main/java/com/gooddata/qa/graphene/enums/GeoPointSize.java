package com.gooddata.qa.graphene.enums;

public enum GeoPointSize {
    DEFAULT("auto (default)", "s-auto__default_"),
    SIZE_0_5X("0.5x", "s-0_5x"),
    SIZE_0_75X("0.75x", "s-0_75x"),
    NORMAL("normal", "s-normal"),
    SIZE_1_25X("1.25x", "s-1_25x"),
    SIZE_1_5X("1.5x", "s-1_5x");
    private String size;
    private String cssSize;

    GeoPointSize(String size, String cssSize) {
        this.size = size;
        this.cssSize = cssSize;
    }

    public String getCssSize() { return cssSize;
    }

    public String getSize() { return size;
    }
}
