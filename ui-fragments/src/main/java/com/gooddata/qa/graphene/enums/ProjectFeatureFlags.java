package com.gooddata.qa.graphene.enums;

public enum ProjectFeatureFlags {

    ENABLE_DATA_EXPLORER("enableDataExplorer"),
    DASHBOARD_SCHEDULE("dashboardSchedule"),
    DASHBOARD_SCHEDULE_RECIPIENTS("dashboardScheduleRecipients");
    
    private String featureFlag;
    
    private ProjectFeatureFlags(String featureFlag) {
        this.featureFlag = featureFlag;
    }
    
    public String getFlagName() {
        return this.featureFlag;
    }
}
