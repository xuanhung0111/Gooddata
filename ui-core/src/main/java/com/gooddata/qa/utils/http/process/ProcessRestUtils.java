package com.gooddata.qa.utils.http.process;

import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.utils.http.RestApiClient;

public final class ProcessRestUtils {

    private static final Logger log = Logger.getLogger(ProcessRestUtils.class.getName());

    private ProcessRestUtils() {
    }

    public static String getDataloadProcessOwner(final RestApiClient restApiClient, final String projectId)
            throws IOException, JSONException {
        final String processesUri = format("/gdc/projects/%s/dataload/processes", projectId);
        final JSONArray processes = getJsonObject(restApiClient, processesUri, HttpStatus.OK)
                .getJSONObject("processes").getJSONArray("items");

        JSONObject it;
        for (int i = 0, n = processes.length(); i < n; i++) {
            it = processes.getJSONObject(i).getJSONObject("process");
            if (!"DATALOAD".equals(it.getString("type"))) continue;
            return it.getString("ownerLogin");
        }
        throw new RuntimeException("Cannot find dataload process!");
    }

    public static String executeProcess(final RestApiClient restApiClient, final String executionUri,
            final String executable, final Map<String, String> params)
                    throws ParseException, JSONException, IOException {
        final String pollingUri = createProcessExecution(HttpStatus.CREATED, restApiClient, executionUri, executable, params);
        log.info("Execution polling uri: " + pollingUri);
        return pollingUri;
    }

    public static String createProcessExecution(final HttpStatus expectedStatusCode, final RestApiClient restApiClient,
            final String executionUri, final String executable, final Map<String, String> params)
                    throws JSONException, ParseException, IOException {
        final String postBody = new JSONObject().put("execution", new JSONObject() {{
            put("executable", executable);
            put("params", params);
        }}).toString();

        final JSONObject responseObject = getJsonObject(restApiClient,
                restApiClient.newPostMethod(executionUri, postBody),
                expectedStatusCode);

        if (expectedStatusCode != HttpStatus.CREATED)
            return responseObject.getJSONObject("error")
                    .getString("message");

        return responseObject.getJSONObject("executionTask")
                .getJSONObject("links")
                .getString("poll");
    }
}
