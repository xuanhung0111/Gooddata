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
	public void deleteAllSimpleGeoProjects() {
		deleteProjects("simple-project-geo");
	}
	
	@Test(dependsOnGroups = { "deleteProjectsInit" })
	public void deleteAllGoodSalesPerfCheckProjects() {
		deleteProjects("GoodSales-perf-test");
	}
	
	private void deleteProjects(String projectSubstring) {
		browser.get(getRootUrl() + PAGE_PROJECTS);
		waitForElementVisible(projectsPage.getRoot());
		List<String> projectsToDelete = projectsPage.getProjectsIds(projectSubstring);
		System.out.println("Going to delete " + projectsToDelete.size() + " projects, " + projectsToDelete.toString());
		for (String projectToDelete : projectsToDelete) {
			deleteProject(projectToDelete);
		}
	}
	
}
