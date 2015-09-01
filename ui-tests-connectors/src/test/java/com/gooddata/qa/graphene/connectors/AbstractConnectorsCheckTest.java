package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.fragments.greypages.connectors.ConnectorFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.Connectors;

import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

public abstract class AbstractConnectorsCheckTest extends AbstractProjectTest {

    protected static final String PAGE_GDC_CONNECTORS = "gdc/projects/${projectId}/connectors";

    protected static final By BY_GP_CONFIG_LINK = By.partialLinkText("config");
    protected static final By BY_GP_SETTINGS_LINK = By.partialLinkText("settings");

    private static final int DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT = 180; // 15 minutes

    private static final String PROCESS_FULL_LOAD_JSON = "{\"process\":{\"incremental\":false}}";
    public static final String ANOTHER_PROCESS_IS_ALREADY_RUNNING = "Another process is already running.";

    protected int integrationProcessCheckLimit = DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT;

    protected boolean integrationActivated = false;

    protected Connectors connectorType;

    protected Map<String, String[]> expectedDashboardsAndTabs;

    @FindBy(tagName = "form")
    protected ConnectorFragment connector;

    @BeforeClass(dependsOnMethods = {"loadRequiredProperties"})
    public void initProperties() {
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
        projectTitle = connectorType.getName() + "Connector-test";
        String projectTemplateOverride = System.getProperty("projectTemplate");
        if (projectTemplateOverride != null && !projectTemplateOverride.isEmpty()) {
            projectTemplate = projectTemplateOverride;
        } else {
            projectTemplate = connectorType.getTemplate();
        }
        System.out.println(String.format("Template %s will be for project creation...", projectTemplate));
        projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;
    }

    /**
     * ------------- Shared test methods -----------
     */

    @Test(groups = {"connectorInit"}, dependsOnMethods = {"createProject"})
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

    @Test(groups = {"connectorBasicREST"}, dependsOnGroups = {"connectorIntegration"})
    public void testConnectorProcessesResource() throws JSONException {
        openUrl(getProcessesUri());
        verifyProcessesResourceJSON();
    }

    @Test(groups = {"connectorWalkthrough"}, dependsOnMethods = {"testConnectorProcessesResource"})
    public void verifyProjectDashboards() {
        // verify created project and count dashboard tabs
        verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, true);
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

    public void initIntegration() throws JSONException {
        openUrl(getConnectorUri());
        waitForElementPresent(BY_GP_PRE_JSON, browser);
        waitForElementPresent(connector.getRoot());
        connector.createIntegration(projectTemplate);
        integrationActivated = true;
    }

    public String gotoIntegrationSettings() {
        Graphene.guardHttp(waitForElementVisible(BY_GP_CONFIG_LINK, browser)).click();
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK, browser)).click();
        waitForElementVisible(BY_GP_FORM, browser);
        return browser.getCurrentUrl();
    }

    protected void scheduleIntegrationProcess(int checkIterations, int expectedProcessesCount)
            throws JSONException {
        openUrl(getProcessesUri());
        JSONObject json = loadJSON();
        if (!testParams.isReuseProject()) {
            assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == expectedProcessesCount,
                    "There are no processes for new project yet");
        }
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT, browser)).click();

        waitForIntegrationProcessSynchronized(browser, checkIterations);
    }

    /**
     * Schedules (starts) new integration process over gray pages and waits till it's synchronized (finished).
     * It also handles situation when process has already been started by scheduler and waits for this one.
     *
     * @param checkIterations how many iterations of checks should it do before it fails (there's a 5s pause between checks)
     * @throws JSONException
     */
    protected void scheduleIntegrationProcess(int checkIterations) throws JSONException {
        openUrl(getProcessesUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT, browser)).click();

        handleAlreadyRunningProcess();

        waitForIntegrationProcessSynchronized(browser, checkIterations);
    }

    private void handleAlreadyRunningProcess() throws JSONException {
        final JSONObject afterClick = loadJSON();
        if (afterClick.has("error") &&
                ANOTHER_PROCESS_IS_ALREADY_RUNNING.equals(afterClick.getJSONObject("error").getString("message"))) {
            openUrl(getProcessesUri());
            final JSONObject processes = loadJSON();
            final String processLink = processes.getJSONObject("processes").getJSONArray("items").getJSONObject(0)
                    .getJSONObject("process").getJSONObject("links").getString("self");
            openUrl(processLink);
        }
    }

    protected void waitForIntegrationProcessSynchronized(WebDriver browser, int checkIterations)
            throws JSONException {
        String processUrl = browser.getCurrentUrl();
        System.out.println("Waiting for process synchronized: " + processUrl);
        int i = 0;
        String status = getProcessStatus();
        while (!"SYNCHRONIZED".equals(status) && i < checkIterations) {
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

    private String getProcessStatus() throws JSONException {
        JSONObject json = loadJSON();
        return json.getJSONObject("process").getJSONObject("status").getString("code");
    }

    protected String getConnectorUri() {
        return PAGE_GDC_CONNECTORS.replace("${projectId}", testParams.getProjectId()) + "/" + connectorType.getConnectorId();
    }

    protected String getIntegrationUri() {
        return getConnectorUri() + "/integration";
    }

    protected String getProcessesUri() {
        return getIntegrationUri() + "/processes";
    }

    protected void verifyConnectorResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertTrue(json.has("connector"), "connector object is missing");

        JSONObject connector = json.getJSONObject("connector");
        assertEquals(connector.getString("connectorId"), connectorType.getConnectorId(), "connectorId");
        assertTrue(connector.has("links"), "links object is missing");

        JSONObject links = connector.getJSONObject("links");
        assertEquals(links.getString("integration"), "/" + getIntegrationUri(), "integration link");
    }

    protected void verifyIntegrationResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertTrue(json.has("integration"), "integration object is missing");

        JSONObject integration = json.getJSONObject("integration");
        assertEquals(integration.getBoolean("active"), true, "active integration");
        assertEquals(integration.getString("projectTemplate"), projectTemplate, "project template");
        assertTrue(integration.has("lastFinishedProcess"), "lastFinishedProcess key is missing");
        assertTrue(integration.has("lastSuccessfulProcess"), "lastSuccessfulProcess key is missing");
        assertTrue(integration.has("runningProcess"), "runningProcess key is missing");
        assertTrue(integration.has("links"), "links object is missing");

        JSONObject links = integration.getJSONObject("links");
        assertEquals(links.getString("self"), "/" + getIntegrationUri(), "self link");
        assertEquals(links.getString("processes"), "/" + getProcessesUri(), "processes link");
    }

    protected void verifyProcessesResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertTrue(json.has("processes"), "processes object is missing");

        JSONObject processes = json.getJSONObject("processes");
        assertTrue(processes.has("items"), "items array is missing");
        JSONArray items = processes.getJSONArray("items");
        assertTrue(items.length() > 0, "at least one process should exist");
        assertTrue(items.getJSONObject(0).has("process"), "process object is missing");

        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        assertTrue(process.has("started"), "started key is missing");
        assertTrue(process.has("finished"), "finished key is missing");
        assertTrue(process.getJSONObject("status").has("detail"), "detail key is missing");
        assertTrue(process.getJSONObject("status").has("description"), "description key is missing");
        assertTrue(process.getJSONObject("status").has("code"), "status code is missing");
        assertTrue(process.getJSONObject("links").getString("self").startsWith("/" + getProcessesUri()),
                "self link is incorrect");
    }

    protected void runConnectorProjectFullLoad() throws JSONException, IOException {
        getRestApiClient();
        HttpRequestBase postRequest = restApiClient.newPostMethod("/" + getProcessesUri(), PROCESS_FULL_LOAD_JSON);
        HttpResponse postResponse = restApiClient.execute(postRequest);
        assertEquals(postResponse.getStatusLine().getStatusCode(), 201, "Invalid return code when running connector full load");
        JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
        browser.get(getBasicRootUrl() + json.getString("uri"));
        waitForIntegrationProcessSynchronized(browser, integrationProcessCheckLimit);
    }
}
