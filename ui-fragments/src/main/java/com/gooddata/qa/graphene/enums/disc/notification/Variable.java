package com.gooddata.qa.graphene.enums.disc.notification;

public enum Variable {

    PROJECT("params.PROJECT", "${params.PROJECT}"),
    USER("params.USER", "${params.USER}"),
    USER_EMAIL("params.USER_EMAIL", "${params.USER_EMAIL}"),
    PROCESS_URI("params.PROCESS_URI", "${params.PROCESS_URI}"),
    PROCESS_ID("params.PROCESS_ID", "${params.PROCESS_ID}"),
    PROCESS_NAME("params.PROCESS_NAME", "${params.PROCESS_NAME}"),
    EXECUTABLE("params.EXECUTABLE", "${params.EXECUTABLE}"),
    SCHEDULE_ID("params.SCHEDULE_ID", "${params.SCHEDULE_ID}"),
    SCHEDULE_NAME("params.SCHEDULE_NAME", "${params.SCHEDULE_NAME}"),
    SCHEDULE_TIME("params.SCHEDULED_TIME", "${params.SCHEDULED_TIME}"),
    LOG("params.LOG", "${params.LOG}"),
    START_TIME("params.START_TIME", "${params.START_TIME}"),
    FINISH_TIME("params.FINISH_TIME", "${params.FINISH_TIME}"),
    ERROR_MESSAGE("params.ERROR_MESSAGE", "${params.ERROR_MESSAGE}"),
    DATASETS("params.DATASETS", "${params.DATASETS}"),
    CONSECUTIVE_FAILURES("params.CONSECUTIVE_FAILURES", "${params.CONSECUTIVE_FAILURES}");

    private String name;
    private String value;

    private Variable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
