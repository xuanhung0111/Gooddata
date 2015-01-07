package com.gooddata.qa.graphene.entity.dlui;

public class ProcessInfo {

    private String projectId;
    private String processName;
    private String processType;
    private String processId;

    public String getProjectId() {
        return projectId;
    }

    public ProcessInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getProcessName() {
        return processName;
    }

    public ProcessInfo setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    public String getProcessType() {
        return processType;
    }

    public ProcessInfo setProcessType(String processType) {
        this.processType = processType;
        return this;
    }

    public String getProcessId() {
        return processId;
    }

    public ProcessInfo setProcessId(String processId) {
        this.processId = processId;
        return this;
    }
}
