package com.gooddata.qa.graphene.enums;

public enum ProjectEnvironment {

    PRODUCTION, DEVELOPMENT, TESTING;

    public static ProjectEnvironment getEnvironmentByName(String enviroment) {
        if (enviroment != null && enviroment.length() > 0) {
            for (ProjectEnvironment env : values()) {
                if (env.toString().toLowerCase().equals(enviroment)) return env;
            }
        }
        return TESTING;
    }
}
