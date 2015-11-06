package com.gooddata.qa.graphene;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.browser.BrowserUtils.getCurrentBrowserAgent;
import static com.gooddata.qa.browser.BrowserUtils.maximize;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.DWHDriver;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

public abstract class AbstractProjectTest extends AbstractUITest {

    protected static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes

    protected String projectTitle = "simple-project";
    protected String projectTemplate = "";
    protected int projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;

    protected boolean addUsersWithOtherRoles = false;
    // validations are enabled by default on any child class
    protected boolean validateAfterClass = true;

    @Test(groups = {PROJECT_INIT_GROUP})
    public void init() throws JSONException {
        System.out.println("Current browser agent is: " + getCurrentBrowserAgent(browser).toUpperCase());

        String executionEnv = System.getProperty("test.execution.env");
        if (executionEnv != null && executionEnv.contains("browserstack-mobile")) {
            System.out.println("Maximizing window is ignored for execution on mobile devices at Browserstack.");
        } else {
            // Use this utility to maximize browser in chrome - MAC
            // browser.manage().window().maximize(); do not work
            maximize(browser);
        }

        // sign in with admin user
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP}, groups = {"createProject"})
    public void createProject() throws JSONException, IOException {
        if (testParams.isReuseProject()) {
            if (testParams.getProjectId() != null && !testParams.getProjectId().isEmpty()) {
                System.out.println("Project will be re-used, id: " + testParams.getProjectId());
                return;
            } else {
                System.out.println("Project reuse is expected, but projectId is missing, new project will be created...");
            }
        }

        if (!canAccessGreyPage(browser)) {
            System.out.println("Use REST api to create project.");
            testParams.setProjectId(RestUtils.createProject(getRestApiClient(), projectTitle, projectTitle,
                    projectTemplate, testParams.getAuthorizationToken(), DWHDriver.PG,
                    testParams.getProjectEnvironment()));

        } else {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());

            projectTitle += "-" + testParams.getDwhDriver().name();
            if (projectTemplate.isEmpty()) {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                        testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations));
            } else {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, projectTemplate,
                        testParams.getAuthorizationToken(), DWHDriver.PG, testParams.getProjectEnvironment(),
                        projectCreateCheckIterations));

                if (testParams.getDwhDriver().equals(DWHDriver.VERTICA)) {
                    String exportToken = exportProject(true, true, false, projectCreateCheckIterations * 5);
                    deleteProject(testParams.getProjectId());

                    openUrl(PAGE_GDC_PROJECTS);
                    waitForElementVisible(gpProject.getRoot());
                    testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                            testParams.getAuthorizationToken2(), testParams.getDwhDriver(),
                            testParams.getProjectEnvironment(), projectCreateCheckIterations));
                    importProject(exportToken, projectCreateCheckIterations * 5);
                }
            }
            Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
        }

        if (addUsersWithOtherRoles) addUsersWithOtherRolesToProject();
    }

    @AfterClass(alwaysRun = true)
    public void validateProjectTearDown() throws JSONException {
        //it is necessary to login admin to validate project on afterClass
        logout();
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

        if (validateAfterClass) {
            System.out.println("Going to validate project after tests...");
            // TODO remove when ATP-1520, ATP-1519, ATP-1822 are fixed
            String testName = this.getClass().getSimpleName();
            if (!canAccessGreyPage(browser) || testName.contains("Coupa") || testName.contains("Pardot")
                    ||testName.contains("Zendesk4")) {
                System.out.println("Validations are skipped for Coupa, Pardot and Zendesk4 projects"
                        + " or running in IE, Android and Safari");
                return;
            }
            assertEquals(validateProject(), "OK");
        } else {
            System.out.println("Validations were skipped at this test class...");
        }
    }

    @AfterClass(dependsOnMethods = {"validateProjectTearDown"}, alwaysRun = true)
    public void deleteProjectTearDown(ITestContext context) {
        if (testParams.isReuseProject()) {
            System.out.println("Project is being re-used and won't be deleted.");
            return;
        }
        System.out.println("Delete mode is set to " + testParams.getDeleteMode().toString());
        String projectId = testParams.getProjectId();
        if (projectId != null && projectId.length() > 0) {
            switch (testParams.getDeleteMode()) {
                case DELETE_ALWAYS:
                    System.out.println("Project will be deleted...");
                    deleteProject(projectId);
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (context.getFailedTests().size() == 0) {
                        System.out.println("Test was successful, project will be deleted...");
                        deleteProject(projectId);
                    } else {
                        System.out.println("Test wasn't successful, project won't be deleted...");
                    }
                    break;
                case DELETE_NEVER:
                    System.out.println("Delete mode set to NEVER, project won't be deleted...");
                    break;
            }
        } else {
            System.out.println("No project created -> no delete...");
        }
    }
}
