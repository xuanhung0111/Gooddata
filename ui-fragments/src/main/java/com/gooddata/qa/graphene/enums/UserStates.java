package com.gooddata.qa.graphene.enums;

public enum UserStates {

    ACTIVE("users-filter-active"),
    DEACTIVATED("users-filter-inactive"),
    INVITED("users-filter-pending");

    private String className;

    private UserStates(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}