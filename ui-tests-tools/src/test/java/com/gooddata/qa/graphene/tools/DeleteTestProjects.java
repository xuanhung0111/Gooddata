package com.gooddata.qa.graphene.tools;

import java.util.List;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;

/**
 * This is a helper class to delete test projects from required GD host
 *
 * @author michal.vanco@gooddata.com
 */
@Test(groups = {"tools"}, description = "Tools tests")
public class DeleteTestProjects extends AbstractTest {

    @BeforeClass
    public void initStartPage() {
        startPage = ui.PAGE_LOGIN;
    }

    @Test(groups = "deleteProjectsInit")
    public void initTest() throws JSONException {
        validSignInWithDemoUser(false);
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllConnectorCheckProjects() throws InterruptedException {
        deleteProjects("CheckConnector");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllGoodSalesCheckProjects() throws InterruptedException {
        deleteProjects("GoodSales-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllSimpleProjects() throws InterruptedException {
        deleteProjects("simple-project");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllSimpleGeoProjects() throws InterruptedException {
        deleteProjects("simple-project-geo");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllGoodSalesPerfCheckProjects() throws InterruptedException {
        deleteProjects("GoodSales-perf-test");
    }

    private void deleteProjects(String projectSubstring) throws InterruptedException {
        openUrl(ui.PAGE_PROJECTS);
        waitForElementVisible(ui.projectsPage.getRoot());
        Thread.sleep(5000);
        List<String> projectsToDelete = ui.projectsPage.getProjectsIds(projectSubstring);
        System.out.println("Going to delete " + projectsToDelete.size() + " projects, " + projectsToDelete.toString());
        for (String projectToDelete : projectsToDelete) {
            ui.deleteProject(projectToDelete);
        }
    }

}
