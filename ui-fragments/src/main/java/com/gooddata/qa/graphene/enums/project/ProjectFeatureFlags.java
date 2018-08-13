package com.gooddata.qa.graphene.enums.project;

public enum ProjectFeatureFlags {

    HIDE_DASHBOARD_SCHEDULE("hideDashboardSchedule"),
    DASHBOARD_SCHEDULE_RECIPIENTS("dashboardScheduleRecipients"),
    DISPLAY_USER_MANAGEMENT("displayUserManagement"),
    NPS_STATUS("npsStatus"),
    ANALYTICAL_DESIGNER("analyticalDesigner"),
    ENABLE_CSV_UPLOADER("enableCsvUploader"),
    ENABLE_ANALYTICAL_DASHBOARDS("enableAnalyticalDashboards"),
    DISABLE_SAVED_FILTERS("disableSavedFilters"),
    ENABLE_CHANGE_LANGUAGE("enableChangeLanguage"),
    HIDE_KPI_ALERT_LINK("hideKPIAlertLinks"),
    FISCAL_CALENDAR_ENABLED("fiscalCalendarEnabled"),
    USE_AVAILABLE_ENABLED("useAvailableEnabled"),
    ENABLE_ETL_COMPONENT("enableEtlComponent"),
    CONTROL_EXECUTION_CONTEXT_ENABLED("controlExecutionContextEnabled"),
    EXPORT_TO_XLSX_ENABLED("exportToXLSXEnabled"),
    CELL_MERGED_BY_DEFAULT("cellMergedByDefault"),
    ACTIVE_FILTERS_BY_DEFAULT("activeFiltersByDefault"),
    REPORT_HEADER_PAGING_ENABLED("reportHeaderPagingEnabled");

    private final String featureFlag;

    private ProjectFeatureFlags(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String getFlagName() {
        return this.featureFlag;
    }
}
