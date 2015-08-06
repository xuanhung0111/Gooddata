package com.gooddata.qa.graphene.tools;

import java.util.List;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.UserRoles;

import org.json.JSONException;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

/**
 * This is a helper class to delete test projects from required GD host
 *
 * @author michal.vanco@gooddata.com
 */
@Test(groups = {"tools"}, description = "Tools tests")
public class DeleteTestProjects extends AbstractUITest {

    @Test(groups = {"deleteProjectsInit", PROJECT_INIT_GROUP})
    public void initTest() throws JSONException {
        signIn(false, UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllConnectorCheckProjects() throws InterruptedException {
        deleteProjects("Connector-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllGoodSalesCheckProjects() throws InterruptedException {
        deleteProjects("GoodSales-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllSimpleProjects() throws InterruptedException {
        deleteProjects("SimpleProject-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllDiscProjects() throws InterruptedException {
        deleteProjects("Disc-test");
    }

    private void deleteProjects(String projectSubstring) throws InterruptedException {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        Thread.sleep(5000);
        List<String> projectsToDelete = projectsPage.getProjectsIds(projectSubstring);
        System.out.println("Going to delete " + projectsToDelete.size() + " projects, " + projectsToDelete.toString());
        for (String projectToDelete : projectsToDelete) {
            deleteProject(projectToDelete);
        }
    }

}
