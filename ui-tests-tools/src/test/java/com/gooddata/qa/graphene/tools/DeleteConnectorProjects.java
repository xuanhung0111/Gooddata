package com.gooddata.qa.graphene.tools;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

/**
 * This is a helper class to delete connector check projects from required GD host
 * 
 * @author michal.vanco@gooddata.com
 *
 */
@Test(groups = { "tools" }, description = "Tools tests")
public class DeleteConnectorProjects extends AbstractTest {
	
	private static final By BY_PROJECTS_PANEL = By.id("projectsCentral");
	private static final By BY_PROJECT_PANEL = By.id("p-projectPage");
	private static final By BY_PROJECTS_LIST = By.id("myProjects");
	
	@BeforeClass
	public void initStartPage() {
		startPage = "login.html";
	}

	@Test
	public void deleteAllConnectorCheckProjects() throws JSONException {
		validSignInWithDemoUser(false);
		browser.get(getRootUrl() + PAGE_PROJECTS);
		waitForElementVisible(BY_PROJECTS_LIST);
		ProjectsPage projectsPage = Graphene.createPageFragment(ProjectsPage.class, browser.findElement(BY_PROJECTS_PANEL));
		List<String> projectsToDelete = projectsPage.getProjectsIds("CheckConnector");
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
