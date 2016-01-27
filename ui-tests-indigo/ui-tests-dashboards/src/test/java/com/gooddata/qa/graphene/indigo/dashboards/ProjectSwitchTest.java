package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class ProjectSwitchTest  extends DashboardWithWidgetsTest {
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 10);
    private static final String NEW_PROJECT_NAME = "N-" + UNIQUE_ID;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        // note that during project creation, dwh driver name is appended to project title
        projectTitle = "E-" + UNIQUE_ID;
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void switchProjectsTest() throws ParseException, JSONException, IOException {
        String newProjectId = createProject(NEW_PROJECT_NAME);
        setupFeatureFlagInProject(newProjectId, ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS);

        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "switchProjectsTest-initial", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), projectTitle);

        indigoDashboardsPage.switchProject(NEW_PROJECT_NAME).getSplashScreen();
        takeScreenshot(browser, "switchProjectsTest-switched", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), NEW_PROJECT_NAME);

        indigoDashboardsPage.switchProject(projectTitle).waitForDashboardLoad();
        takeScreenshot(browser, "switchProjectsTest-switched-back", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), projectTitle);

        ProjectRestUtils.deleteProject(getRestApiClient(), newProjectId);
    }

    private String createProject(String name) {
        String projectId = null;

        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());
        try {
            projectId = gpProject.createProject(name, name,
                    null, testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                    testParams.getProjectEnvironment(), 60);

        } catch (JSONException e) {
            fail("There is problem when creating new project: " + e);
        }

        return projectId;
    }

}
