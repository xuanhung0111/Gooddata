package com.gooddata.qa.graphene.enums;

public enum DISCNotificationEvents {

    SUCCESS("success"),
    FAILURE("failure"),
    PROCESS_STARTED("process started"),
    PROCESS_SCHEDULED("process scheduled"),
    CUSTOM_EVENT("custom event");

    private String eventOption;

    private DISCNotificationEvents(String eventOption) {
        this.eventOption = eventOption;
    }

    public String getEventOption() {
        return eventOption;
    }
}
