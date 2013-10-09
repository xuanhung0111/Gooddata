package com.gooddata.qa.graphene.enums;

public enum ExportFormat {
	
	PDF ("pdf", "PDF"),
	IMAGE_PNG ("png", "Image (PNG)"),
	CSV ("csv", "Excel (XLS)"),
	EXCEL_XLS ("xls", "CSV"),
	ALL ("all", "Used for schedules...");
	
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
