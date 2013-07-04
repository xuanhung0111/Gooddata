package com.gooddata.qa.graphene.connectors;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.greypages.connectors.ConnectorFragment;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

public class AbstractConnectorsCheckTest extends AbstractTest {
	
	protected static final String PAGE_GDC_CONNECTORS = "gdc/projects/${projectId}/connectors";
	protected static final String PAGE_GDC_CONNECTORS_INTEGRATION = PAGE_GDC_CONNECTORS + "/${connectorType}/integration";
	protected static final String PAGE_GDC_CONNECTORS_INTEGRATION_PROCESSES = PAGE_GDC_CONNECTORS_INTEGRATION + "/processes";
	
	protected static final By BY_GP_CONFIG_LINK = By.partialLinkText("config");
	protected static final By BY_GP_SETTINGS_LINK = By.partialLinkText("settings");
	
	private static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes
	private static final int DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT = 180; // 15 minutes
	
	protected int projectCheckLimit = DEFAULT_PROJECT_CHECK_LIMIT;
	protected int integrationProcessCheckLimit = DEFAULT_INTEGRATION_PROCESS_CHECK_LIMIT;
	
	protected boolean integrationActivated = false;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "gdc";
	}
	
	public void initProject(String projectTitle, Connectors connectorType, int checkIterations) throws JSONException, InterruptedException {
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(BY_GP_FORM);
		ProjectFragment project = Graphene.createPageFragment(ProjectFragment.class, browser.findElement(BY_GP_FORM));
		projectId = project.createProject(projectTitle, "", connectorType.getTemplate(), authorizationToken, projectCheckLimit);
		System.out.println("Project with ID enabled: " + projectId);
	}
	
	public void initIntegration(Connectors connectorType) throws JSONException, InterruptedException {
		browser.get(getRootUrl() + PAGE_GDC_CONNECTORS.replace("${projectId}", projectId) + "/" + connectorType.getConnectorId());
		waitForElementPresent(BY_GP_PRE_JSON);
		ConnectorFragment connector = Graphene.createPageFragment(ConnectorFragment.class, browser.findElement(BY_GP_FORM));
		connector.createIntegration(connectorType.getTemplate());
		integrationActivated = true;
	}
	
	public String gotoIntegrationSettings() {
		waitForElementVisible(BY_GP_CONFIG_LINK);
		Graphene.guardHttp(browser.findElement(BY_GP_CONFIG_LINK)).click();
		waitForElementVisible(BY_GP_SETTINGS_LINK);
		Graphene.guardHttp(browser.findElement(BY_GP_SETTINGS_LINK)).click();
		waitForElementVisible(BY_GP_FORM);
		return browser.getCurrentUrl();
	}
	
	protected void scheduleIntegrationProcess(Connectors connectoryType, int checkIterations) throws JSONException, InterruptedException {
		browser.get(getRootUrl() + PAGE_GDC_CONNECTORS_INTEGRATION_PROCESSES.replace("${projectId}", projectId).replace("${connectorType}", connectoryType.getConnectorId()));
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 0, "There are no processes for new project yet");
		Graphene.guardHttp(browser.findElement(BY_GP_BUTTON_SUBMIT)).click();
		
		waitForIntegrationProcessSynchronized(browser, connectoryType, checkIterations);
	}
	
	protected void waitForIntegrationProcessSynchronized(WebDriver browser, Connectors connectorType, int checkIterations) throws JSONException, InterruptedException {
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
		browser.get(getRootUrl() + PAGE_GDC_CONNECTORS_INTEGRATION_PROCESSES.replace("${projectId}", projectId).replace("${connectorType}", connectorType.getConnectorId()));
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 1, "There is one finished process");
	}
	
	private String getProcessStatus(WebDriver browser) throws JSONException {
		JSONObject json = loadJSON();
		return json.getJSONObject("process").getJSONObject("status").getString("code");
	}
	
	protected void disableIntegration(Connectors connectorType) throws JSONException {
		if (integrationActivated) {
			browser.get(getRootUrl() + PAGE_GDC_CONNECTORS_INTEGRATION.replace("${projectId}", projectId).replace("${connectorType}", connectorType.getConnectorId()));
			waitForElementVisible(BY_GP_FORM);
			ConnectorFragment integration = Graphene.createPageFragment(ConnectorFragment.class, browser.findElement(BY_GP_FORM));
			integration.disableIntegration();
		} else {
			System.out.println("Integration wasn't created - nothing to disable...");
		}
	}

}
