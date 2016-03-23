package com.gooddata.qa.utils.http.indigo;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.deleteObject;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.GoodData;
import com.gooddata.md.Dataset;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.VisualizationMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.http.RestApiClient;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

/**
 * REST utilities for Indigo task
 */
public class IndigoRestUtils {

    private static final String AMOUNT = "Amount";
    private static final String LOST = "Lost";
    private static final String NUM_OF_ACTIVITIES = "# of Activities";
    private static final String DATE_DATA_SET_CREATED = "Date (Created)";

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
                    put("content", new JSONObject());
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
        String content = VISUALIZATION_WIDGET_BODY.get().replace("${title}", vizConfig.getTitle());

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("visualization")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Add KPI to analytical dashboard
     *
     * @param restApiClient
     * @param projectId
     * @param dashboardUri
     * @param widgetUri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static void addKpiWidgetToAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final String dashboardUri, final String widgetUri) throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets")
            .put(widgetUri);

        executeRequest(restApiClient, restApiClient.newPutMethod(dashboardUri, dashboard.toString()), HttpStatus.OK);
    }

    /**
     * Delete KPI from analytical dashboard
     *
     * @param restApiClient
     * @param projectId
     * @param dashboardUri
     * @param widgetUri
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static void deleteKpiWidgetFromAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final String dashboardUri, final String widgetUri) throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        final JSONArray widgets = dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets");
        final JSONArray newWidgets = new JSONArray();
        for (int i = 0, n = widgets.length(); i < n; i++) {
            final String uri = widgets.getString(i);
            if (!widgetUri.equals(uri)) {
                newWidgets.put(uri);
            }
        }
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .put("widgets", newWidgets);

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
     * A helper method to prepare analytical dashboard with some kpis added
     *
     * @param restApiClient
     * @param goodData
     * @param projectId
     * @throws org.json.JSONException
     * @throws java.io.IOException
     */
    public static void prepareAnalyticalDashboardTemplate(final RestApiClient restApiClient,
            final GoodData goodData, final String projectId) throws JSONException, IOException {
        // delete all dashboards, if some exist
        for (String dashboardLink: getAnalyticalDashboards(restApiClient, projectId)) {
            deleteObject(restApiClient, dashboardLink);
        }

        final Project project = goodData.getProjectService().getProjectById(projectId);
        final MetadataService service = goodData.getMetadataService();
        final String amountMetricUri = service.getObjUri(project, Metric.class, title(AMOUNT));
        final String lostMetricUri = service.getObjUri(project, Metric.class, title(LOST));
        final String numOfActivitiesUri = service.getObjUri(project, Metric.class, title(NUM_OF_ACTIVITIES));
        final String dataSetUri = getDateDataSetCreatedUri(goodData, projectId);

        final String amountWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(AMOUNT)
                .metric(amountMetricUri)
                .dateDataSet(dataSetUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .build());
        final String lostWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(LOST)
                .metric(lostMetricUri)
                .dateDataSet(dataSetUri)
                .comparisonType(Kpi.ComparisonType.LAST_YEAR)
                .comparisonDirection(Kpi.ComparisonDirection.BAD)
                .build());
        final String numOfActivitiesWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(NUM_OF_ACTIVITIES)
                .metric(numOfActivitiesUri)
                .dateDataSet(dataSetUri)
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
        final String drillToWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("DrillTo")
                .metric(amountMetricUri)
                .dateDataSet(dataSetUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .drillToDashboard("/gdc/md/p8aqohkx4htbrau1wpk6k68crltlojig/obj/916")
                .drillToDashboardTab("adzD7xEmdhTx")
                .build());

        final List<String> widgetUris = asList(amountWidget, lostWidget, numOfActivitiesWidget, drillToWidget);
        createAnalyticalDashboard(restApiClient, projectId, widgetUris);
    }

    /**
     * Get date data set created uri
     *
     * @param goodData
     * @param projectId
     * @return date data set created uri
     */
    public static String getDateDataSetCreatedUri(final GoodData goodData, final String projectId) {
        return goodData.getMetadataService()
            .getObjUri(goodData.getProjectService().getProjectById(projectId),
                    Dataset.class,
                    title(DATE_DATA_SET_CREATED));
    }
}
