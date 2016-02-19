package com.gooddata.qa.utils.http.project;

import org.apache.commons.lang3.StringUtils;

import com.gooddata.GoodData;
import com.gooddata.project.Environment;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.project.ProjectFeatureFlag;
import com.gooddata.project.ProjectService;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;

public final class ProjectRestUtils {

    private ProjectRestUtils() {
    }

    public static String createBlankProject(final GoodData goodData, final String title,
            final String authorizationToken, final ProjectDriver projectDriver, final Environment environment) {
        return createProject(goodData, title, null, authorizationToken, projectDriver, environment);
    }

    public static String createProject(final GoodData goodData, final String title, final String template,
            final String authorizationToken, final ProjectDriver projectDriver, final Environment environment) {
        final Project project = new Project(title, authorizationToken);
        if (!StringUtils.isBlank(template)) project.setProjectTemplate(template);
        project.setDriver(projectDriver);
        project.setEnvironment(environment);

        return goodData.getProjectService().createProject(project).get().getId();
    }

    public static void deleteProject(final GoodData goodData, final String projectId) {
        final ProjectService service = goodData.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }

    public static void setFeatureFlagInProject(final GoodData goodData, final String projectId,
            final ProjectFeatureFlags featureFlag, final boolean enabled) {
        final ProjectService service = goodData.getProjectService();
        final Project project = service.getProjectById(projectId);

        final ProjectFeatureFlag flag;
        if (service.listFeatureFlags(project).stream()
                .anyMatch(e -> e.getName().equals(featureFlag.getFlagName()))) {
            flag = service.getFeatureFlag(service.getProjectById(projectId), featureFlag.getFlagName());
        } else {
            flag = service.createFeatureFlag(project, new ProjectFeatureFlag(featureFlag.getFlagName(), false));
        }

        flag.setEnabled(enabled);
        service.updateFeatureFlag(flag);
    }
}
