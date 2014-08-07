package com.gooddata.qa.graphene.enums;

public enum DISCProjectFilters {

	ALL("all", ""),
	FAILED("failed", "No failed data loading processes in any project."),
	RUNNING("running", "No data loading processes are running in any project now."),
	SCHEDULED("scheduled", "No data loading processes are scheduled to run in any project."),
	SUCCESSFUL("successful", ""),
	UNSCHEDULED("unscheduled", ""),
	DISABLED("disabled", "");
	
	private String option;
	private String emptyStateMessage;
	
	private DISCProjectFilters(String option, String emptyStateMessage) {
		this.option = option;
		this.emptyStateMessage = emptyStateMessage;
	}
	
	public String getOption() {
		return this.option;
	}
	
	public String getEmptyStateMessage () {
		return this.emptyStateMessage;
	}
}
