package com.gooddata.qa.graphene.enums.user;

public enum UserRoles {

    ADMIN("2", "Admin"),
    EDITOR("5", "Editor"),
    EDITOR_AND_INVITATIONS("10", "EditorAndInvitations"),
    EDITOR_AND_USER_ADMIN("11", "EditorAndUserAdmin"),
    EXPLORER("8", "Explorer"),
    EXPLORER_EMBEDDED("9", "ExplorerEmbedded"),
    VIEWER("4", "Viewer"),
    VIEWER_DISABLED_EXPORT("12", "ViewerDisabledExport"),
    DASHBOARD_ONLY("3", "DashboardOnly"),
    UNVERIFIED_ADMIN("1");

    private final String id;
    private final String name;

    private UserRoles(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private UserRoles(String id) {
        this(id, "");
    }

    public String getRoleId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
