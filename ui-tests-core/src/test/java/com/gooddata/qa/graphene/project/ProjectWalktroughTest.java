package com.gooddata.qa.graphene.project;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;

@Test(groups = {"projectWalkthrough"}, description = "Tests for verification of existing project - test goes over all dashboard tabs and create screenshots")
public class ProjectWalktroughTest extends AbstractTest {

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
        testParams.setProjectId(testParams.loadProperty("projectId"));
    }

    @Test(groups = {"projectWalkthroughInit"})
    public void userLogin() throws JSONException {
        // sign in with demo user
        ui.signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = {"projectWalkthroughInit"})
    public void verifyProject() throws InterruptedException {
        ui.verifyProjectDashboardsAndTabs(false, null, true);
    }

}