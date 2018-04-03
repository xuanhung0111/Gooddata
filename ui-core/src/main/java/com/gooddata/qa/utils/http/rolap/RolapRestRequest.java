package com.gooddata.qa.utils.http.rolap;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.http.RestRequest.initPostRequest;
import static java.lang.String.format;

public class RolapRestRequest extends CommonRestRequest {

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
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    public RolapRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Post etl pull integration
     *
     * @param integrationEntry
     * @return is the integration successful or not
     */
    public boolean postEtlPullIntegration(String integrationEntry) throws JSONException, ParseException, IOException {
        String content = new JSONObject().put("pullIntegration", integrationEntry).toString();
        String pullingUri = getJsonObject(initPostRequest(format(PULL_DATA_LINK, projectId), content), HttpStatus.CREATED)
                .getJSONObject("pull2Task").getJSONObject("links").getString("poll");

        while (executeRequest(RestRequest.initGetRequest(pullingUri)) == HttpStatus.ACCEPTED.value()) {
            sleepTightInSeconds(5);
        }
        return "OK".equals(getJsonObject(pullingUri).getJSONObject("wTaskStatus").getString("status"));
    }

    /**
     * Execute MAQL
     *
     * @param maql
     * @return polling uri
     */
    public String executeMAQL(String maql) throws ParseException, JSONException, IOException {
        return getJsonObject(
                initPostRequest(String.format(LDM_MANAGE_LINK, projectId),
                        MAQL_EXECUTION_BODY.get().replace("${maql}", maql)))
                .getJSONArray("entries")
                .getJSONObject(0)
                .getString("link");
    }
}
