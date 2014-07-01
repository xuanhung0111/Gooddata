package com.gooddata.qa.graphene.enums;

public enum DISCProcessTypes {

	DEFAULT("CloudConnect", "graph"),
	GRAPH("CloudConnect", "graph"),
	RUBY("Ruby scripts", "script");
	
	private String processTypeOption;
	private String processTypeExecutable;
	
	private DISCProcessTypes (String processTypeOption, String processTypeExecutable) {
		this.processTypeOption = processTypeOption;
		this.processTypeExecutable = processTypeExecutable;
	}
	
	public String getProcessTypeOption() {
		return processTypeOption;
	}
	
	public String getProcessTypeExecutable() {
		return processTypeExecutable;
	}
}
