package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.TemplateAbstractTest;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.greypages.connectors.ConnectorFragment;
import com.gooddata.qa.utils.http.RestUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.springframework.http.HttpStatus;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public abstract class AbstractConnectorsCheckTest extends TemplateAbstractTest {

    private static final String PAGE_GDC_CONNECTORS = "gdc/projects/${projectId}/connectors";

    private static final By BY_GP_CONFIG_LINK = By.partialLinkText("config");
    protected static final By BY_GP_SETTINGS_LINK = By.partialLinkText("settings");

    private static final int DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT = 180; // 15 minutes

    private static final String PROCESS_FULL_LOAD_JSON = "{\"process\":{\"incremental\":false}}";
    private static final String ANOTHER_PROCESS_IS_ALREADY_RUNNING = "Another process is already running.";

    protected int integrationProcessCheckLimit = DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT;

    private boolean integrationActivated = false;

    protected Connectors connectorType;

    @FindBy(tagName = "form")
    private ConnectorFragment connector;

    @Override
    protected void initProperties() {
        projectTemplate = connectorType.getTemplate();
        projectTitle = connectorType.getName() + "Connector-test";
        System.out.println(String.format("Template %s will be for project creation...", projectTemplate));
        projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;
    }

    @Override
    public void configureStartPage() {
        startPageContext = new StartPageContext() {
            
            @Override
            public void waitForStartPageLoaded() {
                waitForElementVisible(By.className("param"),browser);
            }
            
            @Override
            public String getStartPage() {
                return "gdc";
            }
        };
    }

    /**
     * ------------- Shared test methods -----------
     */

    @Test(groups = {"connectorInit"}, dependsOnGroups = {"createProject"})
    public void createIntegration() throws JSONException {
        // verify that connector resource exist
        openUrl(getConnectorUri());
        verifyConnectorResourceJSON();
        if (!testParams.isReuseProject()) {
            // create integration
            initIntegration();
        }
    }

    @Test(groups = {"connectorBasicREST"}, dependsOnGroups = {"connectorInit"})
    public void testConnectorIntegrationResource() throws JSONException {
        openUrl(getIntegrationUri());
        verifyIntegrationResourceJSON();
    }

    @Test(dependsOnGroups = {"connectorWalkthrough"}, alwaysRun = true)
    public void disableConnectorIntegration() throws JSONException {
        if (integrationActivated) {
            openUrl(getIntegrationUri());
            waitForElementVisible(connector.getRoot());
            connector.disableIntegration();
        } else {
            System.out.println("Integration wasn't created - nothing to disable...");
        }
    }

    @Test(dependsOnMethods = {"disableConnectorIntegration"}, alwaysRun = true)
    public void deleteConnectorIntegration(ITestContext context) throws JSONException {
        if (integrationActivated) {
            openUrl(getIntegrationUri());
            waitForElementVisible(connector.getRoot());
            System.out.println("Delete mode is set to " + testParams.getDeleteMode().toString());
            switch (testParams.getDeleteMode()) {
                case DELETE_ALWAYS:
                    System.out.println("Integration will be deleted...");
                    connector.deleteIntegration();
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (context.getFailedTests().size() == 0) {
                        System.out.println("Test was successful, integration will be deleted...");
                        connector.deleteIntegration();
                    } else {
                        System.out.println("Test wasn't successful, integration won't be deleted...");
                    }
                    break;
                case DELETE_NEVER:
                    System.out.println("Delete mode set to NEVER, integration won't be deleted...");
                    break;
            }
        } else {
            System.out.println("Integration wasn't created - nothing to delete...");
        }
    }

    /**
     * ------------------------
     */

    private void initIntegration() throws JSONException {
        openUrl(getConnectorUri());
        waitForElementPresent(BY_GP_PRE_JSON, browser);
        waitForElementPresent(connector.getRoot());
        connector.createIntegration(projectTemplate);
        integrationActivated = true;
    }

    protected String gotoIntegrationSettings() {
        Graphene.guardHttp(waitForElementVisible(BY_GP_CONFIG_LINK, browser)).click();
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK, browser)).click();
        waitForElementVisible(BY_GP_FORM, browser);
        return browser.getCurrentUrl();
    }

    /**
     * Schedules (starts) new integration process over gray pages and waits till it's synchronized (finished).
     * It waits for any already existing running processes and then tries to schedule fresh one. This step is repeated
     * until fresh process is created by this test.
     */
    protected void scheduleIntegrationProcess(int checkIterations) throws JSONException {
        while (!scheduleIntegrationProcessOrUseExisting(checkIterations)) {
            // do nothing
        }
    }

    /**
     * Schedules (starts) new integration process over gray pages and waits till it's synchronized (finished).
     * It also handles situation when process has already been started by scheduler and waits for this one.
     *
     * @param checkIterations how many iterations of checks should it do before it fails (there's a 5s pause between checks)
     * @return <code>true</code> if new process has been scheduled, <code>false</code> if existing one has been used
     * @throws JSONException If there is a syntax error in the JSON string or if there is a problem with JSON parsing.
     */
    protected boolean scheduleIntegrationProcessOrUseExisting(int checkIterations) throws JSONException {
        openUrl(getProcessesUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT, browser)).click();

        final boolean alreadyRunningProcess = handleAlreadyRunningProcess();

        waitForIntegrationProcessSynchronized(browser, checkIterations);
        return !alreadyRunningProcess;
    }

    /**
     * Returns <code>true</code> if there was running process, <code>false</code> otherwise
     */
    private boolean handleAlreadyRunningProcess() throws JSONException {
        final JSONObject afterClick = loadJSON();
        if (afterClick.has("error") &&
                ANOTHER_PROCESS_IS_ALREADY_RUNNING.equals(afterClick.getJSONObject("error").getString("message"))) {
            openUrl(getProcessesUri());
            final JSONObject processes = loadJSON();
            final String processLink = processes.getJSONObject("processes").getJSONArray("items").getJSONObject(0)
                    .getJSONObject("process").getJSONObject("links").getString("self");
            openUrl(processLink);
            return true;
        }
        return false;
    }

    private void waitForIntegrationProcessSynchronized(WebDriver browser, int checkIterations)
            throws JSONException {
        String processUrl = browser.getCurrentUrl();
        System.out.println("Waiting for process synchronized: " + processUrl);
        int i = 0;
        String status = getProcessStatus();
        while (!isFinalStatus(status) && i < checkIterations) {
            System.out.println("Current process status is: " + status);
            assertNotEquals(status, "ERROR", "Error status appeared");
            sleepTightInSeconds(5);
            browser.get(processUrl);
            status = getProcessStatus();
            i++;
        }
        assertEquals(status, "SYNCHRONIZED", "Process is synchronized");
        System.out.println("Integration was synchronized at +- " + (i * 5) + "seconds");
    }

    private boolean isFinalStatus(final String status) {
        return
                "SYNCHRONIZED".equals(status) ||
                "ERROR".equals(status) ||
                "USER_ERROR".equals(status);
    }

    private String getProcessStatus() throws JSONException {
        JSONObject json = loadJSON();
        return json.getJSONObject("process").getJSONObject("status").getString("code");
    }

    private String getConnectorUri() {
        return PAGE_GDC_CONNECTORS.replace("${projectId}", testParams.getProjectId()) + "/" + connectorType.getConnectorId();
    }

    protected String getIntegrationUri() {
        return getConnectorUri() + "/integration";
    }

    protected String getProcessesUri() {
        return getIntegrationUri() + "/processes";
    }

    private void verifyConnectorResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertThat("Connector resource JSON " + json.toString() + " contains 'connector' key",
                json.has("connector"),
                is(true));

        JSONObject connector = json.getJSONObject("connector");
        assertThat("Connector resource JSON contains correct connectorId",
                connector.getString("connectorId"),
                is(connectorType.getConnectorId()));

        assertThat("Connector resource JSON " + json.toString() + " contains 'links' key",
                connector.has("links"),
                is(true));

        JSONObject links = connector.getJSONObject("links");
        assertThat("Connector resource JSON contains correct integration link",
                links.getString("integration"),
                is("/" + getIntegrationUri()));
    }

    private void verifyIntegrationResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertThat("Integration resource JSON " + json.toString() + " contains 'integration' key",
                json.has("integration"),
                is(true));

        JSONObject integration = json.getJSONObject("integration");
        assertThat("Integration is active", integration.getBoolean("active"), is(true));
        assertThat("Integration has correct template", integration.getString("projectTemplate"), is(projectTemplate));
        assertThat("Integration resource JSON " + json.toString() + " contains 'lastFinishedProcess' key",
                integration.has("lastFinishedProcess"),
                is(true));
        assertThat("Integration resource JSON " + json.toString() + " contains 'lastSuccessfulProcess' key",
                integration.has("lastSuccessfulProcess"),
                is(true));
        assertThat("Integration resource JSON " + json.toString() + " contains 'runningProcess' key",
                integration.has("runningProcess"),
                is(true));
        assertThat("Integration resource JSON " + json.toString() + " contains 'links' key",
                integration.has("links"),
                is(true));

        JSONObject links = integration.getJSONObject("links");
        assertThat("Integration resource JSON contains correct self link",
                links.getString("self"),
                is("/" + getIntegrationUri()));
        assertThat("Integration resource JSON contains correct processes link",
                links.getString("processes"),
                is("/" + getProcessesUri()));
    }

    protected void runConnectorProjectFullLoad() throws JSONException, IOException {
        final JSONObject json = RestUtils.getJsonObject(getRestApiClient(),
                getRestApiClient().newPostMethod("/" + getProcessesUri(), PROCESS_FULL_LOAD_JSON),
                HttpStatus.CREATED);
        browser.get(getBasicRootUrl() + json.getString("uri"));
        waitForIntegrationProcessSynchronized(browser, integrationProcessCheckLimit);
    }
}
