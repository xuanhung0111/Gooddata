package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.enums.DWHDriver;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractProjectTest extends AbstractUITest {

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
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = {"projectInit"}, groups = {"tests"})
    public void createProject() throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());

        projectTitle += "-" + testParams.getDwhDriver().name();
        if (projectTemplate.isEmpty()) {
            testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null, testParams.getAuthorizationToken(), testParams.getDwhDriver(), projectCreateCheckIterations));
        } else {
            testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, projectTemplate, testParams.getAuthorizationToken(), DWHDriver.PG, projectCreateCheckIterations));

            if (testParams.getDwhDriver().equals(DWHDriver.VERTICA)) {
                String exportToken = exportProject(true, true, projectCreateCheckIterations * 5);
                deleteProject(testParams.getProjectId());

                openUrl(PAGE_GDC_PROJECTS);
                waitForElementVisible(gpProject.getRoot());
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null, testParams.getAuthorizationToken2(), testParams.getDwhDriver(), projectCreateCheckIterations));
                importProject(exportToken, projectCreateCheckIterations * 5);
            }
        }
        Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
    }

    @Test(dependsOnGroups = {"tests"})
    public void validateProjectAfterTests() throws JSONException {
        // TODO remove when ATP-1520, ATP-1519, ATP-1822 are fixed
        String testName = this.getClass().getSimpleName();
        if (testName.contains("Coupa") || testName.contains("Pardot") || testName.contains("Zendesk4")) {
            System.out.println("Validations are skipped for Coupa, Pardot and Zendesk4 projects");
            return;
        }
        assertEquals(validateProject(), "OK");
    }

    @Test(dependsOnMethods = {"validateProjectAfterTests"}, alwaysRun = true)
    public void deleteProject() {
        deleteProjectByDeleteMode(successfulTest);
    }
}
