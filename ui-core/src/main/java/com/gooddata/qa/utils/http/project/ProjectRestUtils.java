package com.gooddata.qa.utils.http.project;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriTemplate;

import com.gooddata.qa.graphene.enums.project.DWHDriver;
import com.gooddata.qa.graphene.enums.project.ProjectEnvironment;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestApiClient;

public final class ProjectRestUtils {

    private static final Logger log = Logger.getLogger(ProjectRestUtils.class.getName());

    private ProjectRestUtils() {
    }

    private static final String PROJECT_LINK = "/gdc/projects/";
    private static final String FEATURE_FLAGS_URI = "/gdc/internal/account/profile/featureFlags";
    private static final String FEATURE_FLAGS = "featureFlags";
    private static final UriTemplate FEATURE_FLAGS_PROJECT_URI_TEMPLATE =
            new UriTemplate("/gdc/projects/{projectId}/projectFeatureFlags");
    private static final String PROJECT_FEATURE_FLAG_VALUE_IDENTIFIER = "value";
    private static final String PROJECT_FEATURE_FLAG_KEY_IDENTIFIER = "key";
    private static final String PROJECT_FEATURE_FLAG_CONTAINER_IDENTIFIER = "featureFlag";

    private static final String CREATE_PROJECT_CONTENT_BODY;

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

    public static void setFeatureFlags(final RestApiClient restApiClient,
            final FeatureFlagOption... featureFlagOptions) throws IOException, JSONException {
        final JSONObject json = getJsonObject(restApiClient, FEATURE_FLAGS_URI);
        for (FeatureFlagOption featureFlagOption : featureFlagOptions) {
            json.getJSONObject(FEATURE_FLAGS).put(featureFlagOption.getFeatureFlagName(),
                    featureFlagOption.isOn());
        }
        executeRequest(restApiClient, restApiClient.newPutMethod(FEATURE_FLAGS_URI, json.toString()), HttpStatus.NO_CONTENT);
    }

    public static boolean isFeatureFlagEnabled(final RestApiClient restApiClient, final String featureFlagName)
            throws IOException, JSONException {
        final JSONObject flags = getJsonObject(restApiClient, FEATURE_FLAGS_URI).getJSONObject(FEATURE_FLAGS);
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

            executeRequest(restApiClient,
                    restApiClient.newPostMethod(projectFeatureFlagsUri.toString(), featureFlagObject.toString()),
                    HttpStatus.CREATED);
        }
    }

    public static void enableFeatureFlagInProject(RestApiClient restApiClient, String projectId,
            ProjectFeatureFlags featureFlag) throws JSONException {
        setFeatureFlagsToProject(restApiClient, projectId,
                new FeatureFlagOption(featureFlag.getFlagName(), true));
    }

    public static void disableFeatureFlagInProject(RestApiClient restApiClient, String projectId,
            ProjectFeatureFlags featureFlag) throws JSONException {
        setFeatureFlagsToProject(restApiClient, projectId,
                new FeatureFlagOption(featureFlag.getFlagName(), false));
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

    private static boolean waitForProjectCreated(RestApiClient restApiClient, String projectId)
            throws ParseException, JSONException, IOException {
        String currentStatus = "";

        // checking in 10 minutes
        for (int i = 0; i < 120; i++) {
            currentStatus = getProjectState(restApiClient, projectId);
            log.info("Current status: " + currentStatus);
            if (Stream.of("ERROR", "DELETED", "CANCELED", "ENABLED", "DISABLED", "ARCHIVED")
                    .anyMatch(currentStatus::equals)) break;
            sleepTightInSeconds(5);
        }

        return "ENABLED".equals(currentStatus);
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
}
