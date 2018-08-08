package com.gooddata.qa.utils.http.dashboards;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
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
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class DashboardRestRequest extends CommonRestRequest {
    private static final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";
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
     * Lock/unlock metric
     * note: if there are dashboards or reports contain this metric
     * metric cannot unlock
     */
    public void setLockedMetric(String metricTitle, boolean isLocked) throws JSONException, IOException {
        String metricUri = getMetricByTitle(metricTitle).getUri();
        final JSONObject json = getJsonObject(metricUri);
        json.getJSONObject("metric").getJSONObject("meta").put("locked", BooleanUtils.toInteger(isLocked));
        executeRequest(RestRequest.initPutRequest(metricUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Set dashboard as private or public
     *
     * @param title
     * @param isPrivate
     * @throws JSONException
     * @throws IOException
     */
    public void setPrivateDashboard(String title, boolean isPrivate) throws JSONException, IOException {
        String dashboardUri = getDashboardUri(title);
        final JSONObject json = getJsonObject(dashboardUri);
        json.getJSONObject("projectDashboard").getJSONObject("meta").put("unlisted", BooleanUtils.toInteger(isPrivate));
        executeRequest(RestRequest.initPutRequest(dashboardUri, json.toString()), HttpStatus.OK);
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

    /**
     *
     * @param dashboard
     * @param metric
     * @throws IOException
     */
    public void addUseAvailableMetricToDashboardFilters(final String dashboard, final String metric)
            throws IOException {
        final String dashboardUri = getDashboardUri(dashboard);
        final JSONObject dashboarObject = getJsonObject(initGetRequest(dashboardUri));
        final JSONArray filters = dashboarObject.getJSONObject("projectDashboard").getJSONObject("content")
                .getJSONArray("filters");
        JSONObject currentObj;

        for (int i = 0, n = filters.length(); i < n; i++) {
            currentObj = filters.getJSONObject(i).getJSONObject("filterItemContent");
            if (currentObj.has("useAvailable")) {
                continue;
            }
            currentObj.put("useAvailable", new JSONObject() {{
                put("metrics", new JSONArray().put(getMetricByTitle(metric).getUri()));
            }});
        }

        executeRequest(initPostRequest(dashboardUri + "?mode=edit", dashboarObject.toString()), HttpStatus.OK);
    }

    /**
     * To set a new format date. Have to change default of element which same request title to 1.
     * And make sure that no another element's default equal 1.
     * Example: To change from short label(Aug 1991) with default 1 to long label(August 1991) with default 2
     * Firstly, change default of long label to 1. Then, change default of short label to 2.
     * To avoid case as 2 definitions is similar.
     *
     * @param attribute
     * @param requestedFormatLabel
     * @throws IOException
     */
    public void setDefaultFormatDateFilter(String attribute, FormatLabel requestedFormatLabel)
            throws IOException {
        final JSONArray jsonArray = getJsonObject(getAttributeByTitle(attribute).getUri())
                .getJSONObject("attribute").getJSONObject("content").getJSONArray("displayForms");
        int requestedFormat = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject meta = jsonArray.getJSONObject(i).getJSONObject("meta");
            if (meta.getString("title").contains(requestedFormatLabel.getValue())) {
                requestedFormat = jsonArray.getJSONObject(i).getJSONObject("content").getInt("default");
                final String uri = meta.getString("uri");
                setDefault(uri, 1);
                break;
            }
        }
        final String uri = jsonArray.getJSONObject(0).getJSONObject("meta").getString("uri");
        setDefault(uri, requestedFormat);
    }

    private void setDefault(String uri, int requestedFormat) throws IOException {
        JSONObject json = getJsonObject(uri);
        json.getJSONObject("attributeDisplayForm").getJSONObject("content").put("default", requestedFormat);
        executeRequest(initPutRequest(uri, json.toString()), HttpStatus.OK);
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

    public enum FormatLabel {
        SHORT_LABEL("Short"),
        LONG_LABEL("Long"),
        NUMBER_LABEL("Number");

        private String value;

        FormatLabel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
