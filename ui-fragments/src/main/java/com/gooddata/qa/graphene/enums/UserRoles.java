package com.gooddata.qa.graphene.enums;

public enum UserRoles {
    ADMIN("2"),
    EDITOR("5"),
    VIEWER("4"),
    DASHBOARD_ONLY("3"),
    UNVERIFIED_ADMIN("1");

    private final String id;

    private UserRoles(String id) {
        this.id = id;
    }

    public String getRoleId() {
        return id;
    }

}
