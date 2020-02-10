package com.gooddata.qa.graphene.enterprise;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.fragments.freegrowth.WorkspaceHeader;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import org.testng.annotations.Test;

public class BasicEnterpriseTest extends AbstractProjectTest {

    @Test(dependsOnGroups = {"createProject"})
    public void gotoProjectFromProjectPage() {
        initProjectsPage().goToProject(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"gotoProjectFromProjectPage"})
    public void checkPerfectPixel() {
        waitForOpeningIndigoDashboard();

        WorkspaceHeader workspaceHeader = WorkspaceHeader.getWorkspaceHeaderInstance(browser);
        workspaceHeader.verifyWorkspaceHeader();
        workspaceHeader.verifyKpiDashboardMenuActive();
        IndigoDashboardsPage.getInstance(browser).addDashboard();
    }
}
