package com.gooddata.qa.graphene.enums.disc;

public enum __Executable {

    SUCCESSFUL_GRAPH("Basic/graph/successfulGraph.grf"),
    ERROR_GRAPH("Basic/graph/errorGraph.grf"),
    LONG_TIME_RUNNING_GRAPH("Basic/graph/longTimeRunningGraph.grf"),
    SHORT_TIME_ERROR_GRAPH("Basic/graph/shortTimeErrorGraph.grf");

    private String value;

    private __Executable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
