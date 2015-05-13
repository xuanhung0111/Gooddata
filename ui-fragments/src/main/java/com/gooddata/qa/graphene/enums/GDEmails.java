package com.gooddata.qa.graphene.enums;

public enum GDEmails {
    FROM_NO_REPLY("no-reply@gooddata.com");

    private String emailAddress;

    private GDEmails(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }
}