package com.gooddata.qa.graphene.enums;

public enum UserRoles {

    ADMIN("2", "Admin"),
    EDITOR("5", "Editor"),
    VIEWER("4", "Viewer"),
    DASHBOARD_ONLY("3", "Embedded Dashboard Only"),
    UNVERIFIED_ADMIN("1");

    private final String id;
    private final String text;

    private UserRoles(String id, String text) {
        this.id = id;
        this.text = text;
    }

    private UserRoles(String id) {
        this(id, "");
    }

    public String getRoleId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
