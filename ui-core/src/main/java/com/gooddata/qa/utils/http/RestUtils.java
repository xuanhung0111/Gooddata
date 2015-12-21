package com.gooddata.qa.utils.http;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriTemplate;

import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.graphene.enums.project.DWHDriver;
import com.gooddata.qa.graphene.enums.project.ProjectEnvironment;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class RestUtils {

    public static final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";
    private static final String USERS_LINK = "/gdc/projects/%s/users";
    private static final String ROLE_LINK = "/gdc/projects/%s/roles/%s";
    private static final String LDM_LINK = "/gdc/projects/%s/ldm";
    private static final String LDM_MANAGE_LINK = "/gdc/md/%s/ldm/manage2";
    private static final String DASHBOARD_EDIT_MODE_LINK = "/gdc/md/%s/obj/%s?mode=edit";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";
    private static final String MUF_LINK = "/gdc/md/%s/userfilters";
    private static final String DOMAIN_USER_LINK = "/gdc/account/domains/default/users";
    private static final String ADD_USER_CONTENT_BODY;
    private static final String FEATURE_FLAGS_URI = "/gdc/internal/account/profile/featureFlags";
    private static final String FEATURE_FLAGS = "featureFlags";
    private static final UriTemplate FEATURE_FLAGS_PROJECT_URI_TEMPLATE =
            new UriTemplate("/gdc/projects/{projectId}/projectFeatureFlags");
    private static final String PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER = "value";
    private static final String PROJECT_FEATURE_FLAG_KEY_IDENTIFIER = "key";
    private static final String PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER = "featureFlag";
    private static final String GROUPS_URI = "/gdc/internal/usergroups";
    private static final String CREATE_USER_CONTENT_BODY;
    private static final String MAQL_EXECUTION_BODY;
    private static final String MUF_OBJ;
    private static final String USER_FILTER;
    private static final String USER_GROUP_MODIFY_MEMBERS_LINK = "/gdc/userGroups/%s/modifyMembers";
    private static final String PROJECT_MODEL_VIEW_LINK = "/gdc/projects/%s/model/view";
    private static final String CREATE_COMMENT_CONTENT_BODY;
    public static final String TARGET_POPUP = "pop-up";
    public static final String TARGET_EXPORT = "export";
    private static final String PROJECT_LINK = "/gdc/projects/";
    private static final String CREATE_PROJECT_CONTENT_BODY;
    private static final String USER_PROFILE_LINK = "/gdc/account/profile/";
    private static final String UPDATE_USER_INFO_CONTENT_BODY;
    private static final String QUERY_FACTS_URI = "/gdc/md/${projectId}/query/facts";
    private static final String PULL_DATA_LINK = "/gdc/md/%s/etl/pull2";

    static {
        try {
            ADD_USER_CONTENT_BODY = new JSONObject() {{
                put("user", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("userRoles", new JSONArray().put("${userRoles}"));
                        put("status", "ENABLED");
                    }});
                    put("links", new JSONObject() {{
                        put("self", "/gdc/account/profile/${email}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            CREATE_COMMENT_CONTENT_BODY = new JSONObject() {{
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
    }

    static {
        try {
            CREATE_USER_CONTENT_BODY = new JSONObject() {{
                put("accountSetting", new JSONObject() {{
                    put("login", "${userEmail}");
                    put("password", "${userPassword}");
                    put("email", "${userEmail}");
                    put("verifyPassword", "${userPassword}");
                    put("firstName", "FirstName");
                    put("lastName", "LastName");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            MAQL_EXECUTION_BODY = new JSONObject() {{
                put("manage", new JSONObject() {{
                    put("maql", "${maql}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exeception during json object initialization! ", e);
        }
    }

    static {
        try {
            MUF_OBJ = new JSONObject() {{
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
            throw new IllegalStateException(
                    "There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            USER_FILTER = new JSONObject() {{
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
            throw new IllegalStateException(
                    "There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            CREATE_PROJECT_CONTENT_BODY = new JSONObject() {{
                put("project", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("guidedNavigation", 1);
                        put("driver", "${driver}");
                        put("authorizationToken", "${authorizationToken}");
                        put("environment", "${environment}");
                    }});
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                        put("summary", "${summary}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            UPDATE_USER_INFO_CONTENT_BODY = new JSONObject() {{
                put("accountSetting", new JSONObject() {{
                    put("firstName", "${firstName}");
                    put("lastName", "${lastName}");
                    put("old_password", "${old_password}");
                    put("password", "${password}");
                    put("verifyPassword", "${verifyPassword}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exception during json object initialization! ", e);
        }
    }

    private RestUtils() {
    }

    public static String createNewUser(RestApiClient restApiClient, String userEmail,
            String userPassword) throws ParseException, JSONException, IOException {
        String contentBody = CREATE_USER_CONTENT_BODY.replace("${userEmail}", userEmail).replace("${userPassword}",
                userPassword);
        HttpRequestBase postRequest = restApiClient.newPostMethod(DOMAIN_USER_LINK, contentBody);
        String userUri;
        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.CREATED,
                "New user is not created!");
            userUri = new JSONObject(EntityUtils.toString(postResponse.getEntity())).getString("uri");
            System.out.println("New user uri: " + userUri);
            EntityUtils.consumeQuietly(postResponse.getEntity());
        } finally {
            postRequest.releaseConnection();
        }

        return userUri;
    }

    public static void deleteUser(RestApiClient restApiClient, String deletetedUserUri) {
        HttpRequestBase deleteRequest = restApiClient.newDeleteMethod(deletetedUserUri);
        try {
            HttpResponse response = restApiClient.execute(deleteRequest, HttpStatus.OK, "User is not deleted!");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            deleteRequest.releaseConnection();
        }
    }

    public static void addUserToProject(RestApiClient restApiClient, String projectId, String inviteeEmail,
            UserRoles role) throws ParseException, IOException, JSONException {
        String usersUri = String.format(USERS_LINK, projectId);
        String roleUri = String.format(ROLE_LINK, projectId, role.getRoleId());
        String contentBody = ADD_USER_CONTENT_BODY.replace("${userRoles}", roleUri)
                .replace("${email}", inviteeEmail);
        System.out.println("content of json: " + contentBody);
        HttpRequestBase postRequest = restApiClient.newPostMethod(usersUri, contentBody);
        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
            assertFalse(json.getJSONObject("projectUsersUpdateResult").getString("successful").equals("[]"),
                    "User isn't assigned properly into the project");
            System.out.println(format("Successfully assigned user %s to project %s", inviteeEmail, projectId));

            EntityUtils.consumeQuietly(postResponse.getEntity());
        } finally {
            postRequest.releaseConnection();
        }
    }


    public static String addUserGroup(RestApiClient restApiClient, String projectId,final String name)
            throws JSONException, IOException {
        @SuppressWarnings("serial")
        JSONObject payload = new JSONObject(new HashMap<String, Object>() {{
            put("userGroup", new HashMap<String, Object>() {{
                put("content", new HashMap<String, Object>() {{
                    put("name", name);
                    put("project", String.format("/gdc/projects/%s", projectId));
                }});
            }});
        }});

        HttpRequestBase request = restApiClient.newPostMethod(GROUPS_URI, payload.toString());
        HttpResponse response = restApiClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        boolean successful = statusCode == CREATED.value() || statusCode == CONFLICT.value();

        assertTrue(successful, "User group could not be created, got HTTP " + statusCode);
        String userGroupUri =
                new JSONObject(EntityUtils.toString(response.getEntity())).getString("uri");
        System.out.println("New user group uri: " + userGroupUri);

        return userGroupUri;
    }

    public static void deleteUserGroup(RestApiClient restApiClient, String groupUri) {
        HttpRequestBase request = restApiClient.newDeleteMethod(groupUri);
        restApiClient.execute(request, HttpStatus.NO_CONTENT, "User group could not be deleted");
    }

    public static void addUsersToUserGroup(RestApiClient restApiClient, String userGroupId, String... userURIs)
            throws JSONException {
        modifyUsersInUserGroup(restApiClient, userGroupId, "ADD", userURIs);
    }

    public static void removeUsersFromUserGroup(RestApiClient restApiClient, String userGroupId,
            String... userURIs) throws JSONException {
        modifyUsersInUserGroup(restApiClient, userGroupId, "REMOVE", userURIs);
    }

    public static void setUsersInUserGroup(RestApiClient restApiClient, String userGroupId, String... userURIs)
            throws JSONException {
        modifyUsersInUserGroup(restApiClient, userGroupId, "SET", userURIs);
    }

    private static void modifyUsersInUserGroup(RestApiClient restApiClient, String userGroupId,
            String operation, String... userURIs) throws JSONException {
        HttpRequestBase postRequest = null;
        try {
            String modifyMemberUri = String.format(USER_GROUP_MODIFY_MEMBERS_LINK, userGroupId);
            postRequest = restApiClient.newPostMethod(modifyMemberUri,
                    buildModifyMembersContent(operation, userURIs));
            restApiClient.execute(postRequest, HttpStatus.NO_CONTENT, "Modify Users on User Group failed");
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
        }
    }

    private static String buildModifyMembersContent(final String operation, final String... userURIs)
            throws JSONException {
        return new JSONObject() {{
            put("modifyMembers", new JSONObject() {{
                put("operation", operation);
                put("items", new JSONArray(userURIs));
            }});
        }}.toString();
    }

    public static String getLDMImageURI(RestApiClient restApiClient, String projectId, String host)
            throws ParseException, IOException, JSONException {
        String ldmUri = String.format(LDM_LINK, projectId);
        HttpRequestBase getRequest = restApiClient.newGetMethod(ldmUri);
        HttpResponse getResponse = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code");
        JSONObject json = new JSONObject(EntityUtils.toString(getResponse.getEntity()));
        String uri = json.getString("uri");
        // TODO fix on resource rather then here
        if (uri.matches("^/gdc_img.*")) {
            uri = "https://" + host + uri;
        }
        return uri;
    }

    public static boolean isValidImage(RestApiClient restApiClient, WebElement ele) {
        String imgSrc = ele.getAttribute("src");
        if (imgSrc == null) return false;

        HttpResponse response = restApiClient.execute(restApiClient.newGetMethod(imgSrc));
        return response.getStatusLine().getStatusCode() == 200;
    }

    public static void executePostRequest(final RestApiClient restApiClient, final String uri, String content) {
        HttpRequestBase request = restApiClient.newPostMethod(uri, content);
        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    public static JSONObject getFacts(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        String uri = QUERY_FACTS_URI.replace("${projectId}", projectId);
        HttpRequestBase request = restApiClient.newGetMethod(uri);
        HttpResponse response;
        try {
            response = restApiClient.execute(request, HttpStatus.OK, "Invalid status code");
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        } finally {
            request.releaseConnection();
        }
    }

    public static String getFactUriByName(final RestApiClient restApiClient, final String projectId, final String name)
            throws JSONException, IOException {
        JSONObject facts = getFacts(restApiClient, projectId);
        JSONArray factEntries = facts.getJSONObject("query").getJSONArray("entries");

        for (int i = 0, n = factEntries.length(); i < n; i++) {
            JSONObject fact = factEntries.getJSONObject(i);
            if (fact.getString("title").equals(name)) {
                return fact.getString("link");
            }
        }

        return null;
    }

    public static JSONObject getJSONObjectFrom(final RestApiClient restApiClient, final String uri)
            throws IOException, JSONException {
        return getJSONObjectFrom(restApiClient, uri, HttpStatus.OK);
    }

    public static JSONObject getJSONObjectFrom(final RestApiClient restApiClient, final String uri,
            HttpStatus expectedStatusCode) throws IOException, JSONException {
        return new JSONObject(getResource(restApiClient, uri, expectedStatusCode));
    }

    public static void setFeatureFlags(final RestApiClient restApiClient,
            final FeatureFlagOption... featureFlagOptions) throws IOException, JSONException {
        JSONObject json = getJSONObjectFrom(restApiClient, FEATURE_FLAGS_URI);
        for (FeatureFlagOption featureFlagOption : featureFlagOptions) {
            json.getJSONObject(FEATURE_FLAGS).put(featureFlagOption.getFeatureFlagName(),
                    featureFlagOption.isOn());
        }
        HttpRequestBase putRequest = restApiClient.newPutMethod(FEATURE_FLAGS_URI, json.toString());
        restApiClient.execute(putRequest, HttpStatus.NO_CONTENT, "Invalid status code");
    }

    public static boolean isFeatureFlagEnabled(final RestApiClient restApiClient, final String featureFlagName)
            throws IOException, JSONException {
        JSONObject flags = getJSONObjectFrom(restApiClient, FEATURE_FLAGS_URI).getJSONObject(FEATURE_FLAGS);
        if (!flags.has(featureFlagName))
            return false;
        return Boolean.getBoolean(flags.getString(featureFlagName));
    }

    public static void setFeatureFlagsToProject(final RestApiClient restApiClient, final String projectId,
            final FeatureFlagOption... featureFlagOptions) throws JSONException {
        final URI projectFeatureFlagsUri = FEATURE_FLAGS_PROJECT_URI_TEMPLATE.expand(projectId);
        JSONObject featureFlagObject;
        JSONObject valuesJsonObject;

        for (FeatureFlagOption featureFlagOption : featureFlagOptions) {
            featureFlagObject = new JSONObject();
            valuesJsonObject = new JSONObject();

            valuesJsonObject.put(PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER, featureFlagOption.isOn());
            valuesJsonObject.put(PROJECT_FEATURE_FLAG_KEY_IDENTIFIER, featureFlagOption.getFeatureFlagName());
            featureFlagObject.put(PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER, valuesJsonObject);

            final HttpPost postRequest = restApiClient.newPostMethod(projectFeatureFlagsUri.toString(),
                    featureFlagObject.toString());
            try {
               HttpResponse response =
                       restApiClient.execute(postRequest, HttpStatus.CREATED, "Invalid status code");
               EntityUtils.consumeQuietly(response.getEntity());
            } finally {
                postRequest.releaseConnection();
            }
        }
    }

    public static String createMUFObj(final RestApiClient restApiClient, String projectID, String mufTitle,
            Map<String, Collection<String>> conditions) throws IOException, JSONException {
        String mdObjURI = format(OBJ_LINK, projectID);
        String MUFExpressions = buildFilterExpression(projectID, conditions);
        String contentBody = MUF_OBJ.replace("${MUFExpression}", MUFExpressions).replace("${MUFTitle}", mufTitle);
        HttpRequestBase postRequest = restApiClient.newPostMethod(mdObjURI, contentBody);

        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "New MUF is not created!");
            String result = EntityUtils.toString(postResponse.getEntity());
            JSONObject json = new JSONObject(result);
            EntityUtils.consumeQuietly(postResponse.getEntity());
            return json.getString("uri");
        } finally {
            postRequest.releaseConnection();
        }
    }

    private static String buildFilterExpression(final String projectID, Map<String,
            Collection<String>> conditions) {
      //syntax: "([<Attribute_URI_1>] IN ([<element_URI_1>], [element_URI_2], [...] )) AND
      //([<Attribute_URI_2>] IN ([<element_URI_1>], [element_URI_2], [...]))";
        List<String> expressions = Lists.newArrayList();
        String attributeURI = "[/gdc/md/%s/obj/%s]";
        final String elementURI = "[/gdc/md/%s/obj/%s/elements?id=%s]";
        String expression = "(%s IN (%s))";
        for (final String attributeID : conditions.keySet()) {
            expressions.add(format(expression,
                    format(attributeURI, projectID, attributeID),
                    Joiner.on(",").join(Collections2.transform(conditions.get(attributeID),
                            new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    return format(elementURI, projectID, attributeID, input);
                                }
                            }))
            ));
        }
        return Joiner.on(" AND ").join(expressions);
    }

    public static void addMUFToUser(final RestApiClient restApiClient, String projectURI, String user,
            String mufURI) {
        String urserFilter = format(MUF_LINK, projectURI);
        String contentBody = USER_FILTER.replace("${email}", user).replace("$MUFExpression",
                mufURI);
        HttpRequestBase postRequest = restApiClient.newPostMethod(urserFilter, contentBody);

        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "the MUF is not applied");
            EntityUtils.consumeQuietly(postResponse.getEntity());
        } finally {
            postRequest.releaseConnection();
        }
    }

    public static void setDrillReportTargetAsPopup(final RestApiClient restApiClient, String projectID,
            String dashboardID) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_POPUP, null);
    }

    public static void setDrillReportTargetAsExport(final RestApiClient restApiClient, String projectID,
            String dashboardID, String exportFormat) throws JSONException, IOException {
        setDrillReportTarget(restApiClient, projectID, dashboardID, TARGET_EXPORT, exportFormat);
    }

    private static void setDrillReportTarget(final RestApiClient restApiClient, String projectID,
            String dashboardID, String target, String exportFormat) throws JSONException, IOException {
        HttpRequestBase getRequest = null;
        HttpRequestBase postRequest = null;
        try {
            String dashboardEditModeURI = String.format(DASHBOARD_EDIT_MODE_LINK, projectID, dashboardID);
            getRequest = restApiClient.newGetMethod(dashboardEditModeURI);
            HttpResponse response = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONObject drills = json.getJSONObject("projectDashboard").getJSONObject("content").
                    getJSONArray("tabs").getJSONObject(0).getJSONArray("items").getJSONObject(0).
                    getJSONObject("reportItem").getJSONArray("drills").getJSONObject(0);
            drills.put("target", target);
            if (TARGET_POPUP.equals(target)) {
                drills.remove("export");
            } else if (TARGET_EXPORT.equals(target)) {
                JSONObject exportOptions = new JSONObject();
                exportOptions.put("format", exportFormat);
                drills.put("export", exportOptions );
            }
            postRequest = restApiClient.newPostMethod(dashboardEditModeURI, json.toString());
            response = restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            if (postRequest !=null) {
                postRequest.releaseConnection();
            }
        }
    }

    public static void changeMetricFormat(final RestApiClient restApiClient, String metricUri, String newFormat)
            throws ParseException, JSONException, IOException {
        HttpRequestBase getRequest = null;
        HttpRequestBase putRequest = null;

        try {
            getRequest = restApiClient.newGetMethod(metricUri);
            HttpResponse response = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONObject content = json.getJSONObject("metric").getJSONObject("content");
            content.put("format", newFormat);
            EntityUtils.consumeQuietly(response.getEntity());
            putRequest = restApiClient.newPutMethod(metricUri, json.toString());
            response = restApiClient.execute(putRequest, HttpStatus.OK, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            if (putRequest != null) {
                putRequest.releaseConnection();
            }
        }

    }

    public static void changeMetricExpression(final RestApiClient restApiClient, String metricUri,
            String newExpression) throws ParseException, JSONException, IOException {
        HttpRequestBase getRequest = null;
        HttpRequestBase putRequest = null;

        try {
            getRequest = restApiClient.newGetMethod(metricUri);
            HttpResponse response = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONObject content = json.getJSONObject("metric").getJSONObject("content");
            content.put("expression", newExpression);
            EntityUtils.consumeQuietly(response.getEntity());
            putRequest = restApiClient.newPutMethod(metricUri, json.toString());
            response = restApiClient.execute(putRequest, HttpStatus.OK, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            if (putRequest != null) {
                putRequest.releaseConnection();
            }
        }
    }

    public static String executeMAQL(RestApiClient restApiClient, String projectId, String maql)
            throws ParseException, JSONException, IOException {
        String contentBody = MAQL_EXECUTION_BODY.replace("${maql}", maql);
        HttpRequestBase postRequest =
                restApiClient.newPostMethod(String.format(LDM_MANAGE_LINK, projectId), contentBody);

        try {
            HttpResponse postResponse =
                    restApiClient.execute(postRequest, HttpStatus.OK,
                            "LDM is not updated successful!");
            JSONObject responseBody =
                    new JSONObject(EntityUtils.toString(postResponse.getEntity()));
            String pollingUri =
                    responseBody.getJSONArray("entries").getJSONObject(0).get("link").toString();

            EntityUtils.consumeQuietly(postResponse.getEntity());

            return pollingUri;
        } finally {
            postRequest.releaseConnection();
        }
    }

    public static void enableFeatureFlagInProject(RestApiClient restApiClient, String projectId,
            ProjectFeatureFlags featureFlag) throws JSONException {
        setFeatureFlagsToProject(restApiClient, projectId,
                new FeatureFlagOption(featureFlag.getFlagName(), true));
    }

    public static void verifyValidLink(RestApiClient restApiClient, String link) {
        HttpRequestBase getRequest = restApiClient.newGetMethod(link);
        try {
            HttpResponse getResponse = restApiClient.execute(getRequest);
            assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value(),
                    "Invalid link: " + link);

            EntityUtils.consumeQuietly(getResponse.getEntity());
        } finally {
            getRequest.releaseConnection();
        }
    }

    public static String addComment(RestApiClient restApiClient, String projectId, String comment,
            String objectId) throws ParseException, JSONException, IOException {
        String objectUri = format(OBJ_LINK, projectId) + objectId;

        System.out.println("Verify object id: " + objectUri);
        getJSONObjectFrom(restApiClient, objectUri);

        String content = CREATE_COMMENT_CONTENT_BODY.replace("${title}", comment).replace("#{related}", objectUri);
        HttpRequestBase postRequest = restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId),
                content);

        try {
            HttpResponse postResponse = restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
            HttpEntity entity = postResponse.getEntity();
            String uri =  new JSONObject(EntityUtils.toString(entity)).getJSONObject("comment")
                    .getJSONObject("meta").getString("uri");
            EntityUtils.consumeQuietly(entity);
            return uri;
        } finally {
            postRequest.releaseConnection();
        }
    }

    /**
     * DTO to hold feature flag option (name and status - on/off)
     */
    public static class FeatureFlagOption {

        private final String featureFlagName;
        private final boolean on;

        private FeatureFlagOption(String featureFlagName, boolean on) {
            this.featureFlagName = featureFlagName;
            this.on = on;
        }

        public String getFeatureFlagName() {
            return featureFlagName;
        }

        public boolean isOn() {
            return on;
        }

        public static FeatureFlagOption createFeatureClassOption(final String featureFlagName, final boolean on) {
            return new FeatureFlagOption(featureFlagName, on);
        }
    }

    public static int waitingForAsyncTask(RestApiClient restApiClient, String pollingUri) {
        while (getAsyncTaskStatusCode(restApiClient, pollingUri) == HttpStatus.ACCEPTED.value()) {
            System.out.println("Async task is running...");
            sleepTightInSeconds(2);
        }

        return getAsyncTaskStatusCode(restApiClient, pollingUri);
    }

    private static int getAsyncTaskStatusCode(RestApiClient restApiClient, String pollingUri) {
        HttpRequestBase getRequest = restApiClient.newGetMethod(pollingUri);
        try {
            HttpResponse getResponse = restApiClient.execute(getRequest);

            int responseStatusCode = getResponse.getStatusLine().getStatusCode();
            System.out.println("Reponse status: " + responseStatusCode);

            EntityUtils.consumeQuietly(getResponse.getEntity());

            return responseStatusCode;
        } finally {
            getRequest.releaseConnection();
        }
    }

    public static String getAsyncTaskStatus(RestApiClient restApiClient, String pollingUri) {
        HttpRequestBase getRequest = restApiClient.newGetMethod(pollingUri);
        HttpResponse getResponse;
        String status = "";
        try {
            getResponse = restApiClient.execute(getRequest);
            if (getResponse.getEntity() != null) {
                String responseEntity = EntityUtils.toString(getResponse.getEntity());
                JSONObject taskObject = new JSONObject(responseEntity);
                String key = "";
                if (!taskObject.isNull("wTaskStatus"))
                    key = "wTaskStatus";
                else if (!taskObject.isNull("taskState"))
                    key = "taskState";
                else
                    throw new IllegalStateException(
                            "The status object is not existing! The current response is: "
                                    + taskObject.toString());

                status = taskObject.getJSONObject(key).getString("status");
            }
            System.out.println("Async task status is: " + status);

            EntityUtils.consumeQuietly(getResponse.getEntity());
        } catch (Exception e) {
            throw new IllegalStateException("There is an exeption when polling state!", e);
        } finally {
            getRequest.releaseConnection();
        }

        return status;
    }

    public static String getResourceWithCustomAcceptHeader(RestApiClient restApiClient, String uri,
            HttpStatus expectedStatusCode, String acceptHeader) {
        return getResource(restApiClient, uri, expectedStatusCode, acceptHeader);
    }

    public static String getResource(RestApiClient restApiClient, String uri,
            HttpStatus expectedStatusCode) {
        return getResource(restApiClient, uri, expectedStatusCode,
                ContentType.APPLICATION_JSON.getMimeType());
    }

    private static String getResource(RestApiClient restApiClient, String uri,
            HttpStatus expectedStatusCode, String acceptHeader) {
        HttpRequestBase getRequest = restApiClient.newGetMethod(uri);
        if (!acceptHeader.equals(ContentType.APPLICATION_JSON.getMimeType()))
            getRequest.setHeader("Accept", acceptHeader);
        HttpResponse getResponse;
        String response = "";
        try {
            getResponse = restApiClient.execute(getRequest, expectedStatusCode, "Invalid status code");
            HttpEntity entity = getResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
            EntityUtils.consumeQuietly(getResponse.getEntity());
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when getting API resource!", e);
        } finally {
            getRequest.releaseConnection();
        }

        return response;
    }

    public static JSONObject getProjectModelView(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        JSONObject modelViewObject = null;
        String pollingUri =
                getPollingUriFrom(restApiClient, projectId, format(PROJECT_MODEL_VIEW_LINK, projectId));
        HttpRequestBase request = restApiClient.newGetMethod(pollingUri);
        try {
            int status;
            HttpResponse getResponse;
            do {
                getResponse = restApiClient.execute(request);
                status = getResponse.getStatusLine().getStatusCode();
                System.out.println("Current polling status: " + status);
                if (HttpStatus.valueOf(status) == HttpStatus.OK) {
                    modelViewObject = new JSONObject(EntityUtils.toString(getResponse.getEntity()));
                    break;
                }
                EntityUtils.consumeQuietly(getResponse.getEntity());
            } while(status == HttpStatus.ACCEPTED.value());

        } finally {
            request.releaseConnection();
        }
        return modelViewObject;
    }

    public static JSONObject getDatasetModelView(RestApiClient restApiClient, String projectId, String dataset)
            throws ParseException, JSONException, IOException {
        JSONObject projectModelView = getProjectModelView(restApiClient, projectId);

        System.out.println("Get dataset model view...");
        JSONArray datasets = projectModelView.getJSONObject("projectModelView").getJSONObject("model")
                .getJSONObject("projectModel").getJSONArray("datasets");

        for (int i = 0; i < datasets.length(); i++) {
            JSONObject object = datasets.getJSONObject(i).getJSONObject("dataset");
            if (!dataset.equals(object.getString("title")))
                continue;
            return object;
        }
        throw new NoSuchElementException("Dataset json object not found!");
    }

    public static <T> T getDatasetElementFromModelView(RestApiClient restApiClient, String projectId,
            String dataset, DatasetElements element, Class<T> returnType) throws ParseException, JSONException,
            IOException {
        Object object =
                getDatasetModelView(restApiClient, projectId, dataset).get(element.toString().toLowerCase());
        System.out.println(format("Get %s of dataset %s...", element, dataset));
        if(returnType.isInstance(object)) {
            return returnType.cast(object);
        }
        throw new NoSuchElementException("Dataset element not found!");
    }

    public static String updateFirstNameOfCurrentAccount(RestApiClient restApiClient, String newFirstName)
            throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newGetMethod("/gdc/account/profile/current");
        String currentProfileUri;
        JSONObject currentProfile;
        String oldFirstName;

        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Invalid status code");
            currentProfile = new JSONObject(EntityUtils.toString(response.getEntity()))
                .getJSONObject("accountSetting");
            oldFirstName = currentProfile.getString("firstName");
            currentProfileUri = currentProfile.getJSONObject("links").getString("self");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }

        request = restApiClient.newPutMethod(currentProfileUri,
                new JSONObject().put("accountSetting",
                        new JSONObject().put("country", currentProfile.get("country"))
                        .put("phoneNumber", currentProfile.get("phoneNumber"))
                        .put("timezone", currentProfile.get("timezone"))
                        .put("companyName", currentProfile.get("companyName"))
                        .put("lastName", currentProfile.get("lastName"))
                        .put("firstName", newFirstName)
                ).toString());

        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
        return oldFirstName;
    }

    private static String getPollingUriFrom(RestApiClient restApiClient, String projectId, String uri)
            throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newGetMethod(uri);
        String pollUri = "";
        try {
            HttpResponse response = restApiClient.execute(request);
            pollUri =  new JSONObject(EntityUtils.toString(response.getEntity()))
                .getJSONObject("asyncTask").getJSONObject("link").getString("poll");

            EntityUtils.consumeQuietly(response.getEntity());
            System.out.println("Poll link: " + pollUri);
        } finally {
            request.releaseConnection();
        }
        return pollUri;
    }

    public static void deleteObject(RestApiClient restApiClient, String uri) {
        HttpRequestBase request = restApiClient.newDeleteMethod(uri);
        HttpResponse response = restApiClient.execute(request);
        EntityUtils.consumeQuietly(response.getEntity());
    }

    public static String getLoginFromProfileUri(RestApiClient restApiClient, String profileUri)
            throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newGetMethod("/gdc/account/profile/" + profileUri);
        String login = "";
        try {
            HttpResponse response = restApiClient.execute(request);
            login = new JSONObject(EntityUtils.toString(response.getEntity())).
                    getJSONObject("accountSetting").getString("login");
        } finally {
            request.releaseConnection();
        }
        return login;
    }

    public static String createBlankProject(RestApiClient restApiClient, String title, String summary,
            String authorizationToken, DWHDriver dwhDriver, ProjectEnvironment enviroment)
                    throws ParseException, JSONException, IOException {
        return createProject(restApiClient, title, summary, null, authorizationToken, dwhDriver, enviroment);
    }

    public static String createProject(RestApiClient restApiClient, String title, String summary,
            String template, String authorizationToken, DWHDriver dwhDriver, ProjectEnvironment enviroment)
                    throws ParseException, JSONException, IOException {
        String contentBody = CREATE_PROJECT_CONTENT_BODY;

        if(nonNull(template)){
            JSONObject contentBodyJsonObject = new JSONObject(contentBody);
            contentBodyJsonObject.getJSONObject("project")
                    .getJSONObject("meta")
                    .put("projectTemplate", template);
            contentBody = contentBodyJsonObject.toString();
        }

        contentBody = contentBody.replace("${title}", title)
                .replace("${summary}", summary)
                .replace("${driver}", dwhDriver.getValue())
                .replace("${authorizationToken}", authorizationToken)
                .replace("${environment}", enviroment.toString());

        HttpRequestBase request = restApiClient.newPostMethod(PROJECT_LINK, contentBody);
        String projectId = null;
        try {
            HttpResponse response = restApiClient.execute(request);
            String projectUri = new JSONObject(EntityUtils.toString(response.getEntity())).getString("uri");
            projectId = projectUri.substring(projectUri.lastIndexOf("/") + 1);
        } finally {
            request.releaseConnection();
        }

        if (!waitForProjectCreated(restApiClient, projectId)) {
            throw new RuntimeException("Cannot create project!");
        }
        return projectId;
    }

    public static void deleteProject(RestApiClient restApiClient, String projectId)
                    throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newDeleteMethod(PROJECT_LINK + projectId);
        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Project is not deleted!");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    public static void deleteDashboardTab(RestApiClient restApiClient, String dashboardUri, String tabName)
            throws IOException, JSONException {
        JSONObject dashboard = getJSONObjectFrom(restApiClient, dashboardUri);
        JSONArray tabs = dashboard.getJSONObject("projectDashboard").getJSONObject("content").getJSONArray("tabs");

        JSONArray newTabs = new JSONArray();
        for (int i = 0, n = tabs.length(); i < n; i++) {
            JSONObject tab = tabs.getJSONObject(i);
            if (!tabName.equals(tab.getString("title"))) {
                newTabs.put(tab);
            }
        }

        dashboard.getJSONObject("projectDashboard").getJSONObject("content").put("tabs", newTabs);

        HttpRequestBase postRequest = restApiClient.newPostMethod(dashboardUri + "?mode=edit",
                dashboard.toString());

        try {
            restApiClient.execute(postRequest, HttpStatus.OK, "Invalid status code");
        } finally {
            postRequest.releaseConnection();
        }
    }

    public static JSONObject getCurrentUserProfile(RestApiClient restApiClient)
            throws JSONException, IOException {
        return getJSONObjectFrom(restApiClient, USER_PROFILE_LINK + "current")
                .getJSONObject("accountSetting");
    }

    public static void updateCurrentUserPassword(RestApiClient restApiClient, String oldPassword,
            String newPassword) throws ParseException, JSONException, IOException {
        JSONObject userProfile = getCurrentUserProfile(restApiClient);
        String userProfileUri = userProfile.getJSONObject("links").getString("self");
        String content = UPDATE_USER_INFO_CONTENT_BODY
                .replace("${firstName}", userProfile.get("firstName").toString())
                .replace("${lastName}", userProfile.get("lastName").toString())
                .replace("${old_password}", oldPassword)
                .replace("${password}", newPassword)
                .replace("${verifyPassword}", newPassword);
        HttpRequestBase request = restApiClient.newPutMethod(userProfileUri, content);
        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    public static JSONObject getUserProfileByEmail(RestApiClient restApiClient, String email)
            throws ParseException, JSONException, IOException {
        String userUri = DOMAIN_USER_LINK + "?login=" + email.replace("@", "%40");
        try {
            return getJSONObjectFrom(restApiClient, userUri).getJSONObject("accountSettings")
                    .getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("accountSetting");
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean postEtlPullIntegration(RestApiClient restApiClient, String projectId, String integrationEntry)
            throws JSONException, ParseException, IOException {

        HttpRequestBase request = restApiClient.newPostMethod(format(PULL_DATA_LINK, projectId),
                new JSONObject().put("pullIntegration", integrationEntry).toString());

        try {
            HttpResponse response = restApiClient.execute(request);
            String pullingUri = new JSONObject(EntityUtils.toString(response.getEntity()))
                    .getJSONObject("pull2Task")
                    .getJSONObject("links")
                    .getString("poll");
            EntityUtils.consumeQuietly(response.getEntity());

            String pullingStatus = "";

            while (true) {
                pullingStatus = getEtlPullingStatus(restApiClient, pullingUri);
                System.out.println("The pulling status is: " + pullingStatus);

                if (!pullingStatus.equals("RUNNING"))
                    break;
                sleepTightInSeconds(5);
            }

            return "OK".equals(pullingStatus);
        } finally {
            request.releaseConnection();
        }
    }

    public static String getWebDavUrl(RestApiClient restApiClient) throws IOException, JSONException {
        JSONArray links = getJSONObjectFrom(restApiClient, "/gdc", HttpStatus.OK).getJSONObject("about")
                .getJSONArray("links");

        JSONObject link;
        for (int i = 0, n = links.length(); i < n; i++) {
            link = links.getJSONObject(i);
            if (!"user-uploads".equals(link.getString("title"))) continue;
            return link.getString("link");
        }

        return "";
    }

    private static String getProjectState(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        return getProjectInfo(restApiClient, projectId).getJSONObject("project")
                .getJSONObject("content")
                .getString("state");
    }

    private static JSONObject getProjectInfo(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newGetMethod(PROJECT_LINK + projectId);
        try {
            HttpResponse response = restApiClient.execute(request, HttpStatus.OK, "Cannot get project info!");
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        } finally {
            request.releaseConnection();
        }
    }

    private static boolean waitForProjectCreated(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        String currentStatus = "";

        // checking in 10 minutes
        for (int i = 0; i < 120; i++) {
            currentStatus = getProjectState(restApiClient, projectId);
            System.out.println("Current status: " + currentStatus);
            if (Stream.of("ERROR", "DELETED", "CANCELED", "ENABLED", "DISABLED", "ARCHIVED")
                    .anyMatch(currentStatus::equals)) break;
            sleepTightInSeconds(5);
        }

        return "ENABLED".equals(currentStatus);
    }

    private static String getEtlPullingStatus(RestApiClient restApiClient, String pullingUri)
            throws ParseException, JSONException, IOException {
        HttpRequestBase request = restApiClient.newGetMethod(pullingUri);

        try {
            HttpResponse response = restApiClient.execute(request);

            JSONObject taskStatus = new JSONObject(EntityUtils.toString(response.getEntity()))
                .getJSONObject("wTaskStatus");

            System.out.println("Task status: \n" + taskStatus + "\n");
            return taskStatus.getString("status");

        } finally {
            request.releaseConnection();
        }
    }
}
