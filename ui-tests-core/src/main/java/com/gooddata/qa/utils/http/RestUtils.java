package com.gooddata.qa.utils.http;

import com.gooddata.qa.graphene.enums.UserRoles;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.*;

public class RestUtils {

    private static final String usersLink = "/gdc/projects/%s/users";
    private static final String roleUriLink = "/gdc/projects/%s/roles/%s";
    private static final String addUserContentBody = "{\"user\":{\"content\":{\"userRoles\":[\"%s\"],\"status\":\"ENABLED\"},\"links\":{\"self\":\"%s\"}}}";
    private static final String ldmLink = "/gdc/projects/%s/ldm";

    private static final String FEATURE_FLAGS_URI = "/gdc/internal/account/profile/featureFlags";
    private static final String FEATURE_FLAGS = "featureFlags";
    private static final UriTemplate FEATURE_FLAGS_PROJECT_URI_TEMPLATE = new UriTemplate("/gdc/projects/{projectId}/projectFeatureFlags");
    private static final String PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER = "value";
    private static final String PROJECT_FEATURE_FLAG_KEY_IDENTIFIER = "key";
    private static final String PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER = "featureFlag";
    
    private static final String GROUPS_URI = "/gdc/internal/usergroups";

    private RestUtils() {
    }

    public static void addUserToProject(String host, String projectId, String domainUser,
                                        String domainPassword, String inviteeProfile, UserRoles role) throws ParseException, IOException, JSONException {

        RestApiClient restApiClient = new RestApiClient(host, domainUser, domainPassword, true, false);
        String usersUri = String.format(usersLink, projectId);
        String roleUri = String.format(roleUriLink, projectId, role.getRoleId());
        String contentBody = String.format(addUserContentBody, roleUri, inviteeProfile);
        HttpRequestBase postRequest = restApiClient.newPostMethod(usersUri, contentBody);
        HttpResponse postResponse = restApiClient.execute(postRequest);
        assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
        JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
        assertFalse(json.getJSONObject("projectUsersUpdateResult").getString("successful").equals("[]"), "User isn't assigned properly into the project");
        System.out.println(String.format("Successfully assigned user %s to project %s by domain admin %s", inviteeProfile, projectId, domainUser));
    }
    
    public static void addUserGroup(RestApiClient restApiClient, String projectId, final String name) {
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
    }

    public static String getLDMImageURI(String host, String projectId, String user, String password) throws ParseException, IOException, JSONException {
        RestApiClient restApiClient = new RestApiClient(host, user, password, true, false);
        String ldmUri = String.format(ldmLink, projectId);
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
