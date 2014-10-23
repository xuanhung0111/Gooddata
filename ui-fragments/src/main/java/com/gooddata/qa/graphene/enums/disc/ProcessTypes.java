package com.gooddata.qa.graphene.enums.disc;

public enum ProcessTypes {

    DEFAULT("CloudConnect", "graph"),
    GRAPH("CloudConnect", "graph"),
    RUBY("Ruby scripts", "script");

    private String processTypeOption;
    private String processTypeExecutable;

    private ProcessTypes(String processTypeOption, String processTypeExecutable) {
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
