package com.gooddata.qa.graphene.entity.disc;

import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;

public class ExecutionDetails {

    private ScheduleStatus status;
    private String errorMessage;
    private String startTime;
    private String endTime;
    private String scheduledTime;
    private String scheduleLogLink;

    public ExecutionDetails() {
        this.status = ScheduleStatus.OK;
        this.errorMessage = "";
        this.startTime = "";
        this.endTime = "";
        this.scheduledTime = "";
        this.scheduleLogLink = null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ExecutionDetails setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public ExecutionDetails setStatus(ScheduleStatus status) {
        this.status = status;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public ExecutionDetails setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public ExecutionDetails setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public ExecutionDetails setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
        return this;
    }

    public String getScheduleLogLink() {
        return scheduleLogLink;
    }

    public ExecutionDetails setScheduleLogLink(String scheduleLogLink) {
        this.scheduleLogLink = scheduleLogLink;
        return this;
    }
}
