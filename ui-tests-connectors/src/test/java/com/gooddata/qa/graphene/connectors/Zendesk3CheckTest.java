package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "zendesk3" }, description = "Checklist tests for Zendesk3 connector in GD platform")
public class Zendesk3CheckTest extends AbstractConnectorsCheckTest {
	
	private String zendesk3UploadUser;
	private String zendesk3UploadUserPassword;
	private String zendesk3ApiUrl;
	
	private static final By BY_INPUT_API_URL = By.name("apiUrl");
	
	private static final String[] expectedZendeskTabs = {
		"Overview", "Ticket Creation", "Ticket Distribution", "Performance", "Backlog", "Open Issues", "Customer Satisfaction"
	};	
	
	@BeforeClass
	public void loadRequiredProperties() {
		zendesk3ApiUrl = loadProperty("connectors.zendesk3.apiUrl");
		zendesk3UploadUser = loadProperty("connectors.zendesk3.uploadUser");
		zendesk3UploadUserPassword = loadProperty("connectors.zendesk3.uploadUserPassword");
	}
	
	@Test(groups = {"zendesk3BasicWalkthrough"})
	public void gd_Connectors_ZD_001_PrepareProjectFromTemplate() throws InterruptedException, JSONException {
		// sign in with demo user
		validSignInWithDemoUser(true);
		
		// create connector project
		initProject("Zendesk3CheckConnector", Connectors.ZENDESK3, projectCheckLimit);
		
		// create integration
		initIntegration(Connectors.ZENDESK3);
		
		// go to page with integration settings
		String settingsUrl = gotoIntegrationSettings();
		
		// zendesk3 specific configuration of API Url (with specific upload user)
		signInAtGreyPages(zendesk3UploadUser, zendesk3UploadUserPassword);
		browser.get(settingsUrl);
		waitForElementPresent(BY_INPUT_API_URL).sendKeys(zendesk3ApiUrl);
		Graphene.guardHttp(waitForElementPresent(BY_GP_BUTTON_SUBMIT)).click();
		JSONObject json = loadJSON();
		Assert.assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendesk3ApiUrl, "Zendesk3 API URL was not set to expected value");
		
		// sign in back with demo user
		validSignInWithDemoUser(true);
		
		// process schedule
		scheduleIntegrationProcess(Connectors.ZENDESK3, integrationProcessCheckLimit);
		
		// verify created project and count dashboard tabs
		verifyProjectDashboardTabs(true, expectedZendeskTabs.length, expectedZendeskTabs, true);
		successfulTest = true;
	}
	
	@Test(dependsOnGroups = { "zendesk3BasicWalkthrough" }, alwaysRun = true)
	public void disableConnectorIntegration() throws JSONException {
		disableIntegration(Connectors.ZENDESK3);
	}
	
	@Test(dependsOnMethods = { "disableConnectorIntegration"}, alwaysRun = true)
	public void deleteProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
