package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

import static org.testng.Assert.*;

@Test(groups = { "connectors", "zendesk3" }, description = "Checklist tests for Zendesk3 connector in GD platform")
public class Zendesk3CheckTest extends AbstractConnectorsCheckTest {
	
	private String zendeskUploadUser;
	private String zendeskUploadUserPassword;
	private String zendeskApiUrl;
	
	private static final By BY_INPUT_API_URL = By.name("apiUrl");
	
	@BeforeClass
	public void loadRequiredProperties() {
		zendeskApiUrl = loadProperty("connectors.zendesk.apiUrl");
		zendeskUploadUser = loadProperty("connectors.zendesk.uploadUser");
		zendeskUploadUserPassword = loadProperty("connectors.zendesk.uploadUserPassword");
		
		connectorType = Connectors.ZENDESK3;
		expectedDashboardTabs = new String[]{
				"Overview", "Ticket Creation", "Ticket Distribution", "Performance", "Backlog", "Open Issues", "Customer Satisfaction"
		};
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testConnectorIntegrationResource" })
	public void testZendesk3IntegrationConfiguration() throws InterruptedException, JSONException {
		openUrl(getIntegrationUri());
		// go to page with integration settings
		String settingsUrl = gotoIntegrationSettings();
		
		// zendesk3 specific configuration of API Url (with specific upload user)
		signInAtGreyPages(zendeskUploadUser, zendeskUploadUserPassword);
		browser.get(settingsUrl);
		waitForElementPresent(BY_INPUT_API_URL).sendKeys(zendeskApiUrl);
		Graphene.guardHttp(waitForElementPresent(BY_GP_BUTTON_SUBMIT)).click();
		JSONObject json = loadJSON();
		assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendeskApiUrl, "Zendesk3 API URL was not set to expected value");
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testZendesk3IntegrationConfiguration" })
	public void testZendesk3Integration() throws InterruptedException, JSONException {
		// sign in back with demo user
		validSignInWithDemoUser(true);
		// process schedule
		scheduleIntegrationProcess(integrationProcessCheckLimit);
	}
}
