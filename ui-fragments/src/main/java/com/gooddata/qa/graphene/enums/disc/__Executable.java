package com.gooddata.qa.graphene.enums.disc;

public enum __Executable {

    SUCCESSFUL_GRAPH("/graph/successfulGraph.grf"),
    ERROR_GRAPH("/graph/errorGraph.grf"),
    LONG_TIME_RUNNING_GRAPH("/graph/longTimeRunningGraph.grf"),
    SHORT_TIME_ERROR_GRAPH("/graph/shortTimeErrorGraph.grf"),
    CTL_GRAPH("/graph/CTL_Function.grf");

    private String path;

    private __Executable(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
