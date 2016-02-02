package com.gooddata.qa.utils.http.rolap;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.utils.http.RestApiClient;

public final class RolapRestUtils {

    private static final Logger log = Logger.getLogger(RolapRestUtils.class.getName());

    private RolapRestUtils() {
    }

    private static final String LDM_MANAGE_LINK = "/gdc/md/%s/ldm/manage2";
    private static final String PULL_DATA_LINK = "/gdc/md/%s/etl/pull2";

    private static final Supplier<String> MAQL_EXECUTION_BODY = () -> {
        try {
            return new JSONObject() {{
                put("manage", new JSONObject() {{
                    put("maql", "${maql}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exeception during json object initialization! ", e);
        }
    };

    public static int waitingForAsyncTask(final RestApiClient restApiClient, final String pollingUri) {
        final Supplier<HttpRequestBase> request = () -> restApiClient.newGetMethod(pollingUri);
        while (executeRequest(restApiClient, request.get()) == HttpStatus.ACCEPTED.value()) {
            log.info("Async task is running...");
            sleepTightInSeconds(2);
        }
        return executeRequest(restApiClient, request.get());
    }

    public static String getAsyncTaskStatus(final RestApiClient restApiClient, final String pollingUri)
            throws IOException, JSONException {
        final JSONObject taskObject = getJsonObject(restApiClient, pollingUri);

        String key = "";
        if (!taskObject.isNull("wTaskStatus")) key = "wTaskStatus";
        else if (!taskObject.isNull("taskState")) key = "taskState";
        else throw new IllegalStateException("The status object is not existing! The current response is: "
                + taskObject.toString());

        final String status = taskObject.getJSONObject(key).getString("status");
        log.info("Async task status is: " + status);
        return status;
    }

    public static boolean postEtlPullIntegration(final RestApiClient restApiClient, final String projectId,
            final String integrationEntry) throws JSONException, ParseException, IOException {
        final String content = new JSONObject().put("pullIntegration", integrationEntry).toString();
        final String pullingUri = getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(PULL_DATA_LINK, projectId), content),
                HttpStatus.CREATED)
                    .getJSONObject("pull2Task")
                    .getJSONObject("links")
                    .getString("poll");

        while (executeRequest(restApiClient, restApiClient.newGetMethod(pullingUri)) == HttpStatus.ACCEPTED.value()) {
            sleepTightInSeconds(5);
        }
        return "OK".equals(getJsonObject(restApiClient, pullingUri).getJSONObject("wTaskStatus").getString("status"));
    }

    public static String executeMAQL(final RestApiClient restApiClient, final String projectId, final String maql)
            throws ParseException, JSONException, IOException {
        final String contentBody = MAQL_EXECUTION_BODY.get().replace("${maql}", maql);
        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(String.format(LDM_MANAGE_LINK, projectId), contentBody))
                    .getJSONArray("entries")
                    .getJSONObject(0)
                    .getString("link");
    }
}
