package com.gooddata.qa.graphene.entity.disc;

public class ExecutionTask {

    private int statusCode;
    private String detailLink;
    private String error;

    public ExecutionTask() {}

    public ExecutionTask(int statusCode, String detailLink, String error) {
        this.statusCode = statusCode;
        this.detailLink = detailLink;
        this.error = error;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public int setStatusCode(int statusCode) {
        return this.statusCode = statusCode;
    }

    public String getDetailLink() {
        return this.detailLink;
    }

    public String setDetailLink(String detailLink) {
        return this.detailLink = detailLink;
    }

    public String getError() {
        return this.error;
    }

    public String setError(String error) {
        return this.error = error;
    }
}
