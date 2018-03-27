package com.gooddata.qa.graphene.enums.report;

public enum ExportFormat {

    PDF("pdf", "PDF"),
    PDF_PORTRAIT("pdf", "PDF (Portrait)"),
    PDF_LANDSCAPE("pdf", "PDF (Landscape)"),
    IMAGE_PNG("png", "Image (PNG)"),
    CSV("csv", "CSV (formatted)"),
    RAW_CSV("csv", "CSV (raw data)"),
    SCHEDULES_EMAIL_CSV("csv", "CSV"),
    EXCEL_XLSX("xlsx", "XLSX..."),
    SCHEDULES_EMAIL_EXCEL_XLSX("xlsx", "Excel (XLSX)"),
    ALL("all", "Used for schedules...");

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
