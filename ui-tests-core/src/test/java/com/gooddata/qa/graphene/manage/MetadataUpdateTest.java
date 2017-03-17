package com.gooddata.qa.graphene.manage;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MetadataUpdateTest extends AbstractProjectTest {

    private final static String TARGET_PROJECT_TITLE = "Target-Project";

    @BeforeClass
    public void setProperties() {
        // these tests are meaningful when performing testing on created project
        testParams.setReuseProject(true);
    }

    @Override
    public void enableDynamicUser() {
        // these tests are working on the project which is intended to reuse for a long time
        // so it makes sense to keep it clean
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRedBarOnModelPage() {
        initModelPage();
        Screenshots.takeScreenshot(browser, "testRedBarOnModelTab", getClass());
        CheckUtils.checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testProjectExportImport() throws JSONException {
        String exportToken = exportProject(true, true, false, DEFAULT_PROJECT_CHECK_LIMIT);
        String workingProject = testParams.getProjectId();
        String targetProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), TARGET_PROJECT_TITLE,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams
                        .getProjectEnvironment());

        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
        } finally {
            deleteProject(testParams.getProjectId());
            testParams.setProjectId(workingProject);
        }
    }
}
