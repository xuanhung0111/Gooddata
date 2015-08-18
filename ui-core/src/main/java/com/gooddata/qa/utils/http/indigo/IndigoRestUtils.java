package com.gooddata.qa.utils.http.indigo;

import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class IndigoRestUtils {

    private static final String ANALYTICAL_DASHBOARD_BODY;
    private static final String KPI_WIDGET_BODY;

    private static final String AMOUNT_OBJ_ID = "1279";
    private static final String LOST_OBJ_ID = "1283";
    private static final String NUM_OF_ACTIVITIES_OBJ_ID = "14636";
    private static final String DATE_DIM_CREATED_OBJ_ID = "1";

    static {
        try {
            ANALYTICAL_DASHBOARD_BODY = new JSONObject() {{
                put("analyticalDashboard", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "title");
                    }});
                    put("content", new JSONObject() {{
                        put("widgets", new JSONArray());
                        put("filters", new JSONArray());
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            KPI_WIDGET_BODY = new JSONObject() {{
                put("kpi", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                    put("content", new JSONObject() {{
                        put("comparisonType", "none");
                        put("metric", "${metric}");
                        put("dateDimension", "${dateDimension}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    private static List<String> getAnalyticalDashboards(RestApiClient restApiClient, String projectId)
            throws JSONException, IOException {
        String analyticalDashboardsUri = "/gdc/md/" + projectId + "/query/analyticaldashboard";

        HttpRequestBase request = restApiClient.newGetMethod(analyticalDashboardsUri);

        try {
            HttpResponse response = restApiClient.execute(request);
            JSONObject JSONresponse = new JSONObject(EntityUtils.toString(response.getEntity()));

            JSONArray entries = JSONresponse.getJSONObject("query").getJSONArray("entries");
            EntityUtils.consumeQuietly(response.getEntity());

            List<String> dashboardLinks = new ArrayList<>();
            for (int i = 0; i < entries.length(); i++) {
                String dashboardLink = entries.getJSONObject(i).getString("link");
                dashboardLinks.add(dashboardLink);
            }

            return dashboardLinks;
        } finally {
            request.releaseConnection();
        }
    }

    private static String createKpiWidget(RestApiClient restApiClient, String projectId,
            String title, String metricUri, String dateDimensionUri) throws JSONException, IOException {
        String content = KPI_WIDGET_BODY
            .replace("${title}", title)
            .replace("${metric}", metricUri)
            .replace("${dateDimension}", dateDimensionUri);

        HttpRequestBase postRequest = restApiClient.newPostMethod(format(RestUtils.CREATE_AND_GET_OBJ_LINK, projectId),
                content);

        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
            HttpEntity entity = postResponse.getEntity();
            String uri =  new JSONObject(EntityUtils.toString(entity)).getJSONObject("kpi")
                    .getJSONObject("meta").getString("uri");
            EntityUtils.consumeQuietly(entity);
            return uri;
        } finally {
            postRequest.releaseConnection();
        }
    }

    private static String createAnalyticalDashboard(RestApiClient restApiClient, String projectId,
            Collection<String> widgetUris) throws JSONException, IOException {

        // TODO: consider better with .put() and have clever template
        String widgets = new JSONArray(widgetUris).toString();
        String content = ANALYTICAL_DASHBOARD_BODY.replace("\"widgets\":[]", "\"widgets\":"+widgets);

        HttpRequestBase postRequest = restApiClient.newPostMethod(format(RestUtils.CREATE_AND_GET_OBJ_LINK, projectId),
                content);

        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
            HttpEntity entity = postResponse.getEntity();
            String uri =  new JSONObject(EntityUtils.toString(entity)).getJSONObject("analyticalDashboard")
                    .getJSONObject("meta").getString("uri");
            EntityUtils.consumeQuietly(entity);
            return uri;
        } finally {
            postRequest.releaseConnection();
        }

    }

    private static String getObjectUri(String projectId, String objectId) {
        return "/gdc/md/${projectId}/obj/${objectId}".replace("${projectId}", projectId)
                .replace("${objectId}", objectId);
    }


    public static void prepareAnalyticalDashboardTemplate(RestApiClient restApiClient, String projectId) throws JSONException, IOException {
        // delete all dashboards, if some exist
        for (String dashboardLink: getAnalyticalDashboards(restApiClient, projectId)) {
            RestUtils.deleteObject(restApiClient, dashboardLink);
        }

        String amountMetricUri = getObjectUri(projectId, AMOUNT_OBJ_ID);
        String lostMetricUri = getObjectUri(projectId, LOST_OBJ_ID);
        String numOfActivitiesUri = getObjectUri(projectId, NUM_OF_ACTIVITIES_OBJ_ID);
        String dateDimensionUri = getObjectUri(projectId, DATE_DIM_CREATED_OBJ_ID);

        String amountWidget = createKpiWidget(restApiClient, projectId, "Amount", amountMetricUri, dateDimensionUri);
        String lostWidget = createKpiWidget(restApiClient, projectId, "Lost", lostMetricUri, dateDimensionUri);
        String numOfActivitiesWidget = createKpiWidget(restApiClient, projectId, "# of Activities", numOfActivitiesUri, dateDimensionUri);

        List<String> widgetUris = Arrays.asList(amountWidget, lostWidget, numOfActivitiesWidget);
        createAnalyticalDashboard(restApiClient, projectId, widgetUris);
    }

}
