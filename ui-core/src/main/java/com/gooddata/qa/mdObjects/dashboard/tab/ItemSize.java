package com.gooddata.qa.mdObjects.dashboard.tab;

enum ItemSize {
    FILTER_ITEM(190, 40),
    REPORT_ITEM(220, 310);

    private int sizeX;
    private int sizeY;

    ItemSize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }
}
