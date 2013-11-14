package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.greypages.connectors.ConnectorFragment;

public class AbstractConnectorsCheckTest extends AbstractTest {
	
	protected static final String PAGE_GDC_CONNECTORS = "gdc/projects/${projectId}/connectors";
	
	protected static final By BY_GP_CONFIG_LINK = By.partialLinkText("config");
	protected static final By BY_GP_SETTINGS_LINK = By.partialLinkText("settings");
	
	private static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes
	private static final int DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT = 180; // 15 minutes
	
	protected int projectCheckLimit = DEFAULT_PROJECT_CHECK_LIMIT;
	protected int integrationProcessCheckLimit = DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT;
	
	protected boolean integrationActivated = false;
	
	@FindBy(tagName="form")
	protected ConnectorFragment connector;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "gdc";
	}
	
	public void initProject(String projectTitle, final Connectors connectorType, int checkIterations) throws JSONException, InterruptedException {
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject(projectTitle, "", connectorType.getTemplate(), authorizationToken, projectCheckLimit);
		System.out.println("Project with ID enabled: " + projectId);
	}
	
	public void initIntegration(final Connectors connectorType) throws JSONException, InterruptedException {
		openUrl(getConnectorUri(connectorType));
		waitForElementPresent(BY_GP_PRE_JSON);
		waitForElementPresent(connector.getRoot());
		connector.createIntegration(connectorType.getTemplate());
		integrationActivated = true;
	}
	
	public String gotoIntegrationSettings() {
		Graphene.guardHttp(waitForElementVisible(BY_GP_CONFIG_LINK)).click();
		Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK)).click();
		waitForElementVisible(BY_GP_FORM);
		return browser.getCurrentUrl();
	}
	
	protected void scheduleIntegrationProcess(final Connectors connectorType, int checkIterations) throws JSONException, InterruptedException {
		openUrl(getProcessesUri(connectorType));
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 0, "There are no processes for new project yet");
		Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT)).click();
		
		waitForIntegrationProcessSynchronized(browser, connectorType, checkIterations);
	}
	
	protected void waitForIntegrationProcessSynchronized(WebDriver browser, final Connectors connectorType, int checkIterations) throws JSONException, InterruptedException {
		String processUrl = browser.getCurrentUrl();
		System.out.println("Waiting for process synchronized: " + processUrl);
		int i = 0;
		String status = getProcessStatus(browser);
		while (!"SYNCHRONIZED".equals(status) && i < checkIterations) {
			System.out.println("Current process status is: " + status);
			Assert.assertNotEquals(status, "ERROR", "Error status appeared");
			Thread.sleep(5000);
			browser.get(processUrl);
			status = getProcessStatus(browser);
			i++;
		}
		Assert.assertEquals(status, "SYNCHRONIZED", "Process is synchronized");
		System.out.println("Integration was synchronized at +- " + (i * 5) + "seconds");
		// may not be correct since another integration process can be scheduled automatically...
		browser.get(getRootUrl() + getProcessesUri(connectorType));
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 1, "There is one finished process");
	}
	
	private String getProcessStatus(WebDriver browser) throws JSONException {
		JSONObject json = loadJSON();
		return json.getJSONObject("process").getJSONObject("status").getString("code");
	}
	
	protected void disableIntegration(final Connectors connectorType) throws JSONException {
		if (integrationActivated) {
			openUrl(getIntegrationUri(connectorType));
			waitForElementVisible(connector.getRoot());
			connector.disableIntegration();
		} else {
			System.out.println("Integration wasn't created - nothing to disable...");
		}
	}

	protected String getConnectorUri(final Connectors connectorType) {
		return PAGE_GDC_CONNECTORS.replace("${projectId}", projectId) + "/" + connectorType.getConnectorId();
	}

	protected String getIntegrationUri(final Connectors connectorType) {
		return getConnectorUri(connectorType) + "/integration";
	}

	protected String getProcessesUri(final Connectors connectorType) {
		return getIntegrationUri(connectorType) + "/processes";
	}
	
	protected void verifyConnectorResourceJSON(final Connectors connectorType) throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("connector"), "connector object is missing");

        JSONObject connector = json.getJSONObject("connector");
        Assert.assertEquals(connector.getString("connectorId"), connectorType.getConnectorId(), "connectorId");
        Assert.assertTrue(connector.has("links"), "links object is missing");

        JSONObject links = connector.getJSONObject("links");
        Assert.assertEquals(links.getString("integration"), "/" + getIntegrationUri(connectorType), "integration link");
    }

	protected void verifyIntegrationResourceJSON(final Connectors connectorType) throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("integration"), "integration object is missing");

        JSONObject integration = json.getJSONObject("integration");
        Assert.assertEquals(integration.getBoolean("active"), true, "active integration");
        Assert.assertEquals(integration.getString("projectTemplate"), connectorType.getTemplate(), "project template");
        Assert.assertTrue(integration.has("lastFinishedProcess"), "lastFinishedProcess key is missing");
        Assert.assertTrue(integration.has("lastSuccessfulProcess"), "lastSuccessfulProcess key is missing");
        Assert.assertTrue(integration.has("runningProcess"), "runningProcess key is missing");
        Assert.assertTrue(integration.has("links"), "links object is missing");

        JSONObject links = integration.getJSONObject("links");
        Assert.assertEquals(links.getString("self"), "/" + getIntegrationUri(connectorType), "self link");
        Assert.assertEquals(links.getString("processes"), "/" + getProcessesUri(connectorType), "processes link");
    }
	
	protected void verifyProcessesResourceJSON(final Connectors connectorType) throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("processes"), "processes object is missing");

        JSONObject processes = json.getJSONObject("processes");
        Assert.assertTrue(processes.has("items"), "items array is missing");
        JSONArray items = processes.getJSONArray("items");
        Assert.assertTrue(items.length() > 0, "at least one process should exist");
        Assert.assertTrue(items.getJSONObject(0).has("process"), "process object is missing");

        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        Assert.assertTrue(process.has("started"), "started key is missing");
        Assert.assertTrue(process.has("finished"), "finished key is missing");
        Assert.assertTrue(process.getJSONObject("status").has("detail"), "detail key is missing");
        Assert.assertTrue(process.getJSONObject("status").has("description"), "description key is missing");
        Assert.assertTrue(process.getJSONObject("status").has("code"), "status code is missing");
        Assert.assertTrue(process.getJSONObject("links").getString("self").startsWith("/" + getProcessesUri(connectorType)), "self link is incorrect");
    }
}
