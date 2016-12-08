package com.gooddata.qa.graphene.enums.disc;

public enum __Executable {

    SUCCESSFUL_GRAPH("/graph/successfulGraph.grf"),
    ERROR_GRAPH("/graph/errorGraph.grf"),
    LONG_TIME_RUNNING_GRAPH("/graph/longTimeRunningGraph.grf"),
    SHORT_TIME_ERROR_GRAPH("/graph/shortTimeErrorGraph.grf");

    private String value;

    private __Executable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
