package com.gooddata.qa.graphene.enums.indigo;

import org.openqa.selenium.By;

public enum ShortcutPanel {

    TRENDED_OVER_TIME(By.cssSelector(".s-recommendation-metric-over-time-canvas")),
    AS_A_COLUMN_CHART(By.cssSelector(".s-recommendation-metric-canvas"));

    private By locator;

    private ShortcutPanel(By locator) {
        this.locator = locator;
    }

    public By getLocator() {
        return locator;
    }
}
