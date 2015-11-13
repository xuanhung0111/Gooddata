package com.gooddata.qa.graphene.enums;

public class GDEmails {
    public static GDEmails NO_REPLY = new GDEmails("no-reply@gooddata.com", 3);
    public static GDEmails NOREPLY = new GDEmails("noreply@gooddata.com", 5);
    public static GDEmails INVITATION = new GDEmails("invitation@gooddata.com", 10);
    public static GDEmails REGISTRATION = new GDEmails("registration@gooddata.com", 10);

    private String emailAddress;
    private int maxWaitingTimeInMinute;

    public GDEmails(String emailAddress, int maxWaitingTimeInMinute) {
        this.emailAddress = emailAddress;
        this.maxWaitingTimeInMinute = maxWaitingTimeInMinute;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public int getMaxWaitingTimeInMinute() {
        return this.maxWaitingTimeInMinute;
    }
}
