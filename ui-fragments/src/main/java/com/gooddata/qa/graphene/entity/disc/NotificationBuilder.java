package com.gooddata.qa.graphene.entity.disc;

import com.gooddata.qa.graphene.enums.disc.NotificationEvents;

public class NotificationBuilder {

    private String processName;
    private String email;
    private String subject;
    private String message;
    private NotificationEvents event;
    private String customEventName;
    private int index;
    private boolean isSaved;

    public NotificationBuilder() {
        processName = null;
        email = null;
        subject = null;
        message = null;
        event = NotificationEvents.SUCCESS;
        customEventName = null;
        index = 0;
        this.isSaved = true;
    }

    public String getEmail() {
        return email;
    }

    public NotificationBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public NotificationBuilder setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public NotificationBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public NotificationEvents getEvent() {
        return event;
    }

    public NotificationBuilder setEvent(NotificationEvents event) {
        this.event = event;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public NotificationBuilder setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getProcessName() {
        return processName;
    }

    public NotificationBuilder setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    public String getCustomEventName() {
        return customEventName;
    }

    public NotificationBuilder setCustomEventName(String customEventName) {
        this.customEventName = customEventName;
        return this;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public NotificationBuilder setSaved(boolean isSaved) {
        this.isSaved = isSaved;
        return this;
    }
}
