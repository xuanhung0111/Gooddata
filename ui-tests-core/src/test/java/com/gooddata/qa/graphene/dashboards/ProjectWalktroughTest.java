package com.gooddata.qa.graphene.dashboards;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;

@Test(groups = {"projectWalkthrough"}, description = "Tests for verification of existing project - test goes over all dashboard tabs and create screenshots")
public class ProjectWalktroughTest extends AbstractUITest {

    @BeforeClass
    public void initProperties() {
        testParams.setProjectId(testParams.loadProperty("projectId"));
    }

    @Test(groups = {PROJECT_INIT_GROUP, "projectWalkthroughInit"})
    public void userLogin() throws JSONException {
        // sign in with demo user
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = {"projectWalkthroughInit"})
    public void verifyProject() {
        verifyProjectDashboardsAndTabs(false, null, true);
    }

}