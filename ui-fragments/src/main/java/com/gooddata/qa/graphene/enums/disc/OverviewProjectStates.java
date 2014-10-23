package com.gooddata.qa.graphene.enums.disc;

import org.openqa.selenium.By;

public enum OverviewProjectStates {

    ALL("all"),
    FAILED("failed", "No failed data loading processes. Good job!", ".ait-overview-field-failed"),
    RUNNING("running", "No data loading processes are running right now.", ".ait-overview-field-running"),
    SCHEDULED("scheduled", "No data loading processes are scheduled to run.", ".ait-overview-field-scheduled"),
    SUCCESSFUL("successful", "No data loading processes have successfully finished.", ".ait-overview-field-successful"),
    STOPPED("stopped");

    private String option;
    private String overviewEmptyState;
    private String overviewFieldCssSelector;

    private OverviewProjectStates(String option, String overviewEmptyState,
            String overviewFieldCssSelector) {
        this.option = option;
        this.overviewEmptyState = overviewEmptyState;
        this.overviewFieldCssSelector = overviewFieldCssSelector;
    }

    private OverviewProjectStates(String option, String overviewEmptyState) {
        this(option, overviewEmptyState, "");
    }

    private OverviewProjectStates(String option) {
        this(option, "", "");
    }

    public String getOption() {
        return this.option;
    }

    public String getOverviewEmptyState() {
        return this.overviewEmptyState;
    }

    public By getOverviewFieldBy() {
        if (this == ALL || this == STOPPED)
            throw new UnsupportedOperationException();
        return By.cssSelector(overviewFieldCssSelector);
    }
}
