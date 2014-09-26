package com.gooddata.qa.graphene.enums;

public enum DISCOverviewProjectStates {

	ALL("all", ""),
	FAILED("failed", "No failed data loading processes. Good job!"),
	RUNNING("running", "No data loading processes are running right now."),
	SCHEDULED("scheduled", "No data loading processes are scheduled to run."),
	SUCCESSFUL("successful", "No data loading processes have successfully finished."),
	STOPPED("stopped", "");
	
	private String option;
	private String overviewEmptyState;
	
	private DISCOverviewProjectStates(String option, String overviewEmptyState) {
		this.option = option;
		this.overviewEmptyState = overviewEmptyState;
	}
	
	public String getOption() {
		return this.option;
	}
	
	public String getOverviewEmptyState() {
		return this.overviewEmptyState;
	}
}
