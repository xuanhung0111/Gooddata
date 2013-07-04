package com.gooddata.qa.graphene.project;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;

@Test(groups = { "projectSimple" }, description = "Tests for basic project functionality in GD platform")
public class SimpleProjectTest extends AbstractTest {
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	}
	
	@Test(groups = { "projectSimpleInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectSimpleInit" })
	public void createSimpleProject() throws JSONException, InterruptedException {
		waitForProjectsPageLoaded();
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(BY_GP_FORM);
		ProjectFragment project = Graphene.createPageFragment(ProjectFragment.class, browser.findElement(BY_GP_FORM));
		projectId = project.createProject("simple-project", "", "", authorizationToken, 12);
	}
	
	@Test(dependsOnMethods = { "createSimpleProject" })
	public void deleteSimpleProject() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
		waitForProjectPageLoaded();
		ProjectAndUsersPage projectPage = Graphene.createPageFragment(ProjectAndUsersPage.class, browser.findElement(BY_PROJECT_PANEL));
		System.out.println("Going to delete project: " + projectId);
		projectPage.deteleProject();
	}

}
