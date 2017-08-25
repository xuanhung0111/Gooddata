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
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.google.common.collect.Lists;
/**
 * REST utilities for old dashboards task
 */
public final class DashboardsRestUtils {

    private static final Logger log = Logger.getLogger(DashboardsRestUtils.class.getName());

    private DashboardsRestUtils() {
    }

    private static final String TARGET_POPUP = "pop-up";
    private static final String TARGET_EXPORT = "export";

    private static final String MUF_LINK = "/gdc/md/%s/userfilters";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";
    private static final String DASHBOARD_EDIT_MODE_LINK = "/gdc/md/%s/obj/%s?mode=edit";
    private static final String VARIABLE_LINK = "/gdc/md/%s/variables/item";
    private static final String GET_VARIABLE_LINK = "/gdc/md/%s/query/prompts";
    private static final String PROJECT_DASHBOARDS_URI = "/gdc/md/%s/query/projectdashboards";

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

    private static final Supplier<String> DASHBOARD_BODY = () -> {
        try {
            return new JSONObject() {{
              put("projectDashboard", new JSONObject() {{
                  put("content", new JSONObject() {{
                      put("rememberFilters", 0);
                      put("tabs", new JSONArray() {{
                          put(new JSONObject() {{
                              put("title", "First Tab");
                              put("items", new JSONArray());
                          }});
                      }});
                      put("filters", new JSONArray());
                  }});
                  put("meta", new JSONObject() {{
                      put("title", "$title");
                      put("locked", 0);
                      put("unlisted", 1);
                  }});
              }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> SET_DEFAULT_DASHBOARD_CONTENT = () -> {
        try {
            return new JSONObject() {{
                put("defaults", new JSONObject() {{
                    put("projectUri", "/gdc/projects/${projectID}");
                    put("dashboardUri", "${dashboardUri}");
                    put("tabId", "${tabId}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization!", e);
        }
    };

    /**
     * Create dashboard
     * 
     * @param restApiClient
     * @param projectId
     * @param title
     * @return new dashboard uri
     */
    public static String createDashboard(final RestApiClient restApiClient, final String projectId, final String title)
            throws JSONException, IOException {
        final String content = DASHBOARD_BODY.get().replace("$title", title);

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("projectDashboard")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    /**
     * Delete dashboard tab
     * 
     * @param restApiClient
     * @param dashboardUri
     * @param tabName
     */
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

    /**
     * Add comment for object
     * 
     * @param restApiClient
     * @param projectId
     * @param comment
     * @param objectId
     * @return comment uri
     */
    public static String addComment(final RestApiClient restApiClient, final String projectId,
            final String comment, final String objectId) throws ParseException, JSONException, IOException {
        final String objectUri = objectId.startsWith("/gdc") ? objectId : format(OBJ_LINK, projectId) + objectId;

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

    /**
     * Create filter variable
     * 
     * @param restApiClient
     * @param projectId
     * @param name           variable name
     * @return attributeUri
     */
    public static String createFilterVariable(final RestApiClient restApiClient, final String projectId,
            final String name, final String attributeUri) throws ParseException, JSONException, IOException {
        final String content = new JSONObject() {{
            put("prompt", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("attribute", attributeUri);
                    put("type", "filter");
                }});
                put("meta", new JSONObject() {{
                    put("tags", "");
                    put("deprecated", 0);
                    put("summary", "");
                    put("isProduction", 1);
                    put("title", name);
                    put("category", "prompt");
                }});
            }});
        }}.toString();

        String variableUri = getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                .getJSONObject("prompt")
                .getJSONObject("meta")
                .getString("uri");

        getJsonObject(restApiClient, restApiClient.newPostMethod(format(VARIABLE_LINK, projectId),
                new JSONObject() {{
                    put("variable", new JSONObject() {{
                        put("expression", "TRUE");
                        put("level", "project");
                        put("prompt", variableUri);
                        put("related", format("/gdc/projects/%s", projectId));
                        put("type", "filter");
                    }});
                }}.toString()));

        return variableUri;
    }

    public static String getVariableUri(RestApiClient restApiClient, String projectId, String title)
            throws JSONException, IOException {
        JSONArray variables = getJsonObject(restApiClient, format(GET_VARIABLE_LINK, projectId))
                .getJSONObject("query")
                .getJSONArray("entries");

        for (int i = 0; i < variables.length(); i++) {
            if (title.equals(variables.getJSONObject(i).getString("title"))) {
                return variables.getJSONObject(i).getString("link");
            }
        }

        throw new RuntimeException("No variable matches with title: " + title);
    }

    public static String getDashboardUri(RestApiClient restApiClient, String projectId, String title)
            throws JSONException, IOException {
        JSONArray dashboards = getJsonObject(restApiClient, format(PROJECT_DASHBOARDS_URI, projectId))
                .getJSONObject("query")
                .getJSONArray("entries");

        for (int i = 0; i < dashboards.length(); i++) {
            if (title.equals(dashboards.getJSONObject(i).getString("title"))) {
                return dashboards.getJSONObject(i).getString("link");
            }
        }

        throw new RuntimeException("No dashboard matches with title: " + title);
    }

    public static String getTabId(RestApiClient restApiClient, String projectId, String dashboardTitle, String tabTitle)
            throws JSONException, IOException {
        JSONArray tabs = getJsonObject(restApiClient, getDashboardUri(restApiClient, projectId, dashboardTitle))
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
     * @param restApiClient
     * @param projectID
     * @param dashboardName
     * @throws tabName
     * @throws JSONException
     * @throws IOException
     * @return default link
     */
    public static void setDefaultDashboardForDomainUser(final RestApiClient restApiClient, final String projectID,
            final String dashboardName, final String tabName) throws ParseException, JSONException, IOException {
        final String defaultUserUri = UserManagementRestUtils
                .getCurrentUserProfile(restApiClient)
                .getJSONObject("links")
                .getString("self")
                .concat("/settings/defaults");
        final String dashboardUri = getDashboardUri(restApiClient, projectID, dashboardName);
        final String tabID = getTabId(restApiClient, projectID, dashboardName, tabName);
        final String content = SET_DEFAULT_DASHBOARD_CONTENT.get()
                .replace("${projectID}", projectID)
                .replace("${dashboardUri}", dashboardUri)
                .replace("${tabId}", tabID);
        executeRequest(restApiClient, restApiClient.newPutMethod(defaultUserUri, content), HttpStatus.NO_CONTENT);
    }

    /**
     * Change metric format
     * 
     * @param restApiClient
     * @param metricUri
     * @param newFormat
     */
    public static void changeMetricFormat(final RestApiClient restApiClient, final String metricUri,
            final String newFormat) throws ParseException, JSONException, IOException {
        final JSONObject json = getJsonObject(restApiClient, metricUri);
        final JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("format", newFormat);
        executeRequest(restApiClient, restApiClient.newPutMethod(metricUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Change metric expression
     * 
     * @param restApiClient
     * @param metricUri
     * @param newExpression
     */
    public static void changeMetricExpression(final RestApiClient restApiClient, final String metricUri,
            final String newExpression) throws ParseException, JSONException, IOException {
        final JSONObject json = getJsonObject(restApiClient, metricUri);
        final JSONObject content = json.getJSONObject("metric").getJSONObject("content");
        content.put("expression", newExpression);
        executeRequest(restApiClient, restApiClient.newPutMethod(metricUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Create mandatory user filter object with simple expression '%s IN (%s)'using uri
     * 
     * @param restApiClient
     * @param projectID
     * @param mufTitle
     * @param conditions
     * @return mandatory user filter uri
     */
    public static String createSimpleMufObjByUri(final RestApiClient restApiClient, final String projectID,
            final String mufTitle, final Map<String, Collection<String>> conditions)
                    throws ParseException, JSONException, IOException {
        final String expression = "(%s IN (%s))";
        final List<String> expressions = Lists.newArrayList();

        for (final String attribute : conditions.keySet()) {
            expressions.add(format(expression,
                    format("[%s]", attribute),
                    conditions.get(attribute).stream().map(e -> format("[%s]", e)).collect(joining(","))));
        }

        return createMufObjectByUri(restApiClient, projectID, mufTitle, expressions.stream().collect(joining(" AND ")));
    }

    /**
     * Create mandatory user filter object with complex expression using uri
     * 
     * @param restApiClient
     * @param projectID
     * @param mufTitle
     * @param expression
     * @return mandatory user filter uri
     */
    public static String createMufObjectByUri(final RestApiClient restApiClient, final String projectID,
            final String mufTitle, final String expression) throws ParseException, JSONException, IOException {
        final String contentBody = MUF_OBJ.get()
                .replace("${MUFExpression}", expression)
                .replace("${MUFTitle}", mufTitle);

        return getJsonObject(restApiClient, restApiClient.newPostMethod(format(OBJ_LINK, projectID), contentBody))
                .getString("uri");
    }

    /**
     * Add mandatory user filter to specific user
     * 
     * @param restApiClient
     * @param projectId
     * @param user
     * @param mufURI
     */
    public static void addMufToUser(final RestApiClient restApiClient, final String projectId, final String user,
            final String mufURI) throws ParseException, IOException {
        final String urserFilter = format(MUF_LINK, projectId);
        final String contentBody = USER_FILTER.get().replace("${email}", user).replace("$MUFExpression", mufURI);

        executeRequest(restApiClient, restApiClient.newPostMethod(urserFilter, contentBody), HttpStatus.OK);
    }

    public static void removeAllMufFromUser(RestApiClient restApiClient, String projectId, String user)
            throws ParseException, IOException {
        addMufToUser(restApiClient, projectId, user, "");
    }

    /**
     * Set drill report as popup for specific dashboard
     * 
     * @param restApiClient
     * @param projectID
     * @param dashboardID
     */
    public static void setDrillReportTargetAsPopup(final RestApiClient restApiClient, final String projectID,
            final String dashboardID) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_POPUP, null);
    }

    /**
     * Set drill report as export for specific dashboard
     * 
     * @param restApiClient
     * @param projectID
     * @param dashboardID
     * @param exportFormat
     */
    public static void setDrillReportTargetAsExport(final RestApiClient restApiClient, final String projectID,
            final String dashboardID, final String exportFormat) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_EXPORT, exportFormat);
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
