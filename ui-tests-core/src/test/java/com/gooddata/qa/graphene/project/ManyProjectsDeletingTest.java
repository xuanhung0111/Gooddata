package com.gooddata.qa.graphene.project;

import com.gooddata.sdk.model.project.Environment;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.sdk.service.project.ProjectService;
import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.http.RestClient;
import java.time.ZonedDateTime;
import org.testng.annotations.Test;
import org.threeten.extra.Days;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsNot.not;

public class ManyProjectsDeletingTest extends AbstractTest {
    private int retentionDaysNumber;
    private Environment testingEnv = Environment.TESTING;
    private ProjectService service;

    @Test
    public void deleteProjects() {
        // Load and create needed test properties.
        initTestProperties();

        // Filter old projects with TESTING environment.
        Collection<Project> oldTestingProjects = this.getOldTestingProjects();

        // BE CAREFUL! below code will delete many projects.
        deleteManyProjects(oldTestingProjects);
        sleepTightInSeconds(30);

        // Assert deleted project's id to make this test pass or fail.
        List<String> allEnabledProjectIds = convertToProjectIdCollection(service.getProjects());
        List<String> deletedProjectIds = convertToProjectIdCollection(oldTestingProjects);

        deletedProjectIds.forEach(projectId -> {
            try {
                assertThat(allEnabledProjectIds, not(hasItems(projectId)));
            } catch (AssertionError e) {
                // Custom error message doens't show all ENABLED project Ids in platform.
                throw new AssertionError("Assert fail at this project id: " + projectId + "\n" +
                        "This is all projects are not deleted yet : " + getEnabledProjectId(oldTestingProjects));
            }
        });
    }

    /**
     * Load and create properties
     */
    private void initTestProperties() {
        // load value, default is 30 days
        retentionDaysNumber = testParams.getRetentionDays();

        // Create RestClient and ProjectService with domain user.
        String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        RestClient restClient = new RestClient(
                new RestClient.RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true));
        service = restClient.getProjectService();
    }

    /**
     * Get projects have specific environment and were created before some days ago.
     *
     * @return a collection of projects with specific environment which were created before number of days ago.
     */
    private Collection<Project> getOldTestingProjects() {
        // Filter projects with specific Environments and older than some days.
        Collection<Project> result = service.getProjects()
                .stream()
                .filter(this::filterOldTestingProject)
                .collect(Collectors.toList());
        log.info("There are total " + result.size()
                + " projects were created " + retentionDaysNumber + " days ago"
                + " with environment is " + testingEnv);

        return result;
    }

    /**
     * Is project old and TESTING environment.
     */
    private boolean filterOldTestingProject(Project project) {
        return testingEnv.toString().equals(project.getEnvironment())
                && (getProjectAge(project) >= retentionDaysNumber);
    }

    /**
     * Delete many projects
     *
     * @param projects many projects in collection
     */
    private void deleteManyProjects(Collection<Project> projects) {
        projects.forEach(project -> {
            try {
                log.info("Deleting " + project.getEnvironment() + " - " + project.getId() + " - " + project.getTitle());
                service.removeProject(project);
            } catch (Exception exp) {
                log.warning("Fail to delete project " + project.getId() + " with id: " + project.getTitle()
                        + ". Detail exception is:" + "\n" + exp.toString());
                return;
            }
        });
    }

    /**
     * Covert collection of projects to list of project IDs.
     */
    private List<String> convertToProjectIdCollection(Collection<Project> projects) {
        return projects.stream().map(Project::getId).collect(Collectors.toList());
    }

    /**
     * Get ID of ENABLED project from input collection.
     */
    private List<String> getEnabledProjectId(Collection<Project> projects) {
        return projects.stream()
                .filter(Project::isEnabled)
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    /**
     * How many days is the project created.
     *
     * @param project
     * @return number of days old
     */
    private int getProjectAge(Project project) {
        return Days.between(project.getCreated(), ZonedDateTime.now()).getAmount();
    }
}
