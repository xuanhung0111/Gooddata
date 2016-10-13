package com.gooddata.qa.utils.http.indigo;

import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
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
                        put("ignoreDashboardFilters", new JSONArray());
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
        return getMdObjectValues(restApiClient, projectId, "analyticaldashboard", jsonObj -> jsonObj.getString("link"));
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
     * Create an insight. Currently, the insight could be a combination of measure, view by
     * and stack by.
     *
     * @param restApiClient
     * @param projectId
     * @param insightConfig
     * @return
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    public static String createInsight(final RestApiClient restApiClient, final String projectId,
            final InsightMDConfiguration insightConfig) throws ParseException, JSONException, IOException {
        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId),
                        initInsightObject(insightConfig).toString())).getJSONObject("visualization")
                                .getJSONObject("meta").getString("uri");
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
        return getMdObjectValues(restApiClient, projectId, "visualizations", jsonObj -> jsonObj.getString("title"));
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
        return getMdObjectValue(restApiClient, projectId, "visualizations",
                jsonObj -> insight.equals(jsonObj.getString("title")), jsonObj -> jsonObj.getString("link"));
    }

    /**
     * get all insight widget titles
     *
     * @param restApiClient
     * @param projectId
     * @return list of titles. Otherwise, return empty list
     * @throws JSONException
     * @throws IOException
     */
    public static List<String> getInsightWidgetTitles(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        return getMdObjectValues(restApiClient, projectId, "visualizationwidgets",
                jsonObj -> jsonObj.getString("title"));
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
        return getMdObjectValue(restApiClient, projectId, "kpi", jsonObj -> kpi.equals(jsonObj.getString("title")),
                jsonObj -> jsonObj.getString("link"));
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

    private static int getDashboardWidgetCount(final RestApiClient restApiClient, final String dashboardUri)
            throws JSONException, IOException {
        return getJsonObject(restApiClient, dashboardUri).getJSONObject("analyticalDashboard")
                .getJSONObject("content").getJSONArray("widgets").length();
    }

    private static JSONObject initInsightObject(final InsightMDConfiguration insightConfig) throws JSONException {
        return new JSONObject() {{
            put("visualization", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("buckets", new JSONObject() {{
                        put("measures", initMeasureObjects(insightConfig.getMeasureBuckets()));
                        put("categories", initCategoryObjects(insightConfig.getCategoryBuckets()));
                        put("filters", new JSONArray());
                    }});
                    put("type", insightConfig.getType().getLabel());
                }});
                put("meta", new JSONObject() {{
                    put("title", insightConfig.getTitle());
                }});
            }});
        }};
    }

    private static JSONArray initMeasureObjects(final List<MeasureBucket> measureBuckets) throws JSONException {
        return new JSONArray() {{
                if (!CollectionUtils.isEmpty(measureBuckets)) {
                    for (MeasureBucket bucket : measureBuckets) {
                        put(new JSONObject() {{
                            put("measure", new JSONObject() {{
                                put("measureFilters", new JSONArray());
                                put("title", bucket.getTitle());
                                put("showPoP", bucket.hasShowPoP());
                                put("showInPercent", bucket.hasShowInPercent());
                                put("type", bucket.getType());
                                put("objectUri", bucket.getObjectUri());
                            }});
                        }});
                    }
                }
        }};
    }

    private static JSONArray initCategoryObjects(final List<CategoryBucket> categoryBuckets) throws JSONException {
        return new JSONArray() {{
                if (!CollectionUtils.isEmpty(categoryBuckets)) {
                    for (CategoryBucket bucket : categoryBuckets) {
                        put(new JSONObject() {{
                            put("category", new JSONObject() {{
                                put("attribute", bucket.getAttribute());
                                put("displayForm", bucket.getDisplayForm());
                                put("collection", bucket.getCollection());
                                put("type", bucket.getType());
                            }});
                        }});
                    }
                }
        }};
    }

    private static <T> List<T> getMdObjectValues(final RestApiClient restApiClient, final String projectId,
            final String type, final ThrowingFunction<JSONObject, T> func) {
        try {
            JSONArray jsonArray = getMdObjects(restApiClient, projectId, type);
            List<T> results = new ArrayList<>();

            for (int i = 0, n = jsonArray.length(); i < n; i++) {
                results.add(func.apply(jsonArray.getJSONObject(i)));
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T getMdObjectValue(final RestApiClient restApiClient, final String projectId,
            final String type, final ThrowingPredicate<JSONObject> filter,
            final ThrowingFunction<JSONObject, T> func) {
        try {
            JSONArray jsonArray = getMdObjects(restApiClient, projectId, type);
            JSONObject foundObject = null;

            for (int i = 0, n = jsonArray.length(); i < n; i++) {
                foundObject = jsonArray.getJSONObject(i);

                if (filter.test(foundObject)) {
                    return func.apply(foundObject);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Can't find the object");
    }

    private static JSONArray getMdObjects(final RestApiClient restApiClient, final String projectId,
            final String type) throws JSONException, IOException {
        return getJsonObject(restApiClient, "/gdc/md/" + projectId + "/query/" + type).getJSONObject("query")
                .getJSONArray("entries");
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private interface ThrowingPredicate<T> {
        boolean test(T t) throws Exception;
    }
}