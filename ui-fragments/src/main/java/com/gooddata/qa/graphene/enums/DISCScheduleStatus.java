package com.gooddata.qa.graphene.enums;

import org.openqa.selenium.By;

public enum DISCScheduleStatus {

    OK(".status-icon-ok.icon-check"),
    ERROR(".status-icon-error"),
    SCHEDULED(".status-icon-scheduled.icon-sync"),
    DISABLED(".status-icon-disabled.icon-pause"),
    UNSCHEDULED(".ico-unscheduled"),
    RUNNING(".status-icon-running.icon-sync");

    private String iconCss;

    private DISCScheduleStatus(String iconCss) {
        this.iconCss = iconCss;
    }

    public By getIconByCss() {
        return By.cssSelector(this.iconCss);
    }
}
