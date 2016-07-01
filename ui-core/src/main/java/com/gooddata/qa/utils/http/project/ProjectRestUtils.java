package com.gooddata.qa.utils.http.project;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;

import java.util.function.Supplier;
import java.util.stream.StreamSupport;

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

/**
 * REST utilities for project task
 */
public final class ProjectRestUtils {

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
        final Project project = goodData.getProjectService().getProjectById(projectId);

        final ProjectFeatureFlag flag;
        if (StreamSupport.stream(service.listProjectFeatureFlags(project).spliterator(), false)
            .anyMatch(e -> e.getName().equals(featureFlag.getFlagName()))) {
            flag = service.getProjectFeatureFlag(project, featureFlag.getFlagName());
        } else {
            flag = service.createProjectFeatureFlag(project, new ProjectFeatureFlag(featureFlag.getFlagName(), false));
        }

        flag.setEnabled(enabled);
        service.updateProjectFeatureFlag(flag);
    }
}
