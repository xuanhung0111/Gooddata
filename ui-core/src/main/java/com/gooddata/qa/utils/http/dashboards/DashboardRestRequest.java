package com.gooddata.qa.utils.http.dashboards;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.gooddata.qa.utils.http.RestRequest.*;
import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class DashboardRestRequest extends CommonRestRequest {

    private static final String OBJ_LINK = "/gdc/md/%s/obj/";
    private static final String PROJECT_DASHBOARDS_URI = "/gdc/md/%s/query/projectdashboards";
    private static final String DASHBOARD_EDIT_MODE_LINK = "/gdc/md/%s/obj/%s?mode=edit";
    private static final String TARGET_POPUP = "pop-up";
    private static final String TARGET_EXPORT = "export";

    private static final Logger log = Logger.getLogger(DashboardRestRequest.class.getName());

    public DashboardRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * create a dashboard with given content
     *
     * @param content
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public String createDashboard(JSONObject content) throws JSONException, IOException {
        return getJsonObject(initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId), content.toString()))
                .getJSONObject("projectDashboard")
                .getJSONObject("meta")
                .getString("uri");
    }

    /**
     * Delete dashboard tab
     *
     * @param dashboardUri
     * @param tabName
     */
    public void deleteDashboardTab(String dashboardUri, String tabName) throws IOException, JSONException {
        final JSONObject dashboard = getJsonObject(dashboardUri);
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
        executeRequest(initPostRequest(dashboardUri + "?mode=edit", dashboard.toString()), HttpStatus.OK);
    }

    /**
     * Add comment for object
     *
     * @param comment
     * @param objectId
     * @return comment uri
     */
    public String addComment(String comment, String objectId) throws IOException {
        String objectUri = objectId.startsWith("/gdc") ? objectId : format(OBJ_LINK, projectId) + objectId;

        log.info("Verify object id: " + objectUri);
        getJsonObject(objectUri);

        String content = new JSONObject() {{
            put("comment", new JSONObject() {{
                put("meta", new JSONObject() {{
                    put("title", comment);
                }});
                put("content", new JSONObject() {{
                    put("related", objectUri);
                    put("content", comment);
                }});
            }});
        }}.toString();

        return getJsonObject(initPostRequest(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                .getJSONObject("comment")
                .getJSONObject("meta")
                .getString("uri");
    }

    public String getDashboardUri(String title)
            throws JSONException, IOException {
        JSONArray dashboards = getJsonObject(format(PROJECT_DASHBOARDS_URI, projectId))
                .getJSONObject("query")
                .getJSONArray("entries");

        for (int i = 0; i < dashboards.length(); i++) {
            if (title.equals(dashboards.getJSONObject(i).getString("title"))) {
                return dashboards.getJSONObject(i).getString("link");
            }
        }

        throw new RuntimeException("No dashboard matches with title: " + title);
    }

    public void deleteAllDashboards() {
        try {
            JSONArray dashboards = getJsonObject(format(PROJECT_DASHBOARDS_URI, projectId))
                    .getJSONObject("query")
                    .getJSONArray("entries");
            for (int i = 0; i < dashboards.length(); i++) {
                final String uri = dashboards.getJSONObject(i).getString("link");
                deleteObjectsUsingCascade(uri);
            }
        } catch (JSONException | IOException e) {
            throw new RuntimeException("Cannot retrieve dashboard from project:" + projectId);
        }
    }

    public String getTabId(String dashboardTitle, String tabTitle) throws JSONException, IOException {
        JSONArray tabs = getJsonObject(getDashboardUri(dashboardTitle))
                .getJSONObject("projectDashboard")
                .getJSONObject("content")
                .getJSONArray("tabs");

        for (int i = 0; i < tabs.length(); i++) {
            if (tabTitle.equals(tabs.getJSONObject(i).getString("title"))) {
                return tabs.getJSONObject(i).getString("identifier");
            }
        }

        throw new RuntimeException("No tab matches with title: " + tabTitle);
    }

    /**
     * Set default dashboard content
     *
     * @param dashboardName
     * @param tabName
     * @return default link
     * @throws JSONException
     * @throws IOException
     */
    public void setDefaultDashboardForUser(String dashboardName, String tabName, String profileUri) throws IOException {
        JSONObject payload = new JSONObject() {{
            put("defaults", new JSONObject() {{
                put("projectUri", format("/gdc/projects/%s", projectId));
                put("dashboardUri", getDashboardUri(dashboardName));
                put("tabId", getTabId(dashboardName, tabName));
            }});
        }};

        executeRequest(initPutRequest(profileUri, payload.toString()), HttpStatus.NO_CONTENT);
    }

    /**
     * Change metric format
     *
     * @param metricUri
     * @param newFormat
     */
    public void changeMetricFormat(String metricUri, String newFormat) throws IOException {
        JSONObject json = getJsonObject(metricUri);
        JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("format", newFormat);
        executeRequest(initPutRequest(metricUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Change metric expression
     *
     * @param metricUri
     * @param newExpression
     */
    public void changeMetricExpression(String metricUri, String newExpression) throws IOException {
        final JSONObject json = getJsonObject(metricUri);
        final JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("expression", newExpression);
        executeRequest(initPutRequest(metricUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Create mandatory user filter object with simple expression '%s IN (%s)'using uri
     *
     * @param mufTitle
     * @param conditions
     * @return mandatory user filter uri
     */
    public String createSimpleMufObjByUri(String mufTitle, Map<String, Collection<String>> conditions) throws
            IOException {
        final String expression = "(%s IN (%s))";
        final List<String> expressions = Lists.newArrayList();

        for (final String attribute : conditions.keySet()) {
            expressions.add(format(expression,
                    format("[%s]", attribute),
                    conditions.get(attribute).stream().map(e -> format("[%s]", e)).collect(joining(","))));
        }

        return createMufObjectByUri(mufTitle, expressions.stream().collect(joining(" AND ")));
    }

    /**
     * Create mandatory user filter object with complex expression using uri
     *
     * @param mufTitle
     * @param expression
     * @return mandatory user filter uri
     */
    public String createMufObjectByUri(String mufTitle, String expression) throws IOException {
        JSONObject payload = new JSONObject() {{
            put("userFilter", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("expression", expression);
                }});
                put("meta", new JSONObject() {{
                    put("category", "userFilter");
                    put("title", mufTitle);
                }});
            }});
        }};

        return getJsonObject(initPostRequest(format(OBJ_LINK, projectId), payload.toString())).getString("uri");
    }

    /**
     * Add mandatory user filter to specific user
     *
     * @param profileUri
     * @param mufURI
     */
    public void addMufToUser(String profileUri, final String mufURI) {
        JSONObject payload = new JSONObject() {{
            put("userFilters", new JSONObject() {{
                put("items", new JSONArray() {{
                    put(new JSONObject() {{
                        put("user", profileUri);
                        put("userFilters", new JSONArray() {{
                            put(mufURI);
                        }});
                    }});
                }});
            }});
        }};

        executeRequest(initPostRequest(format("/gdc/md/%s/userfilters", projectId),
                payload.toString()), HttpStatus.OK);
    }

    public void removeAllMufFromUser(String profileUri) {
        addMufToUser(profileUri, "");
    }

    /**
     * Set drill report as popup for specific dashboard
     *
     * @param dashboardID
     */
    public void setDrillReportTargetAsPopup(final String dashboardID) throws IOException {
        setDrillReportTarget(dashboardID, TARGET_POPUP, null);
    }

    /**
     * Set drill report as export for specific dashboard
     *
     * @param dashboardID
     * @param exportFormat
     */
    public void setDrillReportTargetAsExport(String dashboardID, String exportFormat) throws IOException {
        setDrillReportTarget(dashboardID, TARGET_EXPORT, exportFormat);
    }

    private void setDrillReportTarget(final String dashboardID, final String target, final String exportFormat)
            throws IOException {
        final String dashboardEditModeURI = format(DASHBOARD_EDIT_MODE_LINK, projectId, dashboardID);
        final JSONObject json = getJsonObject(initGetRequest(dashboardEditModeURI));

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

        executeRequest(initPostRequest(dashboardEditModeURI, json.toString()), HttpStatus.OK);
    }
}
