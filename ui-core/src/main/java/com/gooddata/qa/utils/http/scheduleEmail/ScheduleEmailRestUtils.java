/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http.scheduleEmail;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple control over fast and standard mode of scheduled emails
 * processing in PSS.<br/><br/>
 * <p/>
 * Class calls PUT on /accelerate resource to speed up scheduled
 * emails processing {@link #accelerate()}<br/>
 * Class calls DELETE to /accelerate resource to slow down
 * scheduled emails processing {@link #decelerate()}
 */
public class ScheduleEmailRestUtils {

    private static final String ACCELERATE_URI = "/gdc/internal/projects/%s/scheduledMails/accelerate";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";

    private ScheduleEmailRestUtils() {
    }

    // Speed up scheduled emails processing
    public static void accelerate(RestApiClient restApiClient, String projectId) {
        executeRequest(restApiClient,
                restApiClient.newPutMethod(format(ACCELERATE_URI, projectId), "{\"accelerate\":{}}"));
    }

    // Slow down scheduled emails processing
    public static void decelerate(RestApiClient restApiClient, String projectId) {
        executeRequest(restApiClient,
                restApiClient.newDeleteMethod(format(ACCELERATE_URI, projectId)));
    }

    /**
     * Add executionContext for scheduled mail
     *
     * @param restApiClient
     * @param projectID
     * @param executionContextID
     */
    public static void  addExecutionContext(final RestApiClient restApiClient, final String projectID, int scheduleID,
                                            int executionContextID)throws ParseException, JSONException, IOException {
        JSONObject scheduleEmail = getJsonObject(restApiClient, format(OBJ_LINK, projectID) + scheduleID);
        scheduleEmail.getJSONObject("scheduledMail").getJSONObject("content")
                .getJSONArray("attachments").getJSONObject(0)
                .getJSONObject("dashboardAttachment")
                .put("executionContext", format(OBJ_LINK, projectID) + executionContextID);
        executeRequest(restApiClient,
                restApiClient.newPutMethod(format(OBJ_LINK, projectID) + scheduleID, scheduleEmail.toString()), HttpStatus.OK);
    }
}
