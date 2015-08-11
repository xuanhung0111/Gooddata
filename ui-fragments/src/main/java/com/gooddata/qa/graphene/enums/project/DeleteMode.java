package com.gooddata.qa.graphene.enums.project;

public enum DeleteMode {
    DELETE_ALWAYS,
    DELETE_IF_SUCCESSFUL,
    DELETE_NEVER;

    public static DeleteMode getModeByName(String deleteMode) {
        if (deleteMode != null && deleteMode.length() > 0) {
            for (DeleteMode mode : values()) {
                if (mode.toString().toLowerCase().equals(deleteMode)) return mode;
            }
        }
        return DELETE_IF_SUCCESSFUL;
    }
}
