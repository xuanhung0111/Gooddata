package com.gooddata.qa.graphene.enums;

public enum UserStates {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING("Pending");

    private String text;

    private UserStates(String text) {
        this.text = text + " users";
    }

    public String getText() {
        return text;
    }
}