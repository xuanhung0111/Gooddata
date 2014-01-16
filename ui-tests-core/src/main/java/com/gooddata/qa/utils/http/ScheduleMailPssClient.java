/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

/**
 * Simple control over fast and standard mode of scheduled emails
 * processing in PSS.<br/><br/>
 *
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
		String uri = String.format(ACCELERATE_URI, projectId);
		HttpRequestBase request = restApiClient.newPutMethod(uri, "{\"accelerate\":{}}");
		HttpResponse response = restApiClient.execute(request);
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public void decelerate() {
		String uri = String.format(ACCELERATE_URI, projectId);
		HttpRequestBase request = restApiClient.newDeleteMethod(uri);
		HttpResponse response = restApiClient.execute(request);
		EntityUtils.consumeQuietly(response.getEntity());
	}
}
