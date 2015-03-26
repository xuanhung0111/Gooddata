package com.gooddata.qa.utils.http;

import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.*;

public class RestUtils {

    private static final String USERS_LINK = "/gdc/projects/%s/users";
    private static final String ROLE_LINK = "/gdc/projects/%s/roles/%s";
    private static final String LDM_LINK = "/gdc/projects/%s/ldm";
    private static final String DASHBOARD_EDIT_MODE_LINK = "/gdc/md/%s/obj/%s?mode=edit";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";
    private static final String MUF_LINK = "/gdc/md/%s/userfilters";
    private static final String DOMAIN_USER_LINK = "/gdc/account/domains/default/users";
    private static final String addUserContentBody = "{\"user\":{\"content\":{\"userRoles\":[\"%s\"],\"status\":\"ENABLED\"},\"links\":{\"self\":\"%s\"}}}";
    private static final String FEATURE_FLAGS_URI = "/gdc/internal/account/profile/featureFlags";
    private static final String FEATURE_FLAGS = "featureFlags";
    private static final UriTemplate FEATURE_FLAGS_PROJECT_URI_TEMPLATE = new UriTemplate("/gdc/projects/{projectId}/projectFeatureFlags");
    private static final String PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER = "value";
    private static final String PROJECT_FEATURE_FLAG_KEY_IDENTIFIER = "key";
    private static final String PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER = "featureFlag";
    private static final String GROUPS_URI = "/gdc/internal/usergroups";
    private static final String CREATE_USER_CONTENT_BODY;
    private static final String MUF_OBJ;
    private static final String USER_FILTER;
    
    public static final String TARGET_POPUP = "pop-up";
    public static final String TARGET_EXPORT = "export";
    
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
                    "There is an exeception during json object initialization! ", e);
        }
    }
    
    static {
        try {
            USER_FILTER = new JSONObject() {{
                put("userFilters", new JSONObject() {{
                    put("items", new JSONArray() {{
                        put(new JSONObject() {{
                            put("user", "$UserProfileURI");
                            put("userFilters", new JSONArray() {{
                                put("$MUFExpression");
                            }});
                        }});
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exeception during json object initialization! ", e);
        }
    }

    private RestUtils() {
    }

    public static String createNewUser(RestApiClient restApiClient, String userEmail,
            String userPassword) throws ParseException, JSONException, IOException {
        String contentBody =
                CREATE_USER_CONTENT_BODY.replace("${userEmail}", userEmail).replace("${userPassword}",
                        userPassword);
        HttpRequestBase postRequest = restApiClient.newPostMethod(DOMAIN_USER_LINK, contentBody);
        HttpResponse postReponse = restApiClient.execute(postRequest);
        assertEquals(postReponse.getStatusLine().getStatusCode(), HttpStatus.CREATED.value(),
                "New user is not created!");
        String userUri =
                new JSONObject(EntityUtils.toString(postReponse.getEntity())).getString("uri");
        System.out.println("New user uri: " + userUri);

        return userUri;
    }

    public static void deleteUser(RestApiClient restApiClient, String deletetedUserUri) {
        HttpRequestBase deleteRequest = restApiClient.newDeleteMethod(deletetedUserUri);
        HttpResponse deleteReponse = restApiClient.execute(deleteRequest);
        assertEquals(deleteReponse.getStatusLine().getStatusCode(), HttpStatus.OK.value(),
                "User is not deleted!");
    }

    public static void addUserToProject(String host, String projectId, String domainUser,
                                        String domainPassword, String inviteeProfile, UserRoles role) throws ParseException, IOException, JSONException {

        RestApiClient restApiClient = new RestApiClient(host, domainUser, domainPassword, true, false);
        String usersUri = String.format(USERS_LINK, projectId);
        String roleUri = String.format(ROLE_LINK, projectId, role.getRoleId());
        String contentBody = String.format(addUserContentBody, roleUri, inviteeProfile);
        HttpRequestBase postRequest = restApiClient.newPostMethod(usersUri, contentBody);
        HttpResponse postResponse = restApiClient.execute(postRequest);
        assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
        JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
        assertFalse(json.getJSONObject("projectUsersUpdateResult").getString("successful").equals("[]"), "User isn't assigned properly into the project");
        System.out.println(String.format("Successfully assigned user %s to project %s by domain admin %s", inviteeProfile, projectId, domainUser));
    }

    public static String addUserGroup(RestApiClient restApiClient, String projectId,final String name)
            throws JSONException, IOException {
        final String projectUri = "/gdc/projects/" + projectId;

        @SuppressWarnings("serial")
        JSONObject payload = new JSONObject(new HashMap<String, Object>() {{
            put("userGroup", new HashMap<String, Object>() {{
                put("content", new HashMap<String, Object>() {{
                    put("name", name);
                    put("project", projectUri);
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
        HttpResponse response = restApiClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(statusCode, HttpStatus.NO_CONTENT.value(),
                     "User group could not be deleted, got " + statusCode);
    }

    public static String getLDMImageURI(String host, String projectId, String user, String password) throws ParseException, IOException, JSONException {
        RestApiClient restApiClient = new RestApiClient(host, user, password, true, false);
        String ldmUri = String.format(LDM_LINK, projectId);
        HttpRequestBase getRequest = restApiClient.newGetMethod(ldmUri);
        HttpResponse getResponse = restApiClient.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
        JSONObject json = new JSONObject(EntityUtils.toString(getResponse.getEntity()));
        String uri = json.getString("uri");
        // TODO fix on resource rather then here
        if (uri.matches("^/gdc_img.*")) {
            uri = "https://" + host + uri;
        }
        return uri;
    }

    public static boolean isValidImage(String host, String user, String pass, WebElement ele) {
        String imgSrc = ele.getAttribute("src");
        if (imgSrc == null) return false;

        RestApiClient restApiClient = new RestApiClient(host, user, pass, true, false);
        HttpResponse response = restApiClient.execute(restApiClient.newGetMethod(imgSrc));
        return response.getStatusLine().getStatusCode() == 200;
    }

    private static JSONObject getJSONObjectFrom(final RestApiClient restApiClient, final String uri) throws IOException, JSONException {
        HttpRequestBase getRequest = restApiClient.newGetMethod(uri);
        HttpResponse getResponse = restApiClient.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
        String result = EntityUtils.toString(getResponse.getEntity());
        return new JSONObject(result);
    }

    public static void setFeatureFlags(final RestApiClient restApiClient, final FeatureFlagOption... featureFlagOptions) throws IOException, JSONException {
        JSONObject json = getJSONObjectFrom(restApiClient, FEATURE_FLAGS_URI);
        for (FeatureFlagOption featureFlagOption : featureFlagOptions) {
            json.getJSONObject(FEATURE_FLAGS).put(featureFlagOption.getFeatureFlagName(), featureFlagOption.isOn());
        }
        HttpRequestBase putRequest = restApiClient.newPutMethod(FEATURE_FLAGS_URI, json.toString());
        HttpResponse putResponse = restApiClient.execute(putRequest);
        assertEquals(putResponse.getStatusLine().getStatusCode(), 204, "Invalid status code");
    }

    public static boolean isFeatureFlagEnabled(final RestApiClient restApiClient, final String featureFlagName)
            throws IOException, JSONException {
        JSONObject flags = getJSONObjectFrom(restApiClient, FEATURE_FLAGS_URI).getJSONObject(FEATURE_FLAGS);
        if (!flags.has(featureFlagName))
            return false;
        return Boolean.getBoolean(flags.getString(featureFlagName));
    }

    public static void setFeatureFlagsToProject(final RestApiClient restApiClient, final String projectId, final FeatureFlagOption... featureFlagOptions) throws JSONException {
        final URI projectFeatureFlagsUri = FEATURE_FLAGS_PROJECT_URI_TEMPLATE.expand(projectId);
        JSONObject featureFlagObject;
        JSONObject valuesJsonObject;

        for (FeatureFlagOption featureFlagOption : featureFlagOptions) {
            featureFlagObject = new JSONObject();
            valuesJsonObject = new JSONObject();

            valuesJsonObject.put(PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER, featureFlagOption.isOn());
            valuesJsonObject.put(PROJECT_FEATURE_FLAG_KEY_IDENTIFIER, featureFlagOption.getFeatureFlagName());
            featureFlagObject.put(PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER, valuesJsonObject);

            final HttpPost postRequest = restApiClient.newPostMethod(projectFeatureFlagsUri.toString(), featureFlagObject.toString());
            final HttpResponse postResponse = restApiClient.execute(postRequest);
            assertEquals(postResponse.getStatusLine().getStatusCode(), 201, "Invalid status code");
        }
    }

    public static String createMUFObj(final RestApiClient restApiClient, String projectID, String mufTitle, 
            Map<String, List<String>> conditions) throws IOException, JSONException {
        String mdObjURI = String.format(OBJ_LINK,projectID);
        String MUFExpressions = buildExpression(projectID, conditions);
        System.out.println(MUFExpressions);
        String contentBody = MUF_OBJ.replace("${MUFExpression}", MUFExpressions).replace("${MUFTitle}", mufTitle);
        HttpRequestBase postRequest = restApiClient.newPostMethod(mdObjURI, contentBody);
        HttpResponse postResponse = restApiClient.execute(postRequest);
        assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "New MUF is not created!");
        String result = EntityUtils.toString(postResponse.getEntity());
        JSONObject json = new JSONObject(result);
        return json.getString("uri");
    }

    private static String buildExpression(final String projectID, Map<String, List<String>> conditions) {
      //syntax: "([<Attribute_URI_1>] IN ([<element_URI_1>], [element_URI_2], [...] )) AND 
      //([<Attribute_URI_2>] IN ([<element_URI_1>], [element_URI_2], [...]))";
        List<String> expressions = Lists.newArrayList();
        String attributeURI = "[/gdc/md/%s/obj/%s]";
        final String elementURI = "[/gdc/md/%s/obj/%s/elements?id=%s]";
        String expression = "(%s IN (%s))";
        for (final String attributeID : conditions.keySet()) {
            expressions.add(String.format(expression, 
                    String.format(attributeURI, projectID, attributeID),
                    Joiner.on(",").join(Collections2.transform(conditions.get(attributeID), 
                            new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            return String.format(elementURI, projectID, attributeID, input);
                        }
                    }))
                    ));
        }
        return Joiner.on(" AND ").join(expressions);
    }
    
    public static void addMUFToUser(final RestApiClient restApiClient, String projectURI, String userProfileURI, 
            String mufURI) {
        String urserFilter = String.format(MUF_LINK, projectURI);
        String contentBody = USER_FILTER.replace("$UserProfileURI", userProfileURI).replace("$MUFExpression", 
                mufURI);
        System.out.println(contentBody);
        HttpRequestBase postRequest = restApiClient.newPostMethod(urserFilter, contentBody);
        HttpResponse postReponse = restApiClient.execute(postRequest);
        assertEquals(postReponse.getStatusLine().getStatusCode(), 200, "the MUF is not applied");
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
            HttpResponse response = restApiClient.execute(getRequest);
            assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONObject drills = json.getJSONObject("projectDashboard").getJSONObject("content").getJSONArray("tabs").
                    getJSONObject(0).getJSONArray("items").getJSONObject(0).getJSONObject("reportItem").
                    getJSONArray("drills").getJSONObject(0);
            drills.put("target", target);
            if (TARGET_POPUP.equals(target)) {
                drills.remove("export");
            } else if (TARGET_EXPORT.equals(target)) {
                JSONObject exportOptions = new JSONObject();
                exportOptions.put("format", exportFormat);
                drills.put("export", exportOptions );
            }
            postRequest = restApiClient.newPostMethod(dashboardEditModeURI, json.toString());
            response = restApiClient.execute(postRequest);
            assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid status code");
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            if (postRequest !=null) {
                postRequest.releaseConnection();
            }
           
        }
        
    }

    public static void enableFeatureFlagInProject(RestApiClient restApiClient, String projectId,
            ProjectFeatureFlags featureFlag) throws JSONException {
        setFeatureFlagsToProject(restApiClient, projectId,
                new FeatureFlagOption(featureFlag.getFlagName(), true));
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
}
