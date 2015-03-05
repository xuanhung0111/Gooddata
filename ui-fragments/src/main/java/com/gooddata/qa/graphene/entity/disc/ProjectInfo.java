package com.gooddata.qa.graphene.entity.disc;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ProjectInfo {

    private String projectName;
    private String projectId;
    
    public ProjectInfo() {}

    public ProjectInfo(String projectName, String projectId) {
        this.projectName = projectName;
        this.projectId = projectId;
    }

    public ProjectInfo setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public ProjectInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("projectId", projectId)
                .append("projectName", projectName)
                .toString();
    }
}
