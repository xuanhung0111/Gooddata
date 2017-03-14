package com.gooddata.qa.utils.http.project;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.GoodData;
import com.gooddata.featureflag.FeatureFlagService;
import com.gooddata.featureflag.ProjectFeatureFlag;
import com.gooddata.project.Environment;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.project.ProjectService;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestApiClient;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.awaitility.pollinterval.IterativePollInterval;

/**
 * REST utilities for project task
 */
public final class ProjectRestUtils {

    private static final Logger log = Logger.getLogger(ProjectRestUtils.class.getName());

    private static final String PROJECT_LINK = "/gdc/projects/%s";

    private ProjectRestUtils() {
    }

    private static final Supplier<String> UPDATE_PROJECT_TITLE_BODY = () -> {
        try {
            return new JSONObject() {{
                put("project", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("guidedNavigation", "1");
                        put("environment", "${environment}");
                    }});
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    /**
     * Create blank project
     * 
     * @param goodData
     * @param title
     * @param authorizationToken
     * @param projectDriver
     * @param environment
     * @return project id
     */
    public static String createBlankProject(final GoodData goodData, final String title,
            final String authorizationToken, final ProjectDriver projectDriver, final Environment environment) {
        return createProject(goodData, title, null, authorizationToken, projectDriver, environment);
    }

    /**
     * Create project with specific template
     * 
     * @param goodData
     * @param title
     * @param template
     * @param authorizationToken
     * @param projectDriver
     * @param environment
     * @return project id
     */
    public static String createProject(final GoodData goodData, final String title, final String template,
            final String authorizationToken, final ProjectDriver projectDriver, final Environment environment) {
        final Project project = new Project(title, authorizationToken);
        if (!StringUtils.isBlank(template)) project.setProjectTemplate(template);
        project.setDriver(projectDriver);
        project.setEnvironment(environment);

        return goodData.getProjectService().createProject(project).get().getId();
    }

    public static void updateProjectTitle(final RestApiClient restApiClient, final Project project,
            final String newProjectTitle) {
        final String uri = String.format(PROJECT_LINK, project.getId());
        final String content = UPDATE_PROJECT_TITLE_BODY.get()
                .replace("${title}", newProjectTitle)
                .replace("${environment}", project.getEnvironment());

        executeRequest(restApiClient, restApiClient.newPostMethod(uri, content), HttpStatus.NO_CONTENT);
    }

    /**
     * Delete project
     * 
     * @param goodData
     * @param projectId
     */
    public static void deleteProject(final GoodData goodData, final String projectId) {
        final ProjectService service = goodData.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }

    /**
     * Turn on/off project feature flag
     * 
     * @param goodData
     * @param projectId
     * @param featureFlag
     * @param enabled
     */
    public static void setFeatureFlagInProject(final GoodData goodData, final String projectId,
            final ProjectFeatureFlags featureFlag, final boolean enabled) {
        final FeatureFlagService service = goodData.getFeatureFlagService();
        service.createProjectFeatureFlag(goodData.getProjectService().getProjectById(projectId),
                new ProjectFeatureFlag(featureFlag.getFlagName(), enabled));
    }

    /**
     * Turn on/off project feature flag and check the feature flag's status before exit
     * Because the cache in C3 is 10-20s so if checking the feature flag is not correct, we should wait for sometime
     * Waiting time is 10s++ for each loop
     * 
     * @param goodData
     * @param projectId
     * @param featureFlag
     * @param enabled
     */
    public static void setFeatureFlagInProjectAndCheckResult(final GoodData goodData, final String projectId,
            final ProjectFeatureFlags featureFlag, final boolean enabled) {
        log.info(String.format("Set feature flag %s for project %s %s", featureFlag.getFlagName(), projectId,
                enabled));
        final FeatureFlagService service = goodData.getFeatureFlagService();
        final Project project = goodData.getProjectService().getProjectById(projectId);

        service.createProjectFeatureFlag(project, new ProjectFeatureFlag(featureFlag.getFlagName(), enabled));

        Awaitility.await()
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(IterativePollInterval.iterative(duration -> duration.plus(10, TimeUnit.SECONDS))
                    .startDuration(Duration.TEN_SECONDS))
            .conditionEvaluationListener(condition -> service.createProjectFeatureFlag(project, 
                    new ProjectFeatureFlag(featureFlag.getFlagName(), enabled)))
            .until(() -> service.getProjectFeatureFlag(project, featureFlag.getFlagName()).isEnabled() == enabled);
    }
}
