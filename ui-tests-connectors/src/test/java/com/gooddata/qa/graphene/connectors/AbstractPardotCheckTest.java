package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public abstract class AbstractPardotCheckTest extends AbstractConnectorsCheckTest {

	protected static final By BY_INPUT_PARDOT_ACCOUNT_ID = By.name("accountId");
	
	protected String pardotAccountId;
	protected String pardotUploadUser;
	protected String pardotUploadUserPassword;
	
	@BeforeClass
	public void setCheckLimits() {
		projectCheckLimit = 120;
		integrationProcessCheckLimit = 240;
	}
	
	@BeforeClass
	public void loadRequiredProperties() {
		pardotAccountId = loadProperty("connectors.pardot.accountId");
		pardotUploadUser = loadProperty("connectors.pardot.uploadUser");
		pardotUploadUserPassword = loadProperty("connectors.pardot.uploadUserPassword");
		
		expectedDashboardTabs = new String[]{
				"Marketing KPIs", "Contribution", "Prospects", "Opportunities", "and more"
		};
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testConnectorIntegrationResource" })
	public void testPardotIntegrationConfiguration() throws InterruptedException, JSONException {
		openUrl(getIntegrationUri());
		// go to page with integration settings
		String settingsUrl = gotoIntegrationSettings();

		// pardot specific configuration of API Url (with specific upload user)
		signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
		browser.get(settingsUrl);
		waitForElementVisible(BY_INPUT_PARDOT_ACCOUNT_ID).sendKeys(pardotAccountId);
		Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT)).click();
		JSONObject json = loadJSON();
		assertEquals(json.getJSONObject("settings").getString("accountId"), pardotAccountId, "Pardot accountId was not set to expected value");
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testPardotIntegrationConfiguration" })
	public void testPardotIntegration() throws InterruptedException, JSONException {
		// sign in back with demo user
		validSignInWithDemoUser(true);
		// process schedule
		scheduleIntegrationProcess(integrationProcessCheckLimit);
	}
}
