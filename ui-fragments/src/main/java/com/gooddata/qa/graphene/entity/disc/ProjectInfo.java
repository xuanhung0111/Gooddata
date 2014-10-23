package com.gooddata.qa.graphene.entity.disc;

public class ProjectInfo {

    private String projectName;
    private String projectId;

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
}
