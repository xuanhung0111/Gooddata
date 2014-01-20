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
	
	private String zendesk3UploadUser;
	private String zendesk3UploadUserPassword;
	private String zendesk3ApiUrl;
	
	private static final By BY_INPUT_API_URL = By.name("apiUrl");
	
	@BeforeClass
	public void loadRequiredProperties() {
		zendesk3ApiUrl = loadProperty("connectors.zendesk3.apiUrl");
		zendesk3UploadUser = loadProperty("connectors.zendesk3.uploadUser");
		zendesk3UploadUserPassword = loadProperty("connectors.zendesk3.uploadUserPassword");
		
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
		signInAtGreyPages(zendesk3UploadUser, zendesk3UploadUserPassword);
		browser.get(settingsUrl);
		waitForElementPresent(BY_INPUT_API_URL).sendKeys(zendesk3ApiUrl);
		Graphene.guardHttp(waitForElementPresent(BY_GP_BUTTON_SUBMIT)).click();
		JSONObject json = loadJSON();
		assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendesk3ApiUrl, "Zendesk3 API URL was not set to expected value");
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testZendesk3IntegrationConfiguration" })
	public void testZendesk3Integration() throws InterruptedException, JSONException {
		// sign in back with demo user
		validSignInWithDemoUser(true);
		// process schedule
		scheduleIntegrationProcess(integrationProcessCheckLimit);
	}
}
