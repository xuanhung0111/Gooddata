package com.gooddata.qa.utils.http.dashboards;

import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.utils.http.RestApiClient;
import com.google.common.collect.Lists;

public final class DashboardsRestUtils {

    private static final Logger log = Logger.getLogger(DashboardsRestUtils.class.getName());

    private DashboardsRestUtils() {
    }

    private static final String TARGET_POPUP = "pop-up";
    private static final String TARGET_EXPORT = "export";

    private static final String MUF_LINK = "/gdc/md/%s/userfilters";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";
    private static final String DASHBOARD_EDIT_MODE_LINK = "/gdc/md/%s/obj/%s?mode=edit";

    private static final Supplier<String> MUF_OBJ =  () -> {
        try {
            return new JSONObject() {{
                put("userFilter", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("expression", "${MUFExpression}");
                    }});
                    put("meta", new JSONObject() {{
                        put("category", "userFilter");
                        put("title", "${MUFTitle}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> USER_FILTER = () -> {
        try {
            return new JSONObject() {{
                put("userFilters", new JSONObject() {{
                    put("items", new JSONArray() {{
                        put(new JSONObject() {{
                            put("user", "/gdc/account/profile/${email}");
                            put("userFilters", new JSONArray() {{
                                put("$MUFExpression");
                            }});
                        }});
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> CREATE_COMMENT_CONTENT_BODY = () -> {
        try {
            return new JSONObject() {{
                put("comment", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                    put("content", new JSONObject() {{
                        put("related", "#{related}");
                        put("content", "${title}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    public static void deleteDashboardTab(final RestApiClient restApiClient, final String dashboardUri,
            final String tabName) throws IOException, JSONException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        final JSONArray tabs = dashboard.getJSONObject("projectDashboard")
                .getJSONObject("content")
                .getJSONArray("tabs");

        final JSONArray newTabs = new JSONArray();
        for (int i = 0, n = tabs.length(); i < n; i++) {
            final JSONObject tab = tabs.getJSONObject(i);
            if (!tabName.equals(tab.getString("title"))) {
                newTabs.put(tab);
            }
        }

        dashboard.getJSONObject("projectDashboard").getJSONObject("content").put("tabs", newTabs);
        executeRequest(restApiClient,
                restApiClient.newPostMethod(dashboardUri + "?mode=edit", dashboard.toString()),
                HttpStatus.OK);
    }

    public static String addComment(final RestApiClient restApiClient, final String projectId,
            final String comment, final String objectId) throws ParseException, JSONException, IOException {
        final String objectUri = format(OBJ_LINK, projectId) + objectId;

        log.info("Verify object id: " + objectUri);
        getJsonObject(restApiClient, objectUri);

        final String content = CREATE_COMMENT_CONTENT_BODY.get().replace("${title}", comment)
                .replace("#{related}", objectUri);
        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("comment")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    public static void changeMetricFormat(final RestApiClient restApiClient, final String metricUri,
            final String newFormat) throws ParseException, JSONException, IOException {
        final JSONObject json = getJsonObject(restApiClient, metricUri);
        final JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("format", newFormat);
        executeRequest(restApiClient, restApiClient.newPutMethod(metricUri, json.toString()), HttpStatus.OK);
    }

    public static void changeMetricExpression(final RestApiClient restApiClient, final String metricUri,
            final String newExpression) throws ParseException, JSONException, IOException {
        final JSONObject json = getJsonObject(restApiClient, metricUri);
        final JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("expression", newExpression);
        executeRequest(restApiClient, restApiClient.newPutMethod(metricUri, json.toString()), HttpStatus.OK);
    }

    public static String createMUFObj(final RestApiClient restApiClient, final String projectID,
            final String mufTitle, final Map<String, Collection<String>> conditions)
                    throws IOException, JSONException {
        final String mdObjURI = format(OBJ_LINK, projectID);
        final String MUFExpressions = buildFilterExpression(projectID, conditions);
        final String contentBody = MUF_OBJ.get().replace("${MUFExpression}", MUFExpressions).replace("${MUFTitle}", mufTitle);

        return getJsonObject(restApiClient, restApiClient.newPostMethod(mdObjURI, contentBody)).getString("uri");
    }

    public static void addMUFToUser(final RestApiClient restApiClient, final String projectURI, final String user,
            final String mufURI) throws ParseException, IOException {
        final String urserFilter = format(MUF_LINK, projectURI);
        final String contentBody = USER_FILTER.get().replace("${email}", user).replace("$MUFExpression", mufURI);

        executeRequest(restApiClient, restApiClient.newPostMethod(urserFilter, contentBody), HttpStatus.OK);
    }

    public static void setDrillReportTargetAsPopup(final RestApiClient restApiClient, final String projectID,
            final String dashboardID) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_POPUP, null);
    }

    public static void setDrillReportTargetAsExport(final RestApiClient restApiClient, final String projectID,
            final String dashboardID, final String exportFormat) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_EXPORT, exportFormat);
    }

    private static String buildFilterExpression(final String projectID, final Map<String, Collection<String>> conditions) {
        // syntax: "([<Attribute_URI_1>] IN ([<element_URI_1>], [element_URI_2], [...] )) AND
        // ([<Attribute_URI_2>] IN ([<element_URI_1>], [element_URI_2], [...]))";

        final List<String> expressions = Lists.newArrayList();
        final String attributeURI = "[/gdc/md/%s/obj/%s]";
        final String elementURI = "[/gdc/md/%s/obj/%s/elements?id=%s]";
        final String expression = "(%s IN (%s))";

        for (final String attributeID : conditions.keySet()) {
            expressions.add(format(expression,
                    format(attributeURI, projectID, attributeID),
                    conditions.get(attributeID)
                        .stream()
                        .map(s -> format(elementURI, projectID, attributeID, s))
                        .collect(joining(","))
            ));
        }
        return expressions.stream().collect(joining(" AND "));
    }

    private static void setDrillReportTarget(final RestApiClient restApiClient, final String projectID,
            final String dashboardID, final String target, final String exportFormat)
                    throws JSONException, IOException {
        final String dashboardEditModeURI = format(DASHBOARD_EDIT_MODE_LINK, projectID, dashboardID);
        final JSONObject json = getJsonObject(restApiClient, restApiClient.newGetMethod(dashboardEditModeURI));
        final JSONObject drills = json.getJSONObject("projectDashboard")
                .getJSONObject("content")
                .getJSONArray("tabs")
                .getJSONObject(0)
                .getJSONArray("items")
                .getJSONObject(0)
                .getJSONObject("reportItem")
                .getJSONArray("drills")
                .getJSONObject(0);
        drills.put("target", target);

        if (TARGET_POPUP.equals(target)) {
            drills.remove("export");
        } else if (TARGET_EXPORT.equals(target)) {
            drills.put("export", new JSONObject() {{
                put("format", exportFormat);
            }});
        }

        executeRequest(restApiClient,
                restApiClient.newPostMethod(dashboardEditModeURI, json.toString()),
                HttpStatus.OK);
    }
}
