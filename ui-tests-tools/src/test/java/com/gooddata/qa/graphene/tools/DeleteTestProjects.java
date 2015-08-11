package com.gooddata.qa.graphene.tools;

import java.util.List;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;

import org.json.JSONException;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;

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
    public void deleteAllConnectorCheckProjects() {
        deleteProjects("Connector-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllGoodSalesCheckProjects() {
        deleteProjects("GoodSales-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllSimpleProjects() {
        deleteProjects("SimpleProject-test");
    }

    @Test(dependsOnGroups = {"deleteProjectsInit"})
    public void deleteAllDiscProjects() {
        deleteProjects("Disc-test");
    }

    private void deleteProjects(String projectSubstring) {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        sleepTight(5000);
        List<String> projectsToDelete = projectsPage.getProjectsIds(projectSubstring);
        System.out.println("Going to delete " + projectsToDelete.size() + " projects, " + projectsToDelete.toString());
        for (String projectToDelete : projectsToDelete) {
            deleteProject(projectToDelete);
        }
    }

}
