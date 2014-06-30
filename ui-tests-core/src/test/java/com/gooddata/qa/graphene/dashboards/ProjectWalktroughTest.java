package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.AbstractUITest;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"projectWalkthrough"}, description = "Tests for verification of existing project - test goes over all dashboard tabs and create screenshots")
public class ProjectWalktroughTest extends AbstractUITest {

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
        testParams.setProjectId(testParams.loadProperty("projectId"));
    }

    @Test(groups = {"projectWalkthroughInit"})
    public void userLogin() throws JSONException {
        // sign in with demo user
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = {"projectWalkthroughInit"})
    public void verifyProject() throws InterruptedException {
        verifyProjectDashboardsAndTabs(false, null, true);
    }

}