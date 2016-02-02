/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static java.lang.String.format;

/**
 * Simple control over fast and standard mode of scheduled emails
 * processing in PSS.<br/><br/>
 * <p/>
 * Class calls PUT on /accelerate resource to speed up scheduled
 * emails processing {@link #accelerate()}<br/>
 * Class calls DELETE to /accelerate resource to slow down
 * scheduled emails processing {@link #decelerate()}
 */
public class ScheduleMailPssClient {

    private static final String ACCELERATE_URI = "/gdc/internal/projects/%s/scheduledMails/accelerate";

    private final RestApiClient restApiClient;
    private final String projectId;

    public ScheduleMailPssClient(RestApiClient restApiClient, String projectId) {
        this.restApiClient = restApiClient;
        this.projectId = projectId;
    }

    public void accelerate() {
        executeRequest(restApiClient,
                restApiClient.newPutMethod(format(ACCELERATE_URI, projectId), "{\"accelerate\":{}}"));
    }

    public void decelerate() {
        executeRequest(restApiClient,
                restApiClient.newDeleteMethod(format(ACCELERATE_URI, projectId)));
    }
}
