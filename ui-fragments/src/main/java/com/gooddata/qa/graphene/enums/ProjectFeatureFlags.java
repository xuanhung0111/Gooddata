package com.gooddata.qa.graphene.enums;

public enum ProjectFeatureFlags {

    ENABLE_DATA_EXPLORER("enableDataExplorer"),
    HIDE_DASHBOARD_SCHEDULE("hideDashboardSchedule"),
    DASHBOARD_SCHEDULE_RECIPIENTS("dashboardScheduleRecipients"),
    DISPLAY_USER_MANAGEMENT("displayUserManagement"),
    NPS_STATUS("npsStatus"),
    ANALYTICAL_DESIGNER("analyticalDesigner");

    private String featureFlag;

    private ProjectFeatureFlags(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String getFlagName() {
        return this.featureFlag;
    }
}
