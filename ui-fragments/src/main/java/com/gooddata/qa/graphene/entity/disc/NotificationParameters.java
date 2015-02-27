package com.gooddata.qa.graphene.entity.disc;

import java.util.HashMap;
import java.util.Map;

public class NotificationParameters {

    private Map<String, String> availableParameters;

    public NotificationParameters() {
        availableParameters = new HashMap<String, String>();
    }

    public String getParamValue(String paramName) {
        if (availableParameters.containsKey(paramName))
            return availableParameters.get(paramName);
        else
            throw new UnsupportedOperationException("The parameter " + paramName
                    + " is not available!");
    }

    public NotificationParameters setProjectId(String projectId) {
        availableParameters.put("params.PROJECT", projectId);
        return this;
    }

    public NotificationParameters setUser(String user) {
        availableParameters.put("params.USER", user);
        return this;
    }

    public NotificationParameters setUserEmail(String userEmail) {
        availableParameters.put("params.USER_EMAIL", userEmail);
        return this;
    }

    public NotificationParameters setProcessUri(String processUri) {
        availableParameters.put("params.PROCESS_URI", processUri);
        availableParameters.put("params.PROCESS_ID",
                processUri.substring(processUri.lastIndexOf("/") + 1));
        return this;
    }

    public NotificationParameters setProcessName(String processName) {
        availableParameters.put("params.PROCESS_NAME", processName);
        return this;
    }

    public NotificationParameters setExecutable(String executable) {
        availableParameters.put("params.EXECUTABLE", executable);
        return this;
    }

    public NotificationParameters setScheduleId(String scheduleId) {
        availableParameters.put("params.SCHEDULE_ID", scheduleId);
        return this;
    }

    public NotificationParameters setScheduleName(String scheduleName) {
        availableParameters.put("params.SCHEDULE_NAME", scheduleName);
        return this;
    }

    public NotificationParameters setExecutionDetails(ExecutionDetails executionDetails) {
        if (executionDetails == null)
            executionDetails = new ExecutionDetails();
        availableParameters.put("params.LOG", executionDetails.getScheduleLogLink());
        availableParameters.put("params.SCHEDULED_TIME", executionDetails.getScheduledTime());
        availableParameters.put("params.START_TIME", executionDetails.getStartTime());
        availableParameters.put("params.FINISH_TIME", executionDetails.getEndTime());
        availableParameters.put("params.ERROR_MESSAGE", executionDetails.getErrorMessage());
        return this;
    }

    public NotificationParameters setCustomParam(String customParam) {
        availableParameters.put("params.CUSTOM", customParam);
        return this;
    }
}
