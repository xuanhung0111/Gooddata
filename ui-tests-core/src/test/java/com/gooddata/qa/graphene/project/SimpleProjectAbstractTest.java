package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.fragments.greypages.md.Validation;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;

public abstract class SimpleProjectAbstractTest extends AbstractTest {
	
	protected String projectTitle = "simple-project";
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	}
	
	@Test(groups = { "projectSimpleInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectSimpleInit" }, groups = { "simpleTests" })
	public void createSimpleProject() throws JSONException, InterruptedException {
		waitForProjectsPageLoaded();
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject(projectTitle, "", "", authorizationToken, 12);

        Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
		successfulTest = true;
	}
	
	@Test(dependsOnGroups = { "simpleTests" })
    public void validateSimpleProject() throws JSONException {
        String validationStatus = validateProjectPartial(Validation.INVALID_OBJECTS, Validation.PMD__ELEM_VALIDATION);
        Assert.assertEquals(validationStatus, "OK");

        validationStatus = validateProject();
        Assert.assertEquals(validationStatus, "OK");
    }
	
	@Test(dependsOnMethods = { "validateSimpleProject" }, alwaysRun = true)
	public void deleteSimpleProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
