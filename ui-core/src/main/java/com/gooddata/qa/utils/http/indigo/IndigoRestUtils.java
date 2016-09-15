package com.gooddata.qa.utils.http.indigo;

import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;
import com.gooddata.qa.utils.http.RestApiClient;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

/**
 * REST utilities for Indigo task
 */
public class IndigoRestUtils {

    private static final Logger log = Logger.getLogger(IndigoRestUtils.class.getName());

    private static final Supplier<String> ANALYTICAL_DASHBOARD_BODY = () -> {
        try {
            return new JSONObject() {{
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
    };

    private static final Supplier<String> KPI_WIDGET_BODY = () -> {
        try {
            return new JSONObject() {{
                put("kpi", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                    put("content", new JSONObject() {{
                        put("comparisonType", "${comparisonType}");
                        put("metric", "${metric}");
                        put("dateDataset", "${dateDataSet}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> VISUALIZATION_WIDGET_BODY = () -> {
        try {
            return new JSONObject() {{
                put("visualization", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                    put("content", new JSONObject() {{
                        put("type", "${type}");
                        put("buckets", new JSONObject() {{
                            put("measures", new JSONArray());
                            put("categories", new JSONArray());
                            put("filters", new JSONArray());
                        }});
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> VISUALIZATION_WIDGET_WRAP_BODY = () -> {
        try {
            return new JSONObject() {{
                put("visualizationWidget", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                        put("category", "visualizationWidget");
                    }});
                    put("content", new JSONObject() {{
                        put("visualization", "${visualization}");
                        put("ignoreDashboardFilters", new JSONArray());
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    /**
     * Get analytical dashboards of a project
     *
     * @param restApiClient
     * @param projectId
     * @return list of analytical dashboard links
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static List<String> getAnalyticalDashboards(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        final String analyticalDashboardsUri = "/gdc/md/" + projectId + "/query/analyticaldashboard";
        final JSONArray entries = getJsonObject(restApiClient, analyticalDashboardsUri)
                .getJSONObject("query")
                .getJSONArray("entries");
        final List<String> dashboardLinks = new ArrayList<>();
        for (int i = 0, n = entries.length(); i < n; i++) {
            dashboardLinks.add(entries.getJSONObject(i).getString("link"));
        }

        return dashboardLinks;
    }

    /**
     * Create KPI widget
     *
     * @param restApiClient
     * @param projectId
     * @param kpiConfig
     * @return KPI uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static String createKpiWidget(final RestApiClient restApiClient, final String projectId,
            final KpiMDConfiguration kpiConfig) throws JSONException, IOException {
        String content = KPI_WIDGET_BODY.get()
                .replace("${title}", kpiConfig.getTitle())
                .replace("${metric}", kpiConfig.getMetric())
                .replace("${dateDataSet}", kpiConfig.getDateDataSet())
                .replace("${comparisonType}", kpiConfig.getComparisonType().getJsonKey());

        if (kpiConfig.hasComparison()) {
            final JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("comparisonDirection", kpiConfig.getComparisonDirection().toString());

            content = contentJson.toString();
        }

        if (kpiConfig.hasDrillTo()) {
            final JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("drillTo", new JSONObject() {{
                    put("projectDashboard", kpiConfig.getDrillToDashboard());
                    put("projectDashboardTab", kpiConfig.getDrillToDashboardTab());
                }});

            content = contentJson.toString();
        }

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("kpi")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Create Visualization widget
     *
     * @param restApiClient
     * @param projectId
     * @param vizConfig
     * @return Visualization uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static String createVisualizationWidget(final RestApiClient restApiClient, final String projectId,
            final VisualizationMDConfiguration vizConfig) throws JSONException, IOException {
        String content = VISUALIZATION_WIDGET_BODY.get()
                .replace("${title}", vizConfig.getTitle())
                .replace("${type}", vizConfig.getType());

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("visualization")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Create Visualization widget wrap
     *
     * @param restApiClient
     * @param projectId
     * @param visualizationTitle
     * @param visualizationUri
     * @return Visualization widget uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static String createVisualizationWidgetWrap(final RestApiClient restApiClient, final String projectId,
            final String visualizationUri, final String visualizationTitle) throws JSONException, IOException {
        String content = VISUALIZATION_WIDGET_WRAP_BODY.get()
                .replace("${title}", visualizationTitle)
                .replace("${visualization}", visualizationUri);

        JSONObject response = getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content));

        return response
                    .getJSONObject("visualizationWidget")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Add widget to analytical dashboard
     *
     * @param restApiClient
     * @param projectId
     * @param dashboardUri
     * @param widgetUri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static void addWidgetToAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final String dashboardUri, final String widgetUri) throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets")
            .put(widgetUri);

        executeRequest(restApiClient, restApiClient.newPutMethod(dashboardUri, dashboard.toString()), HttpStatus.OK);
    }

    /**
     * Create new analytical dashboard
     *
     * @param restApiClient
     * @param projectId
     * @param widgetUris
     * @return new analytical dashboard uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static String createAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final Collection<String> widgetUris) throws JSONException, IOException {

        // TODO: consider better with .put() and have clever template
        final String widgets = new JSONArray(widgetUris).toString();
        final String content = ANALYTICAL_DASHBOARD_BODY.get().replace("\"widgets\":[]", "\"widgets\":" + widgets);

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("analyticalDashboard")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Get all created insight names in a specified project
     *
     * @param restApiClient
     * @param projectId
     * @return a list of insight names
     */
    public static List<String> getAllInsightNames(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        final JSONArray objects = getMdObjects(restApiClient, projectId, "visualizations");
        final List<String> insights = new ArrayList<>();
        for (int i = 0, n = objects.length(); i < n; i++) {
            insights.add(objects.getJSONObject(i).getString("title"));
        }

        return insights;
    }

    /**
     * Get insight uri by a specified name
     *
     * @param insight
     * @param restApiClient
     * @param projectId
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static String getInsightUri(final String insight, final RestApiClient restApiClient,
            final String projectId) throws JSONException, IOException {
        return getMdObjectUriByTitle(insight, "visualizations", restApiClient, projectId);
    }

    /**
     * Delete a dashboard by its uri
     *
     * @param restApiClient
     * @param dashboardUri
     */
    public static void deleteAnalyticalDashboard(final RestApiClient restApiClient, final String dashboardUri) {
        executeRequest(restApiClient, restApiClient.newDeleteMethod(dashboardUri), HttpStatus.NO_CONTENT);
    }

    public static String getKpiUri(final String kpi, final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        return getMdObjectUriByTitle(kpi, "kpi", restApiClient, projectId);
    }

    /**
     * Delete widget and its dependencies and dashboards will be deleted when it contains no widget.
     * 
     * @param restApiClient
     * @param projectId
     * @param widgetUris
     * @throws JSONException
     * @throws IOException
     */
    public static void deleteWidgetsUsingCascase(final RestApiClient restApiClient, final String projectId,
            final String... widgetUris) throws JSONException, IOException {

        deleteObjectsUsingCascase(restApiClient, projectId, widgetUris);

        // empty dashboard should be deleted right after widgets
        // this makes UI & automation consistent
        // if having more than 1 dashboard, the first one will be working project by default
        final String workingDashboardUri = getAnalyticalDashboards(restApiClient, projectId).get(0);

        if (getDashboardWidgetCount(restApiClient, workingDashboardUri) == 0) {
            deleteAnalyticalDashboard(restApiClient, workingDashboardUri);
            log.info("The working dashboard has been deleted");
        }
    }

    /**
     * Delete all dashboards and its dependencies.
     * Theoretically, we can create many dashboards using REST
     * This method helps us clean up environment
     * 
     * @param restApiClient
     * @param projectId
     * @throws JSONException
     * @throws IOException
     */
    public static void deleteDashboardsUsingCascase(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        final List<String> dashboardUris = getAnalyticalDashboards(restApiClient, projectId);
        deleteObjectsUsingCascase(restApiClient, projectId,
                dashboardUris.toArray(new String[dashboardUris.size()]));
    }

    private static JSONArray getMdObjects(final RestApiClient restApiClient, final String projectId,
            final String type) throws JSONException, IOException {
        final String query = "/gdc/md/" + projectId + "/query/" + type;
        return getJsonObject(restApiClient, query).getJSONObject("query").getJSONArray("entries");
    }

    private static String getMdObjectUriByTitle(final String title, final String type,
            final RestApiClient restApiClient, final String projectId) throws JSONException, IOException {
        final JSONArray objects = getMdObjects(restApiClient, projectId, type);
        for (int i = 0, n = objects.length(); i < n; i++) {
            if(title.equals(objects.getJSONObject(i).getString("title")))
                return objects.getJSONObject(i).getString("link");
        }
        throw new RuntimeException("There is no " + type + " titled " + title);
    }

    private static int getDashboardWidgetCount(final RestApiClient restApiClient, final String dashboardUri)
            throws JSONException, IOException {
        return getJsonObject(restApiClient, dashboardUri).getJSONObject("analyticalDashboard")
                .getJSONObject("content").getJSONArray("widgets").length();
    }
}
