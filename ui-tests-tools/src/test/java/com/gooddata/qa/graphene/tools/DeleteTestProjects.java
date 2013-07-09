package com.gooddata.qa.graphene.tools;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

/**
 * This is a helper class to delete test projects from required GD host
 * 
 * @author michal.vanco@gooddata.com
 *
 */
@Test(groups = { "tools" }, description = "Tools tests")
public class DeleteTestProjects extends AbstractTest {
	
	@BeforeClass
	public void initStartPage() {
		startPage = "login.html";
	}
	
	@Test(groups = "deleteProjectsInit")
	public void initTest() throws JSONException {
		validSignInWithDemoUser(false);
	}

	@Test(dependsOnGroups = { "deleteProjectsInit" })
	public void deleteAllConnectorCheckProjects() {
		deleteProjects("CheckConnector");
	}
	
	@Test(dependsOnGroups = { "deleteProjectsInit" })
	public void deleteAllGoodSalesCheckProjects() {
		deleteProjects("GoodSales-test");
	}
	
	@Test(dependsOnGroups = { "deleteProjectsInit" })
	public void deleteAllGoodSalesPerfCheckProjects() {
		deleteProjects("GoodSales-perf-test");
	}
	
	private void deleteProjects(String projectSubstring) {
		browser.get(getRootUrl() + PAGE_PROJECTS);
		waitForElementVisible(BY_PROJECTS_LIST);
		ProjectsPage projectsPage = Graphene.createPageFragment(ProjectsPage.class, browser.findElement(BY_PROJECTS_PANEL));
		List<String> projectsToDelete = projectsPage.getProjectsIds(projectSubstring);
		System.out.println("Going to delete " + projectsToDelete.size() + " projects, " + projectsToDelete.toString());
		for (String projectToDelete : projectsToDelete) {
			browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectToDelete + "|projectPage");
			waitForProjectPageLoaded();
			ProjectAndUsersPage projectPage = Graphene.createPageFragment(ProjectAndUsersPage.class, browser.findElement(BY_PROJECT_PANEL));
			System.out.println("Going to delete project: " + projectToDelete);
			projectPage.deteleProject();
		}
	}
	
}
