package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;


@Test(groups = { "connectors", "brightidea" }, description = "Checklist tests for Brightidea connector in GD platform")
public class BrightideaCheckTest extends AbstractConnectorsCheckTest {
	
	private String brightideaApiKey;
	private String brightideaAffiliateId;
	private String brightideaHostname;
	
	private static final String BRIGHTIDEA_TIMEZONE = "Europe/Prague";
	
	private static final By BY_INPUT_API_KEY = By.xpath("//div/label[text()='API key']/../input");
	private static final By BY_INPUT_AFFILIATE_ID = By.xpath("//div/label[text()='Affiliate ID']/../input");
	private static final By BY_INPUT_HOSTNAME = By.xpath("//div/label[text()='Hostname']/../input");
	private static final By BY_SELECT_TIMEZONE = By.xpath("//div/label[text()='Timezone']/../select");
	private static final By BY_SELECT_TIMEZONE_OPTION = By.xpath("//option[@value='" + BRIGHTIDEA_TIMEZONE + "']");
	private static final By BY_FINISH_BUTTON = By.xpath("//button[text()='Finish']");
	
	private static final By BY_SPAN_WELCOME_BEFORE_CONFIG = By.xpath("//span[text()='Welcome to GoodData for Brightidea!']");
	private static final By BY_SPAN_SYNCHRONIZATION_PROGRESS = By.xpath("//span[text()='Almost There!']");
	
	private static final String[] expectedBrightideaTabs = {
		"WebStorms", "Ideas", "Users", "Switchboard", "Pipeline", "Headlines", "Learn More"
	};
	
	@BeforeClass
	public void setCheckLimits() {
		projectCheckLimit = 120;
		integrationProcessCheckLimit = 720;
	}
	
	@BeforeClass
	public void loadRequiredProperties() {
		brightideaApiKey = loadProperty("connectors.brightidea.apiKey");
		brightideaAffiliateId = loadProperty("connectors.brightidea.affiliateId");
		brightideaHostname = loadProperty("connectors.brightidea.hostname");
	}
	
	@Test(groups = {"brightideaWalkthrough"})
	public void gd_Connectors_BI_001_PrepareProjectFromTemplate() throws InterruptedException, JSONException {
		// sign in with demo user
		validSignInWithDemoUser(true);
		
		// create connector project
		initProject("BrightideaCheckConnector", Connectors.BRIGHTIDEA, projectCheckLimit);
		
		// create integration
		initIntegration(Connectors.BRIGHTIDEA);
		
		// Brightidea specific configuration of integration (tfue page)
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId);
		waitForElementVisible(BY_IFRAME);
		browser.switchTo().frame(browser.findElement(BY_IFRAME));
		waitForElementVisible(BY_SPAN_WELCOME_BEFORE_CONFIG);
		waitForElementVisible(BY_INPUT_API_KEY);
		browser.findElement(BY_INPUT_API_KEY).sendKeys(brightideaApiKey);
		browser.findElement(BY_INPUT_AFFILIATE_ID).sendKeys(brightideaAffiliateId);
		browser.findElement(BY_INPUT_HOSTNAME).clear();
		browser.findElement(BY_INPUT_HOSTNAME).sendKeys(brightideaHostname);
		WebElement select = browser.findElement(BY_SELECT_TIMEZONE);
		WebElement option = select.findElement(BY_SELECT_TIMEZONE_OPTION);
		option.click();
		browser.findElement(BY_FINISH_BUTTON).click();
		waitForElementVisible(BY_SPAN_SYNCHRONIZATION_PROGRESS);
		// process is scheduled automatically - check status
		browser.get(getRootUrl() + PAGE_GDC_CONNECTORS_INTEGRATION_PROCESSES.replace("${projectId}", projectId).replace("${connectorType}", Connectors.BRIGHTIDEA.getConnectorId()));
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 1, "Integration process wasn't started...");
		waitForElementVisible(BY_GP_LINK);
		Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
		waitForIntegrationProcessSynchronized(browser, Connectors.BRIGHTIDEA, integrationProcessCheckLimit);
		
		// verify created project and count dashboard tabs
		verifyProjectDashboardTabs(expectedBrightideaTabs.length, expectedBrightideaTabs, true);		
	}
	
	@Test(dependsOnGroups = { "brightideaWalkthrough" }, alwaysRun = true)
	public void disableConnectorIntegration() throws JSONException {
		disableIntegration(Connectors.BRIGHTIDEA);
	}
}
