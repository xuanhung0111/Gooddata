/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http.scheduleEmail;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static java.lang.String.format;

import com.gooddata.qa.utils.http.RestApiClient;

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
}
