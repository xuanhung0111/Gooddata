package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.fragments.greypages.md.Validation;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

public abstract class AbstractProjectTest extends AbstractTest {
	
	protected String projectTitle = "simple-project";
	protected String projectTemplate = "";
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	}
	
	@Test(groups = { "projectInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectInit" }, groups = { "tests" })
	public void createProject() throws JSONException, InterruptedException {
		waitForProjectsPageLoaded();
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject(projectTitle, projectTitle, projectTemplate, authorizationToken, 12);
		Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
	}
	
	@Test(dependsOnGroups = { "tests" })
    public void validateProjectAfterTests() throws JSONException {
        String validationStatus = validateProjectPartial(Validation.INVALID_OBJECTS, Validation.PMD__ELEM_VALIDATION);
        assertEquals(validationStatus, "OK");

        validationStatus = validateProject();
        assertEquals(validationStatus, "OK");
        successfulTest = true;
    }
	
	@Test(dependsOnMethods = { "validateProjectAfterTests" }, alwaysRun = true)
	public void deleteProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
