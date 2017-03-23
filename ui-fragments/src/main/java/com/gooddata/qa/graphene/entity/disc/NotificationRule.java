package com.gooddata.qa.graphene.entity.disc;

import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.gooddata.qa.graphene.enums.disc.notification.Variable;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;

public class NotificationRule {

    private String email;
    private NotificationEvent event;
    private String customEventName;
    private String subject;
    private String message;

    public static String buildMessage(Variable... variables) {
        return Stream.of(variables)
                .map(v -> v.getName() + "==" + v.getValue())
                .collect(joining(" | "));
    }

    public static Map<String, String> getVariablesFromMessage(String message) {
        if (!Stream.of(Variable.values()).anyMatch(v -> message.contains(v.getName()))) {
            throw new RuntimeException("This message: " + message + " contains no variable!");
        }

        Map<String, String> variables = new HashMap<>();
        Stream.of(message.split(" \\| "))
                .map(v -> v.split("=="))
                .forEach(v -> variables.put(v[0], v.length == 2 ? v[1] : ""));

        return variables;
    }

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
