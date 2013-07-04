package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.greypages.connectors.CoupaInstanceFragment;

@Test(groups = { "connectors", "coupa" }, description = "Checklist tests for Coupa connector in GD platform")
public class CoupaCheckTest extends AbstractConnectorsCheckTest {
	
	private static final By BY_INPUT_TIMEZONE = By.name("timeZone");
	private static final By BY_GP_LINK_INSTANCES = By.partialLinkText("instances");
	
	private static final By BY_DIV_BEFORE_CONFIG = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()=\"Oops, there's information missing.\"]");
	private static final By BY_DIV_SYNCHRONIZATION_PROGRESS = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()='Your Coupa Optimizer project is almost ready!']");
	
	private static final String COUPA_INTEGRATION_TIMEZONE = "Europe/Prague";
	
	private String coupaInstanceApiUrl;
	private String coupaInstanceApiKey;
	
	private static final String[] expectedCoupaTabs = {
		"KPIs", "Requisitions", "Approvals", "Purchase Orders", "Suppliers", "Invoices", "Commodities", "Contracts", "Expenses", "Budgets", "All Spend"
	};
	
	@BeforeClass
	public void setCheckLimits() {
		projectCheckLimit = 120;
		integrationProcessCheckLimit = 720;
	}
	
	@BeforeClass
	public void loadRequiredProperties() {
		coupaInstanceApiUrl = loadProperty("connectors.coupa.instance.apiUrl");
		coupaInstanceApiKey = loadProperty("connectors.coupa.instance.apiKey");
	}

	@Test(groups = { "coupaBasicWalkthrough" })
	public void gd_Connectors_CP_001_PrepareProjectFromTemplate() 
			throws InterruptedException, JSONException {
		// sign in with demo user
		validSignInWithDemoUser(true);
		
		// create connector project
		initProject("CoupaCheckConnector", Connectors.COUPA, projectCheckLimit);

		// create integration
		initIntegration(Connectors.COUPA);
		
		// verify empty Coupa dashboard
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId);
		waitForElementVisible(BY_IFRAME);
		browser.switchTo().frame(browser.findElement(BY_IFRAME));
		waitForElementVisible(BY_DIV_BEFORE_CONFIG);

		// go to page with integration settings
		browser.get(getRootUrl() + PAGE_GDC_CONNECTORS_INTEGRATION.replace("${projectId}", projectId).replace("${connectorType}", Connectors.COUPA.getConnectorId()));
		gotoIntegrationSettings();

		// coupa specific configuration
		waitForElementVisible(BY_INPUT_TIMEZONE);
		browser.findElement(BY_INPUT_TIMEZONE).sendKeys(COUPA_INTEGRATION_TIMEZONE);
		Graphene.guardHttp(browser.findElement(BY_GP_BUTTON_SUBMIT)).click();
		Graphene.waitGui().until().element(BY_INPUT_TIMEZONE).value().equalTo(COUPA_INTEGRATION_TIMEZONE);
		waitForElementVisible(BY_GP_LINK_INSTANCES);
		Graphene.guardHttp(browser.findElement(BY_GP_LINK_INSTANCES)).click();
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("coupaInstances").getJSONArray("items").length() == 0, "There are no coupa instances for new project yet");
		
		// create coupa instance
		CoupaInstanceFragment coupaInstance = Graphene.createPageFragment(CoupaInstanceFragment.class, browser.findElement(BY_GP_FORM));
		coupaInstance.createCoupaInstance(Connectors.COUPA.getConnectorId(), coupaInstanceApiUrl, coupaInstanceApiKey);
		
		// verify progress on Coupa dashboard
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId);
		waitForElementVisible(BY_IFRAME);
		browser.switchTo().frame(browser.findElement(BY_IFRAME));
		waitForElementVisible(BY_DIV_SYNCHRONIZATION_PROGRESS);
		
		// process schedule
		scheduleIntegrationProcess(Connectors.COUPA, integrationProcessCheckLimit);

		// verify created project and count dashboard tabs
		verifyProjectDashboardTabs(expectedCoupaTabs.length, expectedCoupaTabs, true);
	}
	
	@Test(dependsOnGroups = { "coupaBasicWalkthrough" }, alwaysRun = true)
	public void disableConnectorIntegration() throws JSONException {
		disableIntegration(Connectors.COUPA);
	}
}
