package com.gooddata.qa.graphene.enums;

public enum DISCProcessTypes {

	DEFAULT("CloudConnect", "graph"),
	GRAPH("CloudConnect", "graph"),
	RUBY("Ruby scripts", "script");
	
	private String processTypeOption;
	private String processTypeExectuable;
	
	private DISCProcessTypes (String processTypeOption, String processTypeExectuable) {
		this.processTypeOption = processTypeOption;
		this.processTypeExectuable = processTypeExectuable;
	}
	
	public String getProcessTypeOption() {
		return processTypeOption;
	}
	
	public String getProcessTypeExecutable() {
		return processTypeExectuable;
	}
}
