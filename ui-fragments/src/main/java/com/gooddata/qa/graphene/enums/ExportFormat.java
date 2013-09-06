package com.gooddata.qa.graphene.enums;

public enum ExportFormat {
	
	PDF ("pdf", "To PDF"),
	IMAGE_PNG ("png", "To image (PNG)"),
	CSV ("csv", "To Excel XLS"),
	EXCEL_XLS ("xls", "To CSV");
	
	private final String name;
	private final String label;
	
	private ExportFormat(String name, String label) {
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
