package com.gooddata.qa.utils.http.indigo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.graphene.entity.visualization.TotalsBucket;
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
import static java.lang.String.format;

/**
 * REST request for Indigo task
 */
public class IndigoRestRequest extends CommonRestRequest{
    private static final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";

    public IndigoRestRequest(final RestClient restClient, final String projectId){
        super(restClient, projectId);
    }
    private static final Logger log = Logger.getLogger(IndigoRestRequest.class.getName());

    private static final Supplier<String> ANALYTICAL_DASHBOARD_BODY = () -> {
        try {
            return new JSONObject() {{
                put("analyticalDashboard", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
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
                        put("dateDataSet", "${dateDataSet}");
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
     * Get uri for specific analytical dashboard
     *
     * @param dashboardTitle dashboard title / name
     * @return Uri of analytical dashboard
     * @throws JSONException
     * @throws IOException
     */
    public String getAnalyticalDashboardUri(final String dashboardTitle) throws JSONException, IOException {
        return getMdObjectValue("analyticaldashboard",
                jsonObj -> dashboardTitle.equals(jsonObj.getString("title")), jsonObj -> jsonObj.getString("link"));
    }

    /**
     * Get analytical dashboards of a project
     *
     * @return list of analytical dashboard links
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public List<String> getAnalyticalDashboards()
            throws JSONException, IOException {
        return getMdObjectValues("analyticaldashboard", jsonObj -> jsonObj.getString("link"));
    }

    /**
     * Get identifier of analytical dashboard
     *
     * @param dashboardTitle dashboard title / name
     * @return identifier of analytical dashboard
     * @throws JSONException
     * @throws IOException
     */
    public String getAnalyticalDashboardIdentifier(final String dashboardTitle) throws JSONException, IOException {
        return getMdObjectValue("analyticaldashboard",
                jsonObj -> dashboardTitle.equals(jsonObj.getString("title")), jsonObj -> jsonObj.getString("identifier"));
    }

    /**
     * Create KPI widget
     *
     * @param kpiConfig
     * @return KPI uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public String createKpiWidget(final KpiMDConfiguration kpiConfig) throws JSONException, IOException {
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

        return getJsonObject(
                RestRequest.initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("kpi")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Get uri of the visualizationClass for given visualization type (GD custom visualizations)
     * Currently visualizationClass's content.url for gd visualizations is "local:<visualizationType>"
     *
     * @param type visualization type, e.g. table, bar...
     * @return visualizationClass uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public String getVisualizationClassUri(final String type) throws JSONException, IOException {
        final String queryUri = "/gdc/md/" + projectId + "/objects/query?category=visualizationClass&limit=50";
        final String localType = "local:" + type;

        final JSONArray visualizationClasses = getJsonObject(queryUri)
            .getJSONObject("objects")
            .getJSONArray("items");

        for (int i = 0, n = visualizationClasses.length(); i < n; i++) {
            final JSONObject visualizationClass = visualizationClasses.getJSONObject(i);
            if (visualizationClass.getJSONObject("visualizationClass").getJSONObject("content").getString("url").equals(localType)) {
                return visualizationClass.getJSONObject("visualizationClass").getJSONObject("meta").getString("uri");
            }
        }

        return null;
    }

    /**
     * Create an insight. Currently, the insight could be a combination of measure, view by
     * and stack by.
     *
     * @param insightConfig
     * @return insight uri
     */
    public String createInsight(final InsightMDConfiguration insightConfig) {
        String jsonObject;
        try {
            String visualizationClassUri = getVisualizationClassUri(insightConfig.getType().getLabel());
            jsonObject = getJsonObject(
                    RestRequest.initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId),
                            initInsightObject(visualizationClassUri, insightConfig).toString())).getJSONObject("visualizationObject")
                    .getJSONObject("meta").getString("uri");
        } catch (ParseException | JSONException | IOException e) {
            throw new RuntimeException("There error while creating Insight", e);
        }
        return jsonObject;
    }

    /**
     * Add Total Results to insight
     *
     * @param insightTitle
     * @param totalsBuckets
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    public String addTotalResults(final String insightTitle, List<TotalsBucket> totalsBuckets)
            throws ParseException, JSONException, IOException {
        String uri = getInsightUri(insightTitle);
        JSONObject insight = getJsonObject(uri);
        insight.getJSONObject("visualizationObject").getJSONObject("content")
                .getJSONArray("buckets").getJSONObject(1)
                .put("totals", initTotalsObjects(totalsBuckets));
        executeRequest(RestRequest.initPutRequest(uri, insight.toString()), HttpStatus.OK);
        return uri;
    }

    /**
     * Create Visualization widget wrap
     *
     * @param visualizationTitle
     * @param visualizationUri
     * @return Visualization widget uri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public String createVisualizationWidget(final String visualizationUri, final String visualizationTitle)
            throws JSONException, IOException {
        String content = VISUALIZATION_WIDGET_BODY.get()
                .replace("${title}", visualizationTitle)
                .replace("${visualization}", visualizationUri);

        JSONObject response = getJsonObject(
                RestRequest.initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId), content));

        return response
                    .getJSONObject("visualizationWidget")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Add widget to analytical dashboard
     *
     * @param dashboardUri
     * @param widgetUri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public void addWidgetToAnalyticalDashboard(final String dashboardUri, final String widgetUri)
            throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(dashboardUri);
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets")
            .put(widgetUri);

        executeRequest(RestRequest.initPutRequest(dashboardUri, dashboard.toString()), HttpStatus.OK);
    }

    /**
     * Create new analytical dashboard
     *
     * @param widgetUris
     * @param title
     * @return new analytical dashboard uri
     */
    public String createAnalyticalDashboard(final Collection<String> widgetUris, final String title) {
        // TODO: consider better with .put() and have clever template
        final String widgets = new JSONArray(widgetUris).toString();
        final String content = ANALYTICAL_DASHBOARD_BODY.get()
                .replace("\"widgets\":[]", "\"widgets\":" + widgets)
                .replace("${title}", title);
        String uri;
        try {
            uri = getJsonObject(
                    RestRequest.initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("analyticalDashboard")
                    .getJSONObject("meta")
                    .getString("uri");
        } catch (JSONException | IOException e) {
            throw new RuntimeException("There is error while getting JSON object", e);
        }
        return uri;
    }

    /**
     * Create new analytical dashboard with default title
     * @param widgetUris
     * @return
     */
    public String createAnalyticalDashboard(final Collection<String> widgetUris) {
        return createAnalyticalDashboard(widgetUris, "title");
    }

    /**
     * Get all created insight names in a specified project
     *
     * @return a list of insight names
     */
    public List<String> getAllInsightNames()
            throws JSONException, IOException {
        return getMdObjectValues("visualizationobjects", jsonObj -> jsonObj.getString("title"));
    }

    /**
     * Get insight uri by a specified name
     *
     * @param insight
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public String getInsightUri(final String insight) throws JSONException, IOException {
        return getMdObjectValue("visualizationobjects",
                jsonObj -> insight.equals(jsonObj.getString("title")), jsonObj -> jsonObj.getString("link"));
    }

    /**
     * Get all insight uris
     *
     * @return
     */
    public List<String> getInsightUris() {
        return getMdObjectValues("visualizationobjects", jsonObj -> jsonObj.getString("link"));
    }

    /**
     * get all insight widget titles
     *
     * @return list of titles. Otherwise, return empty list
     * @throws JSONException
     * @throws IOException
     */
    public List<String> getInsightWidgetTitles()
            throws JSONException, IOException {
        return getMdObjectValues("visualizationwidgets",
                jsonObj -> jsonObj.getString("title"));
    }

    /**
     * Delete a dashboard by its uri
     *
     * @param dashboardUri
     */
    public void deleteAnalyticalDashboard(final String dashboardUri) {
        executeRequest(RestRequest.initDeleteRequest(dashboardUri), HttpStatus.NO_CONTENT);
    }

    public String getKpiUri(final String kpi)
            throws JSONException, IOException {
        return getMdObjectValue("kpi", jsonObj -> kpi.equals(jsonObj.getString("title")),
                jsonObj -> jsonObj.getString("link"));
    }

    /**
     * Delete widget and its dependencies and dashboards will be deleted when it contains no widget.
     *
     * @param widgetUris
     * @throws JSONException
     * @throws IOException
     */
    public void deleteWidgetsUsingCascade(final String... widgetUris) throws JSONException, IOException {

        deleteObjectsUsingCascade(widgetUris);

        // empty dashboard should be deleted right after widgets
        // this makes UI & automation consistent
        // if having more than 1 dashboard, the first one will be working project by default
        final String workingDashboardUri = getAnalyticalDashboards().get(0);

        if (getDashboardWidgetCount(workingDashboardUri) == 0) {
            deleteAnalyticalDashboard(workingDashboardUri);
            log.info("The working dashboard has been deleted");
        }
    }

    /**
     * Delete all dashboards and its dependencies.
     * Theoretically, we can create many dashboards using REST
     * This method helps us clean up environment
     *
     * @throws JSONException
     * @throws IOException
     */
    public void deleteDashboardsUsingCascade()
            throws JSONException, IOException {
        final List<String> dashboardUris = getAnalyticalDashboards();
        deleteObjectsUsingCascade(dashboardUris.toArray(new String[dashboardUris.size()]));
    }

    /**
     * Delete attribute filter on Indigo dashboard
     * @param attributeDisplayFormUri
     * @throws IOException
     * @throws JSONException
     */
    public void deleteAttributeFilterIfExist(final String attributeDisplayFormUri) throws IOException, JSONException {
        // dashboard is now containing 1 filter context
        String targetUri = getFilterContextUris().get(0);

        JSONObject filterContext = getJsonObject(targetUri);
        JSONArray filtersArray = filterContext.getJSONObject("filterContext")
                .getJSONObject("content").getJSONArray("filters");

        JSONArray newArray = new JSONArray();
        for (int i = 0; i < filtersArray.length(); i++) {
            JSONObject obj = filtersArray.getJSONObject(i);

            if (obj.has("attributeFilter") && attributeDisplayFormUri.equals(obj.getJSONObject("attributeFilter")
                    .getString("displayForm")))
                continue;

            newArray.put(obj);
        }

        filterContext.getJSONObject("filterContext")
                .getJSONObject("content")
                .put("filters", newArray);

        executeRequest(RestRequest.initPutRequest(targetUri, filterContext.toString()), HttpStatus.OK);
    }

    private int getDashboardWidgetCount(final String dashboardUri)
            throws JSONException, IOException {
        return getJsonObject(dashboardUri).getJSONObject("analyticalDashboard")
                .getJSONObject("content").getJSONArray("widgets").length();
    }

    private JSONObject initInsightObject(final String visualizationClassUri,
            final InsightMDConfiguration insightConfig) throws JSONException {
        return new JSONObject() {{
            put("visualizationObject", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("visualizationClass", new JSONObject() {{
                        put("uri", visualizationClassUri);
                    }});
                    put("buckets", initBuckets(insightConfig.getMeasureBuckets(), insightConfig.getCategoryBuckets()));
                    put("filters", new JSONArray());
                }});
                put("meta", new JSONObject() {{
                    put("title", insightConfig.getTitle());
                }});
            }});
        }};
    }

    private JSONArray initBuckets(final List<MeasureBucket> measureBuckets, final List<CategoryBucket> categoryBuckets)
            throws JSONException {
        return new JSONArray() {{
            put(new JSONObject() {{
                put("localIdentifier", "measures");
                put("items", initMeasureObjects(measureBuckets));
            }});
            put(new JSONObject() {{
                put("localIdentifier", categoryBuckets.isEmpty() ? "categories" : categoryBuckets.get(0).getCollection());
                put("items", initCategoryObjects(categoryBuckets));
            }});
        }};
    }

    private JSONArray initMeasureObjects(final List<MeasureBucket> measureBuckets) throws JSONException {
        return new JSONArray() {{
                if (!CollectionUtils.isEmpty(measureBuckets)) {
                    for (MeasureBucket bucket : measureBuckets) {
                        put(new JSONObject() {{
                            put("measure", new JSONObject() {{
                                put("localIdentifier", bucket.getLocalIdentifier());
                                put("title", bucket.getTitle());
                                put("definition", new JSONObject() {{
                                    put("measureDefinition", new JSONObject() {{
                                        put("item", new JSONObject() {{
                                            put("uri", bucket.getObjectUri());
                                        }});
                                        put("filters", new JSONArray());
                                        put("computeRatio", bucket.hasShowInPercent());
                                    }});
                                }});
                            }});
                        }});
                    }
                }
        }};
    }

    private static JSONArray initTotalsObjects(final List<TotalsBucket> totalsBuckets) throws JSONException {
        return new JSONArray() {{
            for (TotalsBucket bucket : totalsBuckets) {
                put(new JSONObject() {{
                    put("measureIdentifier", bucket.getMeasureIdentifier());
                    put("attributeIdentifier", bucket.getAttributeIdentifier());
                    put("type", bucket.getType());
                }});
            }
        }};
    }

    private static JSONArray initCategoryObjects(final List<CategoryBucket> categoryBuckets) throws JSONException {
        return new JSONArray() {{
                if (!CollectionUtils.isEmpty(categoryBuckets)) {
                    for (CategoryBucket bucket : categoryBuckets) {
                        put(new JSONObject() {{
                            put("visualizationAttribute", new JSONObject() {{
                                put("localIdentifier", bucket.getLocalIdentifier());
                                put("displayForm", new JSONObject() {{
                                    put("uri", bucket.getDisplayForm());
                                }});
                            }});
                        }});
                    }
                }
        }};
    }

    private <T> List<T> getMdObjectValues(final String type, final ThrowingFunction<JSONObject, T> func) {
        try {
            JSONArray jsonArray = getMdObjects(type);
            List<T> results = new ArrayList<>();

            for (int i = 0, n = jsonArray.length(); i < n; i++) {
                results.add(func.apply(jsonArray.getJSONObject(i)));
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T getMdObjectValue(final String type, final ThrowingPredicate<JSONObject> filter,
            final ThrowingFunction<JSONObject, T> func) {
        try {
            JSONArray jsonArray = getMdObjects(type);
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

    private JSONArray getMdObjects(final String type) throws JSONException, IOException {
        return getJsonObject("/gdc/md/" + projectId + "/query/" + type).getJSONObject("query")
                .getJSONArray("entries");
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private interface ThrowingPredicate<T> {
        boolean test(T t) throws Exception;
    }

    private List<String> getFilterContextUris() {
        return getMdObjectValues("filtercontexts", jsonObj -> jsonObj.getString("link"));
    }
}
