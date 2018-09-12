package com.gooddata.qa.graphene.enums;

public enum Connectors {

    PARDOT_BASE("Pardot-Base", "/projectTemplates/PardotAnalytics/3", "pardot"),
    COUPA("Coupa", "/projectTemplates/CoupaAnalytics/8", "coupa");

    private final String name;
    private final String template;
    private final String connectorId;

    Connectors(String name, String template, String connectorId) {
        this.name = name;
        this.template = template;
        this.connectorId = connectorId;
    }

    public String getName() {
        return name;
    }

    public String getTemplate() {
        return template;
    }

    public String getConnectorId() {
        return connectorId;
    }
}
