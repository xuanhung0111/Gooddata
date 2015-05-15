package com.gooddata.qa.graphene.entity.disc;

public class ExecutionTask {

    private int statusCode;
    private String detailLink;
    private String pollLink;
    private String error;

    public ExecutionTask() {}

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDetailLink() {
        return this.detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getPollLink() {
        return this.pollLink;
    }

    public void setPollLink(String pollLink) {
        this.pollLink = pollLink;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
