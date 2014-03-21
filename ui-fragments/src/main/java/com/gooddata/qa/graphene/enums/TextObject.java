package com.gooddata.qa.graphene.enums;

public enum TextObject {
	HEADLINE ("Headline", "yui3-c-textdashboardwidget-middleText"),
	SUB_HEADLINE ("Sub-Headline", "yui3-c-textdashboardwidget-smallText"),
	DESCRIPTION ("Description", "yui3-c-textdashboardwidget-bigText");
	
	private final String name;
	private final String label;
	
	private TextObject(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }
    
    public String getLabel() {
        return label;
    }

}
