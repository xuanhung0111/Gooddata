package com.gooddata.qa.graphene.entity.disc;

import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;

public class NotificationRule {

    private String email;
    private NotificationEvent event;
    private String customEventName;
    private String subject;
    private String message;

    public NotificationRule withEmail(String email) {
        this.email = email;
        return this;
    }

    public NotificationRule withEvent(NotificationEvent event) {
        this.event = event;
        return this;
    }

    public NotificationRule withCustomEventName(String customEventName) {
        this.customEventName = customEventName;
        return this;
    }

    public NotificationRule withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public NotificationRule withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public NotificationEvent getEvent() {
        return event;
    }

    public String getCustomEventName() {
        return customEventName;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}
