package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class ProjectSwitchTest extends AbstractCsvUploaderTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 10);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String currentProjectId;
    private String newProjectId;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();

        newProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), NEW_PROJECT_NAME,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                testParams.getProjectEnvironment());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void switchProjectsTest() {
        initDataUploadPage().switchProject(NEW_PROJECT_NAME);

        takeScreenshot(browser, "Switch-to-project-" + NEW_PROJECT_NAME, getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        DatasetsListPage.getInstance(browser).switchProject(projectTitle);

        takeScreenshot(browser, "Switch-to-project-" + projectTitle, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Test(dependsOnMethods = { "switchProjectsTest" })
    public void openDataUploadPageAfterDeleteAnotherProject() {
        testParams.setProjectId(newProjectId);

        try {
            initDashboardsPage();
            assertThat(browser.getCurrentUrl(), containsString(newProjectId));

            initProjectsAndUsersPage().deteleProject();
            waitForProjectsPageLoaded(browser);

        } finally {
            testParams.setProjectId(currentProjectId);
        }

        initDataUploadPage();
        takeScreenshot(browser, "Re-open-data-upload-page-of-project-" + currentProjectId +
                "-after-delete-project-" + newProjectId, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }
}
