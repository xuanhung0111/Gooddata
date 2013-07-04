package com.gooddata.qa.graphene.enums;

public enum Connectors {
	
	PARDOT_BASE ("Pardot Base", "/projectTemplates/PardotAnalytics/3", "pardot"),
	PARDOT_PREMIUM ("Pardot Premium", "/projectTemplates/PardotAnalytics/6", "pardot"),
	ZENDESK3 ("Zendesk3", "/projectTemplates/ZendeskAnalytics/9", "zendesk3"),
	GOOGLE_ANALYTICS ("Google Analytics", "/projectTemplates/GoogleAnalytics/3", "googleanalytics"),
	COUPA ("Coupa", "/projectTemplates/CoupaAnalytics/8", "coupa"),
	BRIGHTIDEA ("Brightidea", "/projectTemplates/BrightideaAnalytics/2", "brightidea");
	
	private final String name;
	private final String template;
	private final String connectorId;

    private Connectors(String name, String template, String connectorId) {
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
