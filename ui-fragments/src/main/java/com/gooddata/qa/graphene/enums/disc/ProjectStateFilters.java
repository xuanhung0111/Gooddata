package com.gooddata.qa.graphene.enums.disc;

public enum ProjectStateFilters {

    ALL("all", "No projects matching \"${searchKey}\""),
    FAILED("failed", "No failed data loading processes in any project.", "No failed projects matching \"${searchKey}\""),
    RUNNING("running", "No data loading processes are running in any project now.", "No running projects matching \"${searchKey}\""),
    SCHEDULED("scheduled", "No data loading processes are scheduled to run in any project.", "No scheduled projects matching \"${searchKey}\""),
    SUCCESSFUL("successful", "No successful projects matching \"${searchKey}\""),
    UNSCHEDULED("unscheduled", "No unscheduled projects matching \"${searchKey}\""),
    DISABLED("disabled", "No disabled projects matching \"${searchKey}\"");

    private String option;
    private String emptyStateMessage;
    private String emptySearchResultMessage;

    private ProjectStateFilters(String option, String emptyStateMessage, String emptySearchResultMessage) {
        this.option = option;
        this.emptyStateMessage = emptyStateMessage;
        this.emptySearchResultMessage = emptySearchResultMessage;
    }
    
    private ProjectStateFilters(String option, String emptySearchResultMessage) {
        this(option, "", emptySearchResultMessage);
    }

    public String getOption() {
        return this.option;
    }

    public String getEmptyStateMessage() {
        return this.emptyStateMessage;
    }
    
    public String getEmptySearchResultMessage() {
        return this.emptySearchResultMessage;
    }
}
