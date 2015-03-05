package com.gooddata.qa.graphene.entity.dlui;

public class ADSInstance {

    private String name;
    private String description = "";
    private String authorizationToken;
    private String id;

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public ADSInstance setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ADSInstance setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public ADSInstance setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public ADSInstance setId(String id) {
        this.id = id;
        return this;
    }
}
