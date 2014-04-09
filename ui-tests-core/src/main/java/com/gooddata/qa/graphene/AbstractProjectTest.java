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
    protected int projectCreateCheckIterations = 12; // (12*5s = 1 minute)

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
    }

    @Test(groups = {"projectInit"})
    public void init() throws JSONException {
        // sign in with demo user
        signInAtUI(user, password);
    }

    @Test(dependsOnGroups = {"projectInit"}, groups = {"tests"})
    public void createProject() throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());
        projectId = gpProject.createProject(projectTitle, projectTitle, projectTemplate, authorizationToken, projectCreateCheckIterations);
        Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
    }

    @Test(dependsOnGroups = {"tests"})
    public void validateProjectAfterTests() throws JSONException {
        // TODO remove when ATP-1520, ATP-1519, ATP-1822 are fixed
        String testName = this.getClass().getSimpleName();
        if (testName.contains("Coupa") || testName.contains("Pardot") || testName.contains("Zendesk4")) {
            System.out.println("Validations are skipped for Coupa and Pardot projects");
            return;
        }

        assertEquals(validateProject(), "OK");
    }

    @Test(dependsOnMethods = {"validateProjectAfterTests"}, alwaysRun = true)
    public void deleteProject() {
        deleteProjectByDeleteMode(successfulTest);
    }
}
