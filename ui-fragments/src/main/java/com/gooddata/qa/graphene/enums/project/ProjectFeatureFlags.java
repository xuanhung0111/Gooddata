package com.gooddata.qa.graphene.enums.project;

public enum ProjectFeatureFlags {

    ENABLE_DATA_EXPLORER("enableDataExplorer"),
    HIDE_DASHBOARD_SCHEDULE("hideDashboardSchedule"),
    DASHBOARD_SCHEDULE_RECIPIENTS("dashboardScheduleRecipients"),
    DISPLAY_USER_MANAGEMENT("displayUserManagement"),
    NPS_STATUS("npsStatus"),
    ANALYTICAL_DESIGNER("analyticalDesigner"),
    ENABLE_CSV_UPLOADER("enableCsvUploader"),
    ENABLE_ATTRIBUTE_FILTERS("enableAttributeFilters"),
    ENABLE_ANALYTICAL_DASHBOARDS("enableAnalyticalDashboards"),
    ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS("enableAnalyticalDashboardsVisualizations"),
    DISABLE_SAVED_FILTERS("disableSavedFilters"),
    ENABLE_CHANGE_LANGUAGE("enableChangeLanguage");

    private final String featureFlag;

    private ProjectFeatureFlags(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String getFlagName() {
        return this.featureFlag;
    }
}
