package com.gooddata.qa.utils.http.indigo;

import static java.lang.String.format;

import java.io.IOException;
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

import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;

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
                        put("comparisonType", "${comparisonType}");
                        put("metric", "${metric}");
                        put("dateDimension", "${dateDimension}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    public static List<String> getAnalyticalDashboards(RestApiClient restApiClient, String projectId)
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

    public static String createKpiWidget(RestApiClient restApiClient, String projectId, KpiMDConfiguration kpiConfig)
            throws JSONException, IOException {
        String content = KPI_WIDGET_BODY
                .replace("${title}", kpiConfig.getTitle())
                .replace("${metric}", kpiConfig.getMetric())
                .replace("${dateDimension}", kpiConfig.getDateDimension())
                .replace("${comparisonType}", kpiConfig.getComparisonType().getJsonKey());

        if (kpiConfig.hasComparison()) {
            JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("comparisonDirection", kpiConfig.getComparisonDirection().toString());

            content = contentJson.toString();
        }

        if (kpiConfig.hasDrillTo()) {
            JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("drillTo", new JSONObject() {{
                    put("projectDashboard", kpiConfig.getDrillToDashboard());
                    put("projectDashboardTab", kpiConfig.getDrillToDashboardTab());
                }});

            content = contentJson.toString();
        }

        HttpRequestBase postRequest = restApiClient.newPostMethod(format(RestUtils.CREATE_AND_GET_OBJ_LINK,
                projectId), content);

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

    public static String createAnalyticalDashboard(RestApiClient restApiClient, String projectId,
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

    public static void prepareAnalyticalDashboardTemplate(RestApiClient restApiClient, String projectId)
            throws JSONException, IOException {
        // delete all dashboards, if some exist
        for (String dashboardLink: getAnalyticalDashboards(restApiClient, projectId)) {
            RestUtils.deleteObject(restApiClient, dashboardLink);
        }

        String amountMetricUri = getObjectUri(projectId, AMOUNT_OBJ_ID);
        String lostMetricUri = getObjectUri(projectId, LOST_OBJ_ID);
        String numOfActivitiesUri = getObjectUri(projectId, NUM_OF_ACTIVITIES_OBJ_ID);
        String dateDimensionUri = getObjectUri(projectId, DATE_DIM_CREATED_OBJ_ID);

        String amountWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("Amount")
                .metric(amountMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .build());
        String lostWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("Lost")
                .metric(lostMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(Kpi.ComparisonType.LAST_YEAR)
                .comparisonDirection(Kpi.ComparisonDirection.BAD)
                .build());
        String numOfActivitiesWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("# of Activities")
                .metric(numOfActivitiesUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
        String drillToWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("DrillTo")
                .metric(amountMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .drillToDashboard("/gdc/md/p8aqohkx4htbrau1wpk6k68crltlojig/obj/916")
                .drillToDashboardTab("adzD7xEmdhTx")
                .build());

        List<String> widgetUris = Arrays.asList(amountWidget, lostWidget, numOfActivitiesWidget, drillToWidget);
        createAnalyticalDashboard(restApiClient, projectId, widgetUris);
    }

    private static String getObjectUri(String projectId, String objectId) {
        return "/gdc/md/${projectId}/obj/${objectId}".replace("${projectId}", projectId)
                .replace("${objectId}", objectId);
    }
}
