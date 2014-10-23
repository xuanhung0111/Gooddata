package com.gooddata.qa.graphene.enums.disc;

public enum NotificationEvents {

    SUCCESS("success"),
    FAILURE("failure"),
    PROCESS_STARTED("process started"),
    PROCESS_SCHEDULED("process scheduled"),
    CUSTOM_EVENT("custom event");

    private String eventOption;

    private NotificationEvents(String eventOption) {
        this.eventOption = eventOption;
    }

    public String getEventOption() {
        return eventOption;
    }
}
