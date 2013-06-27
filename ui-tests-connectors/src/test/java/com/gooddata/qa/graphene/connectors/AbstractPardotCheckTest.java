package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

public abstract class AbstractPardotCheckTest extends AbstractConnectorsCheckTest {

	protected static final By BY_INPUT_PARDOT_ACCOUNT_ID = By.name("accountId");
	
	protected String pardotAccountId;
	protected String pardotUploadUser;
	protected String pardotUploadUserPassword;
	
	protected static final String[] expectedPardotTabs = {
		"Marketing KPIs", "Contribution", "Prospects", "Opportunities", "and more"
	};
	
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
	}

	protected void prepareProjectFromTemplate(String projectName, Connectors connectorType)
			throws InterruptedException, JSONException {
		// sign in with demo user
		validSignInWithDemoUser(true);
		
		// create connector project
		initProject(projectName, connectorType, projectCheckLimit);

		// create integration
		initIntegration(connectorType);

		// go to page with integration settings
		String settingsUrl = gotoIntegrationSettings();

		// pardot specific configuration of API Url (with specific upload user)
		signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
		browser.get(settingsUrl);
		waitForElementVisible(BY_INPUT_PARDOT_ACCOUNT_ID);
		browser.findElement(BY_INPUT_PARDOT_ACCOUNT_ID).sendKeys(pardotAccountId);
		Graphene.guardHttp(browser.findElement(BY_GP_BUTTON_SUBMIT)).click();
		JSONObject json = loadJSON();
		Assert.assertEquals(json.getJSONObject("settings").getString("accountId"), pardotAccountId, "Pardot accountId was not set to expected value");
		
		// sign in back with demo user
		validSignInWithDemoUser(true);
		
		// process schedule
		scheduleIntegrationProcess(connectorType, integrationProcessCheckLimit);

		// verify created project and count dashboard tabs
		verifyConnectorProjectDashboardTabs(expectedPardotTabs.length, expectedPardotTabs);
	}

}
